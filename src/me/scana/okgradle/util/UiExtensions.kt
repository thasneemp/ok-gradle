package me.scana.okgradle.util

import com.intellij.ui.components.JBList
import io.reactivex.Observable
import io.reactivex.disposables.Disposables
import javax.swing.event.DocumentEvent
import javax.swing.event.ListSelectionListener

fun HintTextField.observeText(): Observable<String> = Observable.create {
    val listener = object : SimpleDocumentListener() {
        override fun update(e: DocumentEvent) {
            it.onNext(text)
        }
    }
    document.addDocumentListener(listener)
    it.setDisposable(Disposables.fromAction {
        document.removeDocumentListener(listener)
    })
}

sealed class Selection<T> {
    class Item<T>(val value: T) : Selection<T>()
    class None<T> : Selection<T>()
}

fun <T> JBList<T>.observeSelection(): Observable<Selection<T>> = Observable.create {
    val listener = ListSelectionListener { _ ->
        if (isSelectionEmpty) {
            it.onNext(Selection.None())
        } else {
            it.onNext(Selection.Item(model.getElementAt(selectedIndex)))
        }
    }
    addListSelectionListener(listener)
    it.setDisposable(Disposables.fromAction { removeListSelectionListener(listener) })
}