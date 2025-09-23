package com.sciencefair;

import com.sciencefair.model.ScienceProject;
import com.sciencefair.model.TableSlot;
import com.sciencefair.util.ScienceFairCsvUtil;

import java.util.List;

/**
 * Simple debug program to examine the data
 */
public class DebugDataReader {
    
    public static void main(String[] args) {
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
            
            // Count by row
            System.out.println("\nSlots by row:");
            tableSlots.stream()
                .collect(java.util.stream.Collectors.groupingBy(TableSlot::getRow, java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .forEach(entry -> System.out.println("Row " + entry.getKey() + ": " + entry.getValue() + " slots"));
            
            long availableSlots = tableSlots.stream().filter(slot -> !slot.isReserved()).count();
            System.out.println("\nAvailable slots: " + availableSlots);
            System.out.println("Reserved slots: " + (tableSlots.size() - availableSlots));
            
            System.out.println("\n=== DEBUGGING PROJECTS ===");
            List<ScienceProject> projects = ScienceFairCsvUtil.readScienceProjects("data/SampleInputCSV2.csv");
            System.out.println("Total projects loaded: " + projects.size());
            
            System.out.println("\nFirst 5 projects:");
            for (int i = 0; i < Math.min(5, projects.size()); i++) {
                ScienceProject project = projects.get(i);
                System.out.println(String.format("ID=%d, Team=%s, FirstInCat=%s, Category=%s", 
                    project.getProjectID(), project.isTeam(), project.isFirstInCat(), project.getCategory()));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}