package servent.message;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.ServentInfo;
import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.ab.ABBitcakeManager;
import app.snapshot_bitcake.av.AVBitcakeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a bitcake transaction. We are sending some bitcakes to another node.
 * 
 * @author bmilojkovic
 *
 */
public class TransactionMessage extends BasicMessage {

	private static final long serialVersionUID = -333251402058492901L;

	private transient BitcakeManager bitcakeManager;
	
	public TransactionMessage(ServentInfo sender,
							  ServentInfo finalReceiver,
							  ServentInfo receiver,
							  int amount,
							  BitcakeManager bitcakeManager,
							  Map<Integer, Integer> vectorClock) {
		super(MessageType.TRANSACTION, sender, finalReceiver, receiver, vectorClock, String.valueOf(amount));
		this.bitcakeManager = bitcakeManager;
	}

	/**
	 * We want to take away our amount exactly as we are sending, so our snapshots don't mess up.
	 * This method is invoked by the sender just before sending, and with a lock that guarantees
	 * that we are white when we are doing this in Chandy-Lamport.
	 */
	@Override
	public void sendEffect() {
		if(bitcakeManager != null) {
			int amount = Integer.parseInt(getMessageText());

			bitcakeManager.takeSomeBitcakes(amount);

			if (bitcakeManager instanceof ABBitcakeManager) {
				ABBitcakeManager abBitcakeManager = (ABBitcakeManager) bitcakeManager;
				abBitcakeManager.recordGiveTransaction(getOriginalReceiverInfo().getId(), amount);
			} else if (bitcakeManager instanceof AVBitcakeManager) {
				//AVBitcakeManager avBitcakeManager = (AVBitcakeManager) bitcakeManager;
				//avBitcakeManager.recordGiveTransaction(getSenderVectorClock(), getOriginalReceiverInfo().getId(), amount);
			}
		}
	}
}
