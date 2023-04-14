package cli.command;

import app.AppConfig;
import app.CausalBroadcastShared;

import java.util.HashMap;
import java.util.Map;

public class InfoCommand implements CLICommand {

	@Override
	public String commandName() {
		return "info";
	}

	@Override
	public void execute(String args) {
//		AppConfig.timestampedStandardPrint("My info: " + AppConfig.myServentInfo);
//		AppConfig.timestampedStandardPrint("Neighbors:");
//		String neighbors = "";
//		for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
//			neighbors += neighbor + " ";
//		}
//		AppConfig.timestampedStandardPrint(neighbors);
//
		Map<Integer,Integer> vectorclock = new HashMap<>(CausalBroadcastShared.getVectorClock());
		for (Map.Entry<Integer, Integer> entry1 : vectorclock.entrySet()) {
			AppConfig.timestampedStandardPrint(String.valueOf(entry1.getValue()));
		}
		AppConfig.timestampedStandardPrint("Pending messages are empty: "+ CausalBroadcastShared.getPendingMessages());
	}

}
