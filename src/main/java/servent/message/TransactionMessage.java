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
	private int originalReceiverId;
	
	public TransactionMessage(ServentInfo sender, ServentInfo receiver, int amount, BitcakeManager bitcakeManager) {
		super(MessageType.TRANSACTION, sender, receiver, String.valueOf(amount));
		this.bitcakeManager = bitcakeManager;
		this.originalReceiverId = receiver.getId();
	}

	private TransactionMessage(ServentInfo originalSenderInfo, ServentInfo receiverInfo,
							   List<ServentInfo> routeList, Map<Integer, Integer> senderVectorClock,
							   String messageText, int messageId,
							   BitcakeManager bitcakeManager, int originalReceiver) {
		super(MessageType.TRANSACTION, originalSenderInfo, receiverInfo, routeList, senderVectorClock, messageText, messageId);
		this.bitcakeManager = bitcakeManager;
		this.originalReceiverId = originalReceiver;
	}

	public int getOriginalReceiver() {
		return originalReceiverId;
	}

	@Override
	public Message makeMeASender() {
		ServentInfo newRouteItem = AppConfig.myServentInfo;

		List<ServentInfo> newRouteList = new ArrayList<>(getRoute());
		newRouteList.add(newRouteItem);
		Message toReturn = new TransactionMessage(getOriginalSenderInfo(), getReceiverInfo(), newRouteList, getSenderVectorClock(),
				getMessageText(), getMessageId(), bitcakeManager, originalReceiverId);

		return toReturn;
	}

	@Override
	public Message changeReceiver(Integer newReceiverId) {
		if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId)) {
			ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);

			Message toReturn = new TransactionMessage(getOriginalSenderInfo(), newReceiverInfo, getRoute(), getSenderVectorClock(),
					getMessageText(), getMessageId(), bitcakeManager, originalReceiverId);

			return toReturn;
		} else {
			AppConfig.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");
			return null;
		}
	}

	/**
	 * We want to take away our amount exactly as we are sending, so our snapshots don't mess up.
	 * This method is invoked by the sender just before sending, and with a lock that guarantees
	 * that we are white when we are doing this in Chandy-Lamport.
	 */
	@Override
	public void sendEffect() {
		if(bitcakeManager!=null) {
			int amount = Integer.parseInt(getMessageText());

			synchronized (AppConfig.paranoidLock){
				bitcakeManager.takeSomeBitcakes(amount);
				CausalBroadcastShared.incrementClock(AppConfig.myServentInfo.getId());

				if (bitcakeManager instanceof ABBitcakeManager) {
					ABBitcakeManager abBitcakeManager = (ABBitcakeManager) bitcakeManager;
					//abFinancialManager.recordGiveTransaction(getReceiverInfo().getId(), amount);
				}
				if (bitcakeManager instanceof AVBitcakeManager) {
					AVBitcakeManager avBitcakeManager = (AVBitcakeManager) bitcakeManager;
					//avFinancialManager.recordGiveTransaction(getSenderVectorClock(),getReceiverInfo().getId(),amount);
				}
				/*
				if (bitcakeManager instanceof LaiYangBitcakeManager && isWhite()) {
					LaiYangBitcakeManager lyFinancialManager = (LaiYangBitcakeManager)bitcakeManager;

					lyFinancialManager.recordGiveTransaction(getReceiverInfo().getId(), amount);
				}
				*/
			}
		}
	}
}
