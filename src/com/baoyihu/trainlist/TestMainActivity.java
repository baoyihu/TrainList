package com.baoyihu.trainlist;

import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baoyihu.trainlist.ChannelAdapter.TrainAdapter;
import com.baoyihu.trainlist.ChannelView.HorizonScrollChange;
import com.baoyihu.trainlist.ChannelView.MoveDiretcion;
import com.baoyihu.trainlist.ChannelView.MovePosition;
import com.baoyihu.trainlist.ChannelView.VerticalScrollChange;

public class TestMainActivity extends Activity
{
    
    private final int rowMax = 100;
    
    public static final long MILLISECOND_PER_PIX = 6000;
    
    private final long endFeatureMilli = 1 * 24 * 3600 * 1000;
    
    private final long postionMax = endFeatureMilli / MILLISECOND_PER_PIX;
    
    private static final String TAG = "MainActivity";
    
    LinearLayout mainLayout = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout = (LinearLayout)findViewById(R.id.mainLayout);
        zeroDateTime = new Date().getTime();
        zeroDateTime = zeroDateTime / MILLISECOND_PER_PIX * MILLISECOND_PER_PIX;
        TestDataService.init(rowMax, endFeatureMilli);
        mainLayout.post(new Runnable()
        {
            @Override
            public void run()
            {
                ChannelView program = new ChannelView(mainLayout.getContext(), mainLayout);
                mainLayout.addView(program);
                program.setAdapter(testAdapter);
                program.addVerticalListener(new VerticalScrollChange()
                {
                    
                    @Override
                    public void onVScrollStart()
                    {
                        //   Log.i(TAG, "onVScrollStart");
                    }
                    
                    @Override
                    public void onVScrollEnd(MovePosition position)
                    {
                        //  Log.i(TAG, "onVScrollEnd postion:" + position);
                    }
                    
                    @Override
                    public void onVScrollChange(int yPos, MoveDiretcion direction)
                    {
                        // Log.i(TAG, "onVScrollChange:" + yPos + "  direction:" + direction);
                    }
                });
                
                program.addHorizonListener(new HorizonScrollChange()
                {
                    
                    @Override
                    public void onHScrollStart()
                    {
                        //   Log.i(TAG, "onHScrollStart");
                    }
                    
                    @Override
                    public void onHScrollEnd(MovePosition position)
                    {
                        //  Log.i(TAG, "onHScrollEnd:" + position);
                    }
                    
                    @Override
                    public void onHScrollChange(int x, MoveDiretcion direction)
                    {
                        //  Log.i(TAG, "onHScrollChange:" + x + "  direction:" + direction);
                    }
                });
            }
        });
    }
    
    long zeroDateTime = 0;
    
    private View getNewView1(ViewData data, View arg1, ViewGroup arg2)
    {
        TextView textView = null;
        //      Log.i(TAG, "data.pos:" + data.xPostion);
        if (data.rowIndex < rowMax && data.xPostion < postionMax && data.xPostion >= 0)
        {
            long dd = (long)(data.xPostion * MILLISECOND_PER_PIX);
            PlayBill bill = TestDataService.getPlayBill(data.rowIndex, dd + zeroDateTime);
            //    Log.i(TAG, "bill:" + bill);
            if (bill != null)
            {
                if (arg1 != null)
                {
                    textView = (TextView)arg1;
                }
                else
                {
                    textView = new TextView(TestMainActivity.this);
                    textView.setTextSize(16);
                    textView.setSingleLine();
                }
                int width = (int)((bill.end - bill.begin) / MILLISECOND_PER_PIX);
                FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(width, 80);
                layout.leftMargin = (int)((bill.begin - zeroDateTime) / MILLISECOND_PER_PIX);
                
                if (layout.leftMargin >= 0)
                {
                    layout.topMargin = 0;
                    textView.setLayoutParams(layout);
                    //     Log.i(TAG, "leftMargin:" + layout.leftMargin);
                    textView.setText(bill.name);
                    return textView;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
        else
            return null;
        
    }
    
    private final TrainAdapter testAdapter = new TrainAdapter()
    {
        
        @Override
        public View getGrid(ViewData data, View arg1, ViewGroup arg2)
        {
            return getNewView1(data, arg1, arg2);
        }
        
        @Override
        public int getCount()
        {
            return rowMax;
        }
        
        @Override
        public float getWidth()
        {
            
            return postionMax;
        }
        
    };
}
