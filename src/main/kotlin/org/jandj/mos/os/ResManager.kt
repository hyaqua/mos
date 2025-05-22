package org.jandj.mos.os

import org.jandj.mos.machine.Word

class ResManager(val os: OS) {


    fun execute(){
        val waitingForResource = ArrayList<JProcess>()
        for(process in os.processes){
            if(process.pDesc.waitingFor.size > 0){
                waitingForResource.add(process)
            }
        }
        waitingForResource.sortDescending()
        var tmpResources = ArrayList<Resource>()
        var tempWaitedList = ArrayList<ResName>()

        for(process in waitingForResource){
            tempWaitedList = ArrayList<ResName>()
            for (resname in process.pDesc.waitingFor){
                tempWaitedList.add(resname)
            }
            for(res in tempWaitedList){
                if(res == ResName.VIRTUAL_MEMORY){
                    if(os.cpu.memory.enoughMemory()){
                        val memory = os.cpu.memory
                        if(memory.enoughMemory()){
                            val PTR = Word()
                            val vmem = VMem(memory, PTR)
                            val ptr = vmem.initialize()
                            if(ptr > 0){
                                var vmemResource = os.createSimpleResource(os.startStopProc, ResName.VIRTUAL_MEMORY, vmem)
                                giveResource(process,vmemResource)
                            }
                        }
                    }
                } else{
                    val resList = findResListByExtId(os.freeResources, res)
                    for(resource in resList){
                        if(res != null && process.fitRes(resource)){
                            giveResource(process,resource)
                            os.activateProcess(process)
                            break;
                        }
                    }
                }
            }

            if(process.pDesc.waitingFor.size == 0){
                process.pDesc.pState = ProcessState.READY
                os.blockedProcesses.remove(process)
                os.stoppedProcesses.remove(process)
                os.readyProcesses.add(process)

            } else{
                if(process.pDesc.pState == ProcessState.READY){
                    process.pDesc.pState = ProcessState.BLOCKED
                    os.readyProcesses.removeAll { p -> p == process }
//                    os.readyProcesses.remove(process)
                    os.blockedProcesses.add(process)
                    if(os.runProc == process){
                        os.runProc = null
                    }
                } else if(process.pDesc.pState == ProcessState.RUN){
                    process.pDesc.pState = ProcessState.BLOCKED
                    os.blockedProcesses.add(process)
                    os.readyProcesses.removeAll { p -> p == process }
                    if(os.runProc == process){
                        os.runProc = null
                    }
                }
            }

        }
        os.procMan.execute()
    }

    private fun checkForLeakedResources(){
        val destroyedProcesses = os.processes.filter {it.halted}
        for (process in destroyedProcesses){
            process.pDesc.ownedResources.toList().forEach { resource -> {
                println("Warning: Resource ${resource.resDesc.extId} leaked by process ${process.pDesc.intId}")
                if(resource.resDesc.reusable){
                    os.releaseResource(resource)
                }
            } }
        }
    }
    fun giveResource(process: JProcess, res: Resource) {
        process.pDesc.ownedResources.add(res)
        process.pDesc.waitingFor.remove(res.resDesc.extId)
        os.freeResources.remove(res)
        os.usedResources.add(res)

        res.resDesc.user = process
    }



    companion object {
        fun findResListByExtId(list: ArrayList<Resource>, extId: ResName): ArrayList<Resource> {
            val resList = ArrayList<Resource>()
            for(res in list){
                if(res.resDesc.extId == extId){
                    resList.add(res)
                }
            }
            return resList
        }
        fun findResByExtId(list: ArrayList<Resource>, extId: ResName): Resource? {
            for(res in list){
                if(res.resDesc.extId == extId){
                    return res
                }
            }
            return null
        }
    }
}