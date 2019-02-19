package com.margo.samples.unidirectional.list.optimistic;

import com.margo.samples.unidirectional.list.common.UnidirectionalList;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ConcurrentTests {
    private UnidirectionalList<Integer> list;

    @Before
    public void init() {
        list = new UnidirectionalArrayList<>();
        list.add(0);
        list.add(1);
        list.add(2);
        list.add(3);
    }

    @Test
    public void shouldAdd() throws InterruptedException {
        list.add(-1);
        list.add(4);

        Thread.sleep(10);
        assertEquals(6, list.size());
        assertTrue(-1 == list.get(0));
        assertTrue(4 == list.get(5));
        Object[] array = list.toArray();
        System.out.println(Arrays.toString(array));
        assertArrayEquals(new Object[] {-1, 0, 1, 2, 3, 4}, array);
    }

    @Test
    public void shouldRemove() throws InterruptedException {
        list.add(4);
        list.remove(1);
        list.remove(new Integer(4));

        Thread.sleep(10);
//        assertEquals(3, list.size());
        Object[] array = list.toArray();
        System.out.println(Arrays.toString(array));
        assertArrayEquals(new Object[] {0, 2, 3}, array);
    }

    @Test
    public void shouldAddAndRemoveTogether() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        executor.submit(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list.add(10);
            list.add(22);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list.add(21);
            list.add(-1);
        });
        executor.submit(() ->
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list.remove(new Integer(2));
            list.remove(new Integer(3));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list.remove(new Integer(0));
        });

        executor.submit(() ->
        {
            list.add(11);
            list.add(7);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list.remove(new Integer(1));
        });

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        Thread.sleep(500);

        Object[] array = list.toArray();
        System.out.println(Arrays.toString(array));

        assertEquals(6, list.size());
        assertArrayEquals(new Object[]{-1, 7, 10, 11, 21, 22}, array);
    }

    @Test
    public void shouldAddTogether() throws InterruptedException {
        list.add(10);

        ExecutorService executor = Executors.newFixedThreadPool(4);

        List<Callable<Void>> tasks = new ArrayList<>();
        Callable<Void> r1 = () -> {
            System.out.println("In first");

            System.out.println("before add");
            list.add(9);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list.add(5);
            System.out.println("after add");

            return null;
        };

        Callable<Void> r2 = () -> {
            System.out.println("In second");

            System.out.println("before 2nd add");
            list.add(-1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list.add(23);
            System.out.println("after 2nd add");

            return null;
        };

        Callable<Void> r3 = () -> {
            System.out.println("In third");

            System.out.println("before 3rd add");
            list.add(0);
            list.add(21);

            list.add(7);
            System.out.println("after 3rd add");

            return null;
        };

        tasks.add(r1);
        tasks.add(r2);
        tasks.add(r3);
        tasks.add(r1);

        executor.invokeAll(tasks);

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

//        Thread.sleep(10);

        Object[] array = list.toArray();
        System.out.println(Arrays.toString(array));

        assertArrayEquals(new Object[]{-1, 0, 0, 1, 2, 3, 5, 5, 7, 9, 9, 10, 21, 23}, array);
    }
}
