package com.hrules.viewscrollhelper;


import android.animation.TimeInterpolator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class ViewScrollHelper {
    private static final String TAG = "ViewScrollHelper";

    private static final float DEFAULT_HIDE_THRESHOLD = 10;
    private static final float DEFAULT_SHOW_THRESHOLD = 70;

    private float hideThreshold;
    private float showThreshold;

    private View view;
    private int viewHeight;
    private int viewOffset;
    private boolean viewVisible;
    private int viewScrolledDistance;

    private final RecyclerView recyclerView;

    private boolean activated;

    private TimeInterpolator showInterpolator;
    private TimeInterpolator hideInterpolator;

    private OnViewScrollVisibilityChanged listener;

    public ViewScrollHelper(RecyclerView recyclerView, View view) {
        init(view);

        this.recyclerView = recyclerView;
        this.recyclerView.setPadding(recyclerView.getPaddingLeft(), recyclerView.getPaddingTop() + viewHeight, recyclerView.getPaddingRight(), recyclerView.getPaddingBottom());
        this.recyclerView.setOnScrollListener(recyclerViewScrollListener);
    }

    public ViewScrollHelper(RecyclerView recyclerView, View view, OnViewScrollVisibilityChanged onViewScrollVisibilityChanged) {
        this(recyclerView, view);
        setListener(onViewScrollVisibilityChanged);
    }

    private void init(View view) {
        if (view == null) {
            throw new IllegalArgumentException("View must not be null");
        }

        activated = true;

        this.view = view;
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        viewHeight = this.view.getMeasuredHeight();
        viewOffset = 0;
        viewVisible = true;
        viewScrolledDistance = 0;

        hideThreshold = DEFAULT_HIDE_THRESHOLD;
        showThreshold = DEFAULT_SHOW_THRESHOLD;

        showInterpolator = new DecelerateInterpolator(2);
        hideInterpolator = new AccelerateInterpolator(2);
    }

    private void setListener(OnViewScrollVisibilityChanged listener) {
        this.listener = listener;
    }

    private final RecyclerView.OnScrollListener recyclerViewScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newScrollState) {
            super.onScrollStateChanged(recyclerView, newScrollState);
            if (activated && newScrollState == RecyclerView.SCROLL_STATE_IDLE) {
                checkScrollStateChanged();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (activated) {
                checkOnScrolled(dy);
            }
        }
    };

    private void checkOnScrolled(int dy) {
        clipViewOffset();

        view.setTranslationY(-viewOffset);
        notifyOnMove(viewOffset);

        if ((viewOffset < viewHeight && dy > 0) || (viewOffset > 0 && dy < 0)) {
            viewOffset += dy;
        }
        if (viewScrolledDistance < 0) {
            viewScrolledDistance = 0;
        } else {
            viewScrolledDistance += dy;
        }
    }

    private void checkScrollStateChanged() {
        if (viewScrolledDistance < viewHeight) {
            setVisible();
        } else {
            if (viewVisible) {
                if (viewOffset > hideThreshold) {
                    setInvisible();
                } else {
                    setVisible();
                }
            } else {
                if ((viewHeight - viewOffset) > showThreshold) {
                    setVisible();
                } else {
                    setInvisible();
                }
            }
        }
    }

    private void clipViewOffset() {
        if (viewOffset > viewHeight) {
            viewOffset = viewHeight;
            notifyOnHide();
        } else if (viewOffset < 0) {
            viewOffset = 0;
            notifyOnShow();
        }
    }

    private void setVisible() {
        if (viewOffset > 0) {
            view.animate().translationY(0).setInterpolator(showInterpolator).start();
            notifyOnShow();
            viewOffset = 0;
        }
        viewVisible = true;
    }

    private void setInvisible() {
        if (viewOffset < viewHeight) {
            view.animate().translationY(-viewHeight).setInterpolator(hideInterpolator).start();
            notifyOnHide();
            viewOffset = viewHeight;
        }
        viewVisible = false;
    }

    public void show() {
        if (activated && !viewVisible) {
            recyclerView.smoothScrollBy(0, -viewHeight);
        }
    }

    public void hide() {
        if (activated && viewVisible) {
            recyclerView.smoothScrollBy(0, viewHeight);
        }
    }

    public void setViewToHide(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int customViewHeight = view.getMeasuredHeight();
        if (customViewHeight > viewHeight) {
            throw new IllegalArgumentException("View height must be greater than main View");
        }
        viewHeight = customViewHeight;
    }

    public void setViewToHideHeight(int height) {
        if (height > viewHeight) {
            throw new IllegalArgumentException("View height must be greater than main View");
        }
        viewHeight = height;
    }

    private void notifyOnHide() {
        if (listener != null) {
            listener.onHide();
        }
    }

    private void notifyOnShow() {
        if (listener != null) {
            listener.onShow();
        }
    }

    private void notifyOnMove(int distance) {
        if (listener != null) {
            listener.onMove(distance);
        }
    }

    public void setHideThreshold(float hideThreshold) {
        this.hideThreshold = hideThreshold;
    }

    public void setShowThreshold(float showThreshold) {
        this.showThreshold = showThreshold;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isActivated() {
        return activated;
    }

    public float getHideThreshold() {
        return hideThreshold;
    }

    public float getShowThreshold() {
        return showThreshold;
    }

    public TimeInterpolator getShowInterpolator() {
        return showInterpolator;
    }

    public void setShowInterpolator(TimeInterpolator showInterpolator) {
        this.showInterpolator = showInterpolator;
    }

    public TimeInterpolator getHideInterpolator() {
        return hideInterpolator;
    }

    public void setHideInterpolator(TimeInterpolator hideInterpolator) {
        this.hideInterpolator = hideInterpolator;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }
}
