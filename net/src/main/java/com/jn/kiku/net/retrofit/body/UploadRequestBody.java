package com.jn.kiku.net.retrofit.body;

import androidx.annotation.Nullable;

import com.jn.kiku.net.retrofit.callback.ProgressListener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Author：Stevie.Chen Time：2019/8/6
 * Class Comment：监听上传进度的RequestBody
 */
public class UploadRequestBody extends RequestBody {

    private final RequestBody mRequestBody;
    private final ProgressListener mProgressListener;

    public UploadRequestBody(RequestBody requestBody, ProgressListener progressListener) {
        mRequestBody = requestBody;
        mProgressListener = progressListener;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        BufferedSink bufferedSink = Okio.buffer(sink(sink));
        //写入
        mRequestBody.writeTo(bufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();
    }

    private ForwardingSink sink(Sink delegate) {
        return new ForwardingSink(delegate) {
            long totalBytesWrite = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                totalBytesWrite += byteCount;
                if (mProgressListener != null)
                    mProgressListener.onProgress(totalBytesWrite, mRequestBody.contentLength(), totalBytesWrite * 1.0f / mRequestBody.contentLength(), true);
            }
        };
    }
}
