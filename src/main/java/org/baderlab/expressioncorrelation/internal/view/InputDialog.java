package org.baderlab.expressioncorrelation.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.baderlab.expressioncorrelation.internal.model.ExpressionData;
import org.baderlab.expressioncorrelation.internal.view.util.LookAndFeelUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;

/* * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
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
 * * User: Elena Potylitsine
 * * Date: Jul 7, 2004
 * * Time: 3:35:11 PM
 * * Description: CorrelateHistogramDialog displayes the expression data distribution.
 * * In the histogram window the user can select the low and high cutoffs by manually
 * * typing them into the appropriate "Cutoff" text boxes.
 * * The user can choose to use only one set of cutoffs by deselecting the "low" or "high" checkbox.
 * * The user can select the number or percent of interactions to be displayed
 * * by typing into the "Enter" text box and choosing "Number of Interactions" or "Percent of Interactions".
*/

public class InputDialog extends JDialog {

	private static final long serialVersionUID = 7601013329457450675L;
	private final CyServiceRegistrar serviceRegistrar;

	private JComboBox<CyTable> tableCmb;
	private JComboBox<CyColumn> geneColumnCmb;
	private JList<CyColumn> conditionColumnLst;
	private JButton okBtn;
	
	private ExpressionData data;
	private boolean cancelled;
	
	public InputDialog(final Frame parentFrame, final CyServiceRegistrar serviceRegistrar) {
		super(parentFrame, "ExpressionCorrelation");
		this.serviceRegistrar = serviceRegistrar;
		
		setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        init();
	}
	
	public ExpressionData getExpressionData() {
		return cancelled ? null : data;
	}
	
	private boolean validateData() {
		final CyTable table = (CyTable) getTableCmb().getSelectedItem();
		final CyColumn selectedGeneColumn = (CyColumn) getGeneColumnCmb().getSelectedItem();
		final String geneColumnName = selectedGeneColumn != null ? selectedGeneColumn.getName() : null;
		final List<CyColumn> selectedConditions = getConditionColumnLst().getSelectedValuesList();
		
		if (table != null && geneColumnName != null && !selectedConditions.isEmpty()) {
			final String[] conditionNames = new String[selectedConditions.size()];
			int i = 0;
			
			for (final CyColumn c : selectedConditions)
				conditionNames[i++] = c.getName();
		
			data = new ExpressionData(table, geneColumnName, conditionNames);
			
			return true;
		}
		
		data = null;
		
		return false;
	}
	
	@SuppressWarnings("serial")
	private void init() {
		final JLabel tableLbl = new JLabel("Expression Data Column:", JLabel.RIGHT);
        final JLabel geneColumnLbl = new JLabel("Gene Column:", JLabel.RIGHT);
        final JLabel conditionColumnsLbl = new JLabel("Condition Column:", JLabel.RIGHT);
		
        // OK and Cancel buttons
        final JButton cancelBtn = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelled = true;
				dispose();
			}
		});
        
        final JPanel buttonPnl = LookAndFeelUtil.createOkCancelPanel(getOkBtn(), cancelBtn);
        
        // Column List ScrollPane
        final JScrollPane conditionColumnScr = new JScrollPane(getConditionColumnLst());
        
        // Main panel
		final JPanel contentPane = new JPanel();
        final GroupLayout layout = new GroupLayout(contentPane);
        contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addComponent(tableLbl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(geneColumnLbl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(conditionColumnsLbl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addComponent(getTableCmb(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(getGeneColumnCmb(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(conditionColumnScr, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
				)
				.addComponent(buttonPnl)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(tableLbl)
						.addComponent(getTableCmb())
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(geneColumnLbl)
						.addComponent(getGeneColumnCmb())
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(conditionColumnsLbl)
						.addComponent(conditionColumnScr)
				)
				.addComponent(buttonPnl)
		);
        
        setContentPane(contentPane);
        
        LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getOkBtn().getAction(), cancelBtn.getAction());
        getRootPane().setDefaultButton(getOkBtn());
        pack();
	}
	
	private JComboBox<CyTable> getTableCmb() {
		if (tableCmb == null) {
			final DefaultComboBoxModel<CyTable> model = new DefaultComboBoxModel<>();
			tableCmb = new JComboBox<>(model);
			
			final Collator collator = Collator.getInstance(Locale.getDefault());
			final TreeSet<CyTable> globalTables = new TreeSet<>(new Comparator<CyTable>() {
				@Override
				public int compare(final CyTable t1, final CyTable t2) {
					return collator.compare(t1.getTitle(), t2.getTitle());
				}
			});
			globalTables.addAll(serviceRegistrar.getService(CyTableManager.class).getGlobalTables());
			
			model.removeAllElements();
			
			for (final CyTable tbl : globalTables)
				model.addElement(tbl);
			
			tableCmb.setSelectedItem(null);
			
			tableCmb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateGeneColumnCmb();
					updateConditionColumnLst();
					updateOkBtn();
				}
			});
			
			if (!globalTables.isEmpty())
				tableCmb.setSelectedItem(globalTables.first());
		}
		
		return tableCmb;
	}
	
	private JComboBox<CyColumn> getGeneColumnCmb() {
		if (geneColumnCmb == null) {
			geneColumnCmb = new JComboBox<>(new DefaultComboBoxModel<CyColumn>());
		}
		
		return geneColumnCmb;
	}
	
	private JList<CyColumn> getConditionColumnLst() {
		if (conditionColumnLst == null) {
			conditionColumnLst = new JList<>(new DefaultListModel<CyColumn>());
			conditionColumnLst.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (!e.getValueIsAdjusting())
						updateOkBtn();
				}
			});
		}
		
		return conditionColumnLst;
	}
	
	@SuppressWarnings("serial")
	public JButton getOkBtn() {
		if (okBtn == null) {
			okBtn = new JButton(new AbstractAction("OK") {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (validateData()) {
						cancelled = false;
						dispose();
					}
				}
			});
	        okBtn.setEnabled(false);
		}
		
		return okBtn;
	}
	
	private void updateGeneColumnCmb() {
		final CyTable table = (CyTable) getTableCmb().getSelectedItem();
		final Set<CyColumn> allColumns = getSortedColumns(table);
		
		final DefaultComboBoxModel<CyColumn> model = (DefaultComboBoxModel<CyColumn>) getGeneColumnCmb().getModel();
		model.removeAllElements();
		
		for (final CyColumn col : allColumns) {
			if (col.getType() == String.class)
				model.addElement(col);
		}
	}
	
	private void updateConditionColumnLst() {
		final CyTable table = (CyTable) getTableCmb().getSelectedItem();
		final Set<CyColumn> allColumns = getSortedColumns(table);
		
		final DefaultListModel<CyColumn> model = (DefaultListModel<CyColumn>) getConditionColumnLst().getModel();
		model.removeAllElements();
		
		for (final CyColumn col : allColumns) {
			if (Number.class.isAssignableFrom(col.getType()))
				model.addElement(col);
		}
	}
	
	private void updateOkBtn() {
		getOkBtn().setEnabled(
				getTableCmb().getSelectedItem() != null &&
				getGeneColumnCmb().getSelectedItem() != null &&
				!getConditionColumnLst().getSelectedValuesList().isEmpty()
		);
	}
	
	private static TreeSet<CyColumn> getSortedColumns(final CyTable table) {
		final Collator collator = Collator.getInstance(Locale.getDefault());
		final TreeSet<CyColumn> columns = new TreeSet<>(new Comparator<CyColumn>() {
			@Override
			public int compare(final CyColumn c1, final CyColumn c2) {
				return collator.compare(c1.getName(), c2.getName());
			}
		});
		
		if (table != null)
			columns.addAll(table.getColumns());
		
		return columns;
	}
}
