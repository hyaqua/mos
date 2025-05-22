package org.jandj.mos.os

class ProcManager(val os: OS) {


    fun execute(){
//        checkProcesses()
        if (os.runProc != null){
            checkRunning()
        }
        val candidate = getTopPrioReadyProc()
        if(candidate != null && os.runProc == null){
            startProc(candidate)
        } else if(candidate != null){
            if(candidate.pDesc.priority > os.runProc!!.pDesc.priority && (os.runProc!!.pDesc.pState == ProcessState.READY || os.runProc!!.pDesc.extID == ProcName.IDLE)){
                stopProcess()
                startProc(candidate)
            }
        }
    }

    private fun checkRunning(){
        val proc = os.runProc
        if(proc!!.pDesc.pState == ProcessState.BLOCKED){
            stopProcess()
        } else if(proc.pDesc.pState == ProcessState.READY){
            proc.pDesc.pState = ProcessState.RUN
        }
    }

    private fun startProc(newProcess: JProcess){
        os.runProc = newProcess
        newProcess.pDesc.pState = ProcessState.RUN
        os.readyProcesses.remove(newProcess)
        newProcess.loadCPU()
        newProcess.resetTimer()
    }
    private fun stopProcess(){
        val proc = os.runProc!!
        os.runProc = null
        proc.saveCPU()
        if(proc.pDesc.pState != ProcessState.BLOCKED){
            os.readyProcesses.add(proc)
        } else {
            os.blockedProcesses.add(proc)
            os.readyProcesses.remove(proc)
        }
    }
    private fun getTopPrioReadyProc(): JProcess? {
        var tmp: JProcess? = null
        if(os.readyProcesses.isNotEmpty()) {
            tmp = os.readyProcesses[0]
            for (process in os.readyProcesses) {
                if(process > tmp!!){
                    tmp = process
                }
            }
        }

        return tmp
    }
}