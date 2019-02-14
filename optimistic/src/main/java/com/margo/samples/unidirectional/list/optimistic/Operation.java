package com.margo.samples.unidirectional.list.optimistic;

public class Operation {
    private Action action;
    private Object object;

    public Operation(Action action, Object object) {
        this.action = action;
        this.object = object;
    }

    public Action getAction() {
        return action;
    }

    public Object getObject() {
        return object;
    }
}
