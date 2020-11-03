package ru.kuchanov.gp.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus


@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ImageNotFoundException(
    override val message: String? = "Image not found!"
) : RuntimeException(message)

@ResponseStatus(value = HttpStatus.CONFLICT)
class ImageAlreadyExistsException(
    override val message: String? = "Image with this name already exists!"
) : RuntimeException(message)