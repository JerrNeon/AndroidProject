package com.jn.kiku.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.jn.kiku.R;
import com.jn.kiku.entiy.VersionUpdateVO;
import com.jn.kiku.receiver.VersionUpdateReceiver;
import com.jn.kiku.retrofit.RetrofitManage;
import com.jn.kiku.retrofit.callback.ProgressListener;
import com.jn.kiku.utils.FileIOUtils;
import com.jn.kiku.utils.FileTypeUtils;
import com.jn.kiku.utils.ImageUtil;
import com.jn.kiku.utils.IntentUtils;
import com.jn.kiku.utils.UriUtils;

import java.io.File;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * @version V1.0
 * @ClassName: ${CLASS_NAME}
 * @Description: (版本更新服务)
 * @create by: chenwei
 * @date 2018/5/18 12:05
 */
public class VersionUpdateService extends Service {

    protected Notification mNotification = null;//下载任务栏通知框
    private float mCurrentProgress = 0;//下载进度

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        VersionUpdateVO versionUpdateVO = intent.getParcelableExtra(VersionUpdateVO.class.getSimpleName());
        downloadFile(versionUpdateVO);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 下载文件
     *
     * @param versionUpdateVO
     */
    private void downloadFile(final VersionUpdateVO versionUpdateVO) {
        final NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
        builder.setSmallIcon(versionUpdateVO.getAppIconResId());
        builder.setContentTitle(versionUpdateVO.getAppName());
        builder.setTicker(getResources().getString(R.string.versionUpdate_downloadStart));
        final String downLoadFileName = versionUpdateVO.getAppName() + System.currentTimeMillis();
        RetrofitManage.getInstance()
                .getDownloadObservable(versionUpdateVO.getDownLoadUrl(), new ProgressListener() {
                    @Override
                    public void onProgress(long progressBytes, long totalBytes, float progressPercent, boolean done) {
                        //计算每百分之5刷新一下通知栏
                        float progress2 = progressPercent * 100;
                        if (progress2 - mCurrentProgress > 5) {
                            mCurrentProgress = progress2;
                            builder.setContentText(String.format(getResources().getString(R.string.versionUpdate_downloadProgress), (int) progress2));
                            builder.setProgress(100, (int) progress2, false);
                            manager.notify(0, builder.build());
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Function<ResponseBody, String>() {

                    @Override
                    public String apply(ResponseBody responseBody) throws Exception {
                        String fileSuffix = FileTypeUtils.getFileSuffix(responseBody.contentType().toString());//文件后缀名
                        String filePath = ImageUtil.getFileCacheFile().getAbsolutePath() + File.separator + downLoadFileName + "." + fileSuffix;
                        FileIOUtils.writeFileFromIS(filePath, responseBody.byteStream());
                        return filePath;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        builder.setContentText(String.format(getResources().getString(R.string.versionUpdate_downloadProgress), 0));
                        mCurrentProgress = 0;
                        mNotification = builder.build();
                        mNotification.vibrate = new long[]{500, 500};
                        mNotification.defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;
                        manager.notify(0, mNotification);
                    }

                    @Override
                    public void onNext(String filePath) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = UriUtils.getUri(getApplicationContext(), filePath);
                        //设置intent的类型
                        intent.setDataAndType(uri, "application/vnd.android.package-archive");
                        PendingIntent pendingIntent = PendingIntent.getActivity(VersionUpdateService.this, 0, intent, 0);
                        builder.setContentIntent(pendingIntent);
                        builder.setAutoCancel(true);//设置点击后消失
                        builder.setContentText(getResources().getString(R.string.versionUpdate_downloadComplete));
                        builder.setProgress(100, 100, false);
                        manager.notify(0, builder.build());
                        Intent broadcastIntent = new Intent(VersionUpdateReceiver.VERSION_UPDATE_ACTION);
                        broadcastIntent.putExtra(VersionUpdateReceiver.VERSION_UPDATE_ACTION, getResources().getString(R.string.versionUpdate_downloadComplete));
                        sendBroadcast(broadcastIntent);
                        IntentUtils.install(VersionUpdateService.this, filePath);
                        stopSelf();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mCurrentProgress = 0;
                        builder.setContentText(getResources().getString(R.string.versionUpdate_downloadFailure));
                        manager.notify(0, builder.build());
                        Intent broadcastIntent = new Intent(VersionUpdateReceiver.VERSION_UPDATE_ACTION);
                        broadcastIntent.putExtra(VersionUpdateReceiver.VERSION_UPDATE_ACTION, getResources().getString(R.string.versionUpdate_downloadFailure));
                        sendBroadcast(broadcastIntent);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
