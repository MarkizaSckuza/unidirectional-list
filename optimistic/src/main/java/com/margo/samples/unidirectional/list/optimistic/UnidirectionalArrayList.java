package com.margo.samples.unidirectional.list.optimistic;

import com.margo.samples.unidirectional.list.common.AbstractUnidirectionalArrayList;
import com.margo.samples.unidirectional.list.common.ListActions;
import com.margo.samples.unidirectional.list.common.node.Node;
import com.margo.samples.unidirectional.list.common.validator.ListValidator;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UnidirectionalArrayList<T extends Comparable<T>> extends AbstractUnidirectionalArrayList<T> {
    private Queue<Operation> operations;

    public UnidirectionalArrayList() {
        size = 0;
        modCount = 0;
        operations = new ConcurrentLinkedQueue<>();

        Thread t = new Thread(new ActionResolver());
        t.setPriority(10);
        t.start();
    }

    public Iterator<T> iterator() {
        return new Iter();
    }

    public boolean add(T t) {
        ListValidator.validateObjectNotNull(t);
        return operations.add(new Operation(Action.ADD, t));
    }

    private boolean addToList(T t) {
        return super.add(t);
    }

    public boolean remove(Object o) {
        return operations.add(new Operation(Action.REMOVE, o));
    }

    private boolean findPlaceAndRemove(Object o) {
        return super.remove(o);
    }

    private void removeByIndex(Integer index) {
        super.remove(index);
    }

    public boolean addAll(Collection<? extends T> c) {
        ListValidator.validateObjectNotNull(c);

        return operations.add(new Operation(Action.ADD_ALL, c));
    }

    private void addAllToList(Collection<? extends T> c) {
        super.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return operations.add(new Operation(Action.REMOVE_ALL, c));
    }

    private void removeAllFromList(Collection<?> c) {
        super.removeAll(c);
    }

    public void clear() {
        operations.add(new Operation(Action.CLEAR, null));
    }

    private void clearList() {
        super.clear();
    }

    public T remove(int index) {
        operations.add(new Operation(Action.REMOVE_BY_INDEX, index));
        return null;
    }

    public ListIterator<T> listIterator() {
        return new ListIter(0);
    }

    public ListIterator<T> listIterator(int index) {
        ListValidator.validateIndex(index, size);
        return new ListIter(index);
    }

    public List<T> subList(int fromIndex, int toIndex) {
        return null;
    }

    private class ActionResolver implements Runnable {

        @Override
        public void run() {
            while (true) {
                Operation operation = operations.poll();

                if (operation != null) {

                    switch (operation.getAction()) {
                        case ADD:
                            System.out.println("ADDING " + operation.getObject());
                            addToList((T) operation.getObject());
                            System.out.println("ADDED " + operation.getObject());
                            break;
                        case ADD_ALL:
                            System.out.println("ADDING ALL " + operation.getObject());
                            addAllToList((Collection<? extends T>) operation.getObject());
                            System.out.println("ADDED ALL " + operation.getObject());
                            break;
                        case REMOVE:
                            System.out.println("REMOVING " + operation.getObject());
                            findPlaceAndRemove(operation.getObject());
                            System.out.println("REMOVED " + operation.getObject());
                            break;
                        case REMOVE_BY_INDEX:
                            System.out.println("REMOVING BY INDEX " + operation.getObject());
                            removeByIndex((Integer) operation.getObject());
                            System.out.println("REMOVED BY INDEX " + operation.getObject() + " SIZE = " + size);
                            break;
                        case REMOVE_ALL:
                            removeAllFromList((Collection<?>) operation.getObject());
                        case CLEAR:
                            clearList();
                            break;
                    }
                }
            }
        }
    }

    private class Iter implements Iterator<T> {
        private Node<T> lastReturned;
        private Node<T> next;
        private int nextIndex;
        private Node<T> previous;
        private int expectedModCount = modCount;

        public boolean hasNext() {
            return nextIndex < size;
        }

        public T next() {
            checkForComodification();
            if (!hasNext())
                throw new NoSuchElementException();
            previous = lastReturned;
            lastReturned = next;
            next = next.getNext();
            nextIndex++;
            return lastReturned.getValue();
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();

            Node<T> lastNext = lastReturned.getNext();
            UnidirectionalArrayList.this.remove(lastReturned, previous);
            if (next == lastReturned)
                next = lastNext;
            else
                nextIndex--;
            lastReturned = null;
            expectedModCount++;
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    private class ListIter implements ListIterator<T> {
        private Node<T> lastReturned;
        private Node<T> next;
        private int nextIndex;
        private Node<T> previous;
        private int expectedModCount = modCount;

        ListIter(int index) {
            // assert isPositionIndex(index);
            next = (index == size) ? null : ListActions.getNodeWithIndex(index, first);
            nextIndex = index;
        }

        public boolean hasNext() {
            return nextIndex < size;
        }

        public T next() {
            checkForComodification();
            if (!hasNext())
                throw new NoSuchElementException();
            previous = lastReturned;
            lastReturned = next;
            next = next.getNext();
            nextIndex++;
            return lastReturned.getValue();
        }

        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        public T previous() {
            return null;
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return nextIndex - 1;
        }

        public void remove() {
            checkForComodification();
            if (lastReturned == null)
                throw new IllegalStateException();

            Node<T> lastNext = lastReturned.getNext();
            UnidirectionalArrayList.this.remove(lastReturned, previous);
            if (next == lastReturned)
                next = lastNext;
            else
                nextIndex--;
            lastReturned = null;
            expectedModCount++;
        }

        public void set(T t) {
            checkForComodification();
            if (lastReturned == null)
                throw new IllegalStateException();
            checkForComodification();
            lastReturned.setValue(t);
        }

        public void add(T t) {
            checkForComodification();
            lastReturned = null;
            add(t);
            nextIndex++;
            expectedModCount++;
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }
}