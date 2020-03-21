package com.dude.dms.ui.views

import com.dude.dms.backend.data.Tag
import com.dude.dms.backend.data.docs.Doc
import com.dude.dms.backend.data.mails.Mail
import com.dude.dms.backend.service.DocService
import com.dude.dms.backend.service.MailService
import com.dude.dms.backend.service.TagService
import com.dude.dms.brain.FileManager
import com.dude.dms.brain.events.EventManager
import com.dude.dms.brain.events.EventType
import com.dude.dms.brain.t
import com.dude.dms.ui.Const
import com.dude.dms.ui.builder.BuilderFactory
import com.dude.dms.ui.dataproviders.DocDataProvider
import com.dude.dms.ui.dataproviders.GridViewDataProvider
import com.dude.dms.ui.extensions.convert
import com.github.appreciated.app.layout.component.menu.left.items.LeftClickableItem
import com.helger.commons.io.file.FileHelper
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.grid.dnd.GridDropMode
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.router.*
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import dev.mett.vaadin.tooltip.Tooltips
import org.vaadin.olli.FileDownloadWrapper

@Route(value = Const.PAGE_DOCS, layout = MainView::class)
@RouteAlias(value = Const.PAGE_ROOT, layout = MainView::class)
@PageTitle("Docs")
class DocsView(
        private val builderFactory: BuilderFactory,
        private val docService: DocService,
        private val tagService: TagService,
        private val mailService: MailService,
        private val fileManager: FileManager,
        docDataProvider: DocDataProvider,
        eventManager: EventManager
) : GridView<Doc>(), HasUrlParameter<String?> {

    private val ui = UI.getCurrent()

    init {
        eventManager.register(this, Doc::class, EventType.CREATE, EventType.DELETE) { ui.access { grid.dataProvider.refreshAll() } }
        eventManager.register(this, Doc::class, EventType.UPDATE) { ui.access { grid.dataProvider.refreshItem(it) } }
        eventManager.register(this, Tag::class, EventType.CREATE, EventType.UPDATE, EventType.DELETE) { ui.access { grid.dataProvider.refreshAll() } }

        grid.dataProvider = docDataProvider
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES)
        grid.addColumn { it.documentDate?.convert() }.setHeader(t("date"))
        grid.addComponentColumn { builderFactory.tags().container(tagService.findByDoc(it).toMutableSet()) }.setHeader(t("tags"))
        grid.addComponentColumn { createGridActions(it) }
        grid.addColumn { it.guid }
        grid.columns.forEach { it.setResizable(true).setAutoWidth(true) }
        grid.isColumnReorderingAllowed = true

        grid.addItemDoubleClickListener { event -> builderFactory.docs().imageDialog(event.item!!).open() }
        grid.dropMode = GridDropMode.ON_TOP
        grid.addDropListener { event ->
            // Workaround
            val comp = event.source.ui.get().internals.activeDragSourceComponent
            if (comp is LeftClickableItem) {
                val doc = event.dropTargetItem.get()
                tagService.findByName(comp.name)?.let { tag ->
                    val tags = tagService.findByDoc(doc).toMutableSet()
                    if (tags.add(tag)) {
                        doc.tags = tags
                        docService.save(doc)
                        grid.dataProvider.refreshAll()
                    }
                }
            }
        }

        createContextMenu()
    }

    private fun createContextMenu() {
        val menu = grid.addContextMenu()

        menu.setDynamicContentHandler { doc ->
            menu.removeAll()
            if (doc == null) return@setDynamicContentHandler false

            menu.addItem(t("view")) { builderFactory.docs().imageDialog(doc).open() }
            menu.addItem(t("edit")) { builderFactory.docs().editDialog(doc).open() }
            menu.addItem(t("delete")) { builderFactory.docs().deleteDialog(doc).open() }
            true
        }
    }

    private fun createGridActions(doc: Doc): HorizontalLayout {
        val file = fileManager.getPdf(doc.guid)
        val download = FileDownloadWrapper(StreamResource("${doc.guid}.pdf", InputStreamFactory { FileHelper.getInputStream(file) }))
        val downloadButton = Button(VaadinIcon.FILE_TEXT.create())
        download.wrapComponent(downloadButton)
        Tooltips.getCurrent().setTooltip(downloadButton, "Download")

        val edit = Button(VaadinIcon.EDIT.create()) { builderFactory.docs().editDialog(doc).open() }
        Tooltips.getCurrent().setTooltip(edit, t("edit"))

        return HorizontalLayout(download, edit)
    }

    @Suppress("UNCHECKED_CAST")
    private fun refreshFilter(tag: Tag? = null, mail: Mail? = null) {
        val filter = DocDataProvider.Filter(tag, mail)
        val dp = grid.dataProvider as GridViewDataProvider<Doc, DocDataProvider.Filter>
        ui.access {
            try {
                dp.setFilter(filter)
                dp.refreshAll()
            } catch (e: IllegalStateException) {
            } catch (e: IllegalArgumentException) { }
        }
    }

    override fun setParameter(beforeEvent: BeforeEvent, @OptionalParameter t: String?) {
        if (!t.isNullOrEmpty()) {
            val parts = t.split(":").toTypedArray()
            if ("tag".equals(parts[0], ignoreCase = true)) {
                refreshFilter(tag = tagService.findByName(parts[1]))
            } else if ("mail".equals(parts[0], ignoreCase = true)) {
                refreshFilter(mail = mailService.load(parts[1].toLong()))
            }
        } else {
            refreshFilter()
        }
    }
}