package app.snapshot_bitcake;

import app.Cancellable;
import app.snapshot_bitcake.ab.ABSnapshotResult;

import java.util.Map;

/**
 * Describes a snapshot collector. Made not-so-flexibly for readability.
 * 
 * @author bmilojkovic
 *
 */
public interface SnapshotCollector extends Runnable, Cancellable {

	BitcakeManager getBitcakeManager();
	void addABSnapshotInfo(int id, ABSnapshotResult abSnapshotResult);
	void markServentAsDone(int id);
	void startCollecting();
	void initiateTermination();

}