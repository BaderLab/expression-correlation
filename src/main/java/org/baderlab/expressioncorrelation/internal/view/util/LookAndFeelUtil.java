package org.baderlab.expressioncorrelation.internal.view.util;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

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

public final class LookAndFeelUtil {

	public static boolean isAquaLAF() {
		return UIManager.getLookAndFeel() != null && "Mac OS X".equals(UIManager.getLookAndFeel().getName());
	}
	
	public static boolean isNimbusLAF() {
		return UIManager.getLookAndFeel() != null && "Nimbus".equals(UIManager.getLookAndFeel().getName());
	}
	
	public static boolean isWinLAF() {
		return UIManager.getLookAndFeel() != null && "Windows".equals(UIManager.getLookAndFeel().getName());
	}
	
	public static Border createPanelBorder() {
		// Try to create Aqua recessed borders on Mac OS
		Border border = isAquaLAF() ? UIManager.getBorder("InsetBorder.aquaVariant") : null;
		
		if (border == null) {
			if (isWinLAF())
				border = new TitledBorder("");
			else
				border = BorderFactory.createTitledBorder("SAMPLE").getBorder();
		}
			
		if (border == null)
			border = BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"));
		
		return border;
	}
	
	public static Border createTitledBorder(final String title) {
		final Border border;
		
		if (title == null || title.trim().isEmpty()) {
			final Border aquaBorder = isAquaLAF() ? UIManager.getBorder("InsetBorder.aquaVariant") : null;
			border = aquaBorder != null ? aquaBorder : BorderFactory.createTitledBorder("SAMPLE").getBorder();
		} else {
			final Border aquaBorder = isAquaLAF() ? UIManager.getBorder("TitledBorder.aquaVariant") : null;
			final TitledBorder tb = aquaBorder != null ?
					BorderFactory.createTitledBorder(aquaBorder, title) : BorderFactory.createTitledBorder(title);
			border = tb;
		}
		
		return border;
	}
	
	public static JPanel createOkCancelPanel(final JButton okBtn, final JButton cancelBtn) {
		return createOkCancelPanel(okBtn, cancelBtn, new JButton[0]);
	}
	
	public static JPanel createOkCancelPanel(final JButton okBtn, final JButton cancelBtn, JButton... otherBtns) {
		final JPanel panel = new JPanel();
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		final SequentialGroup hg = layout.createSequentialGroup();
		final ParallelGroup vg = layout.createParallelGroup(Alignment.CENTER, false);
		
		if (otherBtns != null) {
			for (int i = 0; i < otherBtns.length; i++) {
				final JButton btn = otherBtns[i];
				hg.addComponent(btn);
				vg.addComponent(btn);
			}
		}
		
		hg.addGap(0, 0, Short.MAX_VALUE);
		
		final JButton btn1 = isMac() ? cancelBtn : okBtn;
		final JButton btn2 = isMac() ? okBtn : cancelBtn;
		
		if (btn1 != null) {
			hg.addComponent(btn1);
			vg.addComponent(btn1);
		}
		if (btn2 != null) {
			hg.addComponent(btn2);
			vg.addComponent(btn2);
		}
		
		layout.setHorizontalGroup(hg);
		layout.setVerticalGroup(vg);
		
		return panel;
	}
	
	public static void setDefaultOkCancelKeyStrokes(final JRootPane rootPane, final Action okAction,
			final Action cancelAction) {
		final InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		if (okAction != null) {
			final String OK_ACTION_KEY = "OK_ACTION_KEY";
			final KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
			inputMap.put(enterKey, OK_ACTION_KEY);
			rootPane.getActionMap().put(OK_ACTION_KEY, okAction);
		}
		
		if (cancelAction != null) {
			final String CANCEL_ACTION_KEY = "CANCEL_ACTION_KEY";
			final KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
			inputMap.put(escapeKey, CANCEL_ACTION_KEY);
			rootPane.getActionMap().put(CANCEL_ACTION_KEY, cancelAction);
		}
	}
	
	public static boolean isMac() {
		return System.getProperty("os.name").startsWith("Mac OS X");
	}
	
	private LookAndFeelUtil() {
	}
}
