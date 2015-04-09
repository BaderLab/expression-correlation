package org.baderlab.expressioncorrelation.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.expressioncorrelation.internal.model.CorrelateActionType.COND_NET_DEF;
import static org.baderlab.expressioncorrelation.internal.model.CorrelateActionType.GENE_NET_DEF;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.expressioncorrelation.internal.model.CorrelateActionType;
import org.baderlab.expressioncorrelation.internal.model.CorrelateSimilarityNetwork;
import org.baderlab.expressioncorrelation.internal.task.CorrelateTask;
import org.baderlab.expressioncorrelation.internal.view.util.LookAndFeelUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;
import org.jmathplot.gui.Plot2DPanel;

/*
 * * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center
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
 */

/**
 * CorrelateHistogramDialog displays the expression data distribution.
 * In the histogram window the user can select the low and high cutoffs by manually
 * typing them into the appropriate "Cutoff" text boxes.
 * The user can choose to use only one set of cutoffs by deselecting the "low" or "high" checkbox.
 * The user can select the number or percent of interactions to be displayed
 * by typing into the "Enter" text box and choosing "Number of Interactions" or "Percent of Interactions".
 */
public class CorrelateHistogramDialog extends JDialog {

	private static final long serialVersionUID = 5187881970988735283L;
	
	private CorrelateSimilarityNetwork network;	//Instance of the Similarity network function
	private JFormattedTextField lowCutoffTxt; 	//Formated field for input of the low cutoff value
	private JFormattedTextField highCutoffTxt;	//Formated field for input of the high cutoff value
	private JFormattedTextField interactionsTxt;	//Formated field to hold the number or percent of interactions

    private JCheckBox lowCutoffCkb;				//Allows the user to select to use low cutoffs or not
    private JCheckBox highCutoffCkb;			//Allows the user to select to use high cutoffs or not
    private JComboBox<String> interactionsCmb; //combo box for choosing either interaction number or percent

    boolean isRow = false; 			//set to false to display column histogram, set to true to display row histogram
    double[] cutoffs = new double[2]; // first element is negative cutoff; 2nd element is positive cutoff
    Number valueLow;					//Holds the low value from the text field
    Number valueHigh;					//Holds the high value from the text field
    Number valueInteractions;			//Holds either the percent or the number of interactions value
    String selectedString = "Number of Interactions";  //represents the choice from the combo box

	private final CyServiceRegistrar serviceRegistrar;

    /**
     * One histogram window is generated for either the column or row similarity matrix calculation
     *
     * @param parentFrame - instance of the Cytoscape frame being used to display the histogram
     * @param row         - boolean variable is true if the matrix being worked on is the row matrix
     * @param newNetwork  - instance of the SimilarityMatrix
     */
    @SuppressWarnings("serial")
	public CorrelateHistogramDialog(
			final Frame parentFrame,
			final boolean row,
			final CorrelateSimilarityNetwork newNetwork,
			final CyServiceRegistrar serviceRegistrar
	) {
        super(parentFrame, "Matrix Parameters");
        this.serviceRegistrar = serviceRegistrar;
        
        setModalityType(ModalityType.APPLICATION_MODAL);
        setResizable(false);
        
        isRow = row;
        network = newNetwork;
        cutoffs = network.getCutoffs(isRow);
        
        final JLabel lowLbl = new JLabel("Low Cutoff:", JLabel.RIGHT);
        final JLabel highLbl = new JLabel("High Cutoff:", JLabel.RIGHT);
        final JLabel interactionsLbl = new JLabel("Interactions:", JLabel.RIGHT);

        // Histogram Panel
        final Plot2DPanel plotPnl = new Plot2DPanel(network.getHistogram(isRow), "Similarity  Histogram", "HISTOGRAM");
        plotPnl.setActionMode(Plot2DPanel.NO_ACTION_MODE);
        plotPnl.setToolBarVisible(false);
        
        // OK and Cancel options
        final String okName = isRow ? "Create Gene Network" : "Create Condition Network";
        final JButton okBtn = new JButton(new OKAction(okName));
        
        final JButton cancelBtn = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
        
        final JPanel buttonPnl = LookAndFeelUtil.createOkCancelPanel(okBtn, cancelBtn);
        
        // Main panel for dialog box
        final JPanel contentPane = new JPanel();
        final GroupLayout layout = new GroupLayout(contentPane);
        contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addGroup(layout.createSequentialGroup()
										.addComponent(getLowCutoffCkb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addComponent(lowLbl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								)
								.addGroup(layout.createSequentialGroup()
										.addComponent(getHighCutoffCkb(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addComponent(highLbl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								)
								.addComponent(interactionsLbl, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addComponent(getLowCutoffTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(getHighCutoffTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addGroup(layout.createSequentialGroup()
										.addComponent(getInteractionsTxt(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addComponent(getInteractionsCmb())
								)
						)
				)
				.addComponent(plotPnl)
				.addComponent(buttonPnl)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getLowCutoffCkb())
						.addComponent(lowLbl)
						.addComponent(getLowCutoffTxt())
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(getHighCutoffCkb())
						.addComponent(highLbl)
						.addComponent(getHighCutoffTxt())
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(interactionsLbl)
						.addComponent(getInteractionsTxt())
						.addComponent(getInteractionsCmb())
				)
				.addComponent(plotPnl)
				.addComponent(buttonPnl)
		);
        
        setContentPane(contentPane);
        
        LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), okBtn.getAction(), cancelBtn.getAction());
        getRootPane().setDefaultButton(okBtn);
    }
    
    /**
     * Listens to changes to the low text field window
     * if a change occurs ->changes the interactions field window as well
     * makes sure the value entered is within bounds otherwise sets value to original cutoffs value
     */
    public void lowGetSet() {
        double value;
        valueLow = (Number) getLowCutoffTxt().getValue();

        if (valueLow != null) {
            value = -valueLow.doubleValue();
            
            if ((value < 0) && (value >= -1)) {
                if (isRow)
                    network.setRowNegCutoff(value);
                else
                    network.setColNegCutoff(value);

                getSet();
                cutoffs = network.getCutoffs(isRow);
                getLowCutoffTxt().setText(Double.toString(cutoffs[0]));
            }
        }
    }

    /**
     * Listens to chages to the high text field window
     * if a change occurs ->changes the interactions field window as well
     * makes sure the value entered is within bounds otherwise sets value to original cutoffs value
     */
    public void highGetSet() {
        double value;
        valueHigh = (Number) getHighCutoffTxt().getValue();
        
        if (valueHigh != null) {
            value = valueHigh.doubleValue();
            
            if ((value > 0) && (value <= 1)) {
                if (isRow)
                    network.setRowPosCutoff(value);
                else
                    network.setColPosCutoff(value);

                getSet();
                cutoffs = network.getCutoffs(isRow);
                getHighCutoffTxt().setText(Double.toString(cutoffs[1]));
            }
        }
    }

    /**
     * Gets cutoffs and sets interaction number
     */
    public void getSet() {
        cutoffs = network.getCutoffs(isRow);
        selectedString = (String) getInteractionsCmb().getSelectedItem();
        
        if (selectedString.equals("Number of Interactions")) {
            getInteractionsTxt().setText(Integer.toString(network.getNumberOfInteractions(isRow, cutoffs)));
        } else if (selectedString.equals("Percent of Interactions")) {
            getInteractionsTxt().setText(Double.toString(network.getPercentOfInteractions(isRow, cutoffs)));
        }
    }

    /**
     * Listens to changes to the interactions text field window
     * if a change occurs ->changes the high and low field windows as well
     * makes sure the value entered is within bounds otherwise sets value to original cutoffs value
     */
    public void interactionsGetSet() {
        double doubleValue;
        int intValue;
        valueInteractions = (Number) getInteractionsTxt().getValue();
        selectedString = (String) getInteractionsCmb().getSelectedItem();
        
        if (selectedString.equals("Number of Interactions") && (valueInteractions != null)) {
            intValue = valueInteractions.intValue();
            
            if (intValue >= 0) {
                network.setCutoffsInteractions(isRow, intValue);
                cutoffs = network.getCutoffs(isRow);
                getLowCutoffTxt().setText(Double.toString(cutoffs[0]));
                getHighCutoffTxt().setText(Double.toString(cutoffs[1]));
                getSet();
            }
        } else if (selectedString.equals("Percent of Interactions") && (valueInteractions != null)) {
            doubleValue = valueInteractions.doubleValue();
            
            if (doubleValue < 0 || doubleValue > 1.0) {
                doubleValue = 1.0;
                network.setCutoffsPercent(isRow, doubleValue);
                cutoffs = network.getCutoffs(isRow);
                getInteractionsTxt().setText(Double.toString(network.getPercentOfInteractions(isRow, cutoffs)));
            }

            network.setCutoffsPercent(isRow, doubleValue);
            cutoffs = network.getCutoffs(isRow);
            getLowCutoffTxt().setText(Double.toString(cutoffs[0]));
            getHighCutoffTxt().setText(Double.toString(cutoffs[1]));
        }
    }

    private JCheckBox getLowCutoffCkb() {
		if (lowCutoffCkb == null) {
			lowCutoffCkb = new JCheckBox();
	        lowCutoffCkb.addItemListener(new CorrelateHistogramDialog.LowCheckBoxAction());
	        lowCutoffCkb.setToolTipText(
	        		"<html>If checked, allows you to set the low cutoff value.<br />" +
	                "If not, a low cuttof value will not be used in the percent calculation</html>"
	        );
	        lowCutoffCkb.setSelected(true);
		}
		
		return lowCutoffCkb;
	}
    
    private JCheckBox getHighCutoffCkb() {
		if (highCutoffCkb == null) {
			highCutoffCkb = new JCheckBox();
	        highCutoffCkb.addItemListener(new CorrelateHistogramDialog.HighCheckBoxAction());
	        highCutoffCkb.setToolTipText(
	        		"<html>If checked, allows you to set the high cutoff value.<br />" +
	                "If not, a high cuttof value will not be used in the percent calculation</html>"
	        );
	        highCutoffCkb.setSelected(true);
		}
		
		return highCutoffCkb;
	}
    
    private JFormattedTextField getLowCutoffTxt() {
		if (lowCutoffTxt == null) {
			lowCutoffTxt = new JFormattedTextField(new DecimalFormat("-0.000"));
	        lowCutoffTxt.setColumns(6);
	        lowCutoffTxt.setHorizontalAlignment(JFormattedTextField.RIGHT);
	        lowCutoffTxt.addPropertyChangeListener("value", new CorrelateHistogramDialog.FormattedTextFieldAction());
	        lowCutoffTxt.setToolTipText(
	        		"<html>Look at the Histogram and set the low cutoff for the network that you want displayed<br />" +
	                "or deselect the checkbox if you do not want a low cutoff value</html>"
	        );
	        lowCutoffTxt.setText(Double.toString(cutoffs[0]));
		}
		
		return lowCutoffTxt;
	}
    
    private JFormattedTextField getHighCutoffTxt() {
		if (highCutoffTxt == null) {
			highCutoffTxt = new JFormattedTextField(new DecimalFormat("0.000"));
	        highCutoffTxt.setColumns(6);
	        highCutoffTxt.setHorizontalAlignment(JFormattedTextField.RIGHT);
	        highCutoffTxt.addPropertyChangeListener("value", new CorrelateHistogramDialog.FormattedTextFieldAction());
	        highCutoffTxt.setToolTipText(
	        		"<html>Look at the Histogram and set the high cutoff for the network that you want displayed<br />" +
	                "or deselect the checkbox if you do not want a high cutoff value</html>"
	        );
	        highCutoffTxt.setText(Double.toString(cutoffs[1]));
		}
		
		return highCutoffTxt;
	}
    
    private JFormattedTextField getInteractionsTxt() {
		if (interactionsTxt == null) {
			interactionsTxt = new JFormattedTextField(new DecimalFormat("########.0####"));
	        interactionsTxt.setColumns(6);
	        interactionsTxt.setHorizontalAlignment(JFormattedTextField.RIGHT);
	        interactionsTxt.addPropertyChangeListener("value", new CorrelateHistogramDialog.FormattedTextFieldAction());
	        final String tipSize =
	        		"<html>Select either the number of interactions you want displayed<br />" +
	                "or the percent of interactions you want diplayed</html>";
	        interactionsTxt.setToolTipText(tipSize);
	        interactionsTxt.setText(Integer.toString(network.getNumberOfInteractions(isRow, cutoffs)));
		}
		
		return interactionsTxt;
	}
    
    private JComboBox<String> getInteractionsCmb() {
		if (interactionsCmb == null) {
	        interactionsCmb = new JComboBox<>();
	        interactionsCmb.addItem("Number of Interactions");
	        interactionsCmb.addItem("Percent of Interactions");
	        interactionsCmb.addItemListener(new CorrelateHistogramDialog.PercentNumberComboBoxAction());
	        interactionsCmb.setToolTipText(
	        		"<html>Check this box to create a Network with the Number of Interactions<br />" +
	                "you want displayed and to set the appropriate cutoffs</html>"
	        );
		}
		
		return interactionsCmb;
	}
    
    /**
     * Listens to changes in any of the windows
     * redirects action depending on change
     */
    private class FormattedTextFieldAction implements PropertyChangeListener {
    	
    	@Override
        public void propertyChange(PropertyChangeEvent e) {
            Object source = e.getSource();
            
            if (source == getLowCutoffTxt())
                lowGetSet();
            else if (source == getHighCutoffTxt())
                highGetSet();
            else if (source == getInteractionsTxt())
                interactionsGetSet();
        }
    }

    /**
     * Listens to changes in checkbox state
     * sets whether to use or not the negative cutoffs in creating the network
     */
    private class LowCheckBoxAction implements ItemListener {
        
    	@Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                if (isRow)
                    network.setRowNegUse(false);
                else
                    network.setColNegUse(false);
                
                getSet();
                getLowCutoffTxt().setEnabled(false);
            } else {
                if (isRow)
                    network.setRowNegUse(true);
                else
                    network.setColNegUse(true);
               
                if (interactionsTxt != null)
                    getSet();
                
                getLowCutoffTxt().setEnabled(true);
            }
        }
    }

    /**
     * Listens to changes in checkbox state
     * sets whether to use or not the positive cutoffs in creating the network
     */
    private class HighCheckBoxAction implements ItemListener {
    	
    	@Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                if (isRow) {
                    network.setRowPosUse(false);
                } else {
                    network.setColPosUse(false);
                }
                
                getSet();
                getHighCutoffTxt().setEnabled(false);
            } else {
                if (isRow)
                    network.setRowPosUse(true);
                else
                    network.setColPosUse(true);
                
                if (interactionsTxt != null)
                    getSet();
                
                getHighCutoffTxt().setEnabled(true);
            }
        }
    }

    /**
     * Listens to changes in combobox state
     * converts between percent and number of interactions upon change of selction
     */
    private class PercentNumberComboBoxAction implements ItemListener {
        
    	@Override
        public void itemStateChanged(ItemEvent e) {
            String selectedString = (String) getInteractionsCmb().getSelectedItem();
            
            if (selectedString.equals("Number of Interactions")) {
                getInteractionsTxt().setText(Integer.toString(network.getNumberOfInteractions(isRow, cutoffs)));
            } else if (selectedString.equals("Percent of Interactions")) {
                getInteractionsTxt().setText(Double.toString(network.getPercentOfInteractions(isRow, cutoffs)));
            }
        }
    }

    /**
     * Listens to OK button action.
     * Upon activation retrieves the latest cutoffs saves them in the singleton class
     * and begins the timed an monitored progress of the network creation
     */
    private class OKAction extends AbstractAction {
        
		private static final long serialVersionUID = -2875741773366595679L;
		
        OKAction(final String name) {
            super(name);
        }

		@Override
        public void actionPerformed(ActionEvent e) {
            dispose();
            
            CorrelateActionType type = COND_NET_DEF;
            
            if (isRow) {
                interactionsGetSet();
                network.saveRowCutoffs(); // Saves the row cutoffs in singleton
                type = GENE_NET_DEF;
            } else {
                interactionsGetSet();
                network.saveColCutoffs(); //Saves the column cutoffs in singleton
            }

            cutoffs = network.getCutoffs(isRow);
            
            // Create a Correlate Task
            final Task task = new CorrelateTask(type, network, serviceRegistrar);

            // Execute Task via TaskManager
            final DialogTaskManager taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
            taskMgr.execute(new TaskIterator(task), new TaskObserver() {
				@Override
				public void taskFinished(ObservableTask task) {
				}
				@Override
				public void allFinished(FinishStatus finishStatus) {
					if (finishStatus.getType() == FinishStatus.Type.SUCCEEDED)
						cutoffs = network.getCutoffs(isRow);
				}
			});
        }
    }
}
