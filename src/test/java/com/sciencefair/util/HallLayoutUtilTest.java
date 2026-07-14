package com.sciencefair.util;

import com.sciencefair.model.TableSlot;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HallLayoutUtilTest {

    @Test
    void disabledLayoutUsesAscendingRowsAndEvenRowReversal() {
        HallLayoutUtil layout = HallLayoutUtil.disabled();
        List<Integer> rows = Arrays.asList(1, 2, 3, 4);

        assertEquals(Arrays.asList(1, 2, 3, 4), layout.getTraversalRowOrder(rows));
        assertFalse(layout.shouldReverseRow(1, rows));
        assertTrue(layout.shouldReverseRow(2, rows));
        assertFalse(layout.shouldReverseRow(3, rows));
        assertTrue(layout.shouldReverseRow(4, rows));
    }

    @Test
    void aisleLayoutAssignsRightSideAscendingAfterAisle() {
        HallLayoutUtil layout = new HallLayoutUtil(3);
        List<Integer> rows = Arrays.asList(1, 2, 3, 4, 5, 6);

        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), layout.getTraversalRowOrder(rows));
    }

    @Test
    void firstRightSideRowKeepsLeftToRightDirectionAfterAisle() {
        HallLayoutUtil layout = new HallLayoutUtil(3);
        List<Integer> rows = Arrays.asList(1, 2, 3, 4, 5, 6);
        Map<Integer, Boolean> reverseMap = layout.buildRowReverseMap(rows);

        assertFalse(reverseMap.get(3));
        assertFalse(reverseMap.get(4));
        assertTrue(reverseMap.get(5));
        assertFalse(reverseMap.get(6));
    }

    @Test
    void rightSideDisplayRowsAreDescendingForColumnLayout() {
        HallLayoutUtil layout = new HallLayoutUtil(3);
        List<Integer> rows = Arrays.asList(1, 2, 3, 4, 5, 6);

        assertEquals(Arrays.asList(1, 2, 3), layout.getLeftSideRows(rows));
        assertEquals(Arrays.asList(6, 5, 4), layout.getRightSideDisplayRows(rows));
    }

    @Test
    void autoPivotSplitsRowsWithLeftSideGettingExtraWhenOdd() {
        assertEquals(0, HallLayoutUtil.computeAutoPivotRow(0));
        assertEquals(1, HallLayoutUtil.computeAutoPivotRow(1));
        assertEquals(1, HallLayoutUtil.computeAutoPivotRow(2));
        assertEquals(2, HallLayoutUtil.computeAutoPivotRow(3));
        assertEquals(2, HallLayoutUtil.computeAutoPivotRow(4));
        assertEquals(3, HallLayoutUtil.computeAutoPivotRow(5));
        assertEquals(3, HallLayoutUtil.computeAutoPivotRow(6));
        assertEquals(4, HallLayoutUtil.computeAutoPivotRow(7));
    }

    @Test
    void firstRightSideRowIsLeftToRightWhenPivotSplitsFourAndTwo() {
        HallLayoutUtil layout = new HallLayoutUtil(4);
        List<Integer> rows = Arrays.asList(1, 2, 3, 4, 5, 6);
        Map<Integer, Boolean> reverseMap = layout.buildRowReverseMap(rows);

        assertTrue(reverseMap.get(4));
        assertFalse(reverseMap.get(5));
        assertTrue(reverseMap.get(6));
    }

    @Test
    void assignmentSlotOrderFollowsTraversalRows() {
        HallLayoutUtil layout = new HallLayoutUtil(3);
        List<TableSlot> slots = Arrays.asList(
                new TableSlot(4, 14, 1, false),
                new TableSlot(1, 12, 1, false),
                new TableSlot(6, 14, 1, false),
                new TableSlot(5, 14, 1, false),
                new TableSlot(3, 12, 1, false),
                new TableSlot(2, 12, 1, false)
        );

        List<TableSlot> ordered = layout.orderSlotsForAssignment(slots);
        assertEquals(1, ordered.get(0).getRow());
        assertEquals(2, ordered.get(1).getRow());
        assertEquals(3, ordered.get(2).getRow());
        assertEquals(4, ordered.get(3).getRow());
        assertEquals(5, ordered.get(4).getRow());
        assertEquals(6, ordered.get(5).getRow());
    }
}
