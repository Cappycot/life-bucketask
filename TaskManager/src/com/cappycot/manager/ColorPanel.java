package com.cappycot.manager;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ColorPanel extends JPanel {
	private static final long serialVersionUID = 420L;
	private JTextField rField;
	private JTextField gField;
	private JTextField bField;
	int r = 240;
	int g = 240;
	int b = 240;
	private JButton colorButton = new JButton("Enter!");

	/**
	 * Create the panel.
	 */
	public ColorPanel(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
		FlowLayout flowLayout = (FlowLayout) getLayout();
		flowLayout.setVgap(0);

		rField = new JTextField();
		rField.setFont(new Font("Monospaced", Font.PLAIN, 16));
		add(rField);
		rField.setColumns(10);

		gField = new JTextField();
		gField.setFont(new Font("Monospaced", Font.PLAIN, 16));
		gField.setColumns(10);
		add(gField);

		bField = new JTextField();
		bField.setFont(new Font("Monospaced", Font.PLAIN, 16));
		bField.setColumns(10);
		add(bField);

		colorButton.setBackground(returnColor());
		colorButton.setFont(new Font("Monospaced", Font.PLAIN, 16));
		add(colorButton);

		colorButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				colorButton.setBackground(returnColor());
			}
		});

	}

	public void correct() {
		try {
			r = Integer.parseInt(rField.getText());
			if (r > 255)
				r = 255;
			else if (r < 0)
				r = 0;
		} catch (Exception e) {
			rField.setText("" + r);
		}
		try {
			g = Integer.parseInt(gField.getText());
			if (g > 255)
				g = 255;
			else if (g < 0)
				g = 0;
		} catch (Exception e) {
			gField.setText("" + g);
		}
		try {
			b = Integer.parseInt(bField.getText());
			if (b > 255)
				b = 255;
			else if (b < 0)
				b = 0;
		} catch (Exception e) {
			bField.setText("" + b);
		}
	}

	public Color returnColor() {
		correct();
		return new Color(r, g, b);
	}
}
