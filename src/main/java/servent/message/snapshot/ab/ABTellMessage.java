package servent.message.snapshot.ab;

import app.AppConfig;
import app.ServentInfo;

import app.snapshot_bitcake.ab.ABSnapshotResult;
import servent.message.BasicMessage;

import servent.message.Message;
import servent.message.MessageType;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ABTellMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 5353500591666465357L;
    private ABSnapshotResult abSnapshotResult;

    public ABTellMessage(ServentInfo originalSenderInfo,
                         ServentInfo originalReceiverInfo,
                         ServentInfo receiverInfo,
                         Map<Integer, Integer> vectorClock,
                         ABSnapshotResult abSnapshotResult) {
        super(MessageType.AB_TELL, originalSenderInfo, originalReceiverInfo, receiverInfo, vectorClock);
        this.abSnapshotResult = abSnapshotResult;
    }

    private ABTellMessage(ServentInfo originalSenderInfo,
                          ServentInfo originalReceiverInfo,
                          ServentInfo receiverInfo,
                          Map<Integer, Integer> senderVectorClock,
                          List<ServentInfo> routeList,
                          String messageText,
                          int messageId,
                          ABSnapshotResult abSnapshotResult) {
        super(MessageType.AB_TELL, originalSenderInfo, originalReceiverInfo, receiverInfo, senderVectorClock, routeList, messageText, messageId);
        this.abSnapshotResult = abSnapshotResult;
    }

    @Override
    public Message makeMeASender() {
        ServentInfo newRouteItem = AppConfig.myServentInfo;

        List<ServentInfo> newRouteList = new ArrayList<>(getRoute());
        newRouteList.add(newRouteItem);
        Message toReturn = new ABTellMessage(
                getOriginalSenderInfo(), getOriginalReceiverInfo(), getReceiverInfo(),
                getSenderVectorClock(), newRouteList,
                getMessageText(), getMessageId(),
                abSnapshotResult);

        return toReturn;
    }

    @Override
    public Message changeReceiver(Integer newReceiverId) {
        if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId)) {
            ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);

            Message toReturn = new ABTellMessage(
                    getOriginalSenderInfo(), getOriginalReceiverInfo(), newReceiverInfo,
                    getSenderVectorClock(), getRoute(),
                    getMessageText(), getMessageId(),
                    abSnapshotResult);

            return toReturn;
        } else {
            AppConfig.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");
            return null;
        }
    }

    public ABSnapshotResult getABSnapshotResult() {
        return abSnapshotResult;
    }
}
