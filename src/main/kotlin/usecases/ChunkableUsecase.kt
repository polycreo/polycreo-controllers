package org.polycreo.presentation.usecases

import java.io.Serializable
import org.polycreo.chunkrequests.Chunkable
import org.polycreo.chunks.Chunk
import org.polycreo.services.ChunkableService
import org.springframework.transaction.annotation.Transactional

interface ChunkableUsecase<E, ID>
    where ID : Serializable,
          ID : Comparable<ID> {

    val service: ChunkableService<E, ID>

    @Transactional(readOnly = true)
    fun findAll(chunkable: Chunkable): Chunk<E> = service.findAll(chunkable)
}
