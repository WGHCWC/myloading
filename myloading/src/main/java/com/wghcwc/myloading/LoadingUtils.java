package com.wghcwc.myloading;

import android.app.Activity;


import com.wghcwc.activitychangeprovider.ActivityChangeListener;
import com.wghcwc.activitychangeprovider.ActivityLifecycle;
import com.wghcwc.activitychangeprovider.ActivityStack;
import com.wghcwc.activitychangeprovider.ActivityState;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * @author wghcwc
 * @date 19-11-26
 */
public class LoadingUtils {
    private static volatile LoadingUtils loadingUtils = new LoadingUtils();
    private static Map<Activity, LoadWrapper> wrapperMap;

    private LoadingUtils() {
        wrapperMap = new WeakHashMap<>();
    }



    public static SVProgressHUD get() {

        LoadWrapper wrapper = getCurrentWrapper();
        if (wrapper != null) {
            return wrapper.getSvProgressHUD();
        }
        return null;
    }

    public static void show() {
        LoadWrapper wrapper = getCurrentWrapper();
        if (wrapper != null) {
            wrapper.show();
        }
    }

    public static void showWith() {
        showWith("加载中..");

    }

    public static void showWith(String info) {
        LoadWrapper wrapper = getCurrentWrapper();
        if (wrapper != null) {
            wrapper.getSvProgressHUD().showWithStatus(info);
        }
    }

    public static void dismiss() {
        LoadWrapper wrapper = getDismissWrapper();

        if (wrapper != null) {
            wrapper.dismiss();
        }
    }

    public static void delayShow() {
        LoadWrapper wrapper = getCurrentWrapper();
        if (wrapper != null) {
            wrapper.delayShow();
        }
    }

    public static void delayShow(int times) {
        LoadWrapper wrapper = getCurrentWrapper();
        if (wrapper != null) {
            wrapper.delayShow(times);
        }
    }

    public static void delayDismiss() {
        LoadWrapper wrapper = getDismissWrapper();
        if (wrapper != null) {
            wrapper.delayDismiss();
        }
    }

    private static LoadWrapper getCurrentWrapper() {
        Activity activity = ActivityStack.currentActivity();
        if (activity == null) {
            return null;
        }
        return loadingUtils.getLoadingWrapper(activity);
    }

    private static LoadWrapper getDismissWrapper() {
        Activity activity = ActivityStack.currentActivity();
        if (activity == null) {
            return null;
        }
        LoadWrapper wrapper = wrapperMap.get(activity);
        if (wrapper == null) {
            return null;
        }
        return loadingUtils.getLoadingWrapper(activity);
    }

    private LoadWrapper getLoadingWrapper(Activity activity) {
        LoadWrapper wrapper = wrapperMap.get(activity);
        if (wrapper == null) {
            wrapper = new LoadWrapper(activity);
            wrapperMap.put(activity, wrapper);
        }
        return wrapper;
    }


    public class LoadWrapper implements ActivityChangeListener {
        private SVProgressHUD svProgressHUD;
        private Disposable disposable;
        private int count;

        private LoadWrapper(Activity activity) {
            ActivityLifecycle.getInstance().add(activity, this);
            svProgressHUD = new SVProgressHUD(activity);
        }

        private SVProgressHUD getSvProgressHUD() {
            return svProgressHUD;
        }

        private void show() {
            svProgressHUD.show();
        }

        private void dismiss() {
            if (svProgressHUD.isShowing())
                svProgressHUD.dismiss();
        }

        /**
         * 0.5秒之内delayDismiss()调用则不显示加载动画,
         */
        private void delayShow() {
            if (svProgressHUD.isShowing()) {
                return;
            }
            Observable.timer(500, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new MyObserver<Long>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            disposable = d;
                        }

                        @Override
                        protected void onSuccess(Long aLong) {
                            svProgressHUD.showWithStatus("加载中...");
                        }


                    });
        }

        /**
         * 多个请求
         *
         * @param times 请求个数
         */
        private void delayShow(int times) {
            count = times;
            delayShow();
        }

        /**
         * 0.5秒之内未显示则取消,已显示则延迟0.5秒消失
         */
        private void delayDismiss() {
            if (--count > 0) {
                return;
            }
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
            if (svProgressHUD.isShowing()) {
                Observable.timer(500, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new MyObserver<Long>() {
                            @Override
                            protected void onSuccess(Long aLong) {
                                svProgressHUD.dismiss();
                            }

                            @Override
                            public void onError(Throwable e) {
                                svProgressHUD.dismiss();
                            }
                        });
            }
        }

        @Override
        public void onActivityDestroy(Activity activity) {
            wrapperMap.remove(activity);
        }


        @Override
        public void onActivitySateChange(Activity activity, ActivityState state) {

        }
    }

}
