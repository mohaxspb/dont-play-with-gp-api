package ru.kuchanov.gp.service.data

import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.UserNotFoundException
import ru.kuchanov.gp.bean.auth.isAdmin
import ru.kuchanov.gp.model.error.GpAccessDeniedException
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Paths

@Service
class ImageServiceImpl @Autowired constructor(val userService: GpUserDetailsService) : ImageService {

    override fun saveImage(userId: Long, image: MultipartFile, imageName: String): String {
        val extension = try {
            FilenameUtils.getExtension(image.originalFilename)
        } catch (ignored: Exception) {
            ""
        }

        val fileDir = "${GpConstants.FilesPaths.IMAGE}/${userId}"
        val fileName = imageName + if (extension.isNotBlank()) ".$extension" else ""
        val fullFileName = "$fileDir/$fileName"

        val readableByteChannel = Channels.newChannel(image.inputStream)
        Files.createDirectories(Paths.get(fileDir))
        val fileOutputStream = FileOutputStream(fullFileName)
        fileOutputStream.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)

        return fullFileName
    }

    override fun getByUserIdAndFileName(userId: Long, imageName: String): ByteArray? {
        val fileDir = "${GpConstants.FilesPaths.IMAGE}/${userId}"
        val file = File(fileDir).listFiles()?.find { it.nameWithoutExtension == imageName || it.name == imageName }
        return file?.let { IOUtils.toByteArray(FileInputStream(file)) }
    }

    override fun deleteByUserIdAndFileName(userId: Long, fileName: String): Boolean {
        val user = userService.getById(userId) ?: throw UserNotFoundException()
        if (user.isAdmin()) {
            TODO()
        } else {
            throw GpAccessDeniedException()
        }
    }
}
