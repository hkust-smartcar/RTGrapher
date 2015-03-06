package grapher;
import gnu.io.*;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Queue;
import java.util.TooManyListenersException;

public class BluetoothSocket implements SerialPortEventListener{
//	 //passed from main GUI
//    GUI window = null;
    //for containing the ports that will be found
    private Enumeration 	   ports = null;
    //map the port names to CommPortIdentifiers
    private HashMap 		   portMap = new HashMap();

    //this is the object that contains the opened port
    private CommPortIdentifier selectedPortIdentifier = null;
    private SerialPort 		   serialPort = null;

    //input and output streams for sending and receiving data
    private InputStream 	   input = null;
    private OutputStream 	   output = null;

    //just a boolean flag that i use for enabling
    //and disabling buttons depending on whether the program
    //is connected to a serial port or not
    private boolean 		  bConnected 		= false;
    /*
     * statusFlag explanation:
     * 		0: not connected and await for connection.
     * 		1: can not connect to the port.
     * 		2: port is in use.
     * 		3: connected successfully.
     * 		4: I/O stream initialization failed.
     * 		5: Too many listener.
     * 		6: Fail to close the serial port.
     * 		7: Fail to get data by listening to serial port event.
     * 		8: Fail to write data.
     * 		9: Data received does not conform to the protocol.
     */
    private int				  statusFlag;
    /*
     * readFlag explanation:
     * 		0: no data available
     * 		1: read success
     * 		2: incoming data cannot be casted to float(i.e. invalid)
     * 		3: queue overflow
     */
    private int				  readFlag;
    private volatile Queue<TaggedData> queue;
    private boolean			  hasDataAvailable;

    static int 		  		  TIMEOUT 			= 1000;

    
    private Protocol		  protocol;
    private	String			  retrievedData;

	  //search for all the serial ports
    //pre style="font-size: 11px;": none
    //post: adds all the found ports to a combo box on the GUI
    public void searchForPorts()
    {
        ports = CommPortIdentifier.getPortIdentifiers();

        while (ports.hasMoreElements())
        {
            CommPortIdentifier curPort = (CommPortIdentifier)ports.nextElement();

            //get only serial ports
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                portMap.put(curPort.getName(), curPort);
            }
        }
    }
    
    public boolean isConnected()
    {
    	return bConnected;
    }
    public Enumeration getPortList()
    {
    	return ports;
    }
    public HashMap<String,CommPortIdentifier> getPortHashMap()
    {
    	return new HashMap<String,CommPortIdentifier>(portMap);
    }
    public void connectToPort(String portName)
    {
    	selectedPortIdentifier=(CommPortIdentifier)portMap.get(portName);
    	CommPort commPort=null;
    	   try
           {
               //the method below returns an object of type CommPort
    		   //TODO The purpose of the line of code below remains unknown. Please Investigate it's behavior.
               commPort = selectedPortIdentifier.open(portName, TIMEOUT);
               //the CommPort object can be casted to a SerialPort object
               serialPort = (SerialPort)commPort;

           }
           catch (PortInUseException e)
           {
               statusFlag=2;
               return;
           }
           catch (Exception e)
           {
        	   statusFlag=1;
        	   return;
           }
    	   
    	   bConnected=true;
    	   statusFlag=3;
    }
    
    public boolean initIOStream()
    {
        //return value for whether opening the streams is successful or not
        boolean successful = false;

        try {
            //
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            writeData(0, "0");

            successful = true;
            return successful;
        }
        catch (IOException e) {
        	statusFlag=4;
        	return successful;
        }
    }
    
    public void initListener()
    {
        try
        {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        }
        catch (TooManyListenersException e)
        {
        	statusFlag=5;
        	return;
        }
    }
    
    public synchronized void disconnect()
    {
        //close the serial port
        try
        {
            writeData(0, "0");
            serialPort.removeEventListener();
            input.close();
            output.close();
            serialPort.close();

            bConnected=false;
        }
        catch (Exception e)
        {
            statusFlag=6;
            e.printStackTrace();
        }
    }
    
    @Override
    public void serialEvent(SerialPortEvent evt) {
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            try
            {
                byte singleData = (byte)input.read();

                if (singleData != protocol.NEW_LINE_ASCII)
                {
                    retrievedData = new String(new byte[] {singleData});
                    //TODO see if the handling below works.
                    if(retrievedData.charAt(0)!=protocol.SIGNAL
                    		||retrievedData.charAt(protocol.DELIM)==-1)
                    {
                    	readFlag=4;
                    	return;
                    }else
                    {
                    	String instruction=retrievedData.substring(1,
                    								protocol.DELIM);
                    	String value=retrievedData.substring(protocol.DELIM+1);
                    	
                    	try {
							int instrI=Integer.parseInt(instruction);
							float val	=Float.parseFloat(value);
							queue.add(new TaggedData(val,instrI));
						} catch (NumberFormatException e) {
							readFlag=5;
							return;
						}
                    }
                    
                    hasDataAvailable=true;
                }else
                {
                	hasDataAvailable=false;
                }
            }
            catch (IllegalStateException e)
            {
            	readFlag=3;
            	hasDataAvailable=false;
            }
            catch (NumberFormatException e)
            {
            	readFlag=2;
            	hasDataAvailable=false;
            }
            catch (Exception e)
            {
                statusFlag=7;
            }

        }
    }
    
    public void writeData(int commandCode, String value)
    {
        try
        {
        	String message="s";
        	
        	message+=commandCode;
        	message+=protocol.DELIM;
        	message+=value;
//        	message+=(char)SPACE_ASCII;
        	
            output.write(message.getBytes());
        }
        catch (Exception e)
        {
        	statusFlag=8;
        }
    }
    
    public int getState()
    {
    	return statusFlag;
    }
    public class TaggedData
    {
    	public float data;
    	public int tag;
    	TaggedData(TaggedData data)
    	{
    		
    	}
    	TaggedData(float d,int t)
    	{
    		data=d;
    		tag=t;
    	}
    }
    
    public int getReadFlag()
    {
    	return readFlag;
    }
    public synchronized TaggedData extractData()
    {
    	if(queue.isEmpty())
    	{
    		hasDataAvailable=false;
    		readFlag=0;
    		return null;
    	}else
    	{
    		return queue.poll();
    	}
    }
    
    public boolean			  dataAvailable()
    {
    	return hasDataAvailable;
    };
}
