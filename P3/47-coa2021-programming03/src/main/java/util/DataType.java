package util;

public class DataType {

    private final char[] data = new char[32];
    private Transformer tf = new Transformer();

    public DataType(String dataStr) {
        int length = dataStr.length();
        if (length == 32) {
            for (int i = 0; i < 32; i++) {
                char temp = dataStr.charAt(i);
                if (temp == '0' || temp == '1') {
                    data[i] = dataStr.charAt(i);
                } else {
                    throw new NumberFormatException(temp + "is not '0' or '1'.");
                }
            }
        } else {
            throw new NumberFormatException(length + " != 32");
        }
    }

    public DataType reservePlusOne()
    {
        char[] dest = new char[data.length];
        //find the first '1' from left to right
        int lastOnePo = data.length - 1;
        for(; lastOnePo >= 0; --lastOnePo)
            if(data[lastOnePo] == '1')
                break;

        for(int i = 0; i < 32; ++i)
        {
            if(i < lastOnePo)
                dest[i] = data[i] == '0' ? '1' : '0';
            else
                dest[i] = data[i];
        }

        return new DataType(String.valueOf(dest));
    }

    public int[] convertIntArray()
    {
        int[] res = new int[32];
        for(int i = 0; i < 32; ++i)
        {
            res[i] = data[i] - '0';
        }
        return res;
    }

    public boolean isZero()
    {
        boolean flag = true;
        for(int i = 0; i < 32; ++i)
        {
            if(data[i] == '1')
                flag = false;
        }
        return flag;
    }

    public int[] extendedArray()
    {
        int[] res = new int[33];
        res[0] = data[0] - '0';
        for(int i = 1; i < 33; ++i)
        {
            res[i] = data[i - 1] - '0';
        }
        return res;
    }

    public int[] floatExponent()
    {
        int[] expo = new int[8];
        for(int i = 1; i < 9; ++i)
        {
            expo[i - 1] = data[i] - '0';
        }
        return expo;
    }

    public int[] floatSignificand()
    {
        int[] significand = new int[32];
        if(tf.isZero(this.floatExponent())) significand[0] = 0;
        else significand[0] = 1;
        int index = 1;
        for (int i = 9; i < 32; ++i)
        {
            significand[index++] = data[i] - '0';
        }
        for(int j = index; j < 32; ++j) significand[j] = 0;
        return significand;
    }

    public void setBit(int index, char val)
    {
        data[index] = val;
    }

    public int getBit(int index)
    {
        return data[index] - '0';
    }

    @Override
    public String toString() {
        return String.valueOf(data);
    }
}
