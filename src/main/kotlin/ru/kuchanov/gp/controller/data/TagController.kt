package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.data.Tag
import ru.kuchanov.gp.service.data.TagService

@RestController
@RequestMapping("/" + GpConstants.TagEndpoint.PATH + "/")
class TagController @Autowired constructor(
    val tagService: TagService
) {

    @GetMapping
    fun index() =
        "Tag endpoint"

    @GetMapping(GpConstants.TagEndpoint.Method.ALL)
    fun getAll(): List<Tag> =
        tagService.findAll()
}
