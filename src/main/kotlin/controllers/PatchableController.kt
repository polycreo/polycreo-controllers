package org.polycreo.presentation.controllers

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonpatch.JsonPatch
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch
import java.io.Serializable
import org.polycreo.httpexceptions.HttpBadRequestException
import org.polycreo.presentation.mappings.PolycreoHandler
import org.polycreo.presentation.usecases.UpdatableUsecase
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

private val logger = mu.KotlinLogging.logger {}

/**
 * Usecase interface to patch, update and read single resource.
 *
 * @param E the domain type the controller manages
 * @param ID the type of the id of the entity the controller manages
 */
interface PatchableController<E : Any, ID : Serializable> {

    val usecase: UpdatableUsecase<E, ID>

    val mapper: ObjectMapper

    /**
     * Patch resource by [JsonPatch].
     *
     * @throws HttpBadRequestException if a request is invalid
     * @throws HttpBadRequestException if changes violate data constraints
     */
    @PolycreoHandler
    @PreAuthorize("hasAnyAuthority('ROOT', #authorityPrefix + 'Patch' + #resourceName)")
    fun patch(@PathVariable id: ID, @RequestBody patch: JsonPatch) = patch0(id, patch::apply)

    /**
     * Patch resource by [JsonMergePatch].
     */
    @PolycreoHandler
    @PreAuthorize("hasAnyAuthority('ROOT', #authorityPrefix + 'Patch' + #resourceName)")
    fun patch(@PathVariable id: ID, @RequestBody patch: JsonMergePatch) = patch0(id, patch::apply)

    // Making this private causes KotlinReflectionInternalError: Could not compute caller for function
    @Suppress("ThrowsCount")
    fun patch0(id: ID, patch: (JsonNode) -> JsonNode): ResponseEntity<E> {
        try {
            val patched = usecase.update(id, PatchUpdateRequest(mapper, patch))
            logger.info { "Success patched: $patched" }
            return ResponseEntity.ok(patched)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            when (e) {
                is IllegalStateException, is IllegalPatchException -> {
                    logger.error { "Failed to patch: invalid request" }
                    throw HttpBadRequestException(e)
                }
                is DataIntegrityViolationException -> {
                    logger.error { "Failed to patch: changes violate data constraints" }
                    throw HttpBadRequestException(e)
                }
                else -> throw e
            }
        }
    }
}

private class PatchUpdateRequest<E : Any>(val mapper: ObjectMapper, val patch: (JsonNode) -> JsonNode) :
        UpdateRequest<E> {

    override fun apply(original: E): E {
        try {
            val originalNode: JsonNode = mapper.valueToTree(original)
            logger.debug { "Before patch: $originalNode" }
            val patchedNode = patch(originalNode)
            logger.debug { "After patch: $patchedNode" }
            return mapper.treeToValue(patchedNode, original.javaClass)
        } catch (e: JsonParseException) {
            throw IllegalStateException("Failed to parse original JSON: $original", e)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            throw IllegalPatchException(e)
        }
    }
}

class IllegalPatchException(cause: Throwable) : RuntimeException(cause)
