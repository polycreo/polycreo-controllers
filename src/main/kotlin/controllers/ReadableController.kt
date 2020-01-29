package org.polycreo.presentation.controllers

import java.io.Serializable
import org.polycreo.httpexceptions.HttpNotFoundException
import org.polycreo.presentation.mappings.PathType
import org.polycreo.presentation.mappings.PolycreoHandler
import org.polycreo.presentation.usecases.ReadableUsecase
import org.polycreo.services.NotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMethod

private val logger = mu.KotlinLogging.logger {}

/**
 * Controller interface to retrieve single entity.
 *
 * @param E the domain type the controller manages
 * @param ID the type of the id of the entity the controller manages
 */
interface ReadableController<E, ID : Serializable> {

    val usecase: ReadableUsecase<E, ID>

    /**
     * Get resource by [id].
     *
     * @return OK [ResponseEntity] of the resource
     * @throws HttpNotFoundException if the resource is not found
     */
    @PolycreoHandler(RequestMethod.GET, PathType.SPECIFIC_ITEM)
    @PreAuthorize("hasAnyAuthority('ROOT', #authorityPrefix + 'Get' + #resourceName)")
    fun get(@PathVariable id: ID): ResponseEntity<E> {
        try {
            val found = usecase.findByIdOrThrow(id)
            logger.info { "Success get: $found" }
            return ResponseEntity.ok(found)
        } catch (e: NotFoundException) {
            val message = "Failed to get: $id not found"
            logger.error { message }
            throw HttpNotFoundException(message, e)
        }
    }
}
