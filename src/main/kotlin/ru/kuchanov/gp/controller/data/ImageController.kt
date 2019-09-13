package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.exception.ImageAlreadyExistsException
import ru.kuchanov.gp.exception.ImageNotFoundException
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
     * returns URL to image
     */
    @PostMapping(
        value = [GpConstants.ImageEndpoint.Method.ADD],
        produces = [
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_GIF_VALUE
        ]
    )
    fun addImage(
        @RequestParam("image") image: MultipartFile,
        @RequestParam("imageName") imageName: String,
        @AuthenticationPrincipal user: GpUser
    ): String {
        if (imageService.getByImageName(imageName) != null) {
            throw ImageAlreadyExistsException()
        }
        return imageService.uploadImage(user, image, imageName)
    }

    @DeleteMapping("{userId}/{fileName:.+}")
    fun delete(
        @PathVariable(value = "userId") userId: Long,
        @PathVariable(value = "fileName") fileName: String
    ): Boolean =
        imageService.deleteByUserIdAndFileName(userId, fileName)

    @ResponseBody
    @GetMapping("{userId}/{fileName:.+}")
    fun getById(
        @PathVariable(value = "userId") userId: Long,
        @PathVariable(value = "fileName") fileName: String
    ): ByteArray =
        imageService.getByUserIdAndFileName(userId, fileName) ?: throw ImageNotFoundException()
}
