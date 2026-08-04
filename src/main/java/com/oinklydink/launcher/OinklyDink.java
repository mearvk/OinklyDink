package com.oinklydink.launcher;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * OinklyDink - The Pig's Tail Java Launcher
 *
 * A clean, professional cross-platform desktop launcher that exclusively
 * launches Java programs. Supports a Main class and two subscripts.
 * Works on Windows 7+, Linux, and macOS.
 *
 * "Worth $88,000,000 or a Man and his Day."
 */
public class OinklyDink extends JFrame {

    private static final String APP_TITLE = "OinklyDink - Pig's Tail Java Launcher";
    private static final String VERSION = "1.0.0";

    /** Detected operating system. */
    private static final OS CURRENT_OS = detectOS();

    // Preferences for persistence
    private final Preferences prefs = Preferences.userNodeForPackage(OinklyDink.class);

    // Animated icon panel
    private AnimatedTailPanel animatedTailPanel;

    // Main class configuration
    private JTextField mainClassField;
    private JTextField mainClasspathField;
    private JTextField mainArgsField;

    // Subscript 1 configuration
    private JTextField sub1ClassField;
    private JTextField sub1ClasspathField;
    private JTextField sub1ArgsField;

    // Subscript 2 configuration
    private JTextField sub2ClassField;
    private JTextField sub2ClasspathField;
    private JTextField sub2ArgsField;

    // JVM options
    private JTextField javaPathField;
    private JTextField jvmOptionsField;

    // Status
    private JTextArea logArea;
    private JLabel statusLabel;

    // Running processes
    private Process mainProcess;
    private Process sub1Process;
    private Process sub2Process;

    public OinklyDink() {
        super(APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(PigTailIcon.createPigTailImage(64));
        initUI();
        loadPreferences();
        setMinimumSize(new Dimension(720, 640));
        setSize(760, 700);
        setLocationRelativeTo(null);

        // Update window icon with animation
        Timer iconTimer = new Timer(80, e -> {
            if (animatedTailPanel != null) {
                setIconImage(animatedTailPanel.getCurrentFrame());
            }
        });
        iconTimer.start();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header with animated pig tail
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content - tabbed pane for launch configs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(getUIFont(Font.PLAIN, 12));

        tabbedPane.addTab("Main Class", createMainPanel());
        tabbedPane.addTab("Subscript 1", createSubscript1Panel());
        tabbedPane.addTab("Subscript 2", createSubscript2Panel());
        tabbedPane.addTab("JVM Settings", createJvmPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom panel with launch buttons and log
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // Animated 3D pig tail — sits still, wiggles on click, then launches
        animatedTailPanel = new AnimatedTailPanel();
        animatedTailPanel.setOnLaunchAction(this::launchAll);
        panel.add(animatedTailPanel, BorderLayout.WEST);

        // Title and tagline
        JPanel titlePanel = new JPanel(new GridLayout(3, 1));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(APP_TITLE);
        titleLabel.setFont(getUIFont(Font.BOLD, 16));
        titleLabel.setForeground(new Color(0xC0, 0x5E, 0x7A));

        JLabel taglineLabel = new JLabel("Click the tail to launch. Careful and comfortable.");
        taglineLabel.setFont(getUIFont(Font.ITALIC, 11));
        taglineLabel.setForeground(Color.GRAY);

        JLabel platformLabel = new JLabel("Platform: " + CURRENT_OS.displayName + " | 3D tail, wiggles on click");
        platformLabel.setFont(getUIFont(Font.PLAIN, 10));
        platformLabel.setForeground(new Color(0x99, 0x99, 0x99));

        titlePanel.add(titleLabel);
        titlePanel.add(taglineLabel);
        titlePanel.add(platformLabel);
        panel.add(titlePanel, BorderLayout.CENTER);

        // Status indicator
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(getUIFont(Font.PLAIN, 11));
        statusLabel.setForeground(new Color(0x2E, 0x7D, 0x32));
        panel.add(statusLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = createGBC();

        addSectionLabel(panel, gbc, "Main Java Class Configuration", 0);

        String cpExample = CURRENT_OS == OS.WINDOWS
                ? "e.g. .\\lib\\*;.\\bin"
                : "e.g. ./lib/*:./bin";

        mainClassField = addFieldRow(panel, gbc, "Main Class:", "e.g. com.example.Main", 1);
        mainClasspathField = addFieldRowWithBrowse(panel, gbc, "Classpath:", cpExample, 2);
        mainArgsField = addFieldRow(panel, gbc, "Arguments:", "e.g. --port 8080 --verbose", 3);

        gbc.gridy = 4;
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);

        return panel;
    }

    private JPanel createSubscript1Panel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = createGBC();

        addSectionLabel(panel, gbc, "Subscript 1 - Secondary Java Process", 0);

        sub1ClassField = addFieldRow(panel, gbc, "Main Class:", "e.g. com.example.Worker", 1);
        sub1ClasspathField = addFieldRowWithBrowse(panel, gbc, "Classpath:", "", 2);
        sub1ArgsField = addFieldRow(panel, gbc, "Arguments:", "e.g. --threads 4", 3);

        gbc.gridy = 4;
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);

        return panel;
    }

    private JPanel createSubscript2Panel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = createGBC();

        addSectionLabel(panel, gbc, "Subscript 2 - Tertiary Java Process", 0);

        sub2ClassField = addFieldRow(panel, gbc, "Main Class:", "e.g. com.example.Monitor", 1);
        sub2ClasspathField = addFieldRowWithBrowse(panel, gbc, "Classpath:", "", 2);
        sub2ArgsField = addFieldRow(panel, gbc, "Arguments:", "e.g. --interval 5000", 3);

        gbc.gridy = 4;
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);

        return panel;
    }

    private JPanel createJvmPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = createGBC();

        addSectionLabel(panel, gbc, "Java Virtual Machine Settings", 0);

        String javaPathHint;
        switch (CURRENT_OS) {
            case WINDOWS:
                javaPathHint = "e.g. C:\\Program Files\\Java\\jdk-17\\bin\\java.exe";
                break;
            case MACOS:
                javaPathHint = "e.g. /Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/java";
                break;
            default:
                javaPathHint = "e.g. /usr/lib/jvm/java-17/bin/java";
                break;
        }

        javaPathField = addFieldRowWithBrowse(panel, gbc, "Java Path:", javaPathHint, 1);
        jvmOptionsField = addFieldRow(panel, gbc, "JVM Options:", "e.g. -Xmx512m -Xms128m -ea", 2);

        // Info label
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 0, 0, 0);
        JLabel infoLabel = new JLabel(
                "<html><b>Note:</b> Leave Java Path empty to use system default (java on PATH)."
                        + "<br>Platform detected: <b>" + CURRENT_OS.displayName + "</b></html>");
        infoLabel.setFont(getUIFont(Font.PLAIN, 11));
        infoLabel.setForeground(Color.GRAY);
        panel.add(infoLabel, gbc);

        gbc.gridy = 4;
        gbc.weighty = 1.0;
        panel.add(new JLabel(), gbc);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));

        JButton launchAllBtn = createStyledButton("Launch All", new Color(0xC0, 0x5E, 0x7A));
        launchAllBtn.addActionListener(e -> launchAll());

        JButton launchMainBtn = createStyledButton("Launch Main", new Color(0x2E, 0x7D, 0x32));
        launchMainBtn.addActionListener(e -> launchMain());

        JButton launchSub1Btn = createStyledButton("Launch Sub 1", new Color(0x15, 0x65, 0xC0));
        launchSub1Btn.addActionListener(e -> launchSubscript1());

        JButton launchSub2Btn = createStyledButton("Launch Sub 2", new Color(0x6A, 0x1B, 0x9A));
        launchSub2Btn.addActionListener(e -> launchSubscript2());

        JButton stopAllBtn = createStyledButton("Stop All", new Color(0xC6, 0x28, 0x28));
        stopAllBtn.addActionListener(e -> stopAll());

        JButton saveBtn = createStyledButton("Save Config", new Color(0x55, 0x55, 0x55));
        saveBtn.addActionListener(e -> savePreferences());

        buttonPanel.add(launchAllBtn);
        buttonPanel.add(launchMainBtn);
        buttonPanel.add(launchSub1Btn);
        buttonPanel.add(launchSub2Btn);
        buttonPanel.add(stopAllBtn);
        buttonPanel.add(saveBtn);

        panel.add(buttonPanel, BorderLayout.NORTH);

        // Log area
        logArea = new JTextArea(6, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font(getMonoFontName(), Font.PLAIN, 11));
        logArea.setBackground(new Color(0xF5, 0xF5, 0xF5));
        logArea.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD)),
                "Launch Log",
                TitledBorder.LEFT, TitledBorder.TOP,
                getUIFont(Font.PLAIN, 11)));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(getUIFont(Font.BOLD, 11));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(110, 30));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    // --- Launch Methods ---

    private void launchAll() {
        log("=== Launching All Java Processes ===");
        launchMain();
        if (!sub1ClassField.getText().trim().isEmpty()) {
            launchSubscript1();
        }
        if (!sub2ClassField.getText().trim().isEmpty()) {
            launchSubscript2();
        }
    }

    private void launchMain() {
        String mainClass = mainClassField.getText().trim();
        if (mainClass.isEmpty()) {
            showError("Main class cannot be empty.");
            return;
        }
        mainProcess = launchJavaProcess("Main", mainClass,
                mainClasspathField.getText().trim(),
                mainArgsField.getText().trim());
    }

    private void launchSubscript1() {
        String className = sub1ClassField.getText().trim();
        if (className.isEmpty()) {
            showError("Subscript 1 class cannot be empty.");
            return;
        }
        sub1Process = launchJavaProcess("Subscript-1", className,
                sub1ClasspathField.getText().trim(),
                sub1ArgsField.getText().trim());
    }

    private void launchSubscript2() {
        String className = sub2ClassField.getText().trim();
        if (className.isEmpty()) {
            showError("Subscript 2 class cannot be empty.");
            return;
        }
        sub2Process = launchJavaProcess("Subscript-2", className,
                sub2ClasspathField.getText().trim(),
                sub2ArgsField.getText().trim());
    }

    /**
     * Launches a Java process in a cross-platform manner.
     * On Windows: uses cmd /c java ...
     * On Linux/macOS: uses /bin/sh -c java ... (or direct invocation)
     */
    private Process launchJavaProcess(String label, String mainClass, String classpath, String args) {
        try {
            String javaExe = javaPathField.getText().trim();
            if (javaExe.isEmpty()) {
                javaExe = "java";
            }

            // Validate it's a java executable — OinklyDink only launches Java!
            String javaExeLower = javaExe.toLowerCase();
            if (!javaExeLower.equals("java") && !javaExeLower.contains("java")) {
                showError("OinklyDink only launches Java programs!\nPath must reference a Java executable.");
                return null;
            }

            // Build command as a list for ProcessBuilder
            List<String> command = new ArrayList<>();

            if (CURRENT_OS == OS.WINDOWS) {
                command.add("cmd");
                command.add("/c");
            }

            command.add(javaExe);

            // JVM options
            String jvmOpts = jvmOptionsField.getText().trim();
            if (!jvmOpts.isEmpty()) {
                for (String opt : jvmOpts.split("\\s+")) {
                    command.add(opt);
                }
            }

            // Classpath
            if (!classpath.isEmpty()) {
                command.add("-cp");
                command.add(classpath);
            }

            // Main class
            command.add(mainClass);

            // Arguments
            if (!args.isEmpty()) {
                for (String arg : args.split("\\s+")) {
                    command.add(arg);
                }
            }

            log("[" + label + "] Launching: " + String.join(" ", command));
            setStatus("Running: " + label, new Color(0x15, 0x65, 0xC0));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            // Inherit environment
            pb.environment().putAll(System.getenv());

            Process process = pb.start();

            // Stream output to log area
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String l = line;
                        SwingUtilities.invokeLater(() -> log("[" + label + "] " + l));
                    }
                } catch (IOException ex) {
                    // Process ended or stream closed
                }
                SwingUtilities.invokeLater(() -> {
                    int exitCode = -1;
                    try {
                        exitCode = process.exitValue();
                    } catch (IllegalThreadStateException e) {
                        // still running
                    }
                    log("[" + label + "] Process ended (exit: " + exitCode + ")");
                    setStatus("Ready", new Color(0x2E, 0x7D, 0x32));
                });
            }, "OinklyDink-" + label + "-Reader").start();

            return process;

        } catch (Exception ex) {
            log("[" + label + "] ERROR: " + ex.getMessage());
            showError("Failed to launch " + label + ":\n" + ex.getMessage());
            return null;
        }
    }

    private void stopAll() {
        log("=== Stopping All Processes ===");
        stopProcess("Main", mainProcess);
        stopProcess("Subscript-1", sub1Process);
        stopProcess("Subscript-2", sub2Process);
        mainProcess = null;
        sub1Process = null;
        sub2Process = null;
        setStatus("Stopped", new Color(0xC6, 0x28, 0x28));
    }

    private void stopProcess(String label, Process process) {
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
            log("[" + label + "] Stopped.");
        }
    }

    // --- Persistence ---

    private void savePreferences() {
        prefs.put("main.class", mainClassField.getText());
        prefs.put("main.classpath", mainClasspathField.getText());
        prefs.put("main.args", mainArgsField.getText());
        prefs.put("sub1.class", sub1ClassField.getText());
        prefs.put("sub1.classpath", sub1ClasspathField.getText());
        prefs.put("sub1.args", sub1ArgsField.getText());
        prefs.put("sub2.class", sub2ClassField.getText());
        prefs.put("sub2.classpath", sub2ClasspathField.getText());
        prefs.put("sub2.args", sub2ArgsField.getText());
        prefs.put("jvm.path", javaPathField.getText());
        prefs.put("jvm.options", jvmOptionsField.getText());
        log("Configuration saved.");
    }

    private void loadPreferences() {
        mainClassField.setText(prefs.get("main.class", ""));
        mainClasspathField.setText(prefs.get("main.classpath", ""));
        mainArgsField.setText(prefs.get("main.args", ""));
        sub1ClassField.setText(prefs.get("sub1.class", ""));
        sub1ClasspathField.setText(prefs.get("sub1.classpath", ""));
        sub1ArgsField.setText(prefs.get("sub1.args", ""));
        sub2ClassField.setText(prefs.get("sub2.class", ""));
        sub2ClasspathField.setText(prefs.get("sub2.classpath", ""));
        sub2ArgsField.setText(prefs.get("sub2.args", ""));
        javaPathField.setText(prefs.get("jvm.path", ""));
        jvmOptionsField.setText(prefs.get("jvm.options", ""));
    }

    // --- UI Helpers ---

    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private void addSectionLabel(JPanel panel, GridBagConstraints gbc, String text, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        JLabel label = new JLabel(text);
        label.setFont(getUIFont(Font.BOLD, 13));
        label.setForeground(new Color(0xC0, 0x5E, 0x7A));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(label, gbc);
        gbc.gridwidth = 1;
    }

    private JTextField addFieldRow(JPanel panel, GridBagConstraints gbc, String labelText, String placeholder, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(getUIFont(Font.PLAIN, 12));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        JTextField field = new JTextField(30);
        field.setFont(getUIFont(Font.PLAIN, 12));
        field.setToolTipText(placeholder);
        panel.add(field, gbc);
        gbc.gridwidth = 1;

        return field;
    }

    private JTextField addFieldRowWithBrowse(JPanel panel, GridBagConstraints gbc, String labelText, String placeholder, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(getUIFont(Font.PLAIN, 12));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField field = new JTextField(25);
        field.setFont(getUIFont(Font.PLAIN, 12));
        field.setToolTipText(placeholder);
        panel.add(field, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton browseBtn = new JButton("...");
        browseBtn.setPreferredSize(new Dimension(30, 25));
        browseBtn.setToolTipText("Browse");
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                field.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        panel.add(browseBtn, gbc);

        return field;
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "OinklyDink Error",
                JOptionPane.ERROR_MESSAGE);
    }

    // --- Cross-Platform Helpers ---

    /**
     * Returns an appropriate UI font for the current platform.
     */
    private static Font getUIFont(int style, int size) {
        String fontName;
        switch (CURRENT_OS) {
            case MACOS:
                fontName = ".SF NS Text";
                break;
            case LINUX:
                fontName = "DejaVu Sans";
                break;
            default:
                fontName = "Segoe UI";
                break;
        }
        Font font = new Font(fontName, style, size);
        // Fallback if font not available
        if (!font.getFamily().equalsIgnoreCase(fontName) && !fontName.startsWith(".")) {
            font = new Font(Font.SANS_SERIF, style, size);
        }
        return font;
    }

    /**
     * Returns an appropriate monospace font name for the current platform.
     */
    private static String getMonoFontName() {
        switch (CURRENT_OS) {
            case MACOS:
                return "Menlo";
            case LINUX:
                return "DejaVu Sans Mono";
            default:
                return "Consolas";
        }
    }

    /**
     * Detects the current operating system.
     */
    private static OS detectOS() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        if (osName.contains("win")) {
            return OS.WINDOWS;
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            return OS.MACOS;
        } else {
            return OS.LINUX;
        }
    }

    /** Supported operating systems. */
    enum OS {
        WINDOWS("Windows"),
        LINUX("Linux"),
        MACOS("macOS");

        final String displayName;

        OS(String displayName) {
            this.displayName = displayName;
        }
    }

    // --- Entry Point ---

    public static void main(String[] args) {
        // Set platform-appropriate look and feel
        try {
            if (CURRENT_OS == OS.MACOS) {
                // macOS specific properties
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("apple.awt.application.name", "OinklyDink");
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to default cross-platform L&F
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        SwingUtilities.invokeLater(() -> {
            OinklyDink launcher = new OinklyDink();
            launcher.setVisible(true);
        });
    }
}
