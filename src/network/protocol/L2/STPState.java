package network.protocol.L2;

public interface STPState {
	public String getStateName();
    public void actionForUserFrame();
    public void actionForBPDU();
    public void learnMacFrame();
    public STPState getNextState();
}
