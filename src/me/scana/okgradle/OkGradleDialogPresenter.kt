package me.scana.okgradle

import com.intellij.openapi.module.Module
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import me.scana.okgradle.data.SearchArtifactInteractor
import me.scana.okgradle.data.repository.Artifact
import me.scana.okgradle.data.repository.SearchResult
import me.scana.okgradle.util.Selection
import java.util.concurrent.TimeUnit

class OkGradleDialogPresenter(private val interactor: SearchArtifactInteractor) : OkGradle.Presenter {

    private val SEARCH_START_DELAY_IN_MILLIS = 500L

    private var selectedArtifact: Artifact? = null
    private var view: OkGradle.View? = null
    private val disposables = CompositeDisposable()

    override fun takeView(view: OkGradle.View) {
        this.view = view
        view.enableButtons(false)
        observeInput(view)
        observeArtifactSelection(view)
    }

    private fun observeInput(view: OkGradle.View) {
        view.userTextInputObservable()
                .debounce(SEARCH_START_DELAY_IN_MILLIS, TimeUnit.MILLISECONDS)
                .doOnNext { this.view?.resetListState() }
                .switchMap { interactor.search(it) }
                .subscribe(this::onSearchResult, this::onCriticalError)
                .attachToLifecycle()
    }

    private fun observeArtifactSelection(view: OkGradle.View) {
        view.userArtifactSelectionObservable()
                .startWith(Selection.None())
                .subscribe(this::onArtifactSelectionChanged, this::onCriticalError)
                .attachToLifecycle()
    }

    private fun onSearchResult(result: SearchResult) = when (result) {
        is SearchResult.Success -> displayResult(result)
        is SearchResult.Error -> displayError(result)
    }

    private fun displayResult(result: SearchResult.Success) {
        view?.showArtifacts(result.artifacts)
        result.suggestion?.let {
            view?.showSuggestion(it)
        }
    }

    private fun displayError(error: SearchResult.Error) {

    }

    private fun onCriticalError(error: Throwable) {
        throw error
    }

    private fun onArtifactSelectionChanged(selection: Selection<Artifact>) = when(selection) {
        is Selection.Item -> {
            selectedArtifact = selection.value
            view?.enableButtons(true)
        }
        is Selection.None -> view?.enableButtons(false)
    }

    override fun dropView() {
        disposables.clear()
        view = null
    }

    override fun onAddDependencyClicked() {

    }

    override fun onModuleSelected(module: Module) {

    }

    private fun Disposable.attachToLifecycle() {
        disposables.add(this)
    }

}
