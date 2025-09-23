package com.sciencefair.model;

/**
 * Represents a table slot available for science fair projects
 */
public class Table {
    private String tableId;
    private String location;
    private int capacity; // in square feet
    private boolean hasElectricity;
    private boolean hasWater;
    private String category; // preferred category for this table
    private String gradeRange; // e.g., "K-2", "3-5", "6-8", "9-12"
    private boolean isAccessible; // wheelchair accessible
    private String notes;
    
    public Table() {}
    
    public Table(String tableId, String location, int capacity, boolean hasElectricity, 
                 boolean hasWater, String category, String gradeRange, boolean isAccessible, String notes) {
        this.tableId = tableId;
        this.location = location;
        this.capacity = capacity;
        this.hasElectricity = hasElectricity;
        this.hasWater = hasWater;
        this.category = category;
        this.gradeRange = gradeRange;
        this.isAccessible = isAccessible;
        this.notes = notes;
    }
    
    // Getters and Setters
    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    
    public boolean isHasElectricity() { return hasElectricity; }
    public void setHasElectricity(boolean hasElectricity) { this.hasElectricity = hasElectricity; }
    
    public boolean isHasWater() { return hasWater; }
    public void setHasWater(boolean hasWater) { this.hasWater = hasWater; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getGradeRange() { return gradeRange; }
    public void setGradeRange(String gradeRange) { this.gradeRange = gradeRange; }
    
    public boolean isAccessible() { return isAccessible; }
    public void setAccessible(boolean accessible) { isAccessible = accessible; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    /**
     * Checks if this table can accommodate the given project based on requirements
     */
    public boolean canAccommodate(Project project) {
        // Check space requirements
        if (project.getEstimatedSpace() > this.capacity) {
            return false;
        }
        
        // Check electricity requirements
        if (project.isRequiresElectricity() && !this.hasElectricity) {
            return false;
        }
        
        // Check water requirements
        if (project.isRequiresWater() && !this.hasWater) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculates a compatibility score between this table and a project
     * Higher score means better match
     */
    public int getCompatibilityScore(Project project) {
        if (!canAccommodate(project)) {
            return 0;
        }
        
        int score = 100;
        
        // Prefer matching categories
        if (this.category != null && this.category.equalsIgnoreCase(project.getCategory())) {
            score += 50;
        }
        
        // Prefer appropriate grade ranges
        if (isGradeCompatible(project.getGrade())) {
            score += 30;
        }
        
        // Slight penalty for oversized tables (encourage efficient use)
        int spaceEfficiency = (project.getEstimatedSpace() * 100) / this.capacity;
        if (spaceEfficiency > 80) {
            score += 20;
        } else if (spaceEfficiency < 40) {
            score -= 10;
        }
        
        return score;
    }
    
    private boolean isGradeCompatible(String projectGrade) {
        if (this.gradeRange == null || projectGrade == null) {
            return true; // No restriction
        }
        
        // Simple grade range matching - can be expanded
        if (gradeRange.equals("K-2")) {
            return projectGrade.matches("[K012]");
        } else if (gradeRange.equals("3-5")) {
            return projectGrade.matches("[345]");
        } else if (gradeRange.equals("6-8")) {
            return projectGrade.matches("[678]");
        } else if (gradeRange.equals("9-12")) {
            return projectGrade.matches("(9|10|11|12)");
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return String.format("Table{id='%s', location='%s', capacity=%d, electricity=%s, water=%s}", 
                           tableId, location, capacity, hasElectricity, hasWater);
    }
}