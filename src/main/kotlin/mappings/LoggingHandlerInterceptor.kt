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

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.polycreo.loggings.Markers
import org.springframework.web.servlet.HandlerInterceptor

private val logger = mu.KotlinLogging.logger {}

/**
 * TODO miyamoto.daisuke.
 */
class LoggingHandlerInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val action = request.getAttribute(PolycreoHandlerMapping.POLYCREO_ACTION_ATTRIBUTE)
        logger.info { "Action [$action] is invoked" }
        return true
    }

    override fun afterCompletion(req: HttpServletRequest, res: HttpServletResponse, handler: Any, ex: Exception?) {
        val action = req.getAttribute(PolycreoHandlerMapping.POLYCREO_ACTION_ATTRIBUTE)
        if (ex == null) {
            logger.info { "Action [$action] completed successfully: ${res.status}" }
        } else {
            logger.error(Markers.ALERT, ex) { "Action [$action] completed with unhandled error: ${res.status}" }
        }
    }
}

// ControllerAdvise または AOP を作ってパラメーター取るぞー。
