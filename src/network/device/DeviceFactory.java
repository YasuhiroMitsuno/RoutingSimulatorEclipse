package network.device;

public abstract class DeviceFactory {
	public final Device create() {
		Device device = createDevice();
		return device;
	}
	
	public final Device create(double x, double y) {
		Device device = createDevice(x, y);
		return device;
	}
	
	protected abstract Device createDevice();
	protected abstract Device createDevice(double x, double y);
}
