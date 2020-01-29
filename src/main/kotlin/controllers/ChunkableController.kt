package org.polycreo.presentation.controllers

import java.io.Serializable
import org.polycreo.chunkrequests.Chunkable
import org.polycreo.presentation.mappings.PathType
import org.polycreo.presentation.mappings.PolycreoHandler
import org.polycreo.presentation.usecases.ChunkableUsecase
import org.polycreo.resources.ChunkedResources
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMethod

private val logger = mu.KotlinLogging.logger {}

interface ChunkableController<E, ID>
    where ID : Serializable,
          ID : Comparable<ID> {

    val usecase: ChunkableUsecase<E, ID>

    @PolycreoHandler(RequestMethod.GET, PathType.ENTIRE_COLLECTION)
    @PreAuthorize("hasAnyAuthority('ROOT', #authorityPrefix + 'List' + #resourceName)")
    fun list(chunkable: Chunkable): ResponseEntity<Any?> {
        val chunk = usecase.findAll(chunkable)
        logger.info { "Success list $chunkable: $chunk" }

        val resource = ChunkedResources("elements", chunk)
        return ResponseEntity.ok(resource)
    }
}
