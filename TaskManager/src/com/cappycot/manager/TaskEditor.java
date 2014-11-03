package com.cappycot.manager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class TaskEditor extends JDialog {
	private static final long serialVersionUID = 69L;
	private JPanel taskPanel = new JPanel();
	private JTextField titleField = new JTextField("New Task");
	private JTextField descField = new JTextField(
			"Description of task to complete.");
	private JComboBox<ActionType> actionType = new JComboBox<>();
	private JTextField actionPath = new JTextField();
	private ColorPanel textColor = new ColorPanel(0, 0, 0);
	private ColorPanel barColor = new ColorPanel(5, 200, 50);
	private int priority = 0;
	private JTextField priorityField = new JTextField("0");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		new TaskEditor(null, null);
	}

	/**
	 * Create the dialog.
	 */
	public TaskEditor(Task task, final TaskWindow adder) {
		final boolean wasOpen = adder != null && adder.isVisible();
		final boolean newTask = task == null;
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				TaskEditor.class.getResource("/com/cappycot/manager/Miku.gif"))); // Loli.png")));
		setAlwaysOnTop(true);
		setVisible(true);
		requestFocus();
		setTitle("Task Editor");
		setBounds(660, 250, 500, 500);
		getContentPane().setLayout(new BorderLayout());
		taskPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(taskPanel, BorderLayout.CENTER);
		taskPanel.setLayout(new GridLayout(11, 0, 0, 5));

		JLabel nameLabel = new JLabel("Task Name");
		nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		nameLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
		taskPanel.add(nameLabel);

		titleField.setHorizontalAlignment(SwingConstants.CENTER);
		titleField.setFont(new Font("Monospaced", Font.PLAIN, 16));
		taskPanel.add(titleField);
		titleField.setColumns(10);

		JLabel descLabel = new JLabel("Description");
		descLabel.setHorizontalAlignment(SwingConstants.CENTER);
		descLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
		taskPanel.add(descLabel);
		descField.setText("Complete this new task.");

		descField.setHorizontalAlignment(SwingConstants.CENTER);
		descField.setFont(new Font("Monospaced", Font.PLAIN, 16));
		descField.setColumns(10);
		taskPanel.add(descField);

		JLabel priLabel = new JLabel("Priority");
		priLabel.setHorizontalAlignment(SwingConstants.CENTER);
		priLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
		taskPanel.add(priLabel);

		JPanel priPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) priPanel.getLayout();
		flowLayout.setVgap(0);
		taskPanel.add(priPanel);

		/* Priority Field */

		JButton leftLeftInc = new JButton("<<");
		leftLeftInc.setFont(new Font("Monospaced", Font.PLAIN, 16));
		priPanel.add(leftLeftInc);
		leftLeftInc.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				increment(-5);
			}
		});

		JButton leftInc = new JButton("<");
		leftInc.setFont(new Font("Monospaced", Font.PLAIN, 16));
		priPanel.add(leftInc);
		leftInc.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				increment(-1);
			}
		});

		priorityField = new JTextField();
		priorityField.setHorizontalAlignment(SwingConstants.CENTER);
		priorityField.setText("0");
		priorityField.setFont(new Font("Monospaced", Font.PLAIN, 16));
		priorityField.setEditable(false);
		priPanel.add(priorityField);
		priorityField.setColumns(10);

		JButton rightInc = new JButton(">");
		rightInc.setFont(new Font("Monospaced", Font.PLAIN, 16));
		priPanel.add(rightInc);
		rightInc.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				increment(1);
			}
		});

		JButton rightRightInc = new JButton(">>");
		rightRightInc.setFont(new Font("Monospaced", Font.PLAIN, 16));
		priPanel.add(rightRightInc);
		rightRightInc.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				increment(5);
			}
		});

		/* Action Field */

		JLabel actionLabel = new JLabel("Action (Type and Path)");
		actionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		actionLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
		taskPanel.add(actionLabel);

		actionType.setBackground(Color.WHITE);
		actionType.setModel(new DefaultComboBoxModel<>(ActionType.values()));
		actionType.setSelectedIndex(0);
		actionType.setFont(new Font("Monospaced", Font.PLAIN, 16));
		taskPanel.add(actionType);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(1.0);
		taskPanel.add(splitPane);

		actionPath.setFont(new Font("Monospaced", Font.PLAIN, 16));
		actionPath.setColumns(10);
		splitPane.setLeftComponent(actionPath);

		/* Set Fields if Editing Preexisting */
		if (task != null) {
			titleField.setText(task.getTitle());
			descField.setText(task.getDesc());
			actionType.setSelectedItem(task.getActionType());
			actionPath.setText(task.getAction().replace('\\', '/'));
			priority = task.getPriority();
			priorityField.setText("" + priority);
			Color t = task.getTextColor();
			textColor = new ColorPanel(t.getRed(), t.getGreen(), t.getBlue());
			Color b = task.getBarColor();
			barColor = new ColorPanel(b.getRed(), b.getGreen(), b.getBlue());
		} else {
			task = new Task("", "", 0, "null", false, ActionType.NONE,
					Color.BLACK, Color.CYAN, 0);
		}

		JButton browseButton = new JButton("Browse...");
		browseButton.setFont(new Font("Monospaced", Font.PLAIN, 16));
		splitPane.setRightComponent(browseButton);
		final TaskEditor hide = this;
		browseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser filer = new JFileChooser();
				hide.setAlwaysOnTop(false);
				int option = filer.showOpenDialog(null);
				hide.setAlwaysOnTop(true);
				if (option == JFileChooser.APPROVE_OPTION) {
					actionType.setSelectedItem(ActionType.FILE);
					actionPath.setText(filer.getSelectedFile().getPath());
				}
			}
		});

		taskPanel.add(textColor);
		taskPanel.add(barColor);

		JPanel optionPanel = new JPanel();
		optionPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(optionPanel, BorderLayout.SOUTH);

		JButton okButton = new JButton("Save");
		okButton.setActionCommand("OK");
		optionPanel.add(okButton);
		getRootPane().setDefaultButton(okButton);
		final TaskEditor editor = this;
		final Task done = task;
		okButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (adder == null) {
					dispose();
					return;
				}
				/* Mod Task */
				done.change(titleField.getText().replace(";", ""), descField
						.getText().replace(";", ""), priority,
						(ActionType) actionType.getSelectedItem(), actionPath
								.getText().replace(";", ""), textColor
								.returnColor(), barColor.returnColor());
				if (newTask) {
					adder.getRunner().addTask(editor);
				} else {
					adder.updateTasks(null);
					adder.getRunner().update();
				}
				adder.setVisible(wasOpen);
				dispose();
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		optionPanel.add(cancelButton);
		cancelButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				adder.setVisible(wasOpen);
				dispose();
			}
		});
	}

	public void increment(int pri) {
		priority += pri;
		if (priority < 0)
			priority = 0;
		else if (priority > 100)
			priority = 100;
		priorityField.setText("" + priority);
	}

	public Task getTask() {
		return new Task(titleField.getText().replace(";", ""), descField
				.getText().replace(";", ""), priority, actionPath.getText()
				.replace(";", ""), false,
				(ActionType) actionType.getSelectedItem(),
				textColor.returnColor(), barColor.returnColor(), 0);
	}
}
