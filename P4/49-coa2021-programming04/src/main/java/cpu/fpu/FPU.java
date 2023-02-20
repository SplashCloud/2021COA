package cpu.fpu;

import cpu.alu.ALU;
import util.DataType;
import util.IEEE754Float;
import util.Transformer;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用3位保护位进行计算
 */
public class FPU {

    private ALU alu = new ALU();
    private int cf;
    private Transformer tf = new Transformer();

    private final String[][] addCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN}
    };

    private final String[][] subCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN}
    };

    /**
     * compute the float add of (dest + src)
     */
    public DataType add(DataType src, DataType dest) {
        // TODO
        return float_add_and_subtract(src.toString(),dest.toString(),false);
    }

    /**
     * compute the float add of (dest - src)
     */
    public DataType sub(DataType src, DataType dest) {
        // TODO
        return float_add_and_subtract(dest.toString(),src.toString(),true);
    }

    public DataType float_add_and_subtract(String src, String dest, boolean isSubtract)
    {
        if(isSubtract) dest = alu.negated(dest);

        //NAN
        if(src.matches(IEEE754Float.NaN_Regular) || dest.matches(IEEE754Float.NaN_Regular))
            return new DataType(IEEE754Float.NaN);

        //ZERO or INF
        String corner_res = cornerCheck(isSubtract ? subCorner : addCorner, src, dest);
        if(corner_res != null) return new DataType(corner_res);

        if(dest.matches(IEEE754Float.P_ZERO) || dest.matches(IEEE754Float.N_ZERO)) return new DataType(dest);
        if(src.matches(IEEE754Float.P_ZERO) || src.matches(IEEE754Float.N_ZERO)) return new DataType(src);

        String larger_expo_float = Integer.parseInt(src.substring(1,9),2) > Integer.parseInt(dest.substring(1,9),2) ?
                                        src : dest;
        String smaller_expo_float = larger_expo_float.equals(src) ? dest : src;

        //sign
        char larger_sign = larger_expo_float.charAt(0);
        char smaller_sign = smaller_expo_float.charAt(0);

        char res_sign;

        //expo
        String larger_expo = larger_expo_float.substring(1,9);
        String smaller_expo = smaller_expo_float.substring(1,9);

        //significand
        StringBuffer larger_significand = new StringBuffer();
        StringBuffer smaller_significand = new StringBuffer();

        if(larger_expo.equals("00000000"))
            larger_significand.append("0");
        else
            larger_significand.append("1");

        if(smaller_expo.equals("00000000"))
            smaller_significand.append("0");
        else
            smaller_significand.append("1");

        larger_significand.append(larger_expo_float.substring(9)).append("000");
        smaller_significand.append(smaller_expo_float.substring(9)).append("000");

        if(larger_expo.equals("00000000")) larger_expo = "00000001";
        if(smaller_expo.equals("00000000")) smaller_expo = "00000001";

        String res_expo = larger_expo;
        //System.out.println(res_expo);

        int right_move_times = Integer.parseInt(larger_expo,2) - Integer.parseInt(smaller_expo,2);
        smaller_significand.replace(0,smaller_significand.length(),rightShift(smaller_significand.toString(),right_move_times));
        if(Integer.parseInt(smaller_significand.toString(),2) == 0) return new DataType(larger_expo_float);

        int[] smaller_significand_array = tf.StringToIntArray(smaller_significand.toString());
        int[] larger_significand_array = tf.StringToIntArray(larger_significand.toString());
        int[] res_significand_array;
        String res_significand;

        boolean isSameSign = src.charAt(0) == dest.charAt(0);
        if(isSameSign)
        {
            res_significand = tf.IntArrayToString(addIntArray(larger_significand_array,smaller_significand_array));
            //System.out.println(res_significand);
            res_sign = larger_sign;
            if(cf == 1)
            {
                res_significand = rightShift(res_significand,1);
                res_significand = "1" + res_significand.substring(1);
                res_expo = tf.IntArrayToString(addIntArray(tf.StringToIntArray(res_expo),tf.StringToIntArray("00000001")));
                if(res_expo.equals("11111111")) return new DataType(res_sign == '0' ? IEEE754Float.P_INF : IEEE754Float.N_INF);
            }
            //System.out.println(res_significand);
            //System.out.println(res_expo);
        }
        else
        {
            res_significand_array = addIntArray(larger_significand_array,tf.reversePlusOne(smaller_significand_array));
            if(cf == 1)
            {
                res_sign = larger_sign;
                res_significand = tf.IntArrayToString(res_significand_array);
            }
            else
            {
                res_sign = smaller_sign;
                res_significand = tf.IntArrayToString(tf.reversePlusOne(res_significand_array));
            }
        }
        if(Integer.parseInt(res_significand,2) == 0) return new DataType(IEEE754Float.P_ZERO);

        while (res_significand.charAt(0) == '0')
        {
            if(res_expo.equals("00000001"))
            {
                res_expo = "00000000";
                break;
            }
            else
            {
                res_significand = alu.leftShift(res_significand,1);
                res_expo = tf.IntArrayToString(addIntArray(tf.StringToIntArray(res_expo),
                        tf.reversePlusOne(tf.StringToIntArray(tf.integerRepresentation("1",27)))));
            }
        }

        //System.out.println(res_significand);
        //System.out.println(res_sign+ " " +res_expo+ " " + res_significand);
        return new DataType(round(res_sign,res_expo,res_significand));

    }

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


    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) && oprB.equals(matrix[1])) {
                return matrix[2];
            }
        }
        return null;
    }

    /**
     * right shift a num without considering its sign using its string format
     *
     * @param operand to be moved
     * @param n       moving nums of bits
     * @return after moving
     */
    private String rightShift(String operand, int n) {
        StringBuilder result = new StringBuilder(operand);  //保证位数不变
        boolean sticky = false;
        for (int i = 0; i < n; i++) {
            sticky = sticky || result.toString().endsWith("1");
            result.insert(0, "0");
            result.deleteCharAt(result.length() - 1);
        }
        if (sticky) {
            result.replace(operand.length() - 1, operand.length(), "1");
        }
        return result.substring(0, operand.length());
    }

    /**
     * 对GRS保护位进行舍入
     *
     * @param sign    符号位
     * @param exp     阶码
     * @param sig_grs 带隐藏位和保护位的尾数
     * @return 舍入后的结果
     */
    private String round(char sign, String exp, String sig_grs) {
        int grs = Integer.parseInt(sig_grs.substring(24), 2);
        String sig = sig_grs.substring(0, 24);
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        return sign + exp + sig.substring(sig.length() - 23);
    }

    /**
     * add one to the operand
     *
     * @param operand the operand
     * @return result after adding, the first position means overflow (not equal to the carray to the next) and the remains means the result
     */
    private String oneAdder(String operand) {
        int len = operand.length();
        StringBuilder temp = new StringBuilder(operand);
        temp.reverse();
        int[] num = new int[len];
        for (int i = 0; i < len; i++) num[i] = temp.charAt(i) - '0';  //先转化为反转后对应的int数组
        int bit = 0x0;
        int carry = 0x1;
        char[] res = new char[len];
        for (int i = 0; i < len; i++) {
            bit = num[i] ^ carry;
            carry = num[i] & carry;
            res[i] = (char) ('0' + bit);  //显示转化为char
        }
        String result = new StringBuffer(new String(res)).reverse().toString();
        return "" + (result.charAt(0) == operand.charAt(0) ? '0' : '1') + result;  //注意有进位不等于溢出，溢出要另外判断
    }



}
