package com.cappycot.manager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class TaskWindow extends JFrame implements WindowListener {

	private static final long serialVersionUID = 1L;
	private TaskRunner thread;
	private ArrayList<Task> tasks = new ArrayList<>();
	private JPanel contentPane = new JPanel();
	private JScrollPane scrollPane = new JScrollPane();
	private JPanel taskPanel = new JPanel();
	private JTextField statusText = new JTextField();
	private JButton taskButton = new JButton("Add task...");
	private final Component verticalStrut = Box.createVerticalStrut(20);

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TaskWindow frame = new TaskWindow(null);
					frame.setVisible(true);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public TaskWindow(TaskRunner thread) {
		this.thread = thread;
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				TaskWindow.class.getResource("/com/cappycot/manager/Miku.gif"))); // Loli.png")));
		setTitle("Miku's Task List V0.3.1");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(320, 40, 1280, 960);
		// setBounds(160, 40, 1600, 900);
		setResizable(false);

		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(5, 5));
		setContentPane(contentPane);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		contentPane.add(scrollPane, BorderLayout.CENTER);

		scrollPane.setViewportView(taskPanel);
		taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.X_AXIS));

		taskPanel.add(verticalStrut);

		statusText.setHorizontalAlignment(SwingConstants.CENTER);
		statusText
				.setText("0 Current Tasks, 0 Daily Tasks, 0 Total Completed Tasks");
		statusText.setFont(new Font("Monospaced", Font.PLAIN, 16));
		statusText.setEditable(false);
		contentPane.add(statusText, BorderLayout.NORTH);
		statusText.setColumns(10);

		taskButton.setFont(new Font("Monospaced", Font.PLAIN, 16));
		contentPane.add(taskButton, BorderLayout.SOUTH);

		addWindowListener(this);

		final TaskWindow window = this;
		taskButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				new TaskEditor(null, window);
			}
		});
	}

	public void updateTasks(ArrayList<Task> newTasks) {
		if (newTasks != null)
			tasks = newTasks;
		taskPanel.removeAll();
		int min = 7;
		int ySize = Task.getSizedY();
		if (tasks.size() > 0) {
			taskPanel.setLayout(new GridLayout(tasks.size() >= min ? tasks
					.size() : min, 0, 0, 0));
			Task sizer = tasks.get(0);
			sizer.resize(taskPanel.getWidth());
			taskPanel.setPreferredSize(new Dimension(taskPanel.getParent()
					.getWidth(), tasks.size() >= min ? (ySize * tasks.size())
					: min * ySize));
			for (Task t : tasks) {
				t.resize(taskPanel.getWidth());
				taskPanel.add(t);
				t.repaint();
			}
			for (int i = 0; i < (min - tasks.size()); i++) {
				taskPanel.add(Box.createVerticalStrut(ySize));
			}
		} else {
			taskPanel.setLayout(new GridLayout(1, 0, 0, 0));
			taskPanel.setPreferredSize(new Dimension(taskPanel.getParent()
					.getWidth(), min * ySize));
			taskPanel.add(Box.createVerticalStrut(200));
			JLabel happy = new JLabel("Hurray! No tasks here!");
			happy.setFont(new Font("Monospaced", Font.PLAIN, 16));
			happy.setHorizontalAlignment(SwingConstants.CENTER);
			taskPanel.add(happy);
			taskPanel.add(Box.createVerticalStrut(200));
		}

		taskPanel.repaint();
		JScrollBar x = scrollPane.getVerticalScrollBar();
		int i = x.getValue();
		x.setValue(x.getMaximum());
		x.setValue(x.getMinimum());
		x.setValue(i);
		updateStatus();
	}

	public TaskRunner getRunner() {
		return thread;
	}

	/* Stats Update Methods */

	private int daily = 0;

	public void setDailyCount(int count) {
		daily = count;
		updateStatus();
	}

	public void updateStatus() {
		statusText.setText(tasks.size() + " Current Tasks, " + daily
				+ " Daily Tasks");
	}

	@Override
	public void windowActivated(WindowEvent e) {
		requestFocus();
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		setVisible(false);
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}
}
