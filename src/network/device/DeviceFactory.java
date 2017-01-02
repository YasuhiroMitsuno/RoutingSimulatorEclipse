package network.device;

public abstract class DeviceFactory {
	public final Device create() {
		Device device = createDevice();
		return device;
	}
	
	protected abstract Device createDevice();
}
