package org.jmathplot.gui.components;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jmathplot.gui.MatrixTablePanel;
import org.jmathplot.gui.PlotPanel;

/**
 * <p>Copyright : BSD License</p>

 * @author Yann RICHET
 * @version 3.0
 */

public class DatasFrame	extends JFrame {

	private static final long serialVersionUID = 7056508830774691525L;
	
	private PlotPanel plotPanel;
	private JPanel panel;

	public DatasFrame(PlotPanel p) {
		super("Datas");
		
		plotPanel = p;
		setPanel();
		setContentPane(panel);
		this.pack();
		setVisible(true);
	}

	private void setPanel() {
		JTabbedPane panels = new JTabbedPane();
		for (int i = 0; i < plotPanel.getPlots().length; i++) {
			panels.add(new MatrixTablePanel(plotPanel.getPlot(i).getDatas()),
				plotPanel.getPlot(i).getName());
		}
		panel = new JPanel();
		panel.add(panels);
	}
}
