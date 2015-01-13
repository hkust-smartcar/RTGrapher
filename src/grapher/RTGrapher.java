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
import java.util.Random;
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
    private File file;
    private FileInputStream fis;
    private FileOutputStream fos;
    private final String PATH;
    private final DynamicTimeSeriesCollection dataset;
    private byte[] b;
    public JLabel noticeLabel;
//    private static final int FAST = 100;
//    private static final int SLOW = FAST * 5;
    private static final Random random = new Random();
    private final int seriesCount;
    private Timer timer;
    public RTGrapher(){
    	super("Untitled");
    	noticeLabel=new JLabel();
    	if(IS_DEBUG){
    		TITLE=new String("Ha");
    		PATH=new String("/dev/tty.SC04-DevB");
    		seriesCount=4;
    	}else{
    		TITLE=JOptionPane.showInputDialog(this,"Graph name:","HA");
    		PATH=JOptionPane.showInputDialog(this,"Path name:",null);
    	}
    	
    	
    	String s;
    	
//    	do{
//    		s=JOptionPane.showInputDialog(this, "Number of graph:",2);
//    	}while(!isInteger(s));
    	file=new File(PATH);
    	try {
			fis = new FileInputStream(file);
			fos = new FileOutputStream(file);
//			System.out.println("Total file size to read (in bytes) : "
//					+ fis.available());
 
//			int content;
//			while ((content = fis.read()) != -1) {
//				// convert to char and display it
//				addData((float)content,1);
//			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} 
    	dataset =
                new DynamicTimeSeriesCollection(seriesCount, COUNT, new Second());
    	//this.seriesCount=Integer.parseInt(s);
            dataset.setTimeBase(new Second());
            float[] f=new float[]{0};
            dataset.addSeries(f, 0, "Fake data");
            dataset.addSeries(f, 1, "Custom Data");
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
            final JButton addValue=new JButton("Add value");
            addValue.addActionListener(new ActionListener(){
            	@Override
            	public void actionPerformed(ActionEvent e){
            		
                        float newData = randomValue();
                        //dataset.advanceTime();
                        dataset.addValue(1,dataset.getNewestIndex(),newData);
            	}
            });
//            final JComboBox combo = new JComboBox();
//            combo.addItem("Fast");
//            combo.addItem("Slow");
//            combo.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    if ("Fast".equals(combo.getSelectedItem())) {
//                        timer.setDelay(FAST);
//                    } else {
//                        timer.setDelay(SLOW);
//                    }
//                }
//            });
            final JTextField intervalTextField=new JTextField("115200");
//            intervalTextField.addActionListener(new ActionListener(){
//            	@Override
//            	public void actionPerformed(ActionEvent e){
//            		if(isInteger(intervalTextField.getText())){
//            			btManager.baudRate=Integer.parseInt(intervalTextField.getText());
//            		}
//            	}
//            });
            //JList<CommPortIdentifier> btList=new JList<CommPortIdentifier>(BTManager.getPortIdentifiers());
            //this.add(btList,BorderLayout.EAST);
            this.add(new ChartPanel(chart), BorderLayout.CENTER);
            this.add(noticeLabel, BorderLayout.NORTH);
            JPanel btnPanel = new JPanel(new FlowLayout());
            btnPanel.add(run);
            btnPanel.add(addValue);
            btnPanel.add(new JLabel("Baud Rate:"));
//          btnPanel.add(intervalTextField);
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
            //btManager.startProcess();
            
        }
    private float randomValue() {
        return (float) (random.nextGaussian() * MINMAX / 10000);
    }
    public void writeNotice(String message){
    	noticeLabel.setText(message);
    }
    private float[] gaussianData() {
        float[] a = new float[COUNT];
        for (int i = 0; i < a.length; i++) {
            a[i] = randomValue();
        }
        return a;
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
                
            	RTGrapher demo = new RTGrapher();
                
                demo.pack();
                RefineryUtilities.centerFrameOnScreen(demo);
                demo.setVisible(true);
                demo.start();     
                Thread thread=new Thread(new Runnable(){
                	@Override
                	public void run(){
                	byte[] b=new byte[4];
                    try{
        			while (true) {
        				// convert to char and display it
        				//System.out.println(demo.fis.read(b));
        				for(int j=0;j<demo.seriesCount;j++){
        				for(int i=0;i<4;i++){
        					demo.fis.read(b,i,1);
        				}
        					demo.addData(ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat(),j);
        				}
//        				byte[] bytes = ByteBuffer.allocate(2).putShort((short) 1)
//        						.order(ByteOrder.BIG_ENDIAN).array();
//                		demo.fos.write(bytes,0,bytes.length);
        			}
            		
                    }catch(IOException e){
                    	e.printStackTrace();
                    }
                    	finally {
                    		try {
                    			if (demo.fis != null)
                    				demo.fis.close();
                    		} catch (IOException ex) {
                    			ex.printStackTrace();
                    		}
                    	}
                	}
                });
                thread.start();
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
}
