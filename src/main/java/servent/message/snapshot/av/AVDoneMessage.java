package servent.message.snapshot.av;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

import java.io.Serial;
import java.util.List;
import java.util.Map;

public class AVDoneMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -3776374925605174188L;

    public AVDoneMessage(ServentInfo originalSenderInfo,
                         ServentInfo originalReceiverInfo,
                         ServentInfo receiverInfo,
                         Map<Integer, Integer> vectorClock) {
        super(MessageType.AV_DONE, originalSenderInfo, originalReceiverInfo, receiverInfo, vectorClock);
    }

    public AVDoneMessage(MessageType type,
                          ServentInfo originalSenderInfo,
                          ServentInfo originalReceiverInfo,
                          ServentInfo receiverInfo,
                          Map<Integer, Integer> senderVectorClock,
                          List<ServentInfo> routeList,
                          String messageText,
                          int messageId) {
        super(MessageType.AV_DONE, originalSenderInfo, originalReceiverInfo, receiverInfo, senderVectorClock, routeList,  messageText, messageId);
    }
}
