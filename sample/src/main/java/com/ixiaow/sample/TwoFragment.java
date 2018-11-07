package com.ixiaow.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ixiaow.multilayout.MultiLayout;


/**
 * 编写人： xw
 * 创建时间：2018/11/2 14:20
 * 功能描述：首页分类
 */
public class TwoFragment extends Fragment {


    private MultiLayout mTopicLayout;
    private static final String[] TAB_NAMES = {
            "全部", "赛事", "原创", "少年", "少女", "日漫",
            "杂志", "热血", "搞笑", "治愈", "惊秫", "古风",
            "全部全部", "赛事2", "原创1", "少年2", "少女1", "日漫2",
            "杂志eeee3", "热血2", "搞笑1", "治愈2", "惊秫2", "古风2",
            "全部1", "赛事1", "原创2", "少年1", "少女", "日漫2",
            "杂志1", "热血1", "搞笑2", "治愈1", "惊秫1", "古风1"
    };

    private ViewPager mViewPager;

    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_two, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTopicLayout = view.findViewById(R.id.topic_layout);
//        mTopicLayout.setTabNames(Arrays.asList(TAB_NAMES));
        mViewPager = view.findViewById(R.id.viewPager);

        mViewPager.setAdapter(new TopicAdapter());
        mTopicLayout.setupWithViewPager(mViewPager);

    }

    private static class TopicAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return TAB_NAMES.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return TAB_NAMES[position];
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            TextView textView = new TextView(container.getContext());
            textView.setTextSize(16);
            textView.setTextColor(Color.RED);
            textView.setText(TAB_NAMES[position]);
            textView.setGravity(Gravity.CENTER);
            container.addView(textView);
            return textView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
