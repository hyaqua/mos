package org.jandj.mos

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.*
import org.jandj.mos.os.JProcess
import org.jandj.mos.os.OS
import org.jandj.mos.os.ResName


abstract class Controller {
    var os: OS? = null
}
class MainWindowController: Controller() {
    @FXML
    lateinit var Slider: Slider
    @FXML
    lateinit var SpeedLabel: Label

    @FXML
    lateinit var startButton: Button
    @FXML
    lateinit var MemList: ListView<Any>
    @FXML
    lateinit var regList: ListView<Any>
    @FXML
    lateinit var resList: ListView<Any>
    @FXML
    lateinit var procList: ListView<Any>

    fun initialize() {
        Slider.valueProperty().addListener { _, _, newValue -> run {
            SpeedLabel.text = "Speed: ${newValue.toLong()}"
            os?.Speed = newValue.toLong()
        }
        }
    }
    fun init(){
        os?.stop = false
    }
    @FXML
    fun onStartButtonClick(actionEvent: ActionEvent) {
        if(startButton.text == "Start"){
            startButton.text = "Stop"
            os?.stop = false
            val osThread = Thread(os)
            osThread.start()
        } else{
            startButton.text = "Start"
            os?.stop = true
        }
    }

    fun os_stop(actionEvent: ActionEvent) {
        os!!.createResource(os!!.startStopProc, ResName.OS_STOP,null)
        startButton.text = "Start"
    }


}
class SysIOController: Controller() {
    @FXML
    lateinit var runButton: Button
    @FXML
    lateinit var Input: TextField
    @FXML
    lateinit var outputText: TextArea

    @FXML
    fun runProc(actionEvent: ActionEvent) {
        os!!.io.runProc()
    }
}
class UserIOController: Controller() {
    @FXML
    lateinit var runButton: Button
    @FXML
    lateinit var Input: TextField
    @FXML
    lateinit var outputText: TextArea
    @FXML
    fun initialize() {
        Input.isEditable = false
        Input.isDisable = true
        runButton.isDisable = true
        outputText.isEditable = false
    }
    @FXML
    fun initInput(process: JProcess) {
        Input.isEditable = true
        Input.isDisable = false
        runButton.isDisable = false
    }

    fun sendInput(actionEvent: ActionEvent) {
        val tmp = Input.text.substring(0,4)
        os!!.createResource(os!!.startStopProc, ResName.IVEDIMO_SRAUTAS, tmp)
        Input.clear()
        Input.isEditable = false
        Input.isDisable = true
        runButton.isDisable = true
    }
}
