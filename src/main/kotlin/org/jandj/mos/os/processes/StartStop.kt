package org.jandj.mos.os.processes

import org.jandj.mos.machine.CPU
import org.jandj.mos.os.*

class StartStop(intId: Int, extId: ProcName, pName: String,
                processList: ArrayList<JProcess>, parentProcess: JProcess?,
                cpu: CPU, os: OS,
                pState: ProcessState, priority: Int)
                : JProcess(intId, extId, pName,
                processList, parentProcess,
                cpu, os,
                pState, priority,)  {




    override fun step() {
        when(nextInstruction){
            1 -> {
                createSysResources()
                nextInstruction++
            }
            2 -> {
                createSysProcesses()
                nextInstruction++
            }
            3 -> {
                os.requestResource(this, ResName.OS_STOP)
                nextInstruction++
            }
            4 -> {
                destroySysProcesses()
                nextInstruction++
            }
            5 -> {
                destroySysResources()
                os.stop = true
                nextInstruction = 1
            }
        }
    }
    fun createSysResources(){
        os.createResource(this, ResName.VARTOTOJO_ATMINTIS, os.cpu.memory)
        os.createResource(this, ResName.IVEDIMO_IRENGINYS, os.io.userIO)
        os.createResource(this, ResName.ISVEDIMO_IRENGINIS, os.io.userIO)
        os.createResource(this, ResName.KIETASIS_DISKAS, os.io.hdd)
    }
    fun createSysProcesses(){
        os.createProcess(this, ProcName.JCL)
        os.createProcess(this, ProcName.LOADER)
        os.createProcess(this, ProcName.MAIN_PROC)
        os.createProcess(this, ProcName.INTERRUPT)
        os.createProcess(this, ProcName.GET_LINE)
        os.createProcess(this, ProcName.PRINT_LINE)
        os.createProcess(this, ProcName.IDLE)
    }
    fun destroySysResources() {
        val tmpList: ArrayList<Resource> = this.pDesc.createdResources.clone() as ArrayList<Resource>
        for(res in tmpList){
            os.destroyResource(res)
        }
    }
    fun destroySysProcesses() {
        val tmpList: ArrayList<JProcess> = this.pDesc.childrenList.clone() as ArrayList<JProcess>
        for(proc in tmpList){
            os.destroyProcess(proc)
        }
    }


}