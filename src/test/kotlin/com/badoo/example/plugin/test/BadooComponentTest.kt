package com.badoo.example.plugin.test

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import java.io.File

interface BadooComponentTest {
    fun cleanupTest() {
        // ApplicationManager.getApplication().getComponent(ApplicationComponent::class.java).disposeComponent()
    }

    fun getTestDataPath(): String
}