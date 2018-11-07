package com.ixiaow.multilayout;

import android.content.Context;
import android.content.res.ColorStateList;
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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 编写人： xw
 * 创建时间：2018/11/5 11:10
 * 功能描述： 自定义多行tabLayout控件
 */
public class MultiLayout extends LinearLayout implements View.OnClickListener,
        ViewPager.OnAdapterChangeListener, ViewPager.OnPageChangeListener {
    private static final String TAG = "TopicLayout";

    private int mTopicLayoutWidth; //当前控件的宽度
    private List<String> mTabNames; //tab名称集合
    private Context mContext;//上下文
    //Tab选择监听事件
    private OnTabSelectListener mOnTabSelectListener;
    //Indicator 画笔
    private Paint mPaint;
    //Indicator区域设置
    private RectF rectF;
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
    //tabText集合
    private List<TextView> mTabTextList;
    //tabText的宽度
    private int mTabWidth;
    //tabText的高度
    private int mTabHeight;
    //最小的tabMargin
    private float mTabMinMargin = 0;

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
    }

    /*
     * 初始化
     */
    private void init(Context context) {
        mContext = context;
        //设置当前控件的orientation为垂直
        setOrientation(VERTICAL);

        mTabWidth = mContext.getResources().getDimensionPixelSize(R.dimen.topic_tab_text_width);
        mTabHeight = mContext.getResources().getDimensionPixelSize(R.dimen.topic_tab_text_height);

        //设置indicator的宽度
        rectF = new RectF(0, 0, convertDimension(22), convertDimension(2));
        //设置画笔
        mPaint = new Paint();
        //颜色为红色
        mPaint.setColor(Color.RED);
        //抗锯齿
        mPaint.setAntiAlias(true);
        //填充
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        //在ViewGroup中如果用到了onDraw(),则必须调用此方法
        setWillNotDraw(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //注册一个全局布局监听器，用于监听布局处理完毕后的回调事件
        this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN) //最小版本16
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "onGlobalLayout...");
                //移除当前监听器，不然会进行多次监听
                MultiLayout.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //获取当前控件的大小
                mTopicLayoutWidth = getMeasuredWidth();
                /*
                 * 更新tabs
                 * 这个方法本来是可以在onMeasure或者onLayout中执行的，
                 * 但是经过测试发现在这两个方法中进行更新tabs，添加进去的tabs不显示，通过测试才利用了
                 * {@link getViewTreeObserver()#addOnGlobalLayoutListener}
                 */
                updateTabs();
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && !isOnce && getChildCount() > 0) {//当tab全部添加完毕后，会调用此方法
            isOnce = true;//将其置为true,保证此方法只会调用一次

            //遍历所有的tabText，设置右边的margin
            for (TextView textView : mTabTextList) {
                LayoutParams layoutParams = (LayoutParams) textView.getLayoutParams();
                layoutParams.rightMargin = (int) mTabMinMargin;
                textView.setLayoutParams(layoutParams);
            }
            //取得最后一个child
            View child = getChildAt(getChildCount() - 1);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            //为了画indicator，所以最后一个child需要设置距离底部的距离
            lp.bottomMargin = (int) convertDimension(3);
            child.setLayoutParams(lp);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        if (isOnce || childCount <= 0) { //防止多次测量
            return;
        }

        //遍历当前控件的所有子控件
        for (int i = 0; i < childCount; i++) {

            LinearLayout linearLayout = (LinearLayout) getChildAt(i);
            if (linearLayout.getChildCount() <= 0) {
                continue;
            }
            //测量子控件的实际宽度
            int size = MeasureSpec.getSize(widthMeasureSpec);
            int widthMeasure = MeasureSpec.makeMeasureSpec(size, MeasureSpec.AT_MOST);
            linearLayout.measure(widthMeasure, heightMeasureSpec);
            int measuredWidth = linearLayout.getMeasuredWidth();
            //计算每个子控件中每个子控件需要的间隔距离
            float margin = (mTopicLayoutWidth - measuredWidth) * 1.0f / linearLayout.getChildCount();
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //保存画布
        canvas.save();
        //移动当前画布
        canvas.translate(mIndicatorTranslateX, mIndicatorTranslateY);
        //画圆角矩形
        canvas.drawRoundRect(rectF, convertDimension(1), convertDimension(1), mPaint);
        //恢复保存的画布
        canvas.restore();
    }

    /**
     * 设置tab的名称
     *
     * @param tabNames list
     */
    public void setTabNames(List<String> tabNames) {
        if (mTabNames != null && !mTabNames.isEmpty()) {
            mTabNames.clear();
        }
        mTabNames = tabNames;//tabs赋值
        updateTabs();//更新tab
        isOnce = false;//如果新更新了tab，则需要重新测量和布局
        requestLayout();//请求测量和布局
    }

    /**
     * 关联viewPager
     *
     * @param viewPager ViewPager，调用此方法前最好先给viewPager设置adapter
     */
    public void setupWithViewPager(ViewPager viewPager) {
        this.mViewPager = viewPager;
        if (!setTabNamesByAdapter(viewPager)) return;
        //设置adapter变化的监听事件
        this.mViewPager.addOnAdapterChangeListener(this);
        //设置viewPager切换pager的监听事件
        this.mViewPager.addOnPageChangeListener(this);
    }

    /**
     * 设置tabNames通过viewPager
     *
     * @param viewPager ViewPager
     * @return true设置成功
     */
    private boolean setTabNamesByAdapter(ViewPager viewPager) {
        //获取adapter
        PagerAdapter adapter = viewPager.getAdapter();
        if (adapter == null) {//如果为空则什么都不做
            return false;
        }
        /*
         * 获取adapter中的数据个数，然后遍历数据获取pageTitle
         */
        int count = adapter.getCount();
        List<String> tabNames = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            CharSequence title = adapter.getPageTitle(i);
            tabNames.add((String) title);
        }
        setTabNames(tabNames);
        return true;
    }

    /**
     * 根据tabNames中的数据去更新view
     */
    private void updateTabs() {
        Log.d(TAG, "update Tabs...");
        this.removeAllViews();
        if (mTabNames == null || mTabNames.isEmpty()) {
            Log.d(TAG, "mTabName is empty");
            return;
        }

        if (mTopicLayoutWidth == 0) {
            Log.d(TAG, "mTopicLayoutWidth is 0");
            return;
        }

        //创建一个mTabTextList用于存放创建的TextView
        if (mTabTextList != null && !mTabTextList.isEmpty()) {
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
            //如果等于0，说明是第一个控件
            if (mViewWidth == 0) {
                tabText.setSelected(true);//第一个控件默认选中
                mCurrentTabText = tabText;//当前控件
            }

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
            if (tmpViewWidth <= mTopicLayoutWidth) {
                mViewWidth += width;
                linearLayout.addView(tabText);
                continue;
            }

            /*
             *   linearLayout不能再添加tabText时，需要将linearLayout加入到当前控件中，然后重新创建linearLayout
             */
            mViewWidth = width;
            linearLayout = newLinearLayout();
            this.addView(linearLayout);
            linearLayout.addView(tabText);
        }
        //初始化indicator
        initIndicator();
        Log.d(TAG, "childCount: " + getChildCount());
    }


    private void initIndicator() {
        int count = getChildCount();
        if (count <= 0) {
            return;
        }

        LinearLayout linearLayout = (LinearLayout) getChildAt(0);
        int childCount = linearLayout.getChildCount();

        if (childCount > 0) {
            TextView child = (TextView) linearLayout.getChildAt(0);
            //测量text
            Paint paint = new Paint();
            paint.setTextSize(child.getTextSize());
            float measureText = paint.measureText(child.getText().toString());
            //设置indicator的左边距离
            rectF.left = (child.getMeasuredWidth() - measureText) / 2f;
            //设置indicator的上边距离
            rectF.top = child.getMeasuredHeight();
            //设置indicator的右边距离
            rectF.right += rectF.left;
            //设置indicator的底部距离
            rectF.bottom += rectF.top;
            //重绘indicator
            postInvalidate();
        }
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
     * 获取dp的值
     *
     * @param value 单位是dp
     * @return dp值
     */
    private float convertDimension(int value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                mContext.getResources().getDisplayMetrics());
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
        float density = mContext.getResources().getDisplayMetrics().density;
        int textSize = (int) (mContext.getResources().getDimensionPixelSize(R.dimen.topic_tab_text_size) / density);
        ColorStateList colorStateList = mContext.getResources().getColorStateList(R.color.selector_topic_tab_text);
        tabText.setTextSize(textSize);
        tabText.setTextColor(colorStateList);
        tabText.setGravity(Gravity.CENTER);
        tabText.setMaxLines(1);
        tabText.setEllipsize(TextUtils.TruncateAt.END);
        tabText.setText(tabName);
        LayoutParams layoutParams = new LayoutParams(mTabWidth, mTabHeight);
        tabText.setLayoutParams(layoutParams);
        tabText.setOnClickListener(this);
        return tabText;
    }

    /**
     * 更新indicator的位置
     *
     * @param v        当前点击的tabText
     * @param position tabText的位置，此位置是每一行对应的位置
     */
    private void updateIndicator(View v, int position) {
        LayoutParams params = (LayoutParams) v.getLayoutParams();
        int leftMargin = params.width;
        int x = (int) ((leftMargin + mTabMinMargin) * position);//设置x轴移动的距离

        int indexOfChild = indexOfChild((View) v.getParent());
        int y = params.height * indexOfChild;//设置y轴移动的距离

        if ((x != 0 || y != 0) && mIndicatorTranslateX == x && mIndicatorTranslateY == y) {
            return;//如果多次点击的是同一个tabText则不在进行绘制
        }
        mIndicatorTranslateX = x;
        mIndicatorTranslateY = y;
        postInvalidate();
    }

    /*
     * 测量view控件的宽高
     */
    private void measureTabText(TextView view) {
        int width = mContext.getResources().getDimensionPixelSize(R.dimen.topic_tab_text_width);
        int height = mContext.getResources().getDimensionPixelSize(R.dimen.topic_tab_text_height);
        int w = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int h = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        view.measure(w, h);
    }

    @Override
    public void onClick(View v) {

        if (!(v instanceof TextView)) {
            return;
        }
        TextView textView = (TextView) v;
        //tabText点击时，如果ViewPager不为空则需要与其联动
        if (mViewPager != null) {
            int index = mTabNames.indexOf(textView.getText().toString());
            mViewPager.setCurrentItem(index);
        } else { //如果ViewPager为空则自己处理状态选择事件
            selectTabText(textView);
        }
    }

    /**
     * 选择TabText
     *
     * @param textView tabText
     */
    private void selectTabText(TextView textView) {
        textView.setSelected(true);
        if (mCurrentTabText != null) {
            mCurrentTabText.setSelected(false);
        }
        mCurrentTabText = textView;
        LinearLayout parent = (LinearLayout) textView.getParent();
        int position = parent.indexOfChild(textView);
        updateIndicator(textView, position);
        if (mOnTabSelectListener != null) {
            mOnTabSelectListener.select(textView, position);
        }
    }


    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        TextView textView = mTabTextList.get(position);
        selectTabText(textView);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    @Override
    public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter,
                                 @Nullable PagerAdapter newAdapter) {
        setTabNamesByAdapter(viewPager);
    }

    /**
     * tab选择监听事件
     */
    public interface OnTabSelectListener {
        void select(TextView tabText, int position);
    }
}
