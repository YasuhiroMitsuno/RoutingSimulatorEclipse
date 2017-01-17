package network.device;

import java.awt.geom.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;

import network.datagram.L2.Frame;
import network.protocol.L2.STP.STP;

public class Port {
    private final ActivationInputQueue<Frame> inputQueue;
    private final ActivationOutputQueue<Frame> outputQueue;
    private Boolean using = false;
    private Device delegate;
    private Port connectedPort;
    private int number;
    private long inStat;
    private long outStat;
    private int speed;
    public Boolean slow;
    private boolean linkup;

    public Port(Device delegate) {
        this(delegate, 0);
    }

    public Port(Device delegate, int number) {
    	this();
        this.delegate = delegate;
        this.number = number;
    }
    
    public Port() {
        inputQueue = new ActivationInputQueue<Frame>();
        outputQueue = new ActivationOutputQueue<Frame>();
        inputQueue.setDelegate(this);
        outputQueue.setDelegate(this);
        outputQueue.start();
        inputQueue.start();
        inStat = 0;
        outStat = 0;
        speed = 10;
        slow = false;
    	linkup = false;
    }
    
    public void setSpeed(int speed) {
    	this.speed = speed;
    }
    
    public int getSpeed() {
    	return this.speed;
    }

    /* Add the received frame to the input queue */
    public void receive(Frame frame) {
        /* Reproduce the transfer delay with sleep & neccesary */
    	if (linkup) {
    		this.using = true;
    	}
        try {
            Thread.sleep(0, 1);
        }catch(InterruptedException e) {
        }
        if (this.inputQueue.put(frame)) {
            inStat += frame.getLength();
        }
        this.using = false;
    }

    /* Add the frame to the output queue */
    public void send(Frame frame) {
        this.outputQueue.put(frame);
    }

    public int getSendingCount() {
        return outputQueue.size();
    }

    /* Take out frame from the output queue and forward it */
    protected void output(Frame frame) {
    	if (!this.isConnected()) {
    		System.out.println("Port not connected.");
    		return;
    	}
        this.using = true;
        if (slow) {
            try {
                Thread.sleep(1);
            }catch(InterruptedException e) {
            }
        }
        outStat += frame.getLength();
        this.connectedPort.receive(frame);
        this.using = false;
    }

    public void connect(Port port) {
        this.connectedPort = port;
    }

    public Boolean isConnected() {
        return (this.connectedPort != null);
    }

    public void fetch(Frame frame) {
        delegate.fetch(frame, this.number);
    }

    public int getNumber() {
        return number;
    }

    public Boolean isUsing() {
        return using;
    }

    private String getByteString(long size) {
        String str;
        if (size >= 1073741824) {
            str = String.format("%.2fGB", (double)size / 1073741824);
        } else if (size >= 1048576) {
            str = String.format("%.2fMB", (double)size / 1048576);
        } else if (size >= 1024) {
            str = String.format("%.2fKB", (double)size / 1024);
        } else {
            str = String.format("%4dByte", size);
        }
        return str;
    }
    
    public boolean getLinkup() {
    	return linkup;
    }

    public void draw(Graphics g) {
        if (using || !isConnected()) {
            g.setColor(Color.BLACK);
        } else if (linkup && connectedPort.getLinkup()) {
        	g.setColor(Color.GREEN);
        } else {
        	g.setColor(Color.ORANGE);
        }
        
        if (delegate.stp.getState(getNumber()) == STP.State.FORWARDING) {
        	this.linkup = true;
        } else {
        	this.linkup = false;
        }

        g.fillOval(-10/2, -10/2, 10, 10);

        if (!isConnected()) return;

        g.setColor(Color.BLACK);
        g.drawString(" " + delegate.stp.getState(getNumber()), 0, 0);
        if (delegate.stp.isDesignatedPort(getNumber())) {
        	g.drawString(" DP", -25, 0);
        }
        g.drawString("↓" + getByteString(inStat), 15, -7);
        g.drawString("↑" + getByteString(outStat), 85, -7);

/*
        g.setColor(Color.GREEN);
        g.drawString(inputQueue.ratioString(), 15, 0);
        g.setColor(Color.BLUE);
        g.drawString(outputQueue.ratioString(), 15 + 70, 0);
*/
    }
}
