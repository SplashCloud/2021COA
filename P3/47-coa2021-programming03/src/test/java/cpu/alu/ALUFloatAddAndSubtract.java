package cpu.alu;

import org.junit.Test;
import util.DataType;
import util.Transformer;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import java.util.Random;

public class ALUFloatAddAndSubtract {


    private final ALU alu = new ALU();
    private final Transformer transformer = new Transformer();
    private DataType src;
    private DataType dest;
    private DataType result;
    private Random random = new Random();

    @Test
    public void AddTest1() {
        for(int i = 0 ; i < 100; i++) {
//            double src_float = (random.nextInt(2) -  random.nextFloat()) * random.nextInt();
//            double dest_float = (random.nextInt(2) -  random.nextFloat()) * random.nextInt();
            double src_float = (random.nextInt(2) -  random.nextFloat()) / random.nextInt();
            double dest_float = (random.nextInt(2) -  random.nextFloat()) / random.nextInt();
            double add = src_float + dest_float;
            double subtract = src_float - dest_float;
            System.out.println(src_float);
            System.out.println(dest_float);
            String src_str = transformer.floatToBinary(src_float + "");
            String dest_str = transformer.floatToBinary(dest_float + "");
            src = new DataType(src_str);
            dest = new DataType(dest_str);
            System.out.println(src);
            System.out.println(dest);

            result = alu.float_add(src, dest);
            String add_ans = transformer.floatToBinary(add + "");
            assertEquals(add_ans, result.toString());

            result = alu.float_subtract(src, dest);
            String subtract_ans = transformer.floatToBinary(subtract + "");
            assertEquals(subtract_ans, result.toString());
        }
    }


}
