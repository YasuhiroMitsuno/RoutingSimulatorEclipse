import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.Canvas;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.Point;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.BasicStroke;
import java.util.*;
import java.awt.image.*;
import java.util.regex.*;

import network.device.Device;
import network.datagram.L2.Frame;
import network.datagram.L3.Packet;
import network.device.L2Switch;
import network.device.L2SwitchFactory;
import network.device.L3Switch;
import network.device.L3SwitchFactory;
import network.device.Terminal;
import network.device.TerminalFactory;

class DeviceController extends Thread {
	RoutingSimulator delegate;
    List<Device> devices;
    List<Device[]> connection;
    List<Integer[]> offset;
    boolean drawOval = false;
    boolean drawDescription = false;

    L2SwitchFactory l2SwitchFactory;
    L3SwitchFactory l3SwitchFactory;    
    TerminalFactory terminalFactory;
    
    public DeviceController() {
	devices = new ArrayList<Device>();
	offset = new ArrayList<Integer[]>();        
	connection = new ArrayList<Device[]>();
	l2SwitchFactory = new L2SwitchFactory();	
	l3SwitchFactory = new L3SwitchFactory();	
	terminalFactory = new TerminalFactory();	
//    this.start();
    }
    
    public DeviceController(RoutingSimulator delegate) {
    	this();
    	this.delegate = delegate;
    }

    public void run() {
        while(true) {
            try{	
                Thread.sleep(1000);
            } catch (InterruptedException e){}
            for(int i=0;i<devices.size();i++) {
                Device d = devices.get(i);
  //              d.position.setLocation(d.position.getX()+Math.random() - 0.5, d.position.getY()+Math.random() - 0.5);
                if (d.selected) {
                	delegate.setLog(d.getLog());
                }
            }

//	        reconnect();
        }
    }

    public void add(double x, double y) {
	Device nd = l2SwitchFactory.create(x, y);//new Terminal(x, y);
        nd.MTU = 1500;
        //	nd.start();
        devices.add(nd);

	for(int i=0;i<devices.size();i++) {
	    Device d = devices.get(i);
	    if (nd == d) continue;
	    if (distance(nd, d) <= 20 && distance(nd,d) < 20) {
		//		if (nd.connectionSize() >= 3 || d.connectionSize() >= 3) continue;
                Integer a[] = new Integer[2];
                a[0] = nd.getNumber();
                a[1] = d.getNumber();
                offset.add(a);
		Device devices[] = new Device[2];
		devices[0] = nd;
		devices[1] = d;
		connection.add(devices);
                
                Device.connect(nd, d);                
	    }
	}
    }
    
    public void add_device(Device device, double x, double y) {
        device.MTU = 1500;
    //    nd.start();
        
        devices.add(device);
        int c=0;
		for(int i=0;i<devices.size();i++) {
		    Device d = devices.get(i);
		    if (device == d) continue;
		    if (distance(device, d) <= 20 && distance(device,d) < 20) {
			//		if (nd.connectionSize() >= 3 || d.connectionSize() >= 3) continue;
	
                Integer a[] = new Integer[2];
                a[0] = device.getNumber();
                a[1] = d.getNumber();
                offset.add(a);
				Device devices[] = new Device[2];
				devices[0] = device;
				devices[1] = d;
				connection.add(devices);
		                
	            Device.connect(device, d);                
		    }
		}
    }

    public void add_l2(double x, double y) {
        L2Switch l2Switch = (L2Switch)l2SwitchFactory.create(x, y);
        add_device(l2Switch, x, y);
    }
    
    public void add_l3(double x, double y) {
        L3Switch l3Switch = (L3Switch)l3SwitchFactory.create(x, y);
        add_device(l3Switch, x, y);
    }
    
    public void add_terminal(double x, double y) {
    	Terminal terminal = (Terminal)terminalFactory.create(x, y);
        add_device(terminal, x, y);
    }

    public void reconnect() {
        /*
        connection.clear();
    	for(int i=0;i<devices.size();i++) {
    	    Device d = devices.get(i);
    	    for(int j=i;j<devices.size();j++) {
		Device nd = devices.get(j);
		if (nd == d) continue;
		if (distance(nd, d) <= 20 && distance(nd,d) < 20) {
                    Device devices[] = new Device[2];
                    devices[0] = nd;
                    devices[1] = d;
                    connection.add(devices);
                    if (d.has_connection(nd)) continue;
		    //		if (nd.connectionSize() >= 3 || d.connectionSize() >= 3) continue;
		    nd.add_connection(d);
		    d.add_connection(nd);


		} else {
                    if (d.has_connection(nd)) {
                        d.delete_connection(nd);
                        nd.delete_connection(d);
                    }
                }
            }
    	}
        */
    }

    public void init_spanning_tree() {
        /*
        for(int i=0;i<devices.size();i++) {
            Device d = devices.get(i);
            if (d.selected) {
                d.init();
                break;
            }
        }
        */
    }
    public void init_route() {
        /*
        for(int i=0;i<devices.size();i++) {
            Device d = devices.get(i);
            if (d.selected) {
                d.init2();
                break;
            }
        }
        */
    }
    public void openConsole() {
        for(int i=0;i<devices.size();i++) {
            Device d = devices.get(i);
            if (d.selected) {
            	Console console = new Console(d);
                break;
            }
        }
    }
    public double distance(Device d1, Device d2) {
	return Math.sqrt(Math.pow(d1.position.getX() - d2.position.getX(), 2) +
			 Math.pow(d1.position.getY() - d2.position.getY(), 2));
    }
    public void draw(Graphics g, double zoom, double scale, boolean desc) {
	show(g, scale);

	for(int i=0;i<devices.size();i++) {
	    devices.get(i).draw(g, scale,  zoom, drawOval);
	}
        /*
	if (drawDescription) {
	    for(int i=0;i<devices.size();i++) {
		devices.get(i).drawDescription(g, scale,  zoom);
	    }
	}
        */
    }

    
    private boolean isParent(Device d1, Device d2) {
        /*
        Pattern p = Pattern.compile(d1.addr+",[0-9]+$");
        Matcher m = p.matcher(d2.addr);
        if (m.find()) return true;
        p = Pattern.compile(d2.addr+",[0-9]+$");
        m = p.matcher(d1.addr);
        return m.find();
        */
        //        return d1.port.get_port(d2) == 0 || d2.port.get_port(d1) == 0;
        return false;
    }
    public void show(Graphics g, double scale) {
	Graphics2D g2 = (Graphics2D)g;
	AffineTransform af = g2.getTransform();
	for (int i=0;i<connection.size();i++) {
	    Device d1 = connection.get(i)[0];
	    Device d2 = connection.get(i)[1];
	    Point2D p1 = af.transform(d1.position, null);
	    Point2D p2 = af.transform(d2.position, null);
            /*
	    if (d1.stream_device == d2 || d2.stream_device == d1) {
		g.setColor(Color.GREEN);
		g2.setStroke(new BasicStroke(25.0f));
	    } else if (isParent(d1, d2)) {
		g.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(1.5f));
	    }  else {
		g.setColor(Color.GRAY);
        float dash[] = {3.0f, 5.0f};
		g2.setStroke(new BasicStroke(0.5f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,2.0f,dash,0.0f));
	    }
            */
            g.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1.5f));
                
	    g.drawLine((int)(d1.position.getX()*scale),(int)(d1.position.getY() *scale - 15 * offset.get(i)[0]),
                (int)(d2.position.getX()*scale),(int)((d2.position.getY() *scale - 15 * offset.get(i)[1])));

        double dx = d2.position.getX() - d1.position.getX();
        double dy = d2.position.getY() - d1.position.getY();
        double norm = Math.sqrt(dx*dx + dy*dy);
		g.setColor(Color.magenta);
        AffineTransform aG = g2.getTransform();
        /* ローカル座標 */
        AffineTransform aL = new AffineTransform(aG);
        // if (d1.port.get_port(d2) != -1) {
        //     /* 上下反転ローカル変換 */
        //     aL.scale(1.0, -1.0);
        //     aL.translate((int)(d1.position.getX() *scale), -(int)(d1.position.getY()*scale));
        //     g2.setTransform(aL);
        //     g2.drawString(Integer.toString(d1.port.get_port(d2)), + (int)(dx*15/norm) -4 - (int)(dy*8/norm), - (int)(dy*15/norm) + 5 - (int)(dx*8/norm));
        //     g2.setTransform(aG);
        // }
        // if (d2.port.get_port(d1) != -1) {
        //     aL = new AffineTransform(aG);
        //     /* 上下反転ローカル変換 */
        //     aL.scale(1.0, -1.0);
        //     aL.translate((int)(d2.position.getX() *scale), -(int)(d2.position.getY()*scale));
        //     g2.setTransform(aL);
        //     g2.drawString(Integer.toString(d2.port.get_port(d1)), - (int)(dx*15/norm) -4 + (int)(dy*8/norm), (int)(dy*15/norm) + 5 + (int)(dx*8/norm));
        //     g2.setTransform(aG);
        // }
	}
	g.setColor(Color.BLACK);
	g2.setStroke(new BasicStroke(1.0f));
    }
    public void selectInRect(Rectangle selectedRect, double scale) {
	for(int i=0;i<devices.size();i++) {
	    Device d = devices.get(i);
	    if (selectedRect.contains(d.position.getX()*scale, d.position.getY()*scale)) {
		d.selected = true;
	    } else {
		d.selected = false;
	    }
	}
    }

    public void execute(String command) {
    	for(int i=0;i<devices.size();i++) {
    	    Device d = devices.get(i);
    	    if (d.selected) {
    	    	d.execute(command);
    	    }
    	}
    }
    
    public void moveSelectedDevice(double scale, int dx, int dy) {
	for(int i=0;i<devices.size();i++) {
	    Device d = devices.get(i);
	    if (d.selected) {
		d.position.setLocation(d.position.getX()+dx/(scale), d.position.getY()+dy/(scale));
	    }
	}
	reconnect();
    }
    public boolean contains(Point2D p, double scale,  double zoom) {

	for(int i=0;i<devices.size();i++) {
	    Device d = devices.get(i);
	    if (p.distance(d.position.getX()*scale, d.position.getY()*scale) < 14/zoom) {
		return true;
	    }
	}
	return false;
    }

    public void send_packet() {

        for(int i=0;i<devices.size();i++) {
            Device d = devices.get(i);
            if (d.selected) {
                //                    Device to = devices.get((int)(Math.random()*(double)devices.size()));
                byte[] bytes = { (byte)0x45, (byte)0x00, (byte)0x00, (byte)0x28,
                                 (byte)0xe6, (byte)0x0f, (byte)0x40, (byte)0x00,
                                 (byte)0x40, (byte)0x06, (byte)0x35, (byte)0x33,
                                 (byte)0xc0, (byte)0xa8, (byte)0x0b, (byte)0x07,
                                 (byte)0x11, (byte)0x9a, (byte)0x42, (byte)0x44,
                                 (byte)0xc3, (byte)0x2d, (byte)0x01, (byte)0xbb,
                                 (byte)0xf0, (byte)0xf0, (byte)0xa6, (byte)0xbe,
                                 (byte)0x2a, (byte)0x0b, (byte)0xbf, (byte)0xdc,
                                 (byte)0x50, (byte)0x10, (byte)0x20, (byte)0x00,
                                 (byte)0x29, (byte)0xc7, (byte)0x00, (byte)0x00
                };
                Packet packet = new Packet(bytes);
                packet.setData(new byte[65535 - 20]);
                packet.setDestination("192.168.0.2");
                packet.setSource("10.0.0.2");
                packet.setTTL(2);
                Frame frame = new Frame();
                frame.setData(packet.getBytes());
                SendFrameThread sfThread = new SendFrameThread();
                sfThread.setDelegate(d);
                sfThread.setFrame(frame);
                sfThread.start();
                //		        Device to = devices.get(j);
                //                      d.send(to.get_label());
            }
        }
    }

    
    class SendFrameThread extends Thread {
        private Device delegate;
        private Frame frame;
        
        void setDelegate(Device delegate) {
            this.delegate = delegate;
        }
        
        void setFrame(Frame frame) {
            this.frame = frame;
        }
        public void run() {
            for (int i=0;i<1;i++) {
                while(delegate.getSendingCount() > 0) {
                    try {
                        Thread.sleep(50);
                    }catch(InterruptedException e) {
                    }
                }
                delegate.sendFrame(i, frame);
            }
        }
    }

    // class Device {
    // private int dotSize = 7;
    // private Point2D position;
    // private int addr;
    // public double length = 20.0;
    // public boolean selected = false;
    // String name;
    // List<Device> cDevices; /* 接続しているデバイスリスト */
    //
    // public Device() {
    //     position = new Point2D.Double(0,0);
    //     name = "192.168.11.1a";
    //     cDevices = new ArrayList<Device>();
    // }
    // public Device(double x, double y) {
    //     position = new Point2D.Double(x,y);
    //     name = "192.168.11.1a";
    //     cDevices = new ArrayList<Device>();
    // }
    // public String getAddr() {
    //     return name;
    // }
    // public Point2D getPosition() {
    //     return position;
    // }
    // public void draw(Graphics g, double scale, double zoom, boolean drawOval) {
    //     Graphics2D g2 = (Graphics2D)g;
    //     /* ワールド座標 */
    //     AffineTransform aG = g2.getTransform();
    //     /* ローカル座標 */
    //     AffineTransform aL = new AffineTransform(aG);
    //
    //     /* 上下反転ローカル変換 */
    //     aL.scale(1.0, -1.0);
    //
    //     aL.translate((int)(position.getX() *scale), -(int)(position.getY()*scale));
    //
    //
    //     Point2D pT = aL.transform(new Point(0,0), null);
    //
    //     if ((pT.getX() < -length*scale*zoom || pT.getX() > 800 + length*scale*zoom ||  pT.getY() < -length*scale*zoom || pT.getY() > 800 + length*scale*zoom)) {
    // 	return;
    //     }
    //
    //
    //     g2.setTransform(aL);
    //
    //     g.fillOval(-dotSize/2, -dotSize/2, dotSize, dotSize);
    //     if (selected) {
    // 	g2.setColor(Color.GREEN);
    // 	g.fillOval(-(int)(dotSize*2/Math.sqrt(zoom)), -(int)(dotSize*2/Math.sqrt(zoom)), (int)(dotSize*4/Math.sqrt(zoom)), (int)(dotSize*4/Math.sqrt(zoom)));
    // 	g2.setColor(Color.BLACK);
    //     } else {
    // 	g2.setColor(Color.BLACK);
    //     }
    //
    //
    //     if (drawOval || selected) {
    // 	g.drawOval((int)(-length*scale),(int)(-length*scale),(int)(length*scale*2),(int)(length*scale*2));
    //     }
    //
    //     /* ワールド座標に戻す */
    //     g2.setTransform(aG);
    //     g2.setColor(Color.BLACK);
    // }
    // public void drawDescription(Graphics g, double scale, double zoom) {
    //     Graphics2D g2 = (Graphics2D)g;
    //     /* ワールド座標 */
    //     AffineTransform aG = g2.getTransform();
    //     /* ローカル座標 */
    //     AffineTransform aL = new AffineTransform(aG);
    //
    //     /* 上下反転ローカル変換 */
    //     aL.scale(1.0, -1.0);
    //
    //     aL.translate((int)(position.getX() *scale), -(int)(position.getY()*scale));
    //
    //
    //     Point2D pT = aL.transform(new Point(0,0), null);
    //
    //     if ((pT.getX() < -length*scale*zoom || pT.getX() > 800 + length*scale*zoom ||  pT.getY() < -length*scale*zoom || pT.getY() > 800 + length*scale*zoom)) {
    // 	return;
    //     }
    //
    //     g2.setTransform(aL);
    //
    //     /* 詳細の表示 */
    //     g2.setColor(Color.BLUE);
    //     g2.drawString(name,  10,  -14);
    //     g2.drawString("["+position.getX()+","+position.getY()+"]", 10, 0);
    //
    //     g2.setColor(Color.BLACK);
    //
    //     /* ワールド座標に戻す */
    //     g2.setTransform(aG);
    //     g2.setColor(Color.BLACK);
    // }
    // public void addConnection(Device d) {
    //     cDevices.add(d);
    // }
    // public void clearConnection() {
    //     cDevices.clear();
    // }
    // public int connectionSize() {
    //     return cDevices.size();
    // }
    // }
}
