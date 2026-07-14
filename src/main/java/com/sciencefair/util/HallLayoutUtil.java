package com.sciencefair.util;

import com.sciencefair.model.TableSlot;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles center-aisle hall layout: assignment traversal order, display row order,
 * and per-row left/right snake direction.
 */
public final class HallLayoutUtil {

    public static final String PREF_AISLE_PIVOT_ROW = "aislePivotRow";
    public static final String PREF_AISLE_AUTO_DETECT = "aislePivotAutoDetect";

    private final int aislePivotRow;

    /**
     * Splits hall rows across the center aisle with the left side getting the extra row when odd.
     * e.g. 6 rows → pivot 3 (3 left, 3 right); 7 rows → pivot 4 (4 left, 3 right).
     */
    public static int computeAutoPivotRow(int totalRows) {
        if (totalRows <= 0) {
            return 0;
        }
        return (totalRows + 1) / 2;
    }

    public HallLayoutUtil(int aislePivotRow) {
        this.aislePivotRow = Math.max(0, aislePivotRow);
    }

    public static HallLayoutUtil disabled() {
        return new HallLayoutUtil(0);
    }

    public boolean isAisleLayoutEnabled() {
        return aislePivotRow > 0;
    }

    public int getAislePivotRow() {
        return aislePivotRow;
    }

    public List<Integer> getSortedRows(Collection<Integer> rows) {
        return rows.stream().sorted().collect(Collectors.toList());
    }

    /**
     * Row order for project assignment when aisle layout is active:
     * left side ascending (1..pivot), then right side ascending (pivot+1..max).
     * After crossing the aisle, projects fill row 4 first (bottom right), then 5, then 6.
     * Use {@link #getRightSideDisplayRows} for the visual column order (6, 5, 4 top-down).
     */
    public List<Integer> getTraversalRowOrder(Collection<Integer> rows) {
        List<Integer> sorted = getSortedRows(rows);
        if (sorted.isEmpty()) {
            return sorted;
        }
        if (!isAisleLayoutEnabled() || aislePivotRow >= sorted.get(sorted.size() - 1)) {
            return sorted;
        }

        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();
        for (int row : sorted) {
            if (row <= aislePivotRow) {
                left.add(row);
            } else {
                right.add(row);
            }
        }

        List<Integer> result = new ArrayList<>(left);
        result.addAll(right);
        return result;
    }

    /**
     * Whether tables/slots within a row should render right-to-left for snake flow.
     * After crossing the aisle, the first row on the right keeps the same direction
     * as the last row on the left (left-to-right).
     */
    public boolean shouldReverseRow(int row, Collection<Integer> rows) {
        return buildRowReverseMap(rows).getOrDefault(row, row % 2 == 0);
    }

    public Map<Integer, Boolean> buildRowReverseMap(Collection<Integer> rows) {
        List<Integer> order = getTraversalRowOrder(rows);
        Map<Integer, Boolean> reverseMap = new LinkedHashMap<>();

        if (!isAisleLayoutEnabled()) {
            for (int row : order) {
                reverseMap.put(row, row % 2 == 0);
            }
            return reverseMap;
        }

        boolean reverse = false;
        Integer previousRow = null;
        for (int row : order) {
            if (previousRow != null) {
                boolean crossedAisle = previousRow <= aislePivotRow && row > aislePivotRow;
                if (crossedAisle) {
                    // First right-side row flows left-to-right from the aisle (continues after last left row)
                    reverse = false;
                } else {
                    reverse = !reverse;
                }
            }
            reverseMap.put(row, reverse);
            previousRow = row;
        }
        return reverseMap;
    }

    public List<Integer> getLeftSideRows(Collection<Integer> rows) {
        return getSortedRows(rows).stream()
                .filter(r -> !isAisleLayoutEnabled() || r <= aislePivotRow)
                .collect(Collectors.toList());
    }

    public List<Integer> getRightSideRows(Collection<Integer> rows) {
        if (!isAisleLayoutEnabled()) {
            return Collections.emptyList();
        }
        return getSortedRows(rows).stream()
                .filter(r -> r > aislePivotRow)
                .collect(Collectors.toList());
    }

    /**
     * Right-side rows for side-by-side display: descending (6, 5, 4) top-down so row 4
     * sits at the bottom (across from row 3) while assignment still fills 4, 5, 6.
     */
    public List<Integer> getRightSideDisplayRows(Collection<Integer> rows) {
        List<Integer> rightRows = new ArrayList<>(getRightSideRows(rows));
        Collections.reverse(rightRows);
        return rightRows;
    }

    public boolean crossesAisleAfter(Integer previousRow, int nextRow) {
        return isAisleLayoutEnabled()
                && previousRow != null
                && previousRow <= aislePivotRow
                && nextRow > aislePivotRow;
    }

    /**
     * Reorders table slots for the assignment dealer loop.
     */
    public List<TableSlot> orderSlotsForAssignment(List<TableSlot> tableSlots) {
        Map<Integer, List<TableSlot>> byRow = tableSlots.stream()
                .collect(Collectors.groupingBy(TableSlot::getRow, LinkedHashMap::new, Collectors.toList()));

        List<Integer> rowOrder = getTraversalRowOrder(byRow.keySet());
        List<TableSlot> ordered = new ArrayList<>();
        for (int row : rowOrder) {
            List<TableSlot> rowSlots = byRow.get(row);
            if (rowSlots == null) {
                continue;
            }
            rowSlots.sort(Comparator.comparing(TableSlot::getTableSlotID));
            ordered.addAll(rowSlots);
        }
        return ordered;
    }

    /**
     * Maps each row and 1-based table position to its snake-flow table number.
     */
    public Map<Integer, Map<Integer, Integer>> buildSnakeTableNumbers(Map<Integer, Integer> rowTableCounts) {
        List<Integer> rowOrder = getTraversalRowOrder(rowTableCounts.keySet());
        Map<Integer, Map<Integer, Integer>> tableNumbers = new HashMap<>();
        int nextTableNumber = 1;

        for (int row : rowOrder) {
            int tablesInRow = rowTableCounts.getOrDefault(row, 0);
            Map<Integer, Integer> rowMap = new HashMap<>();
            for (int position = 1; position <= tablesInRow; position++) {
                rowMap.put(position, nextTableNumber++);
            }
            tableNumbers.put(row, rowMap);
        }
        return tableNumbers;
    }

    public int resolveTableNumber(
            Map<Integer, Map<Integer, Integer>> snakeTableNumbers,
            int row,
            int tableIndex,
            int tablesInRow,
            boolean reverseRow) {
        int tablePosition = reverseRow
                ? tablesInRow - tableIndex
                : tableIndex + 1;
        Map<Integer, Integer> rowMap = snakeTableNumbers.get(row);
        if (rowMap == null) {
            return tablePosition;
        }
        return rowMap.getOrDefault(tablePosition, tablePosition);
    }
}
