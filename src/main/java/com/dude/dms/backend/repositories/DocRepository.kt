package com.dude.dms.backend.repositories

import com.dude.dms.backend.data.Tag
import com.dude.dms.backend.data.docs.Attribute
import com.dude.dms.backend.data.docs.Doc
import com.dude.dms.backend.data.mails.Mail
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DocRepository : JpaRepository<Doc, Long> {

    fun findByGuid(guid: String): Doc?

    fun findByTags(tag: Tag): Set<Doc>

    fun countByTags(tag: Tag): Long

    fun findTop10ByRawTextContaining(rawText: String): Set<Doc>

    fun countByRawTextContaining(rawText: String): Long

    fun findTop10ByRawTextContainingIgnoreCase(rawText: String): Set<Doc>

    fun countByRawTextContainingIgnoreCase(rawText: String): Long

    fun findByAttributeValues_AttributeEquals(attribute: Attribute): Set<Doc>

    fun countByAttributeValues_AttributeEquals(attribute: Attribute): Long

    @Query("SELECT doc FROM Doc doc WHERE (:tag is null or :tag MEMBER OF doc.tags) AND (:mail is null or :mail = doc.mail)" +
            " AND (:text is null or LOWER(doc.rawText) LIKE LOWER(CONCAT('%', :text, '%')))")
    fun findByFilter(
            @Param("tag") tag: Tag?,
            @Param("mail") mail: Mail?,
            @Param("text") text: String?,
            pageable: Pageable
    ): Page<Doc>

    @Query("SELECT COUNT(*) FROM Doc doc WHERE (:tag is null or :tag MEMBER OF doc.tags) AND (:mail is null or :mail = doc.mail)" +
            " AND (:text is null or LOWER(doc.rawText) LIKE LOWER(CONCAT('%', :text, '%')))")
    fun countByFilter(@Param("tag") tag: Tag?, @Param("mail") mail: Mail?, @Param("text") text: String?): Long
}