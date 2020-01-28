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

import java.lang.reflect.Method
import javax.servlet.http.HttpServletRequest
import org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.MediaType
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/**
 * [RequestMappingHandlerMapping] extension to support [PolycreoController] and [PolycreoHandler].
 */
class PolycreoHandlerMapping : RequestMappingHandlerMapping() {

    companion object {
        const val POLYCREO_ACTION_ATTRIBUTE: String = "org.polycreo.mapping.PolycreoHandlerMapping.actionName"
    }

    override fun isHandler(beanType: Class<*>): Boolean {
        return super.isHandler(beanType) ||
                AnnotationUtils.findAnnotation(beanType, PolycreoController::class.java) != null
    }

    @Suppress("SpreadOperator", "ReturnCount")
    override fun getMappingForMethod(method: Method, handlerType: Class<*>): RequestMappingInfo? {
        val methodAnnotation = findMergedAnnotation(method, PolycreoHandler::class.java)
            ?: return super.getMappingForMethod(method, handlerType)
        val controllerAnnotation = findMergedAnnotation(handlerType, PolycreoController::class.java)
            ?: return null
        return RequestMappingInfo
            .paths(determinePath(methodAnnotation, controllerAnnotation))
            .methods(methodAnnotation.method)
            .mappingName(determineActionName(methodAnnotation, controllerAnnotation))
            .params(*methodAnnotation.params)
            .headers(*methodAnnotation.headers)
            .consumes(*methodAnnotation.consumes)
            .produces(*methodAnnotation.produces)
            .build()
    }

    private fun determineActionName(
        methodAnnotation: PolycreoHandler,
        controllerAnnotation: PolycreoController
    ) = methodAnnotation.actionVerb + controllerAnnotation.resourceName.capitalize()

    private fun determinePath(
        methodAnnotation: PolycreoHandler,
        controllerAnnotation: PolycreoController
    ) = when (methodAnnotation.pathType) {
        PathType.ENTIRE_COLLECTION -> "/${controllerAnnotation.pluralizedResourceName}"
        PathType.SPECIFIC_ITEM -> "/${controllerAnnotation.pluralizedResourceName}/{id}"
    }

    override fun handleMatch(info: RequestMappingInfo, lookupPath: String, request: HttpServletRequest) {
        super.handleMatch(info, lookupPath, request)
        request.setAttribute(POLYCREO_ACTION_ATTRIBUTE, info.name)
    }
}

object MediaTypes {

    /**
     * A String equivalent of [MediaTypes.APPLICATION_JSON_MERGE_PATCH].
     */
    const val APPLICATION_JSON_MERGE_PATCH_VALUE = "application/merge-patch+json"

    /**
     * A String equivalent of [MediaTypes.APPLICATION_JSON_PATCH].
     */
    const val APPLICATION_JSON_PATCH_VALUE = "application/json-patch+json"

    /**
     * Public constant media type for `application/merge-patch+json`.
     */
    val APPLICATION_JSON_MERGE_PATCH: MediaType = MediaType(APPLICATION_JSON_MERGE_PATCH_VALUE)

    /**
     * Public constant media type for `application/json-patch+json`.
     */
    val APPLICATION_JSON_PATCH: MediaType = MediaType(APPLICATION_JSON_PATCH_VALUE)
}
