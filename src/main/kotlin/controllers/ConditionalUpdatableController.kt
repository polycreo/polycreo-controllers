package org.polycreo.presentation.controllers

import java.io.Serializable
import org.polycreo.httpexceptions.HttpConflictException
import org.polycreo.httpexceptions.HttpNotFoundException
import org.polycreo.presentation.mappings.PathType
import org.polycreo.presentation.mappings.PolycreoHandler
import org.polycreo.presentation.usecases.ConditionalUpdatableUsecase
import org.polycreo.services.NotFoundException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

private val logger = mu.KotlinLogging.logger {}

interface ConditionalUpdatableController<E, ID : Serializable, C, R : UpdateRequest<E>> {

    val usecase: ConditionalUpdatableUsecase<E, ID, C>

    @PolycreoHandler(
        actionVerb = "Update",
        method = RequestMethod.POST,
        pathType = PathType.SPECIFIC_ITEM,
        params = ["version"]
    )
    @PreAuthorize("hasAnyAuthority('ROOT', #authorityPrefix + 'Update' + #resourceName)")
    fun update(
        @PathVariable id: ID,
        @Validated @RequestBody request: R,
        @RequestParam version: C?
    ): ResponseEntity<E> {
        try {
            val updated = usecase.update(id, request, version)
            logger.info { "Success updated: $updated" }
            return ResponseEntity.ok(updated)
        } catch (e: NotFoundException) {
            val message = "Failed to update: $id is not found"
            logger.error { message }
            throw HttpNotFoundException(message, e)
        } catch (e: OptimisticLockingFailureException) {
            val message = "Failed to update: optimistic lock for $id is failed.  Expected version is $version"
            logger.error { message }
            throw HttpConflictException(message, e)
        }
    }
}
