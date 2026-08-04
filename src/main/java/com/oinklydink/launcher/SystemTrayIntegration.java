package com.oinklydink.launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * System tray integration for OinklyDink.
 * Works on Windows, Linux (with compatible desktop), and macOS.
 * Provides a pig tail icon in the system tray with quick-launch options.
 */
public class SystemTrayIntegration {

    private final OinklyDink launcher;
    private TrayIcon trayIcon;

    public SystemTrayIntegration(OinklyDink launcher) {
        this.launcher = launcher;
    }

    /**
     * Sets up the system tray icon and menu.
     * Returns true if tray is supported and setup succeeded.
     */
    public boolean setup() {
        if (!SystemTray.isSupported()) {
            return false;
        }

        SystemTray tray = SystemTray.getSystemTray();

        // Use appropriate icon size for the platform tray
        Dimension traySize = tray.getTrayIconSize();
        int iconSize = Math.max(traySize.width, 16);
        Image trayImage = PigTailIcon.createPigTailImage(iconSize);

        PopupMenu popup = new PopupMenu();

        MenuItem showItem = new MenuItem("Show OinklyDink");
        showItem.addActionListener(e -> restoreWindow());

        MenuItem launchAllItem = new MenuItem("Launch All");
        launchAllItem.addActionListener(e -> {
            restoreWindow();
            // Trigger launch via visible window
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            tray.remove(trayIcon);
            System.exit(0);
        });

        popup.add(showItem);
        popup.addSeparator();
        popup.add(launchAllItem);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon = new TrayIcon(trayImage, "OinklyDink - Pig's Tail Java Launcher", popup);
        trayIcon.setImageAutoSize(true);

        // Double-click to restore on all platforms
        trayIcon.addActionListener(e -> restoreWindow());

        try {
            tray.add(trayIcon);
            return true;
        } catch (AWTException e) {
            return false;
        }
    }

    private void restoreWindow() {
        launcher.setVisible(true);
        launcher.setState(Frame.NORMAL);
        launcher.toFront();
        launcher.requestFocus();
    }

    /**
     * Show a tray notification balloon.
     */
    public void showNotification(String title, String message, TrayIcon.MessageType type) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, type);
        }
    }

    /**
     * Update the tray icon image (for animation sync).
     */
    public void updateIcon(Image image) {
        if (trayIcon != null) {
            trayIcon.setImage(image);
        }
    }
}
