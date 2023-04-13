package app.snapshot_bitcake;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import app.AppConfig;
import app.snapshot_bitcake.ab.ABBitcakeManager;
import app.snapshot_bitcake.ab.ABSnapshotResult;
import app.snapshot_bitcake.av.AVBitcakeManager;
import app.snapshot_bitcake.cl.CLSnapshotResult;
import app.snapshot_bitcake.cl.ChandyLamportBitcakeManager;
import app.snapshot_bitcake.ly.LYSnapshotResult;
import app.snapshot_bitcake.ly.LaiYangBitcakeManager;
import app.snapshot_bitcake.naive.NaiveBitcakeManager;
import servent.message.Message;
import servent.message.snapshot.naive.NaiveAskAmountMessage;
import servent.message.util.MessageUtil;

/**
 * Main snapshot collector class. Has support for Naive, Chandy-Lamport
 * and Lai-Yang snapshot algorithms.
 * 
 * @author bmilojkovic
 *
 */
public class SnapshotCollectorWorker implements SnapshotCollector {


	private volatile boolean working = true;
	private volatile boolean terminateAV = false;
	private AtomicBoolean collecting = new AtomicBoolean(false);
	private SnapshotType snapshotType;
	private BitcakeManager bitcakeManager;
	private Map<Integer, ABSnapshotResult> collectedABValues = new ConcurrentHashMap<>();
	private Map<Integer, Boolean> avDoneMessages = new ConcurrentHashMap<>();

	public SnapshotCollectorWorker(SnapshotType snapshotType) {
		this.snapshotType = snapshotType;
		
		switch(snapshotType) {
		case AB:
			bitcakeManager = new ABBitcakeManager();
			break;
		case AV:
			bitcakeManager = new AVBitcakeManager();
			break;
		case NONE:
			AppConfig.timestampedErrorPrint("Making snapshot collector without specifying type. Exiting...");
			System.exit(0);
		}
	}
	
	@Override
	public BitcakeManager getBitcakeManager() {
		return bitcakeManager;
	}
	
	@Override
	public void run() {
		while(working) {
			
			/*
			 * Not collecting yet - just sleep until we start actual work, or finish
			 */
			while (collecting.get() == false) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (working == false) {
					return;
				}
			}
			
			/*
			 * Collecting is done in three stages:
			 * 1. Send messages asking for values
			 * 2. Wait for all the responses
			 * 3. Print result
			 */
			
			//1 send asks
			switch (snapshotType) {
				case AB:
					((ABBitcakeManager)bitcakeManager).tokenEvent(this);
					break;
				case AV:
					//((AVBitcakeManager)bitcakeManager).tokenEvent();
				case NONE:
					//Shouldn't be able to come here. See constructor.
					break;
			}
			
			//2 wait for responses or finish
			boolean waiting = true;
			while (waiting) {
				switch (snapshotType) {
					case AB:
						if (collectedABValues.size() == AppConfig.getServentCount())
							waiting = false;
						break;
					case AV:
						if (avDoneMessages.size() == AppConfig.getServentCount() - 1) {
							waiting = false;
							//((AVBitcakeManager)bitcakeManager).sendTerminateMessage(this);
						}
						break;
					case NONE:
						//Shouldn't be able to come here. See constructor.
						break;
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if (working == false) {
					return;
				}
			}
			
			//print
			int sum;
			switch (snapshotType) {
				case AB:
					sum = 0;
					for (Entry<Integer, ABSnapshotResult> nodeResult : collectedABValues.entrySet()) {
						sum += nodeResult.getValue().getRecordedAmount();
						AppConfig.timestampedStandardPrint(
								"Recorded bitcake amount for " + nodeResult.getKey() + " = " + nodeResult.getValue().getRecordedAmount());
					}
					for(int i = 0; i < AppConfig.getServentCount(); i++) {
						for (int j = 0; j < AppConfig.getServentCount(); j++) {
							if (i != j) {
								if (AppConfig.getInfoById(i).getNeighbors().contains(j) &&
										AppConfig.getInfoById(j).getNeighbors().contains(i)) {
									int ijAmount = collectedABValues.get(i).getGiveHistory().get(j);
									int jiAmount = collectedABValues.get(j).getGetHistory().get(i);

									if (ijAmount != jiAmount) {
										String outputString = String.format(
												"Unreceived bitcake amount: %d from servent %d to servent %d",
												ijAmount - jiAmount, i, j);
										AppConfig.timestampedStandardPrint(outputString);
										sum += ijAmount - jiAmount;
									}
								}
							}
						}
					}

					AppConfig.timestampedStandardPrint("System bitcake count: " + sum);
					collectedABValues.clear(); //reset for next invocation
					break;
				case AV:
					while(!terminateAV){
						//Loop until it's time for termination
					}
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					avDoneMessages.clear();
					break;
				case NONE:
					//Shouldn't be able to come here. See constructor.
					break;
			}
			collecting.set(false);
		}

	}

	@Override
	public void addABSnapshotInfo(int id, ABSnapshotResult abSnapshotResult) {
		collectedABValues.put(id, abSnapshotResult);
	}

	@Override
	public void markServentAsDone(int id) {
		avDoneMessages.put(id, true);
	}

	public void initiateTermination(){
		terminateAV = true;
	}

	/*
	@Override
	public boolean isCollecting() {
		return collecting.get();
	}
	*/
	
	@Override
	public void startCollecting() {
		boolean oldValue = this.collecting.getAndSet(true);
		
		if (oldValue == true) {
			AppConfig.timestampedErrorPrint("Tried to start collecting before finished with previous.");
		}
	}
	
	@Override
	public void stop() {
		working = false;
	}

}
