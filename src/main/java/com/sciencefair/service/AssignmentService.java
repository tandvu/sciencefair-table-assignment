package com.sciencefair.service;

import com.sciencefair.model.Assignment;
import com.sciencefair.model.Project;
import com.sciencefair.model.Table;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for assigning science fair projects to table slots
 */
public class AssignmentService {
    
    /**
     * Assigns projects to tables using a greedy algorithm with scoring
     * 
     * Algorithm:
     * 1. Calculate compatibility scores for all project-table pairs
     * 2. Sort projects by priority (special requirements first, then by space needs)
     * 3. For each project, assign to the best available table
     * 4. Mark assigned tables as unavailable
     * 5. Create assignments for all tables (assigned or unassigned)
     */
    public List<Assignment> assignProjectsToTables(List<Project> projects, List<Table> tables) {
        List<Assignment> assignments = new ArrayList<>();
        Set<String> assignedTableIds = new HashSet<>();
        Set<String> assignedProjectIds = new HashSet<>();
        
        // Sort projects by priority for assignment
        List<Project> sortedProjects = prioritizeProjects(projects);
        
        // Assign each project to the best available table
        for (Project project : sortedProjects) {
            if (assignedProjectIds.contains(project.getProjectId())) {
                continue; // Already assigned
            }
            
            Table bestTable = findBestTable(project, tables, assignedTableIds);
            
            if (bestTable != null) {
                Assignment assignment = new Assignment(
                    bestTable.getTableId(),
                    project.getProjectId(),
                    generateAssignmentReason(project, bestTable),
                    bestTable.getCompatibilityScore(project)
                );
                assignments.add(assignment);
                assignedTableIds.add(bestTable.getTableId());
                assignedProjectIds.add(project.getProjectId());
            }
        }
        
        // Create unassigned entries for remaining tables
        for (Table table : tables) {
            if (!assignedTableIds.contains(table.getTableId())) {
                Assignment unassigned = new Assignment(
                    table.getTableId(),
                    null,
                    "No suitable project found",
                    0
                );
                assignments.add(unassigned);
            }
        }
        
        // Sort assignments by table ID for consistent output
        assignments.sort(Comparator.comparing(Assignment::getTableId));
        
        return assignments;
    }
    
    /**
     * Prioritizes projects for assignment
     * Priority order:
     * 1. Projects with special requirements (electricity, water)
     * 2. Projects with larger space requirements
     * 3. Projects by grade level (younger first)
     */
    private List<Project> prioritizeProjects(List<Project> projects) {
        return projects.stream()
            .sorted((p1, p2) -> {
                // Special requirements first
                int p1SpecialCount = (p1.isRequiresElectricity() ? 1 : 0) + (p1.isRequiresWater() ? 1 : 0);
                int p2SpecialCount = (p2.isRequiresElectricity() ? 1 : 0) + (p2.isRequiresWater() ? 1 : 0);
                
                if (p1SpecialCount != p2SpecialCount) {
                    return Integer.compare(p2SpecialCount, p1SpecialCount); // Descending
                }
                
                // Larger space requirements next
                if (p1.getEstimatedSpace() != p2.getEstimatedSpace()) {
                    return Integer.compare(p2.getEstimatedSpace(), p1.getEstimatedSpace()); // Descending
                }
                
                // Younger grades first (assuming they need more accessible locations)
                return compareGrades(p1.getGrade(), p2.getGrade());
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Finds the best available table for a project
     */
    private Table findBestTable(Project project, List<Table> tables, Set<String> assignedTableIds) {
        return tables.stream()
            .filter(table -> !assignedTableIds.contains(table.getTableId()))
            .filter(table -> table.canAccommodate(project))
            .max(Comparator.comparingInt(table -> table.getCompatibilityScore(project)))
            .orElse(null);
    }
    
    /**
     * Generates a human-readable reason for the assignment
     */
    private String generateAssignmentReason(Project project, Table table) {
        List<String> reasons = new ArrayList<>();
        
        if (table.getCategory() != null && table.getCategory().equalsIgnoreCase(project.getCategory())) {
            reasons.add("Category match");
        }
        
        if (project.isRequiresElectricity() && table.isHasElectricity()) {
            reasons.add("Electricity available");
        }
        
        if (project.isRequiresWater() && table.isHasWater()) {
            reasons.add("Water available");
        }
        
        if (isGradeCompatible(table.getGradeRange(), project.getGrade())) {
            reasons.add("Grade-appropriate");
        }
        
        int spaceEfficiency = (project.getEstimatedSpace() * 100) / table.getCapacity();
        if (spaceEfficiency > 80) {
            reasons.add("Efficient space use");
        }
        
        if (reasons.isEmpty()) {
            return "Best available match";
        }
        
        return String.join(", ", reasons);
    }
    
    /**
     * Compares grades for sorting (K, 1, 2, ... 12)
     */
    private int compareGrades(String grade1, String grade2) {
        int g1 = gradeToNumber(grade1);
        int g2 = gradeToNumber(grade2);
        return Integer.compare(g1, g2);
    }
    
    /**
     * Converts grade string to number for comparison
     */
    private int gradeToNumber(String grade) {
        if (grade == null) return 999;
        if (grade.equalsIgnoreCase("K")) return 0;
        try {
            return Integer.parseInt(grade);
        } catch (NumberFormatException e) {
            return 999; // Unknown grades go last
        }
    }
    
    /**
     * Checks if a grade is compatible with a grade range
     */
    private boolean isGradeCompatible(String gradeRange, String projectGrade) {
        if (gradeRange == null || projectGrade == null) {
            return true; // No restriction
        }
        
        int grade = gradeToNumber(projectGrade);
        
        if (gradeRange.equals("K-2")) {
            return grade >= 0 && grade <= 2;
        } else if (gradeRange.equals("3-5")) {
            return grade >= 3 && grade <= 5;
        } else if (gradeRange.equals("6-8")) {
            return grade >= 6 && grade <= 8;
        } else if (gradeRange.equals("9-12")) {
            return grade >= 9 && grade <= 12;
        }
        
        return true; // Default to compatible
    }
    
    /**
     * Generates a summary report of the assignment results
     */
    public String generateAssignmentSummary(List<Assignment> assignments, List<Project> projects) {
        StringBuilder summary = new StringBuilder();
        
        long assignedTables = assignments.stream().filter(Assignment::isAssigned).count();
        long totalTables = assignments.size();
        long unassignedProjects = projects.size() - assignedTables;
        
        summary.append("=== ASSIGNMENT SUMMARY ===\n");
        summary.append(String.format("Total tables: %d\n", totalTables));
        summary.append(String.format("Tables assigned: %d\n", assignedTables));
        summary.append(String.format("Tables unassigned: %d\n", totalTables - assignedTables));
        summary.append(String.format("Total projects: %d\n", projects.size()));
        summary.append(String.format("Projects assigned: %d\n", assignedTables));
        summary.append(String.format("Projects unassigned: %d\n", unassignedProjects));
        
        if (unassignedProjects > 0) {
            summary.append("\n=== UNASSIGNED PROJECTS ===\n");
            Set<String> assignedProjectIds = assignments.stream()
                .filter(Assignment::isAssigned)
                .map(Assignment::getProjectId)
                .collect(Collectors.toSet());
            
            projects.stream()
                .filter(p -> !assignedProjectIds.contains(p.getProjectId()))
                .forEach(p -> summary.append(String.format("- %s (%s)\n", p.getProjectName(), p.getStudentName())));
        }
        
        return summary.toString();
    }
}