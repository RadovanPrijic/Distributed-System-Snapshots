package servent.message.snapshot.ab;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

import java.io.Serial;
import java.util.List;
import java.util.Map;

public class ABTokenMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -5282790629581336062L;

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
