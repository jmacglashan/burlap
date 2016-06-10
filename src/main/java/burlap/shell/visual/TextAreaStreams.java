package burlap.shell.visual;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Object that provides an {@link java.io.InputStream} and {@link java.io.OutputStream} that affect the content
 * of a {@link javax.swing.JTextArea}. Specifically, this code is used to instantiate a GUI for the {@link burlap.shell.EnvironmentShell}
 * used by a {@link VisualExplorer}.
 * @author James MacGlashan.
 */
public class TextAreaStreams {

	protected JTextArea area;

	protected StringBuffer inputBuf = new StringBuffer();
	protected int bufIndex = 0;

	protected TextOut tout = new TextOut();
	protected TextIn tin = new TextIn();

	public TextAreaStreams(JTextArea area) {
		this.area = area;
	}

	/**
	 * Returns the {@link java.io.OutputStream} for the {@link javax.swing.JTextArea}
	 * @return the {@link TextAreaStreams.TextOut} {@link java.io.OutputStream}
	 */
	public TextOut getTout() {
		return tout;
	}


	/**
	 * Returns the {@link java.io.InputStream} for the {@link javax.swing.JTextArea}.
	 * @return the {@link TextAreaStreams.TextIn} {@link java.io.InputStream}
	 */
	public TextIn getTin() {
		return tin;
	}


	/**
	 * Adds data to the {@link java.io.InputStream}
	 * @param input the string data to add.
	 */
	public void receiveInput(String input){
		area.append(input);
		synchronized(inputBuf){
			inputBuf.append(input);
			inputBuf.notifyAll();
		}
	}

	public class TextOut extends OutputStream{

		@Override
		public void write(byte[] b) throws IOException {
			String s = new String(b);
			area.append(s);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			String s = new String(b, off, len);
			area.append(s);
		}

		@Override
		public void write(int b) throws IOException {
			String s = new String(new byte[]{(byte)b});
			area.append(s);
		}
	}


	public class TextIn extends InputStream{


		@Override
		public int read() throws IOException {

			int b;
			synchronized(inputBuf){

				while(inputBuf.length() == 0){
					try {
						inputBuf.wait();
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}


				if(bufIndex == inputBuf.length()){
					b = -1;
				}
				else {
					char c = inputBuf.charAt(bufIndex);
					b = (int) c;
				}

				bufIndex++;
				if(bufIndex == inputBuf.length()+1){
					inputBuf.setLength(0);
					bufIndex = 0;
				}


				inputBuf.notifyAll();
			}

			return b;
		}
	}


}
