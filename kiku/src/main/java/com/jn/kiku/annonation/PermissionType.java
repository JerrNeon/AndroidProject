package com.jn.kiku.annonation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.jn.kiku.annonation.PermissionType.CALL_PHONE;
import static com.jn.kiku.annonation.PermissionType.CAMERA;
import static com.jn.kiku.annonation.PermissionType.CAMERA_WRITE_EXTERNAL_STORAGE;
import static com.jn.kiku.annonation.PermissionType.LOCATION;
import static com.jn.kiku.annonation.PermissionType.WRITE_EXTERNAL_STORAGE;

/**
 * @version V2.0
 * @ClassName: ${CLASS_NAME}
 * @Description: (权限类型)
 * @create by: chenwei
 * @date 2018/5/17 17:41
 */
@IntDef({CAMERA, WRITE_EXTERNAL_STORAGE, CALL_PHONE, LOCATION, CAMERA_WRITE_EXTERNAL_STORAGE})
@Retention(RetentionPolicy.SOURCE)
public @interface PermissionType {
    int CAMERA = 1;//相机
    int WRITE_EXTERNAL_STORAGE = 2;//存储空间(打开相册)
    int CALL_PHONE = 3;//电话
    int LOCATION = 4;//定位
    int CAMERA_WRITE_EXTERNAL_STORAGE = 5;//相机&存储空间(打开相册)
}
