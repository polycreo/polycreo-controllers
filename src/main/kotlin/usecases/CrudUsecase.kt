package org.polycreo.presentation.usecases

import java.io.Serializable
import org.polycreo.services.CrudService

abstract class CrudUsecase<E, ID : Serializable>(override val service: CrudService<E, ID>) :
        CreatableUsecase<E, ID>,
        UpdatableUsecase<E, ID>,
        DeletableUsecase<E, ID>
