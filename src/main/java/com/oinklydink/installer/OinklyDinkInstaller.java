package com.oinklydink.installer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * OinklyDink Cross-Platform Java Installer
 *
 * A simple, elegant installer that works on Windows, Linux, and macOS.
 * Copies the launcher JAR and platform-appropriate scripts to the
 * user's chosen install location.
 *
 * "Worth $88,000,000 or a Man and his Day."
 */
public class OinklyDinkInstaller extends JFrame {

    private static final String APP_NAME = "OinklyDink";
    private static final String VERSION = "1.0.0";
    private static final String JAR_NAME = "oinklydink-launcher-" + VERSION + ".jar";

    private static final OS CURRENT_OS = detectOS();

    private JTextField installPathField;
    private JTextArea logArea;
    private JButton installButton;
    private JProgressBar progressBar;

    public OinklyDinkInstaller() {
        super("OinklyDink Installer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        initUI();
        setSize(520, 420);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(new EmptyBorder(16, 20, 16, 20));
        setContentPane(root);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Install OinklyDink \u2014 Pig\u2019s Tail Java Launcher");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        title.setForeground(new Color(0xC0, 0x5E, 0x7A));
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("<html>Version " + VERSION + " &bull; " + CURRENT_OS.displayName + " detected</html>");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        subtitle.setForeground(Color.GRAY);
        header.add(subtitle, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        // Center - path selection
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));

        JPanel pathPanel = new JPanel(new BorderLayout(6, 0));
        pathPanel.setBorder(BorderFactory.createTitledBorder("Install Location"));

        installPathField = new JTextField(getDefaultInstallPath());
        installPathField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        pathPanel.add(installPathField, BorderLayout.CENTER);

        JButton browseBtn = new JButton("Browse...");
        browseBtn.addActionListener(e -> browseInstallPath());
        pathPanel.add(browseBtn, BorderLayout.EAST);

        centerPanel.add(pathPanel, BorderLayout.NORTH);

        // Log
        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        logArea.setBackground(new Color(0xF8, 0xF8, 0xF8));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Progress"));
        centerPanel.add(scroll, BorderLayout.CENTER);

        root.add(centerPanel, BorderLayout.CENTER);

        // Bottom - buttons and progress
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 8));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        bottomPanel.add(progressBar, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        installButton = new JButton("Install");
        installButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        installButton.addActionListener(this::doInstall);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> System.exit(0));

        buttonPanel.add(cancelBtn);
        buttonPanel.add(installButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        root.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void doInstall(ActionEvent evt) {
        installButton.setEnabled(false);
        String installPath = installPathField.getText().trim();

        new Thread(() -> {
            try {
                install(installPath);
            } catch (Exception ex) {
                log("ERROR: " + ex.getMessage());
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Installation failed:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    installButton.setEnabled(true);
                });
            }
        }, "Installer-Thread").start();
    }

    private void install(String installPath) throws Exception {
        Path destDir = Paths.get(installPath);

        log("Installing OinklyDink to: " + destDir);
        progress(5);

        // Create install directory
        Files.createDirectories(destDir);
        log("Created directory: " + destDir);
        progress(15);

        // Copy the launcher JAR
        Path sourceJar = findSourceJar();
        Path destJar = destDir.resolve(JAR_NAME);
        Files.copy(sourceJar, destJar, StandardCopyOption.REPLACE_EXISTING);
        log("Copied: " + JAR_NAME);
        progress(50);

        // Write platform-specific launcher scripts
        switch (CURRENT_OS) {
            case WINDOWS:
                writeWindowsBat(destDir, destJar);
                progress(70);
                writeWindowsUninstaller(destDir);
                progress(80);
                break;
            case MACOS:
                writeMacScript(destDir, destJar);
                progress(70);
                writeMacUninstaller(destDir);
                progress(80);
                break;
            case LINUX:
                writeLinuxScript(destDir, destJar);
                progress(70);
                writeDesktopEntry(destDir, destJar);
                progress(75);
                writeLinuxUninstaller(destDir);
                progress(80);
                break;
        }

        progress(90);
        log("---");
        log("Installation complete!");
        log("Location: " + destDir);
        progress(100);

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "OinklyDink installed successfully!\n\n" + getLaunchInstructions(destDir),
                    "Installation Complete", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // --- Platform-specific file generation ---

    private void writeWindowsBat(Path destDir, Path jarPath) throws IOException {
        String bat = "@echo off\r\n"
                + "title OinklyDink\r\n"
                + "java -jar \"" + jarPath.toString() + "\" %*\r\n";
        Path batFile = destDir.resolve("OinklyDink.bat");
        Files.writeString(batFile, bat);
        log("Created: OinklyDink.bat");
    }

    private void writeWindowsUninstaller(Path destDir) throws IOException {
        String bat = "@echo off\r\n"
                + "echo Uninstalling OinklyDink...\r\n"
                + "del /q \"" + destDir.resolve(JAR_NAME) + "\"\r\n"
                + "del /q \"" + destDir.resolve("OinklyDink.bat") + "\"\r\n"
                + "echo Done. You may delete this folder manually.\r\n"
                + "del /q \"%~f0\"\r\n";
        Files.writeString(destDir.resolve("uninstall.bat"), bat);
        log("Created: uninstall.bat");
    }

    private void writeMacScript(Path destDir, Path jarPath) throws IOException {
        String script = "#!/bin/bash\n"
                + "# OinklyDink Launcher\n"
                + "exec java -jar \"" + jarPath.toString() + "\" \"$@\"\n";
        Path scriptFile = destDir.resolve("oinklydink");
        Files.writeString(scriptFile, script);
        makeExecutable(scriptFile);
        log("Created: oinklydink (executable)");
    }

    private void writeMacUninstaller(Path destDir) throws IOException {
        String script = "#!/bin/bash\n"
                + "echo \"Uninstalling OinklyDink...\"\n"
                + "rm -f \"" + destDir.resolve(JAR_NAME) + "\"\n"
                + "rm -f \"" + destDir.resolve("oinklydink") + "\"\n"
                + "echo \"Done. You may delete " + destDir + " manually.\"\n"
                + "rm -f \"$0\"\n";
        Path uninstall = destDir.resolve("uninstall.sh");
        Files.writeString(uninstall, script);
        makeExecutable(uninstall);
        log("Created: uninstall.sh");
    }

    private void writeLinuxScript(Path destDir, Path jarPath) throws IOException {
        String script = "#!/bin/bash\n"
                + "# OinklyDink - Pig's Tail Java Launcher\n"
                + "exec java -jar \"" + jarPath.toString() + "\" \"$@\"\n";
        Path scriptFile = destDir.resolve("oinklydink");
        Files.writeString(scriptFile, script);
        makeExecutable(scriptFile);
        log("Created: oinklydink (executable)");
    }

    private void writeDesktopEntry(Path destDir, Path jarPath) throws IOException {
        String desktop = "[Desktop Entry]\n"
                + "Version=1.0\n"
                + "Type=Application\n"
                + "Name=OinklyDink\n"
                + "Comment=Pig's Tail Java Launcher\n"
                + "Exec=java -jar \"" + jarPath.toString() + "\"\n"
                + "Terminal=false\n"
                + "Categories=Development;Java;\n";

        // Write to install dir
        Path desktopFile = destDir.resolve("oinklydink.desktop");
        Files.writeString(desktopFile, desktop);
        log("Created: oinklydink.desktop");

        // Try to install to user's applications folder
        Path userApps = Paths.get(System.getProperty("user.home"),
                ".local", "share", "applications");
        try {
            Files.createDirectories(userApps);
            Files.copy(desktopFile, userApps.resolve("oinklydink.desktop"),
                    StandardCopyOption.REPLACE_EXISTING);
            log("Registered in application menu.");
        } catch (Exception e) {
            log("Note: Could not register in app menu (copy manually).");
        }
    }

    private void writeLinuxUninstaller(Path destDir) throws IOException {
        String script = "#!/bin/bash\n"
                + "echo \"Uninstalling OinklyDink...\"\n"
                + "rm -f \"" + destDir.resolve(JAR_NAME) + "\"\n"
                + "rm -f \"" + destDir.resolve("oinklydink") + "\"\n"
                + "rm -f \"" + destDir.resolve("oinklydink.desktop") + "\"\n"
                + "rm -f \"$HOME/.local/share/applications/oinklydink.desktop\"\n"
                + "echo \"Done.\"\n"
                + "rm -f \"$0\"\n";
        Path uninstall = destDir.resolve("uninstall.sh");
        Files.writeString(uninstall, script);
        makeExecutable(uninstall);
        log("Created: uninstall.sh");
    }

    // --- Helpers ---

    private Path findSourceJar() throws FileNotFoundException {
        // Look for the JAR in common locations relative to this installer
        String[] candidates = {
                JAR_NAME,
                "target/" + JAR_NAME,
                "../" + JAR_NAME,
                "../target/" + JAR_NAME,
        };

        // Also check the directory containing this class/jar
        String classPath = System.getProperty("java.class.path", "");
        for (String cp : classPath.split(File.pathSeparator)) {
            if (cp.endsWith(".jar")) {
                Path jarDir = Paths.get(cp).getParent();
                if (jarDir != null) {
                    Path candidate = jarDir.resolve(JAR_NAME);
                    if (Files.exists(candidate)) {
                        return candidate;
                    }
                }
                // The installer jar itself might contain the launcher
                // or BE the launcher
                if (cp.contains("oinklydink")) {
                    return Paths.get(cp);
                }
            }
        }

        for (String candidate : candidates) {
            Path p = Paths.get(candidate);
            if (Files.exists(p)) {
                return p;
            }
        }

        // Try current working directory
        Path cwd = Paths.get(".").toAbsolutePath().normalize();
        Path cwdJar = cwd.resolve(JAR_NAME);
        if (Files.exists(cwdJar)) {
            return cwdJar;
        }

        throw new FileNotFoundException(
                "Cannot find " + JAR_NAME + ". Please place it in the same directory as the installer.");
    }

    private void makeExecutable(Path file) {
        try {
            Set<PosixFilePermission> perms = new HashSet<>(Files.getPosixFilePermissions(file));
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            Files.setPosixFilePermissions(file, perms);
        } catch (Exception e) {
            // Windows doesn't support POSIX permissions - that's fine
        }
    }

    private String getDefaultInstallPath() {
        switch (CURRENT_OS) {
            case WINDOWS:
                String appData = System.getenv("LOCALAPPDATA");
                if (appData == null) appData = System.getProperty("user.home") + "\\AppData\\Local";
                return appData + "\\OinklyDink";
            case MACOS:
                return System.getProperty("user.home") + "/Applications/OinklyDink";
            default:
                return System.getProperty("user.home") + "/.local/opt/oinklydink";
        }
    }

    private String getLaunchInstructions(Path destDir) {
        switch (CURRENT_OS) {
            case WINDOWS:
                return "Double-click OinklyDink.bat in:\n" + destDir;
            case MACOS:
                return "Run: " + destDir.resolve("oinklydink");
            default:
                return "Run: " + destDir.resolve("oinklydink")
                        + "\nOr find OinklyDink in your application menu.";
        }
    }

    private void browseInstallPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose Install Location");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(new File(installPathField.getText()));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            installPathField.setText(
                    chooser.getSelectedFile().getAbsolutePath() + File.separator + "OinklyDink");
        }
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void progress(int pct) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(pct));
    }

    private static OS detectOS() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (osName.contains("win")) return OS.WINDOWS;
        if (osName.contains("mac") || osName.contains("darwin")) return OS.MACOS;
        return OS.LINUX;
    }

    enum OS {
        WINDOWS("Windows"), LINUX("Linux"), MACOS("macOS");
        final String displayName;
        OS(String d) { this.displayName = d; }
    }

    // --- Entry Point ---

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            OinklyDinkInstaller installer = new OinklyDinkInstaller();
            installer.setVisible(true);
        });
    }
}
