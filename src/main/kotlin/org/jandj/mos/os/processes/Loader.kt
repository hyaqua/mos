package org.jandj.mos.os.processes

import org.jandj.mos.machine.CPU
import org.jandj.mos.machine.Word
import java.io.File
import org.jandj.mos.*
import org.jandj.mos.machine.Memory
import org.jandj.mos.os.*
import java.nio.file.Paths

class Loader(intId: Int, extId: ProcName, pName: String,
             processList: ArrayList<JProcess>, parentProcess: JProcess?,
             cpu: CPU, os: OS,
             pState: ProcessState, priority: Int)
:    JProcess(intId, extId, pName,
              processList, parentProcess,
              cpu, os,
              pState, priority,)  {


    override fun step() {
        when(nextInstruction) {
            1->{
                if(ResManager.findResByExtId(pDesc.ownedResources, ResName.PRANESIMAS_LOADER) == null){
                    os.requestResource(this, ResName.PRANESIMAS_LOADER)
                }
                nextInstruction=3
            }
            2 ->{
                if(ResManager.findResByExtId(pDesc.ownedResources, ResName.VARTOTOJO_ATMINTIS) == null) {
                    os.requestResource(this, ResName.VARTOTOJO_ATMINTIS)
                }
                nextInstruction++
            }
            3 ->{
                loadTovMem()
                nextInstruction = 1
            }
        }
    }
    private fun loadTovMem(){
        var vmemRes = ResManager.findResByExtId(pDesc.ownedResources, ResName.PRANESIMAS_LOADER)

        if(vmemRes == null){
            nextInstruction = 1
            return
        }
        var pair = vmemRes.component as Pair<*,*>
        var memRes = pair.second as Resource
        var vMem = pair.first as VMem
        var mem  = memRes.component as Memory
        for(block in 0..<16){
            for(idx in 0..<16){
                vMem.setMemory((block*BlockSize+idx).toUInt(), mem.getSuperMem(block*BlockSize+idx))
            }
        }
        println(vMem.pName)
        os.createResource(this, ResName.UZDUOTIS_VM, vMem)
        os.releaseResource(memRes)
        os.destroyResource(vmemRes)
    }
    private fun readJob() {
        var memory = ResManager.findResByExtId(pDesc.ownedResources, ResName.VIRTUAL_MEMORY)
        if (memory == null) {
            nextInstruction=1
            return
        }
        var vmem: VMem = memory.component as VMem

        this.pDesc.cpu.PTR = vmem.PTR
        val Pranesimas = ResManager.findResByExtId(pDesc.ownedResources, ResName.PRANESIMAS_LOADER)!!
        var fileName: String = ""
        if(Pranesimas.component is Pair<*,*>){
            val pair = Pranesimas.component as Pair<*, *>
            fileName = pair.first as String
            val arr = pair.second as Array<*>
            val blockIdx = arr[0] as Word
            for(i in 0u..15u){
                vmem.setMemory(blockIdx.bytes * 16u + i, arr[(i+1u).toInt()] as Word)
            }
        } else{
            fileName = Pranesimas.component as String
        }
        val prog: Array<Word> = openProgram(fileName)
        var idx = 1
        vmem.pName = prog[idx++].toString() + prog[idx++].toString()
        idx+=2
        var memIdx = (8 * BlockSize).toUInt()
        var string = prog[idx].toString()+prog[idx+1].toString()
        while (string != ".CODESEG") {
            vmem.setMemory(memIdx, prog[idx++])
            string = prog[idx].toString()+prog[idx+1].toString()
            memIdx++
        }
        memIdx = 0u
        idx+=2
        while(prog[idx].toString() != "\$END"){
            vmem.setMemory(memIdx, prog[idx++])
            memIdx++
        }
        os.createResource(this, ResName.UZDUOTIS_VM, vmem)
        os.destroyResource(Pranesimas)
        os.destroyResource(memory)
    }

    fun openProgram(name:String): Array<Word> {
        val reader = File("HDD.hdd")
        val bytes = ArrayList<Word>()
        val readBytes = reader.readBytes()
        var indx = 0
        for(idx in 0 ..<readBytes.size-8 step 4){
            var text = ""
            for(i in idx..< idx + 8){
                text += readBytes[i].toInt().toChar()
            }
            if(text == name){
                indx = idx
                break
            }
        }
        if(indx == 0){
            return emptyArray()
        }
        indx-= 4
        while(true){
            val chars = CharArray(4)
            for(i in 0..<4){
                val charval = readBytes[indx + i].toInt()
                chars[i] = charval.toChar()
            }
            bytes.add(Word(chars))
            indx+=4
            val string = String(chars)
            if(string == "\$END"){
                break
            }
        }

        return bytes.toTypedArray()
    }
}