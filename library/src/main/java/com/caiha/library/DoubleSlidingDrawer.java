package com.caiha.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.RadioButton;
import android.widget.RadioGroup;


/**
 * 类名: DoubleSlidingDrawer <br/>
 * 描述: 自定义双抽屉类<br/>
 * 日期: 2015年1月12日 下午1:56:36 <br/>
 * <br/>
 *
 * @author Caily
 * @version 产品版本信息 yyyy-mm-dd Caily 修改信息<br/>
 * @see
 * @since 1.0
 */
public class DoubleSlidingDrawer extends ViewGroup {
    // RadioButton状态
    private static final int RADIOBUTTON_NOT_CHECKED = -1;
    private static final int RADIOBUTTON_CN_CHECKED = 0;
    private static final int RADIOBUTTON_NET_CHECKED = 1;

    // Handle拖动灵敏度
    private static final int SCROLL_SENSITIVITY = 20;

    // 动画持续时间
    private static final int ANIMATION_DURATION_TIME = 500;

    // 当前View
    private View mView;

    // 当前View的宽高
    private int mViewWidth;
    private int mViewHeight;

    // Handle的宽度
    private int mHandleWidth;

    // Handle的Id
    private int mHandleId;

    // Conent的Id
    private int mContentId;

    // Handle的View
    private View mHandle;

    // Conent的View
    private View mContent;

    // Content的宽度
    private int mContentWidth;

    // Rect实例
    private final Rect mFrame = new Rect();

    // 上次点击handle的Id 变量取值 -1：未选择，0：选择国内，1：选择国际
    private int mLastCheckedId;

    // View的边距
    private int mOffSet;

    // 抽屉是否打开
    private boolean mExpanded;

    // 上次拖动的X值
    private int mLastMoveX;

    // 第一次拖动的X值
    private int mFirstMoveX;

    // 是否拖动
    private boolean isScrolling;
    // 是否正在执行动画
    private boolean mAnimating;

    public DoubleSlidingDrawer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mView = this;
    }

    /**
     * 构造函数，获取View属性
     */
    public DoubleSlidingDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.DoubleSlidingDrawerx,
                defStyle, 0);
        // 获取属性
        mOffSet = (int) obtainStyledAttributes.getDimension(R.styleable.DoubleSlidingDrawerx_offset, 0.0f);
        mHandleId = obtainStyledAttributes.getResourceId(R.styleable.DoubleSlidingDrawerx_handle, 0);
        mContentId = obtainStyledAttributes.getResourceId(R.styleable.DoubleSlidingDrawerx_content, 0);
        obtainStyledAttributes.recycle();
        // 初始化值
        mLastCheckedId = RADIOBUTTON_NOT_CHECKED;

    }

    /**
     * 获取子View
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i("tag", "===========onFinishInflate===========");
        // 获取Handle
        mHandle = findViewById(mHandleId);
        if (mHandle == null) {
            throw new IllegalArgumentException("mHandleId必须指向一个存在的引用");
        }
        // 获取Content
        mContent = findViewById(mContentId);
        if (mContent == null) {
            throw new IllegalArgumentException("mContentId必须指向一个存在的引用");
        }

        ViewPager vp = (ViewPager) mContent;
        // 设置ViewPager监听，可在国内国际切换，同时更改RadioButton
        vp.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                RadioGroup rg = (RadioGroup) mHandle;
                RadioButton rb = (RadioButton) rg.getChildAt(arg0);
                rb.setChecked(true);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    /**
     * 测量子View尺寸
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i("tag", "===========onMeasure===========");

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightSpcMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpcSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpcMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("SlidingDrawer cannot have UNSPECIFIED dimensions");
        }

        final View handle = mHandle;
        final View content = mContent;

        int contentWidth = widthSpecSize - handle.getMeasuredWidth();
        // 测量子View大小
        measureChild(content, MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightSpcSize, MeasureSpec.EXACTLY));

        measureChild(handle, widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(widthSpecSize, heightSpcSize);
    }

    /**
     * 绘制View
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        final long drawingTime = getDrawingTime();
        final View handle = mHandle;
        final View content = mContent;
        drawChild(canvas, handle, drawingTime);
        // 抽屉打开和正在拖动时：绘制Content,否则只绘制Handle
        if (mExpanded || isScrolling) {
            drawChild(canvas, content, drawingTime);
        }

    }

    /**
     * 设置View坐标
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        // 获取父View的宽高
        final int ViewWidth = r - l;
        final int ViewWheight = b - t;

        // 获取handle的宽高
        final View handle = mHandle;
        int handleWidth = handle.getMeasuredWidth();
        int handleHeight = handle.getMeasuredHeight();

        // handle的left
        int handleLeft;

        // handle的top
        int handleTop;

        handleLeft = ViewWidth - handleWidth;

        handleTop = (ViewWheight - handleHeight) / 2;

        final View content = mContent;

        int contentLeft = handleWidth + mOffSet;
        // 配置Content位置
        content.layout(contentLeft, 0, contentLeft + content.getMeasuredWidth(), content.getMeasuredHeight());
        // 根据抽屉是否打开，设置handle不同位置
        if (mExpanded) {
            handle.layout(mOffSet, handle.getTop(), handleWidth + mOffSet, handle.getBottom());
        } else {
            handle.layout(handleLeft, handleTop, handleLeft + handleWidth, handleTop + handleHeight);
        }
        // 保存Handle和Content的宽度
        mHandleWidth = handle.getWidth();
        mContentWidth = content.getWidth();

    }

    /**
     * onInterceptTouchEvent监听
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        final Rect frame = mFrame;
        final View content = mContent;

        content.getHitRect(frame);
        float x = event.getX();
        float y = event.getY();
        // 抽屉打开，Content展示，传递onTouchEvent到子View
        if (mExpanded && frame.contains((int) x, (int) y)) {
            return false;
        }
        // 否则传递到父View的onTouchEvent
        return true;
    }

    /**
     * onTouchEvent监听
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 正在执行动画，不接收Touch事件
        if (mAnimating) {
            return false;
        }
        final int action = event.getAction();
        final View handle = mHandle;
        final Rect frame = mFrame;

        handle.getHitRect(frame);
        float x = event.getX();
        float y = event.getY();

        handle.getHitRect(frame);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMoveX = (int) x;
                mFirstMoveX = mLastMoveX;
                if (!frame.contains((int) x, (int) y)) {
                    return false;
                }
            case MotionEvent.ACTION_MOVE:
                // 判断是否是拖动操作
                if (Math.abs(mFirstMoveX - (int) x) > SCROLL_SENSITIVITY) {
                    isScrolling = true;
                }
                // 移动View
                moveView((int) x);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isScrolling) {
                    // 判断点击的区域
                    if (y < mViewHeight / 2) {
                        mLastCheckedId = RADIOBUTTON_CN_CHECKED;
                    } else {
                        mLastCheckedId = RADIOBUTTON_NET_CHECKED;
                    }
                    setViewLocation((int) x);

                } else {
                    handle.getHitRect(frame);
                    if (!frame.contains((int) x, (int) y)) {
                        return false;
                    }
                    frame.bottom = frame.bottom - handle.getHeight() / 2;
                    if (mExpanded) {
                        // 判断是选择handle的那个区域和点击次数
                        if (frame.contains((int) x, (int) y)) {
                            if (mLastCheckedId == RADIOBUTTON_CN_CHECKED) {
                                // 再次点击同一区域，关闭抽屉
                                mExpanded = false;
                                setViewLocation((int) x);
                            } else {
                                // 再次点击不同区域，切换数据
                                mLastCheckedId = RADIOBUTTON_CN_CHECKED;
                                changeRadioButton(mLastCheckedId);
                            }
                        } else {
                            // 再次点击同一区域，关闭抽屉
                            if (mLastCheckedId == RADIOBUTTON_NET_CHECKED) {
                                mExpanded = false;
                                setViewLocation((int) x);
                            } else {
                                // 再次点击不同区域，切换数据
                                mLastCheckedId = RADIOBUTTON_NET_CHECKED;
                                changeRadioButton(mLastCheckedId);
                            }
                        }

                    } else {
                        // 打开抽屉
                        mExpanded = true;
                        if (frame.contains((int) x, (int) y)) {
                            mLastCheckedId = RADIOBUTTON_CN_CHECKED;
                            changeRadioButton(mLastCheckedId);
                        } else {
                            mLastCheckedId = RADIOBUTTON_NET_CHECKED;
                            changeRadioButton(mLastCheckedId);
                        }
                        // 设置View位置
                        setViewLocation((int) x);
                    }
                }
                break;

        }
        return true;
    }

    /**
     * 根据抽屉状态，设置子View的位置
     *
     * @param positionX 拖动后View在X位置
     * @author Caily
     * @date 2015年1月14日
     * @since 1.0
     */
    private void setViewLocation(int positionX) {
        final View handle = mHandle;
        final View content = mContent;
        // 移动handle，执行相应动画

        int leftHandle = handle.getLeft();
        int leftContent = content.getLeft();
        // 存在拖动操作，判断是否拖过屏幕的一半，超过打开抽屉，未超过关闭抽屉
        if (isScrolling) {
            isScrolling = false;
            if (positionX < mViewWidth / 2) {
                mExpanded = true;
                changeRadioButton(mLastCheckedId);
                startAnimation();
                handle.offsetLeftAndRight(mOffSet - leftHandle);
                mView.invalidate();
            } else {
                mExpanded = false;
                // handle.offsetLeftAndRight(mViewWidth - leftHandle -
                // mHandleWidth);
                changeRadioButton(RADIOBUTTON_NOT_CHECKED);
                startAnimation();
            }
            content.offsetLeftAndRight(mOffSet + mHandleWidth - leftContent);
            return;
        }
        // 没有拖动操作，按点击操作控制
        if (mExpanded) {
            //startAnimation();
            handle.offsetLeftAndRight(mOffSet - leftHandle);
            content.offsetLeftAndRight(mOffSet + mHandleWidth - leftContent);
        } else {
            mLastCheckedId = RADIOBUTTON_NOT_CHECKED;
            changeRadioButton(mLastCheckedId);
            //startAnimation();
        }
        mView.invalidate();
    }

    /**
     * 根据拖动操作，移动View
     *
     * @param positionX 拖动操作X轴的位置
     * @author Caily
     * @date 2015年1月14日
     * @since 1.0
     */
    private void moveView(int positionX) {
        final View handle = mHandle;
        final View content = mContent;
        // 判断拖动位置是否超过边界和最大值
        if (isScrolling && handle.getLeft() >= mOffSet && handle.getRight() <= mViewWidth) {
            int moveX = positionX - mLastMoveX;
            if (handle.getLeft() == mOffSet && moveX < 0) {
                return;
            }
            if (handle.getRight() == mViewWidth && moveX > 0) {
                return;
            }
            handle.offsetLeftAndRight(moveX);
            if (mExpanded) {
                content.offsetLeftAndRight(moveX);
            } else {
                content.setLeft(handle.getLeft() + mHandleWidth);
                content.setRight(handle.getRight() + mContentWidth);
            }
            mLastMoveX = positionX;
        }
        mView.invalidate();
    }

    /**
     * 更改RadioGrou及ViewPager
     *
     * @param mLastCheckedId 选择Handle中RadioButton的位置
     * @author Caily
     * @date 2015年1月14日
     * @since 1.0
     */
    private void changeRadioButton(int mLastCheckedId) {
        RadioGroup rg = (RadioGroup) mHandle;
        RadioButton btnCn = (RadioButton) rg.getChildAt(RADIOBUTTON_CN_CHECKED);
        RadioButton btnNet = (RadioButton) rg.getChildAt(RADIOBUTTON_NET_CHECKED);
        ViewPager vp = (ViewPager) mContent;
        // 设置相应的RadioButton、ViewPager
        switch (mLastCheckedId) {
            case RADIOBUTTON_CN_CHECKED:
                btnCn.setChecked(true);
                vp.setCurrentItem(RADIOBUTTON_CN_CHECKED);
                break;
            case RADIOBUTTON_NET_CHECKED:
                btnNet.setChecked(true);
                vp.setCurrentItem(RADIOBUTTON_NET_CHECKED);
                break;
            case RADIOBUTTON_NOT_CHECKED:
                rg.clearCheck();
                break;
            default:
                break;
        }
    }

    /**
     * 获取屏幕Width
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
    }

    /**
     * 点击操作，执行打开，关闭动画
     *
     * @author Caily
     * @date 2015年1月14日
     * @since 1.0
     */
    private void startAnimation() {
        mAnimating = true;
        mView.clearAnimation();
        TranslateAnimation animation = null;
        final View handle = mHandle;
        int handleLeft = handle.getLeft();
        //动画持续时间系数，根据抽屉位置确定持续时间
        float coefficient = 1.0f;
        if (mExpanded) {
            animation = new TranslateAnimation(handle.getLeft(), 0, 0, 0);
            if (handleLeft != mViewWidth - mHandleWidth) {
                coefficient = (float) handleLeft / mViewWidth;
            }
            animation.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mAnimating = false;
                }
            });
        } else {
            if (handleLeft != mOffSet) {
                coefficient = 1 - (float) (handleLeft) / mViewWidth;
            }
            mExpanded = true;
            animation = new TranslateAnimation(handle.getLeft(), mViewWidth - mHandleWidth, 0, 0);
            handle.offsetLeftAndRight(mOffSet - handle.getLeft());
            // 关闭抽屉，设置动画监听，动画完成后，去除Content
            animation.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    final View handle = mHandle;
                    mExpanded = false;
                    handle.offsetLeftAndRight(mViewWidth - mHandleWidth - handle.getLeft());
                    // mView.invalidate();
                    mAnimating = false;
                }
            });
        }
        animation.setDuration((long) (ANIMATION_DURATION_TIME * Math.abs(coefficient)));
        mView.setAnimation(animation);
        animation.start();
    }
}