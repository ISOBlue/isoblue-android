/*
 * Author: Alex Layton <awlayton@purdue.edu>
 * 
 * Copyright (c) 2013 Purdue University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.isoblue.isoblue;

import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;
import java.util.Vector;

/**
 * @author awlayton
 * 
 */
public class ConstantIndexVector<E> implements Collection<E>, RandomAccess {

    private Vector<E> mVector;
    private int mSize;

    public ConstantIndexVector() {
        mVector = new Vector<E>();
        mSize = 0;
    }

    public int indexOf(E elm) {
        return mVector.indexOf(elm);
    }

    private int minFreeIndex() {
        return mVector.indexOf(null);
    }

    private void trimNulls() {
        while (mVector.lastElement() == null)
            mVector.removeElementAt(mVector.size() - 1);
    }

    @Override
    public synchronized boolean add(E object) {
        int idx;

        if (object == null)
            throw new NullPointerException();
        if (mVector.contains(object))
            return false;

        idx = minFreeIndex();

        if (idx < 0)
            mVector.addElement(object);
        else
            mVector.setElementAt(object, idx);

        mSize++;

        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> arg0) {
        boolean changed;

        changed = false;
        for (E elm : arg0)
            changed = changed || this.add(elm);

        return changed;
    }

    @Override
    public synchronized void clear() {
        mVector.clear();
        mSize = 0;
    }

    @Override
    public boolean contains(Object object) {
        if (object == null)
            throw new NullPointerException();

        return mVector.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> arg0) {
        if (arg0 == null || arg0.contains(null))
            throw new NullPointerException();

        return mVector.containsAll(arg0);
    }

    @Override
    public boolean isEmpty() {
        return mVector.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public synchronized boolean remove(Object object) {
        if (object == null)
            throw new NullPointerException();

        if (mVector.remove(object)) {
            mSize--;
            return true;
        } else {
            return false;
        }
    }

    private synchronized boolean removeCollection(Collection<?> arg0,
            boolean included) {
        Iterator<E> it;
        boolean changed;

        changed = false;
        it = mVector.iterator();
        while (it.hasNext()) {
            if (arg0.contains(it.next()) == changed) {
                it.remove();
                mSize--;
                changed = true;
            }
        }

        trimNulls();
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> arg0) {
        if (arg0 == null || arg0.contains(null))
            throw new NullPointerException();

        return removeCollection(arg0, true);
    }

    @Override
    public boolean retainAll(Collection<?> arg0) {
        if (arg0 == null || arg0.contains(null))
            throw new NullPointerException();

        return removeCollection(arg0, false);
    }

    @Override
    public synchronized int size() {
        return mSize;
    }

    @Override
    public Object[] toArray() {
        return mVector.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return mVector.toArray(array);
    }

    private class Itr implements Iterator<E> {

        private Iterator<E> mIt;

        private Itr() {
            mIt = ConstantIndexVector.this.mVector.iterator();
        }

        @Override
        public boolean hasNext() {
            return mIt.hasNext();
        }

        @Override
        public E next() {
            E next;

            /* Skip over empty indices */
            do {
                next = mIt.next();
            } while (next == null);

            return next;
        }

        @Override
        public void remove() {
            mIt.remove();
        }
    }
}
