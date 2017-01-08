package network.protocol.L2.STP;

public interface STPPortState {
	public String getStateName();
    public void actionForUserFrame();
    public boolean willSendBPDU();
    public void learnMacFrame();
    public STPPortState getNextState();
}
