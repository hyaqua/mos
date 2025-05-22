package org.jandj.mos.os.processes

import org.jandj.mos.machine.CPU
import org.jandj.mos.os.*

class MainProc(intId: Int, extId: ProcName, pName: String,
               processList: ArrayList<JProcess>, parentProcess: JProcess?,
               cpu: CPU, os: OS,
               pState: ProcessState, priority: Int)
    :    JProcess(intId, extId, pName,
    processList, parentProcess,
    cpu, os,
    pState, priority,)  {
    override fun step() {
        when(nextInstruction){
            1 -> {
                os.requestResource(this, ResName.PROG_SUPERVIZORINEJE)
                nextInstruction++
            }
            2 -> {
                sendToJG()
                nextInstruction++
            }
            3 -> {
                checkJobGovernors()
                os.createProcess(this, ProcName.JOB_GOVERNOR)
                nextInstruction = 1
            }
        }
    }

    private fun checkJobGovernors() {
        val processesToDestroy = mutableListOf<JProcess>()

        var jobGovernor: JobGovernor
        for(proc in pDesc.childrenList){
            jobGovernor = proc as JobGovernor
            if(jobGovernor.isJobHalted){
                processesToDestroy.add(proc)
            }
        }
        for(proc in processesToDestroy){
            os.destroyProcess(proc)
        }
    }

    private fun sendToJG() {
        val res = ResManager.findResByExtId(pDesc.ownedResources, ResName.PROG_SUPERVIZORINEJE)
        if(res == null){
            nextInstruction = 1
            return
        }

        os.createResource(this, ResName.UZDUOTIS_IS_MAINPROC, res.component)
        os.destroyResource(res)
    }
}