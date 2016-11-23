package com.smoothsync.smoothsetup.utils;

import org.dmfs.httpessentials.types.Link;
import org.dmfs.iterators.AbstractBaseIterator;
import org.dmfs.iterators.AbstractFilteredIterator;
import org.dmfs.iterators.FilteredIterator;

import java.util.Iterator;


/**
 * An Iterator that only returns {@link Link}s with a specific rel-type.
 *
 * @author Marten Gajda
 */
public final class Related extends AbstractBaseIterator<Link>
{
    private final Iterator<Link> mDelegate;


    public Related(Iterator<Link> delegate, final String rel)
    {
        mDelegate = new FilteredIterator<>(delegate, new AbstractFilteredIterator.IteratorFilter<Link>()
        {
            @Override
            public boolean iterate(Link element)
            {
                return element.relationTypes().contains(rel);
            }
        });
    }


    @Override
    public boolean hasNext()
    {
        return mDelegate.hasNext();
    }


    @Override
    public Link next()
    {
        return mDelegate.next();
    }
}
