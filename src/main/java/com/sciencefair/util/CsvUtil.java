package com.sciencefair.util;

import com.sciencefair.model.Project;
import com.sciencefair.model.Table;
import com.sciencefair.model.Assignment;
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
 * Utility class for reading and writing CSV files
 */
public class CsvUtil {
    
    /**
     * Reads projects from a CSV file
     * Expected CSV format: projectId,projectName,studentName,category,grade,requiresElectricity,requiresWater,specialRequirements,estimatedSpace
     */
    public static List<Project> readProjects(String filePath) throws IOException {
        List<Project> projects = new ArrayList<>();
        
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord csvRecord : csvParser) {
                Project project = new Project();
                project.setProjectId(csvRecord.get("projectId"));
                project.setProjectName(csvRecord.get("projectName"));
                project.setStudentName(csvRecord.get("studentName"));
                project.setCategory(csvRecord.get("category"));
                project.setGrade(csvRecord.get("grade"));
                project.setRequiresElectricity(parseBoolean(csvRecord.get("requiresElectricity")));
                project.setRequiresWater(parseBoolean(csvRecord.get("requiresWater")));
                project.setSpecialRequirements(csvRecord.get("specialRequirements"));
                project.setEstimatedSpace(parseInt(csvRecord.get("estimatedSpace"), 10));
                
                projects.add(project);
            }
        }
        
        return projects;
    }
    
    /**
     * Reads tables from a CSV file
     * Expected CSV format: tableId,location,capacity,hasElectricity,hasWater,category,gradeRange,isAccessible,notes
     */
    public static List<Table> readTables(String filePath) throws IOException {
        List<Table> tables = new ArrayList<>();
        
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord csvRecord : csvParser) {
                Table table = new Table();
                table.setTableId(csvRecord.get("tableId"));
                table.setLocation(csvRecord.get("location"));
                table.setCapacity(parseInt(csvRecord.get("capacity"), 20));
                table.setHasElectricity(parseBoolean(csvRecord.get("hasElectricity")));
                table.setHasWater(parseBoolean(csvRecord.get("hasWater")));
                table.setCategory(csvRecord.get("category"));
                table.setGradeRange(csvRecord.get("gradeRange"));
                table.setAccessible(parseBoolean(csvRecord.get("isAccessible")));
                table.setNotes(csvRecord.get("notes"));
                
                tables.add(table);
            }
        }
        
        return tables;
    }
    
    /**
     * Writes assignments to a CSV file
     * Output CSV format: tableId,projectId,studentName,projectName,category,assignmentReason,compatibilityScore
     */
    public static void writeAssignments(List<Assignment> assignments, List<Project> projects, String filePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                 "tableId", "projectId", "studentName", "projectName", "category", "assignmentReason", "compatibilityScore"))) {
            
            for (Assignment assignment : assignments) {
                String studentName = "";
                String projectName = "";
                String category = "";
                
                if (assignment.isAssigned()) {
                    // Find the project details
                    Project project = projects.stream()
                        .filter(p -> p.getProjectId().equals(assignment.getProjectId()))
                        .findFirst()
                        .orElse(null);
                    
                    if (project != null) {
                        studentName = project.getStudentName();
                        projectName = project.getProjectName();
                        category = project.getCategory();
                    }
                }
                
                csvPrinter.printRecord(
                    assignment.getTableId(),
                    assignment.getProjectId() != null ? assignment.getProjectId() : "",
                    studentName,
                    projectName,
                    category,
                    assignment.getAssignmentReason() != null ? assignment.getAssignmentReason() : "",
                    assignment.getCompatibilityScore()
                );
            }
        }
    }
    
    /**
     * Parse boolean value from string with fallback
     */
    private static boolean parseBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return value.trim().equalsIgnoreCase("true") || 
               value.trim().equalsIgnoreCase("yes") || 
               value.trim().equals("1");
    }
    
    /**
     * Parse integer value from string with fallback
     */
    private static int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}