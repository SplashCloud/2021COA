package cpu;

import cpu.instr.all_instrs.InstrFactory;
import cpu.instr.all_instrs.Instruction;
import cpu.registers.EIP;
import cpu.registers.Register;
import transformer.Transformer;

public class CPU {

    static Transformer transformer = new Transformer();
    static MMU mmu = MMU.getMMU();


    /**
     * execInstr specific numbers of instructions
     *
     * @param number numbers of instructions
     */
    public int execInstr(long number) {
        // 执行过的指令的总长度
        int totalLen = 0;
        while (number > 0) {
            int instrLength = execInstr();
            if (0 > instrLength) {
                break;
            } else {
                number--;
                totalLen += instrLength;
                ((EIP)CPU_State.eip).plus(instrLength);
            }
        }
        return totalLen;
    }

    /**
     * execInstr a single instruction according to eip value
     */
    private int execInstr() {
        String eip = CPU_State.eip.read();
        int len = decodeAndExecute(eip);
        return len;
    }

    private int decodeAndExecute(String eip) {
        int opcode = instrFetch(eip, 1);
        Instruction instruction = InstrFactory.getInstr(opcode);
        assert instruction != null;

        //exec the target instruction
        int len = instruction.exec(opcode);
        return len; //返回指令的字节长度


    }

    /**
     * @param eip
     * @param length opcode的字节数，本作业只使用单字节opcode
     * @return
     */
    public static int instrFetch(String eip, int length) {
        /**
         * 目前默认只有一个数据段
         */
        Register cs = CPU_State.cs;

        // length = length * 8  一个字节8位
        String opcode = String.valueOf(mmu.read(cs.read() + eip, length * 8));
        return Integer.parseInt(transformer.binaryToInt(opcode));
    }

    public void execUntilHlt(){
        // TODO ICC
        Instruction currentInstr = null;
        int currentOp = 0;
        int len;
        CPU_State.ICC = 0;
        while (CPU_State.ICC != 3){
            switch (CPU_State.ICC){
                case 0: // fetch
                    currentOp = instrFetch(CPU_State.eip.read(),1);
                    currentInstr = InstrFactory.getInstr(currentOp);
                    assert currentInstr != null;
                    currentInstr.fetchInstr(CPU_State.eip.read(),currentOp);
                    if (currentInstr.isIndirectAddressing())
                        CPU_State.ICC = 1;
                    else
                        CPU_State.ICC = 2;
                    break;
                case 1: // indirect
                    assert currentInstr != null;
                    currentInstr.fetchOperand();
                    CPU_State.ICC = 2;
                    break;
                case 2: //exec
                    assert currentInstr != null;
                    CPU_State.ICC = 0;
                    len = currentInstr.exec(currentOp);
                    ((EIP) CPU_State.eip).plus(len);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + CPU_State.ICC);
            }
        }
    }

}

