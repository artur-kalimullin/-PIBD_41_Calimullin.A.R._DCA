package ru.ulstu.teacher_service;

import org.junit.jupiter.api.Test;
import ru.ulstu.mergesort.ParallelMergeSort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParallelMergeSortTest {

    @Test
    void testMergeSortFromLabExample() {
        List<Integer> input = List.of(3, 2, 6, 9, 2, 3, 5, 8, 2, 1, 11);
        
        ParallelMergeSort.SortResult<Integer> result = ParallelMergeSort.sort(input, 5);

        List<Integer> expected = List.of(1, 2, 2, 2, 3, 3, 5, 6, 8, 9, 11);
        
        assertEquals(expected, result.sorted());
        
        System.out.println("Input: " + input);
        System.out.println("Result: " + result.sorted());
        System.out.println("Time: " + result.durationMillis() + " ms (" + result.durationNanos() + " ns)");
    }
}