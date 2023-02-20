package memory.cache.cacheReplacementStrategy;

import memory.Memory;
import memory.cache.Cache;
import util.Transformer;

import javax.security.auth.callback.CallbackHandler;

/**
 * TODO 最近不经常使用算法
 */
public class LFUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        Cache.getCache().addVisitedTimes(rowNO);
    }

    @Override
    public int replace(int start, int end, char[] addrTag, char[] input) {
        int min_visited = Cache.getCache().getVisitedTimes(start);
        int rowNo = start;
        for(int i = start; i < end; ++i)
        {
            if(!Cache.getCache().getValidBit(i)) {rowNo = i;break;}
            int visited = Cache.getCache().getVisitedTimes(i);
            if(visited < min_visited)
            {
                min_visited = visited;
                rowNo = i;
            }
        }
        if(Cache.getCache().getValidBit(rowNo) && Cache.getCache().getDirtyBit(rowNo) && Cache.isWriteBack)
        {
            write(rowNo);
            Cache.getCache().setDirtyBit(rowNo,false);
            Cache.getCache().setValidBit(rowNo,true);
        }
        Cache.getCache().setData(rowNo,input);
        Cache.getCache().setTag(rowNo,addrTag);
        Cache.getCache().setValidBit(rowNo,true);
        Cache.getCache().resetVisitedTime(rowNo);
        //System.out.println(rowNo + "  " + new Transformer().charArrayToString(addrTag));
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
