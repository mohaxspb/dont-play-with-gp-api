package ru.kuchanov.gp.service.data

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.kuchanov.gp.bean.auth.GpUser

@Service
class ImageServiceImpl : ImageService {
    override fun getByUserIdAndFileName(userId: Long, imageName: String): ByteArray? {
        TODO()
    }

    override fun getByImageName(imageName: String): ByteArray? {
        TODO()
    }

    override fun uploadImage(user: GpUser, image: MultipartFile, imageName: String): String {
        //todo use file.getOriginalFilename() to get extension
        // i.e. /image/userId/imageName.jpeg
        TODO()
    }

    override fun deleteByUserIdAndFileName(userId: Long, fileName: String): Boolean {
        TODO()
    }
}
