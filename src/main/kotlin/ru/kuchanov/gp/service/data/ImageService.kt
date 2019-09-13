package ru.kuchanov.gp.service.data

import org.springframework.web.multipart.MultipartFile

interface ImageService {

    fun saveImage(userId: Long, image: MultipartFile, imageName: String): String

    fun getByUserIdAndFileName(userId: Long, imageName: String): ByteArray?

    fun deleteByUserIdAndFileName(userId: Long, fileName: String): Boolean
}
