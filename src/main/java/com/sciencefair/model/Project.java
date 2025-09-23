package com.sciencefair.model;

/**
 * Represents a science fair project with its properties and requirements
 */
public class Project {
    private String projectId;
    private String projectName;
    private String studentName;
    private String category;
    private String grade;
    private boolean requiresElectricity;
    private boolean requiresWater;
    private String specialRequirements;
    private int estimatedSpace; // in square feet
    
    public Project() {}
    
    public Project(String projectId, String projectName, String studentName, 
                   String category, String grade, boolean requiresElectricity, 
                   boolean requiresWater, String specialRequirements, int estimatedSpace) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.studentName = studentName;
        this.category = category;
        this.grade = grade;
        this.requiresElectricity = requiresElectricity;
        this.requiresWater = requiresWater;
        this.specialRequirements = specialRequirements;
        this.estimatedSpace = estimatedSpace;
    }
    
    // Getters and Setters
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    
    public boolean isRequiresElectricity() { return requiresElectricity; }
    public void setRequiresElectricity(boolean requiresElectricity) { this.requiresElectricity = requiresElectricity; }
    
    public boolean isRequiresWater() { return requiresWater; }
    public void setRequiresWater(boolean requiresWater) { this.requiresWater = requiresWater; }
    
    public String getSpecialRequirements() { return specialRequirements; }
    public void setSpecialRequirements(String specialRequirements) { this.specialRequirements = specialRequirements; }
    
    public int getEstimatedSpace() { return estimatedSpace; }
    public void setEstimatedSpace(int estimatedSpace) { this.estimatedSpace = estimatedSpace; }
    
    @Override
    public String toString() {
        return String.format("Project{id='%s', name='%s', student='%s', category='%s', grade='%s'}", 
                           projectId, projectName, studentName, category, grade);
    }
}