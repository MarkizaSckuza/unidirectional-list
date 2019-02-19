package com.margo.samples.unidirectional.list.common.lock.strategy;

import com.margo.samples.unidirectional.list.common.lock.ListLock;
import com.margo.samples.unidirectional.list.common.node.Node;

import java.util.concurrent.locks.Lock;

public class ConcurrentStrategy<T extends Comparable<T>, L extends Lock> extends AbstractBaseStrategy<T> {

    private ListLock<T> listLock;

    public ConcurrentStrategy(ListLock<T> listLock) {
        this.listLock = listLock;
    }

    @Override
    public Node<T> add(Node<T> first, Node<T> previousNode, Node<T> fromNode, Node<T> newNode) {
        Node<T> previous = previousNode == null ? first : previousNode;

        for (Node<T> node = fromNode; node != null; node = node.getNext()) {
            if (node.getValue().compareTo(newNode.getValue()) == 0) {
                Node<T> prev = previous.clone();
                Node<T> curr = node.clone();

                Node<T> next = node.getNext() == null ? null : node.getNext().clone();

                Node<T> finalPrevious = previous;
                Node<T> finalNode = node;
                Node<T> finalFirst = first;

                Node<T> result = listLock.lock(previous, node, node.getNext(), () -> {
                    if (compareValues(finalPrevious, finalNode, finalNode.getNext(), prev, curr, next) && finalNode.getValue().compareTo(newNode.getValue()) == 0) {
                        newNode.setNext(finalNode.getNext());
                        finalNode.setNext(newNode);

                        return finalFirst;
                    }
                    return null;
                });

                if (result == null) {
                    if (previous != null) {
                        add(first, previous, node, newNode);
                    } else {
                        add(first, null, first, newNode);
                    }
                } else {
                    return result;
                }
            } else if (node.getValue().compareTo(newNode.getValue()) == 1) {
                Node<T> prev = previous.clone();
                Node<T> curr = node.clone();

                Node<T> next = node.getNext() == null ? null : node.getNext().clone();

                Node<T> finalPrevious = previous;
                Node<T> finalNode = node;

                Node<T> result = listLock.lock(previous, node, node.getNext(), () -> {
                    if (compareValues(finalPrevious, finalNode, finalNode.getNext(), prev, curr, next) && finalNode.getValue().compareTo(newNode.getValue()) == 1) {
                        newNode.setNext(finalNode);

                        Node<T> firstNode = first;

                        if (finalPrevious == first) {
                            firstNode = newNode;
                        } else {
                            finalPrevious.setNext(newNode);
                        }

                        return firstNode;
                    }
                    return null;
                });

                if (result == null) {
                    if (node != null) {
                        add(first, previous, node, newNode);
                    } else {
                        add(first, null, first, newNode);
                    }
                } else {
                    return result;
                }
            } else if (node.getValue().compareTo(newNode.getValue()) == -1 && node.getNext() == null) {
                Node<T> prev = previous.clone();
                Node<T> curr = node.clone();

                Node<T> finalPrevious = previous;
                Node<T> finalNode = node;

                Node<T> result = listLock.lock(previous, node, null, () -> {
                    if (compareValues(finalPrevious, finalNode, null, prev, curr, null) && finalNode.getValue().compareTo(newNode.getValue()) == -1 && finalNode.getNext() == null) {
                        finalNode.setNext(newNode);

                        return first;
                    }
                    return null;
                });

                if (result == null) {
                    if (node != null) {
                        add(first, previous, node, newNode);
                    } else {
                        add(first, null, first, newNode);
                    }
                } else {
                    return result;
                }
            }

            previous = node;
        }
        return null;
    }

    @Override
    public Node<T> remove(Node<T> first, Node<T> previousNode, Node<T> fromNode, T value) {
        Node<T> previous = previousNode == null ? first : previousNode;

        for (Node<T> node = fromNode; node != null; node = node.getNext()) {
            if (value.equals(node.getValue())) {
                Node<T> prev = previous.clone();
                Node<T> curr = node.clone();

                Node<T> next = node.getNext() == null ? null : node.getNext().clone();

                Node<T> finalPrevious = previous;
                Node<T> finalNode = node;
                Node<T> result = listLock.lock(previous, node, null, () -> {
                    if (compareValues(finalPrevious, finalNode, finalNode.getNext(), prev, curr, next)) {
                        return removeConcreteNode(first, finalNode, finalPrevious);
                    }
                    return null;
                });

                if (result == null) {
                    if (node != null) {
                        remove(first, previous, node, value);
                    } else {
                        remove(first, null, first, value);
                    }
                } else {
                    return result;
                }
            }
            previous = node;
        }
        return null;
    }

    @Override
    public Node<T> removeByIndex(int index, Node<T> first) {
        Node<T> nodeToRemove = first;
        Node<T> previous = first;

        for (int i = 0; i < index; i++) {
            previous = nodeToRemove;
            nodeToRemove = nodeToRemove.getNext();
        }

        Node<T> expectedPrevious = previous.clone();
        Node<T> expectedNode = nodeToRemove.clone();
        Node<T> expectedNext = nodeToRemove.getNext().clone();

        Node<T> finalPrevious = previous;
        Node<T> finalNodeToRemove = nodeToRemove;
        Node<T> finalNodeToRemove1 = nodeToRemove;

        Node<T> result = listLock.lock(previous, nodeToRemove, null, () -> {
            if (compareValues(finalPrevious, finalNodeToRemove, finalNodeToRemove1.getNext(), expectedPrevious, expectedNode, expectedNext)) {
                return removeConcreteNode(first, finalNodeToRemove, finalPrevious);
            }
            return null;
        });

        if (result == null) {
            return removeByIndex(index, first);
        }

        return result;
    }
}
