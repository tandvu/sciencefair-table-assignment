package com.sciencefair;

import com.sciencefair.gui.AssignmentGui;
import com.sciencefair.model.Assignment;
import com.sciencefair.model.Project;
import com.sciencefair.model.Table;
import com.sciencefair.service.AssignmentService;
import com.sciencefair.util.CsvUtil;

import javax.swing.SwingUtilities;
import java.io.File;
import java.util.List;

/**
 * Main application class for Science Fair Table Assignment
 */
public class TableAssignmentApp {
    
    public static void main(String[] args) {
        // Check if running in GUI mode (no command line arguments) or CLI mode
        if (args.length == 0) {
            // Launch GUI
            SwingUtilities.invokeLater(() -> {
                System.out.println("Starting Science Fair Table Assignment GUI...");
                new AssignmentGui().setVisible(true);
            });
        } else if (args.length == 3) {
            // Run in command line mode
            String projectsFile = args[0];
            String tablesFile = args[1];
            String outputFile = args[2];
            
            runCommandLine(projectsFile, tablesFile, outputFile);
        } else {
            printUsage();
        }
    }
    
    private static void runCommandLine(String projectsFile, String tablesFile, String outputFile) {
        try {
            System.out.println("Science Fair Table Assignment - Command Line Mode");
            System.out.println("=".repeat(50));
            
            // Validate input files
            if (!new File(projectsFile).exists()) {
                System.err.println("Error: Projects file does not exist: " + projectsFile);
                System.exit(1);
            }
            
            if (!new File(tablesFile).exists()) {
                System.err.println("Error: Tables file does not exist: " + tablesFile);
                System.exit(1);
            }
            
            // Load data
            System.out.println("Loading projects from: " + projectsFile);
            List<Project> projects = CsvUtil.readProjects(projectsFile);
            System.out.println("Loaded " + projects.size() + " projects");
            
            System.out.println("Loading tables from: " + tablesFile);
            List<Table> tables = CsvUtil.readTables(tablesFile);
            System.out.println("Loaded " + tables.size() + " tables");
            
            // Run assignment
            System.out.println("Running assignment algorithm...");
            AssignmentService assignmentService = new AssignmentService();
            List<Assignment> assignments = assignmentService.assignProjectsToTables(projects, tables);
            
            // Save results
            System.out.println("Saving results to: " + outputFile);
            CsvUtil.writeAssignments(assignments, projects, outputFile);
            
            // Print summary
            System.out.println("\n" + assignmentService.generateAssignmentSummary(assignments, projects));
            
            System.out.println("Assignment completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void printUsage() {
        System.out.println("Science Fair Table Assignment Tool");
        System.out.println("=".repeat(35));
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  GUI Mode:");
        System.out.println("    java -jar science-fair-table-assignment.jar");
        System.out.println();
        System.out.println("  Command Line Mode:");
        System.out.println("    java -jar science-fair-table-assignment.jar <projects.csv> <tables.csv> <output.csv>");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar science-fair-table-assignment.jar projects.csv tables.csv assignments.csv");
        System.out.println();
        System.out.println("CSV File Formats:");
        System.out.println("  Projects CSV headers: projectId,projectName,studentName,category,grade,requiresElectricity,requiresWater,specialRequirements,estimatedSpace");
        System.out.println("  Tables CSV headers: tableId,location,capacity,hasElectricity,hasWater,category,gradeRange,isAccessible,notes");
        System.out.println();
        System.out.println("Output CSV headers: tableId,projectId,studentName,projectName,category,assignmentReason,compatibilityScore");
    }
}