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

		private static class Line{
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

		private final LinkedList<Line> lines = new LinkedList<Line>();
		private final LinkedList<Point> points=new LinkedList<Point>();
		private int lastX=0,lastY=0,refX=200,refY=100;
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
		    g.drawRect(refX, refY, 10, 10);
	        Dimension d = getPreferredSize();
		    for (Line line : lines) {
		        g.setColor(line.color);
		        g.drawLine(line.x1+refX, line.y1+refY, line.x2+refX, line.y2+refY);
		    }
//		    doDrawing(g);
		}
}