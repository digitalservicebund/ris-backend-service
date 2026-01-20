<script setup lang="ts">
import Button from "primevue/button"
import { computed } from "vue"
import { RouteLocationRaw } from "vue-router"
import FileNavigator from "@/components/FileNavigator.vue"
import { DocumentationUnit } from "@/domain/documentationUnit"
import { SelectablePanelContent } from "@/types/panelContentMode"
import { isDecision } from "@/utils/typeGuards"
import IconAttachFile from "~icons/ic/baseline-attach-file"
import IconEdit from "~icons/ic/outline-edit"
import IconOpenInNewTab from "~icons/ic/outline-open-in-new"
import IconPreview from "~icons/ic/outline-remove-red-eye"
import IconStickyNote from "~icons/ic/outline-sticky-note-2"
import IconOtherAttachmentsWithFiles from "~icons/material-symbols/folder"
import IconOtherAttachmentsWithoutFiles from "~icons/material-symbols/folder-outline"
import IconImportCategories from "~icons/material-symbols/text-select-move-back-word"

const props = defineProps<{
  panelMode?: SelectablePanelContent
  documentUnit?: DocumentationUnit
  showEditButton?: boolean
  hidePanelModeBar?: boolean
  currentAttachmentIndex: number
}>()

const emit = defineEmits<{
  "panelMode:update": [value: SelectablePanelContent]
  "attachmentIndex:update": [value: number]
}>()

/**
 * Formats the tooltip text with an optional shortcut in parentheses.
 */
const tooltipValue = (text: string, shortcut?: string) => {
  return shortcut ? `${text} (${shortcut})` : text
}

/**
 * Generates the router link destination based on the document type.
 */
const getRouterLinkTo = (suffix: "categories" | "preview") =>
  computed<RouteLocationRaw>(() => {
    return {
      name: isDecision(props.documentUnit)
        ? `caselaw-documentUnit-documentNumber-${suffix}`
        : `caselaw-pending-proceeding-documentNumber-${suffix}`,
      params: {
        documentNumber: props.documentUnit?.documentNumber ?? "undefined",
      },
    }
  })

function emitSidePanelMode(value: SelectablePanelContent) {
  emit("panelMode:update", value)
}

function emitAttachmentIndex(value: number) {
  emit("attachmentIndex:update", value)
}
</script>

<template>
  <div class="m-24 flex flex-row justify-between">
    <div v-if="!hidePanelModeBar" class="flex flex-row -space-x-2">
      <Button
        v-if="isDecision(documentUnit)"
        id="note"
        v-tooltip.bottom="tooltipValue('Notiz', 'n')"
        aria-label="Notiz anzeigen"
        class="focus-visible:z-20"
        data-testid="note-button"
        :severity="panelMode === 'note' ? 'primary' : 'secondary'"
        size="small"
        @click="() => emitSidePanelMode('note')"
      >
        <template #icon>
          <IconStickyNote />
        </template>
      </Button>

      <Button
        v-if="isDecision(documentUnit)"
        id="attachments"
        v-tooltip.bottom="tooltipValue('Originaldokument', 'o')"
        aria-label="Originaldokument anzeigen"
        class="focus-visible:z-20"
        data-testid="attachments-button"
        :severity="panelMode === 'original-document' ? 'primary' : 'secondary'"
        size="small"
        @click="() => emitSidePanelMode('original-document')"
      >
        <template #icon>
          <IconAttachFile />
        </template>
      </Button>

      <Button
        id="preview"
        v-tooltip.bottom="tooltipValue('Vorschau', 'v')"
        aria-label="Vorschau anzeigen"
        class="focus-visible:z-20"
        data-testid="preview-button"
        :severity="panelMode === 'preview' ? 'primary' : 'secondary'"
        size="small"
        @click="() => emitSidePanelMode('preview')"
      >
        <template #icon>
          <IconPreview />
        </template>
      </Button>

      <Button
        id="category-import"
        v-tooltip.bottom="tooltipValue('Rubriken-Import', 'r')"
        aria-label="Rubriken-Import anzeigen"
        class="focus-visible:z-20"
        data-testid="category-import-button"
        :severity="panelMode === 'category-import' ? 'primary' : 'secondary'"
        size="small"
        @click="() => emitSidePanelMode('category-import')"
      >
        <template #icon>
          <IconImportCategories />
        </template>
      </Button>

      <Button
        v-if="isDecision(documentUnit)"
        id="other-attachments"
        v-tooltip.bottom="tooltipValue('Anhänge', 'a')"
        aria-label="Anhänge anzeigen"
        class="focus-visible:z-20"
        data-testid="other-attachments-button"
        :severity="panelMode === 'other-attachments' ? 'primary' : 'secondary'"
        size="small"
        @click="() => emitSidePanelMode('other-attachments')"
      >
        <template #icon>
          <IconOtherAttachmentsWithoutFiles
            v-if="documentUnit?.otherAttachments?.length === 0"
          />
          <IconOtherAttachmentsWithFiles v-else />
        </template>
      </Button>
    </div>

    <FileNavigator
      v-if="panelMode === 'original-document' && isDecision(documentUnit)"
      :attachments="documentUnit!.attachments"
      :current-index="currentAttachmentIndex"
      @select="emitAttachmentIndex"
    />

    <div v-if="panelMode === 'preview'" class="ml-auto flex flex-row">
      <router-link
        v-if="documentUnit?.isEditable && showEditButton"
        target="_blank"
        :to="getRouterLinkTo('categories').value"
      >
        <Button
          v-tooltip.bottom="tooltipValue('Bearbeiten')"
          aria-label="Dokumentationseinheit in einem neuen Tab bearbeiten"
          text
        >
          <template #icon>
            <IconEdit />
          </template>
        </Button>
      </router-link>

      <router-link target="_blank" :to="getRouterLinkTo('preview').value">
        <Button
          v-tooltip.bottom="tooltipValue('In neuem Tab öffnen')"
          aria-label="Vorschau in neuem Tab öffnen"
          size="small"
          text
        >
          <template #icon>
            <IconOpenInNewTab />
          </template>
        </Button>
      </router-link>
    </div>
  </div>
</template>
