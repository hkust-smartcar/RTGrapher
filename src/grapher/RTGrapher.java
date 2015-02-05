package grapher;
import gnu.io.*;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
//import javax.comm.*;
//TODO resolve arrayIndexOutOfBound issue: NumberAxis?
/*
 * last commit: 
 * remove unused comment
 * add export button to export data
 * the grapher can now store the info of recorded points for further investigation
 * debug mode disabled
 * default file path modified
 */
public class RTGrapher extends ApplicationFrame implements SerialPortEventListener{
/*
 * variable declaration
 */
	private static final long serialVersionUID = 6580194594526535628L;
	private static String TITLE;
	private static final boolean IS_DEBUG=false;
    private static final String START = "Start";
    private static final String STOP = "Stop";
    private static final int BAUD_RATE=115200;
    private static final int TIME_OUT=2000;
    //COUNT= total number of entries in each series.(can be get by getItemCount(int series)
    private static final int COUNT = 60;
    private final DynamicTimeSeriesCollection dataset;
    public JLabel noticeLabel;
    private int interval;
    private final boolean isBTModuleEnabled;
    public BTModule bt;
    public FileModule fm;
    public HashMap<DataEntry,Integer> dataSet;
    private final int seriesCount;
    private Timer timer;
    private BufferedReader input;
    private OutputStream output;
    private SerialPort serialPort;
    private static final String PORT_NAMES[] = {
        "/dev/tty.usbserial-A9007UX1", // Mac OS X
        "/dev/tty.SC04-DevB",
        "/dev/ttyUSB0", // Linux
        "COM7", // Windows
};
    /*
     * Constructor
     */
    public RTGrapher(boolean enableBTModule){
    	/*
    	 *Initial Setup: setting background info and determine critical information for the program 
    	 */
    	super("Untitled");
    	interval=20;
    	dataSet=new HashMap<DataEntry,Integer>();
    	isBTModuleEnabled=enableBTModule;
    	noticeLabel=new JLabel();
    	if(IS_DEBUG){
    		TITLE=new String("Ha");
    		seriesCount=3;
    		if(isBTModuleEnabled){
        		String PATH=new String("/dev/tty.SC04-DevB");
    			bt=new BTModule(PATH,4);
    		}else{
    			String PATH=new String("/dev/tty.SC04-DevB");
    			fm=new FileModule(PATH,4);
    		}
    	}else{
    		TITLE=JOptionPane.showInputDialog(this,"Graph name:","HA");
        	String s;
    		do{
        		s=JOptionPane.showInputDialog(this, "Number of graph:",3);
        	}while(!isInteger(s));
    		seriesCount=Integer.parseInt(s);
    		if(isBTModuleEnabled){
//    			initializeBT();
    			String PATH=JOptionPane.showInputDialog(this,"Path name:","/dev/tty.SC04-DevB");
    			bt=new BTModule(PATH,4);
    			bt.setBufferSize(4);
    		}else{
    			String PATH=JOptionPane.showInputDialog(this,"Path name:","/dev/tty.SC04-DevB");
    			fm=new FileModule(PATH,4);
    			fm.setBufferSize(4);
    		}
    	}
    	dataset =
                new DynamicTimeSeriesCollection(seriesCount, COUNT, new Second());
            dataset.setTimeBase(new Second());
            float[] f=new float[]{0};
            //TODO monitor changes
            for(int i=0;i<seriesCount;i++){
            	String id="data";
            	id+=Integer.toString(i+1);
            	//dataset.addSeries(f,i,id);
            	dataset.setSeriesKey(i,id);
            }
            /*
             * GUI components' initialization
             */
            JFreeChart chart = createChart(dataset);
            final JButton run = new JButton(STOP);
            run.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String cmd = e.getActionCommand();
                    if (STOP.equals(cmd)) {
                        timer.stop();
                        run.setText(START);
                    } else {
                        timer.start();
                        run.setText(STOP);
                    }
                }
            });
            //TODO investigate the export button
            final JButton export= new JButton("Export");
            export.addActionListener(new ActionListener(){
            	@Override
            	public void actionPerformed(ActionEvent e){
            		JFileChooser chooser = new JFileChooser();
            	    FileNameExtensionFilter filter = new FileNameExtensionFilter(
            	        "Text Files", "txt");
            	    chooser.setFileFilter(filter);
            	    switch(chooser.showSaveDialog(null)) {
            	    case JFileChooser.CANCEL_OPTION:
            	    case JFileChooser.ERROR_OPTION:
            	    	return;
            	    case JFileChooser.APPROVE_OPTION:
            	    	File file=chooser.getSelectedFile();
            	    	try {
							PrintWriter writer=new PrintWriter(file);
	            	    	for (Map.Entry<DataEntry,Integer> entry : dataSet.entrySet())
	            	    	{
	            	    	    DataEntry data=entry.getKey();
	            	    	    String x=Float.toString(data.x),
	            	    	    	   y=Float.toString(data.y),
	            	    	    	   series=Integer.toString(entry.getValue());
	            	    	    writer.write(x+'\t'+y+'\t'+series+'\n');
	            	    	}
	            	    	writer.close();
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						}

            	    }
            	}
            });
            this.add(new ChartPanel(chart), BorderLayout.CENTER);
            this.add(noticeLabel, BorderLayout.NORTH);
            JPanel btnPanel = new JPanel(new FlowLayout());
            btnPanel.add(run);
            JTextField delayTextField=new JTextField("40");
            this.add(btnPanel, BorderLayout.SOUTH);
            //TODO reactivate advanceTime() if necessary
            timer = new Timer(100, new ActionListener(){
            	@Override
            	public void actionPerformed(ActionEvent e){
            		//dataset.advanceTime();
            	}
            });
            btnPanel.add(export,BorderLayout.SOUTH);
            btnPanel.add(new JLabel("Delay(ms):"));
            delayTextField.addActionListener(new ActionListener(){
            	@Override
            	//suppress interval changing action
            	public void actionPerformed(ActionEvent e){
//            		if(isInteger(delayTextField.getText())){
//            			timer.setDelay(Integer.parseInt(delayTextField.getText()));
//            		}
            	}
            });
            btnPanel.add(delayTextField,BorderLayout.SOUTH);
            /*
             * BTModule thread initiation
             */
            Thread thread=new Thread(bt);
            thread.start();
        }
/*
 * Member function declaration
 */
    
    public void writeNotice(String message){
    	noticeLabel.setText(message);
    }
    
    private JFreeChart createChart(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
            TITLE, "Time", "Values", dataset, true, true, true);
        final XYPlot plot = result.getXYPlot();
        ValueAxis domain = plot.getDomainAxis();
        //TODO investigate auto ranges
        domain.setAutoRange(true);
        ValueAxis range = plot.getRangeAxis();
        range.setAutoRange(true);
        return result;
    }

    public void start() {
        timer.start();
    }
    
    private static boolean isInteger(String s) {
        try { 
            Integer.parseInt(s); 
        } catch(NumberFormatException e) { 
            return false; 
        }
        return true;
    }

    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                
            	RTGrapher demo = new RTGrapher(true);
                
                demo.pack();
                RefineryUtilities.centerFrameOnScreen(demo);
                demo.setVisible(true);
                demo.start();
            }
        });
    }
    //TODO investigate a new method that safely overwrites the oldest value in a series
    public  void addData(float value,int seriesIndex){
    	 dataset.addValue(seriesIndex,dataset.getNewestIndex(),value);
    	 //dataSet.put(new DataEntry((float)dataset.getEndXValue(seriesIndex, 0),value), seriesIndex);
    }
    
    public int getSeriesCount(){
    	return seriesCount;
    }
    /*
     * nested class definition
     */
    public class FileModule implements Runnable{
    	private FileInputStream fis;
    	private FileOutputStream fos;
    	private int bufferSize;
     	private String PATH;
		FileModule(String PATH,int bufferSize){
			try{
				this.bufferSize=bufferSize;
				this.PATH=PATH;
				File file=new File(PATH);
				this.fis=new FileInputStream(file);
				this.fos=new FileOutputStream(file);
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		public void setBufferSize(int size){
     		if(size>=0)
     		bufferSize=size;
     	}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			byte[] b=new byte[4];
			try{
				while(true){
					for(int j=0;j<seriesCount;j++){
	     				for(int i=0;i<bufferSize;i++){
	     					fis.read(b,i,1);
	     				}
	     				RTGrapher.this.addData(ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat(),j);
					}
				}
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				try{
					if(fis!=null)
						fis.close();
					}catch(IOException e){
						e.printStackTrace();
					}
				}
			};
    }
    //TODO the BTModule should now relying on rxtx library instead of java file io
    public class BTModule implements Runnable{
    	private InputStream in;

    	private int bufferSize;
    	private final String PATH;
    	BTModule(String PATH,int bufferSize){
    		//TODO restore the original file io method if necessary
    		this.PATH=PATH;
    		this.bufferSize=bufferSize;
    		initializeBT();
    		try{
    			//File file=new File(PATH);
    			//TODO resolve nullptr exception
    			in=serialPort.getInputStream();
//    			this.fis=new FileInputStream(file);
//    			this.fos=new FileOutputStream(file);
    		}catch(NullPointerException ne){
    			System.out.println("Serial port is not initialized");
    		}catch(IOException e){
    			e.printStackTrace();
    		}
    	}
    	public void setBufferSize(int size){
    		if(size>=0)
    		bufferSize=size;
    	}
    	@Override
    	public void run() {
    		while (true) {
    			byte[] b=new byte[4];
                while (true) {
    				for(int j=0;j<seriesCount;j++){
    				for(int i=0;i<bufferSize;i++){
    					//fis.read(b,i,1);
    					//TODO examine the effect of this new piece of code
    		            int len = -1;
    		            try
    		            {
    		                while ( ( len = this.in.read(b)) > -1 )
    		                {
    		                	//TODO the line for reading the port is here~
    		                	RTGrapher.this.addData(ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat(),j);
    		                }
    		            }
    		            catch ( IOException e )
    		            {
    		                e.printStackTrace();
    		            }      
    				}
    					dataset.advanceTime();
    					//RTGrapher.this.addData(ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat(),j);
    				}
    			}
            	}
            };
    	}
    public class DataEntry{
    	DataEntry(float x,float y){
    		this.x=x;
    		this.y=y;
    	}
    	float x,y;
    }
    public void initializeBT() {
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
                serialPort.setSerialPortParams(BAUD_RATE,
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
	@Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		// TODO Auto-generated method stub
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                    String inputLine=input.readLine();
                    System.out.println(inputLine);
            } catch (Exception e) {
                    //System.err.println(e.toString());
            }
    }
	}
    }
