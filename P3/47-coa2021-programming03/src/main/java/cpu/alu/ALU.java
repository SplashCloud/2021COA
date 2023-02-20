package cpu.alu;

import util.DataType;
import util.Transformer;

import javax.swing.plaf.nimbus.AbstractRegionPainter;
import javax.xml.crypto.Data;
import java.awt.dnd.DropTarget;
import java.util.Arrays;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 */
public class ALU {

    DataType remainderReg;
    Transformer tf = new Transformer();
    int cf;


    private int[] addIntArray(int[] src, int[] dest)
    {
        int[] res = new int[src.length];
        cf = 0;
        for(int i = src.length - 1; i >= 0 ; --i)
        {
            res[i] = cf ^ src[i] ^ dest[i];
            cf = cf & src[i] | cf & dest[i] | dest[i] & src[i];
        }
        return res;
    }

    /**
     * 返回两个二进制整数的和
     * dest + src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType add(DataType src, DataType dest) {
        // TODO
        int[] srcInt = src.convertIntArray();
        int[] destInt = dest.convertIntArray();
        int[] res = addIntArray(srcInt,destInt);

        DataType sum = new DataType(tf.IntArrayToString(res));
        return sum;
    }


    /**
     * 返回两个二进制整数的差
     * dest - src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType sub(DataType src, DataType dest) {
        // TODO
        DataType _src = src.reservePlusOne();
        return add(_src,dest);
    }


    /**
     * 返回两个二进制整数的乘积(结果低位截取后32位)
     * dest * src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType mul(DataType src, DataType dest) {
        //TODO
        int[] P = new int[32];
        for(int i = 0; i < 32; ++i) P[i] = 0;
        int[] srcInt = src.convertIntArray();
        int[] _srcInt = src.reservePlusOne().convertIntArray();
        int[] destInt = dest.convertIntArray();

        int y0 = 0;

        int flag = y0 - destInt[31];
        for(int j = 0; j < 32; ++j)
        {
            //-X or +X
            switch (flag)
            {
                case 1: P = addIntArray(P,srcInt);break;
                case -1: P = addIntArray(P,_srcInt);break;
                default:break;
            }

            //right move
            int P_last = P[31];
            y0 = destInt[31];
            for(int right_move_i = 31; right_move_i >= 1; --right_move_i) {
                P[right_move_i] = P[right_move_i - 1];
                destInt[right_move_i] = destInt[right_move_i - 1];
            }
            P[0] = P[1];
            destInt[0] = P_last;

            //cal Yi - Yi+1
            flag = y0 - destInt[31];
        }

        DataType res = new DataType(tf.IntArrayToString(destInt));
        return res;
    }


    /**
     * 返回两个二进制整数的除法结果
     * 请注意使用不恢复余数除法方式实现
     * dest ÷ src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType div(DataType src, DataType dest) {
        //TODO
        if(src.isZero()) throw new ArithmeticException("Src is zero, can't be divided.");

        int[] new_src = src.extendedArray();
        int[] new_dest = dest.extendedArray();

        boolean isReverse = false;
        if(new_dest[0] == 1)
        {
            isReverse = true;
            new_dest = tf.reversePlusOne(new_dest);
            new_src = tf.reversePlusOne(new_src);
        }

        int sign = new_dest[0];
        int[] _new_src = tf.reversePlusOne(new_src);
        int[] remain = new int[33];
        for(int i = 0; i < 33; ++i)
            remain[i] = new_dest[0];

        int sangBit;
        //1 means plus while 0 means minus
        int sign_flag = remain[0] ^ new_src[0];
        for(int cal_time = 0; cal_time < 34; ++cal_time)
        {
            switch (sign_flag)
            {
                case 0: remain = addIntArray(remain,_new_src);break;
                case 1: remain = addIntArray(remain,new_src);break;
                default:break;
            }

            sangBit = (remain[0] ^ new_src[0]) == 0 ? 1 : 0;
            int destFirstBit = new_dest[0];

            for(int left_move_i = 0; left_move_i < 32; ++left_move_i)
            {
                if(cal_time != 33)
                    remain[left_move_i] = remain[left_move_i + 1];

                new_dest[left_move_i] = new_dest[left_move_i + 1];
            }
            if(cal_time != 33)
                remain[32] = destFirstBit;
            new_dest[32] = sangBit;

            sign_flag = remain[0] ^ new_src[0];
        }


        if(new_dest[0] == 1)
        {
            int[] one = new int[33];
            one[32] = 1;
            for(int i = 0; i < 32; ++i) one[i] = 0;
            new_dest = addIntArray(new_dest,one);
        }

        if(remain[0] != sign)
        {
            if(new_src[0] == sign) remain = addIntArray(remain, new_src);
            else remain = addIntArray(remain, _new_src);
        }

        if(isReverse) remain = tf.reversePlusOne(remain);

        int[] recovery_dest = new int[32];
        int[] recovery_remain = new int[32];

        for(int i = 0; i < 32; ++i)
        {
            recovery_dest[i] = new_dest[i+1];
            recovery_remain[i] = remain[i+1];
        }

        DataType res = new DataType(tf.IntArrayToString(recovery_dest));
        remainderReg = new DataType(tf.IntArrayToString(recovery_remain));
        System.out.println("remain:  " + remainderReg.toString());
        return res;
    }

    public DataType float_add(DataType src, DataType dest)
    {
        return floatAddAndSubtract(src,dest,false);
    }

    //src - dest
    public DataType float_subtract(DataType src, DataType dest)
    {
        return floatAddAndSubtract(src,dest,true);
    }

    //src - dest
    private DataType floatAddAndSubtract(DataType src, DataType dest, boolean isSubtract)
    {

        if(isSubtract) dest.setBit(0,dest.getBit(0) == 0 ? '1' : '0');

        if(src.isZero()) return dest;
        if(dest.isZero()) return src;

        DataType res;

        DataType larger_expo_float =
                (tf.valueOf(tf.IntArrayToString(src.floatExponent()),2) >
                 tf.valueOf(tf.IntArrayToString(dest.floatExponent()),2))
                    ? src : dest;
        DataType smaller_expo_float = larger_expo_float == src ? dest : src;

        int res_sign;

        int[] larger_expo = larger_expo_float.floatExponent();
        if(tf.isZero(larger_expo)) larger_expo = addIntArray(larger_expo,one(8));
        int[] smaller_expo = smaller_expo_float.floatExponent();
        if(tf.isZero(smaller_expo)) smaller_expo = addIntArray(smaller_expo,one(8));

        int[] res_expo = larger_expo;

        int[] larger_significand = larger_expo_float.floatSignificand();
        int[] smaller_significand = smaller_expo_float.floatSignificand();

        //对阶
        while (!tf.isZero(addIntArray(larger_expo,tf.reversePlusOne(smaller_expo))))
        {
            //exponent increment
            smaller_expo = addIntArray(smaller_expo,one(8));
            //significand right move
            right_move(smaller_significand,1,0);
            //judge if significand is zero
            if(tf.isZero(smaller_significand)) return larger_expo_float;
        }

        //add the significand
        int[] res_significand;
        boolean isSameSign = larger_expo_float.getBit(0) == smaller_expo_float.getBit(0);
        if(isSameSign)
        {
            res_significand = addIntArray(larger_significand,smaller_significand);
            res_sign = larger_expo_float.getBit(0);
            if(cf == 1)
            {
                right_move(res_significand,1,1);
                res_expo = addIntArray(res_expo, one(8));
                if(tf.isMaxExpo(res_expo)) return new DataType(res_sign + "1111111100000000000000000000000");
            }
        }
        else
        {
            res_significand = addIntArray(larger_significand,tf.reversePlusOne(smaller_significand));
            if(cf == 1) res_sign = larger_expo_float.getBit(0);
            else
            {
                res_significand = tf.reversePlusOne(res_significand);
                res_sign = smaller_expo_float.getBit(0);
            }
        }
        if(tf.isZero(res_significand)) return new DataType(res_sign + "0000000000000000000000000000000");

        while (res_significand[0] == 0)
        {
            if(tf.isEqual(res_expo,one(8)))
            {
                res_expo = gen_num(0,8);
                break;
            }
            else
            {
                left_move(res_significand,1);
                res_expo = addIntArray(res_expo,tf.reversePlusOne(one(8)));
            }
        }

        StringBuffer sbf = new StringBuffer();
        sbf.append(res_sign).append(tf.IntArrayToString(res_expo)).append(tf.IntArrayToString(res_significand).substring(1,24));
        res = new DataType(sbf.toString());

        return res;
    }

    private DataType floatSubtract(DataType src, DataType dest)
    {
        return null;
    }

    private int[] one(int len)
    {
        int[] one = new int[len];
        one[len - 1] = 1;
        for(int i = 0; i < len - 1; ++i)
        {
            one[i] = 0;
        }
        return one;
    }

    private void right_move(int[] src, int times, int val)
    {
        int len = src.length;
        while (times > 0)
        {
            for(int i = len - 1; i >= 1; --i)
            {
                src[i] = src[i - 1];
            }
            src[0] = val;
            times--;
        }
    }

    private void left_move(int[] src, int times)
    {
        int len = src.length;
        while (times > 0)
        {
            int i = 0;
            for(; i < len - 1; ++i)
            {
                src[i] = src[i+1];
            }
            src[i] = 0;
            times--;
        }
    }

    public int[] gen_num(int val, int len)
    {
        int[] res = new int[len];
        for(int i = 0; i < len; ++i)
        {
            res[i] = val % 2;
            val /= 2;
        }
        return res;
    }

    public static void main(String[] args)
    {
        Transformer tf = new Transformer();
        ALU alu = new ALU();
        System.out.println(alu.div(new DataType(tf.intToBinary("-2037792329")),
                new DataType(tf.intToBinary("606511987"))));
        System.out.println(alu.remainderReg);
    }
}
