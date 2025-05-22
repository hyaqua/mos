package org.jandj.mos.os.processes

import javafx.application.Platform
import org.jandj.mos.UserIOController
import org.jandj.mos.machine.CPU
import org.jandj.mos.machine.Word
import org.jandj.mos.os.*

@Suppress("UNCHECKED_CAST")
class PrintLine (intId: Int, extId: ProcName, pName: String,
                 processList: ArrayList<JProcess>, parentProcess: JProcess?,
                 cpu: CPU, os: OS,
                 pState: ProcessState, priority: Int)
    :    JProcess(intId, extId, pName,
    processList, parentProcess,
    cpu, os,
    pState, priority,) {

    private var device: Resource? = null
    private var description: Resource? = null

    override fun step() {
        when(nextInstruction){
            1 -> {
                os.requestResource(this, ResName.EILUTE_ATMINTYJE)
                nextInstruction++
            }
            2 ->{
                os.requestResource(this, ResName.ISVEDIMO_IRENGINIS)
                nextInstruction++
            }
            3 ->{
                executeOutput()
                nextInstruction++
            }
            4 ->{
                description?.let { os.destroyResource(it) }
                nextInstruction++
            }
            5 ->{
                device?.let { os.releaseResource(it) }
                nextInstruction++
            }
            6 ->{
                os.createResource(this, ResName.ISVESTA_EILUTE, description?.component)
                nextInstruction = 1
            }
        }
    }
    fun executeOutput(){
        device = ResManager.findResByExtId(pDesc.ownedResources, ResName.ISVEDIMO_IRENGINIS)
        description = ResManager.findResByExtId(pDesc.ownedResources, ResName.EILUTE_ATMINTYJE)

        val deviceCon: UserIOController = device?.component as UserIOController
        var desc: Pair<IntType, ProcessDescriptor> = description?.component as Pair<IntType, ProcessDescriptor>

        var output:String

        when(desc.first){
            IntType.PRS -> output = getAx(desc.second.savedState.AX, true)
            IntType.PRN -> output = getAx(desc.second.savedState.AX, false)
            IntType.P -> output = getLineFromMemory((desc.second.process as VirtualMachine).memory,
                                                    desc.second.savedState.pc)
            else -> {
                return
            }
        }
        Platform.runLater {
            deviceCon.outputText.appendText(output)
        }
    }

    private fun getLineFromMemory(memory: VMem?, pc: UShort): String {
        val opcode:Word? = memory?.getMemory(pc.toUInt() - 1u)
        if(opcode == null){
            return ""
        }
        var offset = opcode[1] * 16u + opcode[2]
        var cnt = opcode[3].toInt()
        var print = ""
        var idx = 0
        while(cnt >0){
            print += Char(memory.getMemory(offset)[idx].toInt())
            cnt--
            idx++
            if(idx == 4){
                idx = 0
                offset++
            }
        }
        return print
    }

    private fun getAx(AX: Word, string: Boolean): String {
        if(string){
            var print = ""
            for(i in 0..3){
                print += Char(AX[i].toInt())
            }
            return print
        } else{
            return AX.bytes.toString()
        }
    }


}