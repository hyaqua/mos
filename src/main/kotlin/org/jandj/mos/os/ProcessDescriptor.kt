package org.jandj.mos.os

import org.jandj.mos.machine.CPU

class ProcessDescriptor(
    val intId: Int, val extID: ProcName,
    val processList: ArrayList<JProcess>,
    var pName: String,
    val process: JProcess, val parent: JProcess?,
    var priority: Int,
    var pState: ProcessState,
    var os: OS, val cpu: CPU,) {
    val savedState: RegState = RegState()
    val childrenList: ArrayList<JProcess> = ArrayList()
    val createdResources: ArrayList<Resource> = ArrayList()
    val ownedResources: ArrayList<Resource> = ArrayList()
    val waitingFor: ArrayList<ResName> = ArrayList()

}