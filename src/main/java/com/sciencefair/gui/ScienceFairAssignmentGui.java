package com.sciencefair.gui;

import com.sciencefair.model.ScienceProject;
import com.sciencefair.model.SlotAssignment;
import com.sciencefair.model.TableSlot;
import com.sciencefair.service.ScienceFairAssignmentService;
import com.sciencefair.util.ScienceFairCsvUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
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
    
    private JTextField tableSlotsFileField;
    private JTextField projectsFileField;
    private JTextField outputFileField;
    private JButton runButton;
    private JTextArea resultArea;
    private ScienceFairAssignmentService assignmentService;
    
    public ScienceFairAssignmentGui() {
        this.assignmentService = new ScienceFairAssignmentService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        setTitle("Science Fair Table Assignment");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 550);
        setLocationRelativeTo(null);
        
        // Create components
        tableSlotsFileField = new JTextField(35);
        projectsFileField = new JTextField(35);
        outputFileField = new JTextField(35);
        runButton = new JButton("Assign Projects to Table Slots");
        resultArea = new JTextArea(18, 60);
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        // Set default output location to Desktop
        String userHome = System.getProperty("user.home");
        String defaultOutput = userHome + File.separator + "Desktop" + File.separator + "assignment_results.csv";
        outputFileField.setText(defaultOutput);
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
        gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(15, 0, 10, 0);
        inputPanel.add(runButton, gbc);
        
        // Add panels to frame
        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
        
        // Add instructions at the bottom
        JTextArea instructions = new JTextArea(4, 60);
        instructions.setEditable(false);
        instructions.setBackground(getBackground());
        instructions.setText("Instructions:\n" +
                           "1. Select CSV file with table slots (Row,rowNumSlots,tableSlotID,isReserved)\n" +
                           "2. Select CSV file with projects (projectID,isTeam,isFirstInCat,Category)\n" +
                           "3. Choose output location (defaults to Desktop)\n" +
                           "4. Click 'Assign Projects to Table Slots' to run the assignment");
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
        String outputFile = outputFileField.getText().trim();
        
        // Validate inputs
        if (tableSlotsFile.isEmpty() || projectsFile.isEmpty() || outputFile.isEmpty()) {
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
        
        // Disable button during processing
        runButton.setEnabled(false);
        resultArea.setText("Processing assignment...\n");
        
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
                    
                    publish("\n" + assignmentService.generateAssignmentSummary(assignments, projects, tableSlots));
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
            new ScienceFairAssignmentGui().setVisible(true);
        });
    }
}