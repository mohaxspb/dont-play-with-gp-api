package ru.kuchanov.gp.service.mail

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.auth.isAdmin
import ru.kuchanov.gp.model.dto.data.ArticleDto
import ru.kuchanov.gp.model.dto.data.ArticleTranslationDto
import ru.kuchanov.gp.model.dto.data.ArticleTranslationVersionDto
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import ru.kuchanov.gp.service.data.ArticleService
import ru.kuchanov.gp.service.data.ArticleTranslationService
import ru.kuchanov.gp.service.data.ArticleTranslationVersionService
import ru.kuchanov.gp.service.data.CommentService
import ru.kuchanov.gp.service.util.UrlService
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Service
class MailServiceImpl @Autowired constructor(
    val javaMailSender: JavaMailSender,
    val articleService: ArticleService,
    val translationService: ArticleTranslationService,
    val versionService: ArticleTranslationVersionService,
    val userService: GpUserDetailsService,
    val commentService: CommentService,
    val urlService: UrlService,
    @Value("\${my.mail.admin.address}") val adminEmailAddress: String
) : MailService {

    override fun sendMail(vararg to: String, subj: String, text: String, sendAsHtml: Boolean) {
        javaMailSender.send { mimeMessage: MimeMessage ->
            mimeMessage.setFrom()
            mimeMessage.setRecipients(Message.RecipientType.TO, to.map { InternetAddress(it) }.toTypedArray())
            mimeMessage.subject = subj
            if (sendAsHtml) {
                mimeMessage.setText(text, "utf-8", "html")
            } else {
                mimeMessage.setText(text)
            }
        }
    }

    override fun sendArticleCreatedMail(createdArticle: ArticleDto) {
        sendMail(
            adminEmailAddress,
            subj = "New article created!",
            text = """New article created! ${createdArticle.translations[0].title} by ${createdArticle.author!!.fullName}
                |                
                |You can visit it here: ${urlService.createArticleLink(createdArticle.id)}
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
                |You can visit it here: ${
                urlService.createArticleLink(
                    createdTranslation.articleId,
                    createdTranslation.langId
                )
            }
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
               |You can visit it here:  ${urlService.createArticleLink(translation.articleId, translation.langId)}
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
                |You can visit it here: ${urlService.createArticleLink(article.id)}
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
                |You can visit it here: ${urlService.createArticleLink(article.id)}
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
                |You can visit it here: ${urlService.createArticleLink(translation.articleId, translation.langId)}
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
                |You can visit it here: ${urlService.createArticleLink(translation.articleId, translation.langId)}
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
                |You can visit it here: ${urlService.createArticleLink(translation.articleId)}
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
                |You can visit it here: ${urlService.createArticleLink(translation.articleId)}
            """.trimMargin()
        )
    }

    override fun sendRegistrationEmail(email: String, password: String) {
        val text = """Hello! Welcome to "Don't play with Google!"
            |
            | Here is you password, which you can use to login to site. Also you could login using social networks profiles, if you use same email in it. 
            |
            |Your email: $email
            |Your password: $password
        """.trimMargin()
        sendMail(
            email,
            subj = "Welcome to Don't play with Google!",
            text = text
        )
    }

    @Scheduled(
        /**
         * second, minute, hour, day, month, day of week
         */
//        cron = "*/30 * * * * *" //fi xme test
        cron = "0 5 0 * * *"
    )
    override fun sendStatisticsEmail() {
        val currentDate = LocalDate.now()
        val startDate = currentDate.minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        val endDate = currentDate.atStartOfDay().toInstant(ZoneOffset.UTC)
//        val startDate = currentDate.atStartOfDay().toInstant(ZoneOffset.UTC) //FI XME to test today
//        val endDate = currentDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) //FI XME to test today
        val articlesCreatedToday = articleService.getCreatedArticlesBetweenDates(
            startDate.toString(),
            endDate.toString()
        )
//        println("articlesCreatedToday: $articlesCreatedToday")
        val translationsFromCreatedArticles = articlesCreatedToday.map { articleDto ->
            articleDto.translations.sortedBy { it.created }[0]
        }
//        println("translationIdsFromCreatedArticles: $translationsFromCreatedArticles")
        val versionIdsFromCreatedArticles = translationsFromCreatedArticles.map { translationDto ->
            translationDto.versions.sortedBy { it.created }[0].id
        }
//        println("versionIdsFromCreatedArticles: $versionIdsFromCreatedArticles")
        //ExceptOfMentionedInArticle
        val translationsCreatedToday = translationService.getCreatedTranslationsBetweenDates(
            startDate.toString(),
            endDate.toString(),
            translationsFromCreatedArticles.map { it.id }
        )
//        println("translationsCreatedToday: $translationsCreatedToday")
        val versionIdsFromCreatedTranslations = translationsCreatedToday.map { translation ->
            translation.versions.sortedBy { it.created }[0].id
        }
        //ExceptOfMentionedIn translations and versions from created articles
        val versionsCreatedToday = versionService.getCreatedVersionsBetweenDates(
            startDate.toString(),
            endDate.toString(),
            versionIdsFromCreatedTranslations.plus(versionIdsFromCreatedArticles)
        )
//        println("versionsCreatedToday: $versionsCreatedToday")

        val numOfUsersCreatedToday = userService.countUsersCreatedBetweenDates(
            startDate.toString(),
            endDate.toString()
        )
//        println("numOfUsersCreatedToday: $numOfUsersCreatedToday")

        val articleIdsForCommentsCreatedToday = commentService.getArticleIdsForCommentsCreatedBetweenDates(
            startDate.toString(),
            endDate.toString()
        )
        val articleIdsAndCommentsCountForCommentsCreatedToday = commentService
            .getArticleIdsAndCommentsCountForCommentsCreatedBetweenDates(
                startDate.toString(),
                endDate.toString()
            )
//        println("articleIdsAndCommentsCountForCommentsCreatedToday: $articleIdsAndCommentsCountForCommentsCreatedToday")
        val articlesForCommentsCreatedToday = articleService
            .findAllByIdsAsDtoWithTranslations(articleIdsForCommentsCreatedToday)
//        println("articlesForCommentsCreatedToday: $articlesForCommentsCreatedToday")

        val dateString = SimpleDateFormat("EEE, dd MMMMM yyyy").format(Date.from(startDate))

        val articlesAsHtml = articlesCreatedToday.joinToString(separator = "\n") { articleDto ->
            "<li><a href=\"${urlService.createArticleLink(articleDto.id)}\">${articleDto.translations[0].title}</a> by ${articleDto.author!!.fullName}</li>"
        }
        val createdTranslationsAsHtml = translationsCreatedToday.joinToString(separator = "\n") {
            "<li><a href=\"${
                urlService.createArticleLink(
                    it.articleId,
                    it.langId
                )
            }\">${it.title}</a> by ${it.author!!.fullName}</li>"
        }
        val createdVersionsAsHtml = versionsCreatedToday.joinToString(separator = "\n") { versionDto ->
            val shortenedText =
                versionDto.text.substring(0, versionDto.text.length.takeIf { it <= 100 }?.let { it - 1 } ?: 100)
            val translation = translationService.getOneById(versionDto.articleTranslationId)!!
            val url = urlService.createArticleLink(translation.articleId, translation.langId)
            "<li><a href=\"$url\">$shortenedText</a> by ${versionDto.author!!.fullName}</li>"
        }
        val createdCommentsAsHtml =
            articleIdsAndCommentsCountForCommentsCreatedToday.joinToString(separator = "\n") { articleIdAndCommentCount ->
                val article = articlesForCommentsCreatedToday.find {
                    it.id == articleIdAndCommentCount.getArticleId()
                }!!
                val articleTitle = article.translations[0].title
                val articleUrl = urlService.createArticleLink(article.id)
                "<li><a href=\"$articleUrl\">$articleTitle</a>: ${articleIdAndCommentCount.getCommentsCount()}</li>"
            }
        val totalCommentsCount =
            articleIdsAndCommentsCountForCommentsCreatedToday.sumBy { it.getCommentsCount().toInt() }

        val text = """
                |<h1>There is statistics for $dateString</h1> 
                |<h3>Data:</h3>
                |<ol>
                |   <li>
                |       Articles created: ${articlesCreatedToday.size}
                |       <ul>$articlesAsHtml</ul>
                |   </li>
                |   <li>
                |       Translations created: ${translationsCreatedToday.size}
                |       <ul>$createdTranslationsAsHtml</ul>
                |   </li>
                |   <li>
                |       Text versions created: ${versionsCreatedToday.size}
                |       <ul>$createdVersionsAsHtml</ul>
                |   </li>
                |</ol>
                |==============================
                |<h3>Activity:</h3>
                |<ol>
                |   <li>Users created: $numOfUsersCreatedToday</li>
                |   <li>
                |        Comments created: $totalCommentsCount
                |       <ul>$createdCommentsAsHtml</ul>
                |   </li>
                |</ol>
            """.trimMargin()

//        println(text)

        sendMail(
            adminEmailAddress,
//            "no-reply@dont-play-with-google.com", //fi xme test
            subj = "Statistics for $dateString",
            text = text,
            sendAsHtml = true
        )
    }
}