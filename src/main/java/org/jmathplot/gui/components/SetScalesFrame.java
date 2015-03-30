package org.jmathplot.gui.components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.jmathplot.gui.*;
import org.jmathplot.gui.plotObjects.*;

/**
 * <p>Copyright : BSD License</p>
 * @author Yann RICHET
 * @version 3.0
 */

public class SetScalesFrame extends JFrame {

	private static final long serialVersionUID = -2366396762066112355L;
	
	private PlotPanel plotPanel;
	private JPanel panel;

	public SetScalesFrame(PlotPanel p) {
		super("scales settings");
		plotPanel = p;
		setPanel();
		setContentPane(panel);

		setResizable(false);
		setVisible(true);
	}

	private void setPanel() {
		int nbAxes = plotPanel.getBase().getDimension();

		this.setSize(nbAxes * 300, 200);

		panel = new JPanel();
		GridLayout gbl = new GridLayout(1, nbAxes);
		panel.setLayout(gbl);

		for (int i = 0; i < nbAxes; i++) {
			ScalePanel s = new ScalePanel(plotPanel, i);
			panel.add(s);
		}
	}

	public class ScalePanel extends JPanel {

		private static final long serialVersionUID = 7167614656051518359L;
		
		private PlotPanel plotPanel;
		private int numAxe;

		private String title;
		private int scaleType;
		private double min;
		private double max;

		private JLabel title_label = new JLabel("Title");

		private JTextField title_field;

		private JLabel scale_label = new JLabel("Scale");

		private ButtonGroup scale_group = new ButtonGroup();

		private JRadioButton linear_check = new JRadioButton("Linear");

		private JRadioButton log_check = new JRadioButton("Logarithmic");

		private JLabel bounds_label = new JLabel("Bounds");
		private JLabel min_label = new JLabel("Min");
		private JLabel max_label = new JLabel("Max");
		private JTextField min_field;
		private JTextField max_field;
		private JButton bounds_auto = new JButton("Automatic");

		public ScalePanel(PlotPanel p, int i) {
			numAxe = i;
			plotPanel = p;

			title = plotPanel.getGrid().getLegend(numAxe);
			scaleType = plotPanel.getAxesScales()[numAxe];
			min = plotPanel.getBase().getMinBounds()[numAxe];
			max = plotPanel.getBase().getMaxBounds()[numAxe];

			setComponents();
			addComponents();
			setListeners();
		}

		private void setComponents() {
			title_field = new JTextField(title);

			scale_group.add(linear_check);
			scale_group.add(log_check);
			log_check.setSelected(scaleType == Base.LOG);
			linear_check.setSelected(scaleType == Base.LINEAR);

			min_field = new JTextField("" + min);
			max_field = new JTextField("" + max);
		}

		private void addComponents() {
			this.setSize(300, 200);

			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			this.setLayout(gbl);

			buildConstraints(c, 0, 0, 1, 1, 40, 20);
			c.fill = GridBagConstraints.CENTER;
			c.anchor = GridBagConstraints.CENTER;
			gbl.setConstraints(title_label, c);
			this.add(title_label);

			buildConstraints(c, 1, 0, 2, 1, 60, 20);
			c.fill = GridBagConstraints.HORIZONTAL;
			gbl.setConstraints(title_field, c);
			this.add(title_field);

			buildConstraints(c, 0, 1, 1, 1, 40, 20);
			c.fill = GridBagConstraints.CENTER;
			c.anchor = GridBagConstraints.CENTER;
			gbl.setConstraints(scale_label, c);
			this.add(scale_label);

			buildConstraints(c, 1, 1, 2, 1, 60, 20);
			c.fill = GridBagConstraints.HORIZONTAL;
			gbl.setConstraints(linear_check, c);
			this.add(linear_check);

			buildConstraints(c, 1, 2, 2, 1, 60, 20);
			c.fill = GridBagConstraints.HORIZONTAL;
			gbl.setConstraints(log_check, c);
			this.add(log_check);

			buildConstraints(c, 0, 3, 1, 1, 40, 20);
			c.fill = GridBagConstraints.CENTER;
			c.anchor = GridBagConstraints.CENTER;
			gbl.setConstraints(bounds_label, c);
			this.add(bounds_label);

			buildConstraints(c, 1, 3, 1, 1, 20, 20);
			c.fill = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.CENTER;
			gbl.setConstraints(min_label, c);
			this.add(min_label);

			buildConstraints(c, 2, 3, 1, 1, 50, 20);
			c.fill = GridBagConstraints.HORIZONTAL;
			gbl.setConstraints(min_field, c);
			this.add(min_field);

			buildConstraints(c, 1, 4, 1, 1, 20, 20);
			c.fill = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.CENTER;
			gbl.setConstraints(max_label, c);
			this.add(max_label);

			buildConstraints(c, 2, 4, 1, 1, 50, 20);
			c.fill = GridBagConstraints.HORIZONTAL;
			gbl.setConstraints(max_field, c);
			this.add(max_field);

			buildConstraints(c, 1, 5, 2, 1, 60, 20);
			c.fill = GridBagConstraints.CENTER;
			gbl.setConstraints(bounds_auto, c);
			this.add(bounds_auto);
		}

		private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
			gbc.gridx = gx;
			gbc.gridy = gy;
			gbc.gridwidth = gw;
			gbc.gridheight = gh;
			gbc.weightx = wx;
			gbc.weighty = wy;
		}

		private void setListeners() {
			title_field.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e) {
					setTitle();
				}

				public void keyPressed(KeyEvent e) {}

				public void keyTyped(KeyEvent e) {}
			});

			log_check.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setScale();
				}
			});
			linear_check.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setScale();
				}
			});

			min_field.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e) {
					setBounds();
				}

				public void keyPressed(KeyEvent e) {}

				public void keyTyped(KeyEvent e) {}
			});
			max_field.addKeyListener(new KeyListener() {
				public void keyReleased(KeyEvent e) {
					setBounds();
				}

				public void keyPressed(KeyEvent e) {}

				public void keyTyped(KeyEvent e) {}
			});

			bounds_auto.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setBoundsAuto();
				}
			});
		}

		public void toCommandLine(String s) {
			System.out.println(s + " : ");
			System.out.println("title = " + title);
			System.out.println("scaleType = " + scaleType);
			System.out.println("min = " + min);
			System.out.println("max = " + max);
		}

		private void setTitle() {
			plotPanel.setAxeLabel(numAxe, title_field.getText());
		}

		private void setBounds() {
			try {
				plotPanel.setFixedBounds(numAxe, Double.parseDouble(min_field.getText()),
					Double.parseDouble(max_field.getText()));
			} catch (IllegalArgumentException iae) {
				JOptionPane.showConfirmDialog(null, iae.getMessage()
					, "Error"
					, JOptionPane.DEFAULT_OPTION
					, JOptionPane.ERROR_MESSAGE
					);
				updateBoundsFields();
			}
		}

		private void setScale() {
			try {
				plotPanel.setAxeScale(numAxe, (log_check.isSelected()) ? (Base.LOG) :
					(Base.LINEAR));
			} catch (IllegalArgumentException iae) {
				JOptionPane.showConfirmDialog(null, iae.getMessage()
					, "Error"
					, JOptionPane.DEFAULT_OPTION
					, JOptionPane.ERROR_MESSAGE
					);
				updateBoundsFields();
			}
		}

		private void setBoundsAuto() {
			plotPanel.setAutoBounds(numAxe);
			updateBoundsFields();
		}

		private void updateBoundsFields() {
			min = plotPanel.getBase().getMinBounds()[numAxe];
			max = plotPanel.getBase().getMaxBounds()[numAxe];
			min_field.setText("" + min);
			max_field.setText("" + max);
		}
	}
}
