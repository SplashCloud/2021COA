package cpu.fpu;

import cpu.alu.ALU;
import util.DataType;
import util.IEEE754Float;
import util.Transformer;

import java.util.Arrays;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用3位保护位进行计算
 */
public class FPU {

    ALU alu = new ALU();
    Transformer tf = new Transformer();

    private final String[][] mulCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN}
    };

    private final String[][] divCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
    };


    /**
     * compute the float mul of dest * src
     */
    public DataType mul(DataType src, DataType dest) {
        // TODO
        String src_str = src.toString();
        String dest_str = dest.toString();

        String check_result = cornerCheck(mulCorner,src_str,dest_str);
        if(check_result != null) return new DataType(check_result);

        if(src_str.matches(IEEE754Float.NaN_Regular) || dest_str.matches(IEEE754Float.NaN_Regular))
        {
            return new DataType(IEEE754Float.NaN);
        }

        int src_sign = src_str.charAt(0) - '0';
        int dest_sign = dest_str.charAt(0) - '0';

        int res_sign = src_sign ^ dest_sign;

        if(alu.isZero(src_str.substring(1)) || alu.isZero(dest_str.substring(1)))
        {
            return new DataType( res_sign == 1 ? IEEE754Float.N_ZERO : IEEE754Float.P_ZERO);
        }

        int[] src_expo = tf.StringToIntArray(src_str.substring(1,9));
        int[] dest_expo = tf.StringToIntArray(dest_str.substring(1,9));


        StringBuffer src_significand = new StringBuffer();
        StringBuffer dest_significand = new StringBuffer();

        if(tf.isZero(src_expo)) src_significand.append("0");
        else src_significand.append("1");
        if(tf.isZero(dest_expo)) dest_significand.append("0");
        else dest_significand.append("1");

        if(tf.IntArrayToString(src_expo).equals("00000000"))
            src_expo = alu.addIntArray(src_expo,tf.StringToIntArray("00000001"));
        if(tf.IntArrayToString(dest_expo).equals("00000000"))
            dest_expo = alu.addIntArray(dest_expo,tf.StringToIntArray("00000001"));

        src_significand.append(src_str.substring(9)).append("000");
        dest_significand.append(dest_str.substring(9)).append("000");

        //add expo
        int[] add_expo_res = alu.addIntArray(src_expo,dest_expo);
        boolean overFlow1 = alu.cf == 1;

        int[] true_expo;
        boolean overFlow2 = false;
        boolean underFlow = false;
        true_expo = alu.addIntArray(add_expo_res,tf.reversePlusOne(tf.StringToIntArray("01111111")));
        if(overFlow1)
        {
            overFlow2 = alu.cf == 1;
        }
        else
        {
            underFlow = alu.cf == 0;
        }

        //mul the significand
//        System.out.println(src_significand.toString());
//        System.out.println(dest_significand.toString());
//        System.out.println(Integer.parseInt(src_significand.toString(),2));
//        System.out.println(Integer.parseInt(dest_significand.toString(),2));
        //System.out.println(tf.valueOf(src_significand.toString(),2));
        //System.out.println(tf.valueOf(dest_significand.toString(),2));
        System.out.println(src_significand);
        System.out.println(dest_significand);
        String mul_res = significand_mul(tf.StringToIntArray(src_significand.toString()),tf.StringToIntArray(dest_significand.toString()));
        System.out.println(mul_res);
//        System.out.println(mul_res);
//        System.out.println(Long.parseLong(mul_res,2));
        //System.out.println(tf.valueOf(mul_res,2));
//        if(alu.cf == 1)
//        {
//            mul_res = "1" + rightShift(mul_res,1).substring(1);
//        }
//        System.out.println(mul_res);
//        System.out.println(Long.parseLong("100010010101010000111111110000101111011100000000000000",2));
        //System.out.println(tf.valueOf(mul_res,2));

        //mul_res = rightShift(mul_res,1);
        true_expo = alu.addIntArray(true_expo,tf.StringToIntArray("00000001"));
        if(!underFlow && !overFlow2) if(alu.cf == 1) overFlow2 = true;
        if(underFlow) if(alu.cf == 1) underFlow = false;

        while (underFlow && !alu.isZero(mul_res.substring(0,27)))
        {
            true_expo = alu.addIntArray(true_expo,tf.StringToIntArray("00000001"));
            if(alu.cf == 1) underFlow = false;
            mul_res = rightShift(mul_res,1);
        }

        //System.out.println("expo: " + tf.IntArrayToString(true_expo));
        //System.out.println(underFlow + " " + overFlow2 + " " + overFlow1);
        while (mul_res.charAt(0) == '0' && !underFlow && ( !alu.isZero(tf.IntArrayToString(true_expo)) || overFlow2 ))
        {
            mul_res = alu.leftShift(mul_res,1);
            true_expo = alu.addIntArray(true_expo,tf.reversePlusOne(tf.StringToIntArray("00000001")));
            if(overFlow2) if(tf.IntArrayToString(true_expo).equals("11111111")) overFlow2 = false;
        }

        //System.out.println("expo: " + tf.IntArrayToString(true_expo));

        if(underFlow) return new DataType(res_sign == 1 ? IEEE754Float.N_ZERO : IEEE754Float.P_ZERO);
        if(overFlow2) return new DataType(res_sign == 1 ? IEEE754Float.N_INF : IEEE754Float.P_INF);


        if(alu.isZero(tf.IntArrayToString(true_expo))) mul_res = rightShift(mul_res,1);
        if(alu.isZero(mul_res.substring(0,27))) return new DataType(res_sign == 1 ? IEEE754Float.N_ZERO : IEEE754Float.P_ZERO);

        //System.out.println("expo: " + tf.IntArrayToString(true_expo));
        return new DataType(round(res_sign == 1 ? '1' : '0',tf.IntArrayToString(true_expo),mul_res));
    }


    public String significand_mul(int[] dest_significand, int[] src_significand)
    {
        int len = src_significand.length;
        int[] P = new int[len];
        for(int i = 0; i < len; ++i) P[i] = 0;
        //System.out.println(tf.IntArrayToString(P) + "  " + tf.IntArrayToString(dest_significand));
        for(int j = 0; j < len; ++j)
        {
            int cf = 0;
            if(dest_significand[len-1] == 1)
            {
                P = alu.addIntArray(P,src_significand);
                cf = alu.cf;
            }

            int P_last = P[len-1];
            for(int k = len - 2; k >= 0; --k)
            {
                P[k + 1] = P[k];
                dest_significand[k+1] = dest_significand[k];
            }
            //!!!!!!
            P[0] = cf;
//            System.out.println(cf);
            dest_significand[0] = P_last;
            System.out.println(tf.IntArrayToString(P) + "  " + tf.IntArrayToString(dest_significand));
        }
        return tf.IntArrayToString(P) + tf.IntArrayToString(dest_significand);
    }


    /**
     * compute the float mul of dest / src
     */
    public DataType div(DataType src, DataType dest) {
        // TODO
        String src_str = src.toString();
        String dest_str = dest.toString();

        String check_res = cornerCheck(divCorner,src_str,dest_str);
        if(check_res != null) return new DataType(check_res);

        if(src_str.matches(IEEE754Float.NaN_Regular) || dest_str.matches(IEEE754Float.NaN_Regular))
        {
            return new DataType(IEEE754Float.NaN);
        }

        int dest_sign = dest_str.charAt(0) - '0';
        int src_sign = src_str.charAt(0) - '0';
        int res_sign = dest_sign ^ src_sign;

        if(alu.isZero(src_str.substring(1))) throw new ArithmeticException();
        if(alu.isZero(dest_str.substring(1))) return new DataType(res_sign == 1 ? IEEE754Float.N_ZERO : IEEE754Float.P_ZERO);

        String dest_expo = dest_str.substring(1,9);
        String src_expo = src_str.substring(1,9);

        StringBuffer dest_significand = new StringBuffer("0");
        StringBuffer src_significand = new StringBuffer("0");

        if(alu.isZero(dest_expo))
        {
            dest_significand.append("0");
            dest_expo = tf.IntArrayToString(alu.addIntArray(tf.StringToIntArray(dest_expo),tf.StringToIntArray("00000001")));
        }
        else dest_significand.append("1");
        if(alu.isZero(src_expo))
        {
            src_significand.append("0");
            src_expo = tf.IntArrayToString(alu.addIntArray(tf.StringToIntArray(src_expo),tf.StringToIntArray("00000001")));
        }
        else src_significand.append("1");

        dest_significand.append(dest_str.substring(9)).append("000");
        src_significand.append(src_str.substring(9)).append("000");

        //sub expo
        int[] sub_bias_res = alu.addIntArray(tf.StringToIntArray(dest_expo),tf.reversePlusOne(tf.StringToIntArray(src_expo)));
        boolean underflow1 = alu.cf == 0;

        boolean underflow2 = false;
        boolean overflow = false;
        int[] true_res = alu.addIntArray(sub_bias_res,tf.StringToIntArray("01111111"));
        if(underflow1) underflow2 = alu.cf == 0;
        else overflow = alu.cf == 1;

        //div significand
        String res_significand =
                significand_div(tf.StringToIntArray(dest_significand.toString()),tf.StringToIntArray(src_significand.toString()));

        //System.out.println("significand: " + res_significand);
        if(underflow2) return new DataType(res_sign == 1 ? IEEE754Float.N_ZERO : IEEE754Float.P_ZERO);

        if(alu.isZero(res_significand)) return new DataType(res_sign == 1 ? IEEE754Float.N_ZERO : IEEE754Float.P_ZERO);

        while (res_significand.charAt(0) == '0' && !alu.isZero(tf.IntArrayToString(true_res)))
        {
            res_significand = alu.leftShift(res_significand,1);
            true_res = alu.addIntArray(true_res,tf.reversePlusOne(tf.StringToIntArray("00000001")));
            if(overflow) if(tf.IntArrayToString(true_res).equals("11111111")) overflow = false;
        }

        if(overflow) return new DataType(res_sign == 1 ? IEEE754Float.N_INF : IEEE754Float.P_INF);

        if(res_significand.charAt(0) == '1' && alu.isZero(tf.IntArrayToString(true_res)))
        {
            res_significand = rightShift(res_significand,1);
        }

        return new DataType(round(res_sign == 1 ? '1' : '0', tf.IntArrayToString(true_res),res_significand));
    }

    public String significand_div(int[] dest_significand, int[] src_significand)
    {
//        System.out.println(tf.IntArrayToString(dest_significand));
        System.out.println(tf.IntArrayToString(src_significand) + " " + tf.IntArrayToString(dest_significand));
        int len = dest_significand.length;
        int[] res = new int[len];

        for(int i = 0; i < len; ++i)
        {
            int[] remain = alu.addIntArray(dest_significand,tf.reversePlusOne(src_significand));
            if(alu.cf == 1)
            {
                res[i] = 1;
                dest_significand = tf.StringToIntArray(alu.leftShift(tf.IntArrayToString(remain),1));
            }
            else
            {
                res[i] = 0;
                dest_significand = tf.StringToIntArray(alu.leftShift(tf.IntArrayToString(dest_significand),1));
            }
        }
        System.out.println(tf.IntArrayToString(res).substring(0,27));
        return tf.IntArrayToString(res).substring(0,27);
    }


    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) &&
                    oprB.equals(matrix[1])) {
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
        int grs = Integer.parseInt(sig_grs.substring(24, 27), 2);
        if ((sig_grs.substring(27).contains("1")) && (grs % 2 == 0)) {
            grs++;
        }
        String sig = sig_grs.substring(0, 24); // 隐藏位+23位
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        if (exp.equals("11111111")) {
            return IEEE754Float.P_INF;
        }

        return sign + exp + sig.substring(sig.length() - 23);
    }

    /**
     * add one to the operand
     *
     * @param operand the operand
     * @return result after adding, the first position means overflow (not equal to the carry to the next) and the remains means the result
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
//
    public static void main(String[] args)
    {
        FPU fpu = new FPU();
        Transformer tf = new Transformer();
//        System.out.println(fpu.mul(new DataType(tf.floatToBinary("0.25")),new DataType(tf.floatToBinary("4"))));
//        System.out.println(fpu.mul(new DataType("00000000000000000000000000000001"),
//                new DataType("01111111000000000000000000000001")));
//        System.out.println(fpu.significand_mul(tf.StringToIntArray("111"),tf.StringToIntArray("111")));
//        DataType dest = new DataType("00111111011001100110011001100110");
//        DataType src = new DataType("01001011000110001001011010000000");
//        System.out.println(dest);
//        System.out.println(src);
//        System.out.println(tf.floatToBinary(0.4375 / 0.625 + ""));
//        System.out.println(fpu.mul(src,dest));

//        DataType dest = new DataType(tf.floatToBinary( "0.4375" ));
//        DataType src = new DataType(tf.floatToBinary( "0.5" ));
//        DataType result = fpu.div(src, dest);
//        System.out.println(result);

        System.out.println(tf.binaryToFloat("00000000010000000000000000000000"));
        System.out.println(tf.binaryToFloat("00111111111000000000000000000000"));
        System.out.println(1.75 / 5.8774717541114375E-39);
        System.out.println(
                tf.binaryToFloat(fpu.div(new DataType("00000000010000000000000000000000"),
                        new DataType("00111111111000000000000000000000")).toString()));

    }

//    000000000000000000000001000
//            100000000000000000000001000
//            8
//            67108872
//            000000000000000000000001011000000000000000000001000000
//            1476395072
    //111100001011110000111111110000101111011100000000000000
    //100010010101010000111111110000101111011100000000000000

}
