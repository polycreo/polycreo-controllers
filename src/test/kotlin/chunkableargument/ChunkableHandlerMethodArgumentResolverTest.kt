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

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isSameAs
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.polycreo.chunkrequests.Chunkable
import org.polycreo.chunkrequests.Chunkable.PaginationRelation
import org.polycreo.chunkrequests.Direction
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * Test for [ChunkableHandlerMethodArgumentResolver].
 */
@ExtendWith(MockKExtension::class)
@Suppress("unused", "UNUSED_PARAMETER")
class ChunkableHandlerMethodArgumentResolverTest {

    @RelaxedMockK
    lateinit var mavContainer: ModelAndViewContainer

    @RelaxedMockK
    lateinit var webRequest: NativeWebRequest

    @RelaxedMockK
    lateinit var binderFactory: WebDataBinderFactory

    var sut = ChunkableHandlerMethodArgumentResolver()

    fun simpleHandler(chunkable: Chunkable?) {
    }

    @Test
    fun testSimpleHandler() {
        // setup
        val method = javaClass.getMethod("simpleHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual).isSameAs(sut.fallbackChunkable)
        assertThat(actual.paginationRelation).isNull()
        assertThat(actual.paginationToken).isNull()
        assertThat(actual.maxPageSize).isEqualTo(2000)
        assertThat(actual.direction).isNull()
    }

    fun defaultHandler(@ChunkableDefault chunkable: Chunkable?) {
    }

    @Test
    fun testDefaultHandler() {
        // setup
        val method = javaClass.getMethod("defaultHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isNull()
        assertThat(actual.paginationToken).isNull()
        assertThat(actual.maxPageSize).isEqualTo(10)
        assertThat(actual.direction).isEqualTo(Direction.ASC)
    }

    fun defaultHandlerWithValue(@ChunkableDefault(size = 123) chunkable: Chunkable?) {
    }

    @Test
    fun testDefaultHandlerWithValue() {
        // setup
        val method = javaClass.getMethod("defaultHandlerWithValue", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isNull()
        assertThat(actual.paginationToken).isNull()
        assertThat(actual.maxPageSize).isEqualTo(123)
        assertThat(actual.direction).isEqualTo(Direction.ASC)
    }

    fun multipleChunkable(c1: Chunkable?, c2: Chunkable?) {
    }

    @Test
    fun testMultipleChunkable() {
        // setup
        val method =
            javaClass.getMethod("multipleChunkable", Chunkable::class.java, Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        // assert
        assertThat {
            // exercise
            sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory)
        }.isFailure().all {
            isInstanceOf(IllegalStateException::class)
        }
    }

    @Test
    fun testSimpleHandlerWithSizeParameter() {
        // setup
        val method = javaClass.getMethod("simpleHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("size") } returns "123"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isNull()
        assertThat(actual.paginationToken).isNull()
        assertThat(actual.maxPageSize).isEqualTo(123)
        assertThat(actual.direction).isNull()
    }

    @Test
    fun testDefaultHandlerWithSizeParameter() {
        // setup
        val method = javaClass.getMethod("defaultHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("size") } returns "123"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isNull()
        assertThat(actual.paginationToken).isNull()
        assertThat(actual.maxPageSize).isEqualTo(123)
        assertThat(actual.direction).isNull()
    }

    @Test
    fun testDefaultHandlerWithValueWithSizeParameter() {
        // setup
        val method = javaClass.getMethod("defaultHandlerWithValue", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("size") } returns "234"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isNull()
        assertThat(actual.paginationToken).isNull()
        assertThat(actual.maxPageSize).isEqualTo(234)
        assertThat(actual.direction).isNull()
    }

    @Test
    fun testSimpleHandlerWithExceededSizeParameter() {
        // setup
        val method = javaClass.getMethod("simpleHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("size") } returns "12345"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isNull()
        assertThat(actual.paginationToken).isNull()
        assertThat(actual.maxPageSize).isEqualTo(2000)
        assertThat(actual.direction).isNull()
    }

    @Test
    fun testDefaultHandlerWithExceededSizeParameter() {
        // setup
        val method = javaClass.getMethod("defaultHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("size") } returns "12345"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isNull()
        assertThat(actual.paginationToken).isNull()
        assertThat(actual.maxPageSize).isEqualTo(2000)
        assertThat(actual.direction).isNull()
    }

    @Test
    fun testNegativeSize() {
        // setup
        val method = javaClass.getMethod("simpleHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("size") } returns "-1"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isNull()
        assertThat(actual.paginationToken).isNull()
        assertThat(actual.maxPageSize).isEqualTo(1)
        assertThat(actual.direction).isNull()
    }

    @Test
    fun testSimpleHandlerWithIllegalSizeParameter() {
        // setup
        val method = javaClass.getMethod("simpleHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("size") } returns "foo"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isNull()
        assertThat(actual.paginationToken).isNull()
        assertThat(actual.maxPageSize).isEqualTo(2000)
        assertThat(actual.direction).isNull()
    }

    @Test
    fun testDefaultHandlerWithIllegalSizeParameter() {
        // setup
        val method = javaClass.getMethod("defaultHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("size") } returns "foo"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isNull()
        assertThat(actual.paginationToken).isNull()
        assertThat(actual.maxPageSize).isEqualTo(10)
        assertThat(actual.direction).isNull()
    }

    @Test
    fun testDefaultHandlerWithValueWithIllegalSizeParameter() {
        // setup
        val method = javaClass.getMethod("defaultHandlerWithValue", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("size") } returns "foo"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isNull()
        assertThat(actual.paginationToken).isNull()
        assertThat(actual.maxPageSize).isEqualTo(123)
        assertThat(actual.direction).isNull()
    }

    @Test
    fun testAfter() {
        // setup
        val method = javaClass.getMethod("simpleHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("next") } returns "100"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isEqualTo(PaginationRelation.NEXT)
        assertThat(actual.paginationToken).isNotNull() // TODO assert last=100
        assertThat(actual.maxPageSize).isEqualTo(2000)
        assertThat(actual.direction).isNull()
    }

    @Test
    fun testBefore() {
        // setup
        val method = javaClass.getMethod("simpleHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("prev") } returns "89"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isEqualTo(PaginationRelation.PREV)
        assertThat(actual.paginationToken).isNotNull() // TODO assert first=89
        assertThat(actual.maxPageSize).isEqualTo(2000)
        assertThat(actual.direction).isNull()
    }

    @Test
    fun testAfterAndSize() {
        // setup
        val method = javaClass.getMethod("simpleHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("size") } returns "10"
        every { webRequest.getParameter("next") } returns "100"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isEqualTo(PaginationRelation.NEXT)
        assertThat(actual.paginationToken).isNotNull() // TODO assert last=100
        assertThat(actual.maxPageSize).isEqualTo(10)
        assertThat(actual.direction).isNull()
    }

    @Test
    fun testDirection() {
        // setup
        val method = javaClass.getMethod("simpleHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("direction") } returns "DESC"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isNull()
        assertThat(actual.paginationToken).isNull()
        assertThat(actual.maxPageSize).isEqualTo(2000)
        assertThat(actual.direction).isEqualTo(Direction.DESC)
    }

    @Test
    fun testIllegalDirection() {
        // setup
        val method = javaClass.getMethod("simpleHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("direction") } returns "FOO"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isNull()
        assertThat(actual.paginationToken).isNull()
        assertThat(actual.maxPageSize).isEqualTo(2000)
        assertThat(actual.direction).isNull()
    }

    @Test
    fun testFull() {
        // setup
        val method = javaClass.getMethod("simpleHandler", Chunkable::class.java)
        val methodParameter = MethodParameter(method, 0)
        every { webRequest.getParameter("size") } returns "10"
        every { webRequest.getParameter("next") } returns "89"
        every { webRequest.getParameter("prev") } returns "100"
        every { webRequest.getParameter("direction") } returns "DESC"
        // exercise
        val actual = sut.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory) as Chunkable
        // verify
        assertThat(actual.paginationRelation).isEqualTo(PaginationRelation.NEXT)
        assertThat(actual.paginationToken).isNotNull() // TODO assert first=89
        assertThat(actual.maxPageSize).isEqualTo(10)
        assertThat(actual.direction).isEqualTo(Direction.DESC)
    }
}
