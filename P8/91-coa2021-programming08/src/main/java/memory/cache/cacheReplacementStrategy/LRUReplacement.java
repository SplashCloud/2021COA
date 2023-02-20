package memory.cache.cacheReplacementStrategy;

import memory.Memory;
import memory.cache.Cache;
import util.Transformer;

import javax.security.auth.callback.CallbackHandler;
import java.sql.Time;
import java.util.concurrent.Callable;

/**
 * TODO 最近最少用算法
 */
public class LRUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        Cache.getCache().setTimeStamp(rowNO, System.currentTimeMillis());
    }

    @Override
    public int replace(int start, int end, char[] addrTag, char[] input) {
        long min_timeStamp = Cache.getCache().getTimeStamp(start);
        int rowNo = start;
        for(int i = start; i < end; ++i)
        {
            if(!Cache.getCache().getValidBit(i)) {rowNo = i;break;}
            long timeStamp = Cache.getCache().getTimeStamp(i);
            if(timeStamp < min_timeStamp)
            {
                min_timeStamp = timeStamp;
                rowNo = i;
            }
        }
        if(Cache.getCache().getValidBit(rowNo) && Cache.getCache().getDirtyBit(rowNo) && Cache.isWriteBack)
        {
            write(rowNo);
            Cache.getCache().setDirtyBit(rowNo,false);
            Cache.getCache().setValidBit(rowNo,true);
        }
        Cache.getCache().setTag(rowNo,addrTag);
        Cache.getCache().setData(rowNo,input);
        Cache.getCache().setValidBit(rowNo,true);
        Cache.getCache().setTimeStamp(rowNo,System.currentTimeMillis());
        //System.out.println(rowNo + "   " + new Transformer().charArrayToString(addrTag));
        return rowNo;
    }

    private void write(int rowNo)
    {
        String addr = Cache.getCache().calculate_addr(rowNo);
        Memory.getMemory().write(addr,Cache.LINE_SIZE_B,Cache.getCache().getData(rowNo));
        //System.out.println(Cache.getCache().getData(rowNo));
        //System.out.println(addr);
    }

}





























