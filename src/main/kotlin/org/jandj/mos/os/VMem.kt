package org.jandj.mos.os

import org.jandj.mos.machine.Memory
import org.jandj.mos.machine.Word

class VMem(private var realMem: Memory, var PTR: Word) {
    var pName: String = ""
    fun initialize(): Int{
        var ptr = realMem.getPtr()
        if(ptr >= 0){
            PTR = Word(ptr.toUInt())
        }
        return ptr
    }

    fun getMemory(offset: UInt): Word {
        return realMem.getMemory(PTR, offset)
    }
    fun setMemory(offset: UInt, word: Word){
        realMem.setMemory(PTR, offset, word)
    }

    fun deallocMem() {
        realMem.deallocateMemory(PTR)
    }
}