package com.smoothsync.smoothsetup.utils;

import org.dmfs.iterators.AbstractBaseIterator;
import org.dmfs.iterators.SingletonIterator;

import java.util.Iterator;


/**
 * An {@link Iterator} decorator that always returns at least one element.
 *
 * @author Marten Gajda
 */
public final class Default<E> extends AbstractBaseIterator<E>
{
    private final Iterator<E> mDelegate;


    public Default(Iterator<E> delegate, E defaultValue)
    {
        mDelegate = delegate.hasNext() ? delegate : new SingletonIterator<>(defaultValue);
    }


    @Override
    public boolean hasNext()
    {
        return mDelegate.hasNext();
    }


    @Override
    public E next()
    {
        return mDelegate.next();
    }
}
