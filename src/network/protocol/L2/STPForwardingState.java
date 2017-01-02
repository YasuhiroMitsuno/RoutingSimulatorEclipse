package network.protocol.L2;

public class STPForwardingState implements STPState {
	final static String stateName = "Forwarding";
	
	@Override
	public void actionForUserFrame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionForBPDU() {
		// TODO Auto-generated method stub
		
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
	public STPState getNextState() {
		// TODO Auto-generated method stub
		return null;
	}

}
