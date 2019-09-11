package com.jn.example.request;

import com.jn.example.entiy.XaResult;
import com.jn.kiku.mvp.IBPresenter;
import com.jn.kiku.mvp.RxBasePresenterObserver;
import com.jn.kiku.net.retrofit.exception.OkHttpErrorHelper;
import com.jn.kiku.net.retrofit.exception.OkHttpException;

/**
 * Author：Stevie.Chen Time：2019/7/9
 * Class Comment：返回参数
 */
public abstract class RxObserver<T> extends RxBasePresenterObserver<XaResult<T>, T> {

    public RxObserver(IBPresenter ibPresenter) {
        this(ibPresenter, ALL);
    }

    public RxObserver(IBPresenter mIBPresenter, int errorType) {
        super(mIBPresenter, errorType);
    }

    @Override
    public void onNext(XaResult<T> tXaResult) {
        if (!tXaResult.getCode().equals(ApiStatus.CODE_200)) {
            if (tXaResult.getCode().equals(ApiStatus.CODE_10002)) {
                OkHttpException okHttpException = new OkHttpException(tXaResult.getCode(), tXaResult.getMsg());
                onFailure(okHttpException, "");
            } else {
                OkHttpException okHttpException = new OkHttpException(tXaResult.getCode(), tXaResult.getMsg());
                String errorMsg = "";
                if (mErrorType == ALL)
                    errorMsg = OkHttpErrorHelper.getMessage(okHttpException);
                onFailure(okHttpException, errorMsg);
            }
        } else {
            try {
                onResponse(tXaResult);
                onSuccess(tXaResult.getData());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
