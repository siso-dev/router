/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oakfusion.router.util;


/**
 * This class is borrowed from Android sources.
 *
 * SparseArrays map integers to Objects.  Unlike a normal array of Objects,
 * there can be gaps in the indices.  It is intended to be more efficient
 * than using a HashMap to map Integers to Objects.
 */
public class SparseArray<E> {
	
    private static final Object DELETED = new Object();
	
    private boolean garbage = false;

	private int[] keys;
	private Object[] values;
	private int size;

	/**
     * Creates a new SparseArray containing no mappings.
     */
    public SparseArray() {
        this(10);
    }

    /**
     * Creates a new SparseArray containing no mappings that will not
     * require any additional memory allocation to store the specified
     * number of mappings.
     */
    public SparseArray(int initialCapacity) {
        initialCapacity = idealIntArraySize(initialCapacity);

        keys = new int[initialCapacity];
        values = new Object[initialCapacity];
        size = 0;
    }

	private int idealIntArraySize(int need) {
		return idealByteArraySize(need * 4) / 4;
	}

	private int idealByteArraySize(int need) {
		for (int i = 4; i < 32; i++) {
			if (need <= (1 << i) - 12) {
				return (1 << i) - 12;
			}
		}
		return need;
	}

	/**
     * Gets the Object mapped from the specified key, or <code>null</code>
     * if no such mapping has been made.
     */
    public E get(int key) {
        return get(key, null);
    }

    /**
     * Gets the Object mapped from the specified key, or the specified Object
     * if no such mapping has been made.
     */
    public E get(int key, E valueIfKeyNotFound) {
        int i = binarySearch(keys, 0, size, key);

        if (i < 0 || values[i] == DELETED) {
            return valueIfKeyNotFound;
        } else {
            return (E) values[i];
        }
    }

    /**
     * Removes the mapping from the specified key, if there was any.
     */
    public void delete(int key) {
        int i = binarySearch(keys, 0, size, key);

        if (i >= 0) {
            if (values[i] != DELETED) {
                values[i] = DELETED;
                garbage = true;
            }
        }
    }

    /**
     * Alias for {@link #delete(int)}.
     */
    public void remove(int key) {
        delete(key);
    }

    private void gc() {
        int n = size;
        int o = 0;
        int[] keys = this.keys;
        Object[] values = this.values;

        for (int i = 0; i < n; i++) {
            Object val = values[i];

            if (val != DELETED) {
                if (i != o) {
                    keys[o] = keys[i];
                    values[o] = val;
                }
                o++;
            }
        }

        garbage = false;
        size = o;
    }

    /**
     * Adds a mapping from the specified key to the specified value,
     * replacing the previous mapping from the specified key if there
     * was one.
     */
    public void put(int key, E value) {
        int i = binarySearch(keys, 0, size, key);

        if (i >= 0) {
            values[i] = value;
        } else {
            i = ~i;

            if (i < size && values[i] == DELETED) {
                keys[i] = key;
                values[i] = value;
                return;
            }

            if (garbage && size >= keys.length) {
                gc();

                // Search again because indices may have changed.
                i = ~binarySearch(keys, 0, size, key);
            }

            if (size >= keys.length) {
                int n = idealIntArraySize(size + 1);

                int[] nkeys = new int[n];
                Object[] nvalues = new Object[n];

                System.arraycopy(keys, 0, nkeys, 0, keys.length);
                System.arraycopy(values, 0, nvalues, 0, values.length);

                keys = nkeys;
                values = nvalues;
            }

            if (size - i != 0) {
                System.arraycopy(keys, i, keys, i + 1, size - i);
                System.arraycopy(values, i, values, i + 1, size - i);
            }

            keys[i] = key;
            values[i] = value;
            size++;
        }
    }

    /**
     * Returns the number of key-value mappings that this SparseArray
     * currently stores.
     */
    public int size() {
        if (garbage) {
            gc();
        }

        return size;
    }

    /**
     * Given an index in the range <code>0...size()-1</code>, returns
     * the key from the <code>index</code>th key-value mapping that this
     * SparseArray stores.  
     */
    public int keyAt(int index) {
        if (garbage) {
            gc();
        }

        return keys[index];
    }
    
    /**
     * Given an index in the range <code>0...size()-1</code>, returns
     * the value from the <code>index</code>th key-value mapping that this
     * SparseArray stores.  
     */
    public E valueAt(int index) {
        if (garbage) {
            gc();
        }

        return (E) values[index];
    }

    /**
     * Given an index in the range <code>0...size()-1</code>, sets a new
     * value for the <code>index</code>th key-value mapping that this
     * SparseArray stores.  
     */
    public void setValueAt(int index, E value) {
        if (garbage) {
            gc();
        }

        values[index] = value;
    }
    
    /**
     * Returns the index for which {@link #keyAt} would return the
     * specified key, or a negative number if the specified
     * key is not mapped.
     */
    public int indexOfKey(int key) {
        if (garbage) {
            gc();
        }

        return binarySearch(keys, 0, size, key);
    }

    /**
     * Returns an index for which {@link #valueAt} would return the
     * specified key, or a negative number if no keys map to the
     * specified value.
     * Beware that this is a linear search, unlike lookups by key,
     * and that multiple keys can map to the same value and this will
     * find only one of them.
     */
    public int indexOfValue(E value) {
        if (garbage) {
            gc();
        }

        for (int i = 0; i < size; i++)
            if (values[i] == value)
                return i;

        return -1;
    }

    /**
     * Removes all key-value mappings from this SparseArray.
     */
    public void clear() {
        int n = size;
        Object[] values = this.values;

        for (int i = 0; i < n; i++) {
            values[i] = null;
        }

        size = 0;
        garbage = false;
    }

    /**
     * Puts a key/value pair into the array, optimizing for the case where
     * the key is greater than all existing keys in the array.
     */
    public void append(int key, E value) {
        if (size != 0 && key <= keys[size - 1]) {
            put(key, value);
            return;
        }

        if (garbage && size >= keys.length) {
            gc();
        }

        int pos = size;
        if (pos >= keys.length) {
            int n = idealIntArraySize(pos + 1);

            int[] nkeys = new int[n];
            Object[] nvalues = new Object[n];

            System.arraycopy(keys, 0, nkeys, 0, keys.length);
            System.arraycopy(values, 0, nvalues, 0, values.length);

            keys = nkeys;
            values = nvalues;
        }

        keys[pos] = key;
        values[pos] = value;
        size = pos + 1;
    }
    
    private static int binarySearch(int[] a, int start, int len, int key) {
        int high = start + len, low = start - 1, guess;

        while (high - low > 1) {
            guess = (high + low) / 2;
            if (a[guess] < key) {
				low = guess;
			} else {
				high = guess;
			}
        }

        if (high == start + len) {
			return ~(start + len);
		}
		if (a[high] == key) {
			return high;
		}
		return ~high;
	}

}
