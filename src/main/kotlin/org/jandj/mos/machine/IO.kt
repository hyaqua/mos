package org.jandj.mos.machine

import org.jandj.mos.SysIOController
import org.jandj.mos.UserIOController
import org.jandj.mos.os.OS
import org.jandj.mos.os.ResName
import java.io.File

// Cia tures but kanalu irenginys, krc valdo jis visus skaitymus is ivesties, rasymus i ivesti ir skaitimus is hardo, turim jo prasyt naudojimo jei norim betka daryt krc
class IO(val sysIO: SysIOController, val userIO: UserIOController, val os: OS, val hdd: File) {

    fun printToSysIO(str:String){
        sysIO.outputText.appendText(str)
    }
    fun printToUserIO(str:String){
        userIO.outputText.appendText(str)
    }
    fun runProc(){
        var str = sysIO.Input.text.substring(0,8)
        os.createResource(os.startStopProc!!, ResName.PROG_PAVADINIMAS, str)
        sysIO.Input.clear()
    }

}