package org.polycreo.presentation.usecases

import java.io.Serializable
import org.polycreo.services.DeletableService
import org.springframework.transaction.annotation.Transactional

interface DeletableUsecase<E, ID : Serializable> {

    val service: DeletableService<E, ID>

    @Transactional
    fun delete(entity: E) = service.delete(entity)

    @Transactional
    fun deleteById(id: ID): E? {
        val found: E? = if (this is ReadableUsecase<*, *>) {
            @Suppress("UNCHECKED_CAST")
            (this as ReadableUsecase<E, ID>).findByIdOrThrow(id)
        } else {
            null
        }

        service.deleteById(id)
        return found
    }
}
