package com.margo.samples.unidirectional.list.common;

import com.margo.samples.unidirectional.list.common.validator.ListValidator;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;

public abstract class AbstractUnidirectionalArrayList<T extends Comparable<T>> implements UnidirectionalList<T> {

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contains(Object o) {
        ListValidator.validateObjectNotNull(o);

        return indexOf(o) != -1;
    }

    public boolean containsAll(Collection<?> c) {
        ListValidator.validateObjectNotNull(c);

        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }

    public boolean addAll(Collection<? extends T> c) {
        ListValidator.validateObjectNotNull(c);

        Object[] array = c.toArray();
        boolean result = false;

        for (Object o : array) {
            result = add((T) o);
        }

        return result;
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        throw new NotImplementedException();
    }

    public boolean retainAll(Collection<?> c) {
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        ListValidator.validateObjectNotNull(c);

        Object[] array = c.toArray();
        boolean result = false;

        for (Object o : array) {
            result = remove(o);
        }

        return result;
    }

    public T set(int index, T element) {
        throw new NotImplementedException();
    }

    public void add(int index, T element) {
        throw new NotImplementedException();
    }
}
