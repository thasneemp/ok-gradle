package me.scana.okgradle.util

import me.scana.okgradle.data.repository.Artifact
import javax.swing.AbstractListModel

class ArtifactListModel : AbstractListModel<Artifact>() {

    private val artifacts = mutableListOf<Artifact>()

    fun add(element: Artifact) {
        artifacts.add(element)
        val index = artifacts.size - 1
        fireIntervalAdded(this, index, index)
    }

    fun addAll(elements: List<Artifact>) {
        val previousSize = artifacts.size
        artifacts.addAll(elements)
        fireIntervalAdded(this, previousSize + 1, artifacts.size - 1)
    }

    fun clear() {
        artifacts.clear()
        fireIntervalRemoved(this, 0, artifacts.size)
    }

    override fun getElementAt(index: Int): Artifact {
        return artifacts[index]
    }

    override fun getSize(): Int {
        return artifacts.size
    }
}