package com.litangping.haibei.utils.bitmap;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Created by LiTangping on 2016/3/8.
 */
public class MemoryCacheTool {
    private static LruCache<String,Bitmap> mLruCache;

    static {
        //参数：
        int memory = (int) (Runtime.getRuntime().maxMemory()*1/8);
        mLruCache = new LruCache<String,Bitmap>(memory){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }


    public static void putCache(String url,Bitmap bitmap){
        mLruCache.put(url,bitmap);
    }

    public static Bitmap getCache(String url){
        return mLruCache.get(url);
    }
}
