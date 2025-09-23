package com.sciencefair.model;

/**
 * Represents a table slot in the science fair layout
 */
public class TableSlot {
    private int row;
    private int rowNumSlots;
    private int tableSlotID;
    private boolean isReserved;
    
    public TableSlot() {}
    
    public TableSlot(int row, int rowNumSlots, int tableSlotID, boolean isReserved) {
        this.row = row;
        this.rowNumSlots = rowNumSlots;
        this.tableSlotID = tableSlotID;
        this.isReserved = isReserved;
    }
    
    // Getters and Setters
    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }
    
    public int getRowNumSlots() { return rowNumSlots; }
    public void setRowNumSlots(int rowNumSlots) { this.rowNumSlots = rowNumSlots; }
    
    public int getTableSlotID() { return tableSlotID; }
    public void setTableSlotID(int tableSlotID) { this.tableSlotID = tableSlotID; }
    
    public boolean isReserved() { return isReserved; }
    public void setReserved(boolean reserved) { isReserved = reserved; }
    
    public boolean isAvailable() {
        return !isReserved;
    }
    
    @Override
    public String toString() {
        return String.format("TableSlot{row=%d, slotID=%d, reserved=%s}", 
                           row, tableSlotID, isReserved);
    }
}