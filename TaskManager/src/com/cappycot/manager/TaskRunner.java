package com.cappycot.manager;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SplashScreen;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;

/**
 * The main driving thread that tracks time by the second.<br>
 * Also uses a TrayIcon for notifications.<br>
 * Can only run in Windows, sadly enough...
 * 
 * @author Chris Wang
 */
public class TaskRunner extends Thread {
	public static boolean testing = false;
	/* IO Variables */
	private static boolean cleared = false;
	private static Runtime runtime = Runtime.getRuntime();
	private File taskDir;
	private File dailyFile;
	private File taskFile;
	private File infoFile;
	private PrintWriter pw;
	/* Task Creation/Completion */
	private ArrayList<Task> tasks = new ArrayList<>();
	private ArrayList<Task> daily = new ArrayList<>();
	private boolean newTask = false;
	private boolean update = false;
	/* Time Based Variables */
	private int lastDailyUpdate;
	private long lastUpdate = 0;
	private long lastOpened = 0;
	private int updateInterval = 899_999;
	private int restartInterval = 3_599_999;
	private boolean shutdown = !testing;
	private int shutdownStart = 60;
	private int shutdownTimer = shutdownStart;
	private boolean startup = true;
	private boolean restart = false;
	/* Menu Components */
	private Font font = new Font("Arial", Font.PLAIN, 14);
	private TaskWindow window = new TaskWindow(this);
	private TrayIcon trayIcon = null;
	private MenuItem taskIndicator = new MenuItem("Open");
	private MenuItem disposer = new MenuItem("Shutdowns "
			+ (!testing ? "Enabled..." : "Disabled..."));

	public static void main(String[] args) throws Exception {
		File taskDir;
		if (args != null && args.length > 0)
			taskDir = new File(args[0]);
		else
			taskDir = new File("C:/Users/" + System.getProperty("user.name")
					+ "/Desktop/Tasks");
		new TaskRunner(taskDir).start();
	}

	public static Color parseHexColor(String hex) {
		try {
			int r = Integer.parseInt(hex.substring(0, 2), 16);
			int g = Integer.parseInt(hex.substring(2, 4), 16);
			int b = Integer.parseInt(hex.substring(4, 6), 16);
			return new Color(r, g, b);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Color.BLACK;
	}

	/**
	 * Stolen code from some random guy because I'm too lazy to make rgb hexes.
	 * 
	 * @param color
	 * @return hex
	 */
	public static String getHexColor(Color color) {
		String hexColor = Integer.toHexString(color.getRGB() & 0xffffff);
		if (hexColor.length() < 6) {
			hexColor = "000000".substring(0, 6 - hexColor.length()) + hexColor;
		}
		return hexColor;
	}

	public TaskRunner(File dir) {
		SplashScreen ss = null;
		try {
			ss = SplashScreen.getSplashScreen();
			ss.setImageURL(TaskRunner.class
					.getResource("/com/cappycot/manager/YueHaiIcon.gif"));
			// SplashScreen-Image: com/cappycot/manager/Miku.gif
		} catch (Exception e) {
			ss = null;
		}
		/* Check for any existing process open already... */
		if (!testing && !cleared) {
			try {
				String cmds[] = { "cmd", "/c", "tasklist" };
				Process proc = runtime.exec(cmds);

				InputStream inputstream = proc.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(
						inputstream);
				BufferedReader bufferedreader = new BufferedReader(
						inputstreamreader);

				String line;
				int javas = 0;
				while ((line = bufferedreader.readLine()) != null) {
					if (line.startsWith("javaw.exe"))
						javas++;
					if (javas > 1)
						System.exit(0);
				}
			} catch (Exception e) {
				System.exit(0);
			}
		}
		cleared = true;
		/* Load Main Files */
		taskDir = dir;
		dailyFile = new File(dir.getPath() + "/Daily.txt");
		taskFile = new File(dir.getPath() + "/Main.txt");
		tasks = readTaskFile(taskFile, false);
		daily = readTaskFile(dailyFile, true);
		/* Read Main Info */
		infoFile = new File(dir.getPath() + "/Info.txt");
		try (Scanner sc = new Scanner(infoFile)) {
			while (!sc.nextLine().equals(("[Info]")))
				;
			lastDailyUpdate = Integer.parseInt(sc.nextLine());
		} catch (Exception e) {
			e.printStackTrace();
		}
		window.setDailyCount(daily.size());
		/* Add Miku Icon using Stolen Code */
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().getImage(
					TaskRunner.class
							.getResource("/com/cappycot/manager/Miku.gif"));
			/* Menu Actions */
			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!isShutting())
						openWindow(true);
				}
			};
			ActionListener edit = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!isShutting())
						new TaskEditor(null, window);
				}
			};
			ActionListener shut = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					shutdown = !shutdown;
					disposer.setLabel("Shutdowns "
							+ (shutdown ? "Enabled..." : "Disabled..."));
					update = true;
				}
			};
			ActionListener refresh = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!isShutting())
						update = true;
				}
			};
			ActionListener restartTask = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					restart = true;
				}
			};
			/* Menu Components */
			PopupMenu menu = new PopupMenu("Miku's Tasks");
			menu.setFont(font);
			taskIndicator.setLabel("Open! (" + tasks.size() + " Tasks)");
			menu.add(taskIndicator);
			menu.addActionListener(listener);
			MenuItem editor = new MenuItem("New Task");
			menu.add(editor);
			editor.addActionListener(edit);
			MenuItem updater = new MenuItem("Get Daily Info");
			menu.add(updater);
			updater.addActionListener(refresh);
			MenuItem restarter = new MenuItem("Restart Manager");
			menu.add(restarter);
			restarter.addActionListener(restartTask);
			menu.add(disposer);
			disposer.addActionListener(shut);
			/* Tray Icon Menu */
			trayIcon = new TrayIcon(image, "Miku's Tasks", menu);
			trayIcon.setImageAutoSize(true);
			// set the TrayIcon properties
			trayIcon.addActionListener(listener);
			try {
				tray.add(trayIcon);
				message("Kon'nichiwa!", "Watashi wa Hatsune Miku desu!",
						TrayIcon.MessageType.INFO);
				Thread.sleep(2_000L);
				if (ss != null)
					ss.close();
				startup = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else
			System.exit(0);
	}

	public void update() {
		update = true;
	}

	public void openWindow(boolean visible) {
		if (startup)
			return;
		Collections.sort(tasks);
		window.updateTasks(tasks);
		window.repaint();
		window.setVisible(visible);
	}

	public ArrayList<Task> readTaskFile(File file, boolean daily) {
		ArrayList<Task> newTasks = new ArrayList<>();
		try (Scanner sc = new Scanner(file)) { // Aww yiss Try-with-Resources!
			while (!sc.nextLine().equals("[Tasks]"))
				;
			while (sc.hasNextLine()) {
				String[] args = sc.nextLine().split("; ");
				Task t = new Task(args[0], args[1], Integer.parseInt(args[2]),
						args[4], new Boolean(args[7]), ActionType.get(args[3]),
						parseHexColor(args[5]), parseHexColor(args[6]),
						Integer.parseInt(args[8]));
				t.setWindow(window);
				newTasks.add(t);
			}
		} catch (Exception e) {
			report(e);
		}
		return newTasks;
	}

	public void writeTaskFile(File file, ArrayList<Task> newTasks) {
		try {
			pw = new PrintWriter(file);
			if (file.exists())
				file.delete();
			file.createNewFile();
			pw.println("[Tasks]");
			for (Task t : newTasks)
				pw.println(t.toString());
			pw.close();
		} catch (Exception e) {
			report(e);
		}
	}

	public void updateInfo() {
		/* Update Main File */
		taskIndicator.setLabel("Open! (" + tasks.size() + " Tasks)");
		writeTaskFile(taskFile, tasks);
		/* Update Info File */
		try {
			if (infoFile.exists())
				infoFile.delete();
			infoFile.createNewFile();
			pw = new PrintWriter(infoFile);
			pw.println("[Info]");
			pw.println(lastDailyUpdate);
			pw.close();
		} catch (Exception e) {
			report(e);
		}
	}

	public void mergeDailies() {
		for (Task t : daily) {
			boolean same = false;
			current: for (Task x : tasks) {
				if (t.identical(x)) {
					same = true;
					break current;
				}
			}
			if (!same)
				tasks.add(t);
		}
	}

	public void addTask(TaskEditor editor) {
		Task t = editor.getTask();
		t.setWindow(window);
		tasks.add(t);
		newTask = true;
	}

	public void clear(Task t) {
		// try {
		if (!t.isFinished())
			return;
		tasks.remove(t);
		playSound("Success.wav");
		// } catch (Exception e) {
		// report(e);
		// return;
		// }
		updateInfo();
		openWindow(window.isVisible());
	}

	public void run() {
		while (!restart) {
			/* Time Mark */
			Calendar cal = Calendar.getInstance();
			long currentTime = cal.getTimeInMillis();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int currentDay = cal.get(Calendar.DAY_OF_MONTH);
			/* Potential Message */
			String msg = null;
			/* Calculate Hour of Day */
			boolean dayHours = !(hour >= 1 && hour < 6);
			if (window.isVisible() || lastOpened == 0)
				lastOpened = currentTime;
			// Hours check.
			if (lastDailyUpdate != currentDay && dayHours && hour != 0) {
				daily = readTaskFile(dailyFile, true);
				mergeDailies();
				lastDailyUpdate = currentDay;
				lastUpdate = currentTime;
				update = true;
				msg = "Hi! I added today's daily tasks. You now have "
						+ tasks.size() + " tasks now! (^_^)";
			} else if (!dayHours && shutdown && shutdownTimer == shutdownStart) {
				message("Miku's Warning", "I'm shutting this place in "
						+ (shutdownStart / 60) + " min! ('>_<)",
						TrayIcon.MessageType.WARNING);
				shutdownTimer--;
			}
			// Update Implementation
			if (!dayHours && shutdown && shutdownTimer < shutdownStart) {
				window.setVisible(false);
				shutdownTimer--;
				disposer.setLabel("Shutdown in " + shutdownTimer + " seconds.");
				if (shutdownTimer <= 0)
					shutdown();
			} else if (update
					|| newTask
					|| currentTime > lastUpdate
							+ (updateInterval / ((currentTime - lastOpened)
									/ updateInterval + 1))) {
				// Contingency Fix
				if (shutdownTimer < shutdownStart) {
					shutdownTimer = shutdownStart;
					msg = "Shutdown aborted... " + tasks.size()
							+ " tasks remaining. (-_-)";
					update = false;
				}
				// Refresh Daily Tasks
				daily = readTaskFile(dailyFile, true);
				int toDo = 0;
				for (Task t : tasks) {
					if (t.isDaily())
						toDo++;
				}
				// Set message.
				if (msg == null)
					if (newTask)
						msg = "I added your new task! You have " + tasks.size()
								+ " tasks now. (^_^)";
					else if (update)
						msg = "You currently have " + toDo + " out of "
								+ daily.size()
								+ " daily tasks to do right now. (^_^)";
					else if (currentTime > lastUpdate + restartInterval
							&& lastUpdate > 0)
						break; // Restart entire task messenger.
					else
						msg = tasks.size() > 0 ? "Hello! You have "
								+ tasks.size() + " tasks! Remember? (o_o')"
								: "Hello! You have no tasks right now! (^_^)";
				// Update Window
				window.setDailyCount(toDo);
				openWindow(window.isVisible());
				// Update Time Variables
				if (!update && !newTask)
					lastUpdate = currentTime;
				update = false;
				newTask = false;
				// Send message.
				message("Miku's Update", msg, TrayIcon.MessageType.INFO);
				// Set off update.
				updateInfo();
			}
			/* Waiting Portion */
			try {
				if (window.isVisible()) {
					window.setState(JFrame.NORMAL);
					Thread.sleep(0_250L);
				} else
					Thread.sleep(1_000L);
			} catch (Exception e) {
				System.out.println("Skip...");
			}
		}
		removeAll();
		new TaskRunner(taskDir).start();
	}

	public void removeAll() {
		SystemTray.getSystemTray().remove(trayIcon);
		window.setVisible(false);
	}

	public void message(String title, String msg, TrayIcon.MessageType type) {
		trayIcon.displayMessage(title, msg, type);
		playSound("Notif.wav");
	}

	/**
	 * Optimizer for minimal memory usage.<br>
	 * Allows the player to keep track if a sound playing.
	 */
	public volatile boolean soundPlaying = false;

	/**
	 * Plays a single sound with the name specified from the main file
	 * directory.<br>
	 * Optimized for minimal memory usage. If there is already<br>
	 * a sound playing, this method will not continue.
	 * 
	 * @param soundName
	 */
	public void playSound(final String soundName) {
		if (!soundPlaying) {
			(new Thread() {
				@Override
				public void run() {
					try {
						if (soundPlaying)
							return;
						soundPlaying = true;
						AudioInputStream audioInputStream = AudioSystem
								.getAudioInputStream(new File(taskDir.getPath()
										+ "/" + soundName).getAbsoluteFile());
						Clip clip = AudioSystem.getClip();
						clip.open(audioInputStream);
						clip.start();
						Thread.sleep(clip.getMicrosecondLength() / 1000);
						soundPlaying = false;
						clip.close();
						audioInputStream.close();
					} catch (Exception e) {
						report(e);
					}
				}
			}).start();
		}
	}

	public void report(Exception e) {
		e.printStackTrace();
		trayIcon.displayMessage("Miku's Error",
				"Uh oh! I hit a boo boo! ('>_<)(" + e.getMessage() + "!)",
				TrayIcon.MessageType.ERROR);
	}

	public boolean isShutting() {
		return shutdownTimer < shutdownStart;
	}

	public void shutdown() {
		message("Shutdown!", "Executing shutdown! (*x*)",
				TrayIcon.MessageType.ERROR);
		disposer.setLabel("Shutdown Imminent!");
		try {
			for (int i = 0; i < 10; i++) {
				Thread.sleep(1_000L);
				if (!shutdown) {
					message("Miku's Error", "Shutdown aborted! (* *)",
							TrayIcon.MessageType.ERROR);
					shutdownTimer = shutdownStart;
					Thread.sleep(2_000L);
					return;
				}
			}
			runtime.exec("shutdown -s -t 0");
		} catch (Exception e) {
			message("Miku's Error", "Shutdown failed!",
					TrayIcon.MessageType.ERROR);
			shutdownTimer = shutdownStart;
			return;
		}
		removeAll();
		System.exit(0);
	}
}
