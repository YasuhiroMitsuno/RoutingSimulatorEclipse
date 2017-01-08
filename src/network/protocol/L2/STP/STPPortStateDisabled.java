package network.protocol.L2.STP;

public class STPPortStateDisabled implements STPPortState {
	final static String stateName = "Disable";
	final static STPPortState nextState = new STPPortStateBlocking(); 
	
	@Override
	public void actionForUserFrame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean willSendBPDU() {
		return false;		
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
