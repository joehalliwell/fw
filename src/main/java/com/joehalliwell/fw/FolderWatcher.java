package com.joehalliwell.fw;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

public class FolderWatcher implements Runnable {
	
	static int pollPeriod = 10000;
	TrayIcon trayIcon;
	File[] folders;
	Set<File> seenFiles;
	boolean die = false;
	
	FolderWatcher(File[] folders) throws AWTException {
		this.folders = folders;

		URL url = getClass().getResource("/trayicon.png");
		ImageIcon icon = new ImageIcon(url);
		trayIcon = new TrayIcon(icon.getImage(), "FolderWatcher");
		
		PopupMenu menu = new PopupMenu("FolderWatcher");
		MenuItem quit = new MenuItem("Quit");
		quit.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Exiting");
				System.exit(0);
			}
		});
		menu.add(quit);
		trayIcon.setPopupMenu(menu);
		
		SystemTray tray = SystemTray.getSystemTray();
		tray.add(trayIcon);

		seenFiles = getFiles();
	}
	
	Set<File> getFiles() {
		Set<File> files = new HashSet<File>();
		for (File folder : folders) {
			files.addAll(Arrays.asList(folder.listFiles()));
		}
		return files;
	}
	
	void doCheck() {
		Set<File> oldFiles = seenFiles;
		seenFiles = getFiles();
		
		Set<File> newFiles = new HashSet<File>(seenFiles);
		newFiles.removeAll(oldFiles);
		if (newFiles.isEmpty()) return;
	
		// We have new files
		for (File newFile : newFiles) {
			String msg = newFile.getAbsolutePath() + " arrived";
			System.out.println(msg);
			trayIcon.displayMessage("New file!", msg,  MessageType.INFO);
		}
	}
	
	public void run() {
		while (true) {
			try {
				doCheck();
				Thread.sleep(pollPeriod);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] argv) throws AWTException {
		System.out.println("FolderWatcher (c) Joe Halliwell 2013");
		List<File> folders = new ArrayList<File>(argv.length);
		for (String path : argv) {
			File folder = new File(path);
			if (folder.exists() && folder.isDirectory()) {
				System.out.println("Watching " + path);
				folders.add(folder);
			}
			else {
				System.err.println("Ignoring " + path);
			}
		}
		System.out.println("Watching " + folders.size() + " folders");
		System.out.println("Polling every " + (pollPeriod / 1000) + " seconds");
		
		FolderWatcher fw = new FolderWatcher(folders.toArray(new File[0]));
		fw.run();
	}
}
