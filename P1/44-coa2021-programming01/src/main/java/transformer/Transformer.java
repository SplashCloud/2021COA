package transformer;

import java.beans.beancontext.BeanContext;

import static java.lang.Integer.parseInt;

public class Transformer {
    /**
     * Integer to binaryString
     *
     * @param numStr to be converted
     * @return result
     */
    public String intToBinary(String numStr) {
        //TODO:
        boolean isNeg = numStr.charAt(0) == '-';
        if(isNeg) numStr = numStr.substring(1);

        int num = parseInt(numStr);
        StringBuffer binStr = new StringBuffer();
        while(binStr.length() != 32)
        {
            binStr.append(num % 2);
            num /= 2;
        }

        if(isNeg)
        {
            int lastIdx = binStr.indexOf("1");
            for(int i = lastIdx + 1; i < binStr.length(); i++)
            {
                if(binStr.charAt(i) == '0') binStr.setCharAt(i,'1');
                else binStr.setCharAt(i,'0');
            }
        }

        return binStr.reverse().toString();
    }

    /**
     * BinaryString to Integer
     *
     * @param binStr : Binary string in 2's complement
     * @return :result
     */
    public String binaryToInt(String binStr) {
        //TODO:
        StringBuffer buffer = new StringBuffer(binStr);
        boolean isNeg = buffer.charAt(0) == '1';
        int index = buffer.lastIndexOf("1");
        if(isNeg)
            for(int i = 1; i < index; ++i)
            {
                if(buffer.charAt(i) == '0') buffer.setCharAt(i,'1');
                else buffer.setCharAt(i,'0');
            }

        int sum = 0;
        for(int i = 1; i < buffer.length(); i++) {
            sum += (buffer.charAt(i) - '0') * Math.pow(2, buffer.length() - 1 - i);
        }

        if(isNeg) sum = -1 * sum;

        return (sum+"");
    }
    /**
     * The decimal number to its NBCD code
     * */
    public StringBuffer intToBinary(String numStr, int bits)
    {
        int num = Integer.parseInt(numStr);
        StringBuffer binStr = new StringBuffer();
        while(binStr.length() != bits)
        {
            binStr.append(num % 2);
            num /= 2;
        }
        return binStr.reverse();
    }

    public String decimalToNBCD(String decimalStr) {
        //TODO:
        StringBuffer binStr = new StringBuffer();

        //'+' / '-'
        if(decimalStr.charAt(0) == '-') {
            binStr.append("1101");
            decimalStr = decimalStr.substring(1);
        }
        else binStr.append("1100");

        //add the '0' if binStr's length isn't 32
        for(int j = 0; j < 7 - decimalStr.length(); j++) binStr.append("0000");

        //calculate
        for(int i = 0; i < decimalStr.length(); i++){
            binStr.append(intToBinary(decimalStr.substring(i,i+1),4));
        }
        return binStr.toString();
    }

    /**
     * NBCD code to its decimal number
     * */
    public String binaryToInt(String binStr, int bits)
    {
        int sum = 0;
        for(int i = 0; i < binStr.length(); i++)
        {
            sum += (binStr.charAt(i) - '0') * Math.pow(2,bits - 1 - i);
        }
        return (sum+"");
    }



    public String NBCDToDecimal(String NBCDStr) {
        //TODO:
        String sign = NBCDStr.substring(0,4);
        int sum = 0;
        for(int i = 4; i < NBCDStr.length() ; i += 4)
        {
            String subStr = NBCDStr.substring(i,i+4);
            sum = sum * 10 + Integer.parseInt(binaryToInt(subStr,4));
        }
        return (sign.equals("1100") ? sum + "" : "-" + sum);
    }

    /**
     * Float true value to binaryString  */

    public String signToBinary(float num)
    {
        StringBuffer res = new StringBuffer();
        while(num != 0.0)
        {
            num *= 2;
            if(num >= 1)
            {
                res.append('1');
                num -= 1;
            }
            else res.append('0');
        }
        return res.toString();
    }

    public String floatToBinary(String floatStr) {
        //TODO:
        float num = Float.parseFloat(floatStr);
        //0.0
        if(num == 0.0) return "00000000000000000000000000000000";
        else if(num == -0.0) return "10000000000000000000000000000000";
        //Infinity
        else if(num >= Math.pow(2,128)) return "+Inf";
        else if(num <= -1 * Math.pow(2,128)) return "-Inf";

        StringBuffer res = new StringBuffer();
        //sign bit
        if(num < 0.0) res.append('1');
        else res.append('0');

        num = Math.abs(num);
        //非规格化数
        if(num > 0.0 && num < Math.pow(2,-126))
        {
            res.append("00000000");
            num *= Math.pow(2,126);
            res.append(signToBinary(num));
            while(res.length() < 32) res.append("0");
            return res.toString();
        }
        //规格化数
        for(int i = -126; i <= 127; i++)
        {
            if(num >= Math.pow(2,i) && num < Math.pow(2,i+1))
            {
                res.append(intToBinary((i+127)+"",8));
                num *= Math.pow(2,-1 * i);
                res.append(signToBinary(num - 1));
                while(res.length() < 32) res.append("0");
                return res.toString();
            }
        }
        return "";
    }

    /**
     * Binary code to its float true value
     * */
    public float binToSignificant(String binStr)
    {
        float res = 0;
        for(int i = 0; i < binStr.length(); i++) {
            res += (binStr.charAt(i) - '0') / Math.pow(2,(i+1));
        }
        return res;
    }

    public String binaryToFloat(String binStr) {
        //TODO:
        char sign = binStr.charAt(0);
        String order = binStr.substring(1,9);
        String significant = binStr.substring(9);

        StringBuffer res = new StringBuffer(sign == '0' ? "" : "-");

        int trueOrderNum = 0;
        int OrderNum = Integer.parseInt(binaryToInt(order,8));
        if(OrderNum == 0) trueOrderNum = -126;
        else if(OrderNum != 255) trueOrderNum = OrderNum - 127;

        float num = binToSignificant(significant);
        if(num == 0 && OrderNum == 255) return (sign == '1') ? "-Inf" : "+Inf";
        else if(num != 0 && OrderNum == 255)  return "NaN";
        else if(OrderNum != 0) num += 1;

        res.append(num * Math.pow(2,trueOrderNum));

        return res.toString();
    }

//    public static void main(String[] args)
//    {
//        Transformer tf = new Transformer();
//        System.out.println(tf.floatToBinary("-1712128.0"));
//        System.out.println(tf.binaryToFloat("11001001110100010000000000000000"));
//    }


}
