package com.sciencefair.model;

/**
 * Represents a science fair project entry
 */
public class ScienceProject {
    private int projectID;
    private boolean isTeam;
    private boolean isFirstInCat;
    private String category;
    
    public ScienceProject() {}
    
    public ScienceProject(int projectID, boolean isTeam, boolean isFirstInCat, String category) {
        this.projectID = projectID;
        this.isTeam = isTeam;
        this.isFirstInCat = isFirstInCat;
        this.category = category;
    }
    
    // Getters and Setters
    public int getProjectID() { return projectID; }
    public void setProjectID(int projectID) { this.projectID = projectID; }
    
    public boolean isTeam() { return isTeam; }
    public void setTeam(boolean team) { isTeam = team; }
    
    public boolean isFirstInCat() { return isFirstInCat; }
    public void setFirstInCat(boolean firstInCat) { isFirstInCat = firstInCat; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    @Override
    public String toString() {
        return String.format("ScienceProject{id=%d, team=%s, firstInCat=%s, category='%s'}", 
                           projectID, isTeam, isFirstInCat, category);
    }
}