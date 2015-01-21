package grapher;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
//import javax.comm.*;

public class RTGrapher extends ApplicationFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6580194594526535628L;
	private static String TITLE;
	private static final boolean IS_DEBUG=true;
    private static final String START = "Start";
    private static final String STOP = "Stop";
    private static final float MINMAX = 100;
    private static final int COUNT = 60;
    private static int DELAY_MS;
    private final DynamicTimeSeriesCollection dataset;
    public JLabel noticeLabel;
    private final boolean isBTModuleEnabled;
    public BTModule bt;
    public Map dataSet;
    private static final Random random = new Random();
    private final int seriesCount;
    private Timer timer;
    public RTGrapher(boolean enableBTModule){
    	super("Untitled");
    	isBTModuleEnabled=enableBTModule;
    	noticeLabel=new JLabel();
    	if(IS_DEBUG){
    		TITLE=new String("Ha");
    		seriesCount=3;
    		if(isBTModuleEnabled){
        		String PATH=new String("/dev/tty.SC04-DevB");
    			bt=new BTModule(PATH,4);
    		}
    	}else{
    		TITLE=JOptionPane.showInputDialog(this,"Graph name:","HA");
        	String s;
    		do{
        		s=JOptionPane.showInputDialog(this, "Number of graph:",2);
        	}while(!isInteger(s));
    		seriesCount=Integer.parseInt(s);
    		if(isBTModuleEnabled){
    			String PATH=JOptionPane.showInputDialog(this,"Path name:",null);
    			bt=new BTModule(PATH,4);
    			bt.setBufferSize(4);
    		}
    	}
    	dataset =
                new DynamicTimeSeriesCollection(seriesCount, COUNT, new Second());
            dataset.setTimeBase(new Second());
            float[] f=new float[]{0};
            for(int i=0;i<seriesCount;i++){
            	String id="data";
            	id+=Integer.toString(i+1);
            	dataset.addSeries(f,i,id);
            }
//            dataset.addSeries(f, 0, "Fake data");
//            dataset.addSeries(f, 1, "Custom Data");
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
//            final JButton addValue=new JButton("Add value");
//            addValue.addActionListener(new ActionListener(){
//            	@Override
//            	public void actionPerformed(ActionEvent e){
//            		
//                        float newData = randomValue();
//                        //dataset.advanceTime();
//                        dataset.addValue(1,dataset.getNewestIndex(),newData);
//            	}
//            });
            this.add(new ChartPanel(chart), BorderLayout.CENTER);
            this.add(noticeLabel, BorderLayout.NORTH);
            JPanel btnPanel = new JPanel(new FlowLayout());
            btnPanel.add(run);
//            btnPanel.add(addValue);
            JTextField delayTextField=new JTextField("40");
            this.add(btnPanel, BorderLayout.SOUTH);
            timer = new Timer(100, new ActionListener(){
            	@Override
            	public void actionPerformed(ActionEvent e){
            		//float[] newData = new float[1];
                    //newData[0] = randomValue();
            		dataset.advanceTime();
            		//dataset.appendData(newData,dataset.getNewestIndex(),1);
            	}
            });
            btnPanel.add(new JLabel("Delay(ms):"));
            delayTextField.addActionListener(new ActionListener(){
            	@Override
            	public void actionPerformed(ActionEvent e){
            		if(isInteger(delayTextField.getText())){
            			timer.setDelay(Integer.parseInt(delayTextField.getText()));
            		}
            	}
            });
            btnPanel.add(delayTextField,BorderLayout.SOUTH);
            Thread thread=new Thread(bt);
            thread.start();
        }
    private float randomValue() {
        return (float) (random.nextGaussian() * MINMAX / 10000);
    }
    public void writeNotice(String message){
    	noticeLabel.setText(message);
    }

    private JFreeChart createChart(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
            TITLE, "second", "Values", dataset, true, true, true);
        final XYPlot plot = result.getXYPlot();
        ValueAxis domain = plot.getDomainAxis();
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
        // only got here if we didn't return false
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
    public  void addData(float value,int seriesIndex){
    	 dataset.addValue(seriesIndex,dataset.getNewestIndex(),value);
    }
    public float[] returnData(byte[] b){
    	ByteBuffer bb= ByteBuffer.wrap(b);
    	float[] result=new float[1];
    	result[0]=bb.getFloat();
    	return result;
    }
    public int getSeriesCount(){
    	return seriesCount;
    }
    
    public class BTModule implements Runnable{
    	private FileInputStream fis;
    	private FileOutputStream fos;
    	private int bufferSize;
    	private final String PATH;
    	BTModule(String PATH,int bufferSize){
    		this.PATH=PATH;
    		this.bufferSize=bufferSize;
    		try{
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
    		while (true) {
    			byte[] b=new byte[4];
                try{
    			while (true) {
    				for(int j=0;j<seriesCount;j++){
    				for(int i=0;i<bufferSize;i++){
    					fis.read(b,i,1);
    				}
    					RTGrapher.this.addData(ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat(),j);
    				}
    			}
        		
                }catch(IOException e){
                	e.printStackTrace();
                }
                	finally {
                		try {
                			if (fis != null)
                				fis.close();
                		} catch (IOException ex) {
                			ex.printStackTrace();
                		}
                	}
            	}
            };
    	}
    	
    }
