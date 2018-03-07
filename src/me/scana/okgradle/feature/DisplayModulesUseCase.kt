package me.scana.okgradle.feature

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import javax.swing.JButton
import javax.swing.JPopupMenu

class DisplayModulesUseCase(private val project: Project) {

    fun showModules(from: JButton, onModuleClick: (Module) -> Unit) {
        val popup = JPopupMenu()
        ModuleManager.getInstance(project)
                .modules
                .sortedBy { it.name }
                .forEach {
                    popup.add(it.name).addActionListener { _ ->
                        onModuleClick(it)
                    }
                }
        popup.show(from, from.x, from.y)
    }

}
