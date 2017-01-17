package network.device;

public class Logger {
	private String log;
	
	Logger() {
		
	}
	
	public String getLog() {
		return log;
	}
	
	public void log(String str) {
		log += str + "\n";
	}
}
