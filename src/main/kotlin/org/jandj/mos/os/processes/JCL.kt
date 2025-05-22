package org.jandj.mos.os.processes

import org.jandj.mos.BlockSize
import org.jandj.mos.PageSize
import org.jandj.mos.machine.CPU
import org.jandj.mos.machine.Memory
import org.jandj.mos.machine.Word
import org.jandj.mos.os.JProcess
import org.jandj.mos.os.OS
import org.jandj.mos.os.ProcName
import org.jandj.mos.os.ProcessState
import org.jandj.mos.os.ResManager
import org.jandj.mos.os.ResName
import org.jandj.mos.os.Resource
import java.io.File

// Cia tures but verfikatorius failo, pazet ar viskas oki su failu ir tada jis uzkrautu i supervizorine, arba kazkas tokio
class JCL(intId: Int, extId: ProcName, pName: String,
          processList: ArrayList<JProcess>, parentProcess: JProcess?,
          cpu: CPU, os: OS,
          pState: ProcessState, priority: Int)
    :    JProcess(intId, extId, pName,
    processList, parentProcess,
    cpu, os,
    pState, priority,) {

    var hdd: Resource? = null
    var prog:Resource? = null
    var memory:Resource? = null

    override fun step() {
        when(nextInstruction){
            1->{
                os.requestResource(this, ResName.PROG_PAVADINIMAS)
                nextInstruction++
            }
            2->{
                os.requestResource(this, ResName.KIETASIS_DISKAS)
                nextInstruction++
            }
            3->{
                os.requestResource(this, ResName.VARTOTOJO_ATMINTIS)
                nextInstruction++
            }
            4->{
                JobToSuper()
            }
            5->{
                os.createResource(this, ResName.PROG_SUPERVIZORINEJE, Pair<Any, Resource>(prog!!.component!!, memory!!))
                nextInstruction++
            }
            6->{
                if(ResManager.findResByExtId(pDesc.createdResources, ResName.PROG_SUPERVIZORINEJE) != null){
                    os.printToSysIO("Program ${prog!!.component} started")
                }
                if(hdd != null){
                    os.releaseResource(hdd!!)
                    hdd = null
                }
                if(prog != null){
                    os.destroyResource(prog!!)
                    prog = null
                }
                if(memory != null){
                    os.releaseResource(memory!!)
                    memory = null
                }
                nextInstruction=1
            }
        }
    }

    private fun JobToSuper() {
        memory = ResManager.findResByExtId(pDesc.ownedResources, ResName.VARTOTOJO_ATMINTIS)
        hdd = ResManager.findResByExtId(pDesc.ownedResources, ResName.KIETASIS_DISKAS)
        prog = ResManager.findResByExtId(pDesc.ownedResources, ResName.PROG_PAVADINIMAS)
        var progname: String = ""
        var block: Array<Word>? = null
        val progcomp = prog!!.component
        if(progcomp is Pair<*,*>){
            progname = progcomp.first as String
            block = progcomp.second as Array<Word>
        } else{
            progname = progcomp as String
        }

        val hard: File = hdd!!.component as File
        val mem: Memory = memory!!.component as Memory

        val bytes = hard.readBytes()

        var indx = 0
        var text = (bytes.slice(indx..<indx+8).map{byte -> byte.toInt().toChar()}.toCharArray()).concatToString()
        while (indx <= bytes.size-8){
            if(text == progname){
                val beg = (bytes.slice(indx-4..<indx).map{byte -> byte.toInt().toChar()}.toCharArray()).concatToString()
                if(beg == "\$BEG"){
                    break
                }
            }
            indx+=4
            text = (bytes.slice(indx..<indx+8).map{byte -> byte.toInt().toChar()}.toCharArray()).concatToString()
        }
        if(text != progname){
            nextInstruction=6
            os.printToSysIO("Program ${prog!!.component} does not exist")
            return
        } else if((bytes.slice(indx-4..<indx).map{byte -> byte.toInt().toChar()}.toCharArray()).concatToString() != "\$BEG"){
            nextInstruction=6
            os.printToSysIO("Program ${prog!!.component} is invalid")
        }
        while (text != ".DATASEG" && indx <= bytes.size-8){
            indx+=4
            text = (bytes.slice(indx..<indx+8).map{byte -> byte.toInt().toChar()}.toCharArray()).concatToString()
        }
        if(text != ".DATASEG"){
            nextInstruction=6
            os.printToSysIO("Program ${prog!!.component} is invalid")
            return
        }
        indx+=8
        for (i in 0..<PageSize*BlockSize){
            mem.setSuperMem(i, Word(0u))
        }
        text = (bytes.slice(indx..<indx+8).map{byte -> byte.toInt().toChar()}.toCharArray()).concatToString()
        var idx = 8 * BlockSize
        while (text != ".CODESEG" && indx <= bytes.size-8){
            val word:Word = Word(bytes.slice(indx..<indx+4).map{byte -> byte.toInt().toChar()}.toCharArray())
            mem.setSuperMem(idx, word)
            idx++
            indx+=4
            text = (bytes.slice(indx..<indx+8).map{byte -> byte.toInt().toChar()}.toCharArray()).concatToString()
        }
        if(text != ".CODESEG"){
            nextInstruction=6
            os.printToSysIO("Program ${prog!!.component} is invalid")
            return
        }
        indx+=8
        idx = 0
        text = (bytes.slice(indx..<indx+4).map{byte -> byte.toInt().toChar()}.toCharArray()).concatToString()
        while(text != "\$END" && indx <= bytes.size-4){
            val word:Word = Word(bytes.slice(indx..<indx+4).map{byte -> byte.toInt().toChar()}.toCharArray())
            mem.setSuperMem(idx, word)
            idx++
            indx+=4
            text = (bytes.slice(indx..<indx+4).map{byte -> byte.toInt().toChar()}.toCharArray()).concatToString()
        }
        if(text != "\$END"){
            nextInstruction=6
            os.printToSysIO("Program ${prog!!.component} is invalid")
            return
        }
        if(block != null){
            val blockidx = block[0].bytes.toInt() * BlockSize

            for (i in 0..<BlockSize){
                mem.setSuperMem(blockidx+i, block[i])
            }
        }

        nextInstruction=5
    }

}