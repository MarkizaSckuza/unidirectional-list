package com.margo.samples.unidirectional.list.common.node;

public class Node<T> {
    private volatile T value;
    private volatile Node<T> next;

    public Node(T value) {
        this.value = value;
    }

    public Node(T value, Node<T> next) {
        this.value = value;
        this.next = next;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Node<T> getNext() {
        return next;
    }

    public void setNext(Node<T> next) {
        this.next = next;
    }

    @Override
    public Node<T> clone() {
        return new Node<>(this.value, this.next);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node<?> node = (Node<?>) o;

        return value.equals(node.value);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result;
        return result;
    }
}
