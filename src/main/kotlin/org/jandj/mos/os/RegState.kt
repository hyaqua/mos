package org.jandj.mos.os

import org.jandj.mos.machine.CPU
import org.jandj.mos.machine.Word

class RegState {
    var SF: UByte = 0u
    var pc:UShort = 0u
    var PTR:Word = Word()
    var AX:Word = Word()
    var BX:Word = Word()
    var SI:UByte = 0u
    var PI:UByte = 0u
    fun restoreState(cpu: CPU) {
        cpu.SF = this.SF
        cpu.pc = this.pc
        cpu.PTR = this.PTR
        cpu.AX = this.AX
        cpu.BX = this.BX
        cpu.SI = this.SI
        cpu.PI = this.PI
    }

    fun saveState(cpu: CPU) {
        this.SF = cpu.SF
        this.pc = cpu.pc
        this.PTR = cpu.PTR
        this.AX = cpu.AX
        this.BX = cpu.BX
        this.SI = cpu.SI
        this.PI = cpu.PI
    }

}