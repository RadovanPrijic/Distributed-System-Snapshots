package servent.handler.snapshot.av;

import app.AppConfig;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class AVDoneHandler implements MessageHandler {

    private Message clientMessage;
    private SnapshotCollector snapshotCollector;


    public AVDoneHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.AV_DONE) {
                snapshotCollector.markServentAsDone(clientMessage.getOriginalSenderInfo().getId());
            } else {
                AppConfig.timestampedErrorPrint("Tell amount handler got: " + clientMessage);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
