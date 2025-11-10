<script setup lang="ts">
import Button from "primevue/button"
import { computed } from "vue"
import { RouteLocationRaw } from "vue-router"
import FileNavigator from "@/components/FileNavigator.vue"
import Tooltip from "@/components/Tooltip.vue"
import { DocumentationUnit } from "@/domain/documentationUnit"
import { SelectablePanelContent } from "@/types/panelContentMode"
import { isDecision } from "@/utils/typeGuards"
import IconAttachFile from "~icons/ic/baseline-attach-file"
import IconEdit from "~icons/ic/outline-edit"
import IconOpenInNewTab from "~icons/ic/outline-open-in-new"
import IconPreview from "~icons/ic/outline-remove-red-eye"
import IconStickyNote from "~icons/ic/outline-sticky-note-2"
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

const getRouterLinkTo = (suffix: "categories" | "preview") =>
  computed(() => {
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
      <Tooltip v-if="isDecision(documentUnit)" shortcut="n" text="Notiz">
        <Button
          id="note"
          aria-label="Notiz anzeigen"
          class="focus-visible:z-20"
          data-testid="note-button"
          severity="secondary"
          size="small"
          @click="() => emitSidePanelMode('note')"
        >
          <template #icon>
            <IconStickyNote />
          </template>
        </Button>
      </Tooltip>
      <Tooltip v-if="isDecision(documentUnit)" shortcut="d" text="Datei">
        <Button
          id="attachments"
          aria-label="Dokumente anzeigen"
          class="focus-visible:z-20"
          data-testid="attachments-button"
          severity="secondary"
          size="small"
          @click="() => emitSidePanelMode('attachments')"
        >
          <template #icon>
            <IconAttachFile />
          </template>
        </Button>
      </Tooltip>

      <Tooltip shortcut="v" text="Vorschau">
        <Button
          id="preview"
          aria-label="Vorschau anzeigen"
          class="focus-visible:z-20"
          data-testid="preview-button"
          severity="secondary"
          size="small"
          @click="() => emitSidePanelMode('preview')"
        >
          <template #icon>
            <IconPreview />
          </template>
        </Button>
      </Tooltip>

      <Tooltip shortcut="r" text="Rubriken-Import">
        <Button
          id="category-import"
          aria-label="Rubriken-Import anzeigen"
          class="focus-visible:z-20"
          data-testid="category-import-button"
          severity="secondary"
          size="small"
          @click="() => emitSidePanelMode('category-import')"
        >
          <template #icon>
            <IconImportCategories />
          </template>
        </Button>
      </Tooltip>
    </div>

    <FileNavigator
      v-if="panelMode === 'attachments' && isDecision(documentUnit)"
      :attachments="documentUnit!.attachments"
      :current-index="currentAttachmentIndex"
      @select="emitAttachmentIndex"
    ></FileNavigator>
    <div v-if="panelMode === 'preview'" class="ml-auto flex flex-row">
      <Tooltip
        v-if="documentUnit!.isEditable && showEditButton"
        shortcut="b"
        text="Bearbeiten"
      >
        <router-link
          aria-label="Dokumentationseinheit in einem neuen Tab bearbeiten"
          target="_blank"
          :to="getRouterLinkTo('categories') as RouteLocationRaw"
        >
          <Button text>
            <template #icon>
              <IconEdit />
            </template>
          </Button>
        </router-link>
      </Tooltip>
      <Tooltip text="In neuem Tab öffnen">
        <router-link
          aria-label="Vorschau in neuem Tab öffnen"
          target="_blank"
          :to="getRouterLinkTo('preview') as RouteLocationRaw"
        >
          <Button text>
            <template #icon>
              <IconOpenInNewTab />
            </template>
          </Button>
        </router-link>
      </Tooltip>
    </div>
  </div>
</template>
