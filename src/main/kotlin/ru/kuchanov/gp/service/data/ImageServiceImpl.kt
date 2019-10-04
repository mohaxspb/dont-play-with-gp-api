package ru.kuchanov.gp.service.data

import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.UserNotFoundException
import ru.kuchanov.gp.bean.auth.isAdmin
import ru.kuchanov.gp.exception.ImageAlreadyExistsException
import ru.kuchanov.gp.model.error.GpAccessDeniedException
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Paths

@Service
class ImageServiceImpl @Autowired constructor(
    val userService: GpUserDetailsService,
    val logger: Logger
) : ImageService {

    override fun saveImage(userId: Long, image: MultipartFile, imageName: String?): String {
        println("imageName: $imageName")
        val extension = try {
            if (imageName != null && FilenameUtils.indexOfExtension(imageName) != -1) {
                imageName.substring(imageName.lastIndexOf(".") + 1)
            } else {
                FilenameUtils.getExtension(image.originalFilename)
            }
        } catch (ignored: Exception) {
            ignored.printStackTrace()
            ""
        }
        logger.info("extension: {}", extension)
        println("extension: $extension")
        val fileName = if (imageName != null) {
            val correctedImageName = FilenameUtils.removeExtension(imageName)
            correctedImageName + if (extension.isNotBlank()) ".$extension" else ""
        } else {
            image.name
        }
        logger.info("fileName: {}", fileName)
        println("fileName: $fileName")
        if (getByUserIdAndFileName(userId, imageName ?: image.name) != null) {
            throw ImageAlreadyExistsException()
        }

        val fileDir = "${GpConstants.FilesPaths.IMAGE}/${userId}"
        logger.info("fileDir: {}", fileDir)
        println("fileDir: $fileDir")

        val fullFileName = "$fileDir/$fileName"
        logger.info("fullFileName: {}", fullFileName)
        println("fullFileName: $fullFileName")

        val readableByteChannel = Channels.newChannel(image.inputStream)
        Files.createDirectories(Paths.get(fileDir))
        val fileOutputStream = FileOutputStream(fullFileName)
        fileOutputStream.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)

        readableByteChannel.close()
        fileOutputStream.channel.close()
        fileOutputStream.close()

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
