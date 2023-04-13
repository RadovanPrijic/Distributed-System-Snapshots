package app.snapshot_bitcake.ab;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.snapshot_bitcake.BitcakeManager;
import servent.message.Message;
import servent.message.snapshot.ABTellMessage;
import servent.message.snapshot.TokenMessage;
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

    /*
    public void sendTell(int tokenSenderId) {

        int myId = AppConfig.myServentInfo.getId();
        Message tellMessage = null;
        synchronized (CausalBroadcastShared.gatheringChannel){
            app.snapshot_bitcake.ABSnapshotResult snapshotResult = new app.snapshot_bitcake.ABSnapshotResult(myId, getCurrentBitcakeAmount(), giveHistory, getHistory);
            tellMessage = new ABTellMessage(AppConfig.myServentInfo,null,snapshotResult,tokenSenderId);
        }
        for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
            tellMessage = tellMessage.changeReceiver(neighbor);
            MessageUtil.sendMessage(tellMessage);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        CausalBroadcastShared.incrementClock(AppConfig.myServentInfo.getId());
    }

    public void sendToken(SnapshotCollector snapshotCollector) {

        int myId = AppConfig.myServentInfo.getId();
        Message tokenMessage=null;
        app.snapshot_bitcake.ABSnapshotResult snapshotResult = null;
        synchronized (CausalBroadcastShared.gatheringChannel) {
            snapshotResult = new app.snapshot_bitcake.ABSnapshotResult(
                    myId, getCurrentBitcakeAmount(), giveHistory, getHistory);


            tokenMessage = new TokenMessage(
                    AppConfig.myServentInfo, null);
        }

        for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
            tokenMessage = tokenMessage.changeReceiver(neighbor);

            MessageUtil.sendMessage(tokenMessage);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Message toMe = null;
        toMe = new ABTellMessage(AppConfig.myServentInfo,AppConfig.myServentInfo,snapshotResult,myId);
        CausalBroadcastShared.addPendingMessage(toMe);
        CausalBroadcastShared.checkPendingMessages(snapshotCollector);

    }

     */

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
