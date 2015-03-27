package org.baderlab.expressioncorrelation.internal.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import org.baderlab.expressioncorrelation.internal.view.AboutDialog;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;

/**
 * * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Gary Bader
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
 * *
 * * User: Gary Bader
 * * Date: Jun 25, 2004
 * * Time: 5:38:52 PM
 * * Description: The action to show the About dialog box
 */

/**
 * The action to show the About dialog box
 */
public class AboutAction extends AbstractCyAction {
    
	private static final long serialVersionUID = -1214515517408411083L;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public AboutAction(final String name, final CyServiceRegistrar serviceRegistrar) {
		super(name);
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
    public void actionPerformed(final ActionEvent e) {
		final JFrame parentFrame = serviceRegistrar.getService(CySwingApplication.class).getJFrame();
		
		// Display about box
        final AboutDialog aboutDialog = new AboutDialog(parentFrame);
        aboutDialog.pack();
        aboutDialog.setVisible(true);
    }
}
