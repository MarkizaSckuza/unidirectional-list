package com.margo.samples.unidirectional.list.common.lock.strategy;

import com.margo.samples.unidirectional.list.common.node.Node;

public class SimpleStrategy<T extends Comparable<T>> extends AbstractBaseStrategy<T> {

    @Override
    public Node<T> add(Node<T> first, Node<T> previousNode, Node<T> fromNode, Node<T> newNode) {
        Node<T> previous = first;

        for (Node<T> node = first; node != null; node = node.getNext()) {
            if (node.getValue().compareTo(newNode.getValue()) == 0) {
                newNode.setNext(node.getNext());
                node.setNext(newNode);

                return first;
            } else if (node.getValue().compareTo(newNode.getValue()) == 1) {

                newNode.setNext(node);

                if (previous == first) {
                    first = newNode;
                } else {
                    previous.setNext(newNode);
                }

                return first;
            } else if (node.getValue().compareTo(newNode.getValue()) == -1 && node.getNext() == null) {
                node.setNext(newNode);

                return first;
            }
            previous = node;
        }

        return null;
    }

    @Override
    public Node<T> remove(Node<T> first, Node<T> previousNode, Node<T> fromNode, T value) {
        Node<T> previous = first;
        for (Node<T> node = first; node != null; node = node.getNext()) {

            if (value.equals(node.getValue())) {
                return removeConcreteNode(first, node, previous);
            }
            previous = node;
        }

        return null;
    }

    @Override
    public Node<T> removeByIndex(int index, Node<T> first) {
        Node<T> previous = first;
        int i = 0;
        for (Node<T> node = first; node != null; node = node.getNext()) {
            if (i == index) {
                return removeConcreteNode(first, node, previous);
            } else {
                i++;
                previous = node;
            }
        }
        return null;
    }
}
