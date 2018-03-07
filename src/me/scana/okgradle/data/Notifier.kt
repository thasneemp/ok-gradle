package me.scana.okgradle.data

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project



class Notifier(private val project: Project) {

    private val notificationGroup = NotificationGroup(
            "Ok, Gradle!",
            NotificationDisplayType.BALLOON,
            true
    )

    fun showDependenciesAddedMessage(module: String?, dependencies: List<String>) {
        showMessage("Dependency added to \"$module\"", dependencies.joinToString("\n"))
    }

    fun showDependenciesStatementCopiedMessage() {
        showMessage("Copied!", "Dependency statements have been copied to your clipboard.")
    }

    private fun showMessage(title: String, message: String) {
        val notification = notificationGroup.createNotification(title, null, message, NotificationType.INFORMATION)
        ApplicationManager.getApplication().invokeLater { Notifications.Bus.notify(notification, project) }
    }

}
