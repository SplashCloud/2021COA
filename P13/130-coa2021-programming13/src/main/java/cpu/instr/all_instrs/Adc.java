package cpu.instr.all_instrs;

import cpu.CPU_State;
import cpu.MMU;
import cpu.instr.decode.Operand;
import cpu.instr.decode.OperandType;
import cpu.registers.CS;
import cpu.registers.EFlag;

import static kernel.MainEntry.alu;

public class Adc implements Instruction{

    private MMU mmu = MMU.getMMU();
    private CS cs = (CS) CPU_State.cs;
    private EFlag eflag = (EFlag) CPU_State.eflag;
    private int len;
    private String instr;

    @Override
    public int exec(int opcode) {
        if (opcode == 0x15) {
            Operand imm = new Operand();
            imm.setVal(instr.substring(8, 40));
            imm.setType(OperandType.OPR_IMM);

            String tmp = alu.add(imm.getVal(), CPU_State.eax.read());
            if (eflag.getCF())
                tmp = alu.add("0000000000000000000000000000001",tmp);
            CPU_State.eax.write(tmp);
        }
        return len;
    }

    @Override
    public String fetchInstr(String eip, int opcode) {
        // adc eax imm
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
