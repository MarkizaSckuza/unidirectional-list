package com.margo.samples.unidirectional.list.common.lock;

import com.margo.samples.unidirectional.list.common.action.Action;
import com.margo.samples.unidirectional.list.common.node.Node;

import java.util.concurrent.locks.Lock;

public class StripedListLock<T, L extends Lock> implements ListLock<T> {

    private Striped<L> striped;

    public StripedListLock(Striped<L> striped) {
        this.striped = striped;
    }

    @Override
    public Node<T> lock(Node<T> previous, Node<T> current, Node<T> next, Action<Node<T>> action) {
        L prevLock = striped.get(previous);
        L currLock = striped.get(current);
        L nextLock = next == null ? null : striped.get(next);

        try {
            prevLock.lock();
            currLock.lock();
            if (nextLock != null) {
                nextLock.lock();
            }

            return action.doAction();

        } finally {
            prevLock.unlock();
            currLock.unlock();
            if (nextLock != null) {
                nextLock.unlock();
            }
        }
    }
}
