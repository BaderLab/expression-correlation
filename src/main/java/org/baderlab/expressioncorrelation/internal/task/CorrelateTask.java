package org.baderlab.expressioncorrelation.internal.task;

import javax.swing.JFrame;

import org.baderlab.expressioncorrelation.internal.model.CorrelateActionType;
import org.baderlab.expressioncorrelation.internal.model.CorrelateSimilarityNetwork;
import org.baderlab.expressioncorrelation.internal.view.CorrelateHistogramDialog;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

/**
 * Copyright (c) 2007
 * *
 * * Code written by: Shirley Hui
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
 * * User: Shirley Hui
 * * Date: Apr 4, 2007
 * * Time: 3:35:11 April 7, 2004
 * * Description: CorrelateTask
 * *
 * *
 */


/**
 * This class represents a task that calls methods to calculate a correlation network and/or corresponding histogram
 */
public class CorrelateTask implements Task {

    private CorrelateHistogramDialog histogram;
    
    /**
     * Sets which type thread to run and time
     * case 1: Run and time both the column and row networks
     * case 2: Run and time the column network
     * case 3: Run and time the column histogram
     * case 4: Run and time the row network
     * case 5: Run and time the row histogram
     */
    private final CorrelateActionType type;
    private final CorrelateSimilarityNetwork network;
	private final CyServiceRegistrar serviceRegistrar;

    /**
     * Constructor.
     *
     * @param type Indicates which type to run.
     * @param network The network upon which to calculate correlations
     */
    public CorrelateTask(final CorrelateActionType type, final CorrelateSimilarityNetwork network,
    		final CyServiceRegistrar serviceRegistrar) {
        this.type = type;
        this.network = network;
        this.serviceRegistrar = serviceRegistrar;
    }

    /**
     * Perform the correlation calculations on the network.
     */
    @Override
	public void run(final TaskMonitor tm) throws Exception {
        switch (type) {
            case BUILD_NETWORK:
                colRun();
                rowRun();
                break;
            case COND_NET_DEF:
                colRun();
                break;
            case COND_NET_PREVIEW:
                colHistogram();
                break;
            case GENE_NET_DEF:
                rowRun();
                break;
            case GENE_NET_PREVIEW:
                rowHistogram();
                break;
        }
    }

    private void destroy(final CyNetwork net) {
        if (net != null) {
        	final CyNetworkManager netMgr = serviceRegistrar.getService(CyNetworkManager.class);
        	
        	if (netMgr.networkExists(net.getSUID()))
        		netMgr.destroyNetwork(net);
        	else
        		net.dispose();
		}
    }

    /**
	 * Halts the Task: Not Currently Implemented.
	 */
// TODO
	public void halt() {
		// Task can not currently be halted.
        network.cancel();
        System.out.println("User cancelled task.");
        
		if (histogram != null) {
            System.out.println("Disposing histogram dialog");
            histogram.dispose();
            histogram.setVisible(false);
        }
    }

    /**
     * Gets the Task Title.
     *
     * @return human readable task title.
     */
// TODO
    public String getTitle() {
        return new String("Performing correlation calculations");
    }

    /**
     * The condition matrix calculation
     */
	private void colRun() {
		final CyNetwork net = network.calcCols();

		if (network.cancelled())
			destroy(net);
	}

	/**
	 * The gene matrix calculation
	 */
	private void rowRun() {
		final CyNetwork net = network.calcRows();

		if (network.cancelled())
			destroy(net);
	}

    /**
     * The condition histogram calculation
     */
    private void colHistogram() {
		final JFrame parentFrame = serviceRegistrar.getService(CySwingApplication.class).getJFrame();
		
		histogram = new CorrelateHistogramDialog(parentFrame, false, network, serviceRegistrar); // not-row histogram
		histogram.pack();
		histogram.setVisible(true);
    }

    /**
     * The gene histogram calculation
     */
    private void rowHistogram() {
		final JFrame parentFrame = serviceRegistrar.getService(CySwingApplication.class).getJFrame();
		
		histogram = new CorrelateHistogramDialog(parentFrame, true, network, serviceRegistrar); // row histogram
		histogram.pack();
		histogram.setVisible(true);
    }
	
	@Override
	public void cancel() {
		// TODO Auto-generated method stub
	}
}