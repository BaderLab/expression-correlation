package org.baderlab.expressioncorrelation.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Christian Lopes
 * * Authors: Gary Bader, Elena Potylitsine, Chris Sander, Weston Whitaker
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no type shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

public class ExpressionData {

	private final CyTable table;
	private final String geneColumnName;
	private final String[] conditionNames;
	private String[] geneNames;
	private double[][] allValues;
	
	private final Object lock = new Object();

	public ExpressionData(final CyTable table, final String geneColumnName, final String[] conditionNames) {
		if (table == null)
			throw new IllegalArgumentException("'table' argument is null");
		if (!isValidExpressionData(table))
			throw new IllegalArgumentException("'table' cannot represent valid expression data");
		if (geneColumnName == null)
			throw new IllegalArgumentException("'geneColumnName' argument is null");
		if (table.getColumn(geneColumnName) == null)
			throw new IllegalArgumentException("The source table does not have the column \"" + geneColumnName + "\"");
		if (conditionNames == null || conditionNames.length == 0)
			throw new IllegalArgumentException("'conditionNames' argument is null or empty");
		
		this.table = table;
		this.geneColumnName = geneColumnName;
		this.conditionNames = conditionNames;
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
		synchronized (lock) {
			if (geneNames == null)
				init();
		}
		
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
		synchronized (lock) {
			if (allValues == null)
				init();
		}
		
		return allValues;
	}
	
	public static boolean isValidExpressionData(final CyTable table) {
		if (table != null) {
			boolean hasName = false;
			boolean hasCondition = false;
			
			for (final CyColumn col : table.getColumns()) {
				if (hasName == false && col.getType() == String.class)
					hasName = true;
				else if (hasCondition == false && isValidConditionColumn(col))
					hasCondition = true;
				
				if (hasName && hasCondition)
					return true;
			}
		}
		
		return false;
	}
	
	public static ExpressionData createDefault(final CyTable table) {
		final CyColumn geneColumn = getDefaultGeneColumn(table);
		final String[] conditionNames = getDefaultConditionNames(table);
		
		return new ExpressionData(table, geneColumn.getName(), conditionNames);
	}
	
	public static CyColumn getDefaultGeneColumn(final CyTable table) {
		// Preferred column names for the gene column
		final String[] tokens = new String[] { "genename", "gene", "name" };
		
		// First pass: Look for exact column name
		// Second pass: Select column whose name contains one of the tokens
		// Third pass: Just return the first String typed column
		for (int count = 0; count < 3; count++) {
			for (final CyColumn col : table.getColumns()) {
				if (col.getType() != String.class)
					continue;
				
				if (count == 2)
					return col;
				
				// Remove all special chars and spaces from column name
				final String name = col.getName().replaceAll("[^a-zA-Z]", "").toLowerCase();
				
				for (final String s : tokens) {
					if ( (count == 0 && name.equals(s)) || (count == 1 && name.contains(s)) )
						return col;
				}
			}
		}
		
		return null;
	}
	
	public static String[] getDefaultConditionNames(final CyTable table) {
		final List<String> names = new ArrayList<>();
		
		for (final CyColumn col : table.getColumns()) {
			if (isValidConditionColumn(col))
				names.add(col.getName());
		}
		
		return names.toArray(new String[names.size()]);
	}
	
	public static boolean isValidConditionColumn(final CyColumn col) {
		return Number.class.isAssignableFrom(col.getType()) && 
				!col.getName().equals(CyIdentifiable.SUID) && !col.getName().endsWith(".SUID");
	}
	
	private void init() {
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
				final Number value = row.get(condition, Number.class);
				allValues[i][j] = value != null ? value.doubleValue() : 0.0;
			}
			
			i++;
		}
	}
}
