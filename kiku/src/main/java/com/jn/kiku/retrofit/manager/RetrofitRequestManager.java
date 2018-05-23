package com.jn.kiku.retrofit.manager;

import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jn.kiku.BuildConfig;
import com.jn.kiku.retrofit.callback.ProgressListener;

import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @version V1.0
 * @ClassName: ${CLASS_NAME}
 * @Description: (Retrofit请求)
 * @create by: chenwei
 * @date 2018/5/21 15:51
 */
public class RetrofitRequestManager implements IRetrofitManage {

    private static final String TAG = Retrofit.class.getSimpleName();
    private String mBaseUrl = null;

    public RetrofitRequestManager(String base_url) {
        mBaseUrl = base_url;
    }

    @Override
    public Retrofit createRetrofit() {
        return createRetrofitBuilder().build();
    }

    @Override
    public Retrofit.Builder createRetrofitBuilder() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        OkHttpClient.Builder okHttpClientBuilder = createOkHttpClient();
        Gson gson = createGson();
        if (okHttpClientBuilder != null)
            builder.client(okHttpClientBuilder.build());
        if (gson != null)
            builder.addConverterFactory(GsonConverterFactory.create(gson));
        else
            builder.addConverterFactory(GsonConverterFactory.create());
        return builder;
    }

    @Override
    public Gson createGson() {
        Gson gson;
        if (Build.VERSION.SDK_INT >= 23) {
            GsonBuilder gsonBuilder = new GsonBuilder()
                    .excludeFieldsWithModifiers(Modifier.FINAL,
                            Modifier.TRANSIENT, Modifier.STATIC);
            gson = gsonBuilder.create();
        } else {
            gson = new Gson();
        }
        return gson;
    }

    @Override
    public OkHttpClient.Builder createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(getWriteTimeout(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);
        Interceptor httpLoggingInterceptor = createHttpLoggingInterceptor();
        Interceptor downloadInterceptor = createInterceptor();
        if (httpLoggingInterceptor != null)
            builder.addInterceptor(httpLoggingInterceptor);
        if (downloadInterceptor != null)
            builder.addInterceptor(downloadInterceptor);
        return builder;
    }

    @Override
    public Interceptor createHttpLoggingInterceptor() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.d(TAG, message);
            }
        });
        //Debug环境下才打印日志
        httpLoggingInterceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        return httpLoggingInterceptor;
    }

    @Override
    public Interceptor createInterceptor() {
        return null;
    }

    @Override
    public ProgressListener getProgressListener() {
        return null;
    }

    @Override
    public int getConnectTimeout() {
        return DEFAULT_CONNECTTIMEOUT_TIME;
    }

    @Override
    public int getReadTimeout() {
        return DEFAULT_READTIMEOUT_TIME;
    }

    @Override
    public int getWriteTimeout() {
        return DEFAULT_WRITETIMEOUT_TIME;
    }
}
