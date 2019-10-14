package ru.kuchanov.gp.service.mail

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.model.dto.data.ArticleDto
import ru.kuchanov.gp.model.dto.data.ArticleTranslationDto
import ru.kuchanov.gp.model.dto.data.ArticleTranslationVersionDto
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import ru.kuchanov.gp.service.data.ArticleService
import ru.kuchanov.gp.service.data.ArticleTranslationService
import ru.kuchanov.gp.service.data.ArticleTranslationVersionService
import ru.kuchanov.gp.util.getServerAddress
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Service
class MailServiceImpl @Autowired constructor(
    val javaMailSender: JavaMailSender,
    val articleService: ArticleService,
    val translationService: ArticleTranslationService,
    val versionService: ArticleTranslationVersionService,
    val usersService: GpUserDetailsService,
    @Value("\${my.mail.admin.address}") val adminEmailAddress: String,
    @Value("\${angular.port}") val angularServerPort: String,
    @Value("\${angular.href}") val angularServerHref: String
) : MailService {

    override fun sendMail(vararg to: String, subj: String, text: String) {
        javaMailSender.send { mimeMessage: MimeMessage ->
            mimeMessage.setFrom()
            mimeMessage.setRecipients(Message.RecipientType.TO, to.map { InternetAddress(it) }.toTypedArray())
            mimeMessage.subject = subj
            mimeMessage.setText(text)
        }
    }

    override fun sendArticleCreatedMail(createdArticle: ArticleDto) {
        sendMail(
            adminEmailAddress,
            subj = "New article created!",
            text = """New article created! ${createdArticle.translations[0].title} by ${createdArticle.author!!.fullName}
                |                
                |You can visit it here: ${getServerAddress()}$angularServerPort$angularServerHref#/${GpConstants.ArticleEndpoint.PATH}/${createdArticle.id}
            """.trimMargin()
        )
    }

    override fun sendTranslationCreatedMail(createdTranslation: ArticleTranslationDto) {
        //send to article and other translations authors
        val userEmails = mutableSetOf(adminEmailAddress)
        //article author
        userEmails += usersService.getById(articleService.getOneById(createdTranslation.articleId)?.authorId!!)!!.username
        //translation authors
        val translationAuthorIds = translationService
            .findAllByArticleId(createdTranslation.articleId)
            .map { it.authorId!! }
        userEmails += usersService.getAllById(translationAuthorIds).map { it.username }

        //remove author of translation
        userEmails -= usersService.getById(createdTranslation.authorId!!)!!.username

        sendMail(
            *userEmails.toTypedArray(),
            subj = "New Translation created!",
            text = """New Translation created! ${createdTranslation.title} by ${createdTranslation.author!!.fullName}
                |                
                |You can visit it here: ${getServerAddress()}$angularServerPort$angularServerHref#/${GpConstants.ArticleEndpoint.PATH}/${createdTranslation.articleId}?langId=${createdTranslation.langId}
            """.trimMargin()
        )
    }

    override fun sendVersionCreatedMail(createdVersion: ArticleTranslationVersionDto) {
        //send to article, translation and other versions authors
        val userEmails = mutableSetOf(adminEmailAddress)
        //versionAuthors
        val versionsAuthorsIds = versionService
            .findAllByArticleTranslationId(createdVersion.articleTranslationId)
            .map { it.authorId!! }
        userEmails += usersService.getAllById(versionsAuthorsIds).map { it.username }
        //translation author
        val translation = translationService.getOneById(createdVersion.articleTranslationId)!!
        userEmails += usersService.getById(translation.authorId!!)!!.username
        //article author
        userEmails += usersService.getById(articleService.getOneById(translation.articleId)!!.authorId!!)!!.username

        //remove author of version
        userEmails -= usersService.getById(createdVersion.authorId!!)!!.username

        sendMail(
            *userEmails.toTypedArray(),
            subj = "New text version created!",
            text = """New text version created! ${translation.title} by ${createdVersion.author!!.fullName}
               |
               |You can visit it here: ${getServerAddress()}$angularServerPort$angularServerHref#/${GpConstants.ArticleEndpoint.PATH}/${translation.articleId}?langId=${translation.langId}
            """.trimMargin()
        )
    }

    override fun sendArticleApprovedMail(articleId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendArticlePublishedMail(articleId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendTranslationApprovedMail(translationId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendTranslationPublishedMail(translationId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendVersionApprovedMail(versionId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendVersionPublishedMail(versionId: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}