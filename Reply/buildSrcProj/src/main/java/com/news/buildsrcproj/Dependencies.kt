package com.news.buildsrcproj

import org.gradle.api.artifacts.dsl.DependencyHandler

internal object Dependencies {

    // room-db
    const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    const val room = "androidx.room:room-ktx:${Versions.room}"

}
fun DependencyHandler.roomDB() {
    ksp(com.news.buildsrcproj.Dependencies.roomCompiler)
    implementation(com.news.buildsrcproj.Dependencies.room)
}