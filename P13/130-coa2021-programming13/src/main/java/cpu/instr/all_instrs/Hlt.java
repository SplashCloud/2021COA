package cpu.instr.all_instrs;

import cpu.CPU;
import cpu.CPU_State;
import cpu.MMU;
import cpu.registers.CS;

import java.util.concurrent.Callable;

public class Hlt implements Instruction{

    private MMU mmu = MMU.getMMU();
    private CS cs = (CS) CPU_State.cs;
    private int len;
    private String instr;

    @Override
    public int exec(int opcode) {
        CPU_State.ICC = 3;
        return len;
    }

    @Override
    public String fetchInstr(String eip, int opcode) {
        len = 8 + 32;
        instr = String.valueOf(mmu.read(cs.read() + CPU_State.eip.read(), len));
        return instr;
    }

    @Override
    public boolean isIndirectAddressing() {
        return false;
    }

    @Override
    public void fetchOperand() {

    }
}
