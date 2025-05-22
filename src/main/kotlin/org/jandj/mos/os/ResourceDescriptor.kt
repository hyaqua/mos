package org.jandj.mos.os

import org.jandj.mos.os.JProcess
import org.jandj.mos.os.OS
import org.jandj.mos.os.ResName

class ResourceDescriptor(
    val intId: Int, val extId: ResName,
    val reusable: Boolean,
    val os: OS,
    var component: Any?,
    val creator: JProcess
) {
    var user: JProcess? = null

}