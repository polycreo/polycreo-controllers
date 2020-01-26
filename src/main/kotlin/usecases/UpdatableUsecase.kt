package org.polycreo.presentation.usecases

import java.io.Serializable
import java.util.function.UnaryOperator
import org.polycreo.services.NotFoundException
import org.polycreo.services.UpdatableService
import org.springframework.transaction.annotation.Transactional

/**
 * Usecase interface to update and read single resource.
 *
 * @param E the domain type the usecase manages
 * @param ID the type of the id of the resource the usecase manages
 */
interface UpdatableUsecase<E, ID : Serializable> : ReadableUsecase<E, ID> {

    override val service: UpdatableService<E, ID>

    /**
     * Update resource.
     *
	 * @throws IllegalArgumentException if [id] is `null`
     * @throws NotFoundException if the resource is not found
     * @throws org.springframework.dao.DataIntegrityViolationException
     * @throws org.springframework.dao.DataAccessException if database access is failed
     */
    @Transactional
    fun update(id: ID, request: UnaryOperator<E>): E = service.findById(id)
        .map(request)
        .map(service::update)
        .orElseThrow { NotFoundException("The entity [$id] is not found.") }
}
