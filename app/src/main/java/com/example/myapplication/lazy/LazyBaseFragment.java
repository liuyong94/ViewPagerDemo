package com.example.myapplication.lazy;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LazyBaseFragment extends Fragment {

    private static final String TAG = "LazyBaseFragment";
    protected LayoutInflater mInflater;
    private View mContentView;
    protected Context mContext;
    private ViewGroup mRootContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
    }

    //子类通过重写onCreateView，调用setOnContentView进行布局设置，否则contentView==null，返回null
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mInflater = inflater;
        this.mRootContainer = container;
        onCreateView(savedInstanceState);
        if (mContentView == null) {
        	return super.onCreateView(inflater, container, savedInstanceState);
        }
        return mContentView;
    }

    protected void onCreateView(Bundle savedInstanceState) {

    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        //解决三星FragmentManagerImpl.saveFragmentBasicState 空指针异常
        outState.putString("FragmentManagerImpl_Null_Bug", "FragmentManagerImpl_NULL_Bug");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContentView = null;
        mRootContainer = null;
        mInflater = null;
    }

    public Context getApplicationContext() {
        return mContext;
    }

    public void setContentView(int layoutResID) {
        setContentView((ViewGroup) mInflater.inflate(layoutResID, mRootContainer, false));
    }

    public void setContentView(View view) {
        mContentView = view;
    }

    public View getContentView() {
        return mContentView;
    }

    public View findViewById(int id) {
        if (mContentView != null) {
            return mContentView.findViewById(id);
        }

        return null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    @TargetApi(15)
    public boolean getUserVisibleHint(){
    	try {
    		return super.getUserVisibleHint();
    	} catch(Exception e) {
            Log.e(TAG, "getUserVisibleHint()... e =" + e.toString());
    	}
    	return true;
    }
}