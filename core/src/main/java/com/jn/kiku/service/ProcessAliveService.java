package com.jn.kiku.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

/**
 * Author：Stevie.Chen Time：2019/9/29
 * Class Comment：进程保活服务, 设置服务为前台服务
 */
public class ProcessAliveService extends Service {

    private static final int GRAY_SERVICE_ID = 1001;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * START_STICKY：如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。
     * 随后系统会尝试重新创建service，由于服务状态为开始状态，所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。
     * 如果在此期间没有任何启动命令被传递到service，那么参数Intent将为null。
     * <p>
     * START_NOT_STICKY：“非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统不会自动重启该服务。
     * <p>
     * START_REDELIVER_INTENT：重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，
     * 系统会自动重启该服务，并将Intent的值传入。
     * <p>
     * START_STICKY_COMPATIBILITY：START_STICKY的兼容版本，但不保证服务被kill后一定能重启。
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setForegroundService();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 设置此服务为前台服务，提高优先级，减少被系统杀死的概率
     */
    protected void setForegroundService() {
        Intent innerIntent = new Intent(this, GrayInnerService.class);
        startService(innerIntent);
        startForeground(GRAY_SERVICE_ID, new Notification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 灰色进程保活服务
     * <p>
     * 利用系统的漏洞，不同的服务用同一个Notification时，只要其中一个服务被停止，则Notification消失，
     * 另一个服务则设置为前台服务并且没有Notification
     * </p>
     */
    public static class GrayInnerService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
    }
}
