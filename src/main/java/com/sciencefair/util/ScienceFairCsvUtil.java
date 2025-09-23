package com.sciencefair.util;

import com.sciencefair.model.ScienceProject;
import com.sciencefair.model.SlotAssignment;
import com.sciencefair.model.TableSlot;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reading and writing science fair CSV files
 */
public class ScienceFairCsvUtil {
    /**
     * Reads slot assignments from CSV file
     * Expected format: Row,tableSlotID,isUnassigned,projectID,isTeam,Category
     */
    public static List<SlotAssignment> readSlotAssignments(String filePath) throws IOException {
        List<SlotAssignment> assignments = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord csvRecord : csvParser) {
                SlotAssignment assignment = new SlotAssignment();
                assignment.setRow(Integer.parseInt(csvRecord.get("Row")));
                assignment.setTableSlotID(Integer.parseInt(csvRecord.get("tableSlotID")));
                assignment.setUnassigned(parseBoolean(csvRecord.get("isUnassigned")));
                String projectIdStr = csvRecord.get("projectID");
                assignment.setProjectID((projectIdStr != null && !projectIdStr.isEmpty()) ? Integer.parseInt(projectIdStr) : null);
                String isTeamStr = csvRecord.get("isTeam");
                assignment.setIsTeam((isTeamStr != null && !isTeamStr.isEmpty()) ? parseBoolean(isTeamStr) : null);
                assignment.setCategory(csvRecord.get("Category"));
                assignments.add(assignment);
            }
        }
        return assignments;
    }
    
    /**
     * Reads table slots from CSV file
     * Expected format: Row,rowNumSlots,tableSlotID,isReserved
     */
    public static List<TableSlot> readTableSlots(String filePath) throws IOException {
        List<TableSlot> tableSlots = new ArrayList<>();
        
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord csvRecord : csvParser) {
                TableSlot slot = new TableSlot();
                slot.setRow(Integer.parseInt(csvRecord.get("Row")));
                slot.setRowNumSlots(Integer.parseInt(csvRecord.get("rowNumSlots")));
                slot.setTableSlotID(Integer.parseInt(csvRecord.get("tableSlotID")));
                slot.setReserved(parseBoolean(csvRecord.get("isReserved")));
                
                tableSlots.add(slot);
            }
        }
        
        return tableSlots;
    }
    
    /**
     * Reads science projects from CSV file
     * Expected format: projectID,isTeam,isFirstInCat,Category
     */
    public static List<ScienceProject> readScienceProjects(String filePath) throws IOException {
        List<ScienceProject> projects = new ArrayList<>();
        
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord csvRecord : csvParser) {
                ScienceProject project = new ScienceProject();
                project.setProjectID(Integer.parseInt(csvRecord.get("projectID")));
                project.setTeam(parseBoolean(csvRecord.get("isTeam")));
                project.setFirstInCat(parseBoolean(csvRecord.get("isFirstInCat")));
                project.setCategory(csvRecord.get("Category"));
                
                projects.add(project);
            }
        }
        
        return projects;
    }
    
    /**
     * Writes slot assignments to CSV file
     * Output format: Row,tableSlotID,isUnassigned,projectID,isTeam,Category
     */
    public static void writeSlotAssignments(List<SlotAssignment> assignments, String filePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                 "Row", "tableSlotID", "isUnassigned", "projectID", "isTeam", "Category"))) {
            
            for (SlotAssignment assignment : assignments) {
                csvPrinter.printRecord(
                    assignment.getRow(),
                    assignment.getTableSlotID(),
                    assignment.isUnassigned() ? "TRUE" : "FALSE",
                    assignment.getProjectID() != null ? assignment.getProjectID().toString() : "",
                    assignment.getIsTeam() != null ? (assignment.getIsTeam() ? "TRUE" : "FALSE") : "",
                    assignment.getCategory() != null ? assignment.getCategory() : ""
                );
            }
        }
    }
    
    /**
     * Parse boolean value from string with various formats
     */
    private static boolean parseBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String v = value.trim().toUpperCase();
        return v.equals("TRUE") || v.equals("YES") || v.equals("1");
    }
}