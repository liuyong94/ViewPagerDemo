package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * Created by liuyong on 2021/2/22
 */
public class ViewPagerDemoActivity extends FragmentActivity {

    private static final String TAG = "ViewPagerDemoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()...window = " + getWindow());
        if (needShowGuide()) {
            showGuide();
        } else {
            initFragment();
        }
    }

    private boolean needShowGuide() {
        return true;
    }

    private void showGuide() {
        Intent intent = new Intent(this, TestActivity.class);
        startActivityForResult(intent, 1);
        overridePendingTransition(0, 0);
    }

    private void initFragment() {
        Fragment fragment = Fragment.instantiate(this, MainFragment.class.getName());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, fragment)
                .commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult()... initFragment, getWindow = " + getWindow());
            initFragment();
        }
    }
}
