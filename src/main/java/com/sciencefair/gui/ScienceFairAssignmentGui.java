package com.sciencefair.gui;

import com.sciencefair.model.ScienceProject;
import com.sciencefair.model.SlotAssignment;
import com.sciencefair.model.TableSlot;
import com.sciencefair.service.ScienceFairAssignmentService;
import com.sciencefair.util.ScienceFairCsvUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * GUI for Science Fair Table Assignment with updated CSV format
 */
public class ScienceFairAssignmentGui extends JFrame {
    private JButton openFolderButton;
    private JButton usePreviousBtn;
    // Retry button removed
    private String outputFolder;
    // Removed lastSuccessfulOutputFolder persistence
    // Removed reuse/fixed latest output folder checkboxes per user request

    private JTextField tableSlotsFileField;
    private JTextField projectsFileField;
    private JTextField outputFileField;
    private JButton runButton;
    private JButton openHtmlButton;
    private JButton layoutConfigButton; // Row grouping configuration button
    private JTextPane resultArea;
    private ScienceFairAssignmentService assignmentService;
    private JCheckBox openHtmlAfterRunCheck; // Auto-open HTML after successful run

    // Preferences for remembering last-used directories
    private final java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(ScienceFairAssignmentGui.class);
    private static final String PREF_TABLES_DIR = "lastTableSlotsDir";
    private static final String PREF_PROJECTS_DIR = "lastProjectsDir";
    private static final String PREF_ROW_GROUPS = "rowGroupsText"; // persists row grouping configuration
    
    public ScienceFairAssignmentGui() {
        this.assignmentService = new ScienceFairAssignmentService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        // Enable/disable Use Previous Input Files button based on saved paths
        boolean hasPrevTable = !prefs.get("lastTableSlotsFile", "").isEmpty();
        boolean hasPrevProjects = !prefs.get("lastProjectsFile", "").isEmpty();
        if (usePreviousBtn != null) {
            usePreviousBtn.setEnabled(hasPrevTable || hasPrevProjects);
        }
        // Defer output folder creation until runAssignment; keep placeholder
        outputFolder = null;
        outputFileField.setText("(will be created on run)");
    }
    
    private void initializeComponents() {
    setTitle("Science Fair Table Assignment");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(900, 550);
    setLocationRelativeTo(null);
        
        // Create components
        tableSlotsFileField = new JTextField(35);
        tableSlotsFileField.setBackground(Color.BLACK);
        tableSlotsFileField.setForeground(Color.WHITE);
        projectsFileField = new JTextField(35);
        projectsFileField.setBackground(Color.BLACK);
        projectsFileField.setForeground(Color.WHITE);
        outputFileField = new JTextField(35);
        openFolderButton = new JButton("Open Output Folder");
        openFolderButton.setEnabled(false);
    runButton = new JButton("Run");
    runButton.setToolTipText("Assign Projects to Table Slots");
    runButton.setBackground(new Color(220, 240, 255)); // Light blue background
    openHtmlButton = new JButton("Open Table Layout");
    openHtmlButton.setEnabled(false);
    layoutConfigButton = new JButton("Row Grouping ⚙");
    layoutConfigButton.setToolTipText("Configure row grouping (order & spacing)");
    layoutConfigButton.setEnabled(true); // Enabled at startup per new requirement
    resultArea = new JTextPane();
    resultArea.setEditable(false);
    resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    setStyledText("Please select your Table Slots and Projects CSV files to begin.\n", Color.WHITE);
    }
    
    private void appendColoredText(String text, Color color) {
        StyledDocument doc = resultArea.getStyledDocument();
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setForeground(attributes, color);
        try {
            doc.insertString(doc.getLength(), text, attributes);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private void setStyledText(String text, Color color) {
        resultArea.setText("");
        appendColoredText(text, color);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Table slots file row
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Table Slots CSV (Input 1):"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        inputPanel.add(tableSlotsFileField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JButton browseSlotsBtn = new JButton("Browse");
        browseSlotsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseForFile(tableSlotsFileField, "Select Table Slots CSV");
            }
        });
        inputPanel.add(browseSlotsBtn, gbc);
        
        // Projects file row
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Projects CSV (Input 2):"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        inputPanel.add(projectsFileField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JButton browseProjectsBtn = new JButton("Browse");
        browseProjectsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseForFile(projectsFileField, "Select Projects CSV");
            }
        });
        inputPanel.add(browseProjectsBtn, gbc);
        
    // Output folder row removed
        
    // (Removed options panel for output folder strategies)

    // Button row redesigned so Assign button is fixed at far right and always visible
    openHtmlAfterRunCheck = new JCheckBox("Open HTML after run");
    openHtmlAfterRunCheck.setSelected(true);
    openHtmlAfterRunCheck.setFocusable(false);
    usePreviousBtn = new JButton("Use Previous Input Files");

    JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    leftButtons.add(openHtmlAfterRunCheck);
    leftButtons.add(usePreviousBtn);
    leftButtons.add(openHtmlButton);
    leftButtons.add(layoutConfigButton);
    leftButtons.add(openFolderButton);

    JPanel buttonPanel = new JPanel(new BorderLayout(10,0));
    buttonPanel.add(leftButtons, BorderLayout.CENTER);
    JPanel runHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    // Add a little right padding so the Run button isn't touching the frame edge
    runHolder.setBorder(javax.swing.BorderFactory.createEmptyBorder(0,0,0,12));
    runHolder.add(runButton);
    buttonPanel.add(runHolder, BorderLayout.EAST);
        // Use Previous Input Files button handler
        usePreviousBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String lastTableFile = prefs.get("lastTableSlotsFile", "");
                String lastProjectsFile = prefs.get("lastProjectsFile", "");
                if (!lastTableFile.isEmpty()) {
                    tableSlotsFileField.setText(lastTableFile);
                }
                if (!lastProjectsFile.isEmpty()) {
                    projectsFileField.setText(lastProjectsFile);
                }
                validateInputs();
            }
        });
    gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.EAST; gbc.insets = new Insets(15, 0, 10, 0);
    inputPanel.add(buttonPanel, gbc);
        
        // Add panels to frame
        add(inputPanel, BorderLayout.NORTH);
        resultArea.setBackground(Color.BLACK);
        resultArea.setForeground(Color.WHITE);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // Add instructions at the bottom
        JTextArea instructions = new JTextArea(5, 60);
        instructions.setEditable(false);
        instructions.setBackground(Color.BLACK);
    instructions.setForeground(new Color(173, 216, 230)); // light blue
        instructions.setText("Instructions:\n" +
            "1. Select your Table Slots CSV and Projects CSV files.\n" +
            "2. The output folder is automatically created next to the JAR file and will contain both output.csv and output.html.\n" +
            "3. (Optional) Adjust row grouping via the 'Row Grouping' button.\n" +
            "4. Leave 'Open HTML after run' checked to automatically open the layout when complete.\n" +
            "5. Click 'Run' to execute the assignment (tooltip shows full action).");
        add(instructions, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runAssignment();
            }
        });
        openHtmlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile(outputFileField.getText().trim() + File.separator + "output.html");
            }
        });
        layoutConfigButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openLayoutConfigurator();
            }
        });
        openFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (outputFolder != null) {
                    openFile(outputFolder);
                }
            }
        });
    // Retry button removed

        // Validate input files whenever they are changed
        tableSlotsFileField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
        });
        projectsFileField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
        });
        validateInputs();
    }

    // Validate input file formats and enable/disable buttons accordingly
    private void validateInputs() {
        boolean validTables = isValidTableSlotsFile(tableSlotsFileField.getText().trim());
        boolean validProjects = isValidProjectsFile(projectsFileField.getText().trim());
        boolean enable = validTables && validProjects;
        runButton.setEnabled(enable);
        openHtmlButton.setEnabled(false);
        openFolderButton.setEnabled(false);

        resultArea.setText("");
        // Table Slots file status
        if (tableSlotsFileField.getText().trim().isEmpty()) {
            appendColoredText("Table Slots file: Not selected.\n", Color.WHITE);
        } else if (!validTables) {
            appendColoredText("Table Slots file: ", Color.WHITE);
            appendColoredText("Invalid format.\n", Color.RED);
        } else {
            appendColoredText("Table Slots file: ", Color.WHITE);
            appendColoredText("Valid.\n", Color.GREEN);
        }

        // Projects file status
        if (projectsFileField.getText().trim().isEmpty()) {
            appendColoredText("Projects file: Not selected.\n", Color.WHITE);
        } else if (!validProjects) {
            appendColoredText("Projects file: ", Color.WHITE);
            appendColoredText("Invalid format.\n", Color.RED);
        } else {
            appendColoredText("Projects file: ", Color.WHITE);
            appendColoredText("Valid.\n", Color.GREEN);
        }

        if (enable) {
            appendColoredText("Both files are valid. Click 'Assign Projects to Table Slots' to continue.\n", Color.GREEN);
        }
    }

    // Check if table slots file has required headers
    private boolean isValidTableSlotsFile(String path) {
        if (path.isEmpty() || !(new java.io.File(path).exists())) return false;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(path))) {
            String header = reader.readLine();
            if (header == null) return false;
            return header.contains("Row") && header.contains("rowNumSlots") && header.contains("tableSlotID") && header.contains("isReserved");
        } catch (Exception e) { return false; }
    }

    // Check if projects file has required headers
    private boolean isValidProjectsFile(String path) {
        if (path.isEmpty() || !(new java.io.File(path).exists())) return false;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(path))) {
            String header = reader.readLine();
            if (header == null) return false;
            return header.contains("projectID") && header.contains("isTeam") && header.contains("isFirstInCat") && header.contains("Category");
        } catch (Exception e) { return false; }
    }
    
    private void browseForFile(JTextField textField, String title) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(title);
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        // Set default directory from preferences if available
        try {
            String prefDir = null;
            if (textField == tableSlotsFileField) {
                prefDir = prefs.get(PREF_TABLES_DIR, null);
            } else if (textField == projectsFileField) {
                prefDir = prefs.get(PREF_PROJECTS_DIR, null);
            }
            if (prefDir != null) {
                File dir = new File(prefDir);
                if (dir.exists() && dir.isDirectory()) {
                    fileChooser.setCurrentDirectory(dir);
                }
            } else {
                // fallback: use other file's location if available
                String otherPath = null;
                if (textField == projectsFileField && tableSlotsFileField.getText() != null && !tableSlotsFileField.getText().isEmpty()) {
                    otherPath = tableSlotsFileField.getText();
                } else if (textField == tableSlotsFileField && projectsFileField.getText() != null && !projectsFileField.getText().isEmpty()) {
                    otherPath = projectsFileField.getText();
                }
                if (otherPath != null) {
                    File otherFile = new File(otherPath);
                    File parentDir = otherFile.getParentFile();
                    if (parentDir != null && parentDir.exists()) {
                        fileChooser.setCurrentDirectory(parentDir);
                    }
                } else {
                    String jarPath = ScienceFairAssignmentGui.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                    File jarDir = new File(jarPath).getParentFile();
                    if (jarDir != null && jarDir.exists()) {
                        fileChooser.setCurrentDirectory(jarDir);
                    }
                }
            }
        } catch (Exception e) {
            // fallback: use default
        }
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textField.setText(selectedFile.getAbsolutePath());
            // Save the directory to preferences
            String selectedDir = selectedFile.getParent();
            if (textField == tableSlotsFileField) {
                prefs.put(PREF_TABLES_DIR, selectedDir);
                prefs.put("lastTableSlotsFile", selectedFile.getAbsolutePath());
            } else if (textField == projectsFileField) {
                prefs.put(PREF_PROJECTS_DIR, selectedDir);
                prefs.put("lastProjectsFile", selectedFile.getAbsolutePath());
            }
        }
    }
    
    private void browseForOutputFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Assignment Results");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.setSelectedFile(new File("assignment_results.csv"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".csv")) {
                path += ".csv";
            }
            outputFileField.setText(path);
        }
    }
    
    private void runAssignment() {
    String tableSlotsFile = tableSlotsFileField.getText().trim();
    String projectsFile = projectsFileField.getText().trim();
    // Output folder will be created only after successful assignment
        
        // Validate inputs
        if (tableSlotsFile.isEmpty() || projectsFile.isEmpty()) {
            showError("Please select all required files.");
            return;
        }
        if (!new File(tableSlotsFile).exists()) {
            showError("Table slots file does not exist: " + tableSlotsFile);
            return;
        }
        if (!new File(projectsFile).exists()) {
            showError("Projects file does not exist: " + projectsFile);
            return;
        }
    // Output folder will be selected and created after successful assignment processing
        
    // Disable buttons during processing
    runButton.setEnabled(false);
    openHtmlButton.setEnabled(false);
    setStyledText("Processing assignment...\n", Color.WHITE);
        
        // Run assignment in background thread
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    publish("Reading table slots from: " + tableSlotsFile);
                    List<TableSlot> tableSlots = ScienceFairCsvUtil.readTableSlots(tableSlotsFile);
                    publish("Loaded " + tableSlots.size() + " table slots");
                    
                    publish("Reading projects from: " + projectsFile);
                    List<ScienceProject> projects = ScienceFairCsvUtil.readScienceProjects(projectsFile);
                    publish("Loaded " + projects.size() + " projects");
                    
                    publish("Running assignment algorithm...");
                    List<SlotAssignment> assignments = assignmentService.assignProjectsToSlots(projects, tableSlots);
                    
                    // Always create a fresh timestamped output folder
                    String jarPath = new File(System.getProperty("java.class.path")).getAbsoluteFile().getParent();
                    String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                    outputFolder = jarPath + File.separator + "ScienceFairOutput_" + timestamp;
                    File outDir = new File(outputFolder);
                    if (!outDir.exists()) {
                        outDir.mkdirs();
                    }
                    String outputFile = outputFolder + File.separator + "output.csv";
                    publish("Writing results to: " + outputFile);
                    ScienceFairCsvUtil.writeSlotAssignments(assignments, outputFile);
                    String htmlFile = outputFolder + File.separator + "output.html";
                    // Apply persisted grouping layout if available; otherwise fall back to legacy pair spacing
                    try {
                        String groupingText = prefs.get(PREF_ROW_GROUPS, "").trim();
                        if (!groupingText.isEmpty()) {
                            GroupingLayout layout = buildGroupingLayout(groupingText, assignments);
                            if (layout != null && layout.orderedRows != null && !layout.orderedRows.isEmpty()) {
                                com.sciencefair.ScienceFairTableAssignmentApp.generateHtmlLayout(assignments, htmlFile, layout.orderedRows, false, layout.marginMap);
                            } else {
                                // Fallback if parsing produced nothing
                                // fallback to default style (no custom margins, enable pair spacing)
                                com.sciencefair.ScienceFairTableAssignmentApp.generateHtmlLayout(assignments, htmlFile, null, true, null);
                            }
                        } else {
                            com.sciencefair.ScienceFairTableAssignmentApp.generateHtmlLayout(assignments, htmlFile, null, true, null);
                        }
                    } catch (Exception gx) {
                        publish("Warning: Failed to apply custom grouping. Using default layout. Reason: " + gx.getMessage());
                        com.sciencefair.ScienceFairTableAssignmentApp.generateHtmlLayout(assignments, htmlFile, null, true, null);
                    }
                    publish("HTML results saved to: " + htmlFile);
                    publish("\n" + assignmentService.generateAssignmentSummary(assignments, projects, tableSlots));
                    publish("\nAssignment completed successfully!");
                    publish("Results saved to folder: " + outputFolder);
                    
                } catch (IOException e) {
                    publish("Error: " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    publish("Unexpected error: " + e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    if (message.contains("Assignment completed successfully!")) {
                        appendColoredText(message + "\n", Color.GREEN.brighter());
                    } else {
                        appendColoredText(message + "\n", Color.WHITE);
                    }
                }
                resultArea.setCaretPosition(resultArea.getDocument().getLength());
            }
            
            @Override
            protected void done() {
                runButton.setEnabled(true);
                if (outputFolder != null) {
                    outputFileField.setText(outputFolder);
                    openHtmlButton.setEnabled(true);
                    openFolderButton.setEnabled(true);
                    layoutConfigButton.setEnabled(true);
                    // Auto-open output.html if user opted in
                    if (openHtmlAfterRunCheck != null && openHtmlAfterRunCheck.isSelected()) {
                        try {
                            openFile(outputFolder + File.separator + "output.html");
                        } catch (Exception ignore) { /* silent */ }
                    }
                } else {
                    openHtmlButton.setEnabled(false);
                    openFolderButton.setEnabled(false);
                    layoutConfigButton.setEnabled(false);
                }
            }
        };
        
        worker.execute();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void openFile(String filePath) {
        try {
            Desktop.getDesktop().open(new File(filePath));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Unable to open file: " + filePath, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Opens a dialog allowing the user to:
     *  - Reorder rows (Up/Down)
     *  - Toggle pair spacing (tight gaps between paired rows 2-3, 4-5, ...)
     * Generates a custom preview HTML (layout_custom.html) in the output folder.
     */
    private void openLayoutConfigurator() {
        // Removed 'No Output Yet' dialog per user request; allow configuration anytime.
        final String currentOutputFolder = outputFolder; // capture for lambda
        File csvFile = currentOutputFolder == null ? null : new File(currentOutputFolder + File.separator + "output.csv");
        List<com.sciencefair.model.SlotAssignment> assignments;
        try {
            if (csvFile != null && csvFile.exists()) {
                assignments = com.sciencefair.util.ScienceFairCsvUtil.readSlotAssignments(csvFile.getAbsolutePath());
            } else {
                assignments = java.util.Collections.emptyList();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to read assignments: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            assignments = java.util.Collections.emptyList();
        }
        // Grouping-only UI (row list & pair spacing removed)
        JLabel groupingLabel = new JLabel("Row Groups (e.g. [1,2] [3,4] [5,6,7]):");
        JTextArea groupingArea = new JTextArea(4, 40);
        groupingArea.setLineWrap(true);
        groupingArea.setWrapStyleWord(true);
        String savedGroups = prefs.get(PREF_ROW_GROUPS, "").trim();
        if (!savedGroups.isEmpty()) {
            groupingArea.setText(savedGroups);
        } else {
            groupingArea.setText("[1,2] [3,4] [5,6,7]");
        }
        JScrollPane groupingScroll = new JScrollPane(groupingArea);
        JLabel infoLabel = new JLabel("Order & spacing derived from groups. Small gap within a group, large gap between groups.");
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.ITALIC, 11f));

    JButton previewBtn = new JButton("Generate Preview");
    JButton saveBtn = new JButton("Save");
    JButton resetBtn = new JButton("Reset to Default");
    JButton closeBtn = new JButton("Close");
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(groupingLabel);
        contentPanel.add(Box.createVerticalStrut(4));
        contentPanel.add(groupingScroll);
        contentPanel.add(Box.createVerticalStrut(8));
    contentPanel.add(infoLabel);
    JLabel validationLabel = new JLabel(" ");
    validationLabel.setForeground(Color.RED);
    contentPanel.add(Box.createVerticalStrut(4));
    contentPanel.add(validationLabel);

    JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonsPanel.add(resetBtn);
    buttonsPanel.add(previewBtn);
    buttonsPanel.add(saveBtn);
    buttonsPanel.add(closeBtn);

        JDialog dialog = new JDialog(this, "Layout Configuration", true);
        dialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

    final java.util.List<com.sciencefair.model.SlotAssignment> assignmentsForPreview = assignments; // effectively final
    final JTextArea groupingAreaRef = groupingArea; // effectively final reference
    // Disable preview if no output folder yet (per requirement)
    if (currentOutputFolder == null) {
        previewBtn.setEnabled(false);
        previewBtn.setToolTipText("Run an assignment first to enable preview.");
    }
    // Live validation setup
    javax.swing.event.DocumentListener groupingValidator = new javax.swing.event.DocumentListener() {
        private void reval() {
            ValidationResult vr = validateGrouping(groupingAreaRef.getText(), assignmentsForPreview);
            if (!vr.valid) {
                previewBtn.setEnabled(false);
                saveBtn.setEnabled(false);
                validationLabel.setText(vr.message);
                previewBtn.setToolTipText("Fix grouping format to enable preview.");
            } else if (currentOutputFolder == null) {
                validationLabel.setText(" ");
                previewBtn.setEnabled(false);
                saveBtn.setEnabled(true); // can still save valid grouping even without preview
                previewBtn.setToolTipText("Run an assignment first to enable preview.");
            } else {
                validationLabel.setText(" ");
                previewBtn.setEnabled(true);
                saveBtn.setEnabled(true);
                previewBtn.setToolTipText("Generate preview with current grouping.");
            }
        }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { reval(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { reval(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { reval(); }
    };
    groupingAreaRef.getDocument().addDocumentListener(groupingValidator);
    // Initial validation pass
    groupingValidator.insertUpdate(null);
    previewBtn.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ev) {
            GroupingLayout layout = buildGroupingLayout(groupingAreaRef.getText().trim(), assignmentsForPreview);
            if (currentOutputFolder == null) {
                JOptionPane.showMessageDialog(dialog, "No assignment output yet. Run assignment to generate preview files.", "No Data", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String customHtml = currentOutputFolder + File.separator + "layout_custom.html";
            try {
                if (layout != null && layout.orderedRows != null && !layout.orderedRows.isEmpty()) {
                    com.sciencefair.ScienceFairTableAssignmentApp.generateHtmlLayout(assignmentsForPreview, customHtml, layout.orderedRows, false, layout.marginMap);
                } else {
                    com.sciencefair.ScienceFairTableAssignmentApp.generateHtmlLayout(assignmentsForPreview, customHtml, null, true, null);
                }
                openFile(customHtml);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to generate custom layout: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    });
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = groupingAreaRef.getText().trim();
                prefs.put(PREF_ROW_GROUPS, text);
                // Removed confirmation dialog per user request; silently persist then close
                dialog.dispose();
            }
        });
        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                groupingAreaRef.setText("[1][2,3][4,5][6,7][8,9]");
            }
        });
        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);
    }

    // Validation result helper
    private static class ValidationResult { boolean valid; String message; }

    /**
     * Validates grouping text ensuring:
     *  - Only bracketed groups allowed (every number must appear inside [ ])
     *  - No trailing commas or empty entries in groups
     *  - All existing assignment rows are accounted for exactly once
     */
    private ValidationResult validateGrouping(String text, java.util.List<com.sciencefair.model.SlotAssignment> assignments) {
        ValidationResult vr = new ValidationResult();
        vr.valid = true; vr.message = "";
        if (text == null) text = "";
        text = text.trim();
        if (text.isEmpty()) {
            vr.valid = false; vr.message = "Grouping cannot be empty."; return vr;
        }
        // All tokens must be bracketed; detect any digits outside brackets
        String outside = text.replaceAll("\\[(?:[^\\]]*)\\]", " ");
        if (outside.matches(".*\\d+.*")) {
            vr.valid = false; vr.message = "All row numbers must be inside square brackets."; return vr;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\[(.*?)\\]").matcher(text);
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        int groupCount = 0;
        while (m.find()) {
            groupCount++;
            String inside = m.group(1).trim();
            if (inside.isEmpty()) { vr.valid = false; vr.message = "Empty group [] detected."; return vr; }
            String[] parts = inside.split(",");
            for (String p : parts) {
                String trimmed = p.trim();
                if (trimmed.isEmpty()) { vr.valid = false; vr.message = "Trailing or double comma in group: ["+inside+"]"; return vr; }
                try {
                    int row = Integer.parseInt(trimmed);
                    if (!seen.add(row)) { vr.valid = false; vr.message = "Row " + row + " listed more than once."; return vr; }
                } catch (NumberFormatException nfe) {
                    vr.valid = false; vr.message = "Non-numeric entry '"+trimmed+"'."; return vr; }
            }
        }
        if (groupCount == 0) { vr.valid = false; vr.message = "No valid [..] groups found."; return vr; }
        if (assignments != null && !assignments.isEmpty()) {
            java.util.Set<Integer> allRows = new java.util.TreeSet<>();
            for (com.sciencefair.model.SlotAssignment a : assignments) allRows.add(a.getRow());
            if (!seen.equals(allRows)) {
                java.util.Set<Integer> missing = new java.util.TreeSet<>(allRows); missing.removeAll(seen);
                java.util.Set<Integer> extra = new java.util.TreeSet<>(seen); extra.removeAll(allRows);
                if (!missing.isEmpty()) { vr.valid = false; vr.message = "Missing rows: " + missing; return vr; }
                // Extra (unknown) rows are ignored per new requirement
            }
        }
        return vr;
    }

    /** Container for parsed grouping layout data */
    private static class GroupingLayout {
        java.util.List<Integer> orderedRows;
        java.util.Map<Integer,Integer> marginMap;
    }

    /**
     * Parses grouping text like: [1,2] [3,4] [5,6,7]
     * Builds ordered row list and per-row margin-top map.
     * Returns null if parsing yields no rows (to allow fallback behavior).
     */
    private GroupingLayout buildGroupingLayout(String groupingText, java.util.List<com.sciencefair.model.SlotAssignment> assignments) {
        if (groupingText == null) groupingText = "";
        groupingText = groupingText.trim();
        if (groupingText.isEmpty()) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\[(.*?)\\]").matcher(groupingText);
        java.util.List<java.util.List<Integer>> groups = new java.util.ArrayList<>();
        while (m.find()) {
            String inside = m.group(1).trim();
            if (!inside.isEmpty()) {
                java.util.List<Integer> g = new java.util.ArrayList<>();
                for (String part : inside.split(",")) {
                    try { g.add(Integer.parseInt(part.trim())); } catch (NumberFormatException ignore) {}
                }
                if (!g.isEmpty()) groups.add(g);
            }
        }
        java.util.List<Integer> orderedRows = new java.util.ArrayList<>();
        java.util.Map<Integer,Integer> marginMap = new java.util.HashMap<>();
    // Adjusted gap sizes for clearer visual distinction between rows within a group vs between groups
    final int smallGap = 2;   // was 4
    final int largeGap = 48;  // was 32
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        Integer prev = null;
        for (java.util.List<Integer> g : groups) {
            for (int ri=0; ri<g.size(); ri++) {
                int row = g.get(ri);
                if (prev == null) {
                    marginMap.put(row, 0);
                } else {
                    if (ri == 0) {
                        marginMap.put(row, largeGap);
                    } else {
                        marginMap.put(row, smallGap);
                    }
                }
                prev = row; seen.add(row);
                if (!orderedRows.contains(row)) orderedRows.add(row);
            }
        }
        if (assignments != null && !assignments.isEmpty()) {
            java.util.Set<Integer> allRows = new java.util.TreeSet<>();
            for (com.sciencefair.model.SlotAssignment a : assignments) allRows.add(a.getRow());
            for (Integer r : allRows) if (!seen.contains(r)) {
                marginMap.put(r, prev==null?0:largeGap);
                prev = r;
                orderedRows.add(r);
            }
        }
        if (orderedRows.isEmpty()) return null; // invalid grouping
        GroupingLayout gl = new GroupingLayout();
        gl.orderedRows = orderedRows;
        gl.marginMap = marginMap;
        return gl;
    }
    
    public static void main(String[] args) {
        // Set modern look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to default
        }
        // Create and show GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ScienceFairAssignmentGui().setVisible(true);
            }
        });
    }
}