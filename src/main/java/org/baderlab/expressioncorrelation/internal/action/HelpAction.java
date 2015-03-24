package org.baderlab.expressioncorrelation.internal.action;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;

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
