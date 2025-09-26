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
        generateHtmlLayout(assignments, outputFile, null, true);
    }

    /**
     * Overloaded layout generator allowing custom row ordering and optional pair-spacing logic.
     * If rowOrder is provided, rows are rendered in that sequence (rows not found are ignored; extra rows skipped).
     * Pair spacing logic: when enabled, a very small vertical gap is used between even/odd consecutive pairs (2&3,4&5,...)
     */
    public static void generateHtmlLayout(List<SlotAssignment> assignments, String outputFile, List<Integer> rowOrder, boolean applyPairSpacing) {
            // ...existing code...
        generateHtmlLayout(assignments, outputFile, rowOrder, applyPairSpacing, null);
    }

    /**
     * Advanced generator supporting explicit per-row margin-top overrides.
     * If rowMarginTop is provided, it takes precedence over pair-spacing logic.
     */
    public static void generateHtmlLayout(List<SlotAssignment> assignments, String outputFile, List<Integer> rowOrder, boolean applyPairSpacing, Map<Integer,Integer> rowMarginTop) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            // Group assignments by row
            Map<Integer, List<SlotAssignment>> assignmentsByRow = assignments.stream()
                .collect(Collectors.groupingBy(
                    SlotAssignment::getRow,
                    TreeMap::new,
                    Collectors.toList()
                ));

            // Derive ordered list of rows
            List<Integer> orderedRows;
            if (rowOrder != null && !rowOrder.isEmpty()) {
                orderedRows = rowOrder.stream().filter(assignmentsByRow::containsKey).collect(Collectors.toList());
            } else {
                orderedRows = new ArrayList<>(assignmentsByRow.keySet());
            }
            
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
            // Table slot borders (not legend samples) - consistent 2px inset border for all categories
            writer.println("        .slot:not(.legend-sample).anim { border-color: #38a169 !important; box-shadow: inset 0 0 0 2px #38a169 !important; }");
            writer.println("        .slot:not(.legend-sample).behv { border-color: #3182ce !important; box-shadow: inset 0 0 0 2px #3182ce !important; }");
            writer.println("        .slot:not(.legend-sample).bioc { border-color: #b7791f !important; box-shadow: inset 0 0 0 2px #b7791f !important; }");
            writer.println("        .slot:not(.legend-sample).biom { border-color: #c05621 !important; box-shadow: inset 0 0 0 2px #c05621 !important; }");
            writer.println("        .slot:not(.legend-sample).chem { border-color: #2c7a7b !important; box-shadow: inset 0 0 0 2px #2c7a7b !important; }");
            writer.println("        .slot:not(.legend-sample).cbio { border-color: #3c366b !important; box-shadow: inset 0 0 0 2px #3c366b !important; }");
            writer.println("        .slot:not(.legend-sample).cell { border-color: #805ad5 !important; box-shadow: inset 0 0 0 2px #805ad5 !important; }");
            writer.println("        .slot:not(.legend-sample).comp { border-color: #2c7a7b !important; box-shadow: inset 0 0 0 2px #2c7a7b !important; }");
            writer.println("        .slot:not(.legend-sample).eaev { border-color: #22c55e !important; box-shadow: inset 0 0 0 2px #22c55e !important; }");
            writer.println("        .slot:not(.legend-sample).eemr { border-color: #9333ea !important; box-shadow: inset 0 0 0 2px #9333ea !important; }");
            writer.println("        .slot:not(.legend-sample).eemt { border-color: #f97316 !important; box-shadow: inset 0 0 0 2px #f97316 !important; }");
            writer.println("        .slot:not(.legend-sample).math { border-color: #ca8a04 !important; box-shadow: inset 0 0 0 2px #ca8a04 !important; }");
            writer.println("        .slot:not(.legend-sample).mcro { border-color: #0ea5e9 !important; box-shadow: inset 0 0 0 2px #0ea5e9 !important; }");
            writer.println("        .slot:not(.legend-sample).phys { border-color: #c026d3 !important; box-shadow: inset 0 0 0 2px #c026d3 !important; }");
            writer.println("        .slot:not(.legend-sample).plnt { border-color: #65a30d !important; box-shadow: inset 0 0 0 2px #65a30d !important; }");
            writer.println("        .slot:not(.legend-sample).prod { border-color: #e11d48 !important; box-shadow: inset 0 0 0 2px #e11d48 !important; }");
                        writer.println("        .slot { border-top: 2px solid transparent !important; border-bottom: 2px solid transparent !important; border-left: none !important; border-right: none !important; box-shadow: inset 0 0 0 2px transparent; padding: 4px 2px; text-align: center; min-width: 60px; min-height: 38px; height: 38px; font-weight: 500; position: relative; font-size: 9px; border-radius: 4px; }");
                        writer.println("        .slot:not(.legend-sample).empty { border-color: #a0aec0 !important; box-shadow: inset 0 0 0 2px #a0aec0 !important; background: linear-gradient(135deg, #f7fafc, #edf2f7); color: #a0aec0; border-left: 4px solid #cbd5e0; min-height: 38px; height: 38px; }");
                        writer.println("        .slot:not(.legend-sample).empty.reserved { background: linear-gradient(135deg, #ffe6e6, #ffd6d6); color: #b80000; border-left: 4px solid #ff6b6b; border-color: #ff6b6b !important; box-shadow: inset 0 0 0 2px #ff6b6b !important; min-height: 38px; height: 38px; }");
            writer.println("        .table-block.non-team-table { background: #e5e7eb !important; }");
            writer.println("        .table-block { display: inline-block; margin: 0 3px 4px 0; border: 2px solid #b8b8b8; border-radius: 6px; overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,0.1); background: #fffbe6; position: relative; min-height: 70px; padding-left: 4px; padding-right: 4px; }");
            writer.println("        .table-slots { display: flex; gap: 8px; }");
            writer.println("        .table-block.empty-table { background: inherit !important; }");
            writer.println("        .table-block.team-table { background: #fffde3; }");
            writer.println("        .table-header { background: #e5e7eb; padding: 2px 4px; text-align: center; font-size: 8px; font-weight: 600; color: #495057; border-bottom: 1px solid #dee2e6; position: relative; }");
            writer.println("        .team-table .table-header { background: #fffde3 !important; }");
            writer.println("        .empty-table .table-header { background: inherit !important; }");
            // Removed table-level team icon styling (slot-level icons remain via .team::before)
            writer.println("        .table-slots { display: flex; }");
            writer.println("        .empty { background: linear-gradient(135deg, #f7fafc, #edf2f7); color: #a0aec0; border-left: 4px solid #cbd5e0; }");
            writer.println("        .anim { background: linear-gradient(135deg, #dcfce7, #86efac); color: #14532d; border-left: 4px solid #16a34a; }");
            writer.println("        .behv { background: linear-gradient(135deg, #dbeafe, #93c5fd); color: #1e3a8a; border-left: 4px solid #2563eb; }");
            writer.println("        .bioc { background: linear-gradient(135deg, #fef3c7, #fcd34d); color: #92400e; border-left: 4px solid #d97706; }");
            writer.println("        .biom { background: linear-gradient(135deg, #fed7aa, #fb923c); color: #9a3412; border-left: 4px solid #ea580c; }");
            writer.println("        .chem { background: linear-gradient(135deg, #a7f3d0, #34d399); color: #064e3b; border-left: 4px solid #059669; }");
            writer.println("        .cbio { background: linear-gradient(135deg, #fce7f3, #f9a8d4); color: #831843; border-left: 4px solid #be185d; }");
            writer.println("        .cell { background: linear-gradient(135deg, #ede9fe, #c4b5fd); color: #581c87; border-left: 4px solid #7c3aed; }");
            writer.println("        .comp { background: linear-gradient(135deg, #f0f9ff, #7dd3fc); color: #0c4a6e; border-left: 4px solid #0284c7; }");
            writer.println("        .eaev { background: linear-gradient(135deg, #f0fdf4, #bbf7d0); color: #14532d; border-left: 4px solid #22c55e; }");
            writer.println("        .eemr { background: linear-gradient(135deg, #fef7ff, #f3e8ff); color: #581c87; border-left: 4px solid #9333ea; }");
            writer.println("        .eemt { background: linear-gradient(135deg, #fff7ed, #fed7aa); color: #9a3412; border-left: 4px solid #f97316; }");
            writer.println("        .math { background: linear-gradient(135deg, #fefce8, #fde047); color: #713f12; border-left: 4px solid #ca8a04; }");
            writer.println("        .mcro { background: linear-gradient(135deg, #f0f9ff, #bae6fd); color: #0c4a6e; border-left: 4px solid #0ea5e9; }");
            writer.println("        .phys { background: linear-gradient(135deg, #fdf4ff, #f5d0fe); color: #86198f; border-left: 4px solid #c026d3; }");
            writer.println("        .plnt { background: linear-gradient(135deg, #f7fee7, #d9f99d); color: #365314; border-left: 4px solid #65a30d; }");
            writer.println("        .prod { background: linear-gradient(135deg, #fff1f2, #fda4af); color: #881337; border-left: 4px solid #e11d48; }");
            writer.println("        .anim.legend-sample { border: 3px solid #16a34a !important; }");
            writer.println("        .behv.legend-sample { border: 3px solid #2563eb !important; }");
            writer.println("        .bioc.legend-sample { border: 3px solid #d97706 !important; }");
            writer.println("        .biom.legend-sample { border: 3px solid #ea580c !important; }");
            writer.println("        .chem.legend-sample { border: 3px solid #059669 !important; }");
            writer.println("        .cbio.legend-sample { border: 3px solid #be185d !important; }");
            writer.println("        .cell.legend-sample { border: 3px solid #7c3aed !important; }");
            writer.println("        .comp.legend-sample { border: 3px solid #0284c7 !important; }");
            writer.println("        .eaev.legend-sample { border: 3px solid #22c55e !important; }");
            writer.println("        .eemr.legend-sample { border: 3px solid #9333ea !important; }");
            writer.println("        .eemt.legend-sample { border: 3px solid #f97316 !important; }");
            writer.println("        .math.legend-sample { border: 3px solid #ca8a04 !important; }");
            writer.println("        .mcro.legend-sample { border: 3px solid #0ea5e9 !important; }");
            writer.println("        .phys.legend-sample { border: 3px solid #c026d3 !important; }");
            writer.println("        .plnt.legend-sample { border: 3px solid #65a30d !important; }");
            writer.println("        .prod.legend-sample { border: 3px solid #e11d48 !important; }");
            writer.println("        .empty.legend-sample { border: 3px solid #cbd5e0 !important; }");
            writer.println("        .empty.reserved.legend-sample { border: 3px solid #ff6b6b !important; color: #b80000 !important; }");
            writer.println("        .team::before { content: '👥'; position: absolute; top: 1px; right: 2px; font-size: 8px; opacity: 0.7; }");
            writer.println("        .team { box-shadow: inset 0 0 0 2px currentColor; border-radius: 4px; }");
            writer.println("        .legend { margin-top: 24px; padding: 12px 10px; background: linear-gradient(135deg, #f8f9fa, #e9ecef); border-radius: 10px; }");
            writer.println("        .legend h3 { color: #495057; margin-bottom: 10px; font-size: 1em; font-weight: 500; }");
            writer.println("        .legend-table { font-size: 11px; width: 100%; border-collapse: collapse; border-spacing: 0; }");
            writer.println("        .legend-table td { padding: 1px 2px; vertical-align: middle; line-height: 1; }");
            writer.println("        .legend-table td:first-child { padding-right: 2px; }");
            // Legend layout: two vertical columns (first half left, second half right) using flex
            writer.println("        .legend-grid { display: flex; gap: 32px; align-items: flex-start; }");
            writer.println("        .legend-col { flex: 1; display: flex; flex-direction: column; gap: 4px; }");
            writer.println("        .legend-item { display: flex; align-items: center; font-size: 11px; line-height: 1.1; }");
            writer.println("        .legend-item .legend-sample { margin-right: 6px !important; }");
            writer.println("        .legend-item span { line-height: 1.1; }");
            writer.println("        @media (max-width: 700px) { .legend-grid { flex-direction: column; gap: 12px; } .legend-col { width: 100%; } }");
            writer.println("        .legend-sample { width: 32px !important; height: 16px !important; min-height: 16px !important; border-radius: 3px; font-size: 8px; display: flex; align-items: center; justify-content: center; border: 3px solid transparent !important; padding: 0 !important; }");
            writer.println("        .slot.legend-sample { min-width: 32px !important; width: 32px !important; height: 16px !important; min-height: 16px !important; padding: 0 !important; }");
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
            
            Integer previousRowNumber = null;
            for (int rowNumber : orderedRows) {
                List<SlotAssignment> rowAssignments = assignmentsByRow.get(rowNumber);
                
                // Sort by slot ID to get proper order
                rowAssignments.sort((a, b) -> Integer.compare(a.getTableSlotID(), b.getTableSlotID()));
                
                // Determine vertical spacing based on pairing rule
                int marginTop;
                if (rowMarginTop != null && rowMarginTop.containsKey(rowNumber)) {
                    marginTop = rowMarginTop.get(rowNumber);
                } else if (previousRowNumber == null) {
                    marginTop = 0;
                } else if (applyPairSpacing && previousRowNumber % 2 == 0 && rowNumber == previousRowNumber + 1) {
                    marginTop = 4;
                } else {
                    marginTop = 24;
                }
                previousRowNumber = rowNumber;
                writer.println("            <div class='row-wrapper' style='margin-top: " + marginTop + "px; margin-bottom: 0;'>");
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
                    // Removed table-level team icon; slot-level team markers retained
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
            // Build legend items dynamically, sort alphabetically, split into two columns
            List<String[]> legendItems = new ArrayList<>();
            legendItems.add(new String[]{"anim","Animal Sciences (ANIM)",""});
            legendItems.add(new String[]{"behv","Behavioral & Social Sciences (BEHV)",""});
            legendItems.add(new String[]{"bioc","Biochemistry (BIOC)",""});
            legendItems.add(new String[]{"biom","Biomedical/Health Sci & Eng (BIOM)",""});
            legendItems.add(new String[]{"cbio","Computational Bio & Info (CBIO)",""});
            legendItems.add(new String[]{"cell","Cellular & Molecular Biology (CELL)",""});
            legendItems.add(new String[]{"chem","Chemistry (CHEM)",""});
            legendItems.add(new String[]{"comp","Computer Science (COMP)",""});
            legendItems.add(new String[]{"eaev","Earth & Environmental Sci (EAEV)",""});
            legendItems.add(new String[]{"eemr","Eng: Electrical, Mech, Robotics (EEMR)",""});
            legendItems.add(new String[]{"eemt","Eng: Energy, Materials, Transport (EEMT)",""});
            legendItems.add(new String[]{"math","Mathematics (MATH)",""});
            legendItems.add(new String[]{"mcro","Microbiology (MCRO)",""});
            legendItems.add(new String[]{"phys","Physics & Astronomy (PHYS)",""});
            legendItems.add(new String[]{"plnt","Plant Sciences (PLNT)",""});
            legendItems.add(new String[]{"prod","Product Testing (PROD)",""});
            legendItems.add(new String[]{"empty","Unassigned slot","EMPTY"});
            legendItems.add(new String[]{"empty reserved","Reserved slot","RSRVD"});
            legendItems.sort(Comparator.comparing(a -> a[1].toLowerCase()));
            int half = legendItems.size() / 2; // even count splits evenly
            List<String[]> col1 = legendItems.subList(0, half);
            List<String[]> col2 = legendItems.subList(half, legendItems.size());
            writer.println("            <div class='legend-grid'>");
            writer.println("              <div class='legend-col'>");
            for (String[] it : col1) {
                String classes = it[0];
                String label = it[1];
                String boxText = it[2];
                writer.println("                <div class='legend-item'><div class='slot " + classes + " legend-sample'>" + boxText + "</div><span>" + label + "</span></div>");
            }
            writer.println("              </div>");
            writer.println("              <div class='legend-col'>");
            for (String[] it : col2) {
                String classes = it[0];
                String label = it[1];
                String boxText = it[2];
                writer.println("                <div class='legend-item'><div class='slot " + classes + " legend-sample'>" + boxText + "</div><span>" + label + "</span></div>");
            }
            writer.println("              </div>");
            writer.println("            </div>");
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
        } else if (assignment.isReserved()) {
            return "<strong>RSRVD</strong>";
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
        } else if (assignment.isReserved()) {
            cssClass += "empty reserved";
        } else {
            cssClass += "empty";
        }
        return cssClass;
    }
}