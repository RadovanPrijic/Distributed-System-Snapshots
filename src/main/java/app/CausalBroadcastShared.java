package app;

import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.handler.NullHandler;
import servent.handler.TransactionHandler;
import servent.handler.snapshot.ab.ABTellHandler;
import servent.handler.snapshot.ab.ABTokenHandler;
import servent.handler.snapshot.av.AVDoneHandler;
import servent.handler.snapshot.av.AVTerminateHandler;
import servent.handler.snapshot.av.AVTokenHandler;
import servent.message.Message;
import servent.message.MessageType;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.BiFunction;

/**
 * This class contains shared data for the Causal Broadcast implementation:
 * <ul>
 * <li> Vector clock for current instance
 * <li> Commited message list
 * <li> Pending queue
 * </ul>
 * As well as operations for working with all of the above.
 *
 * @author bmilojkovic
 *
 */
public class CausalBroadcastShared {
    private static Map<Integer, Integer> vectorClock = new ConcurrentHashMap<>();
    private static List<Message> commitedCausalMessageList = new CopyOnWriteArrayList<>();
    private static Queue<Message> pendingMessages = new ConcurrentLinkedQueue<>();
    private static Object pendingMessagesLock = new Object();
    private static final ExecutorService handlerThreadPool = Executors.newWorkStealingPool();
    private static volatile boolean hasBeenTerminated = false;

    public static void initializeVectorClock(int serventCount) {
        for(int i = 0; i < serventCount; i++) {
            vectorClock.put(i, 0);
        }
    }

    public static void incrementClock(int serventId) {
        vectorClock.computeIfPresent(serventId, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer key, Integer oldValue) {
                return oldValue+1;
            }
        });
    }

    public static Map<Integer, Integer> getVectorClock() {
        return vectorClock;
    }

    public static void addPendingMessage(Message msg) {
        pendingMessages.add(msg);
    }

    public static List<Message> getCommitedCausalMessages() {
        List<Message> toReturn = new CopyOnWriteArrayList<>(commitedCausalMessageList);

        return toReturn;
    }

    public static void commitCausalMessage(Message newMessage, SnapshotCollector snapshotCollector) {
        AppConfig.timestampedStandardPrint("Committing " + newMessage);
        commitedCausalMessageList.add(newMessage);
        incrementClock(newMessage.getOriginalSenderInfo().getId());
        checkPendingMessages(snapshotCollector);
    }

    private static boolean otherClockGreater(Map<Integer, Integer> clock1, Map<Integer, Integer> clock2) {
        if (clock1.size() != clock2.size()) {
            throw new IllegalArgumentException("Clocks are not same size how why");
        }

        for(int i = 0; i < clock1.size(); i++) {
            if (clock2.get(i) > clock1.get(i)) {
                return true;
            }
        }

        return false;
    }

    public static void checkPendingMessages(SnapshotCollector snapshotCollector) {
        boolean gotWork = true;

        while (gotWork) {
            gotWork = false;

            synchronized (pendingMessagesLock) {
                Iterator<Message> iterator = pendingMessages.iterator();
                Map<Integer, Integer> myVectorClock = getVectorClock();

                while (iterator.hasNext()) {
                    Message pendingMessage = iterator.next();

                    if (!otherClockGreater(myVectorClock, pendingMessage.getSenderVectorClock())) {
                        gotWork = true;
                        MessageHandler messageHandler = new NullHandler(pendingMessage);

                        commitedCausalMessageList.add(pendingMessage);

                        if((pendingMessage.getMessageType() != MessageType.AV_TERMINATE && pendingMessage.getMessageType() != MessageType.AV_TOKEN)||
                                (pendingMessage.getMessageType() == MessageType.AV_TERMINATE && AppConfig.myServentInfo.getId() != pendingMessage.getOriginalSenderInfo().getId()) ||
                                (pendingMessage.getMessageType() == MessageType.AV_TOKEN && AppConfig.myServentInfo.getId() != pendingMessage.getOriginalSenderInfo().getId()))
                            incrementClock(pendingMessage.getOriginalSenderInfo().getId());

                        switch (pendingMessage.getMessageType()) {
                            case TRANSACTION:
                                if(pendingMessage.getOriginalReceiverInfo().getId() == AppConfig.myServentInfo.getId()){
                                    //System.out.println("Got message " + pendingMessage.getMessageId() + " from " + pendingMessage.getOriginalSenderInfo());
                                    messageHandler = new TransactionHandler(pendingMessage, snapshotCollector.getBitcakeManager());
                                }
                                break;
                            case AB_TOKEN:
                                messageHandler = new ABTokenHandler(pendingMessage, snapshotCollector.getBitcakeManager(), snapshotCollector);
                                break;
                            case AB_TELL:
                                if(pendingMessage.getOriginalReceiverInfo().getId() == AppConfig.myServentInfo.getId())
                                    messageHandler = new ABTellHandler(pendingMessage, snapshotCollector);
                                break;
                            case AV_TOKEN:
                                messageHandler = new AVTokenHandler(pendingMessage, snapshotCollector.getBitcakeManager(), snapshotCollector);
                                break;
                            case AV_DONE:
                                if(pendingMessage.getOriginalReceiverInfo().getId() == AppConfig.myServentInfo.getId())
                                    messageHandler = new AVDoneHandler(pendingMessage, snapshotCollector);
                                break;
                            case AV_TERMINATE:
                                if(!hasBeenTerminated){
                                    hasBeenTerminated = true;
                                    AppConfig.timestampedStandardPrint("Beginning with termination ...");
                                    messageHandler = new AVTerminateHandler(pendingMessage, snapshotCollector);
                                }
                                break;
                        }
                        handlerThreadPool.submit(messageHandler);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }

    public static void stop(){ handlerThreadPool.shutdown(); }
}
