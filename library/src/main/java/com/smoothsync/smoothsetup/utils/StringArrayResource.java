package com.smoothsync.smoothsetup.utils;

import android.content.Context;

import org.dmfs.jems2.iterator.Seq;

import java.util.Iterator;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;


public final class StringArrayResource implements Iterable<String>
{
    private final Context mContext;
    @ArrayRes
    private final int arrayResource;


    public StringArrayResource(Context context, int arrayResource)
    {
        mContext = context.getApplicationContext();
        this.arrayResource = arrayResource;
    }


    @NonNull
    @Override
    public Iterator<String> iterator()
    {
        return new Seq<>(mContext.getResources().getStringArray(arrayResource));
    }
}
