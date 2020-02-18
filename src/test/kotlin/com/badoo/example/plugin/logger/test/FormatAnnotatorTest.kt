package com.badoo.example.plugin.logger.test

import com.badoo.example.plugin.logger.LoggerFormatInspection
import com.badoo.example.plugin.test.BadooComponentTest
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

class FormatAnnotatorTest : LightJavaCodeInsightFixtureTestCase(), BadooComponentTest {
    override fun getTestDataPath() =
            "src/test/kotlin/com/badoo/example/plugin/logger/test/testData/"

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(LoggerFormatInspection())
    }
    override fun tearDown() {
        cleanupTest()
        super.tearDown()
    }

    fun testAllIsOk()
    {
        myFixture.configureByFile("FormatAnnotator1.php")
        myFixture.testHighlighting(true, false, true)
    }

    fun testExtraParameter()
    {
        myFixture.configureByFile("FormatAnnotator2.php")
        myFixture.testHighlighting(true, false, true)

    }

    fun testNotEnoughParameters()
    {
        myFixture.configureByFile("FormatAnnotator3.php")
        myFixture.testHighlighting(true, false, true)
    }

    fun testLoggerProperty()
    {
        myFixture.configureByFile("FormatAnnotator5.php")
        myFixture.testHighlighting(true, false, true)
    }
}