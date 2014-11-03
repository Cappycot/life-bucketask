package com.cappycot.manager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

public class Task extends JPanel implements Comparable<Task> {
	/* Konata, get it together! */
	private static final long serialVersionUID = 26L;
	/* Task Components */
	private String title;
	private String desc;
	private int priority;
	private String action;
	private ActionType actionType;
	/* Extra Information */
	private boolean finished = false;
	// private long dateCreated;
	private int progress = 0;
	private boolean daily;
	/* Swing Components */
	private TaskWindow window;
	private Color textColor = Color.BLACK;
	private Color barColor = Color.BLUE;
	private JSlider slider = new JSlider();
	private JProgressBar progressBar = new JProgressBar();
	private JButton finButton = new JButton("000%!");
	private JButton goButton = new JButton("Title!");
	private JButton editButton = new JButton("Edit!");
	private JLabel descLabel = new JLabel("Desc");

	/**
	 * Create the panel.
	 */
	public Task(String title, String desc, int priority, String action,
			boolean daily, ActionType at, Color c, Color d, int progress) {

		/* Set Variables */
		this.title = title;
		this.desc = desc;
		this.priority = priority;
		this.action = action;
		this.daily = daily;
		this.progress = progress;
		actionType = at;
		textColor = c;
		barColor = d;
		// dateCreated = new Date().getTime();
		/* Assemble Panel */
		setLayout(new BorderLayout(0, 0));
		add(goButton, BorderLayout.NORTH);
		goButton.setFont(new Font("Monospaced", Font.PLAIN, 16));

		add(editButton, BorderLayout.WEST);
		editButton.setFont(new Font("Monospaced", Font.PLAIN, 16));

		add(finButton, BorderLayout.EAST);
		finButton.setFont(new Font("Monospaced", Font.PLAIN, 16));

		JPanel comPanel = new JPanel();
		add(comPanel, BorderLayout.SOUTH);
		comPanel.setLayout(new BorderLayout(0, 0));
		comPanel.add(progressBar, BorderLayout.SOUTH);
		progressBar.setStringPainted(true);
		progressBar.setFont(new Font("Monospaced", Font.PLAIN, 16));
		comPanel.add(slider, BorderLayout.CENTER);

		slider.setToolTipText("Completion Level");
		slider.setSnapToTicks(false);
		slider.setPaintTicks(true);
		slider.setMinorTickSpacing(1);
		slider.setMajorTickSpacing(5);
		slider.setValue(progress);
		slider.setFont(new Font("Monospaced", Font.PLAIN, 16));
		descLabel.setHorizontalAlignment(SwingConstants.CENTER);
		descLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
		descLabel.setText(desc);
		add(descLabel, BorderLayout.CENTER);
		update();

		/* Event Listeners */
		final Task task = this;
		slider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent m) {
				update();
			}
		});
		finButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				doFin();
			}
		});
		editButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				new TaskEditor(task, window);
			}
		});
		goButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (finished)
					return;
				doAction();
			}
		});
	}

	public void doAction() {
		Desktop desk = Desktop.getDesktop();
		try {
			switch (actionType) {
			case FILE:
				desk.open(new File(action));
				break;
			case URL:
				desk.browse(new URI(action));
				break;
			case MAIL:
				desk.mail(new URI("mailto:" + action));
				break;
			case NOTE:
				window.getRunner().message("Miku's Report", action,
						TrayIcon.MessageType.INFO);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			window.getRunner().report(e);
		}
	}

	public void doFin() {
		if (slider.getValue() == 100) {
			finished = true;
			window.getRunner().clear(this);
		} else
			doAction();
	}

	public void change(String title, String desc, int priority, ActionType at,
			String action, Color text, Color bar) {
		this.title = title;
		this.desc = desc;
		this.priority = priority;
		this.action = action;
		actionType = at;
		textColor = text;
		barColor = bar;
		update();
	}

	public void setWindow(TaskWindow window) {
		this.window = window;
	}

	public void setProgress(int progress) {
		this.progress = progress;
		slider.setValue(progress);
		update();
	}

	public void update() {
		int val = slider.getValue();
		progress = val;
		if (val < 10) {
			finButton.setText("00" + val + "%!");
		} else if (val < 100) {
			finButton.setText("0" + val + "%!");
		} else {
			finButton.setText("Done!");
		}
		goButton.setText((daily ? "(Daily) " : "") + title);
		descLabel.setText(desc);
		goButton.setForeground(textColor);
		editButton.setForeground(textColor);
		finButton.setForeground(textColor);
		progressBar.setForeground(barColor);
		progressBar.setValue(progress);
		if (window != null)
			window.getRunner().updateInfo();
	}

	/* Swing Helpers */

	// private static final int divisor = 10;

	public void resize(int x) {
		setPreferredSize(new Dimension(x, getSizedY())); // x / divisor));
	}

	public static int getSizedY() {
		return 125; // (int) (getPreferredSize().getWidth() / divisor);
	}

	/* Processing Methods */

	public boolean identical(Task another) {
		return this.daily == another.daily && this.title.equals(another.title)
				&& this.desc.equals(another.desc)
				&& this.action.equals(another.action)
				&& this.actionType == another.actionType;
	}

	public int compareTo(Task another) {
		int result = another.priority - this.priority;
		if (result != 0)
			return result;
		return this.title.compareTo(another.title);
	}

	/* Getters */

	public boolean isFinished() {
		return finished;
	}

	public boolean isDaily() {
		return daily;
	}

	public String getTitle() {
		return title;
	}

	public String getDesc() {
		return desc;
	}

	public int getPriority() {
		return priority;
	}

	public String getAction() {
		return action;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public Color getTextColor() {
		return textColor;
	}

	public Color getBarColor() {
		return barColor;
	}

	public String toString() {
		return title + "; " + desc + "; " + priority + "; " + actionType + "; "
				+ action + "; " + TaskRunner.getHexColor(textColor) + "; "
				+ TaskRunner.getHexColor(barColor) + "; " + daily + "; "
				+ progress;
	}
}
