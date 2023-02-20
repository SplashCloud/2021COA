package cpu.instr;

import cpu.CPU_State;
import cpu.alu.ALU;
import cpu.mmu.MMU;
import util.DataType;

public class Add implements Instruction{
    private final MMU mmu = MMU.getMMU();
    private final ALU alu = new ALU();
    private int len = 0;
    private String instr;

    @Override
    public int exec(int opcode){
        if (opcode == 0x05){
            len = 1 + 4;
            instr = String.valueOf(mmu.read(CPU_State.cs.read() + CPU_State.eip.read(),len));
            String imm = MMU.ToBitStream(instr.substring(1,5));
            String dest = CPU_State.eax.read();
            String res = alu.add(new DataType(imm), new DataType(dest)).toString();
            CPU_State.eax.write(res);
        }
        return len;
    }
}
