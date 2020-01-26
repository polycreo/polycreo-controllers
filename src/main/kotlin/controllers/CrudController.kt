package org.polycreo.presentation.controllers

import java.io.Serializable
import org.polycreo.presentation.usecases.CrudUsecase

interface CrudController<E : Any, ID : Serializable, CR : CreateRequest<E>, UR : UpdateRequest<E>> :
        CreatableController<E, ID, CR>,
        UpdatableController<E, ID, UR>,
        DeletableController<E, ID> {

    override val usecase: CrudUsecase<E, ID>
}
