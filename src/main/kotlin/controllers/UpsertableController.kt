package org.polycreo.presentation.controllers

import java.io.Serializable
import org.polycreo.presentation.mappings.PathType
import org.polycreo.presentation.mappings.PolycreoHandler
import org.polycreo.presentation.usecases.UpsertableUsecase
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMethod

private val logger = mu.KotlinLogging.logger {}

/**
 * Controller interface to save (upsert) single entity.
 *
 * @param E the domain type the usecase manages
 * @param ID the type of the id of the entity the usecase manages
 */
interface UpsertableController<E : Any, ID : Serializable> : Locatable<E> {

    val usecase: UpsertableUsecase<E, ID>

    /**
     * Saves a given resource.
     *
     * @param id resource identifier
	 * @param newImage resource to save
     * @return [ResponseEntity] of `201 Created`
     *   if a new resource was created or the [usecase] is not [org.polycreo.presentation.usecases.ReadableUsecase].
     *   `200 OK` is returned if an existing resource was updated.
     * @throws org.springframework.dao.DataAccessException if a data access error occurred
	 */
    @PolycreoHandler(RequestMethod.PUT, PathType.SPECIFIC_ITEM)
    @PreAuthorize("hasAnyAuthority('ROOT', #authorityPrefix + 'Upsert' + #resourceName)")
    fun upsert(@PathVariable id: ID, @Validated @RequestBody newImage: E): ResponseEntity<E> {
        val (saved, found) = usecase.upsert(id, newImage)
        logger.info { "Success ${if (found) "updated" else "created"}: $saved" }

        return if (found) {
            ResponseEntity.ok(saved)
        } else {
            ResponseEntity.created(locationOf(saved)).body(saved)
        }
    }
}
