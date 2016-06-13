package com.example.chukc.pulltozoomlistview.view.listview;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chukc.pulltozoomlistview.R;
import com.example.chukc.pulltozoomlistview.view.viewpager.AutoScrollViewPager;

import java.util.ArrayList;


/**
 * 如果不需要下拉刷新直接在canPullDown中返回false，这里的自动加载和下拉刷新没有冲突，通过增加在尾部的footerview实现自动加载，
 * 所以在使用中不要再动footerview了
 */
public class PullableListView extends ListView implements Pullable, AbsListView.OnScrollListener {
    public static final int INIT = 0;
    public static final int LOADING = 1;
    private OnLoadListener mOnLoadListener;
    private ImageView mLoadingView;
    private TextView mStateTextView;
    private int state = INIT;
    private boolean canLoad = true;
    private boolean isFinishde = false; //數據是否全部加載完成
    private RelativeLayout footer;
    protected Scroller mScroller; // used for scroll back
    // 均匀旋转动画
    private RotateAnimation refreshingAnimation;
    private View view;
    private int FooterVisibleHeight = 120;
    private boolean isNoData; //是否没有数据
//    private AnimationDrawable mLoadAnim;

    // -- header view
    protected PullableListViewHeader mHeaderView;
    // header view content, use it to calculate the Header's height. And hide it
    // when disable pull refresh.
    protected AutoScrollViewPager mHeaderViewContent;
    protected int mHeaderViewHeight; // header view's height

    protected boolean mEnablePullRefresh = true;
    protected boolean mPullRefreshing = false; // is refreashing.
    protected PullableListViewRefreshListener mOnRefresh;
    protected int mScrollBack;
    protected final static int SCROLLBACK_HEADER = 0;
    protected final static int SCROLLBACK_FOOTER = 1;

    protected final static int SCROLL_DURATION = 400; // scroll back duration
    protected final static int PULL_LOAD_MORE_DELTA = 50; // when pull up >=
    // 50px
    // at bottom,
    // trigger
    // load more.
    protected final static float OFFSET_RADIO = 1.8f; // support iOS like pull
    private int t0, t1;
    // feature.
    protected float mLastY = -1; // save event y

    // total list items, used to detect is at the bottom of listview.
    protected int mTotalItemCount;
    protected OnScrollListener mScrollListener; // user's scroll listener

    public PullableListView(Context context) {
        super(context);
        init(context);
    }

    public PullableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mScroller = new Scroller(context, new DecelerateInterpolator());
        super.setOnScrollListener(this);
        view = LayoutInflater.from(context).inflate(R.layout.refresh_more,
                null);
        refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(context, R.anim.rotating);

        // init header view
        mHeaderView = new PullableListViewHeader(context);
        mHeaderViewContent = (AutoScrollViewPager) mHeaderView.getContent();
        addHeaderView(mHeaderView, null, false);
        // init header height
        mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mHeaderViewHeight = mHeaderViewContent.getHeight();
                System.out.println("GetmHeadViewHeight = " + mHeaderViewHeight);
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        footer = (RelativeLayout) view.findViewById(R.id.rl_footer);
        mLoadingView = (ImageView) footer.findViewById(R.id.loading_icon);
        mStateTextView = (TextView) footer.findViewById(R.id.loadstate_tv);
        addFooterView(view, null, false);
    }

    /**
     * enable or disable pull down refresh feature.
     *
     * @param refreshListener
     */
    public void setPullRefreshEnable(PullableListViewRefreshListener refreshListener) {
        mEnablePullRefresh = true;
        mHeaderViewContent.setVisibility(View.VISIBLE);
        this.mOnRefresh = refreshListener;

    }

    //添加 停止刷新
    public void stopRefresh() {
        if (mPullRefreshing == true) {
            mPullRefreshing = false;
            resetHeaderHeight();
        }
    }


    public void setImgUrls(ArrayList<Integer> listImg) {
        mHeaderView.setImgUrls(listImg);
    }


    /**
     * stop refresh, reset header view.
     */
    public void stopRefresh(String time) {
        if (mPullRefreshing == true) {
            mPullRefreshing = false;
            resetHeaderHeight();
        }
    }

    /**
     * reset header view's height.
     */
    protected void resetHeaderHeight() {
        int height = mHeaderView.getVisiableHeight();
        Log.i("pullzoom","height================"+height);
        Log.i("pullzoom","mPullRefreshing================"+mPullRefreshing);
        Log.i("pullzoom","mHeaderViewHeight================"+mHeaderViewHeight);
        if (height <= mHeaderViewHeight) {
            return;
        }
        if (height == 0) // not visible.
        {
            return;
        }
        // refreshing and header isn't shown fully. do nothing.
        if (mPullRefreshing && height <= mHeaderViewHeight) {
            return;
        }
        int finalHeight = mHeaderViewHeight; // default: scroll back to dismiss header.
        // is refreshing, just scroll back to show all the header.
        if (mPullRefreshing && height > mHeaderViewHeight) {
            finalHeight = mHeaderViewHeight;
        }
        Log.d("xlistview", "resetHeaderHeight-->" + (finalHeight - height));
        mScrollBack = SCROLLBACK_HEADER;
        mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
        // trigger computeScroll
        invalidate();
    }
    protected void updateHeaderHeight(float delta) {
        if (t0 < 0)
            return;
        mHeaderView.setVisiableHeight((int) delta + mHeaderView.getVisiableHeight());
        if (mEnablePullRefresh && !mPullRefreshing) {
            if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
//				System.out.println("not mPullRefreshing");
                mHeaderView.setState(PullableListViewHeader.STATE_READY);
            } else {
//				System.out.println("mPullRefreshing");
                mHeaderView.setState(PullableListViewHeader.STATE_NORMAL);
            }
        }
        setSelection(0); // scroll to top each time

    }



    @Override
    public void computeScroll() {
        if (mHeaderView.getMeasuredHeight() <= mHeaderViewHeight) {
            return;
        }
        if (mScroller.computeScrollOffset()) {
//            setBottomMargin(mScroller.getCurrY());
            if (mScrollBack == SCROLLBACK_HEADER) {
                mHeaderView.setVisiableHeight(mScroller.getCurrY());
            } else {
            }
            postInvalidate();
        } else {
        }
        super.computeScroll();
    }

    public void setBottomMargin(int height) {
        if (height < 0) return;
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) footer.getLayoutParams();
        lp.bottomMargin = height;
        footer.setLayoutParams(lp);
    }

    protected void resetFooterHeight() {
        int bottomMargin = getBottomMargin();
        if (bottomMargin > 0) {
            mScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION);
            invalidate();
        }
    }


    public int getBottomMargin() {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) footer.getLayoutParams();
        return lp.bottomMargin;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // 按下的时候禁止自动加载
                mLastY = ev.getRawY();
                getParent().requestDisallowInterceptTouchEvent(true);
                canLoad = false;
                mLastY = ev.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                Log.d("xlistview", "xlistView-height=========="+deltaY / OFFSET_RADIO);

                if (getFirstVisiblePosition() == 0 && (mHeaderView.getVisiableHeight() > 0 || deltaY > 0) && !mPullRefreshing) {
                    // the first item is showing, header has shown or pull down.
                    if (mEnablePullRefresh) {
                        if (deltaY > 0) {
                            updateHeaderHeight(deltaY / OFFSET_RADIO);
                        } else {
                            updateHeaderHeight(deltaY);
                        }

                        if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {  //header 拉伸情況下progressbar出現
                            setProgressBarVisible();
                            mHeaderView.setProgressBarDegree(2);
                        } else {
                            setProgressBarDissMiss();
                            mHeaderView.getProgressBar().clearAnimation();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                // 松开手判断是否自动加载
                getParent().requestDisallowInterceptTouchEvent(false);
                mLastY = -1; // reset
                canLoad = true;
                isFinishde = false;
                if (mHeaderView.getVisiableHeight() > mHeaderViewHeight && getFirstVisiblePosition() == 0) {
                    // invoke refresh
                    mHeaderView.setProgressBarDegree(-1);   //开始旋转加载动画
                    startOnRefresh();
                    resetHeaderHeight();
                }else{
                    checkLoad();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    protected void startOnRefresh() {
        Log.i("pullzoom","mEnablePullRefresh================"+mEnablePullRefresh);
        Log.i("pullzoom","mHeaderView.getVisiableHeight()="+mHeaderView.getVisiableHeight()+"====mHeaderViewHeight="+mHeaderViewHeight);
        Log.i("pullzoom","mPullRefreshing================"+mPullRefreshing);
        if (mEnablePullRefresh && mHeaderView.getVisiableHeight() > mHeaderViewHeight && !mPullRefreshing) {
            // System.out.println("refresh");
            mPullRefreshing = true;
            mHeaderView.setState(PullableListViewHeader.STATE_REFRESHING);
            if (mOnRefresh != null) {
                mOnRefresh.onRefresh();
            }
        }
    }

    public void setProgressBarDissMiss() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mHeaderView.getProgressBar().setVisibility(View.GONE);
                mHeaderView.getProgressBar().clearAnimation();
            }
        });
    }

    public void setProgressBarVisible() {
        mHeaderView.getProgressBar().setVisibility(View.VISIBLE);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // 在滚动中判断是否满足自动加载条件
//            checkLoad();
    }

    /**
     * 判断是否满足自动加载条件
     */
    private void checkLoad() {
        if (footer.getHeight() < FooterVisibleHeight)
            showFooter();
        if (reachBottom() && mOnLoadListener != null && state != LOADING
                && canLoad && !isFinishde) {
            Toast.makeText(getContext(),"底部自动加载",Toast.LENGTH_LONG).show();
            isNoData=false;
            changeState(LOADING);
            mOnLoadListener.onLoad(this);
            resetFooterHeight();
        }
    }

    private void changeState(int state) {
        this.state = state;
        switch (state) {
            case INIT:
                mLoadingView.clearAnimation();
                mLoadingView.setVisibility(View.INVISIBLE);
                if (mStateTextView != null)
                    mStateTextView.setVisibility(View.VISIBLE);
                mStateTextView.setText("数据已加载完");
                break;

            case LOADING:
                mLoadingView.setVisibility(View.VISIBLE);
                mLoadingView.startAnimation(refreshingAnimation);
                if (mStateTextView != null)
                    mStateTextView.setVisibility(View.GONE);
                mStateTextView.setText("加载中");
                break;
        }
    }

    /**
     * 完成加载
     */
    public void finishLoading() {
        changeState(INIT);
    }

    @Override
    public boolean canPullDown() {
        if (getCount() == 0) {
            // 没有item的时候也可以下拉刷新
            return true;
        } else if (getFirstVisiblePosition() == 0
                && getChildAt(0).getTop() >= 0) {
            // 滑到ListView的顶部了
            return true;
        } else
            return false;
    }

    @Override
    public boolean canPullUp() {
        if (getCount() == 0) {
            // 没有item的时候也可以上拉加载
            return true;
        } else if (getLastVisiblePosition() == (getCount() - 1)) {
            // 滑到底部了
            if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null
                    && getChildAt(
                    getLastVisiblePosition()
                            - getFirstVisiblePosition()).getBottom() <= getMeasuredHeight()&&!isNoData)
                return true;
        }
        return false;
    }


    public void setOnLoadListener(OnLoadListener listener) {
        this.mOnLoadListener = listener;
    }

    /**
     * @return footerview可见时返回true，否则返回false
     */
    public boolean reachBottom() {
        if (getCount() == 0) {
            // 没有item的时候也可以上拉加载
            return true;
        } else if (getLastVisiblePosition() == (getCount() - 1)) {
            // 滑到底部了
            if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null
                    && getChildAt(
                    getLastVisiblePosition()
                            - getFirstVisiblePosition()).getTop() < getMeasuredHeight())
                return true;
        }
        return false;
    }

    public void setFinishedFooter() {
        this.state = INIT;
        isFinishde = true;
        hideFooter();
    }


    public void hideFooter() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) footer.getLayoutParams();
        params.height = 0;
        footer.setLayoutParams(params);
    }

    private void showFooter() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) footer.getLayoutParams();
        params.height = FooterVisibleHeight;
        footer.setLayoutParams(params);
    }

    public void setFinishedView() {
        mLoadingView.setVisibility(View.INVISIBLE);
        mStateTextView.setText("数据全部加载完成");
    }

    public void removeFooter() {
//        removeFooterView(view);
        footer.setVisibility(View.GONE);
        isNoData=true;
    }

    public boolean getIsNoData(){
        return  isNoData;
    }
    public void addFooter() {
        addFooterView(view, null, false);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mScrollListener = l;
    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mScrollListener != null) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
        checkLoad();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // send to user's listener
        mTotalItemCount = totalItemCount;
        if (mScrollListener != null) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
        View c0 = view.getChildAt(0);

        try {
            t0 = c0.getTop();
            if (firstVisibleItem == 0 && t0 <= 0) {
//                ViewHelper.setY(mHeaderViewContent, -t0 / 2);
            }

        } catch (Exception e) {

        }

    }

    public interface OnLoadListener {
        void onLoad(PullableListView pullableListView);
    }
}

