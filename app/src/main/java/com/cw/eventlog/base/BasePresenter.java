package com.cw.eventlog.base;

public abstract class BasePresenter<T extends IView> implements IPresenter {

    protected BaseActivity mActivity;
    protected T mView;

    public BasePresenter(BaseActivity activity, T view) {
        this.mActivity = activity;
        this.mView = view;
    }

    @Override
    public void detachView() {
        this.mView = null;
    }

}
