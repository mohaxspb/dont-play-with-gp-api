package ru.kuchanov.gp.model.dto.data

import org.springframework.beans.factory.annotation.Value

interface ArticleIdAndCommentsCount {

//    @Value("#{target.article_id}")
    fun getArticleId(): Long
//    @Value("#{target.count}")
    fun getCommentsCount(): Long
}