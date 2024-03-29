package org.jmathplot.gui;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jmathplot.io.MatrixString;
import org.jmathplot.io.files.ASCIIFile;
import org.jmathplot.util.DoubleArray;

public class MatrixTablePanel extends DataPanel {

	private static final long serialVersionUID = -2250259067278253144L;
	
	private JTable table;
	private TableModel model;
	private double[][] M;
	private boolean viewHeaders = false;
	private String[] headers;

	public MatrixTablePanel(double[][] m) {
		M = m;
		headers = new String[M[0].length];
		setModel();
		toWindow();
	}

	@Override
	public void toClipBoard() {
		try {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(MatrixString.printMatrix(M)), null);
		} catch (IllegalStateException e) {
			JOptionPane.showConfirmDialog(null
				,
				"Copy to clipboard failed : " +
				e.getMessage()
				, "Error"
				, JOptionPane.DEFAULT_OPTION
				, JOptionPane.ERROR_MESSAGE
				);
		}
	}

	@Override
	public String toString() {
		return M.toString();
	}

	@Override
	public void toASCIIFile(File file) {
		try {
			ASCIIFile.write(file,MatrixString.printMatrix(M));
		} catch (NullPointerException e) {
			//System.out.println("File not saved");
		}
	}

	private void setModel() {
		Double[][] array = new Double[M.length][M[0].length];
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				array[i][j] = new Double(M[i][j]);
			}
		}

		model = new DefaultTableModel(array, headers);
	}

	public void setHeaders(String[] h) {
		if (h.length != M[0].length) {
			throw new IllegalArgumentException("Headers of the table must have " +
				M[0].length + " elements.");
		}

		headers = h;
		update();
	}

	@Override
	public void update() {
		setModel();
		super.update();
	}

	public void setMatrix(double[][] m) {
		M = m;

		headers = new String[M[0].length];
		update();
	}

	@Override
	protected void toWindow() {
		table = new JTable(model);

		if (!viewHeaders) {
			table.setTableHeader(null);
		}

		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setCellSelectionEnabled(true);

		scrollPane = new JScrollPane(table);

		scrollPane.setPreferredSize(getSize());
		scrollPane.setSize(getSize());

		add(scrollPane, BorderLayout.CENTER);
	}

	public double[][] getMatrix() {
		return M;
	}

	public static void main(String[] args) {
		MatrixTablePanel mt = new MatrixTablePanel(DoubleArray.random(20, 3));
		FrameView fv = new FrameView(mt);
		mt.setMatrix(DoubleArray.random(40, 4));
	}
}
