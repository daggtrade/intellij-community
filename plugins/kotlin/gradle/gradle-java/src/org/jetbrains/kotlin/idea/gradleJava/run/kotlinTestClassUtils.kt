// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.gradleJava.run

import com.intellij.execution.Location
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.asJava.classes.KtFakeLightClass
import org.jetbrains.kotlin.asJava.classes.KtFakeLightMethod
import org.jetbrains.kotlin.asJava.toFakeLightClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

internal fun getTestMethodForKotlinTest(location: Location<*>): PsiMethod? {
    val leaf = when (val psi = location.psiElement) {
        is KtLightElement<*, *> -> psi.kotlinOrigin
        else -> psi
    }
    val function = leaf?.getParentOfType<KtNamedFunction>(false) ?: return null
    return KtFakeLightMethod.get(function)
}

internal fun getTestClassForKotlinTest(location: Location<*>): PsiClass? {
    val leaf = when (val psi = location.psiElement) {
        is KtLightElement<*, *> -> psi.kotlinOrigin
        else -> psi
    }
    val owner = leaf?.getParentOfType<KtDeclaration>(false) as? KtClassOrObject ?: return null
    return owner.toFakeLightClass()
}