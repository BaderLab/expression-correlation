package org.baderlab.expressioncorrelation.internal.model;

public enum CorrelateActionType {
	// construct both row and column
	BUILD_NETWORK("Construct Correlation Networks"),
	// construct col similarity matrix using defaults
	COND_NET_DEF("Condition Network: Using Defaults"),
	// construct histogram + col
	COND_NET_PREVIEW("Condition Network: Preview Histogram"),
	// construct row similarity matrix using defaults
	GENE_NET_DEF("Gene Network: Using Defaults"),
	// construct histogram + row
	GENE_NET_PREVIEW("Gene Network: Preview Histogram");
	
	private final String name;

	private CorrelateActionType(final String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
