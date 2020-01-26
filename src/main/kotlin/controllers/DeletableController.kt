package org.polycreo.presentation.controllers

import java.io.Serializable
import org.polycreo.httpexceptions.HttpNotFoundException
import org.polycreo.presentation.mappings.PolycreoHandler
import org.polycreo.presentation.usecases.DeletableUsecase
import org.polycreo.services.NotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable

private val logger = mu.KotlinLogging.logger {}

interface DeletableController<E, ID : Serializable> {

    val usecase: DeletableUsecase<E, ID>

    @PolycreoHandler
    @PreAuthorize("hasAnyAuthority('ROOT', #authorityPrefix + 'Delete' + #resourceName)")
    fun delete(@PathVariable id: ID): ResponseEntity<E> {
        try {
            val deleted = usecase.deleteById(id)
            logger.info { "Success deleted: $deleted" }

            return if (deleted != null) {
                ResponseEntity.ok(deleted)
            } else {
                ResponseEntity.noContent().build()
            }
        } catch (e: NotFoundException) {
            val message = "Failed to delete: $id not found"
            logger.error { message }
            throw HttpNotFoundException(message, e)
        }
    }
}
