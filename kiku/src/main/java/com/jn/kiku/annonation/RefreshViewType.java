package com.jn.kiku.annonation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.jn.kiku.annonation.RefreshViewType.ALL;
import static com.jn.kiku.annonation.RefreshViewType.NONE;
import static com.jn.kiku.annonation.RefreshViewType.ONLY_LOADMORE;
import static com.jn.kiku.annonation.RefreshViewType.ONLY_REFRESH;

/**
 * @version V2.0
 * @ClassName: ${CLASS_NAME}
 * @Description: (RecyclerView类型)
 * @create by: chenwei
 * @date 2018/5/15 16:07
 */
@IntDef({NONE, ONLY_REFRESH, ONLY_LOADMORE, ALL})
@Retention(RetentionPolicy.SOURCE)
public @interface RefreshViewType {
    int ALL = 0;//刷新和加载更多都有
    int NONE = 1;//纯列表不带刷新和加载更多
    int ONLY_REFRESH = 2;//只有刷新
    int ONLY_LOADMORE = 3;//只有加载更多
}
