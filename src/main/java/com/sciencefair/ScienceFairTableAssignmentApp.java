package com.sciencefair;

import com.sciencefair.gui.ScienceFairAssignmentGui;
import com.sciencefair.model.ScienceProject;
import com.sciencefair.model.SlotAssignment;
import com.sciencefair.model.TableSlot;
import com.sciencefair.service.ScienceFairAssignmentService;
import com.sciencefair.util.ScienceFairCsvUtil;

import javax.swing.SwingUtilities;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Main application class for Science Fair Table Assignment
 * Updated to work with specific CSV format requirements
 */
public class ScienceFairTableAssignmentApp {
    /**
     * Generates an HTML layout from a CSV file (for GUI use)
     */
    public static void generateHtmlLayoutFromCsv(String csvFile, String htmlFile) throws Exception {
        List<SlotAssignment> assignments = com.sciencefair.util.ScienceFairCsvUtil.readSlotAssignments(csvFile);
        generateHtmlLayout(assignments, htmlFile);
    }
    
    public static void main(String[] args) {
        // Debug mode
        if (args.length == 1 && args[0].equals("--debug")) {
            runDebugMode();
            return;
        }
        
        // Check if running in GUI mode (no command line arguments) or CLI mode
        if (args.length == 0) {
            // Launch GUI
            SwingUtilities.invokeLater(() -> {
                System.out.println("Starting Science Fair Table Assignment GUI...");
                new ScienceFairAssignmentGui().setVisible(true);
            });
        } else if (args.length == 3) {
            // Run in command line mode
            String tableSlotsFile = args[0];  // SampleInputCSV1.csv
            String projectsFile = args[1];    // SampleInputCSV2.csv  
            String outputFile = args[2];      // SampleOutputCSV.csv
            
            runCommandLine(tableSlotsFile, projectsFile, outputFile);
        } else {
            printUsage();
        }
    }
    
    private static void runDebugMode() {
        try {
            System.out.println("=== DEBUGGING TABLE SLOTS ===");
            List<TableSlot> tableSlots = ScienceFairCsvUtil.readTableSlots("data/SampleInputCSV1.csv");
            System.out.println("Total slots loaded: " + tableSlots.size());
            
            // Show first few and last few slots
            System.out.println("\nFirst 5 slots:");
            for (int i = 0; i < Math.min(5, tableSlots.size()); i++) {
                TableSlot slot = tableSlots.get(i);
                System.out.println(String.format("Row=%d, SlotID=%d, Reserved=%s", 
                    slot.getRow(), slot.getTableSlotID(), slot.isReserved()));
            }
            
            System.out.println("\nLast 5 slots:");
            for (int i = Math.max(0, tableSlots.size() - 5); i < tableSlots.size(); i++) {
                TableSlot slot = tableSlots.get(i);
                System.out.println(String.format("Row=%d, SlotID=%d, Reserved=%s", 
                    slot.getRow(), slot.getTableSlotID(), slot.isReserved()));
            }
            
            long availableSlots = tableSlots.stream().filter(slot -> !slot.isReserved()).count();
            System.out.println("\nAvailable slots: " + availableSlots);
            System.out.println("Reserved slots: " + (tableSlots.size() - availableSlots));
            
            System.out.println("\n=== DEBUGGING PROJECTS ===");
            List<ScienceProject> projects = ScienceFairCsvUtil.readScienceProjects("data/SampleInputCSV2.csv");
            System.out.println("Total projects loaded: " + projects.size());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void runCommandLine(String tableSlotsFile, String projectsFile, String outputFile) {
        try {
            System.out.println("Science Fair Table Assignment - Command Line Mode");
            System.out.println("=".repeat(50));
            
            // Validate input files
            if (!new File(tableSlotsFile).exists()) {
                System.err.println("Error: Table slots file does not exist: " + tableSlotsFile);
                System.exit(1);
            }
            
            if (!new File(projectsFile).exists()) {
                System.err.println("Error: Projects file does not exist: " + projectsFile);
                System.exit(1);
            }
            
            // Load data
            System.out.println("Loading table slots from: " + tableSlotsFile);
            List<TableSlot> tableSlots = ScienceFairCsvUtil.readTableSlots(tableSlotsFile);
            System.out.println("Loaded " + tableSlots.size() + " table slots");
            
            System.out.println("Loading projects from: " + projectsFile);
            List<ScienceProject> projects = ScienceFairCsvUtil.readScienceProjects(projectsFile);
            System.out.println("Loaded " + projects.size() + " projects");
            
            // Run assignment
            System.out.println("Running assignment algorithm...");
            ScienceFairAssignmentService assignmentService = new ScienceFairAssignmentService();
            List<SlotAssignment> assignments = assignmentService.assignProjectsToSlots(projects, tableSlots);
            
            // Save results
            System.out.println("Saving results to: " + outputFile);
            ScienceFairCsvUtil.writeSlotAssignments(assignments, outputFile);
            
            // Generate single HTML layout file
            String htmlOutFile = outputFile.replace(".csv", ".html");
            System.out.println("Generating HTML layout: " + htmlOutFile);
            generateHtmlLayout(assignments, htmlOutFile);
            
            // Print summary
            System.out.println("\n" + assignmentService.generateAssignmentSummary(assignments, projects, tableSlots));
            
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
        System.out.println("    java -jar science-fair-table-assignment.jar <table_slots.csv> <projects.csv> <output.csv>");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("    java -jar science-fair-table-assignment.jar SampleInputCSV1.csv SampleInputCSV2.csv SampleOutputCSV.csv");
        System.out.println();
        System.out.println("CSV File Formats:");
        System.out.println("  Table Slots CSV (Input 1): Row,rowNumSlots,tableSlotID,isReserved");
        System.out.println("  Projects CSV (Input 2): projectID,isTeam,isFirstInCat,Category");
        System.out.println("  Output CSV: Row,tableSlotID,isUnassigned,projectID,isTeam,Category");
    }
    
    /**
     * Generates an HTML layout file showing table assignments in a visual table format
     */
    private static void generateHtmlLayout(List<SlotAssignment> assignments, String outputFile) {
            // ...existing code...
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Group assignments by row
            Map<Integer, List<SlotAssignment>> assignmentsByRow = assignments.stream()
                .collect(Collectors.groupingBy(
                    SlotAssignment::getRow,
                    TreeMap::new,
                    Collectors.toList()
                ));
            
            // HTML header with embedded CSS
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("    <title>Science Fair Table Assignment - Visual Layout</title>");
            writer.println("    <style>");
            writer.println("        * { box-sizing: border-box; }");
            writer.println("        body { font-family: 'Segoe UI', system-ui, -apple-system, sans-serif; margin: 0; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; }");
            writer.println("        .container { max-width: 1600px; margin: 0 auto; background: rgba(255,255,255,0.95); padding: 30px; border-radius: 20px; box-shadow: 0 20px 60px rgba(0,0,0,0.1); backdrop-filter: blur(10px); }");
            writer.println("        h1 { color: #2d3748; text-align: center; margin-bottom: 40px; font-size: 2.5em; font-weight: 300; letter-spacing: -1px; }");
            writer.println("        .table-container { overflow-x: auto; margin: 30px 0; border-radius: 12px; box-shadow: 0 8px 32px rgba(0,0,0,0.08); }");
            writer.println("        table { border-collapse: separate; border-spacing: 0; width: 100%; font-size: 11px; background: white; border-radius: 12px; overflow: hidden; }");
            writer.println("        .row-label { background: linear-gradient(135deg, #4a5568, #2d3748); color: white; padding: 12px 16px; font-weight: 600; text-align: center; font-size: 12px; text-shadow: 0 1px 2px rgba(0,0,0,0.3); min-width: 60px; }");
            writer.println("        .slot { border-top: 2px solid transparent !important; border-bottom: 2px solid transparent !important; border-left: none !important; border-right: none !important; box-shadow: inset 0 0 0 2px transparent; padding: 4px 2px; text-align: center; min-width: 60px; font-weight: 500; position: relative; font-size: 9px; border-radius: 4px; }");
            writer.println("        .anim { border-color: #68d391 !important; box-shadow: inset 0 0 0 2px #68d391 !important; }");
            writer.println("        .behv { border-color: #4299e1 !important; box-shadow: inset 0 0 0 2px #4299e1 !important; }");
            writer.println("        .bioc { border-color: #d69e2e !important; box-shadow: inset 0 0 0 2px #d69e2e !important; }");
            writer.println("        .biom { border-color: #d53f8c !important; box-shadow: inset 0 0 0 2px #d53f8c !important; }");
            writer.println("        .cell { border-color: #805ad5 !important; box-shadow: inset 0 0 0 2px #805ad5 !important; }");
            writer.println("        .chem { border-color: #e53e3e !important; box-shadow: inset 0 0 0 2px #e53e3e !important; }");
            writer.println("        .comp { border-color: #38b2ac !important; box-shadow: inset 0 0 0 2px #38b2ac !important; }");
            writer.println("        .empty { border-color: #a0aec0 !important; box-shadow: inset 0 0 0 2px #a0aec0 !important; }");
            writer.println("        .table-block.non-team-table { background: #e5e7eb !important; }");
            writer.println("        .table-block { display: inline-block; margin: 0 3px 4px 0; border: 2px solid #b8b8b8; border-radius: 6px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.1); background: #fffbe6; position: relative; min-height: 70px; padding-left: 4px; padding-right: 4px; }");
            writer.println("        .table-slots { display: flex; gap: 8px; }");
            writer.println("        .table-block.empty-table { background: inherit !important; }");
            writer.println("        .table-block.team-table { background: #fffde3; }");
            writer.println("        .table-header { background: #e5e7eb; padding: 2px 4px; text-align: center; font-size: 8px; font-weight: 600; color: #495057; border-bottom: 1px solid #dee2e6; position: relative; }");
            writer.println("        .team-table .table-header { background: #fffde3 !important; }");
            writer.println("        .empty-table .table-header { background: inherit !important; }");
            writer.println("        .team-icon { position: absolute; top: 0px; right: 2px; font-size: 9px; opacity: 0.8; pointer-events: none; z-index: 2; }");
            writer.println("        .table-slots { display: flex; }");
            writer.println("        .empty { background: linear-gradient(135deg, #f7fafc, #edf2f7); color: #a0aec0; border-left: 4px solid #cbd5e0; }");
            writer.println("        .anim { background: linear-gradient(135deg, #f0fff4, #c6f6d5); color: #22543d; border-left: 4px solid #38a169; }");
            writer.println("        .behv { background: linear-gradient(135deg, #ebf8ff, #bee3f8); color: #2a4365; border-left: 4px solid #3182ce; }");
            writer.println("        .bioc { background: linear-gradient(135deg, #fffaf0, #fbd38d); color: #744210; border-left: 4px solid #d69e2e; }");
            writer.println("        .biom { background: linear-gradient(135deg, #fef5e7, #fed7d7); color: #702459; border-left: 4px solid #d53f8c; }");
            writer.println("        .cell { background: linear-gradient(135deg, #faf5ff, #e9d8fd); color: #553c9a; border-left: 4px solid #805ad5; }");
            writer.println("        .chem { background: linear-gradient(135deg, #fff5f5, #fed7d7); color: #742a2a; border-left: 4px solid #e53e3e; }");
            writer.println("        .comp { background: linear-gradient(135deg, #f0fdfa, #c6f7ed); color: #234e52; border-left: 4px solid #38b2ac; }");
            writer.println("        .team::before { content: '👥'; position: absolute; top: 1px; right: 2px; font-size: 8px; opacity: 0.7; }");
            writer.println("        .team { box-shadow: inset 0 0 0 2px currentColor; border-radius: 4px; }");
            writer.println("        .legend { margin-top: 24px; padding: 12px 10px; background: linear-gradient(135deg, #f8f9fa, #e9ecef); border-radius: 10px; }");
            writer.println("        .legend h3 { color: #495057; margin-bottom: 10px; font-size: 1em; font-weight: 500; }");
            writer.println("        .legend-table { font-size: 11px; width: 100%; }");
            writer.println("        .legend-table td { padding: 4px 6px; vertical-align: middle; }");
            writer.println("        .legend-sample { width: 36px; height: 18px; border-radius: 4px; }");
            writer.println("        .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(120px, 1fr)); gap: 8px; margin-bottom: 16px; }");
            writer.println("        .stat-card { background: white; padding: 8px; border-radius: 8px; box-shadow: 0 2px 6px rgba(0,0,0,0.04); text-align: center; }");
            writer.println("        .stat-number { font-size: 1.2em; font-weight: 700; color: #4a5568; }");
            writer.println("        .stat-label { color: #718096; font-size: 0.8em; margin-top: 2px; }");
            writer.println("        @media (max-width: 768px) { .container { padding: 15px; } h1 { font-size: 1.8em; } .slot { min-width: 70px; padding: 6px 2px; font-size: 10px; } .row-label { padding: 8px 10px; font-size: 10px; } }");
            writer.println("    </style>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("    <div class='container'>");
            writer.println("        <h1>Science Fair Table Assignment</h1>");
            
            // Only keep Team Projects, Total Projects, and Table Counts
            // Team Projects: count unique project IDs where isTeam is true
            Set<Integer> teamProjectIds = assignments.stream()
                .filter(a -> a.getIsTeam() != null && a.getIsTeam() && a.getProjectID() != null)
                .map(SlotAssignment::getProjectID)
                .collect(Collectors.toSet());
            int teamProjects = teamProjectIds.size();

            // Total Projects: count unique project IDs (assigned)
            Set<Integer> totalProjectIds = assignments.stream()
                .filter(a -> a.getProjectID() != null && !a.isUnassigned())
                .map(SlotAssignment::getProjectID)
                .collect(Collectors.toSet());
            int totalProjects = totalProjectIds.size();

            // Table Counts: count total tables (each table = 2 slots)
            int tableCount = assignments.size() / 2;

            writer.println("        <div class='stats'>");
            writer.println("            <div class='stat-card'>");
            writer.println("                <div class='stat-number'>" + totalProjects + "</div>");
            writer.println("                <div class='stat-label'>Total Projects</div>");
            writer.println("            </div>");
            writer.println("            <div class='stat-card'>");
            writer.println("                <div class='stat-number'>" + teamProjects + "</div>");
            writer.println("                <div class='stat-label'>Team Projects</div>");
            writer.println("            </div>");
            writer.println("            <div class='stat-card'>");
            writer.println("                <div class='stat-number'>" + tableCount + "</div>");
            writer.println("                <div class='stat-label'>Table Count</div>");
            writer.println("            </div>");
            writer.println("        </div>");
            
            // Generate table
            writer.println("        <div class='table-container'>");
            
            // First, collect all row information to calculate proper table numbers
            Map<Integer, Integer> rowTableCounts = new TreeMap<>();
            for (Map.Entry<Integer, List<SlotAssignment>> rowEntry : assignmentsByRow.entrySet()) {
                int rowNumber = rowEntry.getKey();
                int slotsInRow = rowEntry.getValue().size();
                int tablesInRow = slotsInRow / 2; // 2 slots per table
                rowTableCounts.put(rowNumber, tablesInRow);
            }
            
            for (Map.Entry<Integer, List<SlotAssignment>> rowEntry : assignmentsByRow.entrySet()) {
                int rowNumber = rowEntry.getKey();
                List<SlotAssignment> rowAssignments = rowEntry.getValue();
                
                // Sort by slot ID to get proper order
                rowAssignments.sort((a, b) -> Integer.compare(a.getTableSlotID(), b.getTableSlotID()));
                
                writer.println("            <div style='margin: 20px 0;'>");
                writer.println("                <div class='row-label' style='display: inline-block; margin-right: 20px; vertical-align: top; margin-top: 10px;'>Row " + rowNumber + "</div>");
                
                // Group slots into tables (2 slots per table)
                List<Integer> tableNumbers = new ArrayList<>();
                for (int i = 0; i < rowAssignments.size(); i += 2) {
                    int tableIndex = i / 2; // 0, 1, 2, etc.
                    int tableNumber = calculateSnakeFlowTableNumber(rowNumber, tableIndex + 1, rowTableCounts);
                    tableNumbers.add(tableNumber);
                }
                
                // For even rows, reverse the visual order to create snake flow pattern
                List<Integer> visualOrder = new ArrayList<>();
                for (int i = 0; i < tableNumbers.size(); i++) {
                    visualOrder.add(i);
                }
                if (rowNumber % 2 == 0) {
                    Collections.reverse(visualOrder);
                }
                
                for (int visualIndex : visualOrder) {
                    int tableIndex = visualIndex;
                    int slotIndexStart = tableIndex * 2;
                    int tableNumber = tableNumbers.get(tableIndex);
                    boolean isTeamTable = false;
                    SlotAssignment assignment1 = rowAssignments.get(slotIndexStart);
                    SlotAssignment assignment2 = (slotIndexStart + 1 < rowAssignments.size()) ? rowAssignments.get(slotIndexStart + 1) : null;
                    if (assignment1.getIsTeam() != null && assignment1.getIsTeam()) {
                        isTeamTable = true;
                    }
                    if (assignment2 != null && assignment2.getIsTeam() != null && assignment2.getIsTeam()) {
                        isTeamTable = true;
                    }
                    boolean isEmptyTable = assignment1.isUnassigned() && (assignment2 == null || assignment2.isUnassigned());
                    boolean isNonTeamTable = !isTeamTable && !isEmptyTable;
                    writer.println("                <div class='table-block" + (isTeamTable ? " team-table" : "") + (isEmptyTable ? " empty-table" : "") + (isNonTeamTable ? " non-team-table" : "") + "'>");
                    if (isTeamTable) {
                        writer.print("<span class='team-icon' title='Team Project'>👥</span>");
                    }
                    writer.print("<div class='table-header'>Table " + tableNumber);
                    writer.println("</div>");
                    writer.println("                    <div class='table-slots'>");
                    // For even rows, reverse slot order within table
                    if (rowNumber % 2 == 0) {
                        if (assignment2 != null) {
                            String slotContent2 = generateSlotContent(assignment2);
                            String cssClass2 = generateSlotCssClass(assignment2);
                            writer.println("                        <div class='" + cssClass2 + "'>" + slotContent2 + "</div>");
                        }
                        String slotContent1 = generateSlotContent(assignment1);
                        String cssClass1 = generateSlotCssClass(assignment1);
                        writer.println("                        <div class='" + cssClass1 + "'>" + slotContent1 + "</div>");
                    } else {
                        String slotContent1 = generateSlotContent(assignment1);
                        String cssClass1 = generateSlotCssClass(assignment1);
                        writer.println("                        <div class='" + cssClass1 + "'>" + slotContent1 + "</div>");
                        if (assignment2 != null) {
                            String slotContent2 = generateSlotContent(assignment2);
                            String cssClass2 = generateSlotCssClass(assignment2);
                            writer.println("                        <div class='" + cssClass2 + "'>" + slotContent2 + "</div>");
                        }
                    }
                    writer.println("                    </div>");
                    writer.println("                </div>");
                }
                
                writer.println("            </div>");
            }
            writer.println("        </div>");
            
            // Add legend
            writer.println("        <div class='legend'>");
            writer.println("            <h3>🧬 Category Legend</h3>");
            writer.println("            <table class='legend-table'>");
            writer.println("                <tr><td class='slot anim legend-sample'></td><td>Animal Sciences (ANIM)</td></tr>");
            writer.println("                <tr><td class='slot behv legend-sample'></td><td>Behavioral & Social Sciences (BEHA)</td></tr>");
            writer.println("                <tr><td class='slot bchm legend-sample'></td><td>Biochemistry (BCHM)</td></tr>");
            writer.println("                <tr><td class='slot bmed legend-sample'></td><td>Biomedical/Health Sci & Eng (BMED)</td></tr>");
            writer.println("                <tr><td class='slot cell legend-sample'></td><td>Cellular & Molecular Biology (CELL)</td></tr>");
            writer.println("                <tr><td class='slot chem legend-sample'></td><td>Chemistry (CHEM)</td></tr>");
            writer.println("                <tr><td class='slot cbio legend-sample'></td><td>Computational Bio & Info (CBIO)</td></tr>");
            writer.println("                <tr><td class='slot comp legend-sample'></td><td>Computer Science (COMP)</td></tr>");
            writer.println("                <tr><td class='slot eaev legend-sample'></td><td>Earth & Environmental Sci (EAEV)</td></tr>");
            writer.println("                <tr><td class='slot eemr legend-sample'></td><td>Eng: Electrical, Mech, Robotics (EEMR)</td></tr>");
            writer.println("                <tr><td class='slot eemt legend-sample'></td><td>Eng: Energy, Materials, Transport (EEMT)</td></tr>");
            writer.println("                <tr><td class='slot math legend-sample'></td><td>Mathematics (MATH)</td></tr>");
            writer.println("                <tr><td class='slot mcro legend-sample'></td><td>Microbiology (MCRO)</td></tr>");
            writer.println("                <tr><td class='slot phys legend-sample'></td><td>Physics & Astronomy (PHYS)</td></tr>");
            writer.println("                <tr><td class='slot plnt legend-sample'></td><td>Plant Sciences (PLNT)</td></tr>");
            writer.println("                <tr><td class='slot prod legend-sample'></td><td>Product Testing (PROD)</td></tr>");
            writer.println("                <tr><td class='slot empty legend-sample'>EMPTY</td><td>Reserved/unassigned slot</td></tr>");
            writer.println("            </table>");
            writer.println("            <p style='font-size:10px; margin-top:6px;'><strong>💡 Note:</strong> T### = Table Number (snake flow), P### = Project ID. Team projects are marked with 👥</p>");
            writer.println("        </div>");
            
            writer.println("    </div>");
            writer.println("</body>");
            writer.println("</html>");
            
        } catch (IOException e) {
            System.err.println("Error writing HTML layout file: " + e.getMessage());
        }
    }
    
    /**
     * Generates a visual layout file showing table assignments in a readable format
     */
    private static void generateVisualLayout(List<SlotAssignment> assignments, String outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Group assignments by row
            Map<Integer, List<SlotAssignment>> assignmentsByRow = assignments.stream()
                .collect(Collectors.groupingBy(
                    SlotAssignment::getRow,
                    TreeMap::new,
                    Collectors.toList()
                ));
            
            writer.println("Science Fair Table Assignment - Visual Layout");
            writer.println("===========================================");
            writer.println();
            
            for (Map.Entry<Integer, List<SlotAssignment>> rowEntry : assignmentsByRow.entrySet()) {
                int rowNumber = rowEntry.getKey();
                List<SlotAssignment> rowAssignments = rowEntry.getValue();
                
                // Sort by slot ID to get proper order
                rowAssignments.sort((a, b) -> Integer.compare(a.getTableSlotID(), b.getTableSlotID()));
                
                StringBuilder rowDisplay = new StringBuilder("Row " + rowNumber + ": ");
                
                for (SlotAssignment assignment : rowAssignments) {
                    String slotDisplay;
                    if (!assignment.isUnassigned() && assignment.getProjectID() != null) {
                        Integer projectID = assignment.getProjectID();
                        String category = assignment.getCategory();
                        String categoryAbbrev = getCategoryAbbreviation(category);
                        
                        if (assignment.getIsTeam() != null && assignment.getIsTeam()) {
                            // For team projects, show the project ID spanning multiple slots
                            String teamContent = projectID + "-" + categoryAbbrev + "---" + projectID + "-" + categoryAbbrev;
                            slotDisplay = String.format("[%-20s]", teamContent);
                        } else {
                            String individualContent = projectID + "-" + categoryAbbrev;
                            slotDisplay = String.format("[%-20s]", individualContent);
                        }
                    } else {
                        slotDisplay = String.format("[%-20s]", "EMPTY");
                    }
                    rowDisplay.append(slotDisplay).append(" ");
                }
                
                writer.println(rowDisplay.toString().trim());
            }
            
            writer.println();
            writer.println("Legend:");
            writer.println("  [123-ANIM           ] = Individual project 123 (Animal Sciences)");
            writer.println("  [123-BEHA---123-BEHA] = Team project 123 (Behavioral and Social Sciences, spans 2 slots)");
            writer.println("  [EMPTY              ] = Reserved or unassigned slot");
            writer.println();
            writer.println("Category Abbreviations:");
            writer.println("  ANIM = Animal Sciences");
            writer.println("  BEHA = Behavioral and Social Sciences");
            writer.println("  BCHM = Biochemistry");
            writer.println("  BMED = Biomedical/Health Sciences, and Biomedical Engineering");
            writer.println("  CELL = Cellular and Molecular Biology");
            writer.println("  CHEM = Chemistry");
            writer.println("  CBIO = Computational Biology and Bioinformatics");
            writer.println("  COMP = Computer Science and Systems Software");
            writer.println("  EAEV = Earth and Environmental Sciences");
            writer.println("  EEMR = Engineering: Electrical, Mechanical, and Robotics");
            writer.println("  EEMT = Engineering: Energy, Materials, and Transport");
            writer.println("  MATH = Mathematics");
            writer.println("  MCRO = Microbiology");
            writer.println("  PHYS = Physics and Astronomy");
            writer.println("  PLNT = Plant Sciences");
            writer.println("  PROD = Product Testing");
            
        } catch (IOException e) {
            System.err.println("Error writing visual layout file: " + e.getMessage());
        }
    }
    
    /**
     * Creates abbreviated category names for the visual layout
     */
    private static String getCategoryAbbreviation(String category) {
        if (category == null) return "UNKN";
        Map<String, String> lookup = new HashMap<>();
        lookup.put("Animal Sciences", "ANIM");
        lookup.put("Behavioral and Social Sciences", "BEHA");
        lookup.put("Biochemistry", "BCHM");
        lookup.put("Biomedical/Health Sciences, and Biomedical Engineering", "BMED");
        lookup.put("Cellular and Molecular Biology", "CELL");
        lookup.put("Chemistry", "CHEM");
        lookup.put("Computational Biology and Bioinformatics", "CBIO");
        lookup.put("Computer Science and Systems Software", "COMP");
        lookup.put("Earth and Environmental Sciences", "EAEV");
        lookup.put("Engineering: Electrical, Mechanical, and Robotics", "EEMR");
        lookup.put("Engineering: Energy, Materials, and Transport", "EEMT");
        lookup.put("Mathematics", "MATH");
        lookup.put("Microbiology", "MCRO");
        lookup.put("Physics and Astronomy", "PHYS");
        lookup.put("Plant Sciences", "PLNT");
        lookup.put("Product Testing", "PROD");
        if (lookup.containsKey(category)) return lookup.get(category);
        for (Map.Entry<String, String> entry : lookup.entrySet()) {
            if (category != null && category.contains(entry.getKey())) return entry.getValue();
        }
        return category.length() >= 4 ? category.substring(0, 4).toUpperCase() : category;
    }
    
    /**
     * Calculates the snake flow table number based on row and table position
     * Handles variable row sizes properly by calculating cumulative table counts
     * Row 1: 101-106 (6 tables, left to right)
     * Row 2: 112-107 (6 tables, right to left) 
     * Row 3: 113-118 (6 tables, left to right)
     * Row 4: 125-119 (7 tables, right to left)
     * Row 5: 126-132 (7 tables, left to right)
     * etc.
     */
    private static int calculateSnakeFlowTableNumber(int currentRow, int tablePosition, Map<Integer, Integer> rowTableCounts) {
        // Calculate total tables in all previous rows to get the base table number
        int cumulativeTablesBefore = 0;
        for (int row = 1; row < currentRow; row++) {
            cumulativeTablesBefore += rowTableCounts.getOrDefault(row, 0);
        }
        // Table numbers now start at 1 and increment by 1 for each table
        int tableNumber = 1 + cumulativeTablesBefore + (tablePosition - 1);
        return tableNumber;
    }
    
    /**
     * Generates the content for a slot display
     */
    private static String generateSlotContent(SlotAssignment assignment) {
        if (!assignment.isUnassigned() && assignment.getProjectID() != null) {
            Integer projectID = assignment.getProjectID();
            String category = assignment.getCategory();
            String division = "";
            if (category != null && category.length() >= 2) {
                division = category.substring(0, 2).toUpperCase();
            }
            String categoryAbbrev = getCategoryAbbreviation(category);
            String label = division + "-" + categoryAbbrev;
            return "<strong>P" + projectID + "</strong><br><small>" + label + "</small>";
        } else {
            return "<strong>EMPTY</strong>";
        }
    }
    
    /**
     * Generates the CSS class for a slot
     */
    private static String generateSlotCssClass(SlotAssignment assignment) {
        String cssClass = "slot ";
        if (!assignment.isUnassigned() && assignment.getProjectID() != null) {
            String category = assignment.getCategory();
            String categoryAbbrev = getCategoryAbbreviation(category);
            // Map official code to CSS class
            Map<String, String> codeToCss = new HashMap<>();
            codeToCss.put("ANIM", "anim");
            codeToCss.put("BEHA", "behv");
            codeToCss.put("BCHM", "bioc");
            codeToCss.put("BMED", "biom");
            codeToCss.put("CELL", "cell");
            codeToCss.put("CHEM", "chem");
            codeToCss.put("CBIO", "comp");
            codeToCss.put("COMP", "comp");
            codeToCss.put("EAEV", "eaev");
            codeToCss.put("EEMR", "eemr");
            codeToCss.put("EEMT", "eemt");
            codeToCss.put("MATH", "math");
            codeToCss.put("MCRO", "mcro");
            codeToCss.put("PHYS", "phys");
            codeToCss.put("PLNT", "plnt");
            codeToCss.put("PROD", "prod");
            String cssCat = codeToCss.getOrDefault(categoryAbbrev, "empty");
            cssClass += cssCat;
            if (assignment.getIsTeam() != null && assignment.getIsTeam()) {
                cssClass += " team";
            }
        } else {
            cssClass += "empty";
        }
        return cssClass;
    }
}