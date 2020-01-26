package org.polycreo.presentation.usecases

import java.io.Serializable
import org.polycreo.services.CreatableService
import org.springframework.transaction.annotation.Transactional

interface CreatableUsecase<E, ID : Serializable> {

    val service: CreatableService<E, ID>

    @Transactional
    fun create(entity: E): E = service.create(entity)
}
