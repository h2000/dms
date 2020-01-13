package com.dude.dms.ui.components.dialogs

import com.dude.dms.ui.components.ValueProvider
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.splitlayout.SplitLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.value.ValueChangeMode
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class RegexDialog(private val callBack: TextField, private val matches: ArrayList<String> = ArrayList()) : Dialog() {

    private val regexField = TextField("Regex", callBack.value, "").apply {
        setWidthFull()
        addValueChangeListener { refreshResults() }
        valueChangeMode = ValueChangeMode.EAGER
    }

    private val textArea = TextArea("Test area").apply {
        setWidthFull()
        height = "50%"
        addValueChangeListener { refreshResults() }
        valueChangeMode = ValueChangeMode.EAGER
    }

    private val grid = Grid<String>().apply {
        addColumn { it }.setHeader("Matches")
        setItems(matches)
        height = "30%"
    }

    constructor(callBack: TextField, valueProvider: ValueProvider<String>) : this(callBack) {
        textArea.value = valueProvider.value
        refreshResults()
    }

    init {
        width = "60vw"
        height = "60vh"

        val horizontalLayout = HorizontalLayout(regexField, Button(VaadinIcon.CHECK.create()) { save() }).apply {
            alignItems = FlexComponent.Alignment.END
        }
        val splitLayout = SplitLayout(textArea, grid).apply {
            setWidthFull()
            height = "80%"
            orientation = SplitLayout.Orientation.VERTICAL
        }
        add(horizontalLayout, splitLayout)
    }

    private fun refreshResults() {
        matches.clear()
        if (!regexField.isEmpty && !textArea.isEmpty) {
            regexField.errorMessage = ""
            try {
                val pattern = Pattern.compile(regexField.value)
                textArea.value.split("\n").toTypedArray()
                        .map { pattern.matcher(it) }
                        .forEach {
                            while (it.find()) {
                                val result = it.group()
                                if (result.isNotEmpty()) {
                                    matches.add(result)
                                }
                            }
                        }
            } catch (e: PatternSyntaxException) {
                regexField.errorMessage = "Invalid regex!"
            }
        }
        grid.dataProvider.refreshAll()
    }

    private fun save() {
        callBack.value = regexField.value
        close()
    }
}