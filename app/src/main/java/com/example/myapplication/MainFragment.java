package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyong on 2021/2/22
 */
public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private View rootView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach()...");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_view_pager, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated()...windowToken = " + view.getWindowToken());
        ViewPager viewPager = rootView.findViewById(R.id.viewPager);
        List<Fragment> fragments = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            fragments.add(Fragment.instantiate(getContext(), SubFragment.class.getName()));
        }
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager(), fragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new PageChangeListener());
        viewPager.setOffscreenPageLimit(4);
        viewPager.setCurrentItem(1);
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            String text = "";
            List<Fragment> subFragments = getChildFragmentManager().getFragments();
            for (int i = 0; i < subFragments.size(); i++) {
                Fragment fragment = subFragments.get(i);
                if (fragment instanceof SubFragment) {
                    text = ((SubFragment) fragment).getText();
                }
            }
            Log.i(TAG, "onPageSelected()...position = " + position + ", text = " + text);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private static class PagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragments;

        public PagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int i) {
            Log.i(TAG, "getItem()...position = " + i);
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
