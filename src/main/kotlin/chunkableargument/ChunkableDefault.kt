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

import org.polycreo.chunkrequests.Direction

/**
 * Annotation to set defaults when injecting a [org.polycreo.chunkrequests.Chunkable] into a controller method.
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ChunkableDefault(

    /**
     * Alias for [size]. Prefer to use the [size] method as it makes the annotation declaration more
     * expressive.
     */
    val value: Int = 10,

    /**
     * The default-size the injected [org.polycreo.chunkrequests.Chunkable] should get if no corresponding
     * parameter defined in request (default is 10).
     */
    val size: Int = 10,

    /**
     * The direction to sort by. Defaults to [Direction.ASC].
     */
    val direction: Direction = Direction.ASC
)
