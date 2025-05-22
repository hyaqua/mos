package org.jandj.mos.os.processes

import org.jandj.mos.machine.CPU
import org.jandj.mos.machine.Word
import org.jandj.mos.os.*

class JobGovernor(intId: Int, extId: ProcName, pName: String,
                  processList: ArrayList<JProcess>, parentProcess: JProcess?,
                  cpu: CPU, os: OS,
                  pState: ProcessState, priority: Int)
    :    JProcess(intId, extId, pName,
    processList, parentProcess,
    cpu, os,
    pState, priority,) {

    var isJobHalted: Boolean = false
    var VM: JProcess? = null

    override fun step() {
        when(nextInstruction) {
            1 -> {
                os.requestResource(this, ResName.UZDUOTIS_IS_MAINPROC)
                nextInstruction++
            }
            2 ->{
                os.requestResource(this, ResName.VIRTUAL_MEMORY)
                nextInstruction++
            }
            3 ->{
                createVmemToLoader()
                nextInstruction=10
            }
            10 ->{
                os.requestResource(this, ResName.UZDUOTIS_VM)
                nextInstruction=4
            }
            4 ->{
                createVM()
            }
            5 ->{
                os.requestResource(this, ResName.PERTRAUKIMAS)
                nextInstruction++
            }
            6 ->{
                processInterrupt()
            }
            7 ->{
                os.activateProcess(VM!!)
                os.createResource(this, ResName.RESUME_VM, VM?.pDesc)
                nextInstruction = 5
            }
            8 ->{
                val isvestaEil = ResManager.findResByExtId(pDesc.ownedResources, ResName.ISVESTA_EILUTE)
                if (isvestaEil != null) {
                    os.destroyResource(isvestaEil)
                }
                nextInstruction=7
            }
            9 ->{
                val ivestaEil = ResManager.findResByExtId(pDesc.ownedResources, ResName.IVESTA_EILUTE)
                if (ivestaEil != null) {
                    os.destroyResource(ivestaEil)
                    nextInstruction=7
                }
            }
        }
    }

    private fun processInterrupt() {
        val resInt = ResManager.findResByExtId(pDesc.ownedResources, ResName.PERTRAUKIMAS)
        val intMsg = resInt?.component as IntMsg
        os.destroyResource(resInt)
        when(intMsg.action){
            IntType.ILLEGAL_OPCODE, IntType.ILLEGAL_ASSIGNMENT, IntType.ILLEGAL_ADDRESS, IntType.HALT -> {
                (VM as VirtualMachine).destroy()
                os.destroyProcess(VM!!)
                VM = null
                isJobHalted = true
                nextInstruction = 5
            }
            IntType.IN ->{
                os.createResource(this, ResName.PRANESIMAS_GETLINE, VM?.pDesc)
                os.requestResource(this, ResName.IVESTA_EILUTE)
                nextInstruction = 9
            }
            IntType.PRS, IntType.PRN, IntType.P ->{
                os.createResource(this, ResName.EILUTE_ATMINTYJE, Pair(intMsg.action, VM?.pDesc))
                os.requestResource(this, ResName.ISVESTA_EILUTE)
                nextInstruction = 8
            }
            IntType.ST ->{
                val opcode = (VM as VirtualMachine).memory!!.getMemory(VM!!.pDesc.savedState.pc.minus(1u))
                var progName = ""
                val offset = (opcode[2] * 16u + opcode[3])
                for (j in 0u .. 1u){
                    for(i in 0..3){
                        progName+= Char((VM as VirtualMachine).memory!!.getMemory(offset + j)[i].toInt())
                    }
                }
                if(VM?.pDesc?.cpu?.BX?.bytes != 0u){
                    val block = Array(17) {Word(0u)}
                    val blockIdx = VM!!.pDesc.cpu.AX.bytes.toInt() * 16
                    for(i in 0 .. 15){
                        block[i+1] = Word((VM as VirtualMachine).memory!!.getMemory((blockIdx + i).toUInt()).bytes)
                    }
                    block[0] = Word(VM!!.pDesc.cpu.AX.bytes)
                    os.createResource(os.startStopProc, ResName.PROG_PAVADINIMAS, Pair(progName, block))
                } else{
                    os.createResource(os.startStopProc, ResName.PROG_PAVADINIMAS, progName)
                }
//                (VM as VirtualMachine).destroy()
//                os.destroyProcess(VM!!)
//                isJobHalted = true
                nextInstruction = 7
            }
            IntType.READY ->{
                nextInstruction = 5
            }
        }
    }

    private fun createVM() {
        val vmemRes = ResManager.findResByExtId(pDesc.ownedResources, ResName.UZDUOTIS_VM)
        val vmem = (vmemRes!!.component as VMem)
        os.destroyResource(vmemRes)

        os.createResource(this, ResName.UZDUOTIS_IS_JG, vmem)
        VM = os.createProcess(this, ProcName.VIRTUAL_MACHINE, 2)
        nextInstruction++
    }
    private fun createVmemToLoader(){
        val res = ResManager.findResByExtId(pDesc.ownedResources, ResName.UZDUOTIS_IS_MAINPROC)
        val vMem = ResManager.findResByExtId(pDesc.ownedResources, ResName.VIRTUAL_MEMORY)
        if(res == null || vMem == null){
            nextInstruction = 1
            return
        }
        val mem: VMem = vMem.component as VMem
        mem.pName = (res.component as Pair<*,*>).first as String


        os.createResource(this, ResName.PRANESIMAS_LOADER, Pair<VMem, Any?>(mem,(res.component as Pair<*,*>).second))
        os.destroyResource(res)
        os.destroyResource(vMem)
        return

    }

    override fun fitRes(resource: Resource): Boolean {
        when(resource.resDesc.extId) {
            ResName.PERTRAUKIMAS -> {
                return (resource.component as IntMsg).procIntId == this.pDesc.intId
            }
            ResName.ISVESTA_EILUTE -> {
                return ((resource.component as Pair<*,*>).second as ProcessDescriptor).intId == VM?.pDesc?.intId
            }
            ResName.IVESTA_EILUTE -> {
                return (resource.component as ProcessDescriptor).intId == VM!!.pDesc.intId
            }
            else ->{
                return true
            }
        }
    }
    override fun toString(): String {
        return super.toString() + " (halted: $isJobHalted)"
    }
}