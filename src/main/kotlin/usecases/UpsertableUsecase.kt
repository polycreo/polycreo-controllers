package org.polycreo.presentation.usecases

import java.io.Serializable
import org.polycreo.services.UpsertableService
import org.springframework.transaction.annotation.Transactional

/**
 * Usecase interface to save (upsert) single entity.
 *
 * @param E the domain type the usecase manages
 * @param ID the type of the id of the entity the usecase manages
 */
interface UpsertableUsecase<E, ID : Serializable> {

    val service: UpsertableService<E, ID>

    /**
     * Saves a given resource.
     *
     * Second value of returned [Pair] indicates whether an existing resource was updated or a new resource was created.
     * However, it is always `true` if this is not [ReadableUsecase].
     *
     * @param id resource identifier
     * @param resource resource to save
     * @return [Pair] of saved entity and boolean; true if overwritten, false if newly created
     * @throws org.springframework.dao.DataAccessException if a data access error occurred
     */
    @Transactional
    fun upsert(id: ID, resource: E): Pair<E, Boolean> {
        val found = if (this is ReadableUsecase<*, *>) {
            @Suppress("UNCHECKED_CAST")
            (this as ReadableUsecase<E, ID>).existsById(id)
        } else true

        val saved = service.save(resource)
        return Pair(saved, found)
    }
}
