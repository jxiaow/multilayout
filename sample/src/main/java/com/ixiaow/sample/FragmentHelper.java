package com.ixiaow.sample;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.List;

/**
 * 编写人：xw
 * 创建时间：2018/9/28 9:08
 * 功能描述：fragment关注类
 */
public class FragmentHelper {

    private int mContainerId; //需要用fragment替换的view id
    private FragmentManager mFragmentManager; //fragmentManager v4

    /**
     * 构造方法
     *
     * @param containerId     view id
     * @param fragmentManager fragmentManager
     */
    public FragmentHelper(@IdRes int containerId, @NonNull FragmentManager fragmentManager) {
        mContainerId = containerId;
        mFragmentManager = fragmentManager;
    }

    /**
     * 添加fragment
     *
     * @param fragment 需要添加的fragment
     */
    public void add(Fragment fragment) {
        add(fragment, true);
    }

    /**
     * 添加fragment, 通过isShow控制器显示与否， 异步的
     *
     * @param fragment 需要添加的fragment
     * @param isShow   true 显示，false 不显示
     */
    public void add(Fragment fragment, boolean isShow) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(mContainerId, fragment);
        if (!isShow) {
            transaction.hide(fragment);
        }
        transaction.commit();
    }

    /**
     * 添加fragment, 通过isShow控制器显示与否, 这个会立即添加，同步的
     *
     * @param fragment 需要添加的fragment
     * @param isShow   true 显示，false 不显示
     */
    public void addNow(Fragment fragment, boolean isShow) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(mContainerId, fragment);
        if (!isShow) {
            transaction.hide(fragment);
        }
        transaction.commitNow();
    }

    /**
     * 切换到目标fragment
     *
     * @param fragment 目标fragment
     */
    public void switchFragment(Fragment fragment) {

        //开启事务
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        //获取当前fragmentManager中的所有fragment
        List<Fragment> fragments = mFragmentManager.getFragments();
        for (Fragment f : fragments) { //遍历所有fragment设置隐藏
            transaction.hide(f);
        }
        //判断当前目标fragment是否存在，并且没有被加入
        if (!fragments.contains(fragment) && !fragment.isAdded()) {
            transaction.add(mContainerId, fragment);
        } else {
            transaction.show(fragment);
        }
        //提交事务
        transaction.commit();

    }

    /**
     * 获取所有绑定的fragment
     *
     * @return fragment 集合
     */
    public List<Fragment> getFragmentList() {
        return mFragmentManager.getFragments();

    }
}
