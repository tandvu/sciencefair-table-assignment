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
    private String outputFolder;
    
    private JTextField tableSlotsFileField;
    private JTextField projectsFileField;
    private JTextField outputFileField;
    private JButton runButton;
    private JButton openHtmlButton;
    private JTextPane resultArea;
    private ScienceFairAssignmentService assignmentService;
    
    public ScienceFairAssignmentGui() {
    this.assignmentService = new ScienceFairAssignmentService();
    initializeComponents();
    setupLayout();
    setupEventHandlers();
        // Set default output location to JAR directory, timestamped folder
        String jarPath = new File(System.getProperty("java.class.path")).getAbsoluteFile().getParent();
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        outputFolder = jarPath + File.separator + "ScienceFairOutput_" + timestamp;
        new File(outputFolder).mkdirs();
        outputFileField.setText(outputFolder);
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
    runButton = new JButton("Assign Projects to Table Slots");
    runButton.setBackground(new Color(220, 240, 255)); // Light blue background
    openHtmlButton = new JButton("Open Table Layout");
    openHtmlButton.setEnabled(false);
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
        browseSlotsBtn.addActionListener(e -> browseForFile(tableSlotsFileField, "Select Table Slots CSV"));
        inputPanel.add(browseSlotsBtn, gbc);
        
        // Projects file row
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Projects CSV (Input 2):"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        inputPanel.add(projectsFileField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JButton browseProjectsBtn = new JButton("Browse");
        browseProjectsBtn.addActionListener(e -> browseForFile(projectsFileField, "Select Projects CSV"));
        inputPanel.add(browseProjectsBtn, gbc);
        
    // Output folder row removed
        
    // Button row: all buttons aligned to the far right
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    buttonPanel.add(openHtmlButton);
    buttonPanel.add(openFolderButton);
    buttonPanel.add(runButton);
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
            "3. Click 'Assign Projects to Table Slots' to run the assignment.\n" +
            "4. Use the buttons to open the output files directly after a successful run.");
        add(instructions, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runAssignment();
            }
        });
        openHtmlButton.addActionListener(e -> openFile(outputFileField.getText().trim() + File.separator + "output.html"));
        openFolderButton.addActionListener(e -> {
            if (outputFolder != null) {
                openFile(outputFolder);
            }
        });

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
        // Set default directory to the other file's location if available, else JAR location
        try {
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
        } catch (Exception e) {
            // fallback: use default
        }
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
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
    // Create a new timestamped output folder for each run
    String jarPath = new File(System.getProperty("java.class.path")).getAbsoluteFile().getParent();
    String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
    outputFolder = jarPath + File.separator + "ScienceFairOutput_" + timestamp;
    new File(outputFolder).mkdirs();
    outputFileField.setText(outputFolder);
    String outputFile = outputFolder + File.separator + "output.csv";
        
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
        // Always create output folder if missing
        new File(outputFolder).mkdirs();
        
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
                    
                    publish("Writing results to: " + outputFile);
                    ScienceFairCsvUtil.writeSlotAssignments(assignments, outputFile);
                    // Also generate output.html in same folder
                    String htmlFile = outputFolder + File.separator + "output.html";
                    com.sciencefair.ScienceFairTableAssignmentApp.generateHtmlLayoutFromCsv(outputFile, htmlFile);
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
                openHtmlButton.setEnabled(true);
                    openFolderButton.setEnabled(true);
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
        SwingUtilities.invokeLater(() -> {
            new ScienceFairAssignmentGui().setVisible(true);
        });
    }
}