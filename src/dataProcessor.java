import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;

public class dataProcessor extends JComponent {

	
	private static final long serialVersionUID = 1L;
	private Image container;
	private Graphics2D canvas;
	private int x, y, x0, y0;
	Vector<Point> points = new Vector<Point>();
	Vector<Double> xBound = new Vector<Double>();
	Vector<Double> yBound = new Vector<Double>();
	
	Vector<Point> scaledPoints = new Vector<Point>();

	Vector<Double> slopes = new Vector<Double>();
	
	public double[][] getDescriptors()
	{
		//Scale Points such that one point is stored every 3 pixels in "length"
		scaledPoints = condense(points);
		
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
			normPts[i].x = pts[i].x * norm;
			normPts[i].y = pts[i].y * norm;
			
			//double[] tan = tangent(pts[i], cent);
			double[] normTan = tangent(normPts[i], normCent);
			
			double difference = ((theta((normTan)) - theta0)%(2*Math.PI)) - i;
			result[0][i] = difference;
		}
		
		return result;
		
	}
	
	//Scales data points such that the "line density" is consistent: drawing faster/slower produces same result
	public Vector<Point> condense(Vector<Point> input)
	{
		int a = 0, b = a + 1;
		Vector<Point> normalized = new Vector<Point>();
		
		Iterator<Point> it = input.iterator();

		while (it.hasNext() && a < input.size())
		{
			int test = 0;
			Point tmp = input.get(a);
			Point tmp2 = it.next();
			
			test += findLength(tmp, tmp2);
			
			if (test % 2 == 0)
			{
				normalized.add(tmp2);

				a = b;
			}
			
			it.remove();
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
	
	public double[] centroid(Vector<Point> points)
	{
		Vector<Point> pts = getBoundary(points);
			
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
	public double[] tangent(Point a, double[] centroid)
	{
		double dX, dY;
		
		if (a.getYVal() > centroid[1])
			dY = centroid[1] - a.getYVal();
		else
			dY = a.getYVal() - centroid[1];
		
		if (a.getXVal() > centroid[0])
			dX = a.getXVal() - centroid[0];
		else
			dX = centroid[0] - a.getXVal();
		
		double[] result = {-dY, dX};
		
		return result;
	}
	
	public double theta(double[] tanVec)
	{
		double angle = Math.atan(tanVec[1] / tanVec[0]);
		
		return angle;
	}

	public dataProcessor()
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
	public Vector<Point> getBoundary(Vector<Point> points)
	{	
		Vector<Point> result = new Vector<Point>();
		//Sort points
		points.sort(new compareValues());
		
		//Instantiates Point array
		Point[] r = new Point[points.size()];
		
		//Populates Points array
		for (int i = 0; i < points.size(); i++)
		{
			r[i] = points.elementAt(i);
		}
		
		//Takes convex hull of Point array
		Point[] ri = convex_hull(r);
		
		for (int i = 0; i < ri.length; i++)
			result.add(ri[i]);
		
		return result;
	}
	
	//Clears the canvas, as well as the data arrays
	public void clear()
	{			
		points.clear();
		brain.input.setText("");
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
			if (a.x == b.x)
				return (int) (a.y - b.y); 
			else
				return (int) (a.x - b.x);
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
			Vector<Point> Hi = new Vector<Point>();

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
			
			for (int i = 0; i < H.length; i++)
			{
				Hi.add(H[i]);
			}
			
			
			Point[] result = new Point[Hi.size()];
			for (int i = 0; i < Hi.size(); i++)
			{
				result[i] = Hi.elementAt(i);
			}
		
			return result;
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
		
		public Point isPoint(double x)
		{
			if (this.x == x)
				return this;
			else
				return null;
		}
		
		public boolean equals(Point o)
		{
			if (this.getXVal() == o.getXVal() && this.getYVal() == o.getYVal())
				return true;
			else
				return false;
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
	