package com.sciencefair.service;

import com.sciencefair.model.ScienceProject;
import com.sciencefair.model.SlotAssignment;
import com.sciencefair.model.TableSlot;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for assigning science fair projects to table slots
 * Implementation follows the detailed specification pseudocode exactly
 */
public class ScienceFairAssignmentService {
    
    /**
     * Assigns projects to table slots following the specification's pseudocode
     * 
     * The algorithm iterates slot-by-slot and applies these rules:
     * 1. Reserved slots are left unassigned
     * 2. Team projects at row end are left unassigned (need full table)
     * 3. Category boundaries get empty slots (except after row end)
     * 4. Team projects on even slots are left unassigned (need odd start)
     */
    public List<SlotAssignment> assignProjectsToSlots(List<ScienceProject> projects, List<TableSlot> tableSlots) {
        List<SlotAssignment> assignments = new ArrayList<>();
        
        // A. Initialization (following specification pseudocode exactly)
        int currentProjectIndex = 0;
        boolean lastCategoryEndedAtRowEnd = false;
        boolean categorySpacingUsed = false; // Track if we've already used the category spacing slot
        
        // Sort table slots by row and slot ID to ensure proper order
        List<TableSlot> sortedSlots = tableSlots.stream()
            .sorted(Comparator.comparing(TableSlot::getRow).thenComparing(TableSlot::getTableSlotID))
            .collect(Collectors.toList());
        
        // B. Main Loop: iterate through table slots (CSV #1) and apply assignment rules
        for (int slotIndex = 0; slotIndex < sortedSlots.size(); slotIndex++) {
            TableSlot tableSlot = sortedSlots.get(slotIndex);
            int currentRow = tableSlot.getRow();
            int currentSlot = tableSlot.getTableSlotID();
            
            // Rule 1: currentSlot is reserved in Input CSV #1
            if (tableSlot.isReserved()) {
                assignments.add(new SlotAssignment(currentRow, currentSlot));
                continue; // go to next table slot iteration
            }
            
            // Check if we still have projects to assign
            if (currentProjectIndex >= projects.size()) {
                assignments.add(new SlotAssignment(currentRow, currentSlot));
                continue;
            }
            
            ScienceProject currentProject = projects.get(currentProjectIndex);
            int rowNumSlots = getRowNumSlots(currentRow);
            
            // Rule 2: currentProject is a team project, AND currentSlot is the last slot in a row
            if (currentProject.isTeam() && currentSlot == rowNumSlots) {
                assignments.add(new SlotAssignment(currentRow, currentSlot));
                continue; // go to next table slot iteration (don't assign project)
            }
            
            // Rule 3: currentProject is the first in its category AND currentSlot is not slot #1 in a row
            // AND the last category didn't end at a row end AND we haven't used category spacing yet
            if (currentProject.isFirstInCat() && currentSlot > 1 && !lastCategoryEndedAtRowEnd && !categorySpacingUsed) {
                assignments.add(new SlotAssignment(currentRow, currentSlot));
                categorySpacingUsed = true; // Mark that we've used the category spacing for this category
                continue; // go to next table slot iteration (don't assign project, try again on next slot)
            }
            
            // Rule 4: currentProject is a team project AND currentSlot is even-numbered
            if (currentProject.isTeam() && (currentSlot % 2 == 0)) {
                assignments.add(new SlotAssignment(currentRow, currentSlot));
                continue; // go to next table slot iteration (don't assign project)
            }
            
            // If we've gotten to this point, we have a valid assignment
            if (currentProject.isTeam()) {
                // Team projects get two slots
                assignments.add(new SlotAssignment(currentRow, currentSlot, currentProject));
                
                // Check if we have a next slot for the second part of the team project
                if (slotIndex + 1 < sortedSlots.size()) {
                    slotIndex++; // move to next slot
                    TableSlot nextSlot = sortedSlots.get(slotIndex);
                    assignments.add(new SlotAssignment(nextSlot.getRow(), nextSlot.getTableSlotID(), currentProject));
                    
                    // Check if this team project ended at row end
                    lastCategoryEndedAtRowEnd = (nextSlot.getTableSlotID() == getRowNumSlots(nextSlot.getRow()));
                }
                
                // Move to next project after successful team assignment
                currentProjectIndex++;
            } else {
                // Solo projects get one slot
                assignments.add(new SlotAssignment(currentRow, currentSlot, currentProject));
                
                // Check if this solo project ended at row end
                lastCategoryEndedAtRowEnd = (currentSlot == rowNumSlots);
                
                // Move to next project after successful solo assignment
                currentProjectIndex++;
            }
            
            // Check if we're about to start a new category for the flag logic
            if (currentProjectIndex < projects.size()) {
                ScienceProject nextProject = projects.get(currentProjectIndex);
                if (!nextProject.getCategory().equals(currentProject.getCategory())) {
                    // We've finished a category, reset category spacing flag for the next category
                    categorySpacingUsed = false;
                }
            }
        }
        
        return assignments;
    }
    
    /**
     * Gets the number of slots in a given row based on specification
     */
    private int getRowNumSlots(int row) {
        // Based on the specification: rows 1-3 have 12 slots, rows 4-5 have 14 slots
        if (row >= 1 && row <= 3) {
            return 12;
        } else if (row >= 4 && row <= 5) {
            return 14;
        }
        // Default fallback
        return 12;
    }
    
    /**
     * Generates a summary report of the assignment results
     */
    public String generateAssignmentSummary(List<SlotAssignment> assignments, List<ScienceProject> projects, List<TableSlot> tableSlots) {
        StringBuilder summary = new StringBuilder();
        
        long totalSlots = tableSlots.size(); // Use tableSlots size, not assignments
        long availableSlots = tableSlots.stream().filter(TableSlot::isAvailable).count();
        long reservedSlots = tableSlots.stream().filter(TableSlot::isReserved).count();
        long assignedSlots = assignments.stream().filter(a -> !a.isUnassigned()).count();
        long unassignedSlots = availableSlots - assignedSlots;
        
        long totalProjects = projects.size();
        long assignedProjects = assignedSlots;
        long unassignedProjects = totalProjects - assignedProjects;
        
        summary.append("=== SCIENCE FAIR ASSIGNMENT SUMMARY ===\n");
        summary.append(String.format("Total table slots: %d\n", totalSlots));
        summary.append(String.format("Available slots: %d\n", availableSlots));
        summary.append(String.format("Reserved slots: %d\n", reservedSlots));
        summary.append(String.format("Slots assigned: %d\n", assignedSlots));
        summary.append(String.format("Slots unassigned: %d\n", unassignedSlots));
        summary.append(String.format("Total projects: %d\n", totalProjects));
        summary.append(String.format("Projects assigned: %d\n", assignedProjects));
        summary.append(String.format("Projects unassigned: %d\n", unassignedProjects));
        
        // Category breakdown
        Map<String, Long> categoryCount = projects.stream()
            .collect(Collectors.groupingBy(ScienceProject::getCategory, Collectors.counting()));
        
        summary.append("\n=== PROJECTS BY CATEGORY ===\n");
        categoryCount.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> summary.append(String.format("- %s: %d projects\n", entry.getKey(), entry.getValue())));
        
        if (unassignedProjects > 0) {
            summary.append("\n=== UNASSIGNED PROJECTS ===\n");
            Set<Integer> assignedProjectIds = assignments.stream()
                .filter(a -> !a.isUnassigned())
                .map(SlotAssignment::getProjectID)
                .collect(Collectors.toSet());
            
            projects.stream()
                .filter(p -> !assignedProjectIds.contains(p.getProjectID()))
                .forEach(p -> summary.append(String.format("- Project %d (%s)\n", p.getProjectID(), p.getCategory())));
        }
        
        return summary.toString();
    }
}