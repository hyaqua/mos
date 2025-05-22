package org.jandj.mos.os

import javafx.application.Platform
import org.jandj.mos.MainWindowController
import org.jandj.mos.SysIOController
import org.jandj.mos.UserIOController
import org.jandj.mos.machine.CPU
import org.jandj.mos.machine.IO
import org.jandj.mos.machine.Memory
import org.jandj.mos.os.processes.*

class OS: Runnable {
    var stop: Boolean = false
    val processes = ArrayList<JProcess>()
    val readyProcesses = ArrayList<JProcess>()
    val blockedProcesses = ArrayList<JProcess>()
    val stoppedProcesses = ArrayList<JProcess>()

    var runProc: JProcess? = null

    private val resources = ArrayList<Resource>()
    val usedResources = ArrayList<Resource>()
    val freeResources = ArrayList<Resource>()

    var Speed: Long = 1000

    val cpu: CPU = CPU()
    lateinit var io: IO
    lateinit var mainWindowController: MainWindowController
    lateinit var userIOController: UserIOController
    lateinit var SysIOController: SysIOController
    var procMan = ProcManager(this)
    private var resMan = ResManager(this)
    var startStopProc: JProcess

    private var pid = 0
    private var rid = 0

    init {
        startStopProc = this.createProcess(null, ProcName.START_STOP, 99)!!
        this.runProc = this.startStopProc
        cpu.memory = Memory()
        // set up windowing shit
    }

    override fun run(){
        while (!stop){
            osStep()
            printToMainWindow()
            Thread.sleep(Speed)
        }
    }

    fun printToMainWindow(){
        Platform.runLater {
            mainWindowController.procList.items.clear()
            mainWindowController.procList.items.add("runproc: ${runProc?.pDesc?.pName}")
            mainWindowController.procList.items.addAll(processes)
            mainWindowController.resList.items.clear()
            mainWindowController.resList.items.addAll(resources)
            val reglist = mainWindowController.regList.items

            reglist.clear()
            reglist.add("SF: ${cpu.SF.toString(16)}")
            reglist.add("PC: ${cpu.pc.toString(16)}")
            reglist.add("PTR: ${cpu.PTR.bytes.toString(16)}")
            reglist.add("AX: ${cpu.AX.bytes.toString(16)}")
            reglist.add("BX: ${cpu.BX.bytes.toString(16)}")
            reglist.add("TI: ${cpu.TI.toString(16)}")
            reglist.add("SI: ${cpu.SI.toString(16)}")
            reglist.add("PI: ${cpu.PI.toString(16)}")

            mainWindowController.MemList.items.clear()
            mainWindowController.MemList.items.addAll(cpu.memory.toStrArr())
        }
    }

    fun osStep() {
        if(runProc != null){
            if(runProc!!.pDesc.extID == ProcName.IDLE || !processes.contains(runProc!!)){
                resMan.execute()
            }
            runProc?.fullStep()
        } else{
            resMan.execute()
        }
    }

    private fun genPID(): Int {
        return pid++
    }
    private fun genRID(): Int {
        return rid++
    }


    fun createProcess(creator: JProcess?, extId: ProcName): JProcess? {
        return createProcess(creator, extId, 1)
    }

    fun createProcess(creator: JProcess?, extId: ProcName, priority: Int): JProcess? {
        var process: JProcess? = null
        val intId = genPID()
        when (extId){
            ProcName.START_STOP ->      process = StartStop(intId, extId, "StartStop", processes, creator, cpu, this, ProcessState.READY, 99)
            ProcName.WAIT_FOR_JOB ->    process = WaitForJob(intId,extId, "WaitForJob", processes, creator, cpu, this, ProcessState.READY, 93)
            ProcName.MAIN_PROC ->       process = MainProc(intId,extId, "MainProc", processes, creator, cpu, this, ProcessState.READY, 92)
            ProcName.LOADER ->          process = Loader(intId,extId, "Loader", processes, creator, cpu, this, ProcessState.READY, 88)
            ProcName.JOB_GOVERNOR ->    process = JobGovernor(intId,extId, "JobGovernor", processes, creator, cpu, this, ProcessState.READY, 89)
            ProcName.VIRTUAL_MACHINE -> process = VirtualMachine(intId,extId, "VirtualMachine", processes, creator, cpu, this, ProcessState.READY, 80)
            ProcName.INTERRUPT ->       process = Interrupt(intId,extId, "Interrupt", processes, creator, cpu, this, ProcessState.READY, 90)
            ProcName.PRINT_LINE ->      process = PrintLine(intId,extId, "PrintLine", processes, creator, cpu, this, ProcessState.READY, 91)
            ProcName.GET_LINE ->        process = GetLine(intId,extId, "GetLine", processes, creator, cpu, this, ProcessState.READY, 91)
            ProcName.JCL ->             process = JCL(intId, extId, "JCL", processes, creator, cpu, this, ProcessState.READY, 91)
            ProcName.IDLE ->            process = Idle(intId, extId, "Idle", processes, creator, cpu, this, ProcessState.READY, 1)
        }
        if(process != null){
            this.processes.add(process)
            this.readyProcesses.add(process)
            procMan.execute()
            return process
        }
        procMan.execute()
        return null
    }
    fun destroyProcess(proc: JProcess) {
        val resToDestroy: ArrayList<Resource> = proc.pDesc.createdResources.clone() as ArrayList<Resource>
        for(res in resToDestroy){
            destroyResource(res)
        }
        val procToDestroy: ArrayList<JProcess> = proc.pDesc.childrenList.clone() as ArrayList<JProcess>
        for(res in procToDestroy){
            destroyProcess(res)
        }
        readyProcesses.removeAll { p -> p==proc }
        blockedProcesses.removeAll { p -> p==proc }
        stoppedProcesses.removeAll { p -> p==proc }
        processes.removeAll { p -> p==proc }

        if(proc.pDesc.parent != null){
            proc.pDesc.parent!!.pDesc.childrenList.remove(proc)
        }
    }
    fun stopProcess(process: JProcess) {
        if(runProc?.pDesc?.intId == process.pDesc.intId){
            runProc = null
        }
        when (process.pDesc.pState){
            ProcessState.RUN -> {
                runProc = null
                stoppedProcesses.add(process)
                process.pDesc.pState = ProcessState.READY_STOPPED
            }
            ProcessState.READY -> {
                readyProcesses.remove(process)
                stoppedProcesses.add(process)
                process.pDesc.pState = ProcessState.READY_STOPPED
            }
            ProcessState.BLOCKED -> {
                blockedProcesses.remove(process)
                stoppedProcesses.add(process)
                process.pDesc.pState = ProcessState.BLOCKED_STOPPED
            }
            else -> {

            }
        }
        procMan.execute()
    }

    fun activateProcess(process: JProcess) {
        if(stoppedProcesses.contains(process)){
            stoppedProcesses.remove(process)
            if(process.pDesc.pState == ProcessState.BLOCKED_STOPPED){
                blockedProcesses.add(process)
                process.pDesc.pState = ProcessState.BLOCKED
            } else{
                readyProcesses.add(process)
                process.pDesc.pState = ProcessState.READY
            }
        }
    }

    fun createResource(process: JProcess, extId: ResName, component: Any?): Resource {
        val intId = genRID()
        var res: Resource? = null
        when (extId){
            ResName.ISVEDIMO_IRENGINIS, ResName.IVEDIMO_IRENGINYS -> {
                res = Resource(intId,extId,this,process,true, component)

            }
            else -> {
                res = Resource(intId,extId,this,process,false, component)
            }
        }
        process.pDesc.createdResources.add(res)
        this.resources.add(res)
        this.freeResources.add(res)
        this.resMan.execute()
        return res
    }
    fun releaseResource(res: Resource) {
        if(res.resDesc.user != null){
            val user = res.resDesc.user
            user!!.pDesc.ownedResources.remove(res)
            res.resDesc.user = null
        }
        usedResources.remove(res)
        freeResources.add(res)
        resMan.execute()
    }


    fun destroyResource(res: Resource) {
        if(res.resDesc.user != null){
            val user = res.resDesc.user
            user!!.pDesc.ownedResources.remove(res)
            res.resDesc.user = null
        }
        res.resDesc.creator.pDesc.createdResources.remove(res)
        this.freeResources.remove(res)
        this.usedResources.remove(res)
        this.resources.remove(res)
    }
    fun requestResource(process: JProcess, resource: ResName): Boolean {
        process.pDesc.waitingFor.add(resource)
        this.resMan.execute()
        return process.pDesc.waitingFor.isEmpty()
    }
    private fun getTimer(): Int{
        return cpu.TI.toInt()
    }
    fun createSimpleResource(creator: JProcess, extId: ResName, component: Any?): Resource {
        val res = Resource(genRID(), extId, this, creator, false, component)
        resources.add(res)
        return res
    }

    fun printToSysIO(str:String){
        io.printToSysIO(str + "\n")
    }
}