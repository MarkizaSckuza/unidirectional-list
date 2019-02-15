package com.margo.samples.unidirectional.list.common;

import com.margo.samples.unidirectional.list.common.lock.ListLock;
import com.margo.samples.unidirectional.list.common.node.Node;

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

    public static <T extends Comparable<T>> Node<T> findPlaceAndPutConcurrent(Node<T> first, Node<T> previousNode, Node<T> fromNode, Node<T> newNode, ListLock<T> listLock) {
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
                        findPlaceAndPutConcurrent(first, previous, node, newNode, listLock);
                    } else {
                        findPlaceAndPutConcurrent(first, null, first, newNode, listLock);
                    }
                }else {
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
                        findPlaceAndPutConcurrent(first, previous, node, newNode, listLock);
                    } else {
                        findPlaceAndPutConcurrent(first, null, first, newNode, listLock);
                    }
                }else {
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
                        findPlaceAndPutConcurrent(first, previous, node, newNode, listLock);
                    } else {
                        findPlaceAndPutConcurrent(first, null, first, newNode, listLock);
                    }
                } else {
                    return result;
                }
            }

            previous = node;
        }
        return null;
    }

    public static <T> Node<T> findPlaceAndRemoveConcurrent(Node<T> first, Node<T> previousNode, Node<T> fromNode, T value, ListLock<T> listLock) {
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
                        findPlaceAndRemoveConcurrent(first, previous, node, value, listLock);
                    } else {
                        findPlaceAndRemoveConcurrent(first, null, first, value, listLock);
                    }
                } else {
                    return result;
                }
            }
            previous = node;
        }
        return null;
    }

    public static <T extends Comparable<T>> Node<T> removeByIndexConcurrent(int index, Node<T> first, ListLock<T> listLock) {
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
            return removeByIndexConcurrent(index, first, listLock);
        }

        return  result;
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
}
