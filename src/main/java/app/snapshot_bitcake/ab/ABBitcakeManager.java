package app.snapshot_bitcake.ab;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.SnapshotCollector;
import servent.message.Message;
import servent.message.snapshot.ab.ABTellMessage;
import servent.message.snapshot.TokenMessage;
import servent.message.snapshot.ab.ABTokenMessage;
import servent.message.util.MessageUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class ABBitcakeManager implements BitcakeManager {

    private final AtomicInteger currentAmount = new AtomicInteger(1000);
    private Map<Integer, Integer> giveHistory = new ConcurrentHashMap<>();
    private Map<Integer, Integer> getHistory = new ConcurrentHashMap<>();
    public int recordedAmount = 0;

    public ABBitcakeManager() {
        for(Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
            giveHistory.put(neighbor, 0);
            getHistory.put(neighbor, 0);
        }
    }

    public void handleToken(int collectorId, SnapshotCollector snapshotCollector) {
        Message abTellMessageToMyself, abTellMessageToNeighbor;

        synchronized (AppConfig.paranoidLock){
            recordedAmount = getCurrentBitcakeAmount();
            ABSnapshotResult abSnapshotResult = new ABSnapshotResult(AppConfig.myServentInfo.getId(), recordedAmount, giveHistory, getHistory);

            if (collectorId == AppConfig.myServentInfo.getId()) {
                abTellMessageToMyself = new ABTellMessage(AppConfig.myServentInfo, AppConfig.myServentInfo, abSnapshotResult, collectorId);
                CausalBroadcastShared.addPendingMessage(abTellMessageToMyself);
                CausalBroadcastShared.checkPendingMessages(snapshotCollector);
            }
            else {
                abTellMessageToNeighbor = new ABTellMessage(AppConfig.myServentInfo,null, abSnapshotResult, collectorId);

                for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                    abTellMessageToNeighbor = abTellMessageToNeighbor.changeReceiver(neighbor);
                    MessageUtil.sendMessage(abTellMessageToNeighbor);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                CausalBroadcastShared.incrementClock(AppConfig.myServentInfo.getId());
            }
        }
    }

    public void tokenEvent(SnapshotCollector snapshotCollector) {
        Message abTokenMessageToMyself, abTokenMessageToNeighbor;

        synchronized (AppConfig.paranoidLock) {
            recordedAmount = getCurrentBitcakeAmount();

            abTokenMessageToMyself = new ABTokenMessage(AppConfig.myServentInfo, AppConfig.myServentInfo);
            CausalBroadcastShared.addPendingMessage(abTokenMessageToMyself);
            CausalBroadcastShared.checkPendingMessages(snapshotCollector);

            abTokenMessageToNeighbor = new ABTokenMessage(AppConfig.myServentInfo, null);

            for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                abTokenMessageToNeighbor = abTokenMessageToNeighbor.changeReceiver(neighbor);
                MessageUtil.sendMessage(abTokenMessageToNeighbor);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
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

    public void recordGiveTransaction(int neighbor, int amount) {
        giveHistory.compute(neighbor, new MapValueUpdater(amount));
    }

    public void recordGetTransaction(int neighbor, int amount) {
        getHistory.compute(neighbor, new MapValueUpdater(amount));
    }
}
