package ru.kuchanov.gp.service.data

import org.springframework.web.multipart.MultipartFile
import ru.kuchanov.gp.bean.auth.GpUser

interface ImageService {

    fun getByUserIdAndFileName(userId: Long, imageName: String): ByteArray?

    fun getByImageName(imageName: String): ByteArray?

    fun uploadImage(user: GpUser, image: MultipartFile, imageName: String): String

    fun deleteByUserIdAndFileName(userId: Long, fileName: String): Boolean
}
