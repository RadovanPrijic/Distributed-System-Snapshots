package servent.handler.snapshot.ab;

import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.SnapshotCollector;
import app.snapshot_bitcake.ab.ABBitcakeManager;
import servent.handler.MessageHandler;
import servent.message.Message;

public class ABTokenHandler implements MessageHandler {

    private Message clientMessage;
    private BitcakeManager bitcakeManager;
    private SnapshotCollector snapshotCollector;

    public ABTokenHandler(Message clientMessage, BitcakeManager bitcakeManager, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.bitcakeManager = bitcakeManager;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {
        try{
            ((ABBitcakeManager)bitcakeManager).handleToken(clientMessage.getOriginalSenderInfo(), snapshotCollector);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
