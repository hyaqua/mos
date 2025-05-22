package org.jandj.mos.machine

import org.jandj.mos.machine.Word


fun runCommand(cpu: CPU, opcode:Word):Int{

    when (opcode[0].shr(4)){
        0u ->   handleArithmetic(cpu, opcode)
        1u ->   handleMemory(cpu, opcode)
        2u ->   handleControl(cpu, opcode)
        3u ->   handleIO(cpu, opcode)
        0xFu -> halt(cpu)
        else -> {halt(cpu); return -1}
    }
    return 0
}
private fun handleArithmetic(cpu: CPU, opcode: Word) {
    when (opcode[0].and(0x0Fu)){
        0u -> { // DO NOTHING
            cpu.pc++

        }
        1u -> { // ADD
            val temp = Word(cpu.AX.bytes)
            cpu.AX += cpu.BX
            cpu.pc++
            if (temp > cpu.AX){
                cpu.PI = 5u
            }
        }
        2u -> { // SUB
            val temp = Word(cpu.AX.bytes)
            cpu.AX -= cpu.BX
            cpu.pc++
            cpu.SF = if (cpu.AX < temp){
                cpu.SF.or(0b1000_0000u)
            } else{
                cpu.SF.and(0b0100_0000u)
            }
            cpu.SF = if (cpu.AX == Word(0u)){
                cpu.SF.or(0b01000_0000u)
            } else{
                cpu.SF.and(0b1000_0000u)
            }
        }
        3u -> { // MUL
            cpu.AX *= cpu.BX
            cpu.pc++
        }
        4u -> { // DIV
            cpu.AX /= cpu.BX
            cpu.pc++
        }
        5u -> { // REM
            cpu.AX %= cpu.BX
            cpu.pc++
        }
        6u -> { // INC
            cpu.AX++
            cpu.pc++
        }
        7u -> { // DEC
            cpu.AX--
            cpu.pc++

        }
        8u -> { // CMP
            val opcodes:Array<Word> = arrayOf(
                Word(0x11_00_0F_0Eu),
                Word(0x12_00_00_00u),
                Word(0x11_00_0F_0Fu),
                Word(0x12_00_00_00u),
                Word(0x02_00_00_00u),
                Word(0x10_00_0F_0Fu),
                Word(0x12_00_00_00u),
                Word(0x10_00_0F_0Eu)
            )
            for (op in opcodes) {
                runCommand(cpu, op)
                cpu.pc--
            }
            cpu.pc++
        }
    }
}

private fun handleMemory(cpu: CPU, opcode:Word) {
    when (opcode[0].and(0x0Fu)) {
        0u ->{
            cpu.AX = getMemory(opcode[2] * 16u + opcode[3], cpu)
            cpu.pc++

        }
        1u ->{
            setMemory(opcode[2] * 16u + opcode[3], cpu.AX, cpu)
            cpu.pc++

        }
        2u ->{
            val temp = cpu.AX
            cpu.AX = cpu.BX
            cpu.BX = temp
            cpu.pc++

        }
    }
}

private fun handleControl(cpu: CPU, opcode:Word) {
    when (opcode[0].and(0x0Fu)) {
        0u -> { // JMP
            cpu.pc = (opcode[2] * 16u + opcode[3]).toUShort()

        }
        1u ->{ // JG
            if(cpu.SF.toInt().shr(7) == 1){
                cpu.pc = (opcode[2] * 16u + opcode[3]).toUShort()
            }
            else{
                cpu.pc++
            }

        }
        2u ->{ // JE
            if(cpu.SF.toInt().shr(6).and(0b01) == 1){
                cpu.pc = (opcode[2] * 16u + opcode[3]).toUShort()
            }else{
                cpu.pc++
            }

        }
        3u ->{ // JL
            if(cpu.SF.toInt().shr(6) == 0){
                cpu.pc = (opcode[2] * 16u + opcode[3]).toUShort()
            } else{
                cpu.pc++
            }

        }
        4u ->{

            cpu.SI = 6u
            cpu.pc++
        }
    }
}

private fun handleIO(cpu: CPU, opcode:Word) {
    when (opcode[0].and(0x0Fu)) {
        0u -> {
            cpu.SI = 1u

            cpu.pc++
        }
        1u -> {

            cpu.SI = 2u
            cpu.pc++
        }
        2u -> {

            cpu.SI = 3u
            cpu.pc++
        }
        3u -> {

            cpu.SI = 4u
            cpu.pc++
        }
    }
}

private fun halt(cpu: CPU) {
    cpu.SI = 5u
}


fun setMemory(offset: UInt, word: Word, cpu: CPU){
       cpu.memory.setMemory(cpu.PTR, offset, word)
}
fun getMemory(offset: UInt, cpu: CPU): Word{
    return cpu.memory.getMemory(cpu.PTR, offset)
}
