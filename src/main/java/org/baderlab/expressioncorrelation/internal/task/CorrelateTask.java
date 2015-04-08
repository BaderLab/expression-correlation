package org.baderlab.expressioncorrelation.internal.task;

import org.baderlab.expressioncorrelation.internal.model.CorrelateActionType;
import org.baderlab.expressioncorrelation.internal.model.CorrelateSimilarityNetwork;
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
 * This class represents a task that calls methods to calculate a correlation csNetwork
 */
public class CorrelateTask implements Task {

    /**
     * Sets which type thread to run and time
     * case 1: Run and time both the column and row networks
     * case 2: Run and time the column csNetwork
     * case 3: Run and time the column histogram
     * case 4: Run and time the row csNetwork
     * case 5: Run and time the row histogram
     */
    private final CorrelateActionType type;
    private final CorrelateSimilarityNetwork csNetwork;
	private final CyServiceRegistrar serviceRegistrar;

    /**
     * Constructor.
     *
     * @param type Indicates which type to run.
     * @param csNetwork The csNetwork upon which to calculate correlations
     */
    public CorrelateTask(final CorrelateActionType type, final CorrelateSimilarityNetwork csNetwork,
    		final CyServiceRegistrar serviceRegistrar) {
        this.type = type;
        this.csNetwork = csNetwork;
        this.serviceRegistrar = serviceRegistrar;
    }

    /**
     * Perform the correlation calculations on the csNetwork.
     */
    @Override
	public void run(final TaskMonitor tm) throws Exception {
    	tm.setTitle("Performing Correlation Calculations");
    	
        switch (type) {
            case BUILD_NETWORK:
                colRun(tm);
                rowRun(tm);
                break;
            case COND_NET_DEF:
                colRun(tm);
                break;
            case GENE_NET_DEF:
                rowRun(tm);
                break;
            case COND_NET_PREVIEW:
            	csNetwork.loadColCutoffs(); // Loads previously saved user column cutoffs from the singleton class
            	csNetwork.colHistogram(tm);
            	break;
            case GENE_NET_PREVIEW:
            	csNetwork.loadRowCutoffs(); // Loads previously saved user row cutoffs from the singleton class
            	csNetwork.rowHistogram(tm);
			default:
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
     * The condition matrix calculation
     */
	private void colRun(final TaskMonitor tm) {
		final CyNetwork net = csNetwork.calcCols(tm);

		if (csNetwork.cancelled())
			destroy(net);
	}

	/**
	 * The gene matrix calculation
	 */
	private void rowRun(final TaskMonitor tm) {
		final CyNetwork net = csNetwork.calcRows(tm);

		if (csNetwork.cancelled())
			destroy(net);
	}

	@Override
	public void cancel() {
		csNetwork.cancel();
	}
}