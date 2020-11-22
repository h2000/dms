package com.dude.dms.ui.components.misc

import com.dude.dms.backend.service.DocService
import com.dude.dms.brain.parsing.search.SearchParser
import com.dude.dms.brain.t
import com.dude.dms.utils.*
import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.value.ValueChangeMode
import java.util.*
import kotlin.concurrent.schedule


class SearchBar : HorizontalLayout() {

    private lateinit var textFilter: TextField
    private lateinit var textHintList: SearchHintList
    private val fromFilter: DatePicker
    private val toFilter: DatePicker

    var onChange: (() -> Unit)? = null

    private val searchParser = SearchParser()

    private val viewUI = UI.getCurrent()

    init {
        setWidthFull()

        horizontalLayout(isPadding = false, isSpacing = false) {
            setWidthFull()
            verticalLayout(isPadding = false, isSpacing = false) {
                setWidthFull()
                textFilter = textField {
                    setWidthFull()
                    placeholder = t("search")
                    valueChangeMode = ValueChangeMode.EAGER
                    element.setAttribute("onkeydown", """
                        if (event.key == 'ArrowDown' || event.key == 'ArrowUp') {
                            return false;
                        }
                        return true;
                        """.trimIndent())
                    addFocusListener {
                        textHintList.isVisible = true
                    }
                    addBlurListener {
                        textHintList.isVisible = false
                    }
                    addKeyDownListener(Key.ARROW_DOWN, {
                        textHintList.down()
                    })
                    addKeyDownListener(Key.ARROW_UP, {
                        textHintList.up()
                    })
                    addKeyDownListener(Key.ENTER, {
                        textHintList.select()
                    })
                    suffixComponent = VaadinIcon.CHECK.create().apply { color = "var(--lumo-success-text-color)" }
                    addValueChangeListener {
                        val error = searchParser.setInput(it.value)
                        textHintList.setItems(searchParser.getHints())
                        if (error.isNullOrBlank()) {
                            suffixComponent = VaadinIcon.CHECK.create().apply { color = "var(--lumo-success-text-color)" }
                            (suffixComponent as Icon).clearTooltips()
                        } else {
                            suffixComponent = VaadinIcon.BAN.create().apply { color = "var(--lumo-error-text-color)" }
                            (suffixComponent as Icon).tooltip(error)
                            Timer().schedule(200) { viewUI.access { (suffixComponent as Icon).showTooltip() } }
                        }
                    }
                }
                textHintList = searchHintList {
                    isVisible = false
                    maxWidth = "40em"
                    style["position"] = "absolute"
                    style["top"] = "50px"
                    style["backgroundColor"] = "var(--lumo-base-color)"
                    style["zIndex"] = "1"
                    style["border"] = "2px solid var(--lumo-contrast-20pct)"
                    setItems(searchParser.getHints())
                    onSelect = {
                        val current = textFilter.value
                        if (current.endsWith(" ")) {
                            textFilter.value = "$current${it.text} "
                        } else {
                            var itCopy = it.text
                            while (!current.endsWith(itCopy)) {
                                itCopy = itCopy.dropLast(1)
                            }
                            textFilter.value = current.dropLast(itCopy.length) + it.text + " "
                        }
                        if (it.caretBackwardsMovement > 0) {
                            val index = textFilter.value.length - 1 - it.caretBackwardsMovement
                            textFilter.element.executeJs("this.shadowRoot.children[1].children[1].children[1].children[0].setSelectionRange($index, $index)")
                        }
                    }
                }
            }
        }

        fromFilter = datePicker {
            placeholder = t("from")
            addValueChangeListener { onChange?.invoke() }
            isClearButtonVisible = true
        }
        toFilter = datePicker {
            placeholder = t("to")
            addValueChangeListener { onChange?.invoke() }
            isClearButtonVisible = true
        }
    }

    val filter
        get() = DocService.Filter(
                true, true,
                /*includeAllTags = t("all") == tagIncludeVariant.value,
                includeAllAttributes = t("all") == attributeIncludeVariant.value,
                includedTags = tagIncludeFilter.optionalValue.orElse(null),
                excludedTags = tagExcludeFilter.optionalValue.orElse(null),
                includedAttributes = attributeIncludeFilter.optionalValue.orElse(null),
                excludedAttributes = attributeExcludeFilter.optionalValue.orElse(null),*/
                from = fromFilter.optionalValue.orElse(null),
                to = toFilter.optionalValue.orElse(null),
                //text = textFilter.optionalValue.orElse(null)
        )

    fun refresh() {
        searchParser.refresh()
    }
}