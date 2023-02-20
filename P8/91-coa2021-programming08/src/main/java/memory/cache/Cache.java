package memory.cache;

import memory.Memory;
import memory.cache.cacheReplacementStrategy.FIFOReplacement;
import memory.cache.cacheReplacementStrategy.ReplacementStrategy;
import util.Transformer;

import javax.sound.sampled.Line;
import java.util.Arrays;

/**
 * 高速缓存抽象类
 */
public class Cache {

    public static final boolean isAvailable = true; // 默认启用Cache

    public static final int CACHE_SIZE_B = 1024 * 1024; // 1 MB 总大小

    public static final int LINE_SIZE_B = 1024; // 1 KB 行大小

    private final CacheLinePool cache = new CacheLinePool(CACHE_SIZE_B / LINE_SIZE_B);  // 总大小1MB / 行大小1KB = 1024个行

    private int SETS;   // 组数

    private int setSize;    // 每组行数

    // 单例模式
    private static final Cache cacheInstance = new Cache();

    private Cache() {
    }

    public static Cache getCache() {
        return cacheInstance;
    }

    private ReplacementStrategy replacementStrategy;    // 替换策略

    public static boolean isWriteBack;   // 写策略

    private final Transformer transformer = new Transformer();


    /**
     * 读取[pAddr, pAddr + len)范围内的连续数据，可能包含多个数据块的内容
     *
     * @param pAddr 数据起始点(32位物理地址 = 22位块号 + 10位块内地址)
     * @param len   待读数据的字节数
     * @return 读取出的数据，以char数组的形式返回
     */
    public char[] read(String pAddr, int len) {
        //System.out.println("pAddr: "+pAddr);
        char[] data = new char[len];
        int addr = Integer.parseInt(transformer.binaryToInt("0" + pAddr));
        int upperBound = addr + len;
        int index = 0;
        while (addr < upperBound) {
            int nextSegLen = LINE_SIZE_B - (addr % LINE_SIZE_B);
            if (addr + nextSegLen >= upperBound) {
                nextSegLen = upperBound - addr;
            }
            int rowNO = fetch(transformer.intToBinary(String.valueOf(addr)));
            char[] cache_data = cache.get(rowNO).getData();
            int i = 0;
            while (i < nextSegLen) {
                data[index] = cache_data[addr % LINE_SIZE_B + i];
                index++;
                i++;
            }
            addr += nextSegLen;
        }
        //System.out.println(transformer.charArrayToString(data));
        return data;
    }

    /**
     * 向cache中写入[pAddr, pAddr + len)范围内的连续数据，可能包含多个数据块的内容
     *
     * @param pAddr 数据起始点(32位物理地址 = 22位块号 + 10位块内地址)
     * @param len   待写数据的字节数
     * @param data  待写数据
     */
    public void write(String pAddr, int len, char[] data) {
        if(!isWriteBack)
        {
            Memory.getMemory().write(pAddr,len,data);
        }
        int addr = Integer.parseInt(transformer.binaryToInt("0" + pAddr));
        int upperBound = addr + len;
        int index = 0;
        while (addr < upperBound) {
            int nextSegLen = LINE_SIZE_B - (addr % LINE_SIZE_B);
            if (addr + nextSegLen >= upperBound) {
                nextSegLen = upperBound - addr;
            }
            int rowNO = fetch(transformer.intToBinary(String.valueOf(addr)));
            char[] cache_data = cache.get(rowNO).getData();
            int i = 0;
            while (i < nextSegLen) {
                cache_data[addr % LINE_SIZE_B + i] = data[index];
                index++;
                i++;
            }

            // TODO
            cache.get(rowNO).validBit = true;
            if(isWriteBack) cache.get(rowNO).dirty = true;

            addr += nextSegLen;
        }
    }

    private int map_way()
    {
        if(SETS == CACHE_SIZE_B / LINE_SIZE_B && setSize == 1)
        {
            //直接映射   12(映射到同一行的不同块)  10(行号)  10(块内地址)
            return 1;
        }
        else if(SETS == 1 && setSize == CACHE_SIZE_B / LINE_SIZE_B)
        {
            //关联映射  22(块号)  10(块内地址)
            return 2;
        }
        else
        {
            //2^n-路组关联映射  22-(10-n) (映射到同一组的不同块)  10-n (组号)  10(块内地址)
            return 3;
        }
    }

    public int setSize_to_n()
    {
        for(int i = 0; ; ++i)
        {
            if(setSize <= Math.pow(2,i)) return i;
        }
    }

    /**
     * 查询{@link Cache#cache}表以确认包含pAddr的数据块是否在cache内
     * 如果目标数据块不在Cache内，则将其从内存加载到Cache
     *
     * @param pAddr 数据起始点(32位物理地址 = 22位块号 + 10位块内地址)
     * @return 数据块在Cache中的对应行号
     */
    private int fetch(String pAddr) {
        // TODO
        //计算块号
        //System.out.println("addr: "+pAddr);
        int n = setSize_to_n();
        int blockNO = SETS * Integer.parseInt(transformer.binaryToInt(pAddr.substring(0,22-(10-n))))
                                + Integer.parseInt(transformer.binaryToInt(pAddr.substring(22-(10-n),22)));

        //返回行号
        //System.out.println("blockNo: "+blockNO);
        int LineNo = map(blockNO);
        //System.out.println(LineNo);
        if(LineNo != -1) return LineNo;

        //读取内存
        //取出tag，若不满22位，低位加'0'
        StringBuffer tag = new StringBuffer(pAddr.substring(0,22-(10-n)));
        while (tag.length() < 22) tag.append("0");
        //System.out.println("new_tag:" + tag);
        char[] new_tag = transformer.StringToCharArray(tag.toString());
        //读取内存块，涉及替换原则了
        int setNo = blockNO % SETS;
        //读取内存地址要注意，读取一块，块内地址为0000000000!!!
        if(setSize == 1) replacementStrategy = new FIFOReplacement();
        return replacementStrategy.replace(setNo * setSize,(setNo+1)*setSize,
                new_tag,Memory.getMemory().read(pAddr.substring(0,22)+"0000000000",LINE_SIZE_B));
    }


    private boolean judge_tag(int blockNo, int LineNo)
    {
        //judge cache[LineNo] is the right tag.
        int block_tag = blockNo / SETS;
        String tag = transformer.charArrayToString(cache.get(LineNo).getTag());
        int cache_tag = Integer.parseInt(transformer.binaryToInt(tag.substring(0,22-(10-setSize_to_n()))));
        //System.out.println(cache_tag + "  " + block_tag);
        return block_tag == cache_tag;
    }

    /**
     * 根据目标数据内存地址前22位的int表示，进行映射
     *
     * @param blockNO 数据在内存中的块号
     * @return 返回cache中所对应的行，-1表示未命中
     */

    private int map(int blockNO) {
        // TODO
        int SetNo = blockNO % SETS;
        int LineNo;
        for(LineNo = setSize * SetNo; LineNo < setSize * (SetNo + 1); ++LineNo)
        {
            //如果tag匹配，还需要看是否有效！！
            //System.out.println(judge_tag(blockNO,LineNo));
            if(judge_tag(blockNO,LineNo) && cache.get(LineNo).validBit)
            {
                if(!(replacementStrategy instanceof FIFOReplacement)) replacementStrategy.hit(LineNo);
                return LineNo;
            }
        }
        return -1;
    }

    /**
     * 更新cache
     *
     * @param rowNO 需要更新的cache行号
     * @param tag   待更新数据的Tag
     * @param input 待更新的数据
     */
    public void update(int rowNO, char[] tag, char[] input) {
        // TODO
        cache.get(rowNO).setData(input);
        cache.get(rowNO).setTag(tag);
        cache.get(rowNO).validBit = true;
    }

    public String calculate_addr(int rowNo)
    {
        StringBuffer addr = new StringBuffer();
        int n = setSize_to_n();
        addr.append(transformer.charArrayToString(cache.get(rowNo).getTag()).substring(0,22-(10-n)));
        addr.append(transformer.intToBinary(rowNo / setSize + "").substring(32-(10-n),32));
        addr.append("0000000000");
        return addr.toString();
    }

    /**
     * 从32位物理地址(22位块号 + 10位块内地址)获取目标数据在内存中对应的块号
     *
     * @param pAddr 32位物理地址
     * @return 数据在内存中的块号
     */
    private int getBlockNO(String pAddr) {
        return Integer.parseInt(transformer.binaryToInt("0" + pAddr.substring(0, 22)));
    }


    /**
     * 该方法会被用于测试，请勿修改
     * 使用策略模式，设置cache的替换策略
     *
     * @param replacementStrategy 替换策略
     */
    public void setReplacementStrategy(ReplacementStrategy replacementStrategy) {
        this.replacementStrategy = replacementStrategy;
    }

    /**
     * 该方法会被用于测试，请勿修改
     *
     * @param SETS 组数
     */
    public void setSETS(int SETS) {
        this.SETS = SETS;
    }

    /**
     * 该方法会被用于测试，请勿修改
     *
     * @param setSize 每组行数
     */
    public void setSetSize(int setSize) {
        this.setSize = setSize;
    }

    /**
     * 告知Cache某个连续地址范围内的数据发生了修改，缓存失效
     * 该方法仅在memory类中使用，请勿修改
     *
     * @param pAddr 发生变化的数据段的起始地址
     * @param len   数据段长度
     */
    public void invalid(String pAddr, int len) {
        int from = getBlockNO(pAddr);
        Transformer t = new Transformer();
        int to = getBlockNO(t.intToBinary(String.valueOf(Integer.parseInt(t.binaryToInt("0" + pAddr)) + len - 1)));

        for (int blockNO = from; blockNO <= to; blockNO++) {
            int rowNO = map(blockNO);
            if (rowNO != -1) {
                cache.get(rowNO).validBit = false;
            }
        }
    }

    /**
     * 清除Cache全部缓存
     * 该方法会被用于测试，请勿修改
     */
    public void clear() {
        for (CacheLine line : cache.clPool) {
            if (line != null) {
                line.validBit = false;
                //valid 权力大于 dirty
                //line.dirty = false;
            }
        }
    }

    /**
     * 输入行号和对应的预期值，判断Cache当前状态是否符合预期
     * 这个方法仅用于测试，请勿修改
     *
     * @param lineNOs     行号
     * @param validations 有效值
     * @param tags        tag
     * @return 判断结果
     */
    public boolean checkStatus(int[] lineNOs, boolean[] validations, char[][] tags) {
        if (lineNOs.length != validations.length || validations.length != tags.length) {
            return false;
        }
        for (int i = 0; i < lineNOs.length; i++) {
            CacheLine line = cache.get(lineNOs[i]);
            if (line.validBit != validations[i]) {
                System.out.println(2);
                return false;
            }
            if (!Arrays.equals(line.getTag(), tags[i])) {
                System.out.println(3);
                System.out.println(transformer.charArrayToString(tags[i]));
                System.out.println(transformer.charArrayToString(line.getTag()));
                return false;
            }
        }
        return true;
    }

    //方便外部访问的方法
    public boolean getValidBit(int rowNo)
    {
        return cache.get(rowNo).validBit;
    }

    public void setValidBit(int rowNo,boolean isValid)
    {
        cache.get(rowNo).validBit = isValid;
    }

    public boolean getDirtyBit(int rowNo)
    {
        return cache.get(rowNo).dirty;
    }

    public void setDirtyBit(int rowNo, boolean isDirty)
    {
        cache.get(rowNo).dirty = isDirty;
    }

    public int getVisitedTimes(int rowNo)
    {
        return cache.get(rowNo).visited;
    }

    public void addVisitedTimes(int rowNo)
    {
        cache.get(rowNo).visited++;
    }

    public void resetVisitedTime(int rowNo)
    {
        cache.get(rowNo).visited = 0;
    }


    public long getTimeStamp(int rowNo)
    {
        return cache.get(rowNo).timeStamp;
    }

    public void setTimeStamp(int rowNo, long new_timeStamp)
    {
        cache.get(rowNo).timeStamp = new_timeStamp;
    }

    public void setData(int rowNo,char[] input)
    {
        cache.get(rowNo).setData(input);
    }

    public void setTag(int rowNo, char[] newTag)
    {
        cache.get(rowNo).setTag(newTag);
    }

    public char[] getData(int rowNo)
    {
        return cache.get(rowNo).getData();
    }



    /**
     * 负责对CacheLine进行动态初始化
     */
    private static class CacheLinePool {

        private final CacheLine[] clPool;

        /**
         * @param lines Cache的总行数
         */
        CacheLinePool(int lines) {
            clPool = new CacheLine[lines];
        }

        private CacheLine get(int rowNO) {
            CacheLine l = clPool[rowNO];
            if (l == null) {
                clPool[rowNO] = new CacheLine();
                l = clPool[rowNO];
            }
            return l;
        }
    }


    /**
     * Cache行，每行长度为(1+22+{@link Cache#LINE_SIZE_B})
     */
    private static class CacheLine {

        // 有效位，标记该条数据是否有效
        boolean validBit = false;

        // 脏位，标记该条数据是否被修改
        boolean dirty = false;

        // 用于LFU算法，记录该条cache使用次数
        int visited = 0;

        // 用于LRU和FIFO算法，记录该条数据时间戳
        Long timeStamp = System.currentTimeMillis();

        // 标记，占位长度为22位，有效长度取决于映射策略：
        // 直接映射: 12 位
        // 全关联映射: 22 位
        // (2^n)-路组关联映射: 22-(10-n) 位
        // 注意，tag在物理地址中用高位表示，如：直接映射(32位)=tag(12位)+行号(10位)+块内地址(10位)，
        // 那么对于值为0b1111的tag应该表示为0000000011110000000000，其中前12位为有效长度
        char[] tag = new char[22];

        // 数据
        char[] data = new char[LINE_SIZE_B];

        char[] getData() {
            return this.data;
        }

        char[] getTag() {
            return this.tag;
        }

        void setData(char[] new_data){this.data = new_data;}

        void setTag(char[] new_tag){this.tag = new_tag;}

    }
}
