package com.jn.kiku.retrofit.body;

import android.support.annotation.Nullable;

import com.jn.kiku.retrofit.callback.ProgressListener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * @version V1.0
 * @ClassName: ${CLASS_NAME}
 * @Description: (监听下载进度的ResponseBody)
 * @create by: chenwei
 * @date 2018/5/18 16:58
 */
public class DownloadResponseBody extends ResponseBody {

    private final ResponseBody responseBody;
    private final ProgressListener listener;
    private BufferedSource bufferedSource;

    public DownloadResponseBody(ResponseBody responseBody, ProgressListener listener) {
        this.responseBody = responseBody;
        this.listener = listener;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (null == bufferedSource) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                if (listener != null)
                    listener.onProgress(totalBytesRead, responseBody.contentLength(), totalBytesRead * 1.0f / responseBody.contentLength(), bytesRead == -1);
                return bytesRead;
            }
        };
    }

}
