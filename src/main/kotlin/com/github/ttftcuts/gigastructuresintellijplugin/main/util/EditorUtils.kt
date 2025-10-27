package com.github.ttftcuts.gigastructuresintellijplugin.main.util

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationListener.URL_OPENING_LISTENER
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import java.util.concurrent.atomic.AtomicReference

object EditorUtils {
    /** Based on the LivePlugin show() function **/
    fun showMessage(
        message: Any?,
        title: String = "",
        notificationType: NotificationType = NotificationType.INFORMATION,
        notificationAction: NotificationAction? = null
    ) {
        runLaterOnEdt {
            val notification = Notification("GigaTools", title, message.toString().ifBlank { "[empty message]" }, notificationType)
            if (notificationAction != null) {
                notification.addAction(notificationAction)
            }
            notification.setListener(URL_OPENING_LISTENER)
            ApplicationManager.getApplication().messageBus.syncPublisher(Notifications.TOPIC).notify(notification)
        }
    }

    fun runLaterOnEdt(f: () -> Any) {
        ApplicationManager.getApplication().invokeLater { f.invoke() }
    }

    fun <T> runOnEdt(f: () -> T): T {
        val result = AtomicReference<T>()
        ApplicationManager.getApplication().invokeAndWait({ result.set(f()) }, ModalityState.nonModal())
        return result.get()
    }
}