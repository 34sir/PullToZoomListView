/**
 * GListView's header
 */
package com.example.chukc.pulltozoomlistview.view.listview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.chukc.pulltozoomlistview.R;
import com.example.chukc.pulltozoomlistview.util.DensityUtil;
import com.example.chukc.pulltozoomlistview.view.ColorfulRingProgressView;
import com.example.chukc.pulltozoomlistview.view.viewpager.AutoScrollViewPager;
import com.example.chukc.pulltozoomlistview.view.viewpager.ParallaxPageTransformer;

import java.util.ArrayList;


public class PullableListViewHeader extends LinearLayout {
    private RelativeLayout mContainer;
    //	private AdViewPager adViewPager;
    private AutoScrollViewPager adViewPager;
    private int mState = STATE_NORMAL;

    private static ArrayList<Integer> listImg = new ArrayList<Integer>();
    //	private static final ArrayList<String> listImg = new ArrayList<String>();
    private int width;

    private final int ROTATE_ANIM_DURATION = 180;

    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;

    private Context context;

    private ColorfulRingProgressView progressBar;
    RotateAnimation animation;

    public PullableListViewHeader(Context context) {
        super(context);
        initView(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public PullableListViewHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

//	public void setImgUrls(List<String> imgUrls) {
//		adViewPager.setViewPagerViews(imgUrls);
//	}

    private void initView(Context context) {
        this.context = context;
        animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());//不停顿
        animation.setRepeatCount(Animation.INFINITE);
//        animation.setFillAfter(true);//停在最后
        animation.setDuration(1000);


        width = DensityUtil.getScreenW(context);

        LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT); // 在这可以改变初始高度
        mContainer = (RelativeLayout) LayoutInflater.from(context).inflate(
                R.layout.view_glistview_header, null);
        if (isInEditMode()) {
            return;
        }
        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);

//		adViewPager = new AdViewPager(context, null, width, false);
//		listImg.add(R.mipmap.bgtop );
//		listImg.add(R.mipmap.a );
//		listImg.add(R.mipmap.b );
//		listImg.add(R.mipmap.c );
        adViewPager = new AutoScrollViewPager(context);
//		adViewPager = (AutoScrollViewPager) LayoutInflater.from(context).inflate(R.layout.list_microshop_header1, null).findViewById(R.id.myPager);
        adViewPager.setAdapter(new ImageBunnerAdapter(LayoutInflater.from(context), context));
        adViewPager.setCurrentItem(0);
        adViewPager.setPageTransformer(true, new ParallaxPageTransformer(R.id.image));
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                adViewPager.setInterval(4000); // /设置滚动时间间隔
                adViewPager.setDirection(AutoScrollViewPager.RIGHT); // 设置滚动方向
                adViewPager.setCycle(true);
                adViewPager.setAutoScrollDurationFactor(3);
                adViewPager.setStopScrollWhenTouch(true);
                adViewPager.setBorderAnimation(true);
                adViewPager.startAutoScroll();
            }
        }).start();

        adViewPager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        adViewPager.getParent().requestDisallowInterceptTouchEvent(
                                true);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        adViewPager.getParent().requestDisallowInterceptTouchEvent(
                                false);
                        break;
                }
                return false;
            }
        });
//		adViewPager.setDotsMargin(DensityUtil.dip2px(context, 20));
        mContainer.addView(adViewPager);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params.rightMargin = 30;
        params.topMargin = 30;
        params.height = 80;
        params.width = 80;
        progressBar = new ColorfulRingProgressView(context);
        progressBar.setStartAngle(0);
        progressBar.setStrokeWidth(5);
        progressBar.setFgColorEnd(Color.parseColor("#15a9bc"));
        progressBar.setFgColorStart(Color.parseColor("#15a9bc"));
        mContainer.addView(progressBar, params);

        adViewPager.setHeight(328);
    }

    int i = 0;
    final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            int degree = msg.getData().getInt("degree");
            if (degree != -1) {
                progressBar.setStartAngle(360 - i);
                i = i + degree;
            } else {
                progressBar.startAnimation(animation);
            }
        }

    };

    public void setProgressBarDegree(int degree) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putInt("degree", degree);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public void setImgUrls(ArrayList<Integer> listImg) {
        this.listImg = listImg;
    }

    public ColorfulRingProgressView getProgressBar() {
        return progressBar;
    }
    public void setState(int state) {
        if (state == mState)
            return;

        if (state == STATE_REFRESHING) {

        } else {

        }

        switch (state) {
            case STATE_NORMAL:
                if (mState == STATE_READY) {
                }
                if (mState == STATE_REFRESHING) {
                }
                break;
            case STATE_READY:
                if (mState != STATE_READY) {
                }
                break;
            case STATE_REFRESHING:
                break;
            default:
        }

        mState = state;
    }

    public void setVisiableHeight(int height) {
        System.out.print("height=================" + height);
        if (height < 0)
            height = 0;
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        lp.height = height;
        lp.gravity = Gravity.CENTER;
        // LogUtil.i(height+"");
        mContainer.setLayoutParams(lp);
        adViewPager.setHeight(height);
    }

    public int getVisiableHeight() {
        return mContainer.getHeight();
    }

    public ViewPager getContent() {
        return adViewPager.getViewPager();
    }

//	public void setAdViewPagerHandler(Handler handler) {
//		adViewPager.setHandler(handler);
//	}


    class ImageBunnerAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        public ImageBunnerAdapter(LayoutInflater inflater, Context context) {
            super();
            this.inflater = inflater;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
//            return ESiteBanner.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public Object instantiateItem(ViewGroup container, int position) {
            /*
             * ImageView view = images.get(position % images.size());//添加一个
			 * view.setScaleType(ScaleType.FIT_XY); container.addView(view);
			 */

            View imageLayout = inflater.inflate(R.layout.item_homepager_image, container, false);
            ImageView mImageView = (ImageView) imageLayout.findViewById(R.id.image);
            if (listImg.size() > 0) {
                mImageView.setImageDrawable(context.getDrawable(listImg.get(position % listImg.size())));
            }
            try {
                if (imageLayout.getParent() == null) {
                    // container.addView(view);//这里可能会报一个错。must call
                    // removeView().on the child....first
                    container.addView(imageLayout);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return imageLayout;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }
    }

}
