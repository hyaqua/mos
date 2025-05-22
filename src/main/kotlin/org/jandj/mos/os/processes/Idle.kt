package org.jandj.mos.os.processes

import org.jandj.mos.machine.CPU
import org.jandj.mos.os.JProcess
import org.jandj.mos.os.OS
import org.jandj.mos.os.ProcName
import org.jandj.mos.os.ProcessState

class Idle (intId: Int, extId: ProcName, pName: String,
           processList: ArrayList<JProcess>, parentProcess: JProcess?,
           cpu: CPU, os: OS,
           pState: ProcessState, priority: Int)
    :JProcess(intId, extId, pName,
              processList, parentProcess,
              cpu, os,
              pState, priority,) {

    override fun step() {
        return;
    }
}

