package org.baderlab.expressioncorrelation.internal.action;

import static org.baderlab.expressioncorrelation.internal.model.CorrelateActionType.BUILD_NETWORK;
import static org.baderlab.expressioncorrelation.internal.model.CorrelateActionType.COND_NET_DEF;
import static org.baderlab.expressioncorrelation.internal.model.CorrelateActionType.COND_NET_PREVIEW;
import static org.baderlab.expressioncorrelation.internal.model.CorrelateActionType.GENE_NET_DEF;
import static org.baderlab.expressioncorrelation.internal.model.CorrelateActionType.GENE_NET_PREVIEW;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.expressioncorrelation.internal.model.CorrelateActionType;
import org.baderlab.expressioncorrelation.internal.model.CorrelateSimilarityNetwork;
import org.baderlab.expressioncorrelation.internal.task.CorrelateTask;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Elena Potylitsine
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
 * *
 * Created by IntelliJ IDEA.
 * User: Elena Potylitsine
 * Date: Jul 13, 2004
 * Time: 4:34:43 PM
 * * Description: This CorrelateAction listens to the selections made by the user.
 * *It checks that an Expression Data Matrix is loaded before execution of the commands.
 * *It displays a warning window explaining that the Pearson Correaltion coefficient
 * *is not an optimal algorithm for condition matrixes that contain fewer then 4 parameters
 */

public class CorrelateAction extends AbstractCyAction {

	private static final long serialVersionUID = 6449229882550117844L;
	
    private final CorrelateActionType type;
    private final CyServiceRegistrar serviceRegistrar;

	public CorrelateAction(final CorrelateActionType type, final CyServiceRegistrar serviceRegistrar) {
		super(type.toString());
		this.type = type;
		this.serviceRegistrar = serviceRegistrar;
	}
    
	@Override
	public void actionPerformed(final ActionEvent e) {
        final JFrame parentFrame = serviceRegistrar.getService(CySwingApplication.class).getJFrame();

        final Set<CyTable> globalTables = serviceRegistrar.getService(CyTableManager.class).getGlobalTables();
        
// TODO: First check there is at least one unassigned table        
        if (globalTables == null || globalTables.isEmpty()) {
// TODO: None: "Want to import one now?" :: yes => Run import table task
            JOptionPane.showMessageDialog(parentFrame, "You must load an Expression Matrix File to run this plugin.", "ALERT!!!", JOptionPane.ERROR_MESSAGE);
        } else {
// TODO: "Select the data table from the list:" (also show button to import new one)
        	final CyTable table = globalTables.iterator().next(); // TODO
        	final CorrelateSimilarityNetwork network = new CorrelateSimilarityNetwork(table, serviceRegistrar);
        	
            int colNumber = network.getNumberOfCols(); //number of conditions in the condition network
            int rowNumber = network.getNumberOfRows();
            int selectedOption = JOptionPane.OK_OPTION;

            if (rowNumber < 4 && (type == BUILD_NETWORK || type == COND_NET_PREVIEW || type == COND_NET_DEF)) {
            	// Must only come up in case when < 4 genes and try to do condition matrix
                // the vectors for condition matrix will be of length < 4 not enough
                Object[] options = {
                		"The expresssion data contains less then 4 genes (" + rowNumber + " genes found)." + '\n' +
                        "The Pearson Correlation calculation will not produce" + '\n' +
                        "reliable results for correlating the condition matrix." + '\n' +
                        "Would you like to proceed?"
                };
                selectedOption = JOptionPane.showConfirmDialog(parentFrame, options, "NOT ENOUGH GENES", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            } else if (colNumber < 4 && (type == BUILD_NETWORK || type == GENE_NET_PREVIEW || type == GENE_NET_DEF)) {
            	// Must only come up in case when < 4 conditions and try to do gene matrix
                // the vectors for condition matrix will be of length < 4 not enough
                Object[] options = {
                		"The expresssion data contains less then 4 conditions (" + colNumber + " conditions found)." + '\n' +
                        "The Pearson Correlation calculation will not produce" + '\n' +
                        "reliable results for correlating the gene matrix." + '\n' +
                        "Would you like to proceed?"
                };
                selectedOption = JOptionPane.showConfirmDialog(parentFrame, options, "NOT ENOUGH CONDITIONS", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            }
            
            // Procede if not canceled
            if (selectedOption == JOptionPane.OK_OPTION) {
	            // Create a Correlate Task
	            final Task task = new CorrelateTask(type, network, serviceRegistrar);
	
	            // Execute Task via TaskManager
	            final DialogTaskManager taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
	            taskMgr.execute(new TaskIterator(task));
            }
        }
    }
}

