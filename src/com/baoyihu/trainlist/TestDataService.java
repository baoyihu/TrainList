package com.baoyihu.trainlist;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.util.SparseArray;

public class TestDataService
{
    
    private static SparseArray<List<PlayBill>> billMap = new SparseArray<List<PlayBill>>();
    
    static Random random = null;
    
    public static void init(int count, long timeFeture)
    {
        g_sequence = 10000;
        billMap.clear();
        random = new Random();
        
        long timeBegin = new Date().getTime();
        timeBegin = timeBegin / TestMainActivity.MILLISECOND_PER_PIX * TestMainActivity.MILLISECOND_PER_PIX;
        long timeEnd = timeBegin + timeFeture;
        for (int iLoop = 0; iLoop < count; iLoop++)
        {
            List<PlayBill> list = new ArrayList<PlayBill>();
            
            PlayBill billTmp = null;
            long timeTmp = timeBegin;
            while ((billTmp = getNewPlayBill(iLoop, timeTmp, timeEnd)) != null)
            {
                list.add(billTmp);
                timeTmp = billTmp.end;
            }
            billMap.put(iLoop, list);
        }
        
    }
    
    private static PlayBill getNewPlayBill(int channelId, long timeBeginMilli, long timeEndMilli)
    {
        PlayBill ret = null;
        ret = new PlayBill();
        ret.chanelId = channelId;
        ret.playBillId = getSequence();
        ret.name = getRandomName(random, 5);
        ret.begin = timeBeginMilli;
        ret.end = timeBeginMilli + (random.nextInt(200) + 100) * TestMainActivity.MILLISECOND_PER_PIX;
        // 100pix base; 200pix variable
        //   Log.i("TestDataService", "getNewPlayBill begin:" + ret.begin + "   end:" + ret.end);
        if (ret.end < timeEndMilli)
        {
            return ret;
        }
        else
        {
            return null;
        }
        
    }
    
    private static int g_sequence = 10000;
    
    private static int getSequence()
    {
        return g_sequence++;
    }
    
    public static PlayBill getPlayBill(int channelId, long timeMinute)
    {
        //   Log.i("TestDataService", "getPlayBill at:" + timeMinute);
        List<PlayBill> list = billMap.get(channelId);
        for (PlayBill bill : list)
        {
            if (bill.begin <= timeMinute && bill.end > timeMinute)
            {
                return bill;
            }
        }
        return null;
    }
    
    private static String getRandomName(Random random, int nameSize)
    {
        StringBuilder builder = new StringBuilder();
        for (int iLoop = 0; iLoop < nameSize; iLoop++)
        {
            builder.append((random.nextInt(52) + "A"));
        }
        return builder.toString();
    }
}

class PlayBill
{
    int chanelId;
    
    int playBillId;
    
    String name;
    
    String Content;
    
    /**long time in milliSecond*/
    long begin;
    
    /**long time in milliSecond*/
    long end;
    
    String pictureAddress;
    
    @Override
    public String toString()
    {
        
        return "id:" + playBillId + "left:" + begin;
    }
}