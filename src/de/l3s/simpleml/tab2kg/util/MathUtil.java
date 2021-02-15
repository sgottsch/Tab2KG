package de.l3s.simpleml.tab2kg.util;

import java.util.List;

public class MathUtil {

	public static double correlation(long[] xs, long[] ys) {

		long sx = 0;
		long sy = 0;
		long sxx = 0;
		long syy = 0;
		long sxy = 0;

		int n = xs.length;

		for (int i = 0; i < n; ++i) {
			long x = xs[i];
			long y = ys[i];

			sx += x;
			sy += y;
			sxx += x * x;
			syy += y * y;
			sxy += x * y;
		}

		// covariation
		double cov = sxy / n - sx * sy / n / n;
		// standard error of x
		double sigmax = Math.sqrt(sxx / n - sx * sx / n / n);
		// standard error of y
		double sigmay = Math.sqrt(syy / n - sy * sy / n / n);

		// correlation is just a normalized covariation
		return cov / sigmax / sigmay;
	}
	
	public static double correlation(List<Number> xs, List<Number> ys) {

		double sx = 0;
		double sy = 0;
		double sxx = 0;
		double syy = 0;
		double sxy = 0;

		int n = xs.size();

		for (int i = 0; i < n; ++i) {
			double x = (double) xs.get(i);
			double y = (double) ys.get(i);

			sx += x;
			sy += y;
			sxx += x * x;
			syy += y * y;
			sxy += x * y;
		}

		// covariation
		double cov = sxy / n - sx * sy / n / n;
		// standard error of x
		double sigmax = Math.sqrt(sxx / n - sx * sx / n / n);
		// standard error of y
		double sigmay = Math.sqrt(syy / n - sy * sy / n / n);

		// correlation is just a normalized covariation
		return cov / sigmax / sigmay;
	}

}
