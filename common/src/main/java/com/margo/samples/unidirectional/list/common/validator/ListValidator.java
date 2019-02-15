package com.margo.samples.unidirectional.list.common.validator;

public class ListValidator {

    public static void validateIndex(int index, int size) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds of an array");
        }
    }

    public static void validateObjectNotNull(Object o) {
        if (o == null)
            throw new NullPointerException();
    }
}
