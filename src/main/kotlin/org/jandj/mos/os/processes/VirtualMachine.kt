package org.jandj.mos.os.processes

import org.jandj.mos.machine.CPU
import org.jandj.mos.machine.runCommand
import org.jandj.mos.machine.Word
import org.jandj.mos.os.*

class VirtualMachine(intId: Int, extId: ProcName, pName: String,
                     processList: ArrayList<JProcess>, parentProcess: JProcess?,
                     cpu: CPU, os: OS,
                     pState: ProcessState, priority: Int)
    :    JProcess(intId, extId, pName,
    processList, parentProcess,
    cpu, os,
    pState, priority,) {
    var memory: VMem? = null

    override fun step() {
        when(nextInstruction){
            1 ->{
                if(os.requestResource(this, ResName.UZDUOTIS_IS_JG)){
                    nextInstruction++
                } else{
                    this.cpu.TI = 0u
                }
            }
            2 ->{
                var memRes = ResManager.findResByExtId(pDesc.ownedResources, ResName.UZDUOTIS_IS_JG)
                this.memory = memRes!!.component as VMem
                pDesc.pName = memory!!.pName
                pDesc.cpu.pc = 0u
                cpu.PTR = memory!!.PTR
                os.destroyResource(memRes)
                nextInstruction++
            }
            3 ->{
                vmStep()
            }
            4 ->{
                os.requestResource(this, ResName.RESUME_VM)
                nextInstruction++
            }
            5 ->{
                val resumeResource = ResManager.findResByExtId(pDesc.ownedResources, ResName.RESUME_VM)
                if(resumeResource!=null){
                    os.destroyResource(resumeResource)
                }
                nextInstruction=3
            }
        }
    }

    private fun vmStep() {
        val tempPC = pDesc.cpu.pc
        val opcode:Word = memory!!.getMemory(tempPC.toUInt())
        println("opcode: ${opcode.bytes.toString(16).toUpperCase()}")
        if(opcode[0].and(0x0Fu) == 0xFu){
            this.halted = true
        }
        val res = runCommand(cpu, opcode)
        if (res == -1){
            os.printToSysIO("UNKNOWN OPCODE, HALTING: ${opcode.bytes.toString(16)}")
        }
        saveCPU()
        if (checkInterrupts() != 0){
            os.createResource(this, ResName.PRANESIMAS_PERTRAUKIMUI, pDesc)
            os.stopProcess(this)
            os.requestResource(this, ResName.RESUME_VM)
            nextInstruction = 5

        } else{
            nextInstruction = 3
        }
    }

    override fun fitRes(resource: Resource): Boolean{
        if(resource.resDesc.extId == ResName.RESUME_VM){
            if((resource.component as ProcessDescriptor).intId == pDesc.intId){
                return true
            } else{
                return false
            }
        } else return true
    }

    fun destroy() {
        memory?.deallocMem()
        memory = null
        for (res in pDesc.ownedResources){
            os.destroyResource(res)
        }
    }
    override fun toString(): String{
        var str = super.toString()
        str += ";instruction:${memory?.getMemory(cpu.pc.toUInt())?.bytes?.toString(16)}"
        return str
    }
}