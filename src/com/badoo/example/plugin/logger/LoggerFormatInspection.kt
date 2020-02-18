package com.badoo.example.plugin.logger

import com.badoo.example.plugin.PhpUtil
import com.intellij.codeInspection.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl

private object Inspection {
    enum class Type {
        Int,
        String,
        Double,
        InvalidExpression
    }

    enum class State {
        Text,
        Percent,  // [%]1.5f
        Modifier, // %[1.5]f
        Type,     // %1.5[f], not used
    }

    data class FormatItem(
            val rangeStart : Int,
            val rangeEnd : Int,
            val type: Type)

    val typeMap : Map<Char, Type> = hashMapOf(
            // 'i', 'd', 'f', 's', 'u', 'x', 'X', 'o'
            Pair('i', Type.Int),
            Pair('d', Type.Int),
            Pair('f', Type.Double),
            Pair('s', Type.String),
            Pair('u', Type.Int),
            Pair('x', Type.Int),
            Pair('X', Type.Int),
            Pair('o', Type.Int)
    )

    val loggerFormatMethods = setOf(
            "infof",
            "fatalf",
            "errorf",
            "warningf",
            "noticef",
            "debugf"
    )
}

class LoggerFormatInspection : LocalInspectionTool() {
    override fun getDisplayName() = "Logger format-string"

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        if (session.file !is PhpFile) {
            // нас не интересуют не PHP код
            return super.buildVisitor(holder, isOnTheFly, session)
        }
        return LoggerFormatVisitor(holder, isOnTheFly)
    }
}

class LoggerFormatVisitor(private val holder: ProblemsHolder, onTheFly: Boolean) : PsiElementVisitor() {
    override fun visitElement(element: PsiElement) {
        if (!isLoggerFormatFunction(element)) {
            return // пропускаем всё, что не похоже на функцию логгера
        }

        checkMethodReference(element as MethodReference) //
    }

    private fun checkMethodReference(element : MethodReference) {
        // сигнатура printf - следующя:
        // первый (нулевой) аргумент формат-строка, если таковой нет, то выходим в расчёте на то, что программист ещё не дописал её (хотя, это решение спорно)
        // начиная со второго, идут агрументы формата - вот их-то и будем проверять на соответствие
        val printfArgument = 0
        val firstPossibleArgument = printfArgument + 1
        if (element.parameters.size <= printfArgument) {
            return
        }

        val formatLine = element.parameters[printfArgument] as? StringLiteralExpression // если первый аргумент-не строка -- выходим
                ?: return

        val expectingParameters = getExpectingParameters(formatLine)
        val arguments = element.parameters.slice(IntRange(firstPossibleArgument, element.parameters.size - 1))

        var problems = 0
        if (expectingParameters.isEmpty() && arguments.isNotEmpty()) {
            holder.registerProblem(arguments.first(), "No format item found in first parameter but call has more than one argument", ProblemHighlightType.WARNING)
            ++ problems
        } else if (expectingParameters.isNotEmpty()) {
            var expectingIndex = 0
            for (i in arguments.indices) {
                if (expectingParameters.size <= expectingIndex) {
                    holder.registerProblem(
                            arguments[i],
                            "Format line expecting only ${expectingParameters.size} parameters",
                            ProblemHighlightType.WARNING
                    )
                    ++ problems
                    continue
                }
                ++ expectingIndex
            }

            if (arguments.size < expectingParameters.size) {
                for (i in arguments.size until expectingParameters.size) {
                    val item = expectingParameters[i]
                    holder.registerProblem(
                            formatLine,
                            "Unused format item",
                            ProblemHighlightType.WARNING,
                            TextRange(item.rangeStart + 1, item.rangeEnd + 2)
                    )
                    ++ problems
                }
            }
        }

        if (problems > 0) {
            val elementStart = element.textOffset
            val nameNodeTextRange = element.nameNode!!.textRange

            val nameTextRange = TextRange(nameNodeTextRange.startOffset - elementStart, nameNodeTextRange.endOffset - elementStart)
            holder.registerProblem(element, nameTextRange, "Invalid format function usage")
        }
    }

    private fun isLoggerFormatFunction(element: PsiElement?): Boolean {
        val methodReference = element as? MethodReferenceImpl
                ?: return false;

        if (PhpUtil.resolveType(methodReference.firstChild as PhpPsiElement) != "\\Logger\\Logger") {
            return false
        }

        return Inspection.loggerFormatMethods.contains(methodReference.name)
    }

    private fun getExpectingParameters(stringLiteral : StringLiteralExpression) : Array<Inspection.FormatItem>
    {
        val result = arrayListOf<Inspection.FormatItem>()

        val string = stringLiteral.contents
        var state = Inspection.State.Text
        var charIndex = 0
        var expressionStartIndex = 0

        loop@ for (c in string) {
            // %1.5f
            // %05d
            // %s
            // %60.60s
            when (c) {
                '%' -> {
                    if (state == Inspection.State.Percent) { // %% case
                        state = Inspection.State.Text
                    } else {
                        expressionStartIndex = charIndex
                        state = Inspection.State.Percent
                    }
                }

                'i', 'd', 'f', 's', 'u', 'x', 'X', 'o' -> {
                    if (state == Inspection.State.Percent || state == Inspection.State.Modifier) {
                        result.add(Inspection.FormatItem(expressionStartIndex, charIndex, Inspection.typeMap[c]!!))
                    }

                    state = Inspection.State.Text
                }

                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' -> {
                    if (state == Inspection.State.Percent || state == Inspection.State.Modifier) {
                        state = Inspection.State.Modifier
                    }
                }

                else -> {
                    if (state == Inspection.State.Percent || state == Inspection.State.Modifier) {
                        result.add(Inspection.FormatItem(expressionStartIndex, charIndex, Inspection.Type.InvalidExpression))
                    }
                    state = Inspection.State.Text
                }
            }

            ++ charIndex
        }

        return result.toTypedArray()
    }
}