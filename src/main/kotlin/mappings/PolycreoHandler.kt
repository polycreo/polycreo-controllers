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
 * Polycreo style handler method.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class PolycreoHandler(

	/**
	 * The HTTP request methods to map to.
	 */
    val method: RequestMethod,

    /**
     * Base path pattern.
     */
    val pathType: PathType,

    /**
     * Prefix of path pattern.
     */
    val pathPrefix: String = "",

    /**
     * Suffix of path pattern.
     */
    val pathSuffix: String = "",

	/**
	 * The parameters of the mapped request.
     *
     * @see org.springframework.web.bind.annotation.RequestMapping.params
     */
    val params: Array<String> = [],

    /**
     * The headers of the mapped request.
     *
     * @see org.springframework.web.bind.annotation.RequestMapping.headers
     */
    val headers: Array<String> = [],

    /**
	 * Narrows the primary mapping by media types that can be consumed by the
	 * mapped handler. Consists of one or more media types one of which must
	 * match to the request `Content-Type` header.
     *
     * @see org.springframework.web.bind.annotation.RequestMapping.consumes
     */
    val consumes: Array<String> = [],

    /**
	 * Narrows the primary mapping by media types that can be produced by the
	 * mapped handler. Consists of one or more media types one of which must
	 * be chosen via content negotiation against the "acceptable" media types
	 * of the request. Typically those are extracted from the `"Accept"`
	 * header but may be derived from query parameters, or other.
     *
     * @see org.springframework.web.bind.annotation.RequestMapping.produces
     */
    val produces: Array<String> = []
)

/**
 * REST-ful URL path types.
 */
enum class PathType(private val pathTemplate: String) {

    /**
     * Entire Collection (e.g. `/customers`)
     */
    ENTIRE_COLLECTION("%s/%s%s"),

    /**
     * Specific Item (e.g. `/customers/{id}`)
     */
    SPECIFIC_ITEM("%s/%s/{id}%s");

    /**
     * Build path pattern from [controller] and [handler] metadata.
     */
    fun build(controller: PolycreoController, handler: PolycreoHandler) =
        pathTemplate.format(handler.pathPrefix, controller.pluralizedResourceName, handler.pathSuffix)
}
