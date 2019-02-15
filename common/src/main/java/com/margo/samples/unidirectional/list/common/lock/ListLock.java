package com.margo.samples.unidirectional.list.common.lock;

import com.margo.samples.unidirectional.list.common.action.Action;
import com.margo.samples.unidirectional.list.common.node.Node;

public interface ListLock<T> {

    Node<T> lock(Node<T> previous, Node<T> current, Node<T> next, Action<Node<T>> action);
}
