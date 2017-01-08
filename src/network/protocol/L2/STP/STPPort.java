package network.protocol.L2.STP;

import network.device.Device;
import network.device.Port;

class STPPort {
	int 			portId;
	State 	state;
	int 			pathCost;
	byte[] 			designatedRoot;
	long 			designatedCost = 0;
	byte[] 			designatedBridge;
	int 			designatedPort;
	boolean			topologyChangeAcknowledge;
	boolean			configPending;
	boolean			changeDetectionEnabled;
}
