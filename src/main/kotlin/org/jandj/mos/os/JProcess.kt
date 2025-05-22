package org.jandj.mos.os

import org.jandj.mos.machine.CPU


abstract class JProcess (
    intId: Int,
    extId: ProcName,
    pName: String,
    processList: ArrayList<JProcess>,
    parentProcess: JProcess?,
    var cpu: CPU,
    var os: OS,
    pState: ProcessState,
    priority: Int
): Comparable<JProcess> {
    var halted: Boolean
    val Timer_Interval = 2
    var pDesc: ProcessDescriptor = ProcessDescriptor(intId, extId,
                processList, pName,
        this, parentProcess,
                priority, pState,
                os, cpu)

    var nextInstruction = 1
    private var interval:Int
    init {
        parentProcess?.pDesc?.childrenList?.add(this)
        interval = Timer_Interval
        setTimer(interval)
        halted = false
    }

    fun fullStep(){
        resetTimer()
        if(!halted){
            step()
        }
        decTimer()
        if(checkInterrupts() == 0){
            if (getTimer() == 0)
                timerInterrupt()
        }
    }

    abstract fun step()

    fun loadCPU(){
       pDesc.savedState.restoreState(pDesc.cpu)
    }

    fun saveCPU(){
        pDesc.savedState.saveState(pDesc.cpu)
    }

    override operator fun compareTo(other: JProcess): Int {
        return this.pDesc.priority.compareTo(other.pDesc.priority)
    }
    override fun toString(): String{
        var str = ""
        str += "${pDesc.intId}: ${pDesc.extID.name}"
        str += "(${pDesc.pName});"
        str += "prio: ${pDesc.priority}"
        str += "nextInstruction: $nextInstruction;"
        str += "state: ${pDesc.pState};"
        if(pDesc.pState == ProcessState.BLOCKED){
            str += " Waiting: ${pDesc.waitingFor[0]}"
        }
        return str
    }

    private fun timerInterrupt() {
        saveCPU()
        resetTimer()
        os.stopProcess(this)
        if(pDesc.extID == ProcName.VIRTUAL_MACHINE)
            decPriority()
    }
    private fun decPriority() {
        if(pDesc.priority > 1){
            pDesc.priority--
        }
    }

    private fun setTimer(interval: Int) {
        this.pDesc.cpu.TI = interval.toUByte()
    }

    private fun getTimer(): Int {
        return pDesc.cpu.TI.toInt()
    }

    internal fun checkInterrupts(): Int {
        return pDesc.cpu.PI.toInt() + pDesc.cpu.SI.toInt()
    }

    private fun decTimer() {
        var tmp = pDesc.cpu.TI.toInt()
        if(tmp > 0){
            tmp--
            setTimer(tmp)
        }
    }

    fun resetTimer() {
        setTimer(interval)
    }
    open fun fitRes(resource: Resource): Boolean{
        return true
    }
}