package com.example.myapplication.lazy;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;


/**
 * <h1>懒加载Fragment</h1> 只有创建并显示的时候才会调用onCreateViewLazy方法<br>
 * <br>
 * <p/>
 * 懒加载的原理onCreateView的时候Fragment有可能没有显示出来。<br>
 * 但是调用到setUserVisibleHint(boolean isVisibleToUser),isVisibleToUser =
 * true的时候就说明有显示出来<br>
 * 但是要考虑onCreateView和setUserVisibleHint的先后问题所以才有了下面的代码
 * <p/>
 * 注意：<br>
 * 《1》原先的Fragment的回调方法名字后面要加个Lazy，比如Fragment的onCreateView方法， 就写成onCreateViewLazy <br>
 * 《2》使用该LazyFragment会导致多一层布局深度
 *
 * @author LuckyJayce
 */
public class LazyFragment extends LazyBaseFragment {
	
	private static final String TAG = "LazyFragment";
    public static final String INTENT_BOOLEAN_LAZYLOAD = "intent_boolean_lazyLoad";
    private Bundle mSavedInstanceState;
    private FrameLayout mLayout;

    private boolean mIsInit = false;//真正要显示的View是否已经被初始化（正常加载）
    private boolean mIsLazyLoad = true;
    private boolean mIsStart = false;//是否处于可见状态，in the screen

    @Override
    protected final void onCreateView(Bundle savedInstanceState) {
//        Log.d(TAG, "onCreateView() : " + "getUserVisibleHint():" + super.getUserVisibleHint());
        super.onCreateView(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mIsLazyLoad = bundle.getBoolean(INTENT_BOOLEAN_LAZYLOAD, mIsLazyLoad);
        }
        //判断是否懒加载
        if (mIsLazyLoad) {
            //一旦isVisibleToUser==true即可对真正需要的显示内容进行加载
            if (super.getUserVisibleHint() && !mIsInit) {
                this.mSavedInstanceState = savedInstanceState;
                onCreateViewLazy(savedInstanceState);
                mIsInit = true;
            } else {
                //进行懒加载
                mLayout = new FrameLayout(getApplicationContext());
                super.setContentView(mLayout);
            }
        } else {
            //不需要懒加载，开门江山，调用onCreateViewLazy正常加载显示内容即可
            onCreateViewLazy(savedInstanceState);
            mIsInit = true;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint() called with: " + "isVisibleToUser = [" + isVisibleToUser + "]");
//         Log.d(TAG, "setUserVisibleHint() called with: " + "isVisibleToUser = [" + isVisibleToUser + "]");

        //一旦isVisibleToUser==true即可进行对真正需要的显示内容的加载

        //可见，但还没被初始化
        if (isVisibleToUser && !mIsInit && getContentView() != null) {
            onCreateViewLazy(mSavedInstanceState);
            mIsInit = true;
            onResumeLazy();
        }
        //已经被初始化（正常加载）过了
        if (mIsInit && getContentView() != null) {
            if (isVisibleToUser) {
                mIsStart = true;
                onFragmentStartLazy();
            } else {
                mIsStart = false;
                onFragmentStopLazy();
            }
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        //判断若isLazyLoad==true,移除所有lazy view，加载真正要显示的view
        if (mIsLazyLoad && getContentView() != null && getContentView().getParent() != null) {
            mLayout.removeAllViews();
            View view = mInflater.inflate(layoutResID, mLayout, false);
            mLayout.addView(view);
        }
        //否则，开门见山，直接加载
        else {
            super.setContentView(layoutResID);
        }
    }

    @Override
    public void setContentView(View view) {
        //判断若isLazyLoad==true,移除所有lazy view，加载真正要显示的view
        if (mIsLazyLoad && getContentView() != null && getContentView().getParent() != null) {
            mLayout.removeAllViews();
            mLayout.addView(view);
        }
        //否则，开门见山，直接加载
        else {
            super.setContentView(view);
        }
    }

    @Deprecated
    @Override
    public final void onStart() {
//        Log.d(TAG, "onStart() : " + "getUserVisibleHint():" + super.getUserVisibleHint());
        super.onStart();
        if (mIsInit && !mIsStart && super.getUserVisibleHint()) {
            mIsStart = true;
            onFragmentStartLazy();
        }
    }

    @Deprecated
    @Override
    public final void onStop() {
        super.onStop();
//        Log.d(TAG, "onStop() called: " + "getUserVisibleHint():" + super.getUserVisibleHint());
        if (mIsInit && mIsStart && super.getUserVisibleHint()) {
            mIsStart = false;
            onFragmentStopLazy();
        }
    }

    //当Fragment被滑到可见的位置时，调用
    protected void onFragmentStartLazy() {
//        Log.d(TAG, "onFragmentStartLazy() called with: " + "");
    }

    //当Fragment被滑到不可见的位置，offScreen时，调用
    protected void onFragmentStopLazy() {
//        Log.d(TAG, "onFragmentStopLazy() called with: " + "");
    }

    protected void onCreateViewLazy(Bundle savedInstanceState) {
//        Log.d(TAG, "onCreateViewLazy() called with: " + "mSavedInstanceState = [" + savedInstanceState + "]");
    }

    protected void onResumeLazy() {
        Log.d(TAG, "onResumeLazy() called with: " + "");
    }

    protected void onPauseLazy() {
        Log.d(TAG, "onPauseLazy() called with: " + "");
    }

    protected void onDestroyViewLazy() {

    }

    @Override
    @Deprecated
    public final void onResume() {
//        Log.d(TAG, "onResume() : " + "getUserVisibleHint():" + super.getUserVisibleHint());
        super.onResume();
        if (mIsInit) {
            onResumeLazy();
        }
    }

    @Override
    @Deprecated
    public final void onPause() {
//        Log.d(TAG, "onPause() : " + "getUserVisibleHint():" + super.getUserVisibleHint());
        super.onPause();
        if (mIsInit) {
            onPauseLazy();
        }
    }

    @Override
    @Deprecated
    //kotlin出bug.这个函数不能用final
    public void onDestroyView() {
//        Log.d(TAG, "onDestroyView() : " + "getUserVisibleHint():" + super.getUserVisibleHint());
        super.onDestroyView();
        if (mIsInit) {
            onDestroyViewLazy();
        }
        mIsInit = false;
    }

    public boolean isInit() {
        return mIsInit;
    }

    public void setInit(boolean mIsInit) {
        this.mIsInit = mIsInit;
    }
}