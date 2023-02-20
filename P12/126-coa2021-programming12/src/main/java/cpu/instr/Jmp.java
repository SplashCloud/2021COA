package cpu.instr;

import cpu.CPU;
import cpu.CPU_State;
import cpu.alu.ALU;
import cpu.mmu.MMU;
import util.DataType;

public class Jmp implements Instruction{
    private final MMU mmu = MMU.getMMU();
    private int len = 0;
    private String instr;
    private final ALU alu = new ALU();

    @Override
    public int exec(int opcode){
        if(opcode == 0xe9) {
            len = 1 + 4;
            instr = String.valueOf(mmu.read(CPU_State.cs.read() + CPU_State.eip.read(), len));
            String rel = MMU.ToBitStream(instr.substring(1, 5));
            String eip_value = CPU_State.eip.read();
            String new_value = alu.add(new DataType(rel), new DataType(eip_value)).toString();
            CPU_State.eip.write(new_value);
        }
        return len;
    }
}
