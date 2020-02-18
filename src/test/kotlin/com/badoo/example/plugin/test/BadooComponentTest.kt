package com.badoo.example.plugin.test

interface BadooComponentTest {
    fun cleanupTest() {
        // ApplicationManager.getApplication().getComponent(ApplicationComponent::class.java).disposeComponent()
    }

    fun getTestDataPath(): String
}