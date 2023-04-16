package servent.message.snapshot.av;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

import java.io.Serial;
import java.util.List;
import java.util.Map;

public class AVTerminateMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 5835674549794801669L;

    public AVTerminateMessage(ServentInfo originalSenderInfo,
                              ServentInfo originalReceiverInfo,
                              ServentInfo receiverInfo,
                              Map<Integer, Integer> vectorClock) {
        super(MessageType.AV_TERMINATE, originalSenderInfo, originalReceiverInfo, receiverInfo, vectorClock);
    }
}
