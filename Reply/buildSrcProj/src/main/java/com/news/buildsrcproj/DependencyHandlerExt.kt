package com.news.buildsrcproj

import org.gradle.api.artifacts.dsl.DependencyHandler

internal fun DependencyHandler.ksp(dependency: Any) {
    addDependency("ksp", dependency)
}

internal fun DependencyHandler.implementation(dependency: Any) {
    addDependency("implementation", dependency)
}
private fun DependencyHandler.addDependency(tag: String, dependency: Any) {
    if (dependency is Collection<*>) {
        dependency.forEach {
            add(tag, it!!)
        }
    } else {
        add(tag, dependency)
    }
}