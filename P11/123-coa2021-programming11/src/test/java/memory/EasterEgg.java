package memory;

import cpu.mmu.MMU;
import memory.cache.Cache;
import memory.disk.Disk;
import memory.tlb.TLB;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class EasterEgg {

    private final MMU mmu = MMU.getMMU();

    private final Disk disk = Disk.getDisk();

    private final MemTestHelper helper = new MemTestHelper();

    @Before
    public void init() {
        Memory.PAGE = true;
        Memory.SEGMENT = true;
        helper.clearAll();
        Memory.timer = true;

        // TODO: 修改以下两个属性
        Cache.isAvailable = true;
        TLB.isAvailable = false;
    }

    @Test
    public void EasterEgg1() {
        int len = 32;
        char[] expect = disk.read("00000000000000000000000000000000", len);
        char[] actual = mmu.read("000000000000000000000000000000000000000000000000", len);
        for (int i = 0; i < 300; i++) {
            actual = mmu.read("000000000000000000000000000000000000000000000000", len);
        }
        assertArrayEquals(expect, actual);
        System.out.println(mmu.get_memory().get_times());
    }

    //          | 启用Cache | 不启用Cache |
    //——————————+——————————+————————————+
    // 启用TLB   | 108ms    |4s805ms(304)|
    //——————————+——————————+————————————+
    // 不启用TLB |9s522ms(604)| 14s217ms |
    //——————————+——————————+————————————+

    // 为什么时间差会那么远？
    // 开了TLB 平均访问主存的次数减少 分析代码可以看出

}
