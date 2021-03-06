package com.jn.kiku.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.BaseViewHolder;
import com.jn.kiku.common.api.IImageAdapterView;
import com.jn.kiku.utils.ScreenUtils;
import com.jn.kiku.utils.manager.BaseManager;
import com.jn.kiku.utils.manager.GlideManage;

/**
 * Author：Stevie.Chen Time：2019/8/20
 * Class Comment：BaseAdapterViewHolder
 */
public class BaseAdapterViewHolder extends BaseViewHolder implements IImageAdapterView {

    private Context mContext;
    private Object mImageContext;//用于显示图片的context对象
    private BaseManager mBaseManager;

    public BaseAdapterViewHolder(View view) {
        super(view);
    }

    public BaseAdapterViewHolder setVisibility(int viewId, int visibility) {
        View view = getView(viewId);
        view.setVisibility(visibility);
        return this;
    }

    public BaseAdapterViewHolder setSelected(int viewId, boolean selected) {
        View view = getView(viewId);
        view.setSelected(selected);
        return this;
    }

    @Override
    public BaseViewHolder setText(int viewId, CharSequence value) {
        return super.setText(viewId, null == value ? "" : value);
    }

    @Override
    public void bindImageContext(Activity activity) {
        mContext = activity.getApplicationContext();
        mImageContext = activity;
        mBaseManager = new BaseManager(activity);
    }

    @Override
    public void bindImageContext(Fragment fragment) {
        mContext = fragment.getContext();
        mImageContext = fragment;
        mBaseManager = new BaseManager(fragment, null);
    }

    @Override
    public Object getImageContext() {
        return mImageContext;
    }

    @Override
    public BaseAdapterViewHolder displayImage(int viewId, Object url) {
        BaseManager manager = getBaseManager();
        if (manager != null) {
            ImageView view = getView(viewId);
            manager.displayImage(view, url);
        }
        return this;
    }

    @Override
    public BaseAdapterViewHolder displayRoundImage(int viewId, Object url, int radius) {
        BaseManager manager = getBaseManager();
        if (manager != null) {
            ImageView view = getView(viewId);
            manager.displayRoundImage(view, url, radius);
        }
        return this;
    }

    @Override
    public BaseAdapterViewHolder displayCircleImage(int viewId, Object url) {
        BaseManager manager = getBaseManager();
        if (manager != null) {
            ImageView view = getView(viewId);
            manager.displayCircleImage(view, url);
        }
        return this;
    }

    @Override
    public BaseAdapterViewHolder displayAvatar(int viewId, Object url) {
        BaseManager manager = getBaseManager();
        if (manager != null) {
            ImageView view = getView(viewId);
            manager.displayAvatar(view, url);
        }
        return this;
    }

    @Override
    public BaseAdapterViewHolder displayCircleAvatar(int viewId, Object url) {
        BaseManager manager = getBaseManager();
        if (manager != null) {
            ImageView view = getView(viewId);
            manager.displayCircleAvatar(view, url);
        }
        return this;
    }

    @Override
    public BaseAdapterViewHolder displayWrapImage(int viewId, Object url) {
        displayWrapImage(viewId, url, 0);
        return this;
    }

    @Override
    public BaseAdapterViewHolder displayWrapImage(int viewId, Object url, int totalSpace) {
        displayWrapImage(viewId, url, ScreenUtils.getScreenWidth(mContext) / 2, totalSpace);
        return this;
    }

    @Override
    public BaseAdapterViewHolder displayWrapImage(int viewId, Object url, int width, int totalSpace) {
        ImageView view = getView(viewId);
        GlideManage.displayImage(getImageContext(), url, new DrawableImageViewTarget(view) {

            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                super.onResourceReady(resource, transition);
                int imageWidth = resource.getIntrinsicWidth();
                int imageHeight = resource.getIntrinsicHeight();
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                lp.width = width - totalSpace;
                lp.height = width * imageHeight / imageWidth;
                view.setLayoutParams(lp);
            }

        });
        return this;
    }

    @Override
    public BaseManager getBaseManager() {
        return mBaseManager;
    }
}
