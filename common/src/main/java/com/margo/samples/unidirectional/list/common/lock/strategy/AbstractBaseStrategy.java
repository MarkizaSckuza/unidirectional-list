package com.margo.samples.unidirectional.list.common.lock.strategy;

import com.margo.samples.unidirectional.list.common.node.Node;

public abstract class AbstractBaseStrategy<T extends Comparable<T>> implements Strategy<T> {

    public Node<T> removeConcreteNode(Node<T> first, Node<T> node, Node<T> previous) {
        if (node == previous && node.getNext() != null) {
            first = node.getNext();
        } else {
            previous.setNext(node.getNext());
        }
        return first;
    }

    protected boolean compareValues(Node<T> previous, Node<T> current, Node<T> next, Node<T> expectedPrevious, Node<T> expectedCurrent, Node<T> expectedNext) {
        if (previous.equals(current)) {
            return previous.equals(expectedPrevious) && (previous.getNext() == null) || (previous.getNext().equals(expectedPrevious.getNext()) && current.equals(expectedCurrent) && (next == null && expectedNext == null) || next.equals(expectedNext));
        } else {
            return previous.equals(expectedPrevious) && previous.getNext().equals(expectedCurrent) && current.equals(expectedCurrent) && (next == null && expectedNext == null) || next.equals(expectedNext);
        }
    }
}
