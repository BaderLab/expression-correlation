package org.jmathplot.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.AccessControlException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JToolBar;

import org.jmathplot.gui.DataPanel;

/**
 * <p>Copyright : BSD License</p>
 * @author Yann RICHET
 * @version 1.0
 */

public class DataToolBar extends JToolBar {

	private static final long serialVersionUID = -1376908308746152474L;
	
	protected JButton buttonPasteToClipboard;
	protected JButton buttonSaveFile;

	private boolean denySaveSecurity;
	private JFileChooser fileChooser;

	private DataPanel dataPanel;

	public DataToolBar(DataPanel dp) {
		dataPanel = dp;

		try {
			fileChooser = new JFileChooser();
		} catch (AccessControlException ace) {
			denySaveSecurity = true;
		}

		buttonPasteToClipboard = new JButton("Copy Data");
		buttonPasteToClipboard.setToolTipText("Copy data to ClipBoard");

		buttonSaveFile = new JButton("Save Data");
		buttonSaveFile.setToolTipText("Save data into ASCII file");

		buttonPasteToClipboard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dataPanel.toClipBoard();
			}
		});
		buttonSaveFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooseFile();
			}
		});

		add(buttonPasteToClipboard, null);
		add(buttonSaveFile, null);

		if (!denySaveSecurity) {
			fileChooser.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveFile();
				}
			});
		} else {
			buttonSaveFile.setEnabled(false);
		}
	}

	void saveFile() {
		java.io.File file = fileChooser.getSelectedFile();
		dataPanel.toASCIIFile(file);
	}

	void chooseFile() {
		fileChooser.showSaveDialog(this);
	}
}
