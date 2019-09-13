package ru.kuchanov.gp.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus


@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ImageNotFoundException(msg: String = "Image not found!") : RuntimeException(msg)

@ResponseStatus(value = HttpStatus.CONFLICT)
class ImageAlreadyExistsException(msg: String = "Image with this name already exists!") : RuntimeException(msg)