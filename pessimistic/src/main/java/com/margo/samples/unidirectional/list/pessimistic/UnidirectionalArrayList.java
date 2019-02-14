package com.margo.samples.unidirectional.list.pessimistic;

import com.margo.samples.unidirectional.list.common.ListActions;
import com.margo.samples.unidirectional.list.common.Node;
import com.margo.samples.unidirectional.list.common.UnidirectionalList;
import javafx.util.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class UnidirectionalArrayList<T extends Comparable<T>> implements UnidirectionalList<T> {
    private volatile Node<T> first;
    private int size;
    private int modCount;

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
        ListActions.checkNotNull(o);

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
        ListActions.checkNotNull(t);

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
            Node<T> result = ListActions.findPlaceAndPutSync(first, null, first, node);
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

    public boolean remove(Object o) {
        ListActions.checkNotNull(o);
        Node<T> result = ListActions.findPlaceAndRemoveSync(first, null, first, (T) o);

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

    public boolean containsAll(Collection<?> c) {
        ListActions.checkNotNull(c);

        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }

    public boolean addAll(Collection<? extends T> c) {
        ListActions.checkNotNull(c);

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
        ListActions.checkNotNull(c);

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

        Node<T> result = ListActions.removeByIndexSync(index, first);

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
