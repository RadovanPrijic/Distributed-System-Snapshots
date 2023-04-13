package servent.message.snapshot.ab;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

import java.util.List;
import java.util.Map;

public class ABTokenMessage extends BasicMessage {

    private static final long serialVersionUID = -2718696242684977713L;

    public ABTokenMessage(ServentInfo originalSenderInfo,
                          ServentInfo originalReceiverInfo,
                          ServentInfo receiverInfo,
                          Map<Integer, Integer> vectorClock) {
        super(MessageType.AB_TOKEN, originalSenderInfo, originalReceiverInfo, receiverInfo, vectorClock);
    }

    public ABTokenMessage(MessageType type,
                          ServentInfo originalSenderInfo,
                          ServentInfo originalReceiverInfo,
                          ServentInfo receiverInfo,
                          Map<Integer, Integer> senderVectorClock,
                          List<ServentInfo> routeList,
                          String messageText,
                          int messageId) {
        super(MessageType.AB_TOKEN, originalSenderInfo, originalReceiverInfo, receiverInfo, senderVectorClock, routeList,  messageText, messageId);
    }
}
