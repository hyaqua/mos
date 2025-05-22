package org.jandj.mos.os.processes

import org.jandj.mos.machine.CPU
import org.jandj.mos.os.*

data class IntMsg(val action: IntType, val procIntId: Int)

class Interrupt (intId: Int, extId: ProcName, pName: String,
                 processList: ArrayList<JProcess>, parentProcess: JProcess?,
                 cpu: CPU, os: OS,
                 pState: ProcessState, priority: Int)
    :    JProcess(intId, extId, pName,
    processList, parentProcess,
    cpu, os,
    pState, priority,) {
    override fun step() {
        when(nextInstruction) {
            1 -> {
                os.requestResource(this, ResName.PRANESIMAS_PERTRAUKIMUI)
                nextInstruction++
            }
            2 -> {
                processInterrupt()
                nextInstruction--
            }
        }
    }

    private fun processInterrupt() {
        val desRes = ResManager.findResByExtId(pDesc.ownedResources, ResName.PRANESIMAS_PERTRAUKIMUI)
        val desc = desRes?.component as ProcessDescriptor
        val parentId = desc.parent?.pDesc?.intId
        var type: IntType = IntType.READY

        when(desc.savedState.SI.toInt()) { //PRS, PRN, P, IN, HALT, ST,
            1 -> {
                type = IntType.PRS
            }
            2 -> {
                type = IntType.PRN
            }
            3 -> {
                type = IntType.P
            }
            4 -> {
                type = IntType.IN
            }
            5 -> {
                type = IntType.HALT
            }
            6 -> {
                type = IntType.ST
            }
        }
        when(desc.savedState.PI.toInt()) {
            1 ->{
                type = IntType.ILLEGAL_ADDRESS
            }
            2 ->{
                type = IntType.ILLEGAL_OPCODE
            }
            3 ->{
                type = IntType.ILLEGAL_ASSIGNMENT
            }
        }
        desc.savedState.SI = 0u
        desc.savedState.PI = 0u
        val msg = IntMsg(type, parentId!!)
        os.destroyResource(desRes)
        os.createResource(this, ResName.PERTRAUKIMAS, msg)
    }

}