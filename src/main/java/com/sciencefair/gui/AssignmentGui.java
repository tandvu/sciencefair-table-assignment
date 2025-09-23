package com.sciencefair.gui;

import com.sciencefair.model.Assignment;
import com.sciencefair.model.Project;
import com.sciencefair.model.Table;
import com.sciencefair.service.AssignmentService;
import com.sciencefair.util.CsvUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Simple GUI for selecting input/output files and running the assignment
 */
public class AssignmentGui extends JFrame {
    
    private JTextField projectsFileField;
    private JTextField tablesFileField;
    private JTextField outputFileField;
    private JButton runButton;
    private JTextArea resultArea;
    private AssignmentService assignmentService;
    
    public AssignmentGui() {
        this.assignmentService = new AssignmentService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setTitle("Science Fair Table Assignment");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        
        // Create components
        projectsFileField = new JTextField(30);
        tablesFileField = new JTextField(30);
        outputFileField = new JTextField(30);
        runButton = new JButton("Assign Projects to Tables");
        resultArea = new JTextArea(15, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        // Set default output location to Desktop
        String userHome = System.getProperty("user.home");
        String defaultOutput = userHome + File.separator + "Desktop" + File.separator + "table_assignments.csv";
        outputFileField.setText(defaultOutput);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Create input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Projects file row
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Projects CSV:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        inputPanel.add(projectsFileField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JButton browseProjectsBtn = new JButton("Browse");
        browseProjectsBtn.addActionListener(e -> browseForFile(projectsFileField, "Select Projects CSV"));
        inputPanel.add(browseProjectsBtn, gbc);
        
        // Tables file row
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Tables CSV:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        inputPanel.add(tablesFileField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JButton browseTablesBtn = new JButton("Browse");
        browseTablesBtn.addActionListener(e -> browseForFile(tablesFileField, "Select Tables CSV"));
        inputPanel.add(browseTablesBtn, gbc);
        
        // Output file row
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(new JLabel("Output CSV:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        inputPanel.add(outputFileField, gbc);
        
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JButton browseOutputBtn = new JButton("Browse");
        browseOutputBtn.addActionListener(e -> browseForOutputFile());
        inputPanel.add(browseOutputBtn, gbc);
        
        // Run button row
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(10, 0, 0, 0);
        inputPanel.add(runButton, gbc);
        
        // Add panels to frame
        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
        
        // Add instructions at the bottom
        JTextArea instructions = new JTextArea(3, 50);
        instructions.setEditable(false);
        instructions.setBackground(getBackground());
        instructions.setText("Instructions:\n" +
                           "1. Select the CSV file containing project data\n" +
                           "2. Select the CSV file containing table data\n" +
                           "3. Choose output location (defaults to Desktop)\n" +
                           "4. Click 'Assign Projects to Tables' to run the assignment");
        add(instructions, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runAssignment();
            }
        });
    }
    
    private void browseForFile(JTextField textField, String title) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(title);
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void browseForOutputFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Assignment Results");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.setSelectedFile(new File("table_assignments.csv"));
        
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
        String projectsFile = projectsFileField.getText().trim();
        String tablesFile = tablesFileField.getText().trim();
        String outputFile = outputFileField.getText().trim();
        
        // Validate inputs
        if (projectsFile.isEmpty() || tablesFile.isEmpty() || outputFile.isEmpty()) {
            showError("Please select all required files.");
            return;
        }
        
        if (!new File(projectsFile).exists()) {
            showError("Projects file does not exist: " + projectsFile);
            return;
        }
        
        if (!new File(tablesFile).exists()) {
            showError("Tables file does not exist: " + tablesFile);
            return;
        }
        
        // Disable button during processing
        runButton.setEnabled(false);
        resultArea.setText("Processing assignment...\n");
        
        // Run assignment in background thread
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    publish("Reading projects from: " + projectsFile);
                    List<Project> projects = CsvUtil.readProjects(projectsFile);
                    publish("Loaded " + projects.size() + " projects");
                    
                    publish("Reading tables from: " + tablesFile);
                    List<Table> tables = CsvUtil.readTables(tablesFile);
                    publish("Loaded " + tables.size() + " tables");
                    
                    publish("Running assignment algorithm...");
                    List<Assignment> assignments = assignmentService.assignProjectsToTables(projects, tables);
                    
                    publish("Writing results to: " + outputFile);
                    CsvUtil.writeAssignments(assignments, projects, outputFile);
                    
                    publish("\n" + assignmentService.generateAssignmentSummary(assignments, projects));
                    publish("\nAssignment completed successfully!");
                    publish("Results saved to: " + outputFile);
                    
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
                    resultArea.append(message + "\n");
                }
                resultArea.setCaretPosition(resultArea.getDocument().getLength());
            }
            
            @Override
            protected void done() {
                runButton.setEnabled(true);
            }
        };
        
        worker.execute();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void main(String[] args) {
        // Create and show GUI
        SwingUtilities.invokeLater(() -> {
            new AssignmentGui().setVisible(true);
        });
    }
}