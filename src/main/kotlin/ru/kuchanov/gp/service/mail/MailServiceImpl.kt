package ru.kuchanov.gp.service.mail

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.isAdmin
import ru.kuchanov.gp.model.dto.data.ArticleDto
import ru.kuchanov.gp.model.dto.data.ArticleTranslationDto
import ru.kuchanov.gp.model.dto.data.ArticleTranslationVersionDto
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import ru.kuchanov.gp.service.data.ArticleService
import ru.kuchanov.gp.service.data.ArticleTranslationService
import ru.kuchanov.gp.service.data.ArticleTranslationVersionService
import ru.kuchanov.gp.service.data.CommentService
import ru.kuchanov.gp.util.getServerAddress
import java.time.LocalDate
import java.time.ZoneOffset
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
    val userService: GpUserDetailsService,
    val commentService: CommentService,
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
                |You can visit it here: ${createArticleLink(createdArticle.id)}
            """.trimMargin()
        )
    }

    override fun sendTranslationCreatedMail(createdTranslation: ArticleTranslationDto) {
        //send to article and other translations authors
        val userEmails = mutableSetOf(adminEmailAddress)
        //article author
        userEmails += userService.getById(articleService.getOneById(createdTranslation.articleId)?.authorId!!)!!.username
        //translation authors
        val translationAuthorIds = translationService
            .findAllByArticleId(createdTranslation.articleId)
            .map { it.authorId!! }
        userEmails += userService.getAllById(translationAuthorIds).map { it.username }

        //remove author of translation
        userEmails -= userService.getById(createdTranslation.authorId!!)!!.username

        sendMail(
            *userEmails.toTypedArray(),
            subj = "New Translation created!",
            text = """New Translation created! ${createdTranslation.title} by ${createdTranslation.author!!.fullName}
                |                
                |You can visit it here: ${createArticleLink(createdTranslation.articleId, createdTranslation.langId)}
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
        userEmails += userService.getAllById(versionsAuthorsIds).map { it.username }
        //translation author
        val translation = translationService.getOneById(createdVersion.articleTranslationId)!!
        userEmails += userService.getById(translation.authorId!!)!!.username
        //article author
        userEmails += userService.getById(articleService.getOneById(translation.articleId)!!.authorId!!)!!.username

        //remove author of version
        userEmails -= userService.getById(createdVersion.authorId!!)!!.username

        sendMail(
            *userEmails.toTypedArray(),
            subj = "New text version created!",
            text = """New text version created! ${translation.title} by ${createdVersion.author!!.fullName}
               |
               |You can visit it here:  ${createArticleLink(translation.articleId, translation.langId)}
            """.trimMargin()
        )
    }

    override fun sendArticleApprovedMail(article: ArticleDto) {
        //send to article author if he is not admin
        val articleAuthor = userService.getById(article.authorId!!)!!
        if (!articleAuthor.isAdmin()) {
            sendMail(
                articleAuthor.username,
                subj = "Your article approved!",
                text = """Your article "${article.translations[0].title}" approved!
                |                
                |You can visit it here: ${createArticleLink(article.id)}
            """.trimMargin()
            )
        }
    }

    override fun sendArticlePublishedMail(article: ArticleDto) {
        //send to article author if he is not admin and to admin
        val userEmails = mutableSetOf(adminEmailAddress)
        val articleAuthor = userService.getById(article.authorId!!)!!
        if (!articleAuthor.isAdmin()) {
            userEmails += articleAuthor.username
        }
        sendMail(
            *userEmails.toTypedArray(),
            subj = "Your article published!",
            text = """Your article "${article.translations[0].title}" published!
                |                
                |You can visit it here: ${createArticleLink(article.id)}
            """.trimMargin()
        )
    }

    override fun sendTranslationApprovedMail(translation: ArticleTranslationDto) {
        //send to translation author if he is not admin
        val author = userService.getById(translation.authorId!!)!!
        if (!author.isAdmin()) {
            sendMail(
                author.username,
                subj = "Your translation approved!",
                text = """Your translation "${translation.title}" approved!
                |                
                |You can visit it here: ${createArticleLink(translation.articleId, translation.langId)}
            """.trimMargin()
            )
        }
    }

    override fun sendTranslationPublishedMail(translation: ArticleTranslationDto) {
        //send to translation author if he is not admin and to admin
        val userEmails = mutableSetOf(adminEmailAddress)
        val author = userService.getById(translation.authorId!!)!!
        if (!author.isAdmin()) {
            userEmails += author.username
        }
        sendMail(
            *userEmails.toTypedArray(),
            subj = "Your translation published!",
            text = """Your translation "${translation.title}" published!
                |                
                |You can visit it here: ${createArticleLink(translation.articleId, translation.langId)}
            """.trimMargin()
        )
    }

    override fun sendVersionApprovedMail(version: ArticleTranslationVersionDto) {
        //send to article author if he is not admin
        val author = userService.getById(version.authorId!!)!!
        if (!author.isAdmin()) {
            val translation = translationService.getOneById(version.articleTranslationId)!!
            sendMail(
                author.username,
                subj = "Your text version approved!",
                text = """Your text version for "${translation.title}" approved! 
                |                
                |You can visit it here: ${createArticleLink(translation.articleId)}
            """.trimMargin()
            )
        }
    }

    override fun sendVersionPublishedMail(version: ArticleTranslationVersionDto) {
        //send to version author if he is not admin and to admin
        val userEmails = mutableSetOf(adminEmailAddress)
        val author = userService.getById(version.authorId!!)!!
        if (!author.isAdmin()) {
            userEmails += author.username
        }
        val translation = translationService.getOneById(version.articleTranslationId)!!
        sendMail(
            author.username,
            subj = "Your text version published!",
            text = """Your text version for "${translation.title}" published! 
                |                
                |You can visit it here: ${createArticleLink(translation.articleId)}
            """.trimMargin()
        )
    }

    @Scheduled(
        /**
         * second, minute, hour, day, month, day of week
         */
        cron = "*/30 * * * * *"
    )
    fun sendStatisticsEmail() {
        val currentDate = LocalDate.now()
        val startDate = currentDate.minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        val endDate = currentDate.atStartOfDay().toInstant(ZoneOffset.UTC)
        val articlesCreatedToday = articleService.getCreatedArticlesBetweenDates(
            startDate.toString(),
            endDate.toString()
        )
        //ExceptOfMentionedInArticle
        //todo
//        val translationsCreatedToday = translationService.getCreatedTranslationsBetweenDates(
//            startDate.toString(),
//            endDate.toString()
//        )
        //ExceptOfMentionedIn translations
//        val versionsCreatedToday = TODO()

        val numOfUsersCreatedToday = userService.countUsersCreatedBetweenDates(
            startDate.toString(),
            endDate.toString()
        )

        val articleIdsForCommentsCreatedToday = commentService.getArticleIdsForCommentsCreatedBetweenDates(
            startDate.toString(),
            endDate.toString()
        )
        val articleIdsAndCommentsCountForCommentsCreatedToday = commentService
            .getArticleIdsAndCommentsCountForCommentsCreatedBetweenDates(
                startDate.toString(),
                endDate.toString()
            )
        println("articleIdsAndCommentsCountForCommentsCreatedToday: $articleIdsAndCommentsCountForCommentsCreatedToday")
        val articlesForCommentsCreatedToday = articleService
            .findAllByIdsAsDtoWithTranslations(articleIdsForCommentsCreatedToday)
    }

    private fun createArticleLink(articleId: Long, translationLangId: Long? = null): String {
        val serverAddress = "${getServerAddress()}$angularServerPort$angularServerHref#"
        val articlePage = GpConstants.ArticleEndpoint.PATH
        var link = "$serverAddress/$articlePage/$articleId"
        translationLangId?.let {
            link += "?langId=$it"
        }
        return link
    }
}