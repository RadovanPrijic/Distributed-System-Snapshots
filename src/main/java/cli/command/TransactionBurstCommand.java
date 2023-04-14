package cli.command;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.ServentInfo;
import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.SnapshotCollector;
import servent.message.Message;
import servent.message.TransactionMessage;
import servent.message.util.MessageUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionBurstCommand implements CLICommand {

	private static final int TRANSACTION_COUNT = 5;
	private static final int BURST_WORKERS = 5;
	private static final int MAX_TRANSFER_AMOUNT = 10;

	private final Object clockLock = new Object();
	private SnapshotCollector snapshotCollector;
	private BitcakeManager bitcakeManager;

	public TransactionBurstCommand(SnapshotCollector snapshotCollector, BitcakeManager bitcakeManager) {
		this.snapshotCollector = snapshotCollector;
		this.bitcakeManager = bitcakeManager;
	}
	
	private class TransactionBurstWorker implements Runnable {
		
		@Override
		public void run() {
			//Map<Integer, Integer> myClock = new ConcurrentHashMap<>(CausalBroadcastShared.getVectorClock());

			for (int i = 0; i < TRANSACTION_COUNT; i++) {
				for (int neighbor : AppConfig.myServentInfo.getNeighbors()) {
					ServentInfo neighborInfo = AppConfig.getInfoById(neighbor);
					int amount = 1 + (int)(Math.random() * MAX_TRANSFER_AMOUNT);

					Message transactionMessage;

					synchronized (AppConfig.paranoidLock) {
					Map<Integer, Integer> vectorClock = new ConcurrentHashMap<>(CausalBroadcastShared.getVectorClock());
					transactionMessage = new TransactionMessage(AppConfig.myServentInfo, neighborInfo, null, amount, bitcakeManager, vectorClock);
					transactionMessage.sendEffect();
					CausalBroadcastShared.commitCausalMessage(transactionMessage, snapshotCollector);
					}

					MessageUtil.sendMessage(transactionMessage.changeReceiver(neighbor).makeMeASender());
				}
			}
		}
	}
	
	@Override
	public String commandName() {
		return "transaction_burst";
	}

	@Override
	public void execute(String args) {
		for (int i = 0; i < BURST_WORKERS; i++) {
			Thread t = new Thread(new TransactionBurstWorker());
			
			t.start();
		}
	}

	
}
