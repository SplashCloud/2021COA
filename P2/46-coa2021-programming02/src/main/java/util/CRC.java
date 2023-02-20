package util;

public class CRC {

    /**
     * CRC计算器
     *
     * @return CheckCode
     */
    private static char[] XOR(String polynomial, StringBuffer temp)
    {
        //每次异或的过程,返回每次异或得到的K位余数
        char[] res = new char[temp.length() - 1];
        for(int i = 1; i < polynomial.length(); ++i)
            res[i - 1] = (polynomial.charAt(i) == temp.charAt(i)) ? '0' : '1';
        //System.out.println(temp + " % " + polynomial + " = " + String.valueOf(res));
        return res;
    }

    private static StringBuffer zeroStr(String polynomial)
    {
        //生成K位0串
        StringBuffer zeroString = new StringBuffer();
        while(zeroString.length() < polynomial.length()) zeroString.append('0');
        return zeroString;
    }

    private static char[] tureCal(StringBuffer dataStrBuff, String polynomial)
    {
        int index = 0;

        //K位数组存放每次异或后的余数，最后一次得到的余数就是校验码 CheckCode
        char [] CheckCode = new char[polynomial.length() - 1];
        //第一次，将M位数据的前K位存进数组中
        for(; index < CheckCode.length; ++index)
            CheckCode[index] = dataStrBuff.charAt(index);

        for(;index < dataStrBuff.length(); ++index)
        {
            //每次取M位数据的下一位与之前的余数组合，进行下一次异或
            StringBuffer temp = new StringBuffer(String.valueOf(CheckCode));//不能用Array.toString(CheckCode);得到的是[,,]
            temp.append(dataStrBuff.charAt(index));

            //判断temp首位是否为‘0’，若是和0串作异或，否则和polynomial作异或
            if(temp.charAt(0) == '0') CheckCode = XOR(zeroStr(polynomial).toString(),temp);
            else CheckCode = XOR(polynomial,temp);
        }

        return CheckCode;
    }

    public static char[] Calculate(char[] data, String polynomial) {
        //M位数据补上K位0串得到被除数
        StringBuffer dataStrBuff = new StringBuffer(String.valueOf(data));
        for(int i = 1; i < polynomial.length(); ++i) dataStrBuff.append('0');

        return tureCal(dataStrBuff,polynomial);
    }

    /**
     * CRC校验器
     *
     * @param data       接收方接受的数据流
     * @param polynomial 多项式
     * @param CheckCode  CheckCode
     * @return 余数
     */
    public static char[] Check(char[] data, String polynomial, char[] CheckCode) {
        //M位数据补上K位校验码得到被除数
        StringBuffer dataStrBuff = new StringBuffer(String.valueOf(data));
        dataStrBuff.append(String.valueOf(CheckCode));

        return tureCal(dataStrBuff,polynomial);
    }

}
