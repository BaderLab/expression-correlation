package org.baderlab.expressioncorrelation.internal.action;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;

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

/**
 * The action to show the ExpressionCorrelation help -- links to the documentation web site.
 */
public class HelpAction extends AbstractCyAction {
    
	private static final long serialVersionUID = 4935341904521776081L;

	private static final String DOC_URL = "http://www.baderlab.org/Software/ExpressionCorrelation";

	private final CyServiceRegistrar serviceRegistrar;
	
	public HelpAction(final String name, final CyServiceRegistrar serviceRegistrar) {
		super(name);
		this.serviceRegistrar = serviceRegistrar;
	}
	
    @Override
    public void actionPerformed(final ActionEvent e) {
    	final OpenBrowser openBrowser = serviceRegistrar.getService(OpenBrowser.class);
    	openBrowser.openURL(DOC_URL);
    }
}
