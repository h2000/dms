package com.dude.dms.backend.service

import com.dude.dms.backend.data.Tag
import com.dude.dms.backend.data.docs.Attribute
import com.dude.dms.backend.data.docs.Doc
import com.dude.dms.backend.data.history.TagHistory
import com.dude.dms.backend.data.mails.Mail
import com.dude.dms.backend.data.rules.PlainTextRule
import com.dude.dms.backend.data.rules.RegexRule
import com.dude.dms.backend.repositories.TagRepository
import com.dude.dms.brain.events.EventManager
import org.springframework.stereotype.Service

@Service
class TagService(
        private val tagRepository: TagRepository,
        eventManager: EventManager
) : HistoricalCrudService<Tag, TagHistory>(tagRepository, eventManager) {

    override fun create(entity: Tag) = tagRepository.findByName(entity.name) ?: super.create(entity)

    override fun createHistory(entity: Tag, text: String?, created: Boolean, edited: Boolean) = TagHistory(entity, text, created, edited)

    fun findByName(name: String) = tagRepository.findByName(name)

    fun findByPlainTextRule(rule: PlainTextRule) = tagRepository.findByPlainTextRules(rule)

    fun findByRegexRule(rule: RegexRule) = tagRepository.findByRegexRules(rule)

    fun findTop10ByNameContaining(name: String) = tagRepository.findTop10ByNameContaining(name)

    fun countByNameContaining(name: String) = tagRepository.countByNameContaining(name)

    fun findTop10ByNameContainingIgnoreCase(name: String) = tagRepository.findTop10ByNameContainingIgnoreCase(name)

    fun countByNameContainingIgnoreCase(name: String) = tagRepository.countByNameContainingIgnoreCase(name)

    fun findByDoc(doc: Doc) = tagRepository.findByDocs(doc)

    fun findByMail(mail: Mail) = tagRepository.findByMails(mail)

    fun findByAttribute(attribute: Attribute) = tagRepository.findByAttributes(attribute)
}