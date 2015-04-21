package grapher;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JComponent;
import java.awt.Point;
	public class LineComponent extends JComponent{
		public boolean		mouseEntered=false;
		public boolean		isGroupSelecting=false;
		public static class Line{
		    final int x1; 
		    final int y1;
		    final int x2;
		    final int y2;   
		    final Color color;

		    public Line(int x1, int y1, int x2, int y2, Color color) {
		        this.x1 = x1;
		        this.y1 = y1;
		        this.x2 = x2;
		        this.y2 = y2;
		        this.color = color;
		    }               
		}
		public static class PositionTag{
			public String tag;
			int xPos,yPos;
			public PositionTag(String nameTag,int x,int y)
			{
				this.tag=nameTag;
				this.xPos=x;
				this.yPos=y;
			}
		}
		public int mouseX,mouseY,anX,anY;
		public  final LinkedList<Line> lines = new LinkedList<Line>();
		private final LinkedList<Point> points=new LinkedList<Point>();
		public		  LinkedList<Line> selectedLine=new LinkedList<Line>();
		private int lastX=0,lastY=0;
		public int refX=200,refY=100;
		public final int drefX=250,drefY=80;
		public void addLine(int x1, int x2, int x3, int x4) {
		    addLine(x1, x2, x3, x4, Color.black);

		}

		public void addLine(int x1, int x2, int x3, int x4, Color color) {
		    lines.add(new Line(x1,x2,x3,x4, color));
		    lastX=x3;
		    lastY=x4;
		    points.add(new Point(x3,x4));
		    repaint();
		}
		public void setReferenceCoordinate(int x,int y)
		{
			if(x<0||y<0)return;
			refX=x;
			refY=y;
			repaint();
		}
		public void addLine(int x,int y,Color color)
		{
			addLine(lastX,lastY,x,y,color);
		}
		public void addLine(int x,int y)
		{
			addLine(x,y,Color.black);
		}
		public void clearLines() {
		    lines.clear();
		    repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
		    super.paintComponent(g);
		    g.setColor(Color.red);
		    g.drawRect(refX-5, refY-5, 10, 10);
	        Dimension d = getPreferredSize();
		    for (Line line : lines) {
		        g.setColor(line.color);
		        g.drawLine(line.x1+refX, line.y1+refY, line.x2+refX, line.y2+refY);
		    }
		    for(Line line : selectedLine)
		    {
		    	//TODO may be useful later
		    	g.setColor(Color.RED);
		    	g.drawLine(line.x1+refX, line.y1+refY, line.x2+refX, line.y2+refY);
		    	
		    }
		    if(mouseEntered)
		    {
		    	g.setColor(Color.BLACK);
			    g.drawString("("+mouseX+","+mouseY+")", mouseX+20, mouseY+10);
		    }
		    if(isGroupSelecting)
		    {
		    	g.setColor(Color.BLUE);
		    	if(anY>mouseY&&anX<mouseX)	//QI
		    	{
		    		g.drawRect(anX,mouseY,mouseX-anX,anY-mouseY);
		    	}else if(anY>mouseY&&anX>mouseX) //QII
		    	{
		    		g.drawRect(mouseX,mouseY,anX-mouseX,anY-mouseY);
		    	}else if(anY<mouseY&&anX>mouseX) //QIII
		    	{
		    		g.drawRect(mouseX,anY,anX-mouseX,mouseY-anY);
		    	}else//QIV
		    	{
		    		g.drawRect(anX, anY, mouseX-anX, mouseY-anY);
		    	}
		    }
		    
//		    doDrawing(g);
		}
}