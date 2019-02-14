package com.margo.samples.unidirectional.list.common;

import java.util.concurrent.locks.Lock;

public class ListActions {

    public static <T> Object[] createArray(Node<T> first, int size) {
        Object[] array;

        array = new Object[size];
        int i = 0;

        for (Node<T> node = first; node != null && i < array.length; node = node.getNext()) {
            array[i++] = node.getValue();
        }

        return array;
    }

    public static <T> void clear(Node<T> first) {
        for (Node<T> node = first; node != null; ) {
            Node<T> next = node.getNext();
            node.setNext(null);
            node = next;
        }
    }

    public static <T extends Comparable<T>> Node<T> findPlaceAndPut(Node<T> first, Node<T> newNode) {
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

    public static <T extends Comparable<T>> Node<T> findPlaceAndPutSync(Node<T> first, Node<T> previousNode, Node<T> fromNode, Node<T> newNode) {
        Node<T> previous = previousNode == null ? first : previousNode;

        for (Node<T> node = fromNode; node != null; node = node.getNext()) {
            if (node.getValue().compareTo(newNode.getValue()) == 0) {
                Node<T> prev = previous.clone();
                Node<T> curr = node.clone();
                Node<T> next = node.getNext().clone();

                synchronized (previous) {
                    synchronized (node) {
                        synchronized (node.getNext()) {
                            if (compareValues(previous, node, next, prev, curr, next) && node.getValue().compareTo(newNode.getValue()) == 0) {
                                newNode.setNext(node.getNext());
                                node.setNext(newNode);

                                return first;
                            }
                        }
                    }
                }

                if (previous != null && node != null) {
                    findPlaceAndPutSync(first, previous, node, newNode);
                } else {
                    findPlaceAndPutSync(first, null, first, newNode);
                }
            } else if (node.getValue().compareTo(newNode.getValue()) == 1) {
                Node<T> prev = previous.clone();
                Node<T> curr = node.clone();

                Node<T> next = node.getNext() == null ? null : node.getNext().clone();

                synchronized (previous) {
                    synchronized (node) {
                        if (compareValues(previous, node, next, prev, curr, next) && node.getValue().compareTo(newNode.getValue()) == 1) {
                            newNode.setNext(node);

                            if (previous == first) {
                                first = newNode;
                            } else {
                                previous.setNext(newNode);
                            }

                            return first;
                        }
                    }
                }

                if (previous != null && node != null) {
                    findPlaceAndPutSync(first, previous, node, newNode);
                } else {
                    findPlaceAndPutSync(first, null, first, newNode);
                }
            } else if (node.getValue().compareTo(newNode.getValue()) == -1 && node.getNext() == null) {
                Node<T> prev = previous.clone();
                Node<T> curr = node.clone();

                synchronized (previous) {
                    synchronized (node) {
                        if (compareValues(previous, node, null, prev, curr, null) && node.getValue().compareTo(newNode.getValue()) == -1 && node.getNext() == null) {
                            node.setNext(newNode);

                            return first;
                        }
                    }
                }

                if (previous != null && node != null) {
                    findPlaceAndPutSync(first, previous, node, newNode);
                } else {
                    findPlaceAndPutSync(first, null, first, newNode);
                }
            }

            previous = node;
        }
        return null;
    }

    public static <T extends Comparable<T>, L extends Lock> Node<T> findPlaceAndPutStriped(Node<T> first, Node<T> previousNode, Node<T> fromNode, Node<T> newNode, Striped<L> striped) {
        Node<T> previous = previousNode == null ? first : previousNode;

        for (Node<T> node = fromNode; node != null; node = node.getNext()) {

            if (node.getValue().compareTo(newNode.getValue()) == 0) {
                Node<T> prev = previous.clone();
                Node<T> curr = node.clone();

                Node<T> next = node.getNext() == null ? null : node.getNext().clone();

                Lock prevLock = striped.get(previous);
                Lock currLock = striped.get(node);
                Lock nextLock = striped.get(node.getNext());

                try {
                    prevLock.lock();
                    currLock.lock();
                    nextLock.lock();

                    if (compareValues(previous, node, next, prev, curr, next) && node.getValue().compareTo(newNode.getValue()) == 0) {
                        newNode.setNext(node.getNext());
                        node.setNext(newNode);

                        return first;
                    }
                } finally {
                    prevLock.unlock();
                    currLock.unlock();
                    nextLock.unlock();
                }

                if (previous != null && node != null) {
                    findPlaceAndPutStriped(first, previous, node, newNode, striped);
                } else {
                    findPlaceAndPutStriped(first, null, first, newNode, striped);
                }

            } else if (node.getValue().compareTo(newNode.getValue()) == 1) {

                Node<T> prev = previous.clone();
                Node<T> curr = node.clone();

                Node<T> next = node.getNext() == null ? null : node.getNext().clone();

                Lock prevLock = striped.get(previous);
                Lock currLock = striped.get(node);

                try {
                    prevLock.lock();
                    currLock.lock();

                    if (compareValues(previous, node, next, prev, curr, next) && node.getValue().compareTo(newNode.getValue()) == 1) {
                        newNode.setNext(node);

                        if (previous == first) {
                            first = newNode;
                        } else {
                            previous.setNext(newNode);
                        }

                        return first;
                    }
                } finally {
                    prevLock.unlock();
                    currLock.unlock();
                }

                if (previous != null && node != null) {
                    findPlaceAndPutStriped(first, previous, node, newNode, striped);
                } else {
                    findPlaceAndPutStriped(first, null, first, newNode, striped);
                }

            } else if (node.getValue().compareTo(newNode.getValue()) == -1 && node.getNext() == null) {

                Node<T> prev = previous.clone();
                Node<T> curr = node.clone();

                Lock prevLock = striped.get(previous);
                Lock currLock = striped.get(node);

                try {
                    prevLock.lock();
                    currLock.lock();

                    if (compareValues(previous, node, null, prev, curr, null) && node.getValue().compareTo(newNode.getValue()) == -1 && node.getNext() == null) {
                        node.setNext(newNode);

                        return first;
                    }
                } finally {
                    prevLock.unlock();
                    currLock.unlock();
                }

                if (previous != null && node != null) {
                    findPlaceAndPutStriped(first, previous, node, newNode, striped);
                } else {
                    findPlaceAndPutStriped(first, null, first, newNode, striped);
                }
            }
            previous = node;
        }

        return null;
    }

    public static <T> Node<T> findPlaceAndRemoveSync(Node<T> first, Node<T> previousNode, Node<T> fromNode, T value) {
        Node<T> previous = previousNode == null ? first : previousNode;

        for (Node<T> node = fromNode; node != null; node = node.getNext()) {
            if (value.equals(node.getValue())) {
                Node<T> prev = previous.clone();
                Node<T> curr = node.clone();

                Node<T> next = node.getNext() == null ? null : node.getNext().clone();

                synchronized (previous) {
                    synchronized (node) {

                        if (node.getNext() != null) {
                            synchronized (node.getNext()) {
                                if (compareValues(previous, node, next, prev, curr, next)) {
                                    return removeConcreteNode(first, node, previous);
                                }
                            }
                        } else {
                            if (compareValues(previous, node, next, prev, curr, next)) {
                                return removeConcreteNode(first, node, previous);
                            }
                        }
                    }
                }

                if (previous != null && node != null) {
                    findPlaceAndRemoveSync(first, previous, node, value);
                } else {
                    findPlaceAndRemoveSync(first, null, first, value);
                }
            }
            previous = node;
        }
        return null;
    }

    public static <T extends Comparable<T>, L extends Lock> Node<T> findPlaceAndRemoveStriped(Node<T> first, Node<T> previousNode, Node<T> fromNode, T value, Striped<L> striped) {
        Node<T> previous = previousNode == null ? first : previousNode;

        for (Node<T> node = fromNode; node != null; node = node.getNext()) {
            if (value.equals(node.getValue())) {
                Node<T> prev = previous.clone();
                Node<T> curr = node.clone();

                Node<T> next = node.getNext() == null ? null : node.getNext().clone();

                Lock prevLock = striped.get(previous);
                Lock currLock = striped.get(node);

                try {
                    prevLock.lock();
                    currLock.lock();

                    if (node.getNext() != null) {
                        Lock nextLock = striped.get(node.getNext());

                        try {
                            nextLock.lock();

                            if (compareValues(previous, node, next, prev, curr, next)) {
                                removeConcreteNode(first, node, previous);
                                return first;
                            }
                        } finally {
                            nextLock.unlock();
                        }
                    } else {
                        if (compareValues(previous, node, next, prev, curr, next)) {
                            removeConcreteNode(first, node, previous);
                            return first;
                        }
                    }
                } finally {
                    prevLock.unlock();
                    currLock.unlock();
                }

                if (previous != null && node != null) {
                    findPlaceAndRemoveStriped(first, previous, node, value, striped);
                } else {
                    findPlaceAndRemoveStriped(first, null, first, value, striped);
                }
            }
            previous = node;
        }
        return null;
    }

    public static <T extends Comparable<T>> Node<T> removeByIndexSync(int index, Node<T> first) {
        Node<T> nodeToRemove = first;
        Node<T> previous = first;

        for (int i = 0; i < index; i++) {
            previous = nodeToRemove;
            nodeToRemove = nodeToRemove.getNext();
        }

        Node<T> expectedPrevious = previous.clone();
        Node<T> expectedNode = nodeToRemove.clone();
        Node<T> expectedNext = nodeToRemove.getNext().clone();

        synchronized (previous) {
            synchronized (nodeToRemove) {
                synchronized (nodeToRemove.getNext()) {
                    if (ListActions.compareValues(previous, nodeToRemove, nodeToRemove.getNext(), expectedPrevious, expectedNode, expectedNext)) {
                        return removeConcreteNode(first, nodeToRemove, previous);
                    }
                }
            }
        }

        return removeByIndexSync(index, first);
    }

    public static <T extends Comparable<T>, L extends Lock> Node<T> removeByIndexStriped(int index, Node<T> first, Striped<L> striped) {
        Node<T> nodeToRemove = first;
        Node<T> previous = first;

        for (int i = 0; i < index; i++) {
            previous = nodeToRemove;
            nodeToRemove = nodeToRemove.getNext();
        }

        Node<T> expectedPrevious = previous.clone();
        Node<T> expectedNode = nodeToRemove.clone();
        Node<T> expectedNext = nodeToRemove.getNext().clone();

        Lock prevLock = striped.get(previous);
        Lock currLock = striped.get(nodeToRemove);
        Lock nextLock = striped.get(nodeToRemove.getNext());

        try {
            prevLock.lock();
            currLock.lock();
            nextLock.lock();

            if (ListActions.compareValues(previous, nodeToRemove, nodeToRemove.getNext(), expectedPrevious, expectedNode, expectedNext)) {
                return removeConcreteNode(first, nodeToRemove, previous);
            }

        } finally {
            prevLock.unlock();
            currLock.unlock();
            nextLock.unlock();
        }

        return removeByIndexStriped(index, first, striped);
    }

    public static <T> Node<T> removeConcreteNode(Node<T> first, Node<T> node, Node<T> previous) {
        if (node == previous && node.getNext() != null) {
            first = node.getNext();
        } else {
            previous.setNext(node.getNext());
        }
        return first;
    }

    public static <T> boolean compareValues(Node<T> previous, Node<T> current, Node<T> next, Node<T> expectedPrevious, Node<T> expectedCurrent, Node<T> expectedNext) {
        if (previous.equals(current)) {
            return previous.equals(expectedPrevious) && (previous.getNext() == null) || (previous.getNext().equals(expectedPrevious.getNext()) && current.equals(expectedCurrent) && (next == null && expectedNext == null) || next.equals(expectedNext));
        } else {
            return previous.equals(expectedPrevious) && previous.getNext().equals(expectedCurrent) && current.equals(expectedCurrent) && (next == null && expectedNext == null) || next.equals(expectedNext);
        }
    }

    public static <T> int indexOf(Object o, Node<T> first) {
        int index = 0;

        if (o == null) {
            return -1;
        } else {
            for (Node<T> node = first; node != null; node = node.getNext()) {
                if (o.equals(node.getValue()))
                    return index;
                index++;
            }
        }

        return -1;
    }

    public static <T> int lastIndexOf(Object o, Node<T> first) {
        int index = 0;
        int lastIndex = -1;

        if (o == null) {
            return -1;
        } else {
            for (Node<T> node = first; node != null; node = node.getNext()) {
                if (o.equals(node.getValue()))
                    lastIndex = index;
                index++;
            }
        }

        return lastIndex;
    }

    public static <T> Node<T> getNodeWithIndex(int index, Node<T> first) {
        Node<T> node;

        node = first;

        for (int i = 0; i < index; i++) {
            node = node.getNext();
        }

        return node;
    }

    public static void checkNotNull(Object v) {
        if (v == null)
            throw new NullPointerException();
    }
}
