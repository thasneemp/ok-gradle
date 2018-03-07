package me.scana.okgradle

import com.google.gson.GsonBuilder
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.project.Project
import me.scana.okgradle.data.Notifier
import me.scana.okgradle.data.SearchArtifactInteractor
import me.scana.okgradle.data.repository.*
import okhttp3.OkHttpClient

class OkGradleAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val gson = GsonBuilder()
                .registerTypeAdapter(Spellcheck::class.java, SpellcheckDeserializer())
                .create()
        val networkClient = NetworkClient(OkHttpClient.Builder().build())
        val repositories = listOf(
                GoogleRepository(networkClient),
                MavenRepository(networkClient, gson),
                JitPackRepository(networkClient, gson)
        )
        val interactor = SearchArtifactInteractor(repositories)
        val project = event.getData(DataKeys.PROJECT) as Project
        val notifier = Notifier(project)

        val presenter = OkGradleDialogPresenter(interactor)

        val dialog = OkGradleDialog(notifier, project, presenter)
        dialog.show()
    }
}