package de.tobiashh.javafx.composer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReuseableCheckerTest {

    @Test
    void isReuseableAtPosition() {
        ReuseableChecker reuseableChecker = new ReuseableChecker(2, 2, 0);
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 3));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 3));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 3));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 3));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 3));
        reuseableChecker = new ReuseableChecker(2, 2, 1);
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 3));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 3));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 3));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 3));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 3));
        reuseableChecker = new ReuseableChecker(2, 2, 2);
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 0));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 0));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 1));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 1));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 2));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 2));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 3));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 3));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 3));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 3));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 3));
        reuseableChecker = new ReuseableChecker(2, 2, 3);
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 0));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 0));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 0));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 0));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 1));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 1));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 1));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 1));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 2));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 2));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 2));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 2));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, -1}, 3));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{3, -1, -1, -1}, 3));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, 3, -1, -1}, 3));
        assertFalse(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, 3, -1}, 3));
        assertTrue(reuseableChecker.isReuseableAtPosition(3, new int[]{-1, -1, -1, 3}, 3));
    }
}