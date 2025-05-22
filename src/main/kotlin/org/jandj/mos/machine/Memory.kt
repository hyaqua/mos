package org.jandj.mos.machine

import org.jandj.mos.*


class Memory {
    private val memory: Array<Word> = Array(MEM_SIZE) {Word(0u)}
    private val blockCount = (PageSize -1)* PageCount// Calculate the amount of blocks available to the vms (PageCount-1 because we can have 1 fewer VMs than pages :) )
    private val freeBlocks = Array(blockCount) {0}

    fun setSuperMem(offset:Int, word: Word){
        var real_offset = (PageCount-1) * PageSize * BlockSize + offset
        memory[real_offset] = word
    }
    fun getSuperMem(offset: Int): Word {
        var real_offset = (PageCount-1) * PageSize * BlockSize + offset

        return memory[real_offset]
    }

    fun getMemory(ptr: Word, offset:UInt): Word{
        val blockIdx = offset.toInt() / 16
        val cellIdx = offset.toInt() % 16
        val realBlockIdx = memory[ptr.bytes.toInt()* BlockSize + blockIdx].bytes.toInt()
        return memory[realBlockIdx * 16 + cellIdx]
    }
    fun setMemory(ptr: Word, offset:UInt, word:Word){
        val blockIdx = offset.toInt() / 16
        val cellIdx = offset.toInt() % 16
        val realBlockIdx = memory[ptr.bytes.toInt()* BlockSize + blockIdx].bytes.toInt()
        memory[realBlockIdx * 16 + cellIdx] = word
    }
    fun getPtr(): Int {
        if(!enoughMemory()){
            return -1
        }
        var changed = false
        var ptr: Int = 0 // will hold the address of the vm's page table, all of them are held in the last PageCount-1 blocks of the memory
        for(i in 0..<freeBlocks.size){  // go through the last four blocks (hopefully correct :) )
            if(freeBlocks[i] == 0){ // if block is free for the pagetable then they are all set to 0, else other numbers :)
                ptr = i
                freeBlocks[i] = 1
                changed = true
                break
            }
        }
        println("ptr: $ptr")
        if(!changed){
            return -1
        }
        var enoughBlocks = true
        for(i in 0 ..<BlockSize){
            var j = 0
            while(freeBlocks[j] == 1){
                j++
                if(j >= freeBlocks.size){
                    enoughBlocks = false
                    break
                }
            }
            if(enoughBlocks){
                freeBlocks[j] = 1
                println("took block: $j")
                memory[ptr* BlockSize + i] = Word(j.toUInt())
            } else{
                break
            }
        }
        if (!enoughBlocks){
            for(i in 0..<BlockSize){
                memory[ptr*BlockSize+i] = Word(0u)
            }
            return -1
        }
        return ptr
    }
    fun deallocateMemory(ptr: Word){
        val iPtr = ptr.bytes.toInt()
        if (iPtr < 0 || iPtr >= blockCount) {
            println("Warning: Invalid pointer deallocation attempt: $iPtr")
            return
        }
        val blocksToFree = mutableSetOf<Int>()
        for (i in 0..<PageSize){
            val blockPtr = memory[iPtr * BlockSize + i].bytes.toInt()
            if (blockPtr >= 0 && blockPtr < blockCount) {
                blocksToFree.add(blockPtr)
            }
        }
        for (blockPtr in blocksToFree) {
            memory.fill(Word(0u), blockPtr * 16, blockPtr * 16 + 16)
            freeBlocks[blockPtr] = 0
            println("released block: $blockPtr")
        }
        memory.fill(Word(0u), iPtr * BlockSize, iPtr * BlockSize + BlockSize)
        freeBlocks[iPtr] = 0
        println("released block: $iPtr")
        printBlockStatus()
    }
    private fun printBlockStatus() {
        for (i in freeBlocks.indices) {
            print("$i:${freeBlocks[i]} ")
        }
        println()
    }
    fun enoughMemory(): Boolean{
        return !(freeBlocks.count{it == 0} < PageSize+1)
    }
    fun toStrArr(): ArrayList<String>{
        var arr = ArrayList<String>()
        for(i in 0..< PageCount){
            arr.add("Page $i")
            for(j in 0..< PageSize){
                arr.add("Block $j")
                for(x in 0..<BlockSize){
                    arr.add("$x: ${memory[i * PageSize * BlockSize + j * BlockSize + x].bytes.toString(16)}")
                }
            }
        }
        return arr
    }
}