package org.polycreo.presentation.controllers

import java.io.Serializable
import java.net.URI
import java.util.function.Supplier
import org.polycreo.httpexceptions.HttpConflictException
import org.polycreo.presentation.mappings.PolycreoHandler
import org.polycreo.presentation.usecases.CreatableUsecase
import org.polycreo.services.AlreadyExistsException
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestBody

private val logger = mu.KotlinLogging.logger {}

interface CreatableController<E, ID : Serializable, R : CreateRequest<E>> {

    val usecase: CreatableUsecase<E, ID>

    val path: String

    val idExtractor: (Any?) -> Serializable

    @PolycreoHandler
    @PreAuthorize("hasAnyAuthority('ROOT', #authorityPrefix + 'Create' + #resourceName)")
    fun create(@RequestBody @Validated request: R): ResponseEntity<E> {
        try {
            val entity = request.get()
            val created = usecase.create(entity)
            logger.info { "Success created: $created" }
            return ResponseEntity.created(locationOf(created)).body(created)
        } catch (e: AlreadyExistsException) {
            logger.error { "Failed to create: $request already exists" }
            throw HttpConflictException(e)
        }
    }

    fun locationOf(created: E): URI = URI.create("$path/${idExtractor(created)}")
}

interface CreateRequest<T> : Supplier<T>
