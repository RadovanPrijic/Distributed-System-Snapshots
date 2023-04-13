package servent.message.snapshot.ab;

import app.AppConfig;
import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ABTokenMessage extends BasicMessage {

    private static final long serialVersionUID = -2718696242684977713L;

    public ABTokenMessage(ServentInfo originalSenderInfo, ServentInfo receiverInfo) {
        super(MessageType.AB_TOKEN, originalSenderInfo, receiverInfo);
    }

    public ABTokenMessage(ServentInfo originalSenderInfo, ServentInfo receiverInfo, List<ServentInfo> routeList,
                          Map<Integer, Integer> senderVectorClock, String messageText, int messageId) {
        super(MessageType.AB_TOKEN, originalSenderInfo, receiverInfo, routeList, senderVectorClock, messageText, messageId);
    }
}
