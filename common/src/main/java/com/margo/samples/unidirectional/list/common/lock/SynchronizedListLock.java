package com.margo.samples.unidirectional.list.common.lock;

import com.margo.samples.unidirectional.list.common.action.Action;
import com.margo.samples.unidirectional.list.common.node.Node;

public class SynchronizedListLock<T> implements ListLock<T> {

    @Override
    public Node<T> lock(Node<T> previous, Node<T> current, Node<T> next, Action<Node<T>> action) {
        synchronized (previous) {
            synchronized (current) {
                if (next != null) {
                    synchronized (next) {
                        return action.doAction();
                    }
                } else {
                    return action.doAction();
                }
            }
        }
    }
}
