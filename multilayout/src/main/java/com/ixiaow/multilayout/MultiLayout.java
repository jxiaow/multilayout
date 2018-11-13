package com.ixiaow.multilayout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 编写人： xw
 * 创建时间：2018/11/5 11:10
 * 功能描述： 自定义多行tabLayout控件
 */
public class MultiLayout extends LinearLayout implements View.OnClickListener,
        ViewPager.OnAdapterChangeListener, ViewPager.OnPageChangeListener {

    private static final String TAG = "MultiLayout";

    private Context mContext;//上下文

    //当前控件的宽度
    private int mMultiLayoutWidth;
    //tab名称集合
    private List<String> mTabNames;
    //tabText集合
    private List<TextView> mTabTextList;

    //indicator 画笔
    private Paint mIndicatorPaint;
    //indicator的相对于当前选中tabText的距离，由于后面多次参与计算，所以将值保留
    private float mIndicatorMargin;
    //indicator区域设置
    private RectF mIndicatorRectF;
    //indicator圆角弧度
    private float mIndicatorRadius;
    //切换Tab时，indicator x轴移动的距离
    private float mIndicatorTranslateX;
    //切换Tab时，indicator y轴移动的距离
    private float mIndicatorTranslateY;

    //布局测量记录值，只执行一次标志
    private boolean isOnce = false;

    //记录当前的tab
    private TextView mCurrentTabText;
    //与viewPager关联
    private ViewPager mViewPager;

    //tabText的宽度
    private float mTabWidth;
    //tabText的高度
    private float mTabHeight;
    //最小的tabMargin
    private float mTabMinMargin;
    //tabText的文字大小
    private float mTabTextSize;

    //Tab选择监听事件
    private OnTabSelectListener mOnTabSelectListener;

    //ViewPager数据变化监听
    private MultiLayoutDataSetObserver mDataSetObserver;
    private ColorStateList mTabTextColor;


    /**
     * 设置Tab选择监听事件
     *
     * @param onTabSelectListener tab监听事件
     */
    public void setOnTabSelectListener(OnTabSelectListener onTabSelectListener) {
        this.mOnTabSelectListener = onTabSelectListener;
    }

    public MultiLayout(Context context) {
        this(context, null);
    }

    public MultiLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        initAttr(attrs);
    }

    /**
     * 初始化自定义属性
     *
     * @param attrs 属性集合
     */
    private void initAttr(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.MultiLayout);
        mTabTextSize = typedArray.getDimensionPixelSize(R.styleable.MultiLayout_tab_text_size,
                getResources().getDimensionPixelSize(R.dimen.tab_text_size));
        mTabTextSize = mTabTextSize / mContext.getResources().getDisplayMetrics().density;
        mTabWidth = typedArray.getDimensionPixelSize(R.styleable.MultiLayout_tab_text_width,
                getResources().getDimensionPixelSize(R.dimen.tab_text_width));
        mTabHeight = typedArray.getDimensionPixelSize(R.styleable.MultiLayout_tab_text_height,
                getResources().getDimensionPixelSize(R.dimen.tab_text_height));

        int tabTextSelectColor = mContext.getResources().getColor(R.color.tab_text_select_color);
        tabTextSelectColor = typedArray.getColor(R.styleable.MultiLayout_tab_text_select_color, tabTextSelectColor);

        int tabTextUnSelectColor = mContext.getResources().getColor(R.color.tab_text_color);
        tabTextUnSelectColor = typedArray.getColor(R.styleable.MultiLayout_tab_text_unselect_color, tabTextUnSelectColor);

        mTabTextColor = createTabTextColorStateList(tabTextSelectColor, tabTextUnSelectColor);

        int indicatorColor = typedArray.getColor(R.styleable.MultiLayout_tab_indicator_color, Color.RED);
        float indicatorWidth = typedArray.getDimension(R.styleable.MultiLayout_tab_indicator_width,
                mContext.getResources().getDimensionPixelSize(R.dimen.tab_indicator_width));
        float indicatorHeight = typedArray.getDimension(R.styleable.MultiLayout_tab_indicator_height,
                mContext.getResources().getDimensionPixelSize(R.dimen.tab_indicator_height));
        mIndicatorRadius = typedArray.getDimension(R.styleable.MultiLayout_tab_indicator_radius,
                mContext.getResources().getDimensionPixelSize(R.dimen.tab_indicator_radius));


        typedArray.recycle();
        initIndicator(indicatorColor, indicatorWidth, indicatorHeight);
    }

    /**
     * 创建tabText的textColor
     *
     * @param selectColor   选中时的颜色
     * @param unSelectColor 未选中时的颜色
     * @return ColorStateList
     */
    private ColorStateList createTabTextColorStateList(int selectColor, int unSelectColor) {
        int[] colors = new int[]{selectColor, unSelectColor};
        int[][] state = new int[2][];
        state[0] = new int[]{android.R.attr.state_selected};
        state[1] = new int[]{};
        return new ColorStateList(state, colors);
    }

    /**
     * 初始化indicator
     *
     * @param indicatorColor  indicator颜色
     * @param indicatorWidth  indicator 宽度
     * @param indicatorHeight indicator 高度
     */
    private void initIndicator(int indicatorColor, float indicatorWidth,
                               float indicatorHeight) {
        //设置indicator的宽度
        mIndicatorRectF = new RectF(0, 0, indicatorWidth, indicatorHeight);
        //设置画笔
        mIndicatorPaint = new Paint();
        //颜色为红色
        mIndicatorPaint.setColor(indicatorColor);
        //抗锯齿
        mIndicatorPaint.setAntiAlias(true);
        //填充
        mIndicatorPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mIndicatorMargin = (mTabWidth - indicatorWidth) / 2;
    }

    /*
     * 初始化
     */
    private void init(Context context) {
        this.mContext = context;
        //设置当前控件的orientation为垂直
        setOrientation(VERTICAL);
        //在ViewGroup中如果用到了onDraw(),则必须调用此方法
        setWillNotDraw(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        /*
         * 注册一个全局布局监听器，用于监听布局处理完毕后的回调事件来更新tabs
         * 本来更新tabs是可以在onMeasure或者onLayout中执行的，
         * 但是经过测试发现在这两个方法中进行更新tabs，添加进去的tabs不显示，通过测试才利用了
         * {@link getViewTreeObserver()#addOnGlobalLayoutListener}
         */
        ViewTreeObserver viewTreeObserver = this.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN) //最小版本16
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "onGlobalLayout...");
                //移除当前监听器，不然会进行多次监听
                //获取当前控件的大小
                mMultiLayoutWidth = getMeasuredWidth();
                if (mMultiLayoutWidth != 0) {
                    Log.d(TAG, "mMultiLayoutWidth: " + mMultiLayoutWidth);
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    updateTabs();
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout...");
        //当tab全部添加完毕后，会调用此方法
        if (!isOnce && getChildCount() > 0) {
            Log.d(TAG, "layout");
            isOnce = true;//将其置为true,保证此方法只会调用一次
            //遍历所有的tabText，设置右边的margin
            for (TextView textView : mTabTextList) {
                LayoutParams layoutParams = (LayoutParams) textView.getLayoutParams();
                layoutParams.rightMargin = (int) mTabMinMargin;
                textView.setLayoutParams(layoutParams);
            }

            //默认选择第一个
            if (mViewPager != null) {
                mViewPager.setCurrentItem(0, false);
            }
            selectTabText(mTabTextList.get(0), 0);
        }

        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure...");
        if (!isOnce && getChildCount() > 0) {
            Log.d(TAG, "measure");
            //遍历当前控件的所有子控件
            for (int i = 0; i < getChildCount(); i++) {

                LinearLayout linearLayout = (LinearLayout) getChildAt(i);
                if (linearLayout.getChildCount() <= 0) {
                    continue;
                }
                //测量子控件的实际宽度
                int size = MeasureSpec.getSize(widthMeasureSpec);
                //这里的测量会影响到布局，所以在最后需要调用super.onMeasure
                int widthMeasure = MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST);
                linearLayout.measure(widthMeasure, heightMeasureSpec);
                int measuredWidth = linearLayout.getMeasuredWidth();
                //计算每个子控件中每个子控件需要的间隔距离
                float margin = (mMultiLayoutWidth - measuredWidth) * 1.0f / linearLayout.getChildCount();

                /*
                 * 找出最小间隔
                 */
                if (i == 0) {
                    mTabMinMargin = margin;
                    continue;
                }
                if (margin < mTabMinMargin) {
                    mTabMinMargin = margin;
                }
                Log.d(TAG, "linearLayout measureWidth: " + measuredWidth);
            }

            //取得最后一个child
            View child = getChildAt(getChildCount() - 1);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            //为了画indicator，所以最后一个child需要设置距离底部的距离
            lp.bottomMargin = (int) (mIndicatorRectF.bottom - mIndicatorRectF.top);
            child.setLayoutParams(lp);

            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //保存画布
        canvas.save();
        //移动当前画布
        canvas.translate(mIndicatorTranslateX, mIndicatorTranslateY);
        //画圆角矩形
        canvas.drawRoundRect(mIndicatorRectF, mIndicatorRadius,
                mIndicatorRadius, mIndicatorPaint);
        //恢复保存的画布
        canvas.restore();
    }

    /**
     * 设置tab的名称
     *
     * @param tabNames tab名称集合
     */
    public void initTabNames(List<String> tabNames) {
        updateTabNames(tabNames, false);
    }

    /**
     * 关联viewPager
     *
     * @param viewPager ViewPager，调用此方法前最好先给viewPager设置adapter
     */
    public void setupWithViewPager(@NonNull ViewPager viewPager) {
        this.mViewPager = viewPager;
        PagerAdapter adapter = viewPager.getAdapter();
        if (adapter == null) {
            return;
        }
        mDataSetObserver = new MultiLayoutDataSetObserver();
        adapter.registerDataSetObserver(mDataSetObserver);
        setTabNamesByAdapter(adapter);
        //设置adapter变化的监听事件
        this.mViewPager.addOnAdapterChangeListener(this);
        //设置viewPager切换pager的监听事件
        this.mViewPager.addOnPageChangeListener(this);
    }

    /**
     * 设置tabNames通过viewPager的adapter
     *
     * @param adapter {@link ViewPager#getAdapter()}
     */
    private void setTabNamesByAdapter(@NonNull PagerAdapter adapter) {
        Log.d(TAG, "setTabNames");
        /*
         * 获取adapter中的数据个数，然后遍历数据获取pageTitle
         */
        int count = adapter.getCount();
        List<String> tabNames = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            CharSequence title = adapter.getPageTitle(i);
            tabNames.add((String) title);
        }

        updateTabNames(tabNames, true);
    }

    /**
     * 更新tabName
     *
     * @param tabNames tab名称集合
     */
    public void updateTabNames(List<String> tabNames) {
        updateTabNames(tabNames, true);
    }

    /**
     * 更新数据
     *
     * @param tabNames tab名称集合
     * @param isUpdate true 更新tabText， false不更新
     */
    private void updateTabNames(List<String> tabNames, boolean isUpdate) {
        if (!isEmpty(mTabNames)) {
            mTabNames.clear();//如果以前集合有数据则清空
        }
        mTabNames = tabNames;//tabs赋值
        if (isUpdate) {
            updateTabs();
        }
        isOnce = false;//如果新更新了tab，则需要重新测量和布局
        requestLayout();//请求测量和布局
    }

    /**
     * 获取tabText之间的距离
     *
     * @return tabText之间的距离值
     */
    public float getTabMinMargin() {
        return mTabMinMargin;
    }

    /**
     * 根据tabNames中的数据去更新view
     */
    private void updateTabs() {
        Log.d(TAG, "update Tabs...");
        if (mMultiLayoutWidth == 0) {
            Log.d(TAG, "mMultiLayoutWidth is 0");
            return;
        }

        this.removeAllViews();

        if (isEmpty(mTabNames)) {
            Log.d(TAG, "mTabName is empty");
            return;
        }

        //创建一个mTabTextList用于存放创建的TextView
        if (!isEmpty(mTabTextList)) {
            mTabTextList.clear();
        }
        mTabTextList = new ArrayList<>(mTabNames.size());
        //创建一行水平线性布局
        LinearLayout linearLayout = newLinearLayout();
        this.addView(linearLayout);
        int mViewWidth = 0;//tabTextView的宽度
        //临时宽度，主要是用于当前tabText的宽度+mViewWidth的宽度之后的临时值和控件宽度mTopicLayoutWidth作比较
        int tmpViewWidth;

        //遍历mTabNames，创建布局和TextView,用于动态计算控件的位置
        for (String tabName : mTabNames) {
            TextView tabText = newTabText(tabName);
            mTabTextList.add(tabText);
            //测量控件
            measureTabText(tabText);

            //此处值只能用getMeasuredWidth
            int width = tabText.getMeasuredWidth();
            tmpViewWidth = mViewWidth + width;
            /*
             * 往linearLayout中添加tabText之前,
             * 需要判断tmpViewWidth（当前linearLayout中添加的控件宽度之和 + tabText的宽度）
             * 和 mTopicLayoutWidth之间的关系，如果大于，则不能再添加，否则可以添加
             */
            if (tmpViewWidth <= mMultiLayoutWidth) {
                mViewWidth += width;
                linearLayout.addView(tabText);
                continue;
            }

            /*
             * linearLayout不能再添加tabText时，需要将linearLayout加入到当前控件中，然后重新创建linearLayout
             */
            mViewWidth = width;
            linearLayout = newLinearLayout();
            this.addView(linearLayout);
            linearLayout.addView(tabText);
        }
        Log.d(TAG, "childCount: " + getChildCount());
    }

    /**
     * 创建一个linearLayout布局
     *
     * @return 返回一个linearLayout
     */
    @NonNull
    private LinearLayout newLinearLayout() {
        LinearLayout linearLayout = new LinearLayout(mContext);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(lp);
        linearLayout.setOrientation(HORIZONTAL);
        return linearLayout;
    }

    /**
     * 创建一个tabTextView
     *
     * @param tabName tabText名称
     * @return tabText
     */
    @NonNull
    private TextView newTabText(String tabName) {
        TextView tabText = new TextView(mContext);
        tabText.setTextSize(mTabTextSize);
        tabText.setTextColor(mTabTextColor);
        tabText.setGravity(Gravity.CENTER);
        tabText.setMaxLines(1);
        tabText.setEllipsize(TextUtils.TruncateAt.END);
        tabText.setText(tabName);
        LayoutParams layoutParams = new LayoutParams((int) mTabWidth, (int) mTabHeight);
        tabText.setLayoutParams(layoutParams);
        tabText.setOnClickListener(this);
        return tabText;
    }

    /**
     * 更新indicator的位置
     *
     * @param textView 当前点击的tabText
     * @param position tabText的位置，此位置是每一行对应的位置
     */
    private void updateIndicator(TextView textView, int position) {
        Log.d(TAG, "updateIndicator");
        int translateX;
        int translateY;
        if (position == 0) {
            translateX = (int) mIndicatorMargin;
        } else {
            translateX = (int) ((mTabWidth + mTabMinMargin) * position + mIndicatorMargin);
        }
        translateY = (int) ((indexOfChild((View) textView.getParent()) + 1) * mTabHeight);
        mIndicatorTranslateX = translateX;
        mIndicatorTranslateY = translateY;
        invalidate();
    }

    /*
     * 测量view控件的宽高
     */
    private void measureTabText(TextView view) {
        int w = MeasureSpec.makeMeasureSpec((int) mTabWidth, MeasureSpec.EXACTLY);
        int h = MeasureSpec.makeMeasureSpec((int) mTabHeight, MeasureSpec.EXACTLY);
        view.measure(w, h);
    }

    @Override
    public void onClick(View v) {
        if (mCurrentTabText == v) {
            return;
        }

        if (!(v instanceof TextView)) {
            return;
        }

        TextView textView = (TextView) v;
        //tabText点击时，如果ViewPager不为空则需要与其联动
        int index = mTabTextList.indexOf(textView);
        //处理状态选择事件
        selectTabText(textView, index);
        if (mViewPager != null) {
            mViewPager.setCurrentItem(index, false);
        }
    }

    /**
     * 选择TabText
     *
     * @param textView tabText
     * @param index    当前textView在集合中的下标
     */
    private void selectTabText(@NonNull TextView textView, int index) {
        //将其置为选择状态
        textView.setSelected(true);
        if (mCurrentTabText != null) {
            mCurrentTabText.setSelected(false);//取消前一个的选择状态
        }
        mCurrentTabText = textView;

        LinearLayout parent = (LinearLayout) textView.getParent();
        //找出tabText的位置
        int position = parent.indexOfChild(textView);
        //更新indicator
        updateIndicator(textView, position);
        if (mOnTabSelectListener != null) {//监听事件
            mOnTabSelectListener.select(textView, position, index);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {
        Log.d(TAG, "onPageScrolled");
    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected postion: " + position);
        //根据viewPager页面切换时回调次方法，所以可以通过position获得tabText
        if (isEmpty(mTabTextList)) {
            return;
        }
        TextView textView = mTabTextList.get(position);
        if (mCurrentTabText == textView) {
            return;
        }
        //选择当前tabText
        selectTabText(textView, position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.d(TAG, "onPageScrollStateChanged:  " + state);
    }


    @Override
    public void onAdapterChanged(@NonNull ViewPager viewPager,
                                 @Nullable PagerAdapter oldAdapter,
                                 @Nullable PagerAdapter newAdapter) {
        Log.d(TAG, "onAdapterChange");
        if (oldAdapter != null) {
            //pager发生改变时需要移除当前观察者
            oldAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        if (newAdapter != null) {
            //重新设置adapter，获取数据
            newAdapter.registerDataSetObserver(mDataSetObserver);
            setTabNamesByAdapter(newAdapter);
        }
    }

    /**
     * 数据监听
     */
    private class MultiLayoutDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {//adapter数据更新后会调用此方法
            super.onChanged();
            Log.d(TAG, "update data");
            notifyDataSetChange();
        }
    }

    /**
     * 通知viewPager中的数据已更新
     */
    private void notifyDataSetChange() {
        PagerAdapter adapter = mViewPager.getAdapter();
        if (adapter != null) {
            setTabNamesByAdapter(adapter);
        }
    }

    /**
     * 判断集合是否为空
     *
     * @param collection 集合
     * @return true 为空，false不为空
     */
    public boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * tab选择监听事件
     */
    public interface OnTabSelectListener {
        /**
         * tabText选择事件
         *
         * @param tabText  当前选中的tabText
         * @param position 当前选择tabText的相对位置
         * @param index    当前选择的集合中的绝对位置
         */
        void select(TextView tabText, int position, int index);
    }
}