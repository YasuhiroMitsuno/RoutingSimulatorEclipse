package network.protocol.L2;

public class STPDisableState implements STPState {
	final static String stateName = "Disable";
	final static STPState nextState = new STPBlockingState(); 
	
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
		return nextState;
	}

}
