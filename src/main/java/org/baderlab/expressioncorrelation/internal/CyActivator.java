package org.baderlab.expressioncorrelation.internal;

import static org.baderlab.expressioncorrelation.internal.model.CorrelateActionType.BUILD_NETWORK;
import static org.baderlab.expressioncorrelation.internal.model.CorrelateActionType.COND_NET_DEF;
import static org.baderlab.expressioncorrelation.internal.model.CorrelateActionType.COND_NET_PREVIEW;
import static org.baderlab.expressioncorrelation.internal.model.CorrelateActionType.GENE_NET_DEF;
import static org.baderlab.expressioncorrelation.internal.model.CorrelateActionType.GENE_NET_PREVIEW;

import java.util.Properties;

import org.baderlab.expressioncorrelation.internal.action.AboutAction;
import org.baderlab.expressioncorrelation.internal.action.CorrelateAction;
import org.baderlab.expressioncorrelation.internal.action.HelpAction;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	private static final String APP_MENU = "Apps.ExpressionCorrelation";
	private static final String ADVANCED_OPTIONS_MENU = APP_MENU + ".Advanced Options";
	
	@Override
	public void start(final BundleContext bc) throws Exception {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		
		// Set-up menu options in Apps menu
		int gravity = 0;
		
		final CorrelateAction correlateAction1 = new CorrelateAction(BUILD_NETWORK, serviceRegistrar);
		correlateAction1.setPreferredMenu(APP_MENU);
		correlateAction1.setMenuGravity(gravity++);
		registerAllServices(bc, correlateAction1, new Properties());
		
		final CorrelateAction correlateAction2 = new CorrelateAction(COND_NET_PREVIEW, serviceRegistrar);
		correlateAction2.setPreferredMenu(ADVANCED_OPTIONS_MENU);
		correlateAction2.setMenuGravity(gravity++);
		registerAllServices(bc, correlateAction2, new Properties());
		
		final CorrelateAction correlateAction3 = new CorrelateAction(COND_NET_DEF, serviceRegistrar);
		correlateAction3.setPreferredMenu(ADVANCED_OPTIONS_MENU);
		correlateAction3.setMenuGravity(gravity++);
		registerAllServices(bc, correlateAction3, new Properties());
        
		final CorrelateAction correlateAction4 = new CorrelateAction(GENE_NET_PREVIEW, serviceRegistrar);
		correlateAction4.setPreferredMenu(ADVANCED_OPTIONS_MENU);
		correlateAction4.setMenuGravity(gravity++);
		registerAllServices(bc, correlateAction4, new Properties());
		
		final CorrelateAction correlateAction5 = new CorrelateAction(GENE_NET_DEF, serviceRegistrar);
		correlateAction5.setPreferredMenu(ADVANCED_OPTIONS_MENU);
		correlateAction5.setMenuGravity(gravity++);
		registerAllServices(bc, correlateAction5, new Properties());
        
        final HelpAction helpAction = new HelpAction("Help", serviceRegistrar);
		helpAction.setPreferredMenu(APP_MENU);
		helpAction.setMenuGravity(gravity++);
		registerAllServices(bc, helpAction, new Properties());
		
		final AboutAction aboutAction = new AboutAction("About", serviceRegistrar);
		aboutAction.setPreferredMenu(APP_MENU);
		aboutAction.setMenuGravity(gravity++);
		registerAllServices(bc, aboutAction, new Properties());
	}
}
