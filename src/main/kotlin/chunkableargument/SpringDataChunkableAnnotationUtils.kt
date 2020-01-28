/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.polycreo.presentation.chunkableargument

import java.lang.reflect.Method
import java.util.HashSet
import org.polycreo.chunkrequests.Chunkable
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.MethodParameter

internal object SpringDataChunkableAnnotationUtils {

    /**
     * Asserts uniqueness of all [Chunkable] parameters of the method of the given [parameter].
     */
    fun assertChunkableUniqueness(parameter: MethodParameter) {
        val method = parameter.method ?: throw IllegalArgumentException("MethodParameter must has method")
        if (containsMoreThanOneChunkableParameter(method)) {
            assertQualifiersFor(method.parameterTypes, method.parameterAnnotations)
        }
    }

    /**
     * Returns whether the given [method] has more than one [Chunkable] parameter.
     */
    private fun containsMoreThanOneChunkableParameter(method: Method): Boolean {
        var chunkableFound = false
        for (type in method.parameterTypes) {
            if (chunkableFound && type == Chunkable::class.java) {
                return true
            }
            if (type == Chunkable::class.java) {
                chunkableFound = true
            }
        }
        return false
    }

    /**
     * Asserts that every [Chunkable] parameter of the given parameters carries an [Qualifier] annotation to
     * distinguish them from each other.
     *
     * @param parameterTypes must not be null.
     * @param annotations must not be null.
     */
    fun assertQualifiersFor(
        parameterTypes: Array<Class<*>>,
        annotations: Array<Array<Annotation?>>
    ) {
        val values: MutableSet<String> = HashSet()
        for (i in annotations.indices) {
            if (parameterTypes[i] == Chunkable::class.java) {
                @Suppress("SpreadOperator")
                val qualifier = findAnnotation(*annotations[i]) ?: throw IllegalStateException(
                    "Ambiguous Chunkable arguments in handler method." +
                    " If you use multiple parameters of type Chunkable" +
                    " you need to qualify them with @Qualifier"
                )
                check(values.contains(qualifier.value) == false) { "Values of the user Qualifiers must be unique!" }
                values.add(qualifier.value)
            }
        }
    }

    /**
     * Returns a [Qualifier] annotation from the given array of [Annotation]s. Returns null if the
     * array does not contain a [Qualifier] annotation.
     */
    fun findAnnotation(vararg annotations: Annotation?): Qualifier? =
        annotations.firstOrNull { it is Qualifier } as Qualifier?
}
