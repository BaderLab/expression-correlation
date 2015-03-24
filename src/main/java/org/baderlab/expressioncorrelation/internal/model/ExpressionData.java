package org.baderlab.expressioncorrelation.internal.model;

import java.util.Vector;

import org.cytoscape.model.CyTable;

public class ExpressionData {

	private final CyTable table;

	public ExpressionData(final CyTable table) {
		this.table = table;
	}

	public String getName() {
		return table.getTitle();
	}

	public String[] getGeneNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getConditionNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumberOfGenes() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getNumberOfConditions() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Vector<Vector<mRNAMeasurement>> getAllMeasurements() {
		// TODO Auto-generated method stub
		return null;
	}
}
