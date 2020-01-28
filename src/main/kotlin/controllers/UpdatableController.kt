package org.polycreo.presentation.controllers

import java.io.Serializable
import java.util.function.UnaryOperator
import org.polycreo.httpexceptions.HttpBadRequestException
import org.polycreo.httpexceptions.HttpNotFoundException
import org.polycreo.presentation.mappings.PathType
import org.polycreo.presentation.mappings.PolycreoHandler
import org.polycreo.presentation.usecases.UpdatableUsecase
import org.polycreo.services.NotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMethod

private val logger = mu.KotlinLogging.logger {}

/**
 * Controller interface to update and read single resource.
 *
 * @param E the domain type the controller manages
 * @param ID the type of the id of the resource the controller manages
 */
interface UpdatableController<E, ID : Serializable, R : UpdateRequest<E>> : ReadableController<E, ID> {

    override val usecase: UpdatableUsecase<E, ID>

    /**
     * Update resource.
     *
     * @throws HttpNotFoundException if the resource is not found
     * @throws HttpBadRequestException if changes violate data constraints
     * @throws org.springframework.dao.DataAccessException if database access is failed
     */
    @PolycreoHandler("Update", RequestMethod.POST, PathType.SPECIFIC_ITEM)
    @PreAuthorize("hasAnyAuthority('ROOT', #authorityPrefix + 'Update' + #resourceName)")
    fun update(@PathVariable id: ID, @Validated @RequestBody request: R): ResponseEntity<E> {
        try {
            val updated = usecase.update(id, request)
            logger.info { "Success updated: $updated" }
            return ResponseEntity.ok(updated)
        } catch (e: NotFoundException) {
            val message = "Failed to update: $id is not found"
            logger.error { message }
            throw HttpNotFoundException(message, e)
        } catch (e: DataIntegrityViolationException) {
            val message = "Failed to update: changes violate data constraints"
            logger.error { message }
            throw HttpBadRequestException(message, e)
        }
    }
}

interface UpdateRequest<T> : UnaryOperator<T>
