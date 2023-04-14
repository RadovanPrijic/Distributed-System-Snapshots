package servent.message.snapshot.av;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

import java.io.Serial;
import java.util.List;
import java.util.Map;

public class AVTokenMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -3753755507689855299L;

    public AVTokenMessage(ServentInfo originalSenderInfo,
                          ServentInfo originalReceiverInfo,
                          ServentInfo receiverInfo,
                          Map<Integer, Integer> vectorClock) {
        super(MessageType.AV_TOKEN, originalSenderInfo, originalReceiverInfo, receiverInfo, vectorClock);
    }

    public AVTokenMessage(MessageType type,
                          ServentInfo originalSenderInfo,
                          ServentInfo originalReceiverInfo,
                          ServentInfo receiverInfo,
                          Map<Integer, Integer> senderVectorClock,
                          List<ServentInfo> routeList,
                          String messageText,
                          int messageId) {
        super(MessageType.AV_TOKEN, originalSenderInfo, originalReceiverInfo, receiverInfo, senderVectorClock, routeList,  messageText, messageId);
    }
}
