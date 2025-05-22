package org.jandj.mos.os.processes

import org.jandj.mos.machine.CPU
import org.jandj.mos.os.*

class WaitForJob(intId: Int, extId: ProcName, pName: String,
                 processList: ArrayList<JProcess>, parentProcess: JProcess?,
                 cpu: CPU, os: OS,
                 pState: ProcessState, priority: Int)
    :    JProcess(intId, extId, pName,
    processList, parentProcess,
    cpu, os,
    pState, priority,)  {

    override fun step() {
        when(nextInstruction) {
            1-> {
                os.requestResource(this, ResName.PROG_PAVADINIMAS)
                nextInstruction++
            }
            2 -> {
                takeFile()
                if(pDesc.ownedResources.size == 0){
                    nextInstruction = 1
                }
            }
        }
    }

    private fun takeFile() {
        var fileRes = ResManager.findResByExtId(pDesc.ownedResources, ResName.PROG_PAVADINIMAS)

        if (fileRes == null) {
            nextInstruction = 1
            return
        }
        os.createResource(this, ResName.PROG_SUPERVIZORINEJE, fileRes.component)
        os.destroyResource(fileRes)
    }

}