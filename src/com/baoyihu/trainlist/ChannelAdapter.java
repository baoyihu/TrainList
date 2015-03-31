package com.baoyihu.trainlist;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;

import com.baoyihu.trainlist.ChannelView.HorizonScrollChange;
import com.baoyihu.trainlist.ChannelView.MoveDiretcion;
import com.baoyihu.trainlist.ChannelView.MovePosition;

public class ChannelAdapter extends BaseAdapter implements HorizonScrollChange
{
    public interface TrainAdapter
    {
        View getGrid(ViewData viewData, View arg1, ViewGroup arg2);
        
        int getCount();
        
        float getWidth();
    }
    
    private static final String TAG = "ChannelAdapter";
    
    private TrainAdapter clientAdapter = null;
    
    private final Context context;
    
    private int count = 0;
    
    private float totalWidth = 0;
    
    private int horizonScrollPos = 0;
    
    private ListView channelList = null;
    
    private Rect parentRect = null;
    
    public ChannelAdapter(Context context, ListView view)
    {
        this.context = context;
        this.channelList = view;
    }
    
    public void setAdapter(TrainAdapter adapter)
    {
        clientAdapter = adapter;
    }
    
    public void setCount(int count)
    {
        this.count = count;
    }
    
    public void setTotalWidth(float width)
    {
        totalWidth = width;
    }
    
    @Override
    public int getCount()
    {
        return count;
    }
    
    @Override
    public Object getItem(int arg0)
    {
        
        return null;
    }
    
    @Override
    public long getItemId(int arg0)
    {
        return 0;
    }
    
    @Override
    public View getView(final int rowIndex, View arg1, ViewGroup arg2)
    {
        Log.d(TAG, "getView FrameLayout:" + rowIndex);
        FrameLayout layout;
        if (arg1 != null)
        {
            layout = (FrameLayout)arg1;
            ReuseViewMap.dumpAllChild(layout);
        }
        else
        {
            layout = new FrameLayout(context);
            layout.setLayoutParams(new AbsListView.LayoutParams((int)totalWidth, LayoutParams.WRAP_CONTENT));
            layout.addOnAttachStateChangeListener(onAttachListener);
            layout.setVisibility(View.VISIBLE);
        }
        layout.setTag(new TrainData(rowIndex));
        layout.invalidate();
        iterate(layout, MoveDiretcion.RIGHT_TO_LEFT);
        return layout;
    }
    
    private void iterate(final FrameLayout layout, final MoveDiretcion direction)
    {
        TrainData trainData = (TrainData)layout.getTag();
        
        int rowIndex = trainData.getRowIndex();
        float newPostion = 0;
        float xPos = -10;
        if (direction.equals(MoveDiretcion.RIGHT_TO_LEFT))
        {
            newPostion = trainData.getNextPos();
            if (newPostion < 0)
            {
                xPos = horizonScrollPos + 1;
            }
            else if (newPostion < parentRect.right + horizonScrollPos)
            {
                xPos = newPostion + 1;
            }
        }
        else
        {
            newPostion = trainData.getPrePos();
            if (newPostion < 0)
            {
                xPos = horizonScrollPos - 1;
            }
            else if (newPostion > horizonScrollPos)
            {
                xPos = newPostion - 1;
            }
        }
        
        if (xPos > -10 && clientAdapter != null)
        {
            if (trainData.contains(xPos))
            {
                return;
            }
            
            ViewData viewData = new ViewData(rowIndex, xPos);
            View retView = clientAdapter.getGrid(viewData, ReuseViewMap.reuse(), layout);
            if (retView != null)
            {
                int insertPos = ((FrameLayout.LayoutParams)retView.getLayoutParams()).leftMargin;
                trainData.addView(retView, insertPos + 1);
                layout.addView(retView);
                iterate(layout, direction);
            }
        }
    }
    
    OnAttachStateChangeListener onAttachListener = new OnAttachStateChangeListener()
    {
        
        @Override
        public void onViewDetachedFromWindow(View arg0)
        {
            // it means the listview is gone
        }
        
        @Override
        public void onViewAttachedToWindow(View arg0)
        {
            
        }
    };
    
    public void setRectParen(Rect rect)
    {
        parentRect = rect;
    }
    
    @Override
    public void onHScrollChange(int x, MoveDiretcion diretcion)
    {
        horizonScrollPos = x;
        //  Log.i(TAG, String.format("move to x %d and direction: %s", x, diretcion.toString()));
        int count = channelList.getChildCount();
        for (int iLoop = 0; iLoop < count; iLoop++)
        {
            FrameLayout layout = (FrameLayout)channelList.getChildAt(iLoop);
            iterate(layout, diretcion);
        }
    }
    
    @Override
    public void onHScrollStart()
    {
        //  Log.i(TAG, "onHScrollStart");
    }
    
    @Override
    public void onHScrollEnd(MovePosition position)
    {
        //  Log.i(TAG, "onHScrollEnd: " + position);
    }
    
}

class ReuseViewMap
{
    private static final String TAG = "ReuseViewMap";
    
    private static Set<View> viewSet = new HashSet<View>();
    
    static DisplayMetrics metrics = null;
    
    public static boolean dumpAllChild(ViewGroup group)
    {
        List<View> dumpSet = new ArrayList<View>();
        for (int iLoop = 0; iLoop < group.getChildCount(); iLoop++)
        {
            View view = group.getChildAt(iLoop);
            dumpSet.add(view);
        }
        for (View temp : dumpSet)
        {
            viewSet.add(temp);
            group.removeView(temp);
        }
        
        return true;
    }
    
    public static boolean dumpView(View view, boolean checkRect)
    {
        if (view == null)
        {
            return false;
        }
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)view.getLayoutParams();
        ViewGroup parent = (ViewGroup)view.getParent();
        if (parent == null)
        {
            return false;
        }
        Rect programRect = new Rect();
        parent.getGlobalVisibleRect(programRect);
        
        int[] myPosition = new int[2];
        view.getLocationOnScreen(myPosition);
        if (!checkRect || myPosition[0] + params.width < programRect.left || myPosition[0] > programRect.right)
        {
            viewSet.add(view);
            parent.removeView(view);
            //     Log.i(TAG, "we get gabege view:" + viewSet.size());
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static View reuse()
    {
        Iterator<View> iterator = viewSet.iterator();
        if (iterator.hasNext())
        {
            View view = iterator.next();
            viewSet.remove(view);
            return view;
        }
        else
        {
            return null;
        }
        
    }
}

class TrainData
{
    public int count = -1;
    
    public Deque<Pair<View, Float>> viewList = new ArrayDeque<Pair<View, Float>>();
    
    private static final String TAG = "TrainData";
    
    private final int rowIndex;
    
    public int getRowIndex()
    {
        return rowIndex;
    }
    
    public TrainData(int rowIndex)
    {
        this.rowIndex = rowIndex;
    }
    
    Map<String, String> map;
    
    public float getNextPos()
    {
        if (viewList.size() < 1)
        {
            return -1;
        }
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)viewList.getLast().first.getLayoutParams();
        return params.leftMargin + params.width;
    }
    
    public float getPrePos()
    {
        if (viewList.size() < 1)
        {
            return -1;
        }
        
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)viewList.getFirst().first.getLayoutParams();
        return params.leftMargin;
    }
    
    public boolean contains(float pos)
    {
        boolean ret = false;
        float pre = getPrePos();
        float next = getNextPos();
        if (pre < 0)
        {
            return false;
        }
        //  Log.i(TAG, "contains:" + pos + " pre:" + pre + "  next:" + next);
        if (pos < pre || pos > next)
        {
            ret = false;
        }
        else
        {
            ret = true;
        }
        return ret;
    }
    
    public int getPreWidth()
    {
        if (viewList.size() < 1)
        {
            return 0;
        }
        
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)viewList.getFirst().first.getLayoutParams();
        return params.width;
    }
    
    public void addView(View view, float pos)
    {
        //  Log.i(TAG, "insertView at:" + pos);
        if (pos > getNextPos())
        {
            viewList.addLast(new Pair<View, Float>(view, pos));
            if (ReuseViewMap.dumpView(viewList.getFirst().first, true))
            {
                viewList.removeFirst();
            }
        }
        else
        {
            viewList.addFirst(new Pair<View, Float>(view, pos));
            if (ReuseViewMap.dumpView(viewList.getLast().first, true))
            {
                viewList.removeLast();
            }
        }
        //  printData();
    }
    
    private void printData()
    {
        for (Pair<View, Float> pair : viewList)
        {
            LayoutParams param = (LayoutParams)pair.first.getLayoutParams();
            Log.d(TAG, "view.left " + param.leftMargin + " seconde:" + pair.second);
        }
    }
}

class ViewData
{
    int rowIndex = 0;
    
    float xPostion = 0;
    
    public ViewData(int row, float xPos)
    {
        rowIndex = row;
        
        xPostion = xPos;
    }
    
}