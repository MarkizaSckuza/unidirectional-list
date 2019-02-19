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
            Node<T> result = strategy.add(first, null, first, node);
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

        Node<T> result = strategy.remove(first, null, first, (T) o);

        if (result != null) {
            first = result;

            size--;
            modCount++;
            return true;
        }

        return false;
    }

    public T remove(int index) {
        ListValidator.validateIndex(index, size);

        Node<T> result = strategy.removeByIndex(index, first);

        if (result != null) {
            first = result;

            size--;
            modCount++;
        }

        return null;
    }

//    protected T remove(Node<T> node, Node<T> previous) {
//        if (node == previous && node.getNext() != null) {
//            first = node.getNext();
//        } else {
//            previous.setNext(node.getNext());
//        }
//
//        size--;
//        modCount++;
//
//        return node.getValue();
//    }
}
