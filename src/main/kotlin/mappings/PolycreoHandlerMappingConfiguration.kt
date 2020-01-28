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

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/**
 * TODO miyamoto.daisuke.
 */
@Profile("web")
@Configuration
class PolycreoHandlerMappingConfiguration {

    /**
     * Replace requestMappingHandlerMapping bean with PolycreoHandlerMapping by [WebMvcRegistrations].
     *
     * see WebMvcConfigurationSupport#requestMappingHandlerMapping
     * and WebMvcAutoConfiguration.EnableWebMvcConfiguration#createRequestMappingHandlerMapping
     */
    @Bean
    fun webMvcRegistrationsHandlerMapping(): WebMvcRegistrations = object : WebMvcRegistrations {
        override fun getRequestMappingHandlerMapping(): RequestMappingHandlerMapping {
            return PolycreoHandlerMapping()
        }
    }
}

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(PolycreoHandlerMappingConfiguration::class)
annotation class EnablePolycreoHandlerMapping
