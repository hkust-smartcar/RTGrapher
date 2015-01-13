package grapher;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Enumeration;

import javax.comm.*;
import javax.swing.DefaultListModel;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
public class BTManager{
	static CommPortIdentifier portId;
	private Enumeration<CommPortIdentifier> portList;
    private InputStream inputStream;
    private SerialPort serialPort;
    private RTGrapher grapher;
    public int baudRate;
    public int seriesNumber;
    static DefaultListModel<CommPortIdentifier> portEnum;
    byte[] readBuffer;
    BTManager(RTGrapher grapher){
    	this.grapher=grapher;
    	inputStream=null;
    	serialPort=null;
    	baudRate=115200;
    }

	public boolean openPort(CommPortIdentifier portID){
		try{
			serialPort=(SerialPort)portID.open("RTGrapher", 100000);
		}catch(PortInUseException e){
			grapher.writeNotice(e.getMessage());
			return false;
		}
		try{
			inputStream=serialPort.getInputStream();
		}catch(IOException e){
			grapher.writeNotice(e.getMessage());
			return false;
		}
		try {
            serialPort.setSerialPortParams(baudRate,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException e) {
        	grapher.writeNotice(e.getMessage());
        	return false;
        }
		return true;
	}
	public void	startProcess(){
		portList=CommPortIdentifier.getPortIdentifiers();
		if(portList!=null){
			while(portList.hasMoreElements()){
				portId=portList.nextElement();
				if(openPort(portId)){
					processMessage();
				}
			}
		}
	}
	public void processMessage(){
		if(serialPort!=null&&inputStream!=null){
			try{
				inputStream.read(readBuffer, 0, 4);
				for(int i=0;i<seriesNumber;i++){
					ByteBuffer b = ByteBuffer.wrap(readBuffer);
					grapher.addData(b.getFloat(), i);
				}
			}catch(IOException e){
				grapher.writeNotice(e.getMessage());
			}
		}
	}
	
}
