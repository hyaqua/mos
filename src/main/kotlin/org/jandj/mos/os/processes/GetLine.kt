package org.jandj.mos.os.processes

import javafx.application.Platform
import org.jandj.mos.UserIOController
import org.jandj.mos.machine.CPU
import org.jandj.mos.machine.Word
import org.jandj.mos.os.*

class GetLine (intId: Int, extId: ProcName, pName: String,
               processList: ArrayList<JProcess>, parentProcess: JProcess?,
               cpu: CPU, os: OS,
               pState: ProcessState, priority: Int)
    :    JProcess(intId, extId, pName,
    processList, parentProcess,
    cpu, os,
    pState, priority,) {
    var deviceRes: Resource? = null
    var descRes: Resource? = null
    override fun step() {
        when(nextInstruction) {
            1 ->{
                os.requestResource(this, ResName.PRANESIMAS_GETLINE)
                nextInstruction++
            }
            2 ->{
                os.requestResource(this, ResName.IVEDIMO_IRENGINYS)
                nextInstruction++
            }
            3 ->{
                initiateInput();
                os.requestResource(this, ResName.IVEDIMO_SRAUTAS)
                nextInstruction++
            }
            4 ->{
                endInput();
                nextInstruction++;
            }
            5 ->{
                os.destroyResource(descRes!!)
                os.releaseResource(deviceRes!!)
                nextInstruction = 1;
            }
        }
    }

    fun initiateInput() {
        deviceRes = ResManager.findResByExtId(pDesc.ownedResources, ResName.IVEDIMO_IRENGINYS)
        descRes = ResManager.findResByExtId(pDesc.ownedResources, ResName.PRANESIMAS_GETLINE)

        val device: UserIOController = deviceRes?.component as UserIOController
        val desc = descRes?.component as ProcessDescriptor

        Platform.runLater {
            device.initInput(desc.process)
        }
    }

    private fun endInput() {
        val desc = descRes?.component as ProcessDescriptor
        val mem = (desc.process as VirtualMachine).memory
        val opcode = mem!!.getMemory(desc.savedState.pc-1u)
        val offset = opcode[2] * 16u + opcode[3]
        val inputRes = ResManager.findResByExtId(pDesc.ownedResources, ResName.IVEDIMO_SRAUTAS)!!
        val input: String = inputRes.component as String
        if (input.toUIntOrNull() == null){
            mem.setMemory(offset, Word(input.toCharArray()))
        } else{
            mem.setMemory(offset, Word(input.toUInt()))
        }
        os.destroyResource(inputRes)
        os.createResource(this, ResName.IVESTA_EILUTE, desc)
    }
}