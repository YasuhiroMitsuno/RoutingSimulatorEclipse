package network.protocol.L2;

public class STPListeningState implements STPState {
	final static String stateName = "Listening";
	final static STPState nextState = new STPLearningState(); 
	
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
