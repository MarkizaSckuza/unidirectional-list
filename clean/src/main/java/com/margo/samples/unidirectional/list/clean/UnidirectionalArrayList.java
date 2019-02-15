package com.margo.samples.unidirectional.list.clean;

import com.margo.samples.unidirectional.list.common.ListActions;
import com.margo.samples.unidirectional.list.common.node.Node;
import com.margo.samples.unidirectional.list.common.UnidirectionalList;
import com.margo.samples.unidirectional.list.common.validator.ListValidator;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class UnidirectionalArrayList<T extends Comparable<T>> implements UnidirectionalList<T> {
    private Node<T> first;
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
        ListValidator.validateObjectNotNull(o);

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
        ListValidator.validateObjectNotNull(t);

        Node<T> node = new Node<>(t);

        if (first == null) {
            first = node;

            size++;
            modCount++;

            return true;
        } else {
            Node<T> result = ListActions.findPlaceAndPut(first, node);
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
        ListValidator.validateObjectNotNull(o);

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

    public boolean containsAll(Collection<?> c) {
        ListValidator.validateObjectNotNull(c);

        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }

    public boolean addAll(Collection<? extends T> c) {
        ListValidator.validateObjectNotNull(c);

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
        ListValidator.validateObjectNotNull(c);

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

    public void clear() {
        ListActions.clear(first);
        size = 0;
        modCount++;

    }

    public T get(int index) {
        ListValidator.validateIndex(index, size);
        return ListActions.getNodeWithIndex(index, first).getValue();
    }
    
    public T set(int index, T element) {
        throw new NotImplementedException();
    }

    public void add(int index, T element) {
        throw new NotImplementedException();
    }

    public T remove(int index) {
        ListValidator.validateIndex(index, size);

        Node<T> previous = first;
        int i = 0;
        for (Node<T> node = first; node != null; node = node.getNext()) {
            if (i == index) {
                return remove(node, previous);
            } else {
                i++;
                previous = node;
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

