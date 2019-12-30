package com.wghcwc.myloading;



import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author wghcwc
 * @date 19-12-9
 * 简写Observer,只关心成功
 */
public abstract class MyObserver<T> implements Observer<T> {

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {
        onSuccess(t);
    }

    @Override
    public void onError(Throwable e) {
        onComplete();
    }

    @Override
    public void onComplete() {

    }

    protected abstract void onSuccess(T t);


}
