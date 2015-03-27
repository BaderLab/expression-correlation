package org.baderlab.expressioncorrelation.internal.model;

import java.util.List;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class ExpressionData {

	private final CyTable table;
	private final String geneColumnName;
	private final String[] conditionNames;
	private final String[] geneNames;
	private final double[][] allValues;

	public ExpressionData(final CyTable table, final String geneColumnName, final String[] conditionNames) {
		if (table == null)
			throw new IllegalArgumentException("'table' argument is null");
		if (geneColumnName == null)
			throw new IllegalArgumentException("'geneColumnName' argument is null");
		if (table.getColumn(geneColumnName) == null)
			throw new IllegalArgumentException("The source table does not have the column \"" + geneColumnName + "\"");
		if (conditionNames == null || conditionNames.length == 0)
			throw new IllegalArgumentException("'conditionNames' argument is null or empty");
		
		this.table = table;
		this.geneColumnName = geneColumnName;
		this.conditionNames = conditionNames;
		
		// Gene names and values
		final List<CyRow> allRows = table.getAllRows();
		geneNames = new String[allRows.size()];
		allValues = new double[allRows.size()][conditionNames.length];
		int i = 0;
		
		for (final CyRow row : allRows) {
			final String name = row.get(geneColumnName, String.class);
			geneNames[i] = name;
			
			for (int j = 0; j < conditionNames.length; j++) {
				final String condition = conditionNames[j];
				final Double value = row.get(condition, Double.class);
				allValues[i][j] = value != null ? value : 0.0;
			}
			
			i++;
		}
	}

	public CyTable getTable() {
		return table;
	}
	
	public String getGeneColumnName() {
		return geneColumnName;
	}
	
	public String getName() {
		return table.getTitle();
	}

	public String[] getGeneNames() {
		return geneNames;
	}

	public String[] getConditionNames() {
		return conditionNames;
	}

	public int getNumberOfGenes() {
		return table.getRowCount();
	}

	public int getNumberOfConditions() {
		return conditionNames != null ? conditionNames.length : 0;
	}

	/**
	 * @return All gene/condition values
	 */
	public double[][] getAllValues() {
		return allValues;
	}
}
