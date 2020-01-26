package org.polycreo.presentation.controllers

import java.io.Serializable
import java.net.URI

/**
 * Compute location of resource.
 *
 * @param E the domain type the controller manages
 */
interface Locatable<E : Any> {

    val path: String

    val idExtractor: (Any) -> Serializable

    /**
     * Compute resource location [URI] for the `Location` response header in `201 Created` response.
     *
     * @return location URI of [created] resource
     */
    fun locationOf(created: E): URI = URI.create("$path/${idExtractor(created)}")
}
