package app.snapshot_bitcake.av;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.ServentInfo;
import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.SnapshotCollector;
import servent.message.Message;
import servent.message.snapshot.av.AVDoneMessage;
import servent.message.snapshot.av.AVTerminateMessage;
import servent.message.snapshot.av.AVTokenMessage;
import servent.message.util.MessageUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class AVBitcakeManager implements BitcakeManager {
    private final AtomicInteger currentAmount = new AtomicInteger(1000);
    private Map<Integer, Integer> giveHistory = new ConcurrentHashMap<>();
    private Map<Integer, Integer> getHistory = new ConcurrentHashMap<>();
    private Map<Integer, Integer> tokenVectorClock = null;
    public int recordedAmount = 0;
    public int tokenInitiatorId;

    public AVBitcakeManager() {}

    public void tokenEvent(SnapshotCollector snapshotCollector) {
        Message avTokenMessageToMyself, avTokenMessageToNeighbor;
        tokenInitiatorId = AppConfig.myServentInfo.getId();

        synchronized (AppConfig.paranoidLock) {
            recordedAmount = getCurrentBitcakeAmount();
            Map<Integer, Integer> vectorClock = new ConcurrentHashMap<>(CausalBroadcastShared.getVectorClock());

            avTokenMessageToMyself = new AVTokenMessage(
                    AppConfig.myServentInfo,
                    AppConfig.myServentInfo,
                    AppConfig.myServentInfo,
                    vectorClock);

            CausalBroadcastShared.addPendingMessage(avTokenMessageToMyself);
            CausalBroadcastShared.checkPendingMessages(snapshotCollector);

            tokenVectorClock = new ConcurrentHashMap<>(CausalBroadcastShared.getVectorClock());

            for(Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                getHistory.put(neighbor, 0);
            }

            for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                avTokenMessageToNeighbor = new AVTokenMessage(
                        AppConfig.myServentInfo,
                        AppConfig.myServentInfo,
                        AppConfig.getInfoById(neighbor),
                        vectorClock);

                CausalBroadcastShared.commitCausalMessage(avTokenMessageToNeighbor, snapshotCollector);
                MessageUtil.sendMessage(avTokenMessageToNeighbor.changeReceiver(neighbor).makeMeASender());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void handleToken(Map<Integer, Integer> tokenVectorClock, ServentInfo collectorInfo, SnapshotCollector snapshotCollector){
        Message avDoneMessage;
        tokenInitiatorId = collectorInfo.getId();

        if(collectorInfo.getId() != AppConfig.myServentInfo.getId()){
            synchronized (AppConfig.paranoidLock) {
                recordedAmount = getCurrentBitcakeAmount();
                this.tokenVectorClock = tokenVectorClock;
                Map<Integer, Integer> vectorClock = new ConcurrentHashMap<>(CausalBroadcastShared.getVectorClock());

                for(Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                    getHistory.put(neighbor, 0);
                }

                for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                    avDoneMessage = new AVDoneMessage(
                            AppConfig.myServentInfo,
                            collectorInfo,
                            AppConfig.getInfoById(neighbor),
                            vectorClock);

                    CausalBroadcastShared.commitCausalMessage(avDoneMessage, snapshotCollector);
                    MessageUtil.sendMessage(avDoneMessage.changeReceiver(neighbor).makeMeASender());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void terminationEvent(SnapshotCollector snapshotCollector) {
        Message avTerminateMessageToMyself, avTerminateMessageToNeighbor;

        synchronized (AppConfig.paranoidLock) {
            Map<Integer, Integer> vectorClock = new ConcurrentHashMap<>(CausalBroadcastShared.getVectorClock());

            avTerminateMessageToMyself = new AVTerminateMessage(
                    AppConfig.myServentInfo,
                    AppConfig.myServentInfo,
                    AppConfig.myServentInfo,
                    vectorClock);

            CausalBroadcastShared.addPendingMessage(avTerminateMessageToMyself);
            CausalBroadcastShared.checkPendingMessages(snapshotCollector);

            for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                avTerminateMessageToNeighbor = new AVTerminateMessage(
                        AppConfig.myServentInfo,
                        AppConfig.myServentInfo,
                        AppConfig.getInfoById(neighbor),
                        vectorClock);

                CausalBroadcastShared.commitCausalMessage(avTerminateMessageToNeighbor, snapshotCollector);
                MessageUtil.sendMessage(avTerminateMessageToNeighbor.changeReceiver(neighbor).makeMeASender());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void handleTermination(SnapshotCollector snapshotCollector){
        synchronized (AppConfig.paranoidLock) {
            this.tokenVectorClock = null;
        }

        snapshotCollector.initiateTermination();

        int sum = recordedAmount;
        AppConfig.timestampedStandardPrint("\nRecorded node bitcake amount: " + sum);

        for (Map.Entry<Integer, Integer> entry : getHistory.entrySet()) {
            AppConfig.timestampedStandardPrint("Channel " + AppConfig.myServentInfo.getId() + " <--- " + entry.getKey() + " :");
            AppConfig.timestampedStandardPrint("Unreceived bitcake amount: " + entry.getValue() + " from " + entry.getKey());
            sum += entry.getValue();
        }

        AppConfig.timestampedStandardPrint("Final node bitcake amount: " + sum);
    }

    public void takeSomeBitcakes(int amount) { currentAmount.getAndAdd(-amount);}

    public void addSomeBitcakes(int amount) { currentAmount.getAndAdd(amount);}

    public int getCurrentBitcakeAmount() { return currentAmount.get();}

    private class MapValueUpdater implements BiFunction<Integer, Integer, Integer> {

        private int valueToAdd;

        public MapValueUpdater(int valueToAdd) {
            this.valueToAdd = valueToAdd;
        }

        @Override
        public Integer apply(Integer key, Integer oldValue) {
            return oldValue + valueToAdd;
        }
    }

    public void recordGetTransaction(Map<Integer, Integer> senderVectorClock, int neighbor, int amount) {
        if(tokenVectorClock != null){
            if(tokenVectorClock.get(tokenInitiatorId) >= senderVectorClock.get(tokenInitiatorId))
                getHistory.compute(neighbor, new MapValueUpdater(amount));
        }
    }

}
