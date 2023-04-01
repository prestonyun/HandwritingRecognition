import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Iterator;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;

public class DataProcessor extends JComponent {

	
	private static final long serialVersionUID = 1L;
	private Image container;
	private Graphics2D canvas;
	private int x, y, x0, y0;
	private List<Point> points = new ArrayList<Point>();
	
	public double[][] getDescriptors()
	{
		//Scale Points such that one point is stored every 3 pixels in "length"
		List<Point> scaledPoints = condense(points);
		
		//Ensures that the size of scaledPoints is a power of 2 for FFT
		while ((scaledPoints.size() & (scaledPoints.size() - 1)) != 0)
		{
			scaledPoints.add(new Point());
		}
		Point[] pts = new Point[scaledPoints.size()];
		
		double[][] result = new double[2][scaledPoints.size()];
		
		scaledPoints.toArray(pts);
		
		//Finds the centroid point:
		double[] cent = centroid(scaledPoints);
		double norm = pts.length / (2 * Math.PI);
		double[] normCent = {cent[0] * norm, cent[1] * norm};
		
		//Coordinates of first point:
		double[] originAngle = {pts[0].getXVal(), pts[0].getYVal()};
		
		//Angle of the tangent line of first point:
		double theta0 = theta(originAngle);
		
		//Cumulative Angular Function:
		for (int i = 0; i < pts.length; i++)
		{
			Point[] normPts = Arrays.copyOf(pts, pts.length);
			normPts[i].setXVal(pts[i].getXVal() * norm);
			normPts[i].setYVal(pts[i].getYVal() * norm);

			double[] normTan = tangent(normPts[i], normCent);
			
			double difference = ((theta((normTan)) - theta0)%(2*Math.PI)) - i;
			result[0][i] = difference;
		}
		
		return result;
		
	}
	
	//Scales data points such that the "line density" is consistent: drawing faster/slower produces same result
	public List<Point> condense(List<Point> input) {
		List<Point> normalized = new ArrayList<Point>();
		normalized.add(input.get(0));

		for (int i = 1; i < input.size(); i++) {
			Point tmp = input.get(i);
			if (findLength(normalized.get(normalized.size() - 1), tmp) > 3) {
				normalized.add(tmp);
			}
		}

		return normalized;
	}
	
	public static int findLength(Point a, Point b)
	{
		int x = (int) Math.pow(b.getXVal() - a.getXVal(), 2);
		int y = (int) Math.pow(b.getYVal() - a.getYVal(), 2);
		
		return (int) Math.sqrt(x + y);
	}
	
	//We are going to find the boundary of the shape, the find the center point.
	//Then, since tangent lines are normal to to the radial line, we use the
	//centroid to find a tangential line at each point.
	
	public double[] centroid(List<Point> points)
	{
		List<Point> pts = getBoundary(points);
			
		int xC = 0, yC = 0;
		Iterator<Point> it = pts.iterator();
		
		while (it.hasNext())
		{
			Point t = it.next();
			if (t != null)
			{
				xC += t.getXVal();
				yC += t.getYVal();
			}
		}
		
		xC /= pts.size();
		yC /= pts.size();
		
		double[] result = {xC, yC};
		
		return result;
	}
	
	//Given a point and an origin point, finds a vector that points from the origin to given point, then rotates 90 degrees
	//to obtain a tangent vector
	public double[] tangent(Point a, double[] centroid) {
		double dX, dY;

		dY = centroid[1] - a.getYVal();
		dX = a.getXVal() - centroid[0];

		double[] result = { -dY, dX };

		return result;
	}
	
	public double theta(double[] tanVec)
	{
		double angle = Math.atan2(tanVec[1], tanVec[0]);
		
		return angle;
	}

	public DataProcessor()
	{
		setDoubleBuffered(false);
		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				canvas.setColor(Color.black);
				x0 = e.getX();
				y0 = e.getY();
				
				Point tmp = new Point(x0, y0);
				points.add(tmp);
			} //end mousePressed
		}); //end addMouseListener
		
		addMouseMotionListener(new MouseMotionAdapter()
		{
			public void mouseDragged(MouseEvent e)
			{
				x = e.getX();
				y = e.getY();
				
				if (canvas != null)
				{
					canvas.drawLine(x0,  y0,  x, y);
					
					Point tmp = new Point(x, y);
					points.add(tmp);
					
					repaint();
					
					x0 = x;
					y0 = y;
				} //end if
			} //end mouseDragged
		}); //end addMouseMotionListener
	} //end DrawArea
	
		
	protected void paintComponent(Graphics graphics)
	{
		if (container == null)
		{
			container = createImage(getSize().width, getSize().height);
			canvas = (Graphics2D) container.getGraphics();
			
			clear();
		}
		
		graphics.drawImage(container, 0, 0, null);
	}
	
	//Creates 2D double array to contain Fourier Transformed data
	public List<Point> getBoundary(List<Point> points)
	{	
		List<Point> result = new ArrayList<Point>();
		//Sort points
		Collections.sort(points, new compareValues());
		
		//Instantiates Point array
		Point[] r = new Point[points.size()];
		
		//Populates Points array
		for (int i = 0; i < points.size(); i++)
		{
			r[i] = points.get(i);
		}
		
		//Takes convex hull of Point array
		Point[] ri = convex_hull(r);
		
		result.addAll(Arrays.asList(ri));
		
		return result;
	}
	
	//Clears the canvas, as well as the data arrays
	public void clear()
	{			
		points.clear();
		canvas.setColor(Color.white);
		canvas.fillRect(0,  0,  getSize().width, getSize().height);
		canvas.setColor(Color.black);
		repaint();
	} //end clear
	
	//Comparator used in sortSet() method
	class compareValues implements Comparator<Point>
	{
		public int compare(Point a, Point b)
		{
			if (a.getXVal() == b.getXVal())
				return (int) (a.getYVal() - b.getYVal());
			else
				return (int) (a.getXVal() - b.getXVal());
		}
	}
		
	//Rudimentary cross product function
	public static double cross(Point O, Point A, Point B) {
		return (A.x - O.x) * (B.y - O.y) - (A.y - O.y) * (B.x - O.x);
	}
	
	
	//Method to get convex hull form an array of points
	public static Point[] convex_hull(Point[] P) {
		
			int n = P.length, k = 0;
			Point[] H = new Point[2 * n];

			// Build lower hull
			for (int i = 0; i < n; ++i) {
				while (k >= 2 && cross(H[k - 2], H[k - 1], P[i]) <= 0)
					k--;
				H[k++] = P[i];
			}

			// Build upper hull
			for (int i = n - 2, t = k + 1; i >= 0; i--) {
				while (k >= t && cross(H[k - 2], H[k - 1], P[i]) <= 0)
					k--;
				H[k++] = P[i];
			}
			
			//Work here: 
			if (k > 1 && (H.length & (H.length - 1)) != 0) {
				H = Arrays.copyOfRange(H, 0, k - 1); // remove non-hull vertices after k; remove k - 1 which is a duplicate
			}
			
		return H;
	}
	
}

	//Point class, contains constructor as well as get/set methods for x and y coordinates
	class Point
	{
		double x, y;
	
		public Point()
		{
			
		}
		
		public Point(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
		
		public double getXVal()
		{
			return x;
		}
	
		public double getYVal()
		{
			return y;
		}
		
		public void setXVal(double x)
		{
			this.x = x;
		}
		
		public void setYVal(double y)
		{
			this.y = y;
		}
	}
	