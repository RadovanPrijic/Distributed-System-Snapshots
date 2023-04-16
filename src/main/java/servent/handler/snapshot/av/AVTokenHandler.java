package servent.handler.snapshot.av;

import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.SnapshotCollector;
import app.snapshot_bitcake.ab.ABBitcakeManager;
import app.snapshot_bitcake.av.AVBitcakeManager;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.snapshot.av.AVTokenMessage;

public class AVTokenHandler implements MessageHandler {

    private Message clientMessage;
    private BitcakeManager bitcakeManager;
    private SnapshotCollector snapshotCollector;

    public AVTokenHandler(Message clientMessage, BitcakeManager bitcakeManager, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.bitcakeManager = bitcakeManager;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {
        try{
            ((AVBitcakeManager)bitcakeManager).handleToken(clientMessage.getSenderVectorClock(), clientMessage.getOriginalSenderInfo(), snapshotCollector);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
