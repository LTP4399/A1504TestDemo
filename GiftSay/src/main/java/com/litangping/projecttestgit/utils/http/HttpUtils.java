package com.litangping.haibei.utils.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LiTangping on 2016/3/4.
 * <p/>
 * &#x7f51;&#x7edc;&#x8bf7;&#x6c42;&#x7684;&#x5de5;&#x5177;&#x7c7b;
 */
public class HttpUtils {
    private static final int RESPONSE_SUCCESS = 0x12;//请求成功
    private static final int RESPONSE_FAILED = 0x13;//连接出错
    private static final int RESPONSE_ERROR = 0x14;//返回出错
    private static final int REQUESTCODE_REPEAT = 0x15;//请求码已存在


    private static ExecutorService mExecutorService;
    private static Map<Integer,HttpResponseListener> listenerMap;

    static {
        //创建一个定长的线程池
        mExecutorService = Executors.newFixedThreadPool(4);
        listenerMap = new HashMap<>();
    }

    static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HttpResponseListener listener = listenerMap.get(msg.arg1);
            switch (msg.what){
                case RESPONSE_SUCCESS:
                    listener.onSuccessResponse(msg.obj.toString(),msg.arg1);
                    break;
                case RESPONSE_FAILED:
                    listener.onFailed(msg.arg2,msg.arg1);
                    break;
                case RESPONSE_ERROR:
                    listener.onError((Exception) msg.obj,msg.arg1);
                    break;
                case REQUESTCODE_REPEAT:

                    break;
            }
            listenerMap.remove(msg.arg1);

        }
    };

    /**
     * 发送HTTP请求,请求方式为GET
     * @param urlStr 请求的URL
     * @param listener 请求数据的监听器
     * @param requestId 请求的ID
     * @return 如果请求ID已存在于队列中，或者正在执行相同ID的请求时，返回FALSE，否则返回TRUE
     */
    public static boolean requestGet(final String urlStr, final HttpResponseListener listener,final int requestId) {
        if(listenerMap.containsKey(requestId)){
            return false;
        }else{
            listenerMap.put(requestId,listener);
        }
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                httpGet(urlStr, requestId);
            }
        });
        return true;
    }


    /**
     * 发送HTTP请求,请求方式为POST
     * @param urlStr 请求的URL
     * @param map  发送POST请求的附加数据
     * @param listener 请求数据的监听器
     * @param requestId 请求的ID
     * @return 如果请求ID已存在于队列中，或者正在执行相同ID的请求时，返回FALSE，否则返回TRUE
     */
    public static boolean requestPost(final String urlStr, final Map<String, String> map, final HttpResponseListener listener,final int requestId) {
        if(listenerMap.containsKey(requestId)){
            return false;
        }else{
            listenerMap.put(requestId,listener);
        }
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                httpPost(urlStr, map, requestId);
            }
        });
        return true;
    }


    /**
     * HttpGet请求的实现部分
     *
     * @param urlStr
     * @param requestId
     * @return
     */
    private static void httpGet(String urlStr, int requestId) {
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        StringBuffer sb = new StringBuffer();
        Message message = new Message();
        message.arg1 = requestId;
        try {
//            new String(urlStr.getBytes("UTF-8"));
            URL url = new URL(new String(urlStr.getBytes("GBK")));
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.addRequestProperty("Accept-Charset","GBK");
            httpURLConnection.addRequestProperty("content","text/plain;charset=GBK");
            httpURLConnection.connect();
            //获取请求的状态码
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                //表示请求成功
                inputStream = httpURLConnection.getInputStream();
                int len = 0;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, len));
                }
                message.what = RESPONSE_SUCCESS;
                message.obj = sb.toString();
                handler.sendMessage(message);
            }else{
                message.what = RESPONSE_FAILED;
                message.arg2 = httpURLConnection.getResponseCode();
                handler.sendMessage(message);
            }
        } catch (IOException e) {
            message.what = RESPONSE_ERROR;
            message.obj = e;
            handler.sendMessage(message);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }


    /**
     * 将要发送的map转化为url的后缀
     * @param map 待转化的字符map
     * @return 待发送的数据，不包含？（便于POST请求）
     * @throws UnsupportedEncodingException
     */
    private static String toQuery(Map<String, String> map) {
        Set<String> set = map.keySet();
        StringBuffer sb = new StringBuffer();
        for (String string : set) {
            sb.append(string).append("=");
            String temp = map.get(string);
            if (isContainChinese(temp)) {
                try {
                    temp = URLEncoder.encode(temp, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            sb.append(temp).append("&");
        }
        return sb.toString().substring(0, sb.toString().length() - 1);// 去掉末尾&
    }

    /**
     * 判断是否有需要转化为Unicode字符的字符
     *
     * @param str
     * @return
     */
    private static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 发送HTTP请求,请求方式为POST
     * @param urlStr 请求的URL
     * @param map  发送POST请求的附加数据
     * @param requestId 请求的ID
     * @return 如果请求ID已存在于队列中，或者正在执行相同ID的请求时，返回FALSE，否则返回TRUE
     */
    private static void httpPost(String urlStr, Map<String, String> map, int requestId) {
        StringBuffer stringBuffer = new StringBuffer();// 返回的字符串
        BufferedWriter bufferedWriter = null;
        HttpURLConnection httpURLConnection = null;
        Message message = new Message();
        message.arg1 = requestId;
        try {
            httpURLConnection = (HttpURLConnection) new URL(urlStr).openConnection();
            httpURLConnection.setDoOutput(true);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream()));
            bufferedWriter.write(toQuery(map));
            bufferedWriter.flush();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                String str = null;
                while ((str = bufferedReader.readLine()) != null) {
                    stringBuffer.append(str);
                }
                message.what = RESPONSE_SUCCESS;
                message.obj = stringBuffer.toString();
                handler.sendMessage(message);
            }else{
                message.what = RESPONSE_FAILED;
                message.arg2 = httpURLConnection.getResponseCode();
                handler.sendMessage(message);
            }
        } catch (IOException e) {
            message.what = RESPONSE_ERROR;
            message.obj = e;
            handler.sendMessage(message);
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }


    public static boolean isNetWorkConn(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        } else {
            return networkInfo.isConnected();
        }
    }

}
