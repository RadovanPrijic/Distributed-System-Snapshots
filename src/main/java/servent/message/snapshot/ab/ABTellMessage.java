package servent.message.snapshot.ab;

import app.AppConfig;
import app.ServentInfo;

import app.snapshot_bitcake.ab.ABSnapshotResult;
import servent.message.BasicMessage;

import servent.message.Message;
import servent.message.MessageType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ABTellMessage extends BasicMessage {

    private static final long serialVersionUID = 1536279421038991652L;
    private ABSnapshotResult abSnapshotResult;
    private int collectorId;

    public ABTellMessage(ServentInfo sender, ServentInfo receiver, ABSnapshotResult abSnapshotResult,  int collectorId) {
        super(MessageType.AB_TELL, sender, receiver);
        this.abSnapshotResult = abSnapshotResult;
        this.collectorId = collectorId;
    }

    private ABTellMessage(ServentInfo originalSenderInfo, ServentInfo receiverInfo, List<ServentInfo> routeList,
                          Map<Integer, Integer> senderVectorClock, String messageText, int messageId,
                          ABSnapshotResult abSnapshotResult, int collectorId) {
        super(MessageType.AB_TELL, originalSenderInfo, receiverInfo, routeList, senderVectorClock, messageText, messageId);
        this.abSnapshotResult = abSnapshotResult;
        this.collectorId = collectorId;
    }

    @Override
    public Message makeMeASender() {
        ServentInfo newRouteItem = AppConfig.myServentInfo;

        List<ServentInfo> newRouteList = new ArrayList<>(getRoute());
        newRouteList.add(newRouteItem);
        Message toReturn = new ABTellMessage(getOriginalSenderInfo(), getReceiverInfo(), newRouteList, getSenderVectorClock(),
                getMessageText(), getMessageId(), abSnapshotResult, collectorId);

        return toReturn;
    }

    @Override
    public Message changeReceiver(Integer newReceiverId) {
        if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId)) {
            ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);

            Message toReturn = new ABTellMessage(getOriginalSenderInfo(), newReceiverInfo, getRoute(), getSenderVectorClock(),
                    getMessageText(), getMessageId(), abSnapshotResult, collectorId);

            return toReturn;
        } else {
            AppConfig.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");
            return null;
        }
    }

    public ABSnapshotResult getABSnapshotResult() {
        return abSnapshotResult;
    }

    public int getCollectorId() {
        return collectorId;
    }
}
