package me.scana.okgradle.data

import io.reactivex.Single
import kotlinx.coroutines.experimental.runBlocking
import me.scana.okgradle.data.repository.ArtifactRepository
import me.scana.okgradle.data.repository.SearchResult
import org.apache.http.HttpException
import org.junit.Test
import java.io.IOException

class SearchArtifactInteractorTest {

    @Test
    fun `returns first artifact is available`() {
        val repositories = listOf(
                WithResultArtifactRepository("me.scana.sdk:sdk:1.0.0"),
                WithResultArtifactRepository("org.scana.smth:1.0.0")
        )
        val interactor = SearchArtifactInteractor(repositories)

        runBlocking {
            val result = interactor.search("scana")
            //assertEquals("me.scana.sdk:sdk:1.0.0", result.artifact)
        }
    }

    @Test
    fun `returns last error`() {
        val repositories = listOf(
                ErrorArtifactRepository(IOException()),
                ErrorArtifactRepository(HttpException())
        )
        val interactor = SearchArtifactInteractor(repositories)

        runBlocking {
            val result = interactor.search("scana")
            //assertTrue(result.error is HttpException)
        }
    }

    @Test
    fun `returns suggestions`() {
        val repositories = listOf(
                NoResultWithSuggestionRepository("maybe_this?"),
                ErrorArtifactRepository(HttpException())
        )
        val interactor = SearchArtifactInteractor(repositories)

        runBlocking {
            val result = interactor.search("scana")
            /*assertTrue(result.error is HttpException)
            assertEquals(result.suggestion, "maybe_this?")*/
        }
    }

    class ErrorArtifactRepository(val exception: Exception) : ArtifactRepository {
        override fun search(query: String): Single<SearchResult> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    class NoResultWithSuggestionRepository(val suggestion: String) : ArtifactRepository {
        override fun search(query: String): Single<SearchResult> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    class WithResultArtifactRepository(val artifactId: String) : ArtifactRepository {
        override fun search(query: String): Single<SearchResult> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}