package org.jandj.mos.machine

import org.jandj.mos.machine.Word

class CPU {
    var Mode:UByte = 0u
    var SF: UByte = 0u
    var pc:UShort = 0u
    var PTR:Word = Word()
    var AX:Word = Word()
    var BX:Word = Word()
    var TI:UByte = 0u
    var SI:UByte = 0u
    var PI:UByte = 0u

    lateinit var memory: Memory
}




