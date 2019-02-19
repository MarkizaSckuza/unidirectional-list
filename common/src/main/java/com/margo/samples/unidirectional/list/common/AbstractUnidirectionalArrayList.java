package com.margo.samples.unidirectional.list.common;

import com.margo.samples.unidirectional.list.common.node.Node;
import com.margo.samples.unidirectional.list.common.validator.ListValidator;

public abstract class AbstractUnidirectionalArrayList<T extends Comparable<T>> extends AbstractBaseUnidirectionalArrayList<T> {

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

    protected T remove(Node<T> node, Node<T> previous) {
        if (node == previous && node.getNext() != null) {
            first = node.getNext();
        } else {
            previous.setNext(node.getNext());
        }

        size--;
        modCount++;

        return node.getValue();
    }
}
