package network.protocol.L2.STP;

public class STPPortStateListening implements STPPortState {
	final static String stateName = "Listening";
	final static STPPortState nextState = new STPPortStateLearning(); 
	
	@Override
	public void actionForUserFrame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean willSendBPDU() {
		return true;
	}

	@Override
	public void learnMacFrame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getStateName() {
		// TODO Auto-generated method stub
		return stateName;
	}

	@Override
	public STPPortState getNextState() {
		// TODO Auto-generated method stub
		return nextState;
	}

}
