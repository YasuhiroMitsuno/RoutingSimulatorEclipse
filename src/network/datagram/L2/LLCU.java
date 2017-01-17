package network.datagram.L2;

public class LLCU {
	private byte[] bytes;
	private byte dsap;
	private byte ssap;
	private byte control;
	private byte information;
	
	public LLCU() {
		bytes = new byte[4];
	}
	
	public LLCU(Frame frame) {
		_setBytes(frame.getData());
	}
	
	public LLCU(byte[] bytes) {
		_setBytes(bytes);
	}
	
	private void _setBytes(byte[] bytes) {
		this.bytes = bytes;
		
		this.dsap = bytes[0];
		this.ssap = bytes[1];
		this.control = bytes[2];
		this.information = bytes[3];
	}
	
	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	
	public byte getDsap() {
		return dsap;
	}

	public void setDsap(byte dsap) {
		this.dsap = dsap;
		this.bytes[0] = dsap;
	}

	public byte getSsap() {
		return ssap;
	}

	public void setSsap(byte ssap) {
		this.ssap = ssap;
		this.bytes[1] = ssap;
	}

	public byte getControl() {
		return control;
	}

	public void setControl(byte control) {
		this.control = control;
		this.bytes[2] = control;
	}

	public byte getInformation() {
		return information;
	}

	public void setInformation(byte information) {
		this.information = information;
		this.bytes[3] = information;
	}

	public byte[] getData() {
		int length = bytes.length - 4;
		byte[] data = new byte[length];
		System.arraycopy(bytes, 4, data, 0, length);
		return data;
	}

	public void setData(byte[] data) {
        byte[] newBytes = new byte[data.length + 4];
        System.arraycopy(this.bytes, 0, newBytes, 0, 4);
        System.arraycopy(data, 0, newBytes, 4, data.length);
        this.bytes = newBytes;
	}

	public String description() {
        String str = "";
        str += "Logical-Link Control";
        str += "\n\tDSAP: " + "Spanning Tree BPDU" + " " + String.format("(0x%02x)", this.dsap);
        str += "\n\tIG Bit: " + "Individual";
        str += "\n\tSSAP: " + "Spanning Tree BPDU" + " " + String.format("(0x%02x)", this.ssap);
        str += "\n\tCR Bit: " + "Command";
        str += "\n\tControl Field: " + String.format("(0x%02x)", this.control);
        return str;
		
	}
}
