package grapher;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
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

import javax.swing.Timer;

import java.util.Random;
public class Grapher {
    private static final String 		TITLE		=	"";
    private static final int 			COUNT 		= 	2 * 60;
    private static final float 			MINMAX 		= 	100;
    private static final int			MAX_CHANNEL =	5;
    private static final Random 		random 		= 	new Random();
    private Timer 						timer;
    final DynamicTimeSeriesCollection 	dataset;
    public  JFreeChart 					chart;
    private static 	ChartPanel			chartPanel;
    private static boolean				autoAdjustRange		=	true;
    private int							delay;
   
    ChartPanel							getChartPanel()
    {
    	return chartPanel;
    }
    
    private JFreeChart		createChart(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
            TITLE, "", "", dataset, true, true, false);
        result.setBackgroundPaint(Color.white);
        result.setAntiAlias(true);
        final XYPlot plot = result.getXYPlot();
        ValueAxis domain = plot.getDomainAxis();
        domain.setAutoRange(true);
        ValueAxis range = plot.getRangeAxis();
//        range.setRange(-MINMAX,MINMAX);
        range.setAutoRange(true);
        return result;
    }
    
    public 		Grapher()
    {
    	 dataset=new DynamicTimeSeriesCollection(MAX_CHANNEL, COUNT, new Second());
    	 //TODO the for loop below can cooperate with the checkboxes and add series on demand
    	 for(int i=0;i<MAX_CHANNEL;i++)
    	 {
    		 String name="Series ";
    		 name+=(i+1);
    		 float[] f={0};
    		 dataset.addSeries(f,i,(Comparable<String>)(name));
//    		 dataset.addValue(i, dataset.getNewestIndex(), f[0]);
    	 }
    	 dataset.setTimeBase(new Second(0, 0, 0, 1, 1, 2011));
    	 chartPanel=new ChartPanel(createChart(dataset));
    	 //TODO see if the line below is correct or not
    	 chartPanel.setPreferredSize(new Dimension(291,187));
    	 timer=new Timer(delay,new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 dataset.advanceTime();
             }});
    	 
    }
    public	void	start()
    {
    	timer.start();
    }
    	
    public  void 	stop()
    {
    	timer.stop();
    }
    public	void	setDelay(int delay)
    {
    	timer.setDelay(delay);
    }
    public  void	addData(int index,float value)
    {
    	if(index>MAX_CHANNEL)return;
  
    	dataset.addValue(index, dataset.getNewestIndex(), value);
    }
    public	int		getMaxChannel()
    {
    	return MAX_CHANNEL;
    }
}
