package org.jandj.mos

import atlantafx.base.theme.CupertinoDark
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import org.jandj.mos.machine.IO
import org.jandj.mos.os.OS
import java.io.File

class HelloApplication : Application() {
    var stages: ArrayList<Stage> = ArrayList()


    fun createStage(fxml: String, w: Double, h: Double, title: String, stage: Stage, resizalbe: Boolean, os: OS): Controller {
        val fxmlLoader = FXMLLoader(javaClass.getResource(fxml))
        val scene = Scene(fxmlLoader.load(), w, h)
        val con = fxmlLoader.getController<Any>() as Controller
        con.os = os
        stage.scene = scene
        stage.title = title
        stage.isResizable = resizalbe
        stage.show()
        return con
    }

    override fun start(stage: Stage) {
        val os = OS()
        Application.setUserAgentStylesheet((CupertinoDark::getUserAgentStylesheet)(CupertinoDark()))

        val con = createStage("main-window.fxml", 800.0, 500.0, "MainWindow", stage, resizalbe = true, os) as MainWindowController
        os.mainWindowController = con
        con.init()
        val stage2 = Stage()
        os.SysIOController = createStage("SystemIO.fxml", 600.0, 400.0, "SystemIO", stage2, resizalbe = false, os) as SysIOController
        val stage3 = Stage()
        os.userIOController = createStage("UserIO.fxml", 600.0, 400.0, "UserIO", stage3, resizalbe = false, os) as UserIOController
        os.io = IO(os.SysIOController, os.userIOController, os, File("HDD.hdd"))
        stages.add(stage)
        stages.add(stage2)
        stages.add(stage3)
        for (stage in stages) {
            stage.setOnCloseRequest { event ->
                for(st in stages) {
                    st.close()
                }
            }
        }
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}