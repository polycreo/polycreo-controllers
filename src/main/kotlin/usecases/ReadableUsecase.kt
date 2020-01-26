package org.polycreo.presentation.usecases

import java.io.Serializable
import java.util.Optional
import org.polycreo.services.ReadableService
import org.springframework.transaction.annotation.Transactional

/**
 * Usecase interface to retrieve single entity.
 *
 * @param E the domain type the usecase manages
 * @param ID the type of the id of the entity the usecase manages
 */
interface ReadableUsecase<E, ID : Serializable> {

    val service: ReadableService<E, ID>

    /**
     * Retrieves a resource by its [id].
     *
     * @return the entity
	 * @throws IllegalArgumentException if [id] is `null`
     * @throws NotFoundException if the entity is not found
     * @throws org.springframework.dao.DataAccessException if database access is failed
     */
    @Transactional(readOnly = true)
    fun findByIdOrThrow(id: ID): E = service.findByIdOrThrow(id)

    /**
     * Find resource by [id].
     *
     * @return the entity or empty if not found
	 * @throws IllegalArgumentException if [id] is `null`
     * @throws org.springframework.dao.DataAccessException if database access is failed
     */
    @Transactional(readOnly = true)
    fun findById(id: ID): Optional<E> = service.findById(id)

    /**
     * Returns whether a resource with the given [id] exists.
     *
     * @return `true` if the entity identified by [id] exists
	 * @throws IllegalArgumentException if [id] is `null`
     * @throws org.springframework.dao.DataAccessException if database access is failed
     */
    @Transactional(readOnly = true)
    fun existsById(id: ID): Boolean = service.existsById(id)
}
