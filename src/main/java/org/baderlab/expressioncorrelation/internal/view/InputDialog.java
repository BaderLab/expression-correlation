package org.baderlab.expressioncorrelation.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Collator;
import java.util.Collection;
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.baderlab.expressioncorrelation.internal.model.CorrelateActionType;
import org.baderlab.expressioncorrelation.internal.model.ExpressionData;
import org.baderlab.expressioncorrelation.internal.view.util.LookAndFeelUtil;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.BasicCollapsiblePanel;

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
 
public class InputDialog extends JDialog {

	private static final long serialVersionUID = 7601013329457450675L;
	
	private final CorrelateActionType type;
	private final Collection<CyTable> tables;

	private BasicCollapsiblePanel advancedPnl;
	private JComboBox<CyTable> tableCmb;
	private JComboBox<CyColumn> geneColumnCmb;
	private JList<CyColumn> conditionColumnLst;
	private JButton okBtn;
	
	private ExpressionData data;
	private boolean cancelled;
	
	public InputDialog(
			final Frame parentFrame,
			final CorrelateActionType type,
			final Collection<CyTable> tables
	) {
		super(parentFrame, "ExpressionCorrelation");
		this.type = type;
		this.tables = tables;
		
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
		
        // OK and Cancel buttons
        final JButton cancelBtn = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelled = true;
				dispose();
			}
		});
        
        final JPanel buttonPnl = LookAndFeelUtil.createOkCancelPanel(getOkBtn(), cancelBtn);
        
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
						)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addComponent(getTableCmb(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
				)
				.addComponent(getAdvancedPnl())
				.addComponent(buttonPnl)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(tableLbl)
						.addComponent(getTableCmb())
				)
				.addComponent(getAdvancedPnl())
				.addComponent(buttonPnl)
		);
        
        setContentPane(contentPane);
        
        LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getOkBtn().getAction(), cancelBtn.getAction());
        getRootPane().setDefaultButton(getOkBtn());
        pack();
	}
	
	public JPanel getAdvancedPnl() {
		if (advancedPnl == null) {
			advancedPnl = new BasicCollapsiblePanel("Advanced Options");
			
			// Labels
			final JLabel geneColumnLbl = new JLabel("Gene Column:", JLabel.RIGHT);
	        final JLabel conditionColumnsLbl = new JLabel("Condition Columns:", JLabel.RIGHT);
	        
	        // Column List ScrollPane
	        final JScrollPane conditionColumnScr = new JScrollPane(getConditionColumnLst());
			
	        final GroupLayout layout = new GroupLayout(advancedPnl.getContentPane());
	        advancedPnl.getContentPane().setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(geneColumnLbl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(conditionColumnsLbl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
							.addComponent(getGeneColumnCmb(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(conditionColumnScr, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
							.addComponent(geneColumnLbl)
							.addComponent(getGeneColumnCmb())
					)
					.addGroup(layout.createParallelGroup(Alignment.LEADING, false)
							.addComponent(conditionColumnsLbl)
							.addComponent(conditionColumnScr)
					)
			);
			
			advancedPnl.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent e) {
					pack();
				}
			});
		}
		
		return advancedPnl;
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
			globalTables.addAll(tables);
			
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
			final String name;
			
			if (type == CorrelateActionType.COND_NET_PREVIEW || type == CorrelateActionType.GENE_NET_PREVIEW)
				name = "Show Histogram";
			else if (type == CorrelateActionType.COND_NET_DEF)
				name = "Create Condition Network";
			else if (type == CorrelateActionType.GENE_NET_DEF)
				name = "Create Gene Network";
			else
				name = "Create Networks";
			
			okBtn = new JButton(new AbstractAction(name) {
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
		
		// Try to select the best column for gene names
		if (model.getSize() > 1) {
			final CyColumn geneColumn = ExpressionData.getDefaultGeneColumn(table);
			
			if (geneColumn != null)
				model.setSelectedItem(geneColumn);
		}
	}
	
	private void updateConditionColumnLst() {
		final CyTable table = (CyTable) getTableCmb().getSelectedItem();
		final Set<CyColumn> allColumns = getSortedColumns(table);
		
		final DefaultListModel<CyColumn> model = (DefaultListModel<CyColumn>) getConditionColumnLst().getModel();
		model.removeAllElements();
		
		for (final CyColumn col : allColumns) {
			if (ExpressionData.isValidConditionColumn(col))
				model.addElement(col);
		}
		
		if (!model.isEmpty())
			getConditionColumnLst().getSelectionModel().addSelectionInterval(0, model.size() - 1);
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
