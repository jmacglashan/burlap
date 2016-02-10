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
		area.append(input + "\n");
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
		public int read(byte b[], int off, int len) throws IOException {
			if (b == null) {
				throw new NullPointerException();
			} else if (off < 0 || len < 0 || len > b.length - off) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return 0;
			}

			int c = read();
			if (c == -1) {
				return -1;
			}
			b[off] = (byte)c;

			int i = 1;
			try {
				for (; i < len ; i++) {
					c = read();
					if (c == -1) {
						break;
					}
					b[off + i] = (byte)c;
				}
			} catch (IOException ee) {
			}
			return i;
		}

		@Override
		public int read() throws IOException {

			//System.out.println("Read request");

			int b;
			synchronized(inputBuf){

				while(inputBuf.length() == 0){
					try {
						inputBuf.wait();
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}


				char c = inputBuf.charAt(bufIndex);
				b = (int)c;

				bufIndex++;
				if(bufIndex == inputBuf.length()){
					inputBuf.setLength(0);
					bufIndex = 0;
				}


				inputBuf.notifyAll();
			}

			System.out.println("Read byte " + b);

			return b;
		}
	}


}
