import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class circuit {
	
	public double[] forwardBias, g;
	public static double threshold = 0.3;
	public static Vector<memory> mem = new Vector<memory>();
	public Vector<Double> q = new Vector<Double>();
	
	final int a = 16;
	
	public circuit(double[][] data, String c)
	{
		FastFourierTransformer.transformInPlace(data, DftNormalization.UNITARY, TransformType.FORWARD);
		
		this.g = new double[a];
		for (int i = 0; i < a; i++)
		{
			this.g[i] = 1;
		}
		
		this.forwardBias = new double[a];
		for (int i = 0; i < a; i++)
		{
			this.forwardBias[i] = g[i] * data[0][i];
		}
		
		store(forwardBias, c);
	}
	
	public circuit(double[][] data)
	{
		this(data, null);
	}
	
	public double getQ()
	{
		return match(this.getBackward(), this.forwardBias);
	}
	
	public void setG()
	{
		for (int i = 0; i < g.length; i++)
		{
			this.g[i] = compete(g[i], this.getQ(), this.getQ(), g.length);
		}
		
		for (int i = 0; i < g.length; i++)
		{
			this.forwardBias[i] *= g[i];
		}
		
		//return forwardBias;
	}
	
	public String recall(circuit unit)
	{
		Iterator<memory> it = mem.iterator();

		for (int i = 0; i < 30; i++)
		{
			this.setG();
		}
		
		for (int i = 0; i < g.length; i++)
		{
			//System.out.println(g[i]);
		}
		
		double similarity = 0;
		int index = 0;
		
		while (it.hasNext())
		{
			memory tmp = it.next();
			double sim = match(unit.forwardBias, tmp.weights);
			sim = Math.abs(sim);
			if (sim > similarity && tmp.getLabel() != null)
			{
				similarity = sim;
				index = mem.indexOf(tmp);
			}
			//System.out.println(mem.getLabel() + " " + sim);
		}
		
		return "Best match: " + mem.get(index).getLabel();
	}
	
	//Returns a superposition of attenuated weight vectors
	public double[] getBackward()
	{
		Iterator<memory> it = mem.iterator();
		double[] result = it.next().getMBackward();
		
		while (it.hasNext())
		{
			combine(result, it.next().getMBackward());
		}
		
		return result;
	}
	
	public void store(double[] dataToMemory, String c)
	{
		memory m = new memory(dataToMemory, c);
		
		mem.add(m);
	}
	
	public void showData()
	{
		Iterator<memory> it = mem.iterator();
		while (it.hasNext())
		{
			System.out.println(it.next().getLabel());
		}
	}
	
	
	private class memory
	{
		private double forward;
		private double[] backward, weights;
		private String label;
		
		public memory(double[] pattern, String c)
		{
			this.label = c;
			weights = new double[a];
			double j = 0;
			
			for (int i = 0; i < a; i++)
			{
				j += Math.pow(pattern[i], 2);
			}
			
			for (int i = 0; i < a; i++)
			{
				weights[i] = pattern[i] / j;
			}
			
			forward = f(match(pattern, weights), weights.length);
		}
		
		public String getLabel()
		{
			return label;
		}
		
		public double[] getMBackward()
		{
			backward = attenuate(forward, weights);
			return backward;
		}
	
	}
	
	
	public double f(double value, int n)
	{
		if (value < threshold)
			return 0;
		else
			return Math.pow(value, n);
	}
	
	public static double[] combine(double[] v1, double[] v2)
	{
		double[] result = new double[v1.length];
		
		for (int i = 0; i < v1.length; i++)
		{
			result[i] = v1[i] + v2[i];
		}
		
		return result;
	}
	
	//Scalar measure of similarity
	public static double match(double[] v1, double[] v2)
	{
		double r1 = 0, r2 = 0, r = 0;
		
		for (int i = 0; i < v1.length; i++)
		{
			r += (v1[i] * v2[i]);
		}
		
		for (int i = 0; i < v1.length; i++)
		{
			r1 += Math.pow(v1[i], 2);
			r2 += Math.pow(v2[i], 2);
		}
		
		return r / (Math.sqrt(r1) * Math.sqrt(r2));
	}
	
	public static double[] attenuate(double scalar, double[] vec)
	{
		double[] result = new double[vec.length];
		for (int i = 0; i < vec.length; i++)
		{
			result[i] = vec[i] * scalar;
		}
		
		return result;
	}
	
	//Scales all elements of the vector proportionally such that the largest element is no greater than unity
	public void scale(double[] vec)
	{
		double max = vec[0];
		
		for (int i = 0; i < vec.length; i++)
		{
			if (vec[i] > max)
			{
				max = vec[i];
			}
		}
		
		if (max > 1)
		{
			for (int i = 0; i < vec.length; i++)
			{
				vec[i] /= max;
			}
		}
		
	}
	
	public static double compete(double v1, double v2, double maxV2, double n)
	{
		double k = Math.random(), h;
		
		if (maxV2 > threshold)
			h = maxV2;
		else
			h = threshold;
		
		return Math.max(0,  v1 - (k * Math.pow(h - v2, n)));
	}

}
