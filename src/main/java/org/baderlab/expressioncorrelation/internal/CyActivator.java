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

public class CyActivator extends AbstractCyActivator {

	private static final String APP_MENU = "Apps.ExpressionCorrelation";
	private static final String ADVANCED_OPTIONS_MENU = APP_MENU + ".Advanced Options[2.0]";
	
	@Override
	public void start(final BundleContext bc) throws Exception {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		
		// Set-up menu options in Apps menu
		final CorrelateAction correlateAction1 = new CorrelateAction(BUILD_NETWORK, serviceRegistrar);
		correlateAction1.setPreferredMenu(APP_MENU);
		correlateAction1.setMenuGravity(1.0f);
		registerAllServices(bc, correlateAction1, new Properties());
		
		final CorrelateAction correlateAction2 = new CorrelateAction(COND_NET_PREVIEW, serviceRegistrar);
		correlateAction2.setPreferredMenu(ADVANCED_OPTIONS_MENU);
		correlateAction2.setMenuGravity(2.1f);
		registerAllServices(bc, correlateAction2, new Properties());
		
		final CorrelateAction correlateAction3 = new CorrelateAction(COND_NET_DEF, serviceRegistrar);
		correlateAction3.setPreferredMenu(ADVANCED_OPTIONS_MENU);
		correlateAction3.setMenuGravity(2.2f);
		registerAllServices(bc, correlateAction3, new Properties());
        
		final CorrelateAction correlateAction4 = new CorrelateAction(GENE_NET_PREVIEW, serviceRegistrar);
		correlateAction4.setPreferredMenu(ADVANCED_OPTIONS_MENU);
		correlateAction4.setMenuGravity(2.3f);
		registerAllServices(bc, correlateAction4, new Properties());
		
		final CorrelateAction correlateAction5 = new CorrelateAction(GENE_NET_DEF, serviceRegistrar);
		correlateAction5.setPreferredMenu(ADVANCED_OPTIONS_MENU);
		correlateAction5.setMenuGravity(2.4f);
		registerAllServices(bc, correlateAction5, new Properties());
        
        final HelpAction helpAction = new HelpAction("Help", serviceRegistrar);
		helpAction.setPreferredMenu(APP_MENU);
		helpAction.setMenuGravity(3.0f);
		registerAllServices(bc, helpAction, new Properties());
		
		final AboutAction aboutAction = new AboutAction("About", serviceRegistrar);
		aboutAction.setPreferredMenu(APP_MENU);
		aboutAction.setMenuGravity(4.0f);
		registerAllServices(bc, aboutAction, new Properties());
	}
}
