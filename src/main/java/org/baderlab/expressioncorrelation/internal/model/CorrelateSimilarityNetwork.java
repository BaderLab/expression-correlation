package org.baderlab.expressioncorrelation.internal.model;

import java.util.Collection;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;


/**
 * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Weston Whitaker
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
 * User: weston
 * Date: Jul 7, 2004
 * Time: 4:06:15 PM
 * <br>
 * Methods for converting gene expression data into networks in Cytoscape
 */

public class CorrelateSimilarityNetwork {

    // Variables for the Row  Similarity Matrix (rows usually represent genes)
    private String rowNetName = "Gene Network"; //Default name of the network
    private int rowCurrentStep = 0;               //Used during calc() and histogram()
    private int rowTotalSteps = 0;                //The number rowCurrentStep will reach when the calculations are done
    private boolean rowDone = false;              //Indicates if calc() or histogram() is done
    private double rowPosCutoff = 0.95;          //Default value. Rows with Correlation above this value will be used to
    //      construct the row similarity network if rowUsePos is true
    //      To work properly: 0 <= rowPosCutoff <= 1
    private double rowNegCutoff = -0.95;         //Default value. Rows with Correlation below this value will be used to
    //      construct the row similarity network if rowUseNeg is true
    //      To work properly: -1 <= rowNegCutoff <= 0
    private int[] rowHistogram;                //Similarity matrix histogram values (-1.0 = first bin = 0  to 1.0 = last bin)
    private String[] rowHistogramLabels;       //Labels for the histogram (e.g ["-1.0 to -0.99", ...])
    private boolean rowUsePos = true;           //True if positive interactions are to be used
    private boolean rowUseNeg = true;           //True if negative interactions are to be used
    private int numberOfRows = 0;

    //Variables for the Column Similarity Matrix (columns usually represent conditions)
    private String colNetName = "Cond Network"; //Default name of the network
    private int colCurrentStep = 0;               //Used during calc() and histogram()
    private int colTotalSteps = 0;                //The number rowCurrentStep will reach when the calculations are done
    private boolean colDone = false;              //Indicates if calc() or histogram() is done
    private double colPosCutoff = 0.95;          //Default value. Columns with Correlation above this value will be used to
    //      construct the column similarity network if colUsePos is true
    //      To work properly: 0 <= colPosCutoff <= 1
    private double colNegCutoff = -0.95;         //Default value. Columns with Correlation below this value will be used to
    //      construct the column similarity network if colUseNeg is true
    //      To work properly: -1 <= colNegCutoff <= 0
    private int[] colHistogram;                //Similarity matrix histogram values (-1.0 = first bin = 0  to 1.0 = last bin)
    private String[] colHistogramLabels;       //Labels for the histogram (e.g ["-1.0 to -0.99", ...])
    private boolean colUsePos = true;           //True if positive interactions are to be used
    private boolean colUseNeg = true;           //True if negative interactions are to be used
    private int numberOfCols = 0;

    private boolean cancel;             //This will cancel the current loop in calc() or histogram(). To use call cancel().
    //      The cancel value will automatically be reset to false
    private TaskMonitor taskMonitor;
    
    private final ExpressionData data;
    private final CyServiceRegistrar serviceRegistrar;
	
    /**
     * One instance of CorrelateSimilarityNetwork should be used for one gene network and/or one condition network.
     */
	public CorrelateSimilarityNetwork(final ExpressionData data, final CyServiceRegistrar serviceRegistrar) {
		this.data = data;
		this.serviceRegistrar = serviceRegistrar;
	}

    /**
     * This gives the network the name of the expression data file.
     */
    private void nameNetwork() {
        String fullName = data.getName();
        int start = fullName.lastIndexOf('/');
        String name = fullName.substring(start + 1);
        rowNetName = name + " (genes)";
        colNetName = name + " (conditions)";
    }

    /**
     * <pre>
     * This creates the row (gene) similarity network for all genes above the threshold.
     * <p/>
     * Defaults:
     *      The last gene expression data loaded into Cytoscape is used.
     *      The current positive and negative cutoffs are used (rowNegCutoff rowPosCutoff).
     *      The use of either positive or negative correlations is determined (rowUseNeg  rowUsePos).
     *      A unique tag is appended to the current row network name.
     * <p/>
     * rowCurrentSteps, rowTotalSteps, and rowDone are automatically adjusted.
     *      Used getRowCurrentStep() and rowIsDone() to check the current status of the calculation.
     * <p/>
     * The network is automatically created, but must be manually viewed (using the "Create View" button in the "Edit" menu).
     * </pre>
     *
     * @return Returns the CyNetwork for the row (gene) similarity network.
     */
    public CyNetwork calcRows() {
        rowDone = false;
        
        if (rowNetName.equals("Gene Network"))
            nameNetwork();
        
        return calcRows(rowNetName + ": " + rowNegCutoff + " & " + rowPosCutoff, rowNegCutoff, rowPosCutoff);
    }

    /**
     * <pre>
     * This creates the row (gene) similarity network for all genes above the threshold.
     * <p/>
     * Defaults:
     *      The use of either positive or negative correlations is determined (rowUseNeg  rowUsePos).
     * <p/>
     * rowCurrentSteps, rowTotalSteps, and rowDone are automatically adjusted
     *      Used getRowCurrentStep() and rowIsDone() to check the current status of the calculation.
     * <p/>
     * The network is automatically created, but must be manually viewed (using the "Create View" button in the "Edit" menu).
     * </pre>
     *
     * @param networkName - The name of the row network (e.g. "Genes of" + the data set name ).
     * @param data        - This ExpressionData will be used. It should comply with Cytoscape's ExpressionData format.
     * @param lowCutoff   - Rows with Correlation below this value will be used to construct the row similarity network if rowUseNeg is true.
     * @param highCutoff  - Rows with Correlation above this value will be used to construct the row similarity network if rowUsePos is true.
     * @return
     */
    private CyNetwork calcRows(String networkName, double lowCutoff, double highCutoff) {
        rowDone = false;
        String[] geneNames = data.getGeneNames();
        DoubleMatrix2D inputMatrix = getExpressionMatrix();
        
        return calcRows(networkName, inputMatrix, lowCutoff, highCutoff, geneNames);
    }


    /**
     * <pre>
     * This creates the row (gene) similarity network for all genes above the threshold.
     * <p/>
     * rowCurrentSteps, rowTotalSteps, and rowDone are automatically adjusted.
     *      Used getRowCurrentStep() and rowIsDone() to check the current status of the calculation.
     * <p/>
     * The network is automatically created, but must be manually viewed (using the "Create View" button in the "Edit" menu).
     * </pre>
     *
     * @param networkName - The name of the row network (e.g. "Genes of" + the data set name ).
     * @param inputMatrix - This DoubleMatrix2D will be used to construct the network.
     * @param lowCutoff   - Rows with Correlation below this value will be used to construct the row similarity network if rowUseNeg is true.
     * @param highCutoff  - Rows with Correlation above this value will be used to construct the row similarity network if rowUsePos is true.
     * @return
     */
    private CyNetwork calcRows(String networkName, DoubleMatrix2D inputMatrix, double lowCutoff, double highCutoff, String[] rowNames) {
        rowDone = false;
        
        return calc(true, networkName, inputMatrix, lowCutoff, highCutoff, rowNames);
    }

    /**
     * <pre>
     * This creates the column (condition) similarity network for all genes above the threshold.
     * <p/>
     * Defaults:
     *      The last gene expression data loaded into Cytoscape is used.
     *      The current positive and negative cutoffs are used (colNegCutoff colPosCutoff).
     *      The use of either positie or negative correlations is determined (colUseNeg  colUsePos).
     *      A unique tag is appended to the current column network name.
     * <p/>
     * colCurrentSteps, colTotalSteps, and colDone are automatically adjusted.
     *      Used getColCurrentStep() and colIsDone() to check the current status of the calculation.
     * <p/>
     * The network is automatically created, but must be manually viewed (using the "Create View" button in the "Edit" menu).
     * </pre>
     *
     * @return Returns the CyNetwork for the column (condition) similarity network.
     */
    public CyNetwork calcCols() {
        colDone = false;
        
        if (colNetName.equals("Cond Network"))
            nameNetwork();
        
        return calcCols(colNetName + ": " + colNegCutoff + " & " + colPosCutoff, colNegCutoff, colPosCutoff);
    }

    /**
     * <pre>
     * This creates the column (condition) similarity network for all genes above the threshold.
     * <p/>
     * Defaults:
     *      The use of either positie or negative correlations is determined (colUseNeg  colUsePos).
     * <p/>
     * colCurrentSteps, colTotalSteps, and colDone are automatically adjusted
     *      Used getColCurrentStep() and colIsDone() to check the current status of the calculation.
     * <p/>
     * The network is automatically created, but must be manually viewed (using the "Create View" button in the "Edit" menu).
     * </pre>
     *
     * @param networkName - The name of the col network (e.g. "Conditions of" + the data set name ).
     * @param lowCutoff   - Conditions with Correlation below this value will be used to construct the condition similarity network if colUseNeg is true.
     * @param highCutoff  - Conditions with Correlation above this value will be used to construct the condition similarity network if colUsePos is true.
     * @return Returns the CyNetwork for the column (condition) similarity network.
     */
    private CyNetwork calcCols(String networkName, double lowCutoff, double highCutoff) {
        colDone = false;
        String[] condNames = data.getConditionNames();
        DoubleMatrix2D inputMatrix = getExpressionMatrix();
        
        return calc(false, networkName, inputMatrix, lowCutoff, highCutoff, condNames);
    }

    /**
     * Creates the CyNetwork for all column or row interactions with similarity above the threshold.
     *
     * @param isRowNetwork - true for row network calculation, false for column network calculation
     * @param inputMatrix  - The expression data
     * @param lowCutoff    - To work properly: -1 <= lowCutoff <= 0  (default = -0.9)
     * @param highCutoff   - To work properly: 0 <= highCutoff <= 1 (default = 0.9)
     * @param names        - String of column names with the corresponding index for the expression matrix
     * @return
     */
	private CyNetwork calc(boolean isRowNetwork, String networkName, DoubleMatrix2D inputMatrix, double lowCutoff, double highCutoff, String[] names) {
        // When creating the network, don't automatically create the network view
		final CyNetwork newNetwork = createNetwork(networkName);

        // Does some initial calculations and stores all the calculation data
        final InitiationData data = new InitiationData(isRowNetwork, inputMatrix, true);

        // Checks to see that cutoffs make sense
        double[] cutoffs = cutoffCheck(isRowNetwork, lowCutoff, highCutoff);
        lowCutoff = cutoffs[0];
        highCutoff = cutoffs[1];

        // A calculation needed for the command line/text progress bar
        int mod = data.columns % 10;
        if (data.columns <= 10) mod = 0;

        // The loop below is the main step. Here the Pearson Correlation Coefficient is calculated
        //  note: only the top half of the rectangle is calculated since the matrix is symetric
        // The Similarity Matrix is never stored. Instead, at each point in the Similarity Matrix,
        //  the correlaion is calculated, and if its above the threshold then an edge is created
        //  connecting the nodes involved. The correlation value is then erased.
        //  Information about values below the threshold is lost.
        // This loop is set up to calculate the column correlations, so for the row
        //  network the matrix has been transposed
        if (taskMonitor != null) {
            String type = "condition";
            
            if (isRowNetwork)
                type = "gene";
            
//            taskMonitor.setPercentCompleted(-1);
//            taskMonitor.setStatus("Constructing " + type + " correlation network...");
        }

        // Goes through each column
        for (int i = 0; i < data.columns; i++) {
            // Calculates the correlations for a single column
            core(newNetwork, data, i, lowCutoff, highCutoff, names, false);
            
            if (cancel)
                return newNetwork;

//            if ((i * 10) % (data.columns - mod) == 0) {
//                if (taskMonitor != null)
//                    taskMonitor.setPercentCompleted((int)((double) (i * 100) / data.columns));
//            }
        }

//        if (taskMonitor != null)
//        {
//            taskMonitor.setPercentCompleted(100);
//            taskMonitor.setStatus("Finished constructing network");
//        }

        if (isRowNetwork)
            rowDone = true;
        else
            colDone = true;
        
        cancel = false;
        
        serviceRegistrar.getService(CyNetworkManager.class).addNetwork(newNetwork);
        maybeCreateNetworkView(newNetwork);

        return newNetwork;
    }

    /**
     * Creates the two nodes if they don't exist, and creates an edge between them
     *
     * @param network    - network to create edge
     * @param i          - index of the first node
     * @param j          - index of the second node
     * @param names      - array of names of the nodes
     * @param corr       - correlation value (edge strength)
     */
    public void createEdge(final CyNetwork network, int i, int j, String[] names, double corr) {
        final CyNode source = getNode(network, names[i]);
        final CyNode target = getNode(network, names[j]);
        final CyEdge edge = network.addEdge(source, target, true);
        
        final String VALUE_COL_NAME = "Strength";
		
		if (network.getDefaultEdgeTable().getColumn(VALUE_COL_NAME) == null)
			network.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS).createColumn(VALUE_COL_NAME, Double.class, false);
        
        final String edgeName = names[i] + "_interaction_" + names[j];
        final String interaction = corr > 0 ? "pos_interaction" : "neg_interaction";

        final CyRow row = network.getRow(edge);
		row.set(CyNetwork.NAME, edgeName);
		row.set(CyRootNetwork.SHARED_NAME, edgeName);
		row.set(CyEdge.INTERACTION, interaction);
		row.set(CyEdge.INTERACTION, interaction);
		row.set(VALUE_COL_NAME, corr);
    }
    
    /**
     * Get a node by name or create one if it does not exist.
     * @param network
     * @param name The node name
     * @return An existing or a new node
     */
    private CyNode getNode(final CyNetwork network, final String name) {
    	CyNode node = null;
    	final Collection<CyRow> matchingRows = network.getDefaultNodeTable().getMatchingRows(CyNetwork.NAME, name);
    	
    	if (matchingRows != null && !matchingRows.isEmpty()) {
    		final CyRow row = matchingRows.iterator().next();
    		final Long suid = row.get(CyIdentifiable.SUID, Long.class);
    		
    		if (suid != null)
    			node = network.getNode(suid);
    	}
    	
    	if (node == null) {
    		node = network.addNode();
    		final CyRow row = network.getRow(node);
    		row.set(CyNetwork.NAME, name);
    		row.set(CyRootNetwork.SHARED_NAME, name);
    	}
    	
    	return node;
    }
    
    /**
     * This class does some initial calculations and stores all the calculation data.
     * Once an instance of InitiationData is created, the necessary calculations will be automatically done
     * To use, create an instant of InitiationData, and pass that instance into core()
     */
    private class InitiationData {
    	
        //Variables stored by this class
        public DoubleMatrix2D inputMatrix; //Expression data
        public boolean isRowNetwork;       //true for row network calculation, false for column network calculation
        public int columns;                //Number fo colmns
        public int rows;                   //Number of rows
        public double[] sums;              //Column sums
        public DoubleMatrix1D[] cols;      //Different form of expression data
        public DenseDoubleMatrix1D stdDev; //Standard Deviation
        public boolean usePos = true;        //true if positive cutoffs are to be considered
        public boolean useNeg = true;        //true if positive cutoffs are to be considered
        boolean fullNetwork;               //true if the full network is being calculated, false if only for one gene or condition
        TaskMonitor taskMonitor;

        /**
         * Creating an instance of InitiationData will automatically do all the initial calculations
         *
         * @param isRowNetwork - true for row network calculation, false for column network calculation
         * @param inputMatrix
         * @param fullNetwork  -
         */
        InitiationData(boolean isRowNetwork, DoubleMatrix2D inputMatrix, boolean fullNetwork) {
            this.inputMatrix = inputMatrix;
            this.isRowNetwork = isRowNetwork;
            this.fullNetwork = fullNetwork;
            tempSetup(); //Does initial calculations
        }

        /**
         * Does the initial calculations and deals with the relative global variables
         */
        private void tempSetup() {
            //This sets up the done/step variables and inverts the row matrix
            if (isRowNetwork) {
                rowDone = false;
                rowCurrentStep = 0;
                usePos = rowUsePos;
                useNeg = rowUseNeg;
                Algebra A = new Algebra();
                inputMatrix = A.transpose(inputMatrix);
                
                if (fullNetwork)
                    rowTotalSteps = calcTimeFull(inputMatrix);
                else
                    rowTotalSteps = calcTimeSingle(inputMatrix);
            } else {
                colDone = false;
                colCurrentStep = 0;
                usePos = colUsePos;
                useNeg = colUseNeg;
                
                if (fullNetwork)
                    colTotalSteps = calcTimeFull(inputMatrix);
                else
                    colTotalSteps = calcTimeSingle(inputMatrix);
            }

            //Converts the data into a more accessable form
            rows = inputMatrix.rows();
            columns = inputMatrix.columns();
            sums = new double[columns];
            cols = new DoubleMatrix1D[columns];
            
            for (int i = 0; i < columns; i++) {
                if (isRowNetwork)
                    rowCurrentStep++;
                else
                    colCurrentStep++;
                
                cols[i] = inputMatrix.viewColumn(i);
                sums[i] = cols[i].zSum();
                
                if (cancel)
                    return;
            }

            //Calculates the standard deviation for each column
            stdDev = new DenseDoubleMatrix1D(columns);
            
            for (int i = 0; i < columns; i++) {
                if (isRowNetwork)
                    rowCurrentStep++;
                else
                    colCurrentStep++;
                
                double sumOfProducts = cols[i].zDotProduct(cols[i]);
                stdDev.set(i, Math.sqrt((sumOfProducts - sums[i] * sums[i] / rows) / rows));
                
                if (cancel)
                    return;
            }
        }
    }

    /**
     * Returns cutoffs after checking to see if they make sense
     *
     * @param isRowNetwork - true for row network calculation, false for column network calculation
     * @param lowCutoff    - To work properly: -1 <= lowCutoff <= 0  (default = -0.9)
     * @param highCutoff   - To work properly: 0 <= highCutoff <= 1 (default = 0.9)
     * @return cutoffs[] = [lowCutoff highCutoff]
     */
    private double[] cutoffCheck(boolean isRowNetwork, double lowCutoff, double highCutoff) {
        //Checks to see if valid cutoff values are given
        if (Math.abs(lowCutoff) > 1 | Math.abs(highCutoff) > 1) {
            if (isRowNetwork) {
                lowCutoff = rowNegCutoff;
                highCutoff = rowPosCutoff;
            } else {
                lowCutoff = colNegCutoff;
                highCutoff = colPosCutoff;
            }
// TODO            
//            String message = "Unrealistic lowCutoff or highCutoff values. " + '\n' + "Using default values: lowCutoff=" + lowCutoff + ", highCutoff=" + highCutoff;
//            JOptionPane.showMessageDialog(Cytoscape.getDesktop(), message);
        }
        
        if (lowCutoff > 0)
            lowCutoff = -lowCutoff;
        
        double[] result = new double[2];
        result[0] = lowCutoff;
        result[1] = highCutoff;
        
        return result;
    }

    /**
     * This calculates the correlation for a single column and creates the necessary nodes and edges
     *
     * @param newNetwork - the network where the nodes/egdes should be created
     * @param data       - this InitiationData should contain much of the necessary information
     * @param i          - the column for which the correlation should be calculated
     * @param lowCutoff  - To work properly: -1 <= lowCutoff <= 0  (default = -0.9)
     * @param highCutoff - To work properly: 0 <= highCutoff <= 1 (default = 0.9)
     * @param names      - String of column names with the corresponding index for the expression matrix
     * @param fullColumn - if false, only the top triangle of the correlation matrix will be calculated
     */
    private void core(CyNetwork newNetwork, InitiationData data, int i, double lowCutoff, double highCutoff, String[] names, boolean fullColumn) {
        // Calculates the entire column
        int stop = i;
        if (fullColumn) stop = data.columns;

        for (int j = 0; j < stop; j++) {
            if (data.isRowNetwork)
                rowCurrentStep++;
            else
                colCurrentStep++;

            double corr = calcPearsonCorr(data, i, j);

            if (corr < -1.0)
                continue;

            // Creates nodes and edges if its above the positive cutoff
            if (corr > highCutoff && i != j && data.usePos)
                createEdge(newNetwork, i, j, names, corr);

            // Creates nodes and edges if its below the negative cutoff
            if (corr < lowCutoff && data.useNeg)
                createEdge(newNetwork, i, j, names, corr);
            
            if (cancel)
                return;
        }
    }

    /**
     * Returns the number of steps to complete the entire column network
     *
     * @param inputMatrix
     * @return To calculate rows, input the transpose matrix
     */
    private int calcTimeFull(DoubleMatrix2D inputMatrix) {
        double columns = (double) inputMatrix.columns();
        double steps = (columns * columns) / 2 + 5 * columns / 2;
        
        return (int) steps;
    }

    /**
     * Returns the number of steps  to complete the column interactions for one column
     *
     * @param inputMatrix
     * @return total number of steps needed to complete calc() or histogram() for a single column
     *         To calculate rows, input the transpose matrix
     */
    private int calcTimeSingle(DoubleMatrix2D inputMatrix) {
        int columns = inputMatrix.columns();
        int steps = 3 * columns;
        
        return steps;
    }

    /**
     * Calclates the number of steps to complete the row network, and set rowTotalSteps to this.
     *
     * @return total number of steps needed to complete calc() or histogram() for a full network
     */
    public int getRowTotalSteps() {
        if (rowTotalSteps == 0) {
            Algebra A = new Algebra();
            DoubleMatrix2D matrix = A.transpose(getExpressionMatrix());
            rowTotalSteps = calcTimeFull(matrix);
        }
        
        return rowTotalSteps;
    }

    /**
     * Calculates the row histogram for the current Expression data<br>
     * The histogram is required for the getCutoffs functions
     */
    public void rowHistogram() {
        rowHistogram(getExpressionMatrix());
    }

    /**
     * Calculates the row histogram for the given Expression data<br>
     * The histogram is required for the getCutoffs functions
     */
    private void rowHistogram(DoubleMatrix2D inputMatrix) {
        histogram(true, inputMatrix);
    }

    /**
     * Calculates the column histogram for the current Expression data<br>
     * The histogram is required for the getCutoffs functions
     */
    public void colHistogram() {
        colHistogram(getExpressionMatrix());
    }

    /**
     * Calculates the column histogram for the given Expression data<br>
     * The histogram is required for the getCutoffs functions<br>
     * The top half of the entire similarity matrix is calculated here, but
     * only the histogram is stored
     */
    private void colHistogram(DoubleMatrix2D inputMatrix) {
        histogram(false, inputMatrix);
    }

    /**
     * Calculates the Pearson Correlation
     * r = Sxx/SQRT(Sxx x Syy)
     *
     * @param data - The gene expression data
     * @param i, j - Calculate the correlation between data vectors data.cols[i] and data.cols[j]
     * @return The Pearson correlation value between -1 and 1.  If the correlation calculation contains an error
     * (i.e. divide by zero), return -2.0
     */
    private double calcPearsonCorr(InitiationData data,int i, int j) {
        double sumOfProducts = data.cols[i].zDotProduct(data.cols[j]);
        double cov = (sumOfProducts - data.sums[i] * data.sums[j] / data.rows) / data.rows;
        double corr = cov / (data.stdDev.get(i) * data.stdDev.get(j));

        if (data.stdDev.get(i) == 0 || data.stdDev.get(j) == 0 )
            return -2.0;
        else
            return corr;
    }
    
    /**
     * This calculates either the row or column histogram (a class double array) which is used by other
     * methods to determine the correct cutoffs given a desired number of interactions or vice-versa
     * (e.g. getNumberOfInteractions(boolean isRowNetwork, double[] cutoffs)).
     *
     * @param isRowNetwork - true for row network calculation, false for column network calculation
     * @param inputMatrix  - The expression data
     */
    private void histogram(boolean isRowNetwork, DoubleMatrix2D inputMatrix) {
        InitiationData data = new InitiationData(isRowNetwork, inputMatrix, true);

        int bins = 2000;      //Adjust this number to change the histogram accuracy
        //  bins size = 2/bins (e.g. 2000 bins -> bin size = 0.001)
        int[] histo = new int[bins];

        //A calculation needed for the command line/text progress bar
        int mod = data.columns % 10;
        if (data.columns <= 10) mod = 0;

//        if (taskMonitor != null) {
//            String type = "condition";
//            
//            if (isRowNetwork)
//                type = "gene";
//
//            taskMonitor.setPercentCompleted(-1);
//            taskMonitor.setStatus("Constructing  " + type + " correlation histogram...");
//        }

        for (int i = 0; i < data.columns; i++) {
            for (int j = 0; j < i; j++) {
                if (isRowNetwork)
                    rowCurrentStep++;
                else
                    colCurrentStep++;

                double corr = calcPearsonCorr(data,i,j);
                //double corr = sr.corr(i,j);

                if (corr < -1.0)
                    continue;

                if (i != j) {
                    int binNumber = (int) ((corr + 1) * (((double) bins - 1) / 2));
                    histo[binNumber]++;
                }
                
                if (cancel)
                    return;
            }
//            if ((i * 10) % (data.columns - mod) == 0) {
//                if (taskMonitor != null)
//                    taskMonitor.setPercentCompleted((int)((double) (i * 100) / data.columns));
//            }
        }

//        if (taskMonitor != null) {
//            taskMonitor.setPercentCompleted(100);
//            taskMonitor.setStatus("Finished constructing histogram");
//        }
        String[] labels = new String[bins];

        for (int i = 0; i < bins; i++) {
            labels[i] = "" + format(-1 + ((double) i) / (((double) bins) / 2));
        }

        if (isRowNetwork) {
            rowHistogram = histo;
            rowHistogramLabels = labels;
            rowDone = true;
        } else {
            colHistogram = histo;
            colHistogramLabels = labels;
            colDone = true;
        }
    }

    /**
     * Gives the current histogram values (assumes histogram() has been called)
     *
     * @return rowHistogram
     */
    private int[] getRowHistogram() {
        return rowHistogram;
    }

    /**
     * Gives the current histogram values (assumes histogram() has been called)
     *
     * @return colHistogram
     */
    private int[] getColHistogram() {
        return colHistogram;
    }

    /**
     * Gives the current cutoffs
     *
     * @return [___NegCutoff, ___PosCutoff]
     */
    public double[] getCutoffs(boolean isRowNetwork) {
        double[] cutoff = new double[2];
        
        if (isRowNetwork) {
            cutoff[0] = format(rowNegCutoff);
            cutoff[1] = format(rowPosCutoff);
        } else {
            cutoff[0] = format(colNegCutoff);
            cutoff[1] = format(colPosCutoff);
        }
        
        return cutoff;
    }

    /**
     * Requires that the histogram() function has been called
     * <br>
     * Calculates the highest magnitude cutoffs that will result in a network consisting
     * of the strongest given number of correlations.
     *
     * @param isRowNetwork   - true calculates based on the row histogram, false calculates based on the column histogram
     * @param negative       - true if negative correlation values are to be used
     * @param positive       - true if positive correlation values are to be used
     * @param percentOfEdges - number of edges in the new network divided by all possible edges
     * @return returns the cutoffs needed to produce a network with the desired number of edges
     */
    private double[] getCutoffs(boolean isRowNetwork, boolean negative, boolean positive, double percentOfEdges) {
        int total = 0;
        
        if (isRowNetwork) {
            for (int i = 0; i < rowHistogram.length; i++)
                total += rowHistogram[i];
        } else {
            for (int i = 0; i < colHistogram.length; i++)
                total += colHistogram[i];
        }
        
        int number = (int) (((double) percentOfEdges) * ((double) total));
        
        return getCutoffs(isRowNetwork, number, negative, positive);
    }

    /**
     * Requires that the histogram() function has been called
     * <br>
     * Calculates the highest magnitude cutoffs that will result in a network consisting
     * of the strongest given number of correlations.
     *
     * @param isRowNetwork  - true calculates based on the row histogram, false calculates based on the column histogram
     * @param negative      - true if negative correlation values are to be used
     * @param positive      - true if positive correlation values are to be used
     * @param numberOfEdges - number of edges in the new network
     * @return returns the cutoffs needed to produce a network with the desired number of edges
     */
    private double[] getCutoffs(boolean isRowNetwork, int numberOfEdges, boolean negative, boolean positive) {
        double[] cutoff = new double[2];
        int position = -1;
        int count = 0;
        final int[] histogram = isRowNetwork ? rowHistogram : colHistogram;
        final double[] oldCutoffs = getCutoffs(isRowNetwork);
        
        while (count < numberOfEdges && position < (histogram.length / 2 - 1)) {
            position++;
            
            if (negative)
                count += histogram[position];
            if (positive)
                count += histogram[histogram.length - position - 1];
        }
        
        if (negative)
            cutoff[0] = (-1.0 + ((double) position) * 2.0 / ((double) histogram.length));
        else
            cutoff[0] = oldCutoffs[0];
        
        if (positive)
            cutoff[1] = (1.0 - ((double) position) * 2.0 / ((double) histogram.length));
        else
            cutoff[1] = oldCutoffs[1];
        
        return cutoff;
    }

    public void setRowNegCutoff(double negative) {
        rowNegCutoff = format(negative);
    }

    public void setColNegCutoff(double negative) {
        colNegCutoff = format(negative);
    }

    public void setRowPosCutoff(double positive) {
        rowPosCutoff = format(positive);
    }

    public void setColPosCutoff(double positive) {
        colPosCutoff = format(positive);
    }

    private void setRowCutoffs(double[] cutoffs) {
        rowNegCutoff = format(cutoffs[0]);
        rowPosCutoff = format(cutoffs[1]);
    }

    private void setColCutoffs(double[] cutoffs) {
        colNegCutoff = format(cutoffs[0]);
        colPosCutoff = format(cutoffs[1]);
    }

    public void setColPosUse(boolean usePos) {
        colUsePos = usePos;
    }

    public void setColNegUse(boolean useNeg) {
        colUseNeg = useNeg;
    }

    public void setRowPosUse(boolean usePos) {
        rowUsePos = usePos;
    }

    public void setRowNegUse(boolean useNeg) {
        rowUseNeg = useNeg;
    }

    /**
     * Requires that the histogram() function has been called
     * <br>
     * Calculates the highest magnitude cutoffs that will result in a network consisting
     * of the strongest given number of correlations.
     * <br>
     * Then sets the network's cutoff values to this calculated value
     *
     * @param isRowNetwork  - true calculates based on the row histogram, false calculates based on the column histogram
     * @param numberOfEdges - number of edges in the new network
     */
    public void setCutoffsInteractions(boolean isRowNetwork, int numberOfEdges) {
        boolean usePos = true;
        boolean useNeg = true;
        
        if (isRowNetwork) {
            usePos = rowUsePos;
            useNeg = rowUseNeg;
        } else {
            usePos = colUsePos;
            useNeg = colUseNeg;
        }
        
        double[] cutoffs = getCutoffs(isRowNetwork, numberOfEdges, useNeg, usePos);
        
        if (isRowNetwork) {
            rowNegCutoff = format(cutoffs[0]);
            rowPosCutoff = format(cutoffs[1]);
        } else {
            colNegCutoff = format(cutoffs[0]);
            colPosCutoff = format(cutoffs[1]);
        }
    }

    /**
     * Requires that the histogram() function has been called
     * <br>
     * Calculates the highest magnitude cutoffs that will result in a network consisting
     * of the strongest given number of correlations.
     * <br>
     * Then sets the network's cutoff values to this calculated value
     *
     * @param isRowNetwork   - true calculates based on the row histogram, false calculates based on the column histogram
     * @param percentOfEdges - number of edges in the new network by all possible edges
     */
    public void setCutoffsPercent(boolean isRowNetwork, double percentOfEdges) {
        boolean usePos = true;
        boolean useNeg = true;
        
        if (isRowNetwork) {
            usePos = rowUsePos;
            useNeg = rowUseNeg;
        } else {
            usePos = colUsePos;
            useNeg = colUseNeg;
        }
        
        double[] cutoffs = getCutoffs(isRowNetwork, useNeg, usePos, percentOfEdges);
        
        if (isRowNetwork) {
            rowNegCutoff = format(cutoffs[0]);
            rowPosCutoff = format(cutoffs[1]);
        } else {
            colNegCutoff = format(cutoffs[0]);
            colPosCutoff = format(cutoffs[1]);
        }
    }

    /**
     * Returns the number of edges that would be in a network with the given cutoff values
     *
     * @param isRowNetwork- true calculates based on the row histogram, false calculates based on the column histogram
     * @param cutoffs       - [negativeCutoffValue, positiveCutoffValue]
     * @return number of edges that will be in a network with the given cutoffs
     */
    public int getNumberOfInteractions(boolean isRowNetwork, double[] cutoffs) {
		if (cutoffs == null || cutoffs.length < 2)
			return 0;
        
        final int[] histogram = isRowNetwork ? rowHistogram : colHistogram;
        int count = 0;
        
        boolean[] uses = getUses(isRowNetwork);
        
        if (uses[0]) {
            for (int i = 0; (-1.0 + ((double) i * 2) / histogram.length) < cutoffs[0]; i++)
                count += histogram[i];
        }
        if (uses[1]) {
            for (int j = 0; (1.0 - ((double) j * 2) / histogram.length) >= cutoffs[1]; j++)
                count += histogram[histogram.length - j - 1];
        }
        
        return count;
    }

    /**
     * Returns the percent of the total possible of edges that would be in a network with the given cutoff values
     *
     * @param isRowNetwork- true calculates based on the row histogram, false calculates based on the column histogram
     * @param cutoffs       - [negativeCutoffValue, positiveCutoffValue]
     * @return percent of edges that will be in a network with the given cutoffs
     */
    public double getPercentOfInteractions(boolean isRowNetwork, double[] cutoffs) {
    	final int[] histogram = isRowNetwork ? rowHistogram : colHistogram;
        
        int count = getNumberOfInteractions(isRowNetwork, cutoffs);
        int total = 0;
        
        for (int i = 0; i < histogram.length; i++) {
            total += histogram[i];
        }

        return format(((double) count) / ((double) total));
    }

    public int getNumberOfRows() {
        if (numberOfRows < 1)
            numberOfRows = data.getNumberOfGenes();
        
        return numberOfRows;
    }

    public int getNumberOfCols() {
        if (numberOfCols < 1)
            numberOfCols = data.getNumberOfConditions();
        
        return numberOfCols;
    }

    public void loadColCutoffs() {
        CorrelateCutoffStorage lastCutoffs = CorrelateCutoffStorage.getInstance();
        setColCutoffs(lastCutoffs.getCutoffs(false));
    }

    public void loadRowCutoffs() {
        CorrelateCutoffStorage lastCutoffs = CorrelateCutoffStorage.getInstance();
        setRowCutoffs(lastCutoffs.getCutoffs(true));
    }

    public void saveColCutoffs() {
        CorrelateCutoffStorage lastCutoffs = CorrelateCutoffStorage.getInstance();
        lastCutoffs.setCutoffs(false, getCutoffs(false));
    }

    public void saveRowCutoffs() {
        CorrelateCutoffStorage lastCutoffs = CorrelateCutoffStorage.getInstance();
        lastCutoffs.setCutoffs(true, getCutoffs(true));
    }

    /**
     * Give data in a format recognizable by the jmathplot
     *
     * @param isRowNetwork
     * @return histogram in jmathplot format
     */
    public double[][] getHistogram(boolean isRowNetwork) {
        final int[] histo = isRowNetwork ? getRowHistogram() : getColHistogram();
        final double[][] histogram = new double[histo.length][3];
        
        for (int i = 0; i < histo.length; i++) {
            histogram[i][0] = -1 + ((double) i) / (((double) histo.length) / 2);
            histogram[i][1] = histo[i];
            histogram[i][2] = 1000 / histo.length; //bar width
        }
        
        return histogram;
    }

    /**
     * This terminates the current calc() or histogram() loop. This value is automatically reset.
     * If called when not in a loop, it will cancel the next loop, but will then be reset back to false.
     */
    public void cancel() {
        cancel = true;
    }

    /**
     * Indicates whether or not the task was cancelled
     * @return true if cancel is true, otherwise false
     */
    public boolean cancelled() {
        return cancel;
    }
    
    private boolean[] getUses(boolean isRowNetwork) {
        boolean[] uses = new boolean[2];
        
        if (isRowNetwork) {
            uses[0] = rowUseNeg;
            uses[1] = rowUsePos;
        } else {
            uses[0] = colUseNeg;
            uses[1] = colUsePos;
        }
        
        return uses;
    }
    
    /**
     * Converts the given Expression Data into a DoubleMatrix2D
     */
    private DoubleMatrix2D getExpressionMatrix() {
        int geneNumber = data.getNumberOfGenes();
        int condNumber = data.getNumberOfConditions();
        
        final DenseDoubleMatrix2D expressionMatrix = new DenseDoubleMatrix2D(geneNumber, condNumber);
        final double[][] allValues = data.getAllValues();
        
        for (int i = 0; i < geneNumber; i++) {
            for (int j = 0; j < condNumber; j++) {
                double value = allValues[i][j];
                expressionMatrix.set(i, j, value);
            }
        }
        
        numberOfRows = data.getNumberOfGenes();
        numberOfCols = data.getNumberOfConditions();
        
        return expressionMatrix;
    }
    
    private double format(double input) {
        int new_int = (int) (input * 1000);
        double new_double = (double) new_int / 1000;
        
        return new_double;
    }
    
    private CyNetwork createNetwork(final String name) {
    	final CyNetworkFactory netFactory = serviceRegistrar.getService(CyNetworkFactory.class);
    	final CyNetwork net = netFactory.createNetwork();
    	
    	if (name != null)
    		net.getRow(net).set(CyNetwork.NAME, name);
    	
    	return net;
    }
    
    /**
     * Create network view if number of nodes is below threshold.
     */
    @SuppressWarnings("unchecked")
	private void maybeCreateNetworkView(final CyNetwork network) {
		final CyProperty<Properties> cyProperty = 
				serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		final Properties props = cyProperty.getProperties();
		int viewThreshold = Integer.MAX_VALUE;
		
		try {
			viewThreshold = Integer.parseInt(props.getProperty("viewThreshold"));
		} catch (Exception e) {
		}
        
        if (network.getNodeCount() <= viewThreshold) {
        	final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
			final CyNetworkView netView =
					appMgr.getDefaultNetworkViewRenderer().getNetworkViewFactory().createNetworkView(network);
			serviceRegistrar.getService(CyNetworkViewManager.class).addNetworkView(netView);
			// TODO Apply preferred layout
        }
	}
}
