package com.litangping.haibei.utils.bitmap;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;


import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by LiTangping on 2016/3/8.
 */
public class DiskCacheTool {

    private static DiskLruCache mDiskLruCache;

    /**
     * 初始化
     * @param context
     */
    public static void init(Context context) {
        try {
            mDiskLruCache = mDiskLruCache.open(getCacheDirectory(context), getVersonCode(context), 1, 4 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前版本
     * @param context
     * @return
     */
    private static int getVersonCode(Context context) {
        try{
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }




    /**
     * 获取缓存目录
     * @param context
     * @return
     */
    public static File getCacheDirectory(Context context){
        //外部存储的Cache目录
        if(Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
                && !Environment.isExternalStorageRemovable()){
            //外部存储的缓存目录
            return context.getExternalCacheDir();
        }
        //内部存储的缓存目录
        return context.getCacheDir();
    }

    private static String getCacheKey(String url){
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(url.getBytes());
            byte[] bytes = md.digest();
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0;i<bytes.length;i++){
                stringBuffer.append(Integer.toHexString(0xFF&bytes[i]));
            }
            return stringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return String.valueOf(url.hashCode());
    }

    /**
     * 写入磁盘缓存
     * @param url
     * @param bitmap
     */
    public static void writeDiskCache(String url,Bitmap bitmap){
        String cacheKey = getCacheKey(url);
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(cacheKey);
            if(editor!=null){
                OutputStream outputStream = editor.newOutputStream(0);

                boolean isCompressSuccess = bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
                if(isCompressSuccess){
                    editor.commit();
                }else{
                    editor.abort();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取磁盘缓存的Bitmap对象
     * @param url
     * @return
     */
    public static Bitmap getCacheBitmap(String url){
        String cacheKey = getCacheKey(url);
        try{
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(cacheKey);
            if (snapshot!=null){
                InputStream inputStream = snapshot.getInputStream(0);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新日志文件：主要放到Activity的onDestory
     */
    public static void flush(){
        try{
            mDiskLruCache.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 关闭缓存
     */
    public static void close(){
        try{
            mDiskLruCache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
