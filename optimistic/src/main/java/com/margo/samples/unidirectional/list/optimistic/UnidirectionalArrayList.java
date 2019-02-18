package com.margo.samples.unidirectional.list.optimistic;

import com.margo.samples.unidirectional.list.common.AbstractUnidirectionalArrayList;
import com.margo.samples.unidirectional.list.common.ListActions;
import com.margo.samples.unidirectional.list.common.node.Node;
import com.margo.samples.unidirectional.list.common.validator.ListValidator;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UnidirectionalArrayList<T extends Comparable<T>> extends AbstractUnidirectionalArrayList<T> {
    private volatile Node<T> first;
    private int size;
    private int modCount;

    private Queue<Operation> operations;

    public UnidirectionalArrayList() {
        size = 0;
        modCount = 0;
        operations = new ConcurrentLinkedQueue<>();

        Thread t = new Thread(new ActionResolver());
        t.setPriority(10);
        t.start();
    }

    public int size() {
        return size;
    }

    public Iterator<T> iterator() {
        return new Iter();
    }

    public Object[] toArray() {
        return ListActions.createArray(first, size);
    }

    public <T1> T1[] toArray(T1[] a) {
        return null;
    }

    public boolean add(T t) {
        ListValidator.validateObjectNotNull(t);
        return operations.add(new Operation(Action.ADD, t));
    }

    private boolean addToList(T t) {
        Node<T> newNode = new Node<>(t);

        if (first == null) {
            first = newNode;

            size++;
            modCount++;

            return true;
        } else {
            Node<T> result = ListActions.findPlaceAndPut(first, newNode);
            if (result != null) {
                size++;
                modCount++;

                first = result;
                return true;
            }
            return false;
        }
    }

    public boolean remove(Object o) {
        return operations.add(new Operation(Action.REMOVE, o));
    }

    private boolean findPlaceAndRemove(Object o) {
        Node<T> previous = first;
        for (Node<T> node = first; node != null; node = node.getNext()) {
            if (o.equals(node.getValue())) {
                remove(node, previous);
                return true;
            }
            previous = node;
        }
        return false;
    }

    private T remove(Node<T> node, Node<T> previous) {
        if (node == previous && node.getNext() != null) {
            first = node.getNext();
        } else {
            previous.setNext(node.getNext());
        }

        size--;
        modCount++;

        return node.getValue();
    }

    private void removeByIndex(Integer index) {
        ListValidator.validateIndex(index, size);

        Node<T> previous = first;
        int i = 0;
        for (Node<T> node = first; node != null; node = node.getNext()) {
            if (i == index) {
                remove(node, previous);
                return;
            } else {
                i++;
                previous = node;
            }
        }
    }

    public boolean addAll(Collection<? extends T> c) {
        ListValidator.validateObjectNotNull(c);

        return operations.add(new Operation(Action.ADD_ALL, c));
    }

    private void addAllToList(Collection<? extends T> c) {
        Object[] array = c.toArray();

        for (Object o : array) {
            add((T) o);
        }
    }

    public boolean removeAll(Collection<?> c) {
        return operations.add(new Operation(Action.REMOVE_ALL, c));
    }

    private void removeAllFromList(Collection<?> c) {
        Object[] array = c.toArray();

        for (Object o : array) {
            remove(o);
        }
    }


    public void clear() {
        operations.add(new Operation(Action.CLEAR, null));
    }

    private void clearList() {
        ListActions.clear(first);
        size = 0;
        modCount++;
    }

    public T get(int index) {
        ListValidator.validateIndex(index, size);
        return ListActions.getNodeWithIndex(index, first).getValue();
    }

    public T remove(int index) {
        operations.add(new Operation(Action.REMOVE_BY_INDEX, index));
        return null;
    }

    public int indexOf(Object o) {
        return ListActions.indexOf(o, first);
    }

    public int lastIndexOf(Object o) {
        return ListActions.lastIndexOf(o, first);
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