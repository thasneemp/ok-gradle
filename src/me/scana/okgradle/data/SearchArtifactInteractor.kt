package me.scana.okgradle.data

import io.reactivex.Observable
import io.reactivex.Single
import me.scana.okgradle.data.repository.ArtifactRepository
import me.scana.okgradle.data.repository.SearchResult

class SearchArtifactInteractor(private val repositories: List<ArtifactRepository>) {

    fun search(query: String) : Observable<SearchResult> {
        return Single.concat(repositories.map { it.search(query) })
                .onErrorReturn { SearchResult.Error(it) }
                .toObservable()
    }
}
