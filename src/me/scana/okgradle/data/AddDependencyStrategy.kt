package me.scana.okgradle.data

import com.android.tools.idea.gradle.dsl.api.dependencies.ArtifactDependencySpec
import com.android.tools.idea.gradle.dsl.api.dependencies.CommonConfigurationNames
import com.android.tools.idea.gradle.dsl.api.dependencies.DependenciesModel

const val ANNOTATION_PROCESSOR = "annotationProcessor"

interface AddDependencyStrategy {
    fun addDependency(dependencySpec: ArtifactDependencySpec, model: DependenciesModel): List<String>
    fun getDependencyStatements(dependencySpec: ArtifactDependencySpec): List<String>
}

class AddDependencyStrategyFactory {

    fun create(dependencySpec: ArtifactDependencySpec) = when {
        dependencySpec.hasAnnotationProcessor() -> AnnotationProcessorDependencyStrategy()
        else -> RegularAddDependencyStrategy()
    }
}

class RegularAddDependencyStrategy : AddDependencyStrategy {
    override fun addDependency(dependencySpec: ArtifactDependencySpec, model: DependenciesModel): List<String> {
        model.addArtifact(CommonConfigurationNames.IMPLEMENTATION, dependencySpec)
        return listOf(dependencySpec.compactNotation())
    }

    override fun getDependencyStatements(dependencySpec: ArtifactDependencySpec): List<String> {
        return listOf("${CommonConfigurationNames.IMPLEMENTATION} '${dependencySpec.compactNotation()}'")
    }
}
class AnnotationProcessorDependencyStrategy : AddDependencyStrategy {
    override fun addDependency(dependencySpec: ArtifactDependencySpec, model: DependenciesModel): List<String> {
        val result = mutableListOf<String>()
        model.addArtifact(CommonConfigurationNames.IMPLEMENTATION, dependencySpec)
        result.add(dependencySpec.compactNotation())
        val compilerName = dependencySpec.annotationProcessorName()
        compilerName?.let {
            val annotationProcessorSpec = ArtifactDependencySpec.create(it, dependencySpec.group, dependencySpec.version)
            model.addArtifact(ANNOTATION_PROCESSOR, annotationProcessorSpec)
            result.add(annotationProcessorSpec.compactNotation())
        }
        return result
    }

    override fun getDependencyStatements(dependencySpec: ArtifactDependencySpec): List<String> {
        val result = mutableListOf("${CommonConfigurationNames.IMPLEMENTATION} '${dependencySpec.compactNotation()}'")
        val compilerName = dependencySpec.annotationProcessorName()
        compilerName?.let {
            val annotationProcessorSpec = ArtifactDependencySpec.create(it, dependencySpec.group, dependencySpec.version)
            result.add("$ANNOTATION_PROCESSOR '${annotationProcessorSpec.compactNotation()}'")
        }
        return result
    }
}

private fun ArtifactDependencySpec.hasAnnotationProcessor(): Boolean {
    return "$group:$name" in ARTIFACTS_WITH_ANNOTATION_PROCESSORS
}

private fun ArtifactDependencySpec.annotationProcessorName(): String? {
    return ARTIFACTS_WITH_ANNOTATION_PROCESSORS["$group:$name"]
}

private val ARTIFACTS_WITH_ANNOTATION_PROCESSORS = mapOf(
        "com.google.dagger:dagger" to "dagger-compiler",
        "com.jakewharton:butterknife" to "butterknife-compiler",
        "com.google.auto.value:auto-value" to "auto-value"
)
