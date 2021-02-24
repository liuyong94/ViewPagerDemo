package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.myapplication.lazy.LazyFragment;

import java.util.Random;

/**
 * Created by liuyong on 2021/2/22
 */
public class SubFragment extends LazyFragment {

    private static final String TAG = "SubFragment";
    private TextView mTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()...");
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        Log.i(TAG, "onCreateViewLazy()...");
        setContentView(R.layout.fragment_sub);
        mTextView = (TextView) findViewById(R.id.iv_finish);
        mTextView.setText("fragment" + new Random().nextInt(10));
    }

    public String getText() {
        return mTextView.getText().toString();
    }
}
