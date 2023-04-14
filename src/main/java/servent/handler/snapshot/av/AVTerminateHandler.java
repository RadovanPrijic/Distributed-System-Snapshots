package servent.handler.snapshot.av;

import app.AppConfig;
import app.snapshot_bitcake.BitcakeManager;

import app.snapshot_bitcake.SnapshotCollector;
import app.snapshot_bitcake.av.AVBitcakeManager;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class AVTerminateHandler implements MessageHandler {

    private Message clientMessage;
    private BitcakeManager bitcakeManager;
    private SnapshotCollector snapshotCollector;

    public AVTerminateHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.bitcakeManager = snapshotCollector.getBitcakeManager();
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.AV_TERMINATE) {
                ((AVBitcakeManager)bitcakeManager).handleTermination(snapshotCollector);
            } else {
                AppConfig.timestampedErrorPrint("Tell amount handler got: " + clientMessage);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
