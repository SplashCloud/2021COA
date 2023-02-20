package util;

public class DataType {

    private final char[] data = new char[32];

    public DataType(String dataStr) {
        int length = dataStr.length();
        if (length == 32) {
            for (int i = 0; i < 32; i++) {
                char temp = dataStr.charAt(i);
                if (temp == '0' || temp == '1') {
                    data[i] = dataStr.charAt(i);
                } else {
                    throw new NumberFormatException(temp + " is not '0' or '1'.");
                }
            }
        } else {
            throw new NumberFormatException(length + " != 32");
        }
    }

    @Override
    public String toString() {
        return String.valueOf(data);
    }

    public void setBit(int index, char val)
    {
        data[index] = val;
    }

    public char getBit(int index)
    {
        return data[index];
    }

    public boolean isZero()
    {
        for(int i = 0; i < data.length; ++i)
        {
            if(data[i] != '0')
                return false;
        }
        return true;
    }
}
