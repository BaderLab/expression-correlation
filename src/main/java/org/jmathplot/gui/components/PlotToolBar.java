package org.jmathplot.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.AccessControlException;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.jmathplot.gui.PlotPanel;

/**
 * Allow a separate floatable toolbar which can
 * manage multiple plotpanels (they become selected)


 * <p>Copyright : BSD License</p>

 * @author Yann RICHET - horst.fiedler@tifff.com
 * @version 1.0
 */

public class PlotToolBar extends JToolBar {

	private static final long serialVersionUID = -5529481567586878489L;

	public ButtonGroup buttonGroup;
	public JToggleButton buttonCenter;
	public JToggleButton buttonZoom;
	protected JToggleButton buttonRotate;
	protected JToggleButton buttonViewCoords;
	protected JButton buttonSetScales;
	protected JButton buttonDatas;
	protected JButton buttonSaveGraphic;
	protected JButton buttonReset;

	private boolean denySaveSecurity;
	private JFileChooser fileChooser;

	/** the currently selected PlotPanel */
	private PlotPanel plotPanel;

	public PlotToolBar(PlotPanel pp) {
		plotPanel = pp;

		try {
			fileChooser = new JFileChooser();
		} catch (AccessControlException ace) {
			denySaveSecurity = true;
		}

		buttonGroup = new ButtonGroup();

		buttonCenter = new JToggleButton("Pan Mode", plotPanel.getActionMode() == PlotPanel.TRANSLATION_MODE);
		buttonCenter.setToolTipText("Click and drag to pan the chart");

		buttonZoom = new JToggleButton("Zoom Mode", plotPanel.getActionMode() == PlotPanel.ZOOM_MODE);
		buttonZoom.setToolTipText(
				"<html><b>Left-Click</b> and drag to zoom in.<br />" +
				"<b>Rigth-Click</b> to zoom out.</html>"
		);

		buttonSaveGraphic = new JButton("Export");
		buttonSaveGraphic.setToolTipText("Save graphics to a .PNG File");

		buttonReset = new JButton("Reset");
		buttonReset.setToolTipText("Reset axes");

		buttonZoom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				plotPanel.setActionMode(PlotPanel.ZOOM_MODE);
			}
		});
		buttonCenter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				plotPanel.setActionMode(PlotPanel.TRANSLATION_MODE);
			}
		});
		buttonSaveGraphic.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooseFile();
			}
		});
		buttonReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				plotPanel.resetBase();
			}
		});

		buttonGroup.add(buttonCenter);
		buttonGroup.add(buttonZoom);

		add(buttonCenter);
		add(buttonZoom);
		add(Box.createHorizontalGlue());
		add(buttonSaveGraphic);
		add(buttonReset);

		if (!denySaveSecurity) {
			fileChooser.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveFile();
				}
			});
		} else {
			buttonSaveGraphic.setEnabled(false);
		}
	}

	public PlotPanel getPlotPanel() {
		return plotPanel;
	}

	void chooseFile() {
		fileChooser.showSaveDialog(this);
	}

	void saveFile() {
		java.io.File file = fileChooser.getSelectedFile();
		plotPanel.toGraphicFile(file);
	}
}
