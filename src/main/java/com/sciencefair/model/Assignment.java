package com.sciencefair.model;

/**
 * Represents an assignment of a project to a table slot
 */
public class Assignment {
    private String tableId;
    private String projectId;
    private String assignmentReason;
    private int compatibilityScore;
    
    public Assignment() {}
    
    public Assignment(String tableId, String projectId, String assignmentReason, int compatibilityScore) {
        this.tableId = tableId;
        this.projectId = projectId;
        this.assignmentReason = assignmentReason;
        this.compatibilityScore = compatibilityScore;
    }
    
    // Getters and Setters
    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }
    
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    
    public String getAssignmentReason() { return assignmentReason; }
    public void setAssignmentReason(String assignmentReason) { this.assignmentReason = assignmentReason; }
    
    public int getCompatibilityScore() { return compatibilityScore; }
    public void setCompatibilityScore(int compatibilityScore) { this.compatibilityScore = compatibilityScore; }
    
    public boolean isAssigned() {
        return projectId != null && !projectId.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        if (isAssigned()) {
            return String.format("Assignment{table='%s', project='%s', score=%d}", 
                               tableId, projectId, compatibilityScore);
        } else {
            return String.format("Assignment{table='%s', UNASSIGNED}", tableId);
        }
    }
}