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
import javax.swing.*;

class TestCanvas extends Canvas implements MouseListener, MouseMotionListener, MouseWheelListener {
	private RoutingSimulator delegate;
    private double zoom = 1.0;
    private int x0 = 0;
    private int y0 = 0;
    private double zoomRate = Math.pow(2, 0.3);
    private double scaleRate = Math.pow(2, 0.3);
    private DeviceController deviceController;
    private BufferStrategy bufferStrategy;
    private double scale = 10.0;
    private boolean clipping;
    private Rectangle selectedRect;
    private AffineTransform inverseTransform;
    private JPopupMenu popup = new JPopupMenu();
    private Point2D loc;
    
    public TestCanvas(RoutingSimulator delegate) {
    	this.delegate = delegate;
    }

    private JMenuItem addPopupMenuItem(String name, ActionListener al) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(al);
        popup.add(item);
        return item;
    }

    public void init() {
	deviceController = new DeviceController(delegate);
	//deviceController.start();
	selectedRect = new Rectangle(0,0,0,0);
    addPopupMenuItem("デバイス追加（L2）", new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            deviceController.add_l2(loc.getX()/ scale, loc.getY() /scale);
        }
        });
    addPopupMenuItem("デバイス追加（L3）", new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            deviceController.add_l3(loc.getX()/ scale, loc.getY() /scale);
        }
        });
    addPopupMenuItem("デバイス追加（Terminal）", new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            deviceController.add_terminal(loc.getX()/ scale, loc.getY() /scale);
        }
        });
	addPopupMenuItem("全域木構成", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    deviceController.init_spanning_tree();
		}
	    });
    addPopupMenuItem("初期化開始", new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            deviceController.init_route();
        }
        });
	addPopupMenuItem("パケット送信", new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    deviceController.send_packet();
		}
	    });
    addPopupMenuItem("コンソールを開く", new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            deviceController.openConsole();
        }
        });
	/* リスナー登録 */
	addMouseListener(this);
	addMouseMotionListener(this);
	addMouseWheelListener(this);

	/* バッファの設定 */
	createBufferStrategy(2);
	bufferStrategy = getBufferStrategy();
    }

    public void runLoop() {
        while(true) {
            try{
                //Thread.sleep(1000/10);
                Thread.sleep(1000/20);
            } catch (InterruptedException e){}
            repaint();
        }
    }

    /* 描画処理 */
    public void paint(Graphics _g) {
	Graphics g = bufferStrategy.getDrawGraphics();
	AffineTransform af = new AffineTransform();
	Graphics2D g2 = (Graphics2D)g;

	if (!bufferStrategy.contentsLost()) {
	    g.setColor(Color.white);
	    g.fillRect(0, 0, getSize().width, getSize().height);
	    g.setColor(Color.black);

	    drawCoordinate(g);

	    /* 座標軸表示 */
	    drawAxis(g);

	    /* 表示位置移動変換 */
	    af.scale(1.0, -1.0);
	    af.translate(0, -getSize().height);
	    /* 中心を合わせる */
	    af.translate(getSize().width/2 - (x0)*zoom, getSize().height/2 - (y0)*zoom);
	    /*スケーリング変換 */
	    af.scale(zoom, zoom);

	    g2.setTransform(af);

	    try {
		inverseTransform = af.createInverse();
	    } catch(NoninvertibleTransformException e) {

	    }
	    draw(g);
	    bufferStrategy.show();
	    g.dispose();
	}
    }

    /* 現在の画面中央の座標を表示 */
    public void drawCoordinate(Graphics g) {
	Graphics2D g2 = (Graphics2D)g;
	g.setColor(new Color(238, 238, 238));
	g.fillRect(0, getSize().height-10, getSize().width, 10);
	g.setColor(Color.black);
	g2.drawString("x0: "+x0/10+" y0: "+y0/10, getSize().width - 120, getSize().height);
	g.setClip(0,0,getSize().width,getSize().height-10);
    }
    /* 画面に表示されるグラフ領域 */
    public Rectangle getDrawingRect() {
	return new Rectangle(-(-x0) - (int)(400/zoom), y0 - (int)(400/zoom),
			     (int)(800 / zoom), (int)(800 / zoom));
    }

    public void draw(Graphics g) {
	Graphics2D g2 = (Graphics2D)g;
	boolean desc = true;
	int count;
	if (/*(count = countDevices(g)) > 64 ||*/ zoom < 0.5 || scale < 5) {
	    desc = false;
	}
	drawDevices(g,  desc);
	/* 選択範囲表示 */
	drawSelectedRect(g);
    }

    public void drawSelectedRect(Graphics g) {
	g.drawRect((int)(selectedRect.x), (int)(selectedRect.y),
		   (int)(selectedRect.width), (int)(selectedRect.height));
	g.setColor(Color.BLACK);
    }

    public void drawDevices(Graphics g, boolean desc) {
	deviceController.draw(g, zoom, scale, desc);
    }

    public void drawConnection(Graphics g, Point p1, Point p2) {
	//	    g.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    public void drawAxis(Graphics g) {
	Graphics2D g2 = (Graphics2D)g;
	/* ワールド座標 */
	AffineTransform aG = g2.getTransform();

	Rectangle rect = getDrawingRect();
	g2.setStroke(new BasicStroke(2.0f));

	/* 縦軸用ローカル座標変換 */
	AffineTransform aL = new AffineTransform(aG);
	if (y0*zoom < getSize().height/2 - 30) {
	    aL.translate(getSize().width/2 + (-x0)*zoom, getSize().height/2 + y0*zoom);
	} else {
	    aL.translate(getSize().width/2 + (-x0)*zoom, getSize().height - 30);
	}
	g2.setTransform(aL);

	/* 横軸座標系における原点を求める */
	Point2D h0 = aL.transform(new Point(0,0), null);

	/* 横軸の描画 */
	g.setColor(Color.RED);
	g.drawLine(-10000,0,10000,0);

	int aScale = 1;

	if (zoom < 0.5) {
	    aScale = 10;
	}
	if (zoom < 0.05) {
	    aScale = 100;
	}

	for (int i=0;i<=100;i++) {
	    int aSize = 10;
	    if (i%10 == 0) {
		aSize = 10;
		g2.drawString(Integer.toString(i*aScale), (int)(i*scale*aScale*zoom), 20);
	    } else if (i%5 == 0) {
		aSize = 7;
		g2.drawString(Integer.toString(i*aScale), (int)(i*scale*aScale*zoom), 20);
	    } else {
		aSize = 3;
	    }
	    g.drawLine((int)(i*scale*aScale*zoom),0,(int)(i*scale*aScale*zoom),-aSize);
	}

	/* ワールド座標に戻す */
	g2.setTransform(aG);

	/* 縦軸用ローカル座標変換 */
	aL = new AffineTransform(aG);
	if ((-x0)*zoom > -(getSize().width/2 - 20)) {
	    aL.translate(getSize().width/2 + (-x0)*zoom, getSize().height/2 + y0*zoom);
	} else {
	    aL.translate(20, getSize().height/2 + y0*zoom);
	}
	aL.rotate(-Math.PI/2);
	g2.setTransform(aL);

	/* 縦軸座標系における原点を求める */
	Point2D v0 = aL.transform(new Point(0,0), null);

	/* 縦軸の描画 */
	g.setColor(Color.BLUE);
	g.drawLine(-10000,0,10000,0);
	for (int i=0;i<=100;i++) {
	    int aSize = 10;
	    if (i%10 == 0) {
		aSize = 10;
		g2.drawString(Integer.toString(i*10), (int)(i*scale*10*zoom), -10);
	    } else if (i%5 == 0) {
		aSize = 7;
	    } else {
		aSize = 3;
	    }
	    g.drawLine((int)(i*scale*10*zoom),0,(int)(i*scale*10*zoom),aSize);
	}
	g.setColor(Color.BLACK);

	/* ワールド座標に戻す */
	g2.setTransform(aG);

	/* 座標軸系でのx>0, y>0でクリッピング */
	if (clipping) {
	    g.setClip((int)v0.getX(), 0, getSize().height, (int)h0.getY());
	}
	g2.setStroke(new BasicStroke(1.0f));
    }

    public void setDrawOval(boolean drawOval) {
	deviceController.drawOval = drawOval;
    }
    public void setDrawDescription(boolean drawDescription) {
	deviceController.drawDescription = drawDescription;
    }
    public void setClipping(boolean clipping) {
	this.clipping  = clipping;
    }

    public void add(int x, int y) {
	deviceController.add(x, y);
    }
    public void add() {
	add(0, 0);
    }

    private int _x, _y, _x0, _y0;
    private Point2D p0, p1;
    private int mode = 0;
    public void mousePressed(MouseEvent e) {
	Point p = e.getPoint();
	_x = p.x; _y = p.y;
	_x0 = x0; _y0 = y0;
	mode = (e.getModifiers() & 3) ;
	p1 = inverseTransform.transform(new Point(_x, _y), null);
	if (deviceController.contains(p1, scale, zoom)) {
	    p0 = new Point2D.Double(selectedRect.x, selectedRect.y);
	    mode = 2;
	}
    }

    public void mouseDragged(MouseEvent e) {
	Point p = e.getPoint();
	double diffX = 0, diffY = 0;
	if ((mode & 3) != 0) {
	    if (p.x > getSize().width - 20) {
		x0 += (int)((10)/zoom);
		diffX = 10;
	    }
	    if (p.x < 20) {
		x0 -= (int)((10)/zoom);
		diffX = -10;
	    }
	    if (p.y > getSize().height - 20) {
		y0 -= (int)((10)/zoom);
		diffY = -10;
	    }
	    if (p.y < 20) {
		y0 += (int)((10)/zoom);
		diffY = 10;
	    }
	}

	if ((mode & 3) == 1) {
	    Point2D p2 = inverseTransform.transform(p, null);

	    double tmp;
	    if (p1.getX() < p2.getX()) {
		selectedRect.x = (int)(p1.getX());
	    } else {
		selectedRect.x = (int)(p2.getX());
	    }
	    if (p1.getY() < p2.getY()) {
		selectedRect.y = (int)(p1.getY());
	    } else {
		selectedRect.y = (int)(p2.getY());
	    }

	    selectedRect.width = (int)Math.abs((p2.getX() - p1.getX()));
	    selectedRect.height = (int)Math.abs((p2.getY() - p1.getY()));
	    deviceController.selectInRect(selectedRect, scale);
	} else if ((mode & 3) == 2) {
	    Point2D p2 = inverseTransform.transform(p, null);

	    deviceController.moveSelectedDevice(scale, (int)((p.x - _x + diffX)/zoom), -(int)((p.y - _y - diffY)/zoom));
	    selectedRect.x = (int)(p0.getX() + (p2.getX() - p1.getX()));
	    selectedRect.y = (int)(p0.getY() + (p2.getY() - p1.getY()));
	    _x = p.x; _y = p.y;
	} else {
	    x0 =  _x0 - (int)((p.x - _x)/zoom);
	    y0 =  _y0 + (int)((p.y - _y)/zoom);
	    if (x0 < 0) { x0 = 0; }
	    if (y0 < 0) { y0 = 0; }
	}
    }

    public void mouseReleased(MouseEvent e) {
	Point p = e.getPoint();
	mode = 0;
	selectedRect.x = 0;
	selectedRect.y = 0;
	selectedRect.width = 0;
	selectedRect.height = 0;
    }

    public void mouseMoved(MouseEvent e) {

    }
    public void mouseExited(MouseEvent e) {

    }
    public void mouseEntered(MouseEvent e) {

    }
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
	        loc = inverseTransform.transform(new Point2D.Double(e.getX(), e.getY()), null);
            popup.show(e.getComponent(), e.getX(), e.getY());
        } else {
            deviceController.selectInRect(selectedRect, scale);
        }
    }

    int modifiersBuf = -1;
    public void mouseWheelMoved(MouseWheelEvent e) {
	_x = e.getPoint().x; _y = e.getPoint().y;
	if (modifiersBuf != (modifiersBuf = e.getModifiers())) {
	    return;
	}

	if (e.getModifiers() == 0) {
	    if (e.getWheelRotation()> 0) {
		zoom /= zoomRate;
	    } else {
		zoom *= zoomRate;
	    }
	    if (zoom > 2) { zoom = 2; }
	    if (zoom < 0.02) { zoom = 0.02; }
	} else if(e.getModifiers() == 2) {
	    if (e.getWheelRotation() > 0) {
		scale /= scaleRate;
	    } else {
		scale *= scaleRate;
	    }
	    if (scale > 10) { scale = 10; }
	    if (scale < 1) { scale = 1; }
	}

    }
    
    public void execute(String command) {
    	deviceController.execute(command);
    }
}
