package com.baoyihu.trainlist;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.baoyihu.trainlist.ChannelAdapter.TrainAdapter;

public class ChannelView extends HorizontalScrollView
{
    private static final String TAG = "ChannelView";
    
    private static final boolean needDebug = true;
    
    public static final int SCROLL_STOP_EVENT = 9801;
    
    public static final int SCROLL_MOVE_EVENT = 9802;
    
    public interface HorizonScrollChange
    {
        
        public void onHScrollChange(int x, MoveDiretcion direction);
        
        public void onHScrollStart();
        
        public void onHScrollEnd(MovePosition position);
    }
    
    public interface VerticalScrollChange
    {
        public void onVScrollChange(int x, MoveDiretcion direction);
        
        public void onVScrollStart();
        
        public void onVScrollEnd(MovePosition position);
    }
    
    public enum MovePosition
    {
        MIDDLE, HEAD, TAIL
    }
    
    public enum MoveDiretcion
    {
        RIGHT_TO_LEFT, LEFT_TO_RIGHT, UP_TO_DOWN, DOWN_TO_UP
    }
    
    private int vertivalScrollStatus = SCROLL_STOP_EVENT;
    
    private int horizonlScrollStatus = SCROLL_STOP_EVENT;
    
    private final List<HorizonScrollChange> horizonChange = new ArrayList<HorizonScrollChange>();
    
    private final List<VerticalScrollChange> verticalChange = new ArrayList<VerticalScrollChange>();
    
    private ListView channelListView;
    
    private ChannelAdapter channelAdapter;
    
    private int lastYPos = 0;
    
    public ChannelView(Context context, View parentView)
    {
        super(context);
        
        this.parentView = parentView;
        
        channelListView = new ListView(getContext());
        channelAdapter = new ChannelAdapter(getContext(), channelListView);
        postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                initListView();
            }
        }, 10);
    }
    
    private View parentView = null;
    
    public void SetParent(View parentView)
    {
        this.parentView = parentView;
    }
    
    public void setAdapter(TrainAdapter testAdapter)
    {
        channelAdapter.setAdapter(testAdapter);
        channelAdapter.setCount(testAdapter.getCount());
        channelAdapter.setTotalWidth(testAdapter.getWidth());
    }
    
    /**
     * initList when first receive PlayBill Data
     * */
    private void initListView()
    {
        
        Rect programRect = new Rect();
        parentView.getGlobalVisibleRect(programRect);
        channelAdapter.setRectParen(programRect);
        
        channelListView.setAdapter(channelAdapter);
        channelListView.setDivider(null);
        channelListView.setOnScrollListener(verticalScrollListener);
        
        addView(channelListView);
        setHorizontalScrollBarEnabled(false);
        
        addHorizonListener(channelAdapter);
        
        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        if (needDebug)
        {
            channelListView.postDelayed(statusReport, 200);
        }
        
    }
    
    private final Runnable statusReport = new Runnable()
    {
        @Override
        public void run()
        {
            int count = channelListView.getChildCount();
            int total = 0;
            StringBuilder builder = new StringBuilder("channel's children:");
            for (int iLoop = 0; iLoop < count; iLoop++)
            {
                ViewGroup temp = (ViewGroup)channelListView.getChildAt(iLoop);
                builder.append(temp.getChildCount() + ",");
                total += temp.getChildCount();
            }
            builder.append("total:" + total);
            Log.i(TAG, builder.toString());
            channelListView.postDelayed(this, 3000);
        }
    };
    
    OnScrollListener verticalScrollListener = new OnScrollListener()
    {
        @Override
        public void onScrollStateChanged(AbsListView listview, int arg1)
        {
            if (arg1 == SCROLL_STATE_IDLE)
            {
                vertivalScrollStatus = SCROLL_STOP_EVENT;
                
                MovePosition currentPosion = MovePosition.MIDDLE;
                if (!canScrollList(listview, 1))
                {
                    currentPosion = MovePosition.TAIL;
                }
                else if (!canScrollList(listview, -1))
                {
                    currentPosion = MovePosition.HEAD;
                }
                for (VerticalScrollChange change : verticalChange)
                {
                    change.onVScrollEnd(currentPosion);
                }
            }
            else if (vertivalScrollStatus == SCROLL_STOP_EVENT)
            {
                vertivalScrollStatus = SCROLL_MOVE_EVENT;
                for (VerticalScrollChange change : verticalChange)
                {
                    change.onVScrollStart();
                }
            }
        }
        
        @Override
        public void onScroll(AbsListView listview, int arg1, int arg2, int arg3)
        {
            int x = getScrollY(listview);
            if (lastYPos == x)
            {
                return;
            }
            MoveDiretcion direction = lastYPos < x ? MoveDiretcion.DOWN_TO_UP : MoveDiretcion.UP_TO_DOWN;
            for (VerticalScrollChange change : verticalChange)
            {
                change.onVScrollChange(getScrollY(listview), direction);
            }
            lastYPos = x;
        }
    };
    
    public boolean canScrollList(AbsListView listView, int direction)
    {
        final int childCount = listView.getChildCount();
        if (childCount == 0)
        {
            return false;
        }
        
        final int firstPosition = listView.getFirstVisiblePosition();
        int padingBottom = listView.getListPaddingBottom();
        int padingTop = listView.getListPaddingTop();
        if (direction > 0)
        {
            final int lastBottom = listView.getChildAt(childCount - 1).getBottom();
            final int lastPosition = firstPosition + childCount;
            return lastPosition < listView.getChildCount() || lastBottom > getHeight() - padingBottom;
        }
        else
        {
            final int firstTop = listView.getChildAt(0).getTop();
            return firstPosition > 0 || firstTop < padingTop;
        }
    }
    
    private int getScrollY(AbsListView listView)
    {
        View c = listView.getChildAt(0);
        if (c == null)
        {
            return 0;
        }
        
        return listView.getFirstVisiblePosition() * c.getHeight() - c.getTop();
    }
    
    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy)
    {
        super.onScrollChanged(x, y, oldx, oldy);
        
        for (HorizonScrollChange change : horizonChange)
        {
            MoveDiretcion direction = oldx < x ? MoveDiretcion.RIGHT_TO_LEFT : MoveDiretcion.LEFT_TO_RIGHT;
            
            change.onHScrollChange(getScrollX(), direction);
        }
        if (horizonlScrollStatus == SCROLL_STOP_EVENT)
        {
            horizonlScrollStatus = SCROLL_MOVE_EVENT;
            for (HorizonScrollChange change : horizonChange)
            {
                change.onHScrollStart();
            }
        }
        else
        {
            this.removeCallbacks(checkHorizonScollStatus);
            this.postDelayed(checkHorizonScollStatus, 50);
        }
        
    }
    
    Runnable checkHorizonScollStatus = new Runnable()
    {
        
        @Override
        public void run()
        {
            MovePosition position = MovePosition.MIDDLE;
            if (!ChannelView.this.canScrollHorizontally(1))
            {
                position = MovePosition.TAIL;
            }
            else if (!ChannelView.this.canScrollHorizontally(-1))
            {
                position = MovePosition.HEAD;
            }
            for (HorizonScrollChange change : horizonChange)
            {
                change.onHScrollEnd(position);
            }
            
        }
    };
    
    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        
        if (ev.getAction() == MotionEvent.ACTION_MOVE)
        {
            for (HorizonScrollChange change : horizonChange)
            {
                change.onHScrollStart();
            }
        }
        
        return super.onTouchEvent(ev);
    }
    
    public void addHorizonListener(HorizonScrollChange change)
    {
        horizonChange.add(change);
    }
    
    public void addVerticalListener(VerticalScrollChange change)
    {
        verticalChange.add(change);
    }
}
