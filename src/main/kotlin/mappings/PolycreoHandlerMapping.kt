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

import com.github.fge.jsonpatch.JsonPatch
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch
import java.lang.reflect.Method
import javax.servlet.http.HttpServletRequest
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/**
 * TODO miyamoto.daisuke.
 */
class PolycreoHandlerMapping : RequestMappingHandlerMapping() {

    companion object {
        const val WS2TEN1_ACTION_ATTRIBUTE: String = "org.polycreo.mapping.PolycreoHandlerMapping.actionName"
    }

    override fun isHandler(beanType: Class<*>): Boolean {
        return super.isHandler(beanType) ||
                AnnotationUtils.findAnnotation(beanType, PolycreoController::class.java) != null
    }

    @Suppress("ComplexMethod", "ReturnCount", "MagicNumber")
    override fun getMappingForMethod(method: Method, handlerType: Class<*>): RequestMappingInfo? {
        if (AnnotatedElementUtils.hasAnnotation(method, PolycreoHandler::class.java) == false) {
            return super.getMappingForMethod(method, handlerType)
        }

        val controllerAnnotation = AnnotatedElementUtils.findMergedAnnotation(
            handlerType,
            PolycreoController::class.java
        ) ?: return null

        val builder = when (method.name) {
            "list", "create", "deleteAll" -> RequestMappingInfo
                .paths("/" + controllerAnnotation.pluralizedResourceName)
            "get", "update", "delete", "patch", "upsert" -> RequestMappingInfo
                .paths("/" + controllerAnnotation.pluralizedResourceName + "/{id}")
            else -> return null
        }

        when (method.name) {
            "list", "get" -> builder.methods(RequestMethod.GET)
            "create", "update" -> builder.methods(RequestMethod.POST)
            "upsert" -> builder.methods(RequestMethod.PUT)
            "patch" -> builder.methods(RequestMethod.PATCH)
            "delete", "deleteAll" -> builder.methods(RequestMethod.DELETE)
            else -> return null
        }

        if (method.name == "update") { // TODO refactor MagicNumber
            if (method.parameterCount == 3 && method.parameters[2].name == "version") {
                builder.params("version")
            }
        }

        if (method.name == "patch") { // TODO refactor MagicNumber
            if (method.parameterTypes[1] == JsonMergePatch::class.java) {
                builder.consumes(MediaType.APPLICATION_JSON_VALUE, "application/merge-patch+json")
            } else if (method.parameterTypes[1] == JsonPatch::class.java) {
                builder.consumes("application/json-patch+json")
            }
        }

        val actionName = method.name.capitalize() + controllerAnnotation.resourceName.capitalize()
        return builder.mappingName(actionName)
            .build()
    }

    override fun handleMatch(info: RequestMappingInfo, lookupPath: String, request: HttpServletRequest) {
        super.handleMatch(info, lookupPath, request)
        request.setAttribute(WS2TEN1_ACTION_ATTRIBUTE, info.name)
    }
}
