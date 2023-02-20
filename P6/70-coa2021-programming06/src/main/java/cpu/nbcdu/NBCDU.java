package cpu.nbcdu;

import cpu.alu.ALU;
import util.DataType;
import util.IEEE754Float;
import util.Transformer;

import javax.xml.transform.sax.SAXResult;
import java.util.Locale;
import java.util.Random;

public class NBCDU {

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
     * @param src  A 32-bits NBCD String
     * @param dest A 32-bits NBCD String
     * @return dest + src
     */
    DataType add(DataType src, DataType dest) {
        boolean isSub = src.toString().charAt(3) != dest.toString().charAt(3);
        return new DataType(add_sub_func(src.toString(),dest.toString(),isSub));
    }

    /***
     *
     * @param src A 32-bits NBCD String
     * @param dest A 32-bits NBCD String
     * @return dest - src
     */
    DataType sub(DataType src, DataType dest) {
        boolean isSub = src.toString().charAt(3) == dest.toString().charAt(3);
        return new DataType(add_sub_func(src.toString(),dest.toString(),isSub));
    }

    private String add_sub_func(String src, String dest, boolean isSub)
    {
        //System.out.println("dest: " + dest);
        //System.out.println("src: " + src);
        if(isSub)
        {
            src = src.substring(0,4) + reverse(src.substring(4));
            src = IntArrayToString(addIntArray(StringToIntArray(src),StringToIntArray(toBinary(1,32))));
        }

        //add
        StringBuffer ans = new StringBuffer(dest.substring(0,4));
        src = src.substring(4);
        dest = dest.substring(4);

        String result = add_pro(src,dest);
        //System.out.println("res: "+result);
        if(isSub && cf == 0)
        {
            ans.replace(3,4, ans.charAt(3) == '1' ? "0" : "1");
            result = reverse(result);
            result = add_pro(result,toBinary(1,28));
        }
        //System.out.println(result);
        String re = ans.append(result).toString();
        //System.out.println("res: " + re);
        if(re.substring(4).equals(toBinary(0,28))) return "11000000000000000000000000000000";
        return re;
    }

    private String add_pro(String src, String dest)
    {
        //the sum of number
        int[] res = new int[28];
        //carry bit
        cf = 0;
        int carry = 0;
        //System.out.println(dest);
        //System.out.println(src);
        for(int i = 24; i >= 0; i -= 4)
        {
            int[] src_tmp = StringToIntArray(src.substring(i,i+4));
            int[] dest_tmp = StringToIntArray(dest.substring(i,i+4));
            dest_tmp = addIntArray(dest_tmp,StringToIntArray(toBinary(carry,4)));
            int[] tmp_res = addIntArray(src_tmp,dest_tmp);
            //System.out.println(IntArrayToString(tmp_res));
            carry = cf;
            int is_add_0110 = cf | (tmp_res[0] & tmp_res[1]) | (tmp_res[0] & tmp_res[2]);
            //System.out.println("isSub: "+is_add_0110);
            if(is_add_0110 == 1) tmp_res = addIntArray(tmp_res,StringToIntArray("0110"));
            //System.out.println(IntArrayToString(tmp_res));
            carry = carry | cf;
            //System.out.println("carry: " + carry);
            for (int j = i; j < i + 4; ++j)
            {
                res[j] = tmp_res[j - i];
//                if(i == 24)
//                    System.out.print(res[j] + " ");
            }
            //System.out.println();
        }
        String result = IntArrayToString(res);
        cf = carry;
        return result;
    }

    public String IntArrayToString(int[] src)
    {
        StringBuffer str = new StringBuffer();
        for(int i = 0; i < src.length; ++i)
        {
            str.append(src[i]);
        }
        return str.toString();
    }


    public int[] StringToIntArray(String num)
    {
        int len = num.length();
        int[] res = new int[len];
        for(int i = 0; i < len; ++i)
        {
            res[i] = num.charAt(i) - '0';
        }
        return res;
    }

    private String reverse(String src)
    {
        StringBuffer res = new StringBuffer();
        for(int i = 0; i < 28; i += 4)
            res.append(toBinary(9 - value(src.substring(i,i+4)), 4));
        return res.toString();
    }

    private int value(String num)
    {
        int len = num.length();
        int val = 0;
        for(int i = 0; i < len; ++i)
            val = val * 2 + (num.charAt(i) - '0');
        return val;
    }

    private String toBinary(int num, int len)
    {
        StringBuffer s = new StringBuffer();
        while (num != 0)
        {
            s.append(num % 2);
            num /= 2;
        }
        while (s.length() < len)
            s.append("0");
        if(s.length() > len)
            return s.substring(0,len);
        return s.reverse().toString();
    }
//
//    public static void main(String[] args)
//    {
//        NBCDU nbcdu = new NBCDU();
//        Transformer tf = new Transformer();
//        Random ran = new Random();
//
//        for(int i = 0; i < 1; ++i)
//        {
////            int a = (ran.nextInt() % 10000);
////            int b = (ran.nextInt() % 10000);
//            int a = -0;
//            int b = 0;
//            String dest = tf.decimalToNBCD(a+"");
//            String src = tf.decimalToNBCD(b+"");
//            int sum = a - b;
//            String my_ans = nbcdu.add(new DataType("11000000000000000000000000000000"),
//                    new DataType("11010000000000000000000000000000")).toString();
//            if(!tf.decimalToNBCD(sum+"").equals(my_ans))
//            {
//                System.out.println(a + " + " + b + " = " + sum);
//                System.out.println(dest);
//                System.out.println(src);
//                System.out.println(tf.decimalToNBCD(sum+""));
//                System.out.println(my_ans);
//            }
//        }
//        System.out.println("All cases end!");
//    }
}
