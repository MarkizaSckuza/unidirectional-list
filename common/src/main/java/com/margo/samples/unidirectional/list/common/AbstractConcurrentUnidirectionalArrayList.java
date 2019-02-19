package com.margo.samples.unidirectional.list.common;

import com.margo.samples.unidirectional.list.common.node.Node;
import com.margo.samples.unidirectional.list.common.validator.ListValidator;

public abstract class AbstractConcurrentUnidirectionalArrayList<T extends Comparable<T>> extends AbstractBaseUnidirectionalArrayList<T> {

    public boolean add(T t) {
        ListValidator.validateObjectNotNull(t);

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
            Node<T> result = addToList(t);
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
        ListValidator.validateObjectNotNull(o);
        Node<T> result = removeFromList((T) o);

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

    public T remove(int index) {
        ListValidator.validateIndex(index, size);

        Node<T> result = removeFromList(index);

        if (result != null) {
            synchronized (this) {
                first = result;

                size--;
                modCount++;
            }
        }

        return null;
    }

    protected abstract Node<T> addToList(T value);

    protected abstract Node<T> removeFromList(T object);

    protected abstract Node<T> removeFromList(int index);

}
