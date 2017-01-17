import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class RoutingSimulator extends JFrame implements ComponentListener, ChangeListener, KeyListener {
    private TestCanvas canvas;
    private JScrollPane scrollPane;
    private JTextArea textArea;
    private JCheckBox checkBox[];
    private String command = "";

    public static void main(String args[]) {
	RoutingSimulator simulator = new RoutingSimulator();
	simulator.init();
	simulator.runLoop();
    }

    public RoutingSimulator() {
	this.setLayout(null);
	this.setBounds(100,100,700,700);

	//キャンバスの作成
	canvas = new TestCanvas(this);
	this.add(canvas);
	canvas.setBounds(190,10,500,500);

	JPanel panel = new JPanel();
	checkBox = new JCheckBox[3];
	String[] str = new String[3];
	str[0] = "アドレス ";
	str[1] = "範囲　　 ";
	str[2] = "座標0以下";
	for(int i=0;i<3;i++) {
	    checkBox[i] = new JCheckBox(str[i]);
	    checkBox[i].addChangeListener(this);
	    panel.add(checkBox[i]);
	}

	textArea = new JTextArea("TEST", 5, 100);
//	textArea.setEditable(false);
	scrollPane = new JScrollPane(textArea);
	scrollPane.setBounds(5, 520, 690, 150);
	scrollPane.setAutoscrolls(true);
	
	this.add(scrollPane);
	
	panel.setBounds(0,0,100,100);
	this.add(panel);
	/* リスナー登録 */
	addComponentListener(this);
	
	textArea.addKeyListener(this);

	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.setVisible(true);
    }

    public void R(int x, int y, int w, int h, int c) {
	if (c <= 2) {
	    for (int i=0;i<c;i++) {
		int rx = (int)(Math.random() * w + x);
		int ry = (int)(Math.random() * h + y);
		canvas.add(rx, ry);
	    }
	    return;
	}
	R(x, y, w/2, h/2, c/4);
	R(x + w/2, y, w/2, h/2, c/4);
	R(x, y + h/2, w/2, h/2, c/4);
	R(x + w/2, y + h/2, w/2, h/2, c/4);
    }

    public void init() {
	System.out.println("init");

	canvas.init();
	//	canvas.add();
	//R(10,10,60,60, 4);
	//R(0,0,150,150, 100);
	//R(0,500,500,500, 4*4*4*4*4);
	//R(0,4000,4000,4000, 4*4*4*4*4*4*4*4);
	/*
	  for(int i=0;i<500;i++) {
	  int rx = (int)(Math.random() * 300);
	  int ry = (int)(Math.random() * 300);
	  canvas.add(rx, ry);
	  }
	*/
	checkBox[0].setSelected(true);
    }

    public void runLoop() {
	System.out.println("loop");
	canvas.runLoop();	
    }
    
    public void setLog(String log) {
    	boolean auto = false;
    	JScrollBar scrollBar = scrollPane.getVerticalScrollBar();    		    	
    	if (scrollBar.getValue() == scrollBar.getMaximum()) {
    		auto = true;
    	}
    	this.textArea.setText(log);
    	if (auto) {
    		scrollBar.setValue(scrollBar.getMaximum());
    	}
    }

    /* コンポーネントリスナー */
    public void componentHidden(ComponentEvent e) {}

    public void componentMoved(ComponentEvent e) {}

    public void componentResized(ComponentEvent e) {
    	canvas.setSize(getWidth()-200,getHeight()-200);
    	scrollPane.setBounds(5, getHeight() - 180, getWidth() - 10, scrollPane.getHeight());
    }

    public void componentShown(ComponentEvent e) {}

    /* チェンジリスナー */
    public void stateChanged(ChangeEvent e) {
	if (e.getSource() == checkBox[0]) {
	    canvas.setDrawDescription(checkBox[0].isSelected());
	}
	if (e.getSource() == checkBox[1]) {
	    canvas.setDrawOval(checkBox[1].isSelected());
	}
	if (e.getSource() == checkBox[2]) {
	    canvas.setClipping(checkBox[2].isSelected());
	}
    }
    
	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			System.out.println(command);
			canvas.execute(command);
			command = "";
        } else if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			System.out.println(e.getKeyCode());
			if (command.length() > 1) {
				command = command.substring(0, command.length()-1);
			} else if (command.length() > 0) {
				command = "";
			}
        } else {
        	command = command + e.getKeyChar();
        }
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
