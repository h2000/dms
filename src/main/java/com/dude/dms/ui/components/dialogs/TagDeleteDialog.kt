package com.dude.dms.ui.components.dialogs

import com.dude.dms.backend.data.Tag
import com.dude.dms.backend.service.*
import com.dude.dms.brain.t
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.checkBox
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.icon.VaadinIcon

class TagDeleteDialog(
        private val tag: Tag,
        private val tagService: TagService,
        private val docService: DocService,
        private val mailService: MailService,
        private val attributeService: AttributeService
) : DmsDialog(t("tag.delete"), 20) {

    private lateinit var tagCheck: Checkbox

    private lateinit var docCheck: Checkbox

    private lateinit var mailCheck: Checkbox

    private lateinit var attributeCheck: Checkbox

    init {
        verticalLayout(isPadding = false, isSpacing = false) {
            setSizeFull()

            tagCheck = checkBox(t("tag")) {
                isEnabled = false
                value = true
            }
            docCheck = checkBox("${t("docs")} (${docService.countByTag(tag)})")
            mailCheck = checkBox("${t("mails")} (${mailService.countByTag(tag)})")
            attributeCheck = checkBox("${t("attributes")} (${tag.attributes.size}")
            button(t("delete"), VaadinIcon.TRASH.create()) {
                onLeftClick { delete() }
                setWidthFull()
                addThemeVariants(ButtonVariant.LUMO_ERROR)
            }
        }
    }

    private fun delete() {
        if (docCheck.value) {
            docService.findByTag(tag).forEach(docService::delete)
        }
        if (mailCheck.value) {
            mailService.findByTag(tag).forEach(mailService::delete)
        }
        if (attributeCheck.value) {
            tag.attributes.forEach(attributeService::delete)
            tag.attributes = emptySet()
        }
        tagService.delete(tagService.save(tag))
        close()
    }
}