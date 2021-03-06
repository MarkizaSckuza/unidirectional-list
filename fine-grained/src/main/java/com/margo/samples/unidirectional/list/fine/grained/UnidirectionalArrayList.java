package com.margo.samples.unidirectional.list.fine.grained;

import com.margo.samples.unidirectional.list.common.AbstractUnidirectionalArrayList;
import com.margo.samples.unidirectional.list.common.ListActions;
import com.margo.samples.unidirectional.list.common.lock.ListLock;
import com.margo.samples.unidirectional.list.common.lock.Striped;
import com.margo.samples.unidirectional.list.common.lock.StripedListLock;
import com.margo.samples.unidirectional.list.common.node.Node;
import com.margo.samples.unidirectional.list.common.validator.ListValidator;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class UnidirectionalArrayList<T extends Comparable<T>> extends AbstractUnidirectionalArrayList<T> {
    private static final int DEFAULT_STRIPES_SIZE = 32;

    private volatile Node<T> first;
    private int size;
    private int modCount;

    private ListLock<T> listLock;
    private Striped<ReentrantLock> striped = Striped.reentrantLock(DEFAULT_STRIPES_SIZE);

    public UnidirectionalArrayList() {
        size = 0;
        modCount = 0;

        listLock = new StripedListLock<T, ReentrantLock>(striped);
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
        checkStripesSize();

        Node<T> node = new Node<>(t);

        if (first == null) {
            synchronized (this) {
                if (first == null) {
                    first = node;

                    size++;
                    modCount++;
                    return true;
                }
            }
        } else {
            Node<T> result = ListActions.findPlaceAndPutConcurrent(first, null, first, node, listLock);
            if (result != null) {
                synchronized (this) {
                    size++;
                    modCount++;

                    first = result;
                }
                return true;
            }
        }
        return false;
    }

    private void checkStripesSize() {
        if (size > striped.size() * 3) {
            synchronized (striped) {
                int size = striped.size();

                striped.clear();

                striped = Striped.reentrantLock(size * size);
            }
        } else if (size < striped.size() * 3 && striped.size() > DEFAULT_STRIPES_SIZE) {
            synchronized (striped) {
                striped.clear();
                striped = Striped.reentrantLock(size / 3);
            }
        }
    }

    public boolean remove(Object o) {
        ListValidator.validateObjectNotNull(o);
        checkStripesSize();

        Node<T> result = ListActions.findPlaceAndRemoveConcurrent(first, null, first, (T) o, listLock);

        if (result != null) {
            synchronized (this) {
                first = result;

                size--;
                modCount++;
                return true;
            }
        }

        return false;
    }

    public synchronized void clear() {
        ListActions.clear(first);
        size = 0;
        modCount++;
    }

    public T get(int index) {
        ListValidator.validateIndex(index, size);
        return ListActions.getNodeWithIndex(index, first).getValue();
    }

    public T remove(int index) {
        ListValidator.validateIndex(index, size);
        checkStripesSize();

        Node<T> result = ListActions.removeByIndexConcurrent(index, first, listLock);

        if (result != null) {
            synchronized (this) {
                first = result;

                size--;
                modCount++;
            }
        }

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
}