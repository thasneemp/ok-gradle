package me.scana.okgradle;

import com.android.tools.idea.gradle.dsl.api.GradleBuildModel
import com.android.tools.idea.gradle.dsl.api.dependencies.ArtifactDependencySpec
import com.android.tools.idea.gradle.util.GradleUtil
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.panels.HorizontalLayout
import com.intellij.ui.components.panels.VerticalLayout
import com.intellij.util.ui.TextTransferable
import io.reactivex.Observable
import me.scana.okgradle.data.AddDependencyStrategyFactory
import me.scana.okgradle.data.Notifier
import me.scana.okgradle.data.repository.Artifact
import me.scana.okgradle.util.ArtifactListModel
import me.scana.okgradle.util.HintTextField
import me.scana.okgradle.util.observeSelection
import me.scana.okgradle.util.observeText
import java.awt.Dimension
import javax.swing.*


class OkGradleDialog(
        private val notifier: Notifier,
        private val project: Project,
        private val presenter: OkGradle.Presenter
) : DialogWrapper(false), OkGradle.View {

    private val hintLink = LinkLabel<Any>("", null).apply {
        setListener({ _, _ -> throw RuntimeException() }, null)
    }

    private val hintPanel = JPanel(HorizontalLayout(2)).apply {
        add(JLabel("(did you mean: "))
        add(hintLink)
        add(JLabel("?)"))
    }

    private val libraryQuery = HintTextField().apply { hint = "try typing \'retrofit\'" }

    private val resultsListModel = ArtifactListModel()
    private val resultList = JBList(resultsListModel).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        visibleRowCount = -1
    }

    override fun enableButtons(isEnabled: Boolean) {
        addDependencyButton.isEnabled = isEnabled
        clipboardCopyButton.isEnabled = isEnabled
    }

    private val clipboardCopyButton = JButton().apply {
        text = Copys.COPY_TO_CLIPBOARD_ACTION
        isEnabled = false
        addActionListener { onCopyToClipboardClick() }
    }

    private val addDependencyButton = JButton().apply {
        text = Copys.ADD_DEPENDENCY_ACTION
        isEnabled = false
        addActionListener { presenter.onAddDependencyClicked() }
    }

    init {
        init()
        title = Copys.DIALOG_TILE
    }

    override fun show() {
        presenter.takeView(this)
        super.show()
    }

    override fun dispose() {
        presenter.dropView()
        super.dispose()
    }

    override fun createCenterPanel(): JComponent {
        val panel = buildPanel()
        configureViews()
        return panel
    }

    private fun buildPanel(): JPanel {
        val panel = JPanel(VerticalLayout(8))
        panel.add(
                JPanel(HorizontalLayout(8)).apply {
                    add(JLabel("Which library you need?"))
                    add(hintPanel)
                }
        )

        panel.add(libraryQuery)
        panel.add(JLabel("Select it from a list:"))
        val scrollPane = JBScrollPane(resultList)
        scrollPane.preferredSize = Dimension(500, 200)
        panel.add(scrollPane)
        panel.add(
                JPanel(HorizontalLayout(8)).apply {
                    add(clipboardCopyButton)
                    add(addDependencyButton)
                }
        )
        return panel
    }

    override fun getPreferredFocusedComponent() = libraryQuery

    override fun createActions(): Array<Action> = emptyArray()

    override fun userTextInputObservable(): Observable<String> = libraryQuery.observeText()

    override fun userArtifactSelectionObservable(): Observable<Artifact> = resultList.observeSelection()

    override fun displayModules(modules: List<Module>) {
        val menu = JPopupMenu(Copys.MODULES_TITLE)
        modules.forEach {
            menu.add(it.name).addActionListener { _ ->
                presenter.onModuleSelected(it)
            }
        }
        with(addDependencyButton) {
            menu.show(this, this.x, this.y)
        }
    }

    private fun addSelectedDependency(module: Module) {
        val artifact = resultsListModel.getElementAt(resultList.selectedIndex)

        module.let {
            val buildGradleFile = GradleUtil.getGradleBuildFile(module)
            val model = GradleBuildModel.parseBuildFile(buildGradleFile!!, project, module.name)
            val dependencies = model.dependencies()
            val dependencySpec = ArtifactDependencySpec.create(artifact.name, artifact.groupId, artifact.version)
            val dependencyStrategy = AddDependencyStrategyFactory().create(dependencySpec)
            dependencies?.let {
                WriteCommandAction.runWriteCommandAction(project) {
                    val addedDependencies = dependencyStrategy.addDependency(dependencySpec, dependencies)
                    model.applyChanges()
                    val file = PsiManager.getInstance(project).findFile(buildGradleFile)
                    file?.let {
                        CodeStyleManager.getInstance(project).adjustLineIndent(file, 0)
                    }
                    notifier.showDependenciesAddedMessage(module.name, addedDependencies)
                }

            }
        }
    }

    private fun configureViews() {
        hideSuggestion()
    }

    override fun showArtifacts(artifacts: List<Artifact>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showSuggestion(suggestion: String) {
        hintLink.text = suggestion
        hintPanel.isVisible = true
    }

    private fun onCopyToClipboardClick() {
        val artifact = resultsListModel.getElementAt(resultList.selectedIndex)
        val dependencySpec = ArtifactDependencySpec.create(artifact.name, artifact.groupId, artifact.version)
        val dependencyStrategy = AddDependencyStrategyFactory().create(dependencySpec)
        CopyPasteManager.getInstance().setContents(TextTransferable(dependencyStrategy.getDependencyStatements(dependencySpec).joinToString("\n")))
        notifier.showDependenciesStatementCopiedMessage()
    }

    private fun showResults(artifact: Artifact) {
        resultsListModel.add(artifact)
    }

    private fun hideSuggestion() {
        hintPanel.isVisible = false
    }

    override fun resetListState() {
        resultList.clearSelection()
        resultsListModel.clear()
        hideSuggestion()
    }

}