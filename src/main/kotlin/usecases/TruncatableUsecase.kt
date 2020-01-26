package org.polycreo.presentation.usecases

import java.io.Serializable
import org.polycreo.services.TruncatableService
import org.springframework.transaction.annotation.Transactional

interface TruncatableUsecase<E, ID : Serializable> {

    val service: TruncatableService<E, ID>

    @Transactional
    fun deleteAll() = service.deleteAll()
}
