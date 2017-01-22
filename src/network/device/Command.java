package network.device;

import java.util.StringTokenizer;

import network.datagram.L3.Util;

public class Command {
	private Device delegate;
	private StringTokenizer tokenizer;
	private String currentToken;
	
	public Command(Device delegate) {
		this.delegate = delegate;
	}
	
	private String next() {
		if (tokenizer.hasMoreTokens()) {
			currentToken = tokenizer.nextToken();
		} else {
			currentToken = null;
		}
		return currentToken;
	}
	
	public void execute(String command) {
		tokenizer = new StringTokenizer(command);

		switch (next()) {
		case "ip":
			ip();
			break;
		case "ping":
			ping();
			break;
		case "arp":
			arp();
			break;
		case "show":
			show();
			break;
		}
	}
	
	private void ip() {
		switch (next()) {
		case "addr":
			ipAddr();
			break;
		case "route":
			ipRoute();
			break;
		}
	}
	
	private void ipAddr() {
		String port = next();
		String addr = next();
		String mask = next();
		System.out.println(port + addr + mask);
		delegate.setIP(Integer.parseInt(port), Util.addr2int(addr), Util.addr2int(mask));
	}
	
	private void ipRoute() {
		String addr = next();
		String mask = next();
		String next = next();
		System.out.println(addr + mask + next);
		delegate.setRoute(Util.addr2int(addr), Util.addr2int(mask), Util.addr2int(next));
	}
	
	private void ping() {
		String addr = next();
		System.out.println("ping " + addr);
		delegate.ping(Util.addr2int(addr));
	}
	
	private void arp() {
		String addr = next();
		System.out.println("arp " + addr);
		delegate.arp.arp(Util.addr2int(addr));
	}
	
	private void show() {
		switch (next()) {
		case "ip":
			showIp();
			break;
		case "interface":
			showInterface();
			break;
		case "arp":
			showArp();
			break;
		case "mac":
			showMAC();
			break;
		}
	}
	
	private void showIp() {
		switch (next()) {
		case "addr":
			showIpAddr();
			break;
		case "route":
			showIpRoute();
			break;
		}
	}
	private void showIpAddr() {
		delegate.showIpAddr();
	}
	
	private void showIpRoute() {
		delegate.showIpRoute();
	}
	
	private void showInterface() {
		delegate.showInterface();
	}
	
	private void showArp() {
		delegate.arp.showArpTable();
	}
	
	private void showMAC() {
		delegate.showMAC();
	}
}
