package org.jmathplot.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jmathplot.gui.components.DatasFrame;
import org.jmathplot.gui.components.PlotToolBar;
import org.jmathplot.gui.components.SetScalesFrame;
import org.jmathplot.gui.plotObjects.Base;
import org.jmathplot.gui.plotObjects.BaseScalesDependant;
import org.jmathplot.gui.plotObjects.Coord;
import org.jmathplot.gui.plotObjects.Grid;
import org.jmathplot.gui.plotObjects.Plotable;
import org.jmathplot.gui.plotObjects.RelativeCoord;
import org.jmathplot.gui.plots.Plot;
import org.jmathplot.util.DoubleArray;

public abstract class PlotPanel extends JPanel
								implements MouseListener, MouseMotionListener, ComponentListener, BaseScalesDependant {

	private static final long serialVersionUID = -208368220920306570L;
	
	public final static int NO_ACTION_MODE = -1;
	public final static int ZOOM_MODE = 0;
	public final static int TRANSLATION_MODE = 1;

	public final static int LINEAR = Base.LINEAR;
	public final static int LOG = Base.LOG;

	public final static String SCATTER = "SCATTER";
	public final static String LINE = "LINE";
	public final static String BAR = "BAR";
	public final static String HISTOGRAM = "HISTOGRAM";
	public final static String BOX = "BOX";
	public final static String STAIRCASE = "STAIRCASE";

	public static Color[] colorList = {Color.blue, Color.red, Color.green, Color.yellow, Color.orange, Color.pink,
					  Color.cyan, Color.magenta};

	//anti-aliasing constant
	private final static RenderingHints AALIAS = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);

	public static int[] panelSize = new int[] {400, 400};

	protected Base base;
	protected Grid grid;
	protected Vector plots;
	protected Vector objects;

	private int actionMode = ZOOM_MODE;

	protected int[] mouseCurent = new int[2];
	protected int[] mouseClick = new int[2];

	protected PlotToolBar toolBar;

	/////////////////////////////////////////////
	//////// Constructor & inits ////////////////
	/////////////////////////////////////////////

	/** create local toolbar */
	public PlotPanel() {
		setLayout(new BorderLayout());
		initToolBar();
		initPanel();
		initBasenGrid();
	}

	public PlotPanel(double[] min, double[] max) {
		setLayout(new BorderLayout());
		initToolBar();
		initPanel();
		initBasenGrid(min, max);
	}

	public PlotPanel(double[] min, double[] max, int[] axesScales, String[] axesLabels) {
		setLayout(new BorderLayout());
		initToolBar();
		initPanel();
		initBasenGrid(min, max, axesScales, axesLabels);
	}

	private void initToolBar() {
		toolBar = new PlotToolBar(this);
		toolBar.setFloatable(false);
		add(toolBar, BorderLayout.NORTH);
	}

	private void initPanel() {
		objects = new Vector();
		plots = new Vector();

		setSize(panelSize[0], panelSize[1]);
		setPreferredSize(new Dimension(panelSize[0], panelSize[1]));
		setBackground(Color.white);

		addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/////////////////////////////////////////////
	//////// set actions ////////////////////////
	/////////////////////////////////////////////

	public int getActionMode() {
		return actionMode;
	}
	
	public void setActionMode(final int am) {
		actionMode = am;
		
		toolBar.buttonGroup.setSelected(toolBar.buttonCenter.getModel(), am == PlotPanel.TRANSLATION_MODE);
		toolBar.buttonGroup.setSelected(toolBar.buttonZoom.getModel(), am == PlotPanel.ZOOM_MODE);
		
		if (am == NO_ACTION_MODE) {
			toolBar.buttonCenter.setVisible(false);
			toolBar.buttonZoom.setVisible(false);
		}
	}

	public void setNoteCoords(final boolean b) {
		if (b) {
			for (int i = 0; i < plots.size(); i++) {
				( (Plot) plots.get(i)).setNoteEachCoord(true);
			}
		} else {
			for (int i = 0; i < plots.size(); i++) {
				( (Plot) plots.get(i)).setNoteEachCoord(false);
			}
		}
	}
	
	public void setToolBarVisible(final boolean b) {
		toolBar.setVisible(b);
	}

	/////////////////////////////////////////////
	//////// set/get elements ///////////////////
	/////////////////////////////////////////////

	public abstract void initBasenGrid(double[] min, double[] max, int[] axesScales, String[] axesLabels);

	public abstract void initBasenGrid(double[] min, double[] max);

	public abstract void initBasenGrid();

	public void resetBase() {
		base.setBaseCoords();
		repaint();
	}

	public Base getBase() {
		return base;
	}

	public Plot[] getPlots() {
		Plot[] plotarray = new Plot[plots.size()];
		plots.copyInto(plotarray);
		return plotarray;
	}

	public Plot getPlot(int i) {
		return (Plot) plots.get(i);
	}

	public Plotable[] getObjects() {
		Plotable[] plotablearray = new Plotable[objects.size()];
		objects.copyInto(plotablearray);
		return plotablearray;
	}

	public Plotable getObject(int i) {
		return (Plotable) objects.get(i);
	}

	public Grid getGrid() {
		return grid;
	}

	public int[] getAxesScales() {
		return base.getAxesScales();
	}

	public void setAxesLabels(String[] labels) {
		grid.setLegend(labels);
		repaint();
	}

	public void setAxeLabel(int axe, String label) {
		grid.setLegend(axe, label);
		repaint();
	}

	public void setAxesScales(int[] scales) {
		base.setAxesScales(scales);
		setAutoBounds();
	}

	public void setAxeScale(int axe, int scale) {
		base.setAxesScales(axe, scale);
		setAutoBounds(axe);
	}

	public void setFixedBounds(double[] min, double[] max) {
		base.setFixedBounds(min, max);
		updateBase();
		repaint();
	}

	public void setFixedBounds(int axe, double min, double max) {
		base.setFixedBounds(axe, min, max);
		updateBase();
		repaint();
	}

	public void setAutoBounds() {
		if (plots.size() > 0) {
			Plot plot0 = this.getPlot(0);
			base.setnRoundBounds(DoubleArray.min(plot0.getDatas()),DoubleArray.max(plot0.getDatas()));
		} else { //build default min and max bounds
			double[] min = new double[base.getDimension()];
			double[] max = new double[base.getDimension()];
			for (int i = 0; i < base.getDimension(); i++) {
				if (base.getAxeScale(i) == PlotPanel.LINEAR) {
					min[i] = 0.0;
					max[i] = 1.0;
				} else if (base.getAxeScale(i) == PlotPanel.LOG) {
					min[i] = 1.0;
					max[i] = 10.0;
				}
			}
			base.setnRoundBounds(min, max);
		}
		for (int i = 1; i < plots.size(); i++) {
			Plot ploti = this.getPlot(i);
			base.includeInBounds(DoubleArray.min(ploti.getDatas()));
			base.includeInBounds(DoubleArray.max(ploti.getDatas()));
		}
		updateBase();
		repaint();
	}

	public void setAutoBounds(int axe) {
		if (plots.size() > 0) {
			Plot plot0 = this.getPlot(0);
			base.setnRoundBounds(axe, DoubleArray.min(plot0.getDatas())[axe],DoubleArray.max(plot0.getDatas())[axe]);
		} else { //build default min and max bounds
			double min = 0.0;
			double max = 0.0;
			if (base.getAxeScale(axe) == PlotPanel.LINEAR) {
				min = 0.0;
				max = 1.0;
			} else if (base.getAxeScale(axe) == PlotPanel.LOG) {
				min = 1.0;
				max = 10.0;
			}
			base.setnRoundBounds(axe, min, max);
		}

		for (int i = 1; i < plots.size(); i++) {
			Plot ploti = this.getPlot(i);
			base.includeInBounds(axe,DoubleArray.min(ploti.getDatas())[axe]);
			base.includeInBounds(axe,DoubleArray.max(ploti.getDatas())[axe]);
		}
		updateBase();
		repaint();
	}

	@Override
	public void updateBase() {
		base.setBaseCoords();
		grid.updateBase();
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i)instanceof BaseScalesDependant) {
				( (BaseScalesDependant) (objects.get(i))).updateBase();
			}
		}
	}

	/////////////////////////////////////////////
	//////// add/remove elements ////////////////
	/////////////////////////////////////////////

	public void addLabel(String text, double[] where, Color c) {
		addLabel(text, new RelativeCoord(where, base), c);
	}

	public void addLabel(String text, Coord where, Color c) {
		addPlotable(new org.jmathplot.gui.plotObjects.Label(text, where, c));
	}

	public void addBaseLabel(String text, double[] where, Color c) {
		addPlotable(new org.jmathplot.gui.plotObjects.BaseLabel(text, base, where, c));
	}

	public void addPlotable(Plotable p) {
		objects.add(p);
		repaint();
	}

	public void removePlotable(Plotable p) {
		objects.remove(p);
		repaint();
	}

	public void removePlotable(int i) {
		objects.remove(i);
		repaint();
	}

	public void addPlot(Plot newPlot) {
		plots.add(newPlot);
		setAutoBounds();
	}

	public void addPlot(double[][] XY, String name, String type) {
		addPlot(XY, name, type, colorList[plots.size() % colorList.length]);
	}

	public abstract void addPlot(double[][] XY, String name, String type, Color c);

	public void removePlot(int I) {
		plots.remove(I);
		if (plots.size() == 0) {
			initBasenGrid();
		} else {
			setAutoBounds();
		}
	}

	public void removePlot(Plot p) {
		plots.remove(p);
		if (plots.size() == 0) {
			initBasenGrid();
		} else {
			setAutoBounds();
		}
	}

	public void removeAllPlots() {
		plots.removeAllElements();
		initBasenGrid();
	}

	/////////////////////////////////////////////
	//////// call for toolbar actions ///////////
	/////////////////////////////////////////////

	public void toGraphicFile(File file) {
		// otherwise toolbar appears
		final boolean isToolBarVisible = toolBar.isVisible();
		toolBar.setVisible(false);
		repaint();

		Image image = createImage(getWidth(), getHeight());
		paint(image.getGraphics());
		image = new ImageIcon(image).getImage();

		BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
					      BufferedImage.TYPE_INT_RGB);
		Graphics g = bufferedImage.createGraphics();
		g.drawImage(image, 0, 0, Color.WHITE, null);
		g.dispose();

		// make it reappear
		toolBar.setVisible(isToolBarVisible);
		repaint();

		try {
			ImageIO.write( (RenderedImage) bufferedImage, "JPEG", file);
		} catch (IOException e) {
			JOptionPane.showConfirmDialog(null, "Save failed : " + e.getMessage(), "Error",
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE);
		} catch (IllegalArgumentException e) {
			//System.out.println("File not saved");
		}
	}

	public void displaySetScalesFrame() {
		new SetScalesFrame(this);
	}

	public void displayDatasFrame() {
		new DatasFrame(this);
	}

	/////////////////////////////////////////////
	//////// Paint method ///////////////////////
	/////////////////////////////////////////////

	@Override
	public void paint(Graphics comp) {
		Graphics2D comp2D = (Graphics2D) comp;

		//anti-aliasing methods
		comp2D.addRenderingHints(AALIAS);
		comp2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		comp2D.setColor(getBackground());
		comp2D.fillRect(0, 0, getSize().width, getSize().height);

		grid.plot(comp);

		for (int i = 0; i < objects.size(); i++) {
			getObject(i).plot(comp);
		}

		for (int i = 0; i < plots.size(); i++) {
			getPlot(i).plot(comp);
			getPlot(i).tryNote(mouseCurent, comp);
		}

		switch (actionMode) {
			case ZOOM_MODE:
				comp2D.setColor(Color.black);
				comp2D.drawRect(Math.min(mouseClick[0], mouseCurent[0]), Math.min(mouseClick[1],
					mouseCurent[1]),
					Math.abs(mouseCurent[0] - mouseClick[0]),
					Math.abs(mouseCurent[1] - mouseClick[1]));
				break;
		}

		setBackground(Color.white);

		if (toolBar.isVisible()) {
			toolBar.update(comp);
		}
	}

	/////////////////////////////////////////////
	//////// Listeners //////////////////////////
	/////////////////////////////////////////////

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseCurent[0] = e.getX();
		mouseCurent[1] = e.getY();
		e.consume();
		int[] t;
		switch (actionMode) {
			case TRANSLATION_MODE:
				t = new int[] {mouseCurent[0] - mouseClick[0], mouseCurent[1] - mouseClick[1]};
				base.translate(t);
				mouseClick[0] = mouseCurent[0];
				mouseClick[1] = mouseCurent[1];
				break;
		}
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseClick[0] = e.getX();
		mouseClick[1] = e.getY();
		e.consume();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		mouseCurent[0] = e.getX();
		mouseCurent[1] = e.getY();
		e.consume();
		mouseClick[0] = mouseCurent[0];
		mouseClick[1] = mouseCurent[1];
		int[] origin;
		double[] ratio;
		switch (actionMode) {
			case ZOOM_MODE:
				if (e.getModifiers() == 16) {
					origin = new int[] { (int) (mouseCurent[0] - panelSize[0] / 4),
						 (int) (mouseCurent[1] - panelSize[1] / 4)};
					ratio = new double[] {0.5, 0.5};
				} else {
					origin = new int[] { (int) (mouseCurent[0] - panelSize[0]),
						 (int) (mouseCurent[1] - panelSize[1])};
					ratio = new double[] {2, 2};
				}
				base.dilate(origin, ratio);
				break;
		}
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseCurent[0] = e.getX();
		mouseCurent[1] = e.getY();
		e.consume();
		switch (actionMode) {
			case ZOOM_MODE:
				if ( (e.getModifiers() == 16) && (mouseCurent[0] != mouseClick[0]) &&
					(mouseCurent[1] != mouseClick[1])) {
					int[] origin = {Math.min(mouseClick[0], mouseCurent[0]), Math.min(mouseClick[1],
						mouseCurent[1])};
					double[] ratio = {Math.abs( (double) (mouseCurent[0] -
						mouseClick[0]) / (double) panelSize[0]),
						Math.abs( (double) (mouseCurent[1] -
						mouseClick[1]) / (double) panelSize[1])};
					base.dilate(origin, ratio);
				}
				break;
		}
		mouseClick[0] = mouseCurent[0];
		mouseClick[1] = mouseCurent[1];
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseCurent[0] = e.getX();
		mouseCurent[1] = e.getY();
		e.consume();
		mouseClick[0] = mouseCurent[0];
		mouseClick[1] = mouseCurent[1];
		repaint();
	}

	@Override
	public void componentHidden(ComponentEvent e) {}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentResized(ComponentEvent e) {
		panelSize = new int[] { (int) (this.getSize().getWidth()), (int) (this.getSize().getHeight())};
		base.setPanelSize(panelSize);
		base.updateScreenCoord();
		repaint();
	}

	@Override
	public void componentShown(ComponentEvent e) {}

}
