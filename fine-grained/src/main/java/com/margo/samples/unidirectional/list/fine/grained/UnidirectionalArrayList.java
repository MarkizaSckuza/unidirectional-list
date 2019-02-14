package com.margo.samples.unidirectional.list.fine.grained;

import com.margo.samples.unidirectional.list.common.ListActions;
import com.margo.samples.unidirectional.list.common.Node;
import com.margo.samples.unidirectional.list.common.Striped;
import com.margo.samples.unidirectional.list.common.UnidirectionalList;
import javafx.util.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class UnidirectionalArrayList<T extends Comparable<T>> implements UnidirectionalList<T> {
    private static final int DEFAULT_STRIPES_SIZE = 32;

    private volatile Node<T> first;
    private int size;
    private int modCount;
    private Striped<ReentrantLock> striped = Striped.reentrantLock(DEFAULT_STRIPES_SIZE);

    public UnidirectionalArrayList() {
        size = 0;
        modCount = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(Object o) {
        checkNotNull(o);

        return indexOf(o) != -1;
    }

    public Iterator<T> iterator() {
        return new Iter();
    }

    public Object[] toArray() {
        return createArray();
    }

    private Object[] createArray() {
        return ListActions.createArray(first, size);
    }

    public <T1> T1[] toArray(T1[] a) {
        return null;
    }

    public boolean add(T t) {
        checkNotNull(t);
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
            Node<T> result = ListActions.findPlaceAndPutStriped(first, null, first, node, striped);
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

//    private boolean findPlaceAndAdd(Node<T> previousNode, Node<T> fromNode, Node<T> newNode) {
//        Node<T> previous = previousNode == null ? first : previousNode;
//
//        for (Node<T> node = fromNode; node != null; node = node.getNext()) {
//
//            if (node.getValue().compareTo(newNode.getValue()) == 0) {
//                Node<T> prev = previous.clone();
//                Node<T> curr = node.clone();
//
//                Node<T> next = node.getNext() == null ? null : node.getNext().clone();
//
//                ReentrantLock prevLock = striped.get(previous);
//                ReentrantLock currLock = striped.get(node);
//                ReentrantLock nextLock = striped.get(node.getNext());
//
//                try {
//                    prevLock.lock();
//                    currLock.lock();
//                    nextLock.lock();
//
//                    if (compareValues(previous, node, next, prev, curr, next) && node.getValue().compareTo(newNode.getValue()) == 0) {
//                        newNode.setNext(node.getNext());
//                        node.setNext(newNode);
//
//                        size++;
//                        modCount++;
//                        return true;
//                    }
//                } finally {
//                    prevLock.unlock();
//                    currLock.unlock();
//                    nextLock.unlock();
//                }
//
//                if (previous != null && node != null) {
//                    findPlaceAndAdd(previous, node, newNode);
//                } else {
//                    findPlaceAndAdd(null, first, newNode);
//                }
//
//            } else if (node.getValue().compareTo(newNode.getValue()) == 1) {
//
//                Node<T> prev = previous.clone();
//                Node<T> curr = node.clone();
//
//                Node<T> next = node.getNext() == null ? null : node.getNext().clone();
//
//                ReentrantLock prevLock = striped.get(previous);
//                ReentrantLock currLock = striped.get(node);
//
//                try {
//
//                    prevLock.lock();
//                    currLock.lock();
//
//                    if (compareValues(previous, node, next, prev, curr, next) && node.getValue().compareTo(newNode.getValue()) == 1) {
//                        newNode.setNext(node);
//
//                        if (previous == first) {
//                            first = newNode;
//                        } else {
//                            previous.setNext(newNode);
//                        }
//
//                        size++;
//                        modCount++;
//                        return true;
//                    }
//                } finally {
//                    prevLock.unlock();
//                    currLock.unlock();
//                }
//
//                if (previous != null && node != null) {
//                    findPlaceAndAdd(previous, node, newNode);
//                } else {
//                    findPlaceAndAdd(null, first, newNode);
//                }
//
//            } else if (node.getValue().compareTo(newNode.getValue()) == -1 && node.getNext() == null) {
//
//                Node<T> prev = previous.clone();
//                Node<T> curr = node.clone();
//
//                ReentrantLock prevLock = striped.get(previous);
//                ReentrantLock currLock = striped.get(node);
//
//                try {
//                    prevLock.lock();
//                    currLock.lock();
//
//                    if (compareValues(previous, node, null, prev, curr, null) && node.getValue().compareTo(newNode.getValue()) == -1 && node.getNext() == null) {
//                        node.setNext(newNode);
//
//                        size++;
//                        modCount++;
//                        return true;
//                    }
//                } finally {
//                    prevLock.unlock();
//                    currLock.unlock();
//                }
//
//                if (previous != null && node != null) {
//                    findPlaceAndAdd(previous, node, newNode);
//                } else {
//                    findPlaceAndAdd(null, first, newNode);
//                }
//            }
//            previous = node;
//        }
//
//        return false;
//    }
//
//    private boolean compareValues(Node<T> previous, Node<T> current, Node<T> next, Node<T> expectedPrevious, Node<T> expectedCurrent, Node<T> expectedNext) {
//        if (previous.equals(current)) {
//            return previous.equals(expectedPrevious) && (previous.getNext() == null) || (previous.getNext().equals(expectedPrevious.getNext()) && current.equals(expectedCurrent) && (next == null && expectedNext == null) || next.equals(expectedNext));
//        } else {
//            return previous.equals(expectedPrevious) && previous.getNext().equals(expectedCurrent) && current.equals(expectedCurrent) && (next == null && expectedNext == null) || next.equals(expectedNext);
//        }
//    }

    public boolean remove(Object o) {
        checkNotNull(o);
        checkStripesSize();

        Node<T> result = ListActions.findPlaceAndRemoveStriped(first, null, first, (T) o, striped);

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

//    private boolean findPlaceAndRemoveStriped(Node<T> previousNode, Node<T> fromNode, T value) {
//        Node<T> previous = previousNode == null ? first : previousNode;
//
//        for (Node<T> node = fromNode; node != null; node = node.getNext()) {
//            if (value.equals(node.getValue())) {
//                Node<T> prev = previous.clone();
//                Node<T> curr = node.clone();
//
//                Node<T> next = node.getNext() == null ? null : node.getNext().clone();
//
//                ReentrantLock prevLock = striped.get(previous);
//                ReentrantLock currLock = striped.get(node);
//
//                try {
//                    prevLock.lock();
//                    currLock.lock();
//
//                    if (node.getNext() != null) {
//                        ReentrantLock nextLock = striped.get(node.getNext());
//
//                        try {
//                            nextLock.lock();
//
//                            if (compareValues(previous, node, next, prev, curr, next)) {
//                                removeConcreteNode(node, previous);
//                                return true;
//                            }
//                        } finally {
//                            nextLock.unlock();
//                        }
//                    } else {
//                        if (compareValues(previous, node, next, prev, curr, next)) {
//                            removeConcreteNode(node, previous);
//                            return true;
//                        }
//                    }
//                } finally {
//                    prevLock.unlock();
//                    currLock.unlock();
//                }
//
//                if (previous != null && node != null) {
//                    findPlaceAndRemoveStriped(previous, node, value);
//                } else {
//                    findPlaceAndRemoveStriped(null, first, value);
//                }
//            }
//            previous = node;
//        }
//        return false;
//    }

//    private T removeConcreteNode(Node<T> node, Node<T> previous) {
//        if (node == previous && node.getNext() != null) {
//            first = node.getNext();
//        } else {
//            previous.setNext(node.getNext());
//        }
//        T value = node.getValue();
//        node = null;
//
//        size--;
//        modCount++;
//
//        return value;
//    }

    public boolean containsAll(Collection<?> c) {
        checkNotNull(c);

        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }

    public boolean addAll(Collection<? extends T> c) {
        checkNotNull(c);

        Object[] array = c.toArray();
        boolean result = false;

        for (Object o : array) {
            result = add((T) o);
        }

        return result;
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        throw new NotImplementedException();
    }

    public boolean removeAll(Collection<?> c) {
        checkNotNull(c);

        Object[] array = c.toArray();
        boolean result = false;

        for (Object o : array) {
            result = remove(o);
        }

        return result;
    }

    public boolean retainAll(Collection<?> c) {
        return false;
    }

    public synchronized void clear() {
        ListActions.clear(first);
        size = 0;
        modCount++;
    }

    public T get(int index) {
        checkIndex(index);
        return ListActions.getNodeWithIndex(index, first).getValue();
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds of an array");
        }
    }

    public T set(int index, T element) {
        throw new NotImplementedException();
    }

    public void add(int index, T element) {
        throw new NotImplementedException();
    }

    public T remove(int index) {
        checkIndex(index);
        checkStripesSize();

        Node<T> result = ListActions.removeByIndexStriped(index, first, striped);

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
        checkIndex(index);
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