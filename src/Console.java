import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import network.datagram.L2.Util;
import network.device.Device;

public class Console extends JFrame implements KeyListener {
	JTextArea textArea;
	JScrollPane scrollPane;
//	ConsoleThread consoleThread;
	Device device;
	
	final ByteArrayOutputStream bytes = new ByteArrayOutputStream() {
        @Override
        public synchronized void flush() throws IOException {
            textArea.append(toString());
            reset();
            textArea.setCaretPosition(textArea.getText().length());
            SwingUtilities.invokeLater(new Runnable() {
            	@Override
            	public void run() {
            		JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
            		scrollBar.setValue(scrollBar.getMaximum());
            		
            	}
            });
        }
    };
    


	public Console(Device device) {
		PrintStream out = new PrintStream(bytes, true);
		System.setOut(out);
		this.device = device;
		this.setLayout(null);
		this.setBounds(100,100,400,200);
		this.setTitle("Console " + Util.long2Addr(device.getMACAddress()));
		this.setVisible(true);
		
		textArea = new JTextArea();
		scrollPane = new JScrollPane(textArea);
		scrollPane.setBounds(0, 0, 400, 200);
		scrollPane.setAutoscrolls(true);
		textArea.addKeyListener(this);
		this.add(scrollPane);
		echo("Console> ");
//		consoleThread = new ConsoleThread(this);
//		consoleThread.start();
		
	}
	
	public void echo(String str) {
		textArea.append(str);
	}
	
	String command = "";
	
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			if (command.length() > 1) {
				command = command.substring(0, command.length()-1);
			} else {
				command = "";
				e.consume();
			}
        } else if(e.getModifiers() != KeyEvent.CTRL_MASK && e.getModifiers() != KeyEvent.ALT_MASK && e.getModifiers() != KeyEvent.META_MASK) {
        	command = command + e.getKeyChar();
        }
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (command != "") {
				try {
					ConsoleThread thread = new ConsoleThread(device, command);
					thread.start();
					thread.join();
					echo("Console> ");
		            scrollPane.scrollRectToVisible(new Rectangle(0, Integer.MAX_VALUE - 1, 1, 1));
				} catch (InterruptedException ex) {
					
				}
			}
			command = "";
		}
	}
	
	class ConsoleThread extends Thread {
		Device device;
		String command;
		
		public ConsoleThread(Device device, String command) {
			this.device = device;
			this.command = command;
		}
		
		public void run() {
			 device.execute(command);
		}
	}

}
