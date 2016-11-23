package com.smoothsync.smoothsetup.utils;

import java.util.Iterator;


/**
 * The number of elements returned by an {@link Iterator}.
 *
 * @author Marten Gajda.
 */
public final class Count extends Number
{
    private final Iterator<?> mIterator;
    private int mCount = -1;


    public Count(Iterator<?> iterator)
    {
        mIterator = iterator;
    }


    @Override
    public double doubleValue()
    {
        return count();
    }


    @Override
    public float floatValue()
    {
        return count();
    }


    @Override
    public int intValue()
    {
        return count();
    }


    @Override
    public long longValue()
    {
        return count();
    }


    private int count()
    {
        if (mCount < 0)
        {
            int count = 0;
            while (mIterator.hasNext())
            {
                count += 1;
                mIterator.next();
            }
            mCount = count;
        }
        return mCount;
    }
}
