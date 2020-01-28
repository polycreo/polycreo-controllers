/*
 * Copyright 2017 the original author or authors.
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
package org.polycreo.presentation.mappings

import org.springframework.web.bind.annotation.RequestMethod

/**
 * Framework handler method.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class PolycreoHandler(
    val actionVerb: String,
    val method: RequestMethod,
    val pathType: PathType,
    val pathPrefix: String = "",
    val pathSuffix: String = "",
    val headers: Array<String> = [],
    val params: Array<String> = [],
    val consumes: Array<String> = [],
    val produces: Array<String> = []
)

enum class PathType {

    ENTIRE_COLLECTION,

    SPECIFIC_ITEM
}
