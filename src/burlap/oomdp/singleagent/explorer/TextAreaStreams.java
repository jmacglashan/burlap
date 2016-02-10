package burlap.oomdp.singleagent.explorer;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
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

	public TextOut getTout() {
		return tout;
	}

	public TextIn getTin() {
		return tin;
	}

	public void receiveInput(String input){
		area.append("> " + input);
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
		public int read(byte[] b) throws IOException {
			synchronized(inputBuf){
				while(available() == 0){
					try {
						inputBuf.wait();
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}


			}

			return super.read(b);
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return super.read(b, off, len);
		}

		@Override
		public int available() throws IOException {
			return inputBuf.length() - bufIndex;
		}

		@Override
		public int read() throws IOException {

			int b;
			synchronized(inputBuf){

				while(bufIndex - inputBuf.length() == 0){
					try {
						inputBuf.wait();
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}

				char c = inputBuf.charAt(bufIndex);
				b = Character.getNumericValue(c);

				bufIndex++;
				if(bufIndex == inputBuf.length()){
					inputBuf.setLength(0);
					bufIndex = 0;
				}


				inputBuf.notifyAll();
			}

			return b;
		}
	}


}
