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

import org.polycreo.chunkrequests.ChunkRequest
import org.polycreo.chunkrequests.Chunkable
import org.polycreo.chunkrequests.Chunkable.PaginationRelation
import org.polycreo.chunkrequests.Direction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

private val logger = mu.KotlinLogging.logger {}

/**
 * Extracts paging information from web requests and thus allows injecting [Chunkable] instances into controller
 * methods. Request properties to be parsed can be configured. Default configuration uses request parameters beginning
 * with [DEFAULT_NEXT_PARAMETER], [DEFAULT_PREV_PARAMETER], [DEFAULT_SIZE_PARAMETER],
 * [DEFAULT_DIRECTION_PARAMETER], [DEFAULT_QUALIFIER_DELIMITER].
 *
 *
 * Example usage:
 * <pre>`public class WebMvcConfiguration implements WebMvcConfigurer {
 * // ...
 *
 * public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
 * argumentResolvers.add(new ChunkableHandlerMethodArgumentResolver());
 * }
 * }
`</pre> *
 */
open class ChunkableHandlerMethodArgumentResolver : HandlerMethodArgumentResolver {

    var fallbackChunkable = DEFAULT_CHUNK_REQUEST

    protected var nextParameterName = DEFAULT_NEXT_PARAMETER
        set

    protected var prevParameterName = DEFAULT_PREV_PARAMETER
        set

    protected var sizeParameterName = DEFAULT_SIZE_PARAMETER
        set

    protected var directionParameterName = DEFAULT_DIRECTION_PARAMETER
        set

    /**
     * a general prefix to be prepended to the page number and page size parameters. Useful to namespace the
     * property names used in case they are clashing with ones used by your application. By default, no prefix is used.
     */
    protected var prefix = DEFAULT_PREFIX
        set

    protected var qualifierDelimiter = DEFAULT_QUALIFIER_DELIMITER
        set

    protected var maxPageSize = DEFAULT_MAX_PAGE_SIZE
        set

    /**
     * Returns whether the given [Chunkable] is the fallback one.
     */
    fun isFallbackChunkable(chunkable: Chunkable): Boolean = fallbackChunkable == chunkable

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == Chunkable::class.java

    @Throws(Exception::class)
    override fun resolveArgument(
        methodParameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        SpringDataChunkableAnnotationUtils.assertChunkableUniqueness(methodParameter)
        val next = webRequest.getParameter(getParameterNameToUse(nextParameterName, methodParameter))
        val prev = webRequest.getParameter(getParameterNameToUse(prevParameterName, methodParameter))
        val pageSizeString = webRequest.getParameter(getParameterNameToUse(sizeParameterName, methodParameter))
        val directionString = webRequest.getParameter(getParameterNameToUse(directionParameterName, methodParameter))
        val defaultOrFallback = getDefaultFromAnnotationOrFallback(methodParameter)
        if (listOf(next, prev, pageSizeString, directionString).all { it.isNullOrEmpty() }) {
            return defaultOrFallback
        }
        val pageSize = computePageSize(pageSizeString, defaultOrFallback)
        val direction = Direction.fromOptionalString(directionString).orElse(null)
        return when {
            next.isNullOrEmpty() == false -> ChunkRequest(next, PaginationRelation.NEXT, pageSize, direction)
            prev.isNullOrEmpty() == false -> ChunkRequest(prev, PaginationRelation.PREV, pageSize, direction)
            else -> ChunkRequest(pageSize, direction)
        }
    }

    private fun computePageSize(pageSizeString: String?, defaultOrFallback: Chunkable): Int {
        var pageSize = if (defaultOrFallback.maxPageSize != null) defaultOrFallback.maxPageSize else maxPageSize
        if (pageSizeString.isNullOrEmpty() == false) {
            try {
                pageSize = pageSizeString.toInt()
            } catch (e: NumberFormatException) {
                logger.trace { "invalid page size: $pageSizeString" }
            }
        }
        // Limit lower bound
        pageSize = if (pageSize < 1) 1 else pageSize
        // Limit upper bound
        pageSize = if (pageSize > maxPageSize) maxPageSize else pageSize
        return pageSize
    }

    private fun getDefaultFromAnnotationOrFallback(methodParameter: MethodParameter): Chunkable {
        return if (methodParameter.hasParameterAnnotation(ChunkableDefault::class.java)) {
            getDefaultChunkRequestFrom(methodParameter)
        } else fallbackChunkable
    }

    private fun getDefaultChunkRequestFrom(parameter: MethodParameter): Chunkable {
        val defaults = parameter.getParameterAnnotation(ChunkableDefault::class.java)
                ?: throw IllegalArgumentException("MethodParameter must have @ChunkableDefault")
        var defaultPageSize: Int = defaults.size
        if (defaultPageSize == @Suppress("MagicNumber") 10) {
            defaultPageSize = defaults.value
        }
        if (defaultPageSize < 1) {
            throw IllegalStateException("Invalid default page size configured for method ${parameter.method}! " +
                    "Must not be less than one!")
        }
        return ChunkRequest(defaultPageSize, defaults.direction)
    }

    /**
     * Returns the name of the request parameter to find the [Chunkable] information in. Inspects the given
     * [MethodParameter] for [Qualifier] present and prefixes the given source parameter name with it.
     *
     * @param source the basic parameter name.
     * @param parameter the [MethodParameter] potentially qualified.
     * @return the name of the request parameter.
     */
    protected fun getParameterNameToUse(source: String, parameter: MethodParameter): String {
        val builder = StringBuilder(prefix)
        parameter.getParameterAnnotation(Qualifier::class.java)?.let {
            builder.append(it.value).append(qualifierDelimiter)
        }
        return builder.append(source).toString()
    }

    companion object {
        private const val DEFAULT_NEXT_PARAMETER = "next"

        private const val DEFAULT_PREV_PARAMETER = "prev"

        private const val DEFAULT_SIZE_PARAMETER = "size"

        private const val DEFAULT_DIRECTION_PARAMETER = "direction"

        private const val DEFAULT_PREFIX = ""

        private const val DEFAULT_QUALIFIER_DELIMITER = "_"

        private const val DEFAULT_MAX_PAGE_SIZE = 2000

        val DEFAULT_CHUNK_REQUEST: Chunkable = ChunkRequest(null, null, DEFAULT_MAX_PAGE_SIZE, null)
    }
}
