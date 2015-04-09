package org.baderlab.expressioncorrelation.internal.model;

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
