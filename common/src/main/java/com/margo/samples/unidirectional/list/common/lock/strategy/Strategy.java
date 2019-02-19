package com.margo.samples.unidirectional.list.common.lock.strategy;

import com.margo.samples.unidirectional.list.common.node.Node;

public interface Strategy<T extends Comparable<T>> {

    Node<T> add(Node<T> first, Node<T> previousNode, Node<T> fromNode, Node<T> newNode);

    Node<T> remove(Node<T> first, Node<T> previousNode, Node<T> fromNode, T value);

    Node<T> removeByIndex(int index, Node<T> first);

    Node<T> removeConcreteNode(Node<T> first, Node<T> node, Node<T> previous);
}
