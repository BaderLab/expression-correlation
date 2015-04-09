package org.baderlab.expressioncorrelation.internal.model;

import static org.cytoscape.model.CyNetwork.NAME;
import static org.cytoscape.model.subnetwork.CyRootNetwork.SHARED_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.LoadTableReaderTask;
import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

public class CorrelateSimilarityNetworkTest {

	@Mock CyServiceRegistrar serviceRegistrar;
	@Mock CyApplicationManager appMgr;
	@Mock CyNetworkViewManager netViewMgr;
	@Mock VisualMappingManager vmMgr;
	@Mock CyLayoutAlgorithmManager layoutMgr;
	@Mock NetworkViewRenderer netViewRenderer;
	@Mock VisualStyle style;
	@Mock TaskMonitor tm;
	
	CyNetworkManager netMgr;
	CyNetworkFactory netFactory;
	CyNetworkViewFactory netViewFactory;
	CyTableFactory tableFactory;
	
	CyTable table;
	ExpressionData data;
	CorrelateSimilarityNetwork csNetwork;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		final NetworkTestSupport netTestSupport = new NetworkTestSupport();
		final NetworkViewTestSupport netViewTestSupport = new NetworkViewTestSupport();
		final TableTestSupport tableTestSupport = new TableTestSupport();
		
		netMgr = netTestSupport.getNetworkManager();
		netFactory = netTestSupport.getNetworkFactory();
		netViewFactory = netViewTestSupport.getNetworkViewFactory();
		tableFactory = tableTestSupport.getTableFactory();
		
		final Properties props = new Properties();
		props.setProperty("viewThreshold", "500");
		final CyProperty<Properties> cyProperty =
				new SimpleCyProperty<>("cytoscape3.props", props, Properties.class, SavePolicy.DO_NOT_SAVE);
		
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netMgr);
		when(serviceRegistrar.getService(CyNetworkViewManager.class)).thenReturn(netViewMgr);
		when(serviceRegistrar.getService(CyApplicationManager.class)).thenReturn(appMgr);
		when(serviceRegistrar.getService(VisualMappingManager.class)).thenReturn(vmMgr);
		when(serviceRegistrar.getService(CyLayoutAlgorithmManager.class)).thenReturn(layoutMgr);
		when(serviceRegistrar.getService(CyNetworkFactory.class)).thenReturn(netFactory);
		when(serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)")).thenReturn(cyProperty);
		
		when(netViewRenderer.getNetworkViewFactory()).thenReturn(netViewFactory);
		when(appMgr.getDefaultNetworkViewRenderer()).thenReturn(netViewRenderer);
		when(vmMgr.getCurrentVisualStyle()).thenReturn(style);
		
		table = importTable("/Rosetta.mrna");
		data = ExpressionData.createDefault(table);
		csNetwork = new CorrelateSimilarityNetwork(data, serviceRegistrar);
	}
	
	@Test
	public void testCorrelateSimilarityNetwork() {
		assertEquals("Number of genes", 6152, csNetwork.getNumberOfRows());
		assertEquals("Number of conditions", 300, csNetwork.getNumberOfCols());
	}
	
	@Test
	public void testCalcRows() {
		final CyNetwork net = csNetwork.calcRows(tm);
		final String netName = net.getRow(net).get(CyNetwork.NAME, String.class);
		
		assertEquals("Network name", "AttrTable Rosetta.mrna 1 (genes): -0.95 & 0.95", netName);
		assertEquals("Node count", 29, net.getNodeCount());
		assertEquals("Edge count", 37, net.getEdgeCount());
		
		// Node degrees
		assertNodeDegree(2, "YBR012W_A", net);
		assertNodeDegree(5, "YBR301W", net);
		assertNodeDegree(1, "YCL019W", net);
		assertNodeDegree(1, "YDL245C", net);
		assertNodeDegree(1, "YDL248W", net);
		assertNodeDegree(1, "YDR313C", net);
		assertNodeDegree(3, "YEL049W", net);
		assertNodeDegree(3, "YER138C", net);
		assertNodeDegree(5, "YFL020C", net);
		assertNodeDegree(1, "YFL062W", net);
		assertNodeDegree(1, "YGR294W", net);
		assertNodeDegree(2, "YGR295C", net);
		assertNodeDegree(4, "YHL046C", net);
		assertNodeDegree(1, "YIL176C", net);
		assertNodeDegree(1, "YJL177W", net);
		assertNodeDegree(8, "YJL223C", net);
		assertNodeDegree(2, "YJR028W", net);
		assertNodeDegree(1, "YJR158W", net);
		assertNodeDegree(3, "YJR161C", net);
		assertNodeDegree(6, "YLL064C", net);
		assertNodeDegree(3, "YLR461W", net);
		assertNodeDegree(1, "YML039W", net);
		assertNodeDegree(1, "YML132W", net);
		assertNodeDegree(1, "YMR045C", net);
		assertNodeDegree(2, "YMR051C", net);
		assertNodeDegree(1, "YMR089C", net);
		assertNodeDegree(6, "YNR076W", net);
		assertNodeDegree(6, "YOL161C", net);
		assertNodeDegree(1, "YPR044C", net);
		
		// Edge strengths
		assertEdgeValues(0.9504455312129428, "YER138C_interaction_YCL019W", net);
		assertEdgeValues(0.974244687485713, "YFL020C_interaction_YBR301W", net);
		assertEdgeValues(0.9508673761418709, "YGR295C_interaction_YFL062W", net);
		assertEdgeValues(0.9648272888891004, "YIL176C_interaction_YEL049W", net);
		assertEdgeValues(0.9537203127649099, "YJL223C_interaction_YBR301W", net);
		assertEdgeValues(0.9515865308700858, "YJL223C_interaction_YEL049W", net);
		assertEdgeValues(0.9544591550072195, "YJL223C_interaction_YFL020C", net);
		assertEdgeValues(0.9585130147089678, "YJL223C_interaction_YHL046C", net);
		assertEdgeValues(0.9595760648577858, "YJR028W_interaction_YBR012W_A", net);
		assertEdgeValues(0.9558970703800223, "YJR158W_interaction_YDL245C", net);
		assertEdgeValues(0.951717372242077, "YJR161C_interaction_YDL248W", net);
		assertEdgeValues(0.9528429371793666, "YJR161C_interaction_YGR295C", net);
		assertEdgeValues(0.9609767957938035, "YLL064C_interaction_YBR301W", net);
		assertEdgeValues(0.9597407184812744, "YLL064C_interaction_YFL020C", net);
		assertEdgeValues(0.9589704842782178, "YLL064C_interaction_YGR294W", net);
		assertEdgeValues(0.9640098198073541, "YLL064C_interaction_YJL223C", net);
		assertEdgeValues(0.959052285060014, "YLR461W_interaction_YEL049W", net);
		assertEdgeValues(0.9517419327942324, "YLR461W_interaction_YHL046C", net);
		assertEdgeValues(0.9509149497277245, "YLR461W_interaction_YJL223C", net);
		assertEdgeValues(0.9625062449917634, "YML039W_interaction_YER138C", net);
		assertEdgeValues(0.9510137935045974, "YML132W_interaction_YJR161C", net);
		assertEdgeValues(0.9541481233327844, "YMR045C_interaction_YER138C", net);
		assertEdgeValues(0.9619406728744117, "YMR051C_interaction_YBR012W_A", net);
		assertEdgeValues(0.9592485851452772, "YMR051C_interaction_YJR028W", net);
		assertEdgeValues(0.9531220031307993, "YMR089C_interaction_YDR313C", net);
		assertEdgeValues(0.9541196068653266, "YNR076W_interaction_YBR301W", net);
		assertEdgeValues(0.9544383715672098, "YNR076W_interaction_YFL020C", net);
		assertEdgeValues(0.9554099118123409, "YNR076W_interaction_YHL046C", net);
		assertEdgeValues(0.9628818567546302, "YNR076W_interaction_YJL223C", net);
		assertEdgeValues(0.9636144413719133, "YNR076W_interaction_YLL064C", net);
		assertEdgeValues(0.9564803881934245, "YOL161C_interaction_YBR301W", net);
		assertEdgeValues(0.9541730809120812, "YOL161C_interaction_YFL020C", net);
		assertEdgeValues(0.9518942764325667, "YOL161C_interaction_YHL046C", net);
		assertEdgeValues(0.9602880341520311, "YOL161C_interaction_YJL223C", net);
		assertEdgeValues(0.9646826673056724, "YOL161C_interaction_YLL064C", net);
		assertEdgeValues(0.9582463576839262, "YOL161C_interaction_YNR076W", net);
		assertEdgeValues(0.9500346789714313, "YPR044C_interaction_YJL177W", net);
		
		// All edge interaction values must be "pos_interaction"
		final int count = net.getDefaultEdgeTable().countMatchingRows("interaction", "pos_interaction");
		assertEquals(net.getEdgeCount(), count);
	}
	
	private void assertNodeDegree(final int degree, final String nodeName, final CyNetwork net) {
		final CyNode node = getNodeByName(net, nodeName);
		final int actual = net.getAdjacentEdgeList(node, CyEdge.Type.ANY).size();
		assertEquals("Node degree for " + nodeName, degree, actual);
	}
	
	private void assertEdgeValues(final double strength, final String edgeName, final CyNetwork net) {
		final CyEdge edge = getEdgeByName(net, edgeName);
		final double actual = net.getRow(edge).get("Strength", Double.class);
		assertEquals("Edge strength for " + edgeName, strength, actual, 1e-16);
	}
	
	protected CyNode getNodeByName(final CyNetwork net, final String name) {
		for (CyNode n : net.getNodeList()) {
			CyRow row = net.getRow(n);
			
			if (name.equals(row.get(NAME, String.class)) || name.equals(row.get(SHARED_NAME, String.class)))
				return n;
		}
		
		return null;
	}
	
	protected CyEdge getEdgeByName(final CyNetwork net, final String name) {
		for (CyEdge e : net.getEdgeList()) {
			CyRow row = net.getRow(e);
			
			if (name.equals(row.get(NAME, String.class)) || name.equals(row.get(SHARED_NAME, String.class)))
				return e;
		}
		
		return null;
	}
	
	private CyTable importTable(final String filePath) throws Exception {
		CytoscapeServices.cyTableFactory = tableFactory;
		
		final InputStream is = getClass().getResourceAsStream(filePath);
		final LoadTableReaderTask tableReaderTask = new LoadTableReaderTask(is, ".txt", "Rosetta.mrna");
		tableReaderTask.firstRowAsColumnNames = true;
		tableReaderTask.keyColumnIndex = 0;
		tableReaderTask.startLoadRow = 0;
		tableReaderTask.run(tm);
		
		return tableReaderTask.getTables()[0];
	}
}
