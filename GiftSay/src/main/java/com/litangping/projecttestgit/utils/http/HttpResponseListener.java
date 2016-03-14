package com.litangping.haibei.utils.http;

/**
 * Created by LiTangping on 2016/3/4.
 */
public interface HttpResponseListener {
    void onSuccessResponse(String response, int requestId);
    void onFailed(int responeCode, int requestId);
    void onError(Exception e, int requestId);
}
