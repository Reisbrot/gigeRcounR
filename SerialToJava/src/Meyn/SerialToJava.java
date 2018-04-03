package Meyn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;


public class SerialToJava implements SerialPortEventListener{
	SerialPort serialPort;
        final double initTime = System.currentTimeMillis();
        static long timeElapsed = 0;
        static int totalCounts;
        static int tempCounts;
        static int CPM;
        
        /** The port we're normally going to use. */
	private static final String PORT_NAMES[] = { 
			"/dev/tty.usbserial-A9007UX1", // Mac OS X
                        "/dev/ttyACM0", // Raspberry Pi
			"/dev/ttyUSB0", // Linux
			"COM5", // Windows
	};
	/**
	* A BufferedReader which will be fed by a InputStreamReader 
	* converting the bytes into characters 
	* making the displayed results codepage independent
	*/
	private BufferedReader input;
	/** The output stream to the port */
	private OutputStream output;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;

	public void initialize() {
                // the next line is for Raspberry Pi and 
                // gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
                //Für RB: System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
                                tempCounts++;
				String inputLine=input.readLine();
				System.out.print(inputLine + " at ");
                                timeElapsed = (long) (System.currentTimeMillis() - initTime);
                                System.out.println(timeElapsed);    
                                beep();
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}

	public static void main(String[] args) throws Exception {
		SerialToJava main = new SerialToJava();
		main.initialize();
		Thread t=new Thread() {
			public void run() {
				//the following line will keep this app alive for 1000 seconds,
				//waiting for events to occur and responding to them (printing incoming messages to console).
				try {Thread.sleep(1000000);} catch (InterruptedException ie) {}
			}
		};
		t.start();
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask(){
                @Override
                public void run() {
                    CPM = tempCounts;
                    totalCounts += tempCounts;
                    tempCounts = 0;
                    logInfo();
                }
                },60000,60000); //Effri Minädt
		System.out.println("Started");
	}
        
        static void logInfo(){
            String minutes = " Minuten";
            if((timeElapsed/1000/60+1) == 1)
                minutes = " Minute";
            System.out.print("Strahlung diese Minute: "); System.err.println(CPM);
            System.out.print("Seit " + ((timeElapsed/1000)/60+1) +  minutes + " gab es insgesamt "); System.err.print(totalCounts); System.out.println(" Ausschläge.");
            System.out.print("Durchschnittlich sind das "); System.err.print(totalCounts/(timeElapsed/1000/60+1)); System.out.println(" Schläge pro Minute.");
            Util.writeTo("GammaLogCPM", Integer.toString(CPM), true);
            Util.writeTo("GammaLogTotalCounts", Integer.toString(totalCounts), true);
            Util.writeTo("GammaLogAverage", Long.toString(totalCounts/(timeElapsed/1000/60+1)), true);
            Util.writeTo("test", "test");
        }
        
        static void beep(){
            try {
                byte[] buf = new byte[ 1 ];;
                AudioFormat af = new AudioFormat( (float )44100, 8, 1, true, false );
                SourceDataLine sdl = AudioSystem.getSourceDataLine( af );
                sdl.open();
                sdl.start();
                for( int i = 0; i < 100 * (float )44100 / 1000; i++ ) {
                    double angle = i / ( (float )44100 / 440 ) * 2.0 * Math.PI;
                    buf[ 0 ] = (byte )( Math.sin( angle ) * 100 );
                    sdl.write( buf, 0, 1 );
                }
                sdl.drain();
                sdl.stop();
            } catch (LineUnavailableException ex) {
                Logger.getLogger(SerialToJava.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        

}
