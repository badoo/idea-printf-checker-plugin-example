package com.badoo.example.plugin

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.PhpPsiElement
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.elements.Variable
import com.jetbrains.php.lang.psi.elements.impl.*
import com.jetbrains.php.lang.psi.resolve.types.PhpType
import java.util.ArrayList

object PhpUtil {
    fun resolveType(element: PhpPsiElement) : String? {
        return when (element) {
            is Variable -> resolveOrReturnType(element.type, element.project)
            is ClassReferenceImpl -> element.type.toStringResolved()
            is MethodReferenceImpl -> {
                // Class::method(), $variable->method()
                resolveOrReturnType(element.type, element.project)
            }
            is FieldReferenceImpl -> {// $this->Logger
                resolveOrReturnType(element.type, element.project)
            }
            else -> null
        }
    }

    private fun resolveOrReturnType(type: PhpType, project: Project): String? {
        val typesList = type.typesSorted.filter { it != "?" }
        if (typesList.isEmpty()) {
            return null
        }

        val typeSignature = typesList.last()

        if (typeSignature.startsWith("#M#C") || typeSignature.startsWith("#P#C")) {
            val (classFqn, methodName) = parseMethodCall(typeSignature)

            val phpClass = PhpIndex.getInstance(project).getClassesByFQN(classFqn)
            if (phpClass.isEmpty()) {
                return null
            }
            val method = phpClass.first().findMethodByName(methodName)
            if (method == null) {
                val field = phpClass.first().findFieldByName(methodName, false)
                if (field != null) {
                    return field.type.toStringResolved()
                }
                return null
            }

            return method.type.toStringResolved()
        } else if (typeSignature.startsWith("#C")) {
            val functionFqn =
                    typeSignature
                            .substring(0, typeSignature.indexOf('|'))
                            .removePrefix("#C")
            val funName  = PhpIndex.getInstance(project).getFunctionsByFQN(functionFqn)
            if (funName.isEmpty()) {
                return null
            }

            return funName.first().type.toStringResolved()
        } else if (typeSignature.startsWith("#\u042E")) {
            /*
            #Ю\Spam\EqualUsers\Spam_EqualUsers_BlockedUsersWatchDogTestЮ\Spam\EqualUsers\BlockedUsersWatchDog|\Spam\EqualUsers\BlockedUsersWatchDog|?
             */
            val types = typeSignature.split('|')
                    .filter { it != "?" }
            if (types.isEmpty()) {
                return null
            }

            return types.last()
        }

        return typeSignature
    }

    private fun parseMethodCall(callReference: String) : Pair<String, String> {
        val pipeIndex = callReference.indexOf('|')

        val firstArgReference =
                callReference
                        .removePrefix("#M#C")
                        .removePrefix("#P#C")

        val classFqn = firstArgReference.substring(0, firstArgReference.lastIndexOf('.'))
        val methodName = firstArgReference.substring(firstArgReference.lastIndexOf('.') + 1)

        return Pair(classFqn, methodName)
    }

    fun expressionValue(element: PsiElement, targets: ArrayList<PsiElement>? = null) : String? {
        if (DumbService.isDumb(element.project)) {
            // IDEA is updating indexes, we should not try to use them meanwhile
            // @see documentation for com.intellij.openapi.project.IndexNotReadyException
            return null
        }

        when (element) {
            is StringLiteralExpression -> {
                return expressionValue(element)
            }
            is ClassConstantReferenceImpl -> {
                return expressionValue(element, targets)
            }
            is ConstantReferenceImpl -> {
                return expressionValue(element, targets)
            }
            is ClassConstImpl -> {
                return expressionValue(element, targets)
            }
            is LeafPsiElement -> {
                return expressionValue(element.parent, targets)
            }
            is PhpExpressionImpl -> {
                if (element.type == PhpType.INT) {
                    return element.text
                } else {
                    return null
                }
            }
            else -> {
                return null
            }
        }
    }

    private fun expressionValue(stringLiteral : StringLiteralExpression) : String {
        return stringLiteral.contents
    }

    private fun expressionValue(constantReference: ClassConstantReferenceImpl, targets: ArrayList<PsiElement>?) : String? {
        val phpClassReference = constantReference.classReference as ClassReferenceImpl
        val phpClass = PhpIndex.getInstance(constantReference.project).getClassesByFQN("${phpClassReference.fqn}")

        if (!phpClass.isEmpty()) {
            val field = phpClass.first().findFieldByName(constantReference.name, true)

            if (field?.defaultValue != null) {
                targets?.add(field)
                return expressionValue(field.defaultValue!!, targets)
            }
        }

        return null
    }

    private fun expressionValue(constantReference: ConstantReferenceImpl, targets: ArrayList<PsiElement>?) : String? {
        val constant = PhpIndex.getInstance(constantReference.project).getConstantsByFQN(constantReference.fqn)

        if (!constant.isEmpty()) {
            targets?.addAll(constant)

            val field = constant.first()
            if (field.value != null) {
                return expressionValue(field.value!!, targets)
            }
        } else {
            val constantByName = PhpIndex.getInstance(constantReference.project).getConstantsByName(constantReference.name)
            if (!constantByName.isEmpty()) {
                targets?.addAll(constantByName)

                val field = constantByName.first()
                if (field.value != null) {
                    return expressionValue(field.value!!, targets)
                }
            }
        }

        return null
    }

    private fun expressionValue(classConstant: ClassConstImpl, targets: ArrayList<PsiElement>?) : String? {
        val value = classConstant.defaultValue
        if (value != null) {
            return expressionValue(value, targets)
        }

        return null
    }
}
