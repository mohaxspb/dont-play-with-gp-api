package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.service.data.ImageService

@RestController
@RequestMapping("/" + GpConstants.ImageEndpoint.PATH + "/")
class ImageController @Autowired constructor(
    val imageService: ImageService
) {

    @GetMapping
    fun index() =
        "Image endpoint"

    /**
     * Used to show images on site by URL
     */
    @ResponseBody
    @GetMapping(
        value = ["{userId}/{fileName:.+}"],
        produces = [
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_GIF_VALUE
        ]
    )
    fun get(
        @PathVariable(value = "userId") userId: Long,
        @PathVariable(value = "fileName") fileName: String
    ): ByteArray =
        imageService.getByUserIdAndFileName(userId, fileName) ?: throw ImageNotFoundException()
}

@ResponseStatus(value = org.springframework.http.HttpStatus.NOT_FOUND)
class ImageNotFoundException(
    override val message: String? = "Image not found in!"
) : RuntimeException(message)
