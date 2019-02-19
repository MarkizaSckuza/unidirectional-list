package com.margo.samples.unidirectional.list.fine.grained;

import com.margo.samples.unidirectional.list.common.AbstractConcurrentUnidirectionalArrayList;
import com.margo.samples.unidirectional.list.common.ListActions;
import com.margo.samples.unidirectional.list.common.lock.Striped;
import com.margo.samples.unidirectional.list.common.lock.StripedListLock;
import com.margo.samples.unidirectional.list.common.lock.strategy.ConcurrentStrategy;
import com.margo.samples.unidirectional.list.common.lock.strategy.Strategy;
import com.margo.samples.unidirectional.list.common.node.Node;
import com.margo.samples.unidirectional.list.common.validator.ListValidator;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class UnidirectionalArrayList<T extends Comparable<T>> extends AbstractConcurrentUnidirectionalArrayList<T> {
    private static final int DEFAULT_STRIPES_SIZE = 32;

    private Striped<ReentrantLock> striped;

    public UnidirectionalArrayList() {
        size = 0;
        modCount = 0;

        striped = Striped.reentrantLock(DEFAULT_STRIPES_SIZE);
        strategy = new ConcurrentStrategy<>(new StripedListLock<T, ReentrantLock>(striped));
    }

    @Override
    public boolean add(T t) {
        checkStripesSize();
        return super.add(t);
    }

    @Override
    public boolean remove(Object o) {
        checkStripesSize();
        return super.remove(o);
    }

    @Override
    public T remove(int index) {
        checkStripesSize();
        return super.remove(index);
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

    public void setStrategy(Strategy<T> strategy) {
        this.strategy = strategy;
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
}