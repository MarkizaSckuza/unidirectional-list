package com.margo.samples.unidirectional.list.common.lock;

import com.google.common.base.Preconditions;
import com.google.common.math.IntMath;

import java.math.RoundingMode;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

public abstract class Striped<L> {

    public static final int MAX_POWER_OF_TWO = 1 << (Integer.SIZE - 2);
    /**
     * A bit mask were all bits are set.
     */
    private static final int ALL_SET = ~0;

    private Striped() {
    }

    public abstract L get(Object key);

    public abstract L getAt(int index);

    abstract int indexFor(Object key);

    public abstract int size();

    public abstract void clear();

    public static Striped<ReentrantLock> reentrantLock(int stripes) {
        return custom(stripes, REENTRANT_LOCK_SUPPLIER);
    }

    public static Striped<ReadWriteLock> readWriteLock(int stripes) {
        return custom(stripes, READ_WRITE_LOCK_SUPPLIER);
    }

    private static final Supplier<ReentrantLock> REENTRANT_LOCK_SUPPLIER =
            new Supplier<ReentrantLock>() {
                @Override
                public ReentrantLock get() {
                    return new ReentrantLock();
                }
            };

    private static final Supplier<ReadWriteLock> READ_WRITE_LOCK_SUPPLIER =
            new Supplier<ReadWriteLock>() {
                @Override
                public ReadWriteLock get() {
                    return new ReentrantReadWriteLock();
                }
            };

    static <L> Striped<L> custom(int stripes, Supplier<L> supplier) {
        return new CompactStriped<>(stripes, supplier);
    }

    /**
     * Implementation of Striped where 2^k stripes are represented as an array of the same length,
     * eagerly initialized.
     */
    private static class CompactStriped<L> extends PowerOfTwoStriped<L> {
        /**
         * Size is a power of two.
         */
        private final Object[] array;

        private CompactStriped(int stripes, Supplier<L> supplier) {
            super(stripes);
            Preconditions.checkArgument(stripes <= MAX_POWER_OF_TWO, "Stripes must be <= 2^30)");

            this.array = new Object[mask + 1];
            for (int i = 0; i < array.length; i++) {
                array[i] = supplier.get();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public L getAt(int index) {
            return (L) array[index];
        }

        @Override
        public int size() {
            return array.length;
        }

        @Override
        public void clear() {
            for (Object o : array) {
                o = null;
            }
        }
    }

    private abstract static class PowerOfTwoStriped<L> extends Striped<L> {
        /**
         * Capacity (power of two) minus one, for fast mod evaluation
         */
        final int mask;

        PowerOfTwoStriped(int stripes) {
            Preconditions.checkArgument(stripes > 0, "Stripes must be positive");
            this.mask = stripes > MAX_POWER_OF_TWO ? ALL_SET : ceilToPowerOfTwo(stripes) - 1;
        }

        @Override
        final int indexFor(Object key) {
            int hash = smear(key.hashCode());
            return hash & mask;
        }

        @Override
        public final L get(Object key) {
            return getAt(indexFor(key));
        }
    }

    private static int ceilToPowerOfTwo(int x) {
        return 1 << IntMath.log2(x, RoundingMode.CEILING);
    }

    private static int smear(int hashCode) {
        hashCode ^= (hashCode >>> 20) ^ (hashCode >>> 12);
        return hashCode ^ (hashCode >>> 7) ^ (hashCode >>> 4);
    }

}
