package com.sciencefair.model;

/**
 * Represents a table slot assignment result
 */
public class SlotAssignment {
    private int row;
    private int tableSlotID;
    private boolean isUnassigned;
    private Integer projectID; // Nullable for unassigned slots
    private Boolean isTeam; // Nullable for unassigned slots
    private String category; // Nullable for unassigned slots
    
    public SlotAssignment() {}
    
    public SlotAssignment(int row, int tableSlotID) {
        this.row = row;
        this.tableSlotID = tableSlotID;
        this.isUnassigned = true;
        this.projectID = null;
        this.isTeam = null;
        this.category = null;
    }
    
    public SlotAssignment(int row, int tableSlotID, ScienceProject project) {
        this.row = row;
        this.tableSlotID = tableSlotID;
        this.isUnassigned = false;
        this.projectID = project.getProjectID();
        this.isTeam = project.isTeam();
        this.category = project.getCategory();
    }
    
    // Getters and Setters
    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }
    
    public int getTableSlotID() { return tableSlotID; }
    public void setTableSlotID(int tableSlotID) { this.tableSlotID = tableSlotID; }
    
    public boolean isUnassigned() { return isUnassigned; }
    public void setUnassigned(boolean unassigned) { isUnassigned = unassigned; }
    
    public Integer getProjectID() { return projectID; }
    public void setProjectID(Integer projectID) { 
        this.projectID = projectID;
        this.isUnassigned = (projectID == null);
    }
    
    public Boolean getIsTeam() { return isTeam; }
    public void setIsTeam(Boolean isTeam) { this.isTeam = isTeam; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public void assignProject(ScienceProject project) {
        this.projectID = project.getProjectID();
        this.isTeam = project.isTeam();
        this.category = project.getCategory();
        this.isUnassigned = false;
    }
    
    @Override
    public String toString() {
        if (isUnassigned) {
            return String.format("SlotAssignment{row=%d, slotID=%d, UNASSIGNED}", row, tableSlotID);
        } else {
            return String.format("SlotAssignment{row=%d, slotID=%d, project=%d, team=%s}", 
                               row, tableSlotID, projectID, isTeam);
        }
    }
}