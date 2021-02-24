package com.example.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.example.myapplication.DocsLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuyong on 2018/1/15.
 * 有左滑菜单的item布局
 */
public class SwipeMenuLayout extends ViewGroup {

    private final String TAG = "SwipeMenuLayout";
    private static boolean isTouching;//防止多指滑动
    private static SwipeMenuLayout mViewCache;//存储当前展开菜单的View

    private final int mSpeedLimit = 2000;//手指滑速阀值
    private int mMinSwipeDistance;//滑动事件的最小距离
    private int mPointerId;//多点触摸取第一根手指有效
    private int mHeight;//控件的高度
    private int mMenuWidths;//右侧菜单宽度总和(最大滑动距离)
    private int mLimit;//滑动判定临界值（右侧菜单宽度的30%） 手指抬起时，超过了展开，没超过收起menu
    private View mContentView;
    private PointF mFirstP = new PointF();//记录手指按下的坐标

    private boolean isUserSwiped;//是否滑动
    private boolean isInterceptFlag;
    private boolean isSwipeEnable = true;
    private boolean isOpened;
    private VelocityTracker mVelocityTracker;//记录手指滑速
    private ValueAnimator mExpandAnim, mCloseAnim;
    private DocsLinearLayoutManager mLayoutManager;

    private List<OnMenuStateChangeListener> mStateChangeListeners = new ArrayList<>();
    private OnUserSwipeMenuListener mOnUserSwipeMenuListener;
    private int mDefaultMenuWidths;

    public SwipeMenuLayout(Context context) {
        this(context, null);
    }

    public SwipeMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setSwipeEnable(boolean enable) {
        isSwipeEnable = enable;
    }

    public boolean getSwipeEnable() {
        return isSwipeEnable;
    }

    /**
     * 返回ViewCache
     */
    public static SwipeMenuLayout getViewCache() {
        return mViewCache;
    }

    public void setLayoutManager(DocsLinearLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    private void init(Context context) {
        mMinSwipeDistance = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setClickable(true);//令自己可点击，从而获取触摸事件
        mMenuWidths = 0;//由于ViewHolder的复用机制，每次这里要手动恢复初始值
        mHeight = 0;
        int contentWidth = 0;//以第一个子Item(即ContentItem)的宽度为控件宽度
        int childCount = getChildCount();

        final boolean measureMatchParentChildren = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        boolean isNeedMeasureChildHeight = false;

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            childView.setClickable(true);//令每一个子View可点击，从而获取触摸事件
            if (childView.getVisibility() != GONE) {
                measureChild(childView, widthMeasureSpec, heightMeasureSpec);
                final MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
                mHeight = Math.max(mHeight, childView.getMeasuredHeight()/* + lp.topMargin + lp.bottomMargin*/);
                if (measureMatchParentChildren && lp.height == LayoutParams.MATCH_PARENT) {
                    isNeedMeasureChildHeight = true;
                }
                if (i > 0) {//第一个布局是Left item，从第二个开始才是RightMenu
                    mMenuWidths += childView.getMeasuredWidth();
                } else {
                    mContentView = childView;
                    contentWidth = childView.getMeasuredWidth();
                }
            }
        }
        setMeasuredDimension(getPaddingLeft() + getPaddingRight() + contentWidth,
                mHeight + getPaddingTop() + getPaddingBottom());//宽度取第一个Item(Content)的宽度
        mLimit = mMenuWidths / 5;//滑动判断的临界值
        if (isNeedMeasureChildHeight) {//如果子View的height有MatchParent属性的，设置子View高度
            forceUniformHeight(childCount, widthMeasureSpec);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    /**
     * 给MatchParent的子View设置高度
     */
    private void forceUniformHeight(int count, int widthMeasureSpec) {
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(),
                MeasureSpec.EXACTLY);//以父布局高度构建一个Exactly的测量参数
        for (int i = 0; i < count; ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    int oldWidth = lp.width;//measureChildWithMargins 这个函数会用到宽，所以要保存一下
                    lp.width = child.getMeasuredWidth();
                    measureChildWithMargins(child, widthMeasureSpec, 0, uniformMeasureSpec, 0);
                    lp.width = oldWidth;
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int left = getPaddingLeft();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != GONE) {
                if (i == 0) {//第一个子View是内容 宽度设置为全屏
                    childView.layout(left, getPaddingTop(), left + childView.getMeasuredWidth(), getPaddingTop() + childView.getMeasuredHeight());
                    left = left + childView.getMeasuredWidth();
                } else {
                    childView.layout(left, getPaddingTop(), left + childView.getMeasuredWidth(), getPaddingTop() + childView.getMeasuredHeight());
                    left = left + childView.getMeasuredWidth();
                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isSwipeEnable) {
            acquireVelocityTracker(ev);
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isUserSwiped = false;//判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件。
                    isInterceptFlag = false;//每次DOWN时，默认是不拦截的
                    //多指触屏，只取第一个手指有效
                    if (isTouching) {
                        return false;
                    } else {
                        isTouching = true;
                    }
                    mFirstP.set(ev.getRawX(), ev.getRawY());//判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件。
                    if (mViewCache != null) {//如果down，view和cacheview不一样，则立马让它还原。且把它置为null
                        if (mViewCache != this) {
                            mViewCache.closeMenu();
                            isInterceptFlag = true;//当前有侧滑菜单的View，且不是自己的，就该拦截事件咯。
                        }
                        // getParent().requestDisallowInterceptTouchEvent(true);//只要有一个侧滑菜单处于打开状态， 就不给外层布局上下滑动了
                    }
                    mPointerId = ev.getPointerId(0);//多指触屏，只取第一个手指有效
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isInterceptFlag) {//当前有侧滑菜单的View，且不是自己的，就该拦截事件咯。滑动也不该出现
                        break;
                    }
                    float distanceX = ev.getRawX() - mFirstP.x;
                    if (Math.abs(distanceX) > mMinSwipeDistance || isUserSwiped) {
                        isUserSwiped = true;
                        int toScrollX = (int) (getScrollX() - distanceX);
                        if (toScrollX < 0) {
                            toScrollX = 0;
                        } else if (toScrollX > mMenuWidths) {
                            toScrollX = mMenuWidths;
                        }
                        scrollTo(toScrollX, 0);
                        mFirstP.set(ev.getRawX(), ev.getRawY());
                        if (mLayoutManager != null) {
                            mLayoutManager.enableScrollVertically(false);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    //当前有侧滑菜单的View，且不是自己的，就该拦截事件咯。滑动也不该出现
                    if (!isInterceptFlag && isUserSwiped) {//且滑动了 才判断是否要收起、展开menu
                        //求伪瞬时速度,左滑为负，右滑为正
                        mVelocityTracker.computeCurrentVelocity(500);
                        float velocityX = mVelocityTracker.getXVelocity(mPointerId);
                        if (velocityX >= mSpeedLimit) {//快速右滑
                            closeMenu();
                        } else if (velocityX > 0) {//慢速右滑
                            if (getScrollX() < mMenuWidths - mLimit) {//右滑大于阀值
                                closeMenu();
                            } else {
                                openMenu();
                            }
                        } else if (velocityX > -mSpeedLimit) {//慢速左滑
                            if (getScrollX() > mLimit) {
                                openMenu();
                            } else {
                                closeMenu();
                            }
                        } else {//快速左滑
                            openMenu();
                        }
                    }
                    //释放
                    releaseVelocityTracker();
                    isTouching = false;//没有手指在摸我了
                    if (mLayoutManager != null) {
                        mLayoutManager.enableScrollVertically(true);
                    }
                    break;
                default:
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isSwipeEnable) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (isUserSwiped) {//左滑过程中，拦截点击事件
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (getScrollX() > mMinSwipeDistance) {
                        if (ev.getX() < getWidth() - getScrollX()) {//菜单展开时，点击内容区域
                            if (!isUserSwiped) {//如果是点击事件
                                closeMenu();
                            }
                            return true;//拦截点击事件
                        }
                    }
                    if (isUserSwiped) {// 滑动时，拦截事件
                        return true;
                    }
                    break;
            }
            if (isInterceptFlag) {//点击其他区域关闭
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 展开menu
     */
    public void openMenu() {
        //展开就加入ViewCache：
        mViewCache = this;
        if (mLayoutManager != null) {
            mLayoutManager.enableScrollVertically(false);
        }
        //侧滑菜单展开，屏蔽content长按
        if (null != mContentView) {
            mContentView.setLongClickable(false);
        }
        cancelAnim();
        mExpandAnim = ValueAnimator.ofInt(getScrollX(), mMenuWidths);
        mExpandAnim.addUpdateListener(animation -> scrollTo(Math.min((Integer) animation.getAnimatedValue(), mMenuWidths), 0));
        mExpandAnim.setInterpolator(new OvershootInterpolator());
        mExpandAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isOpened = true;
                notifyOnMenuStateChange(true);
            }
        });
        mExpandAnim.setDuration(300).start();

        if (mOnUserSwipeMenuListener != null) {
            mOnUserSwipeMenuListener.onSwipe(this, true);
        }
    }

    /**
     * 每次执行动画之前都应该先取消之前的动画
     */
    private void cancelAnim() {
        if (mCloseAnim != null && mCloseAnim.isRunning()) {
            mCloseAnim.cancel();
        }
        if (mExpandAnim != null && mExpandAnim.isRunning()) {
            mExpandAnim.cancel();
        }
    }

    /**
     * 关闭menu
     */
    public void closeMenu() {
        mViewCache = null;
        if (mLayoutManager != null) {
            mLayoutManager.enableScrollVertically(true);
        }
        //侧滑菜单展开，屏蔽content长按
        if (null != mContentView) {
            mContentView.setLongClickable(true);
        }
        cancelAnim();
        mCloseAnim = ValueAnimator.ofInt(getScrollX(), 0);
        mCloseAnim.addUpdateListener(animation -> scrollTo((Integer) animation.getAnimatedValue(), 0));
        mCloseAnim.setInterpolator(new AccelerateInterpolator());
        mCloseAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isOpened = false;
                notifyOnMenuStateChange(false);
            }
        });
        mCloseAnim.setDuration(120).start();

        if (mOnUserSwipeMenuListener != null) {
            mOnUserSwipeMenuListener.onSwipe(this, false);
        }
    }

    /**
     * 向VelocityTracker添加MotionEvent
     */
    private void acquireVelocityTracker(final MotionEvent event) {
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * * 释放VelocityTracker
     */
    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (this == mViewCache) {
            mViewCache.quickClose();
            mViewCache = null;
        }
        super.onDetachedFromWindow();
    }

    /**
     * 展开时，禁止长按
     */
    @Override
    public boolean performLongClick() {
        if (getScrollX() > mMinSwipeDistance) {
            return false;
        }
        return super.performLongClick();
    }

    /**
     * 快速展开
     */
    public void quickOpen() {
        //展开就加入ViewCache：
        mViewCache = this;
        if (mLayoutManager != null) {
            mLayoutManager.enableScrollVertically(false);
        }
        //侧滑菜单展开，屏蔽content长按
        if (mContentView != null) {
            mContentView.setLongClickable(false);
        }
        cancelAnim();
        scrollTo(Math.max(mDefaultMenuWidths, mMenuWidths), 0);

        isOpened = true;
        notifyOnMenuStateChange(true);
    }

    /**
     * 快速关闭。
     * 在RecyclerView里，视情况而定，如果是mAdapter.notifyItemRemoved(pos)方法不用调用。
     */
    public void quickClose() {
        if (mLayoutManager != null) {
            mLayoutManager.enableScrollVertically(true);
        }
        if (this == mViewCache) {
            cancelAnim();//先取消展开动画
            mViewCache.scrollTo(0, 0);//关闭
            mViewCache = null;
            isOpened = false;
            notifyOnMenuStateChange(false);
        }
    }

    public void addOnMenuStateChangeListener(OnMenuStateChangeListener listener) {
        if (!mStateChangeListeners.contains(listener)) {
            mStateChangeListeners.add(listener);
        }
    }

    public void removeOnMenuStateChangeListener(OnMenuStateChangeListener listener) {
        mStateChangeListeners.remove(listener);
    }

    public void notifyOnMenuStateChange(boolean isOpen) {
        for (OnMenuStateChangeListener stateChangeListener : mStateChangeListeners) {
            stateChangeListener.onMenuStateChanged(this, isOpen);
        }
    }

    public void setOnUserSwipeMenuListener(OnUserSwipeMenuListener listener) {
        mOnUserSwipeMenuListener = listener;
    }

    public void setDefaultMenuWidths(int defaultMenuWidths) {
        mDefaultMenuWidths = defaultMenuWidths;
    }

    /**
     * menu状态改变的监听，为了使测滑菜单关闭时，"确认删除"隐藏
     */
    public interface OnMenuStateChangeListener {
        void onMenuStateChanged(SwipeMenuLayout view, boolean isOpen);
    }

    /**
     * 用户滑动的监听
     */
    public interface OnUserSwipeMenuListener {
        void onSwipe(SwipeMenuLayout view, boolean isOpen);
    }
}
