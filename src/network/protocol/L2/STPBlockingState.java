package network.protocol.L2;

public class STPBlockingState implements STPState {
	final static String stateName = "Blocking";
	final static STPState nextState = new STPListeningState(); 	
	
	@Override
	public String getStateName() {
		// TODO Auto-generated method stub
		return stateName;
	}
	
	@Override
	public void actionForUserFrame() {
		return;
	}
	
	@Override
	public void actionForBPDU() {
		// TODO Auto-generated method stub
	}

	@Override
	public void learnMacFrame() {
		return;
	}

	@Override
	public STPState getNextState() {
		// TODO Auto-generated method stub
		return nextState;
	}
}
