package org.polycreo.presentation.controllers

import java.io.Serializable
import org.polycreo.presentation.mappings.PathType
import org.polycreo.presentation.mappings.PolycreoHandler
import org.polycreo.presentation.usecases.TruncatableUsecase
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMethod

private val logger = mu.KotlinLogging.logger {}

interface TruncatableController<E, ID : Serializable> {

    val usecase: TruncatableUsecase<E, ID>

    @PolycreoHandler("DeleteAll", RequestMethod.DELETE, PathType.ENTIRE_COLLECTION)
    @PreAuthorize("hasAnyAuthority('ROOT', #authorityPrefix + 'DeleteAll' + #resourceName)")
    fun deleteAll(): ResponseEntity<E> {
        usecase.deleteAll()
        logger.info { "Success deleteAll" }
        return ResponseEntity.noContent().build()
    }
}
