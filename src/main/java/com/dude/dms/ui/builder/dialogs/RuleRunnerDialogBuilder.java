package com.dude.dms.ui.builder.dialogs;

import com.dude.dms.backend.data.docs.Doc;
import com.dude.dms.backend.data.tags.Tag;
import com.dude.dms.backend.service.DocService;
import com.dude.dms.ui.components.dialogs.RuleRunnerDialog;

import java.util.Map;
import java.util.Set;

public final class RuleRunnerDialogBuilder {

    private final Map<Doc, Set<Tag>> result;

    private final DocService docService;

    RuleRunnerDialogBuilder(Map<Doc, Set<Tag>> result, DocService docService) {
        this.docService = docService;
        this.result = result;
    }

    public RuleRunnerDialog build() {
        return new RuleRunnerDialog(result, docService);
    }
}
