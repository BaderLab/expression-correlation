package org.jmathplot.gui.plotObjects;



public class Base2D extends Base {

	public Base2D(double[] Xmi,double[] Xma,int[] dimension,int[] scales,double borderCoeff) {
		super(Xmi,Xma,dimension,scales,borderCoeff);
	}

	@Override
	protected double[] baseCoordsScreenProjectionRatio(double[] xy) {
		double[] sC = new double[2];
		if (axesScales[0] == LOG) {
			sC[0] = (xy[0]-Xmin[0])/(Xmax[0]-Xmin[0]);
		} else {
			sC[0] = (xy[0]-Xmin[0])/(Xmax[0]-Xmin[0]);
		}
		if (axesScales[1] == LOG) {
			sC[1] = (xy[1]-Xmin[1])/(Xmax[1]-Xmin[1]);
		} else {
			sC[1] = (xy[1]-Xmin[1])/(Xmax[1]-Xmin[1]);
		}
		
		return sC;
	}
}