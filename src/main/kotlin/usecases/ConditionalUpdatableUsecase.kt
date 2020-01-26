package org.polycreo.presentation.usecases

import java.io.Serializable
import java.util.function.UnaryOperator
import org.polycreo.services.ConditionalUpdatableService
import org.polycreo.services.NotFoundException
import org.springframework.transaction.annotation.Transactional

interface ConditionalUpdatableUsecase<E, ID : Serializable, C> : UpdatableUsecase<E, ID> {

    override val service: ConditionalUpdatableService<E, ID, C>

    @Transactional
    fun update(id: ID, request: UnaryOperator<E>, condition: C?): E = findById(id)
        .map(request)
        .map { service.update(it, condition) }
        .orElseThrow { NotFoundException("The entity [$id] is not found.") }
}
