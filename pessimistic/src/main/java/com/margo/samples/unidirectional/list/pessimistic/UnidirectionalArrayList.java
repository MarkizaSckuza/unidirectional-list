package com.margo.samples.unidirectional.list.pessimistic;

import com.margo.samples.unidirectional.list.common.AbstractConcurrentUnidirectionalArrayList;
import com.margo.samples.unidirectional.list.common.ListActions;
import com.margo.samples.unidirectional.list.common.lock.ListLock;
import com.margo.samples.unidirectional.list.common.lock.SynchronizedListLock;
import com.margo.samples.unidirectional.list.common.node.Node;
import com.margo.samples.unidirectional.list.common.validator.ListValidator;

import java.util.*;

public class UnidirectionalArrayList<T extends Comparable<T>> extends AbstractConcurrentUnidirectionalArrayList<T> {
    private ListLock<T> listLock;

    public UnidirectionalArrayList() {
        size = 0;
        modCount = 0;

        listLock = new SynchronizedListLock<>();
    }

    @Override
    protected Node<T> addToList(T value) {
        return ListActions.findPlaceAndPutConcurrent(first, null, first, new Node<>(value), listLock);
    }

    @Override
    protected Node<T> removeFromList(T object) {
        return ListActions.findPlaceAndRemoveConcurrent(first, null, first, object, listLock);
    }

    @Override
    protected Node<T> removeFromList(int index) {
        return ListActions.removeByIndexConcurrent(index, first, listLock);
    }

    public Iterator<T> iterator() {
        return new Iter();
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

            Node<T> result = ListActions.removeConcreteNode(first, lastReturned, previous);

            if (result != null) {
                first = result;
            }

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
            Node<T> result = ListActions.removeConcreteNode(first, lastReturned, previous);

            if (result != null) {
                first = result;
            }
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

    private static void checkNotNull(Object v) {
        if (v == null)
            throw new NullPointerException();
    }
}

