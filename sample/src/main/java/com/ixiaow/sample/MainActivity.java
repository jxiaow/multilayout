package com.ixiaow.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FragmentHelper mFragmentHelper;
    private OneFragment mOneFragment;
    private TwoFragment mTwoFragment;
    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentHelper = new FragmentHelper(R.id.container, getSupportFragmentManager());
        mOneFragment = new OneFragment();
        mTwoFragment = new TwoFragment();


        mFragmentHelper.switchFragment(mOneFragment);
        mCurrentFragment = mOneFragment;

        this.findViewById(R.id.one).setOnClickListener(this);
        this.findViewById(R.id.two).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.one:
                switchFragment(mOneFragment);
                break;
            case R.id.two:
                switchFragment(mTwoFragment);
                break;
        }
    }

    private void switchFragment(Fragment fragment) {
        if (mCurrentFragment != null && mCurrentFragment == fragment) {
            return;
        }
        mFragmentHelper.switchFragment(fragment);
        mCurrentFragment = fragment;
    }
}
