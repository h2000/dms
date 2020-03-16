package com.dude.dms.ui.components.dialogs

import com.dude.dms.backend.data.docs.Doc
import com.dude.dms.backend.service.DocService
import com.dude.dms.brain.t
import com.dude.dms.ui.builder.BuilderFactory
import com.dude.dms.ui.components.standard.DmsDatePicker
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.details.Details
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class DocEditDialog(builderFactory: BuilderFactory, private val doc: Doc, private val docService: DocService) : Dialog() {

    private val datePicker = DmsDatePicker(t("date")).apply {
        setWidthFull()
        value = doc.documentDate
    }

    private val tagSelector = builderFactory.tags().selector().forDoc(doc).build().apply { height = "25vw" }

    private val attributeValueContainer = builderFactory.attributes().valueContainer(doc).build().apply {
        setSizeFull()
        maxHeight = "40vh"
    }

    init {
        width = "40vw"
        val saveButton = Button(t("save"), VaadinIcon.DISC.create()) { save() }.apply {
            setWidthFull()
            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        }
        val cancelButton = Button(t("close"), VaadinIcon.CLOSE.create()) { close() }.apply {
            setWidthFull()
            addThemeVariants(ButtonVariant.LUMO_ERROR)
        }
        val tagDetails = Details(t("tags"), tagSelector).apply { element.style["width"] = "100%" }
        val attributeDetails = Details(t("attributes"), attributeValueContainer).apply { element.style["width"] = "100%" }
        val buttonWrapper = HorizontalLayout(saveButton, cancelButton).apply {
            setWidthFull()
        }
        val wrapper = VerticalLayout(datePicker, tagDetails, attributeDetails, buttonWrapper).apply {
            setSizeFull()
            isPadding = false
            isSpacing = false
        }
        add(wrapper)
    }

    private fun save() {
        if (attributeValueContainer.validate()) {
            doc.documentDate = datePicker.value
            doc.tags = tagSelector.selectedTags.toMutableSet()
            docService.save(doc)
            close()
        }
    }
}