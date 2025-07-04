<script setup lang="ts">
import Button from "primevue/button"
import FileNavigator from "@/components/FileNavigator.vue"
import Tooltip from "@/components/Tooltip.vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import { DocumentationUnit } from "@/domain/documentationUnit"
import { SelectablePanelContent } from "@/types/panelContentMode"
import { isDecision } from "@/utils/typeGuards"
import IconAttachFile from "~icons/ic/baseline-attach-file"
import IconEdit from "~icons/ic/outline-edit"
import IconOpenInNewTab from "~icons/ic/outline-open-in-new"
import IconPreview from "~icons/ic/outline-remove-red-eye"
import IconStickyNote from "~icons/ic/outline-sticky-note-2"
import IconSpellCheck from "~icons/material-symbols/spellcheck"
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

const textCheckAll = useFeatureToggle("neuris.text-check-side-panel")

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
      <Tooltip
        v-if="textCheckAll && isDecision(documentUnit)"
        shortcut="t"
        text="Rechtschreibprüfung"
      >
        <Button
          id="text-check"
          aria-label="Rechtschreibprüfung"
          class="focus-visible:z-20"
          data-testid="text-check-button"
          severity="secondary"
          size="small"
          @click="() => emitSidePanelMode('text-check')"
        >
          <template #icon>
            <IconSpellCheck />
          </template>
        </Button>
      </Tooltip>
    </div>

    <FileNavigator
      v-if="panelMode === 'attachments' && isDecision(props.documentUnit)"
      :attachments="props.documentUnit!.attachments"
      :current-index="currentAttachmentIndex"
      @select="emitAttachmentIndex"
    ></FileNavigator>
    <div v-if="panelMode === 'preview'" class="ml-auto flex flex-row">
      <Tooltip
        v-if="props.documentUnit!.isEditable && showEditButton"
        shortcut="b"
        text="Bearbeiten"
      >
        <router-link
          aria-label="Dokumentationseinheit in einem neuen Tab bearbeiten"
          target="_blank"
          :to="
            isDecision(documentUnit)
              ? {
                  name: 'caselaw-documentUnit-documentNumber-categories',
                  params: {
                    documentNumber: props.documentUnit!.documentNumber,
                  },
                }
              : {
                  name: 'caselaw-pending-proceeding-documentNumber-categories',
                  params: {
                    documentNumber: props.documentUnit!.documentNumber,
                  },
                }
          "
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
          :to="
            isDecision(documentUnit)
              ? {
                  name: 'caselaw-documentUnit-documentNumber-preview',
                  params: {
                    documentNumber: props.documentUnit!.documentNumber,
                  },
                }
              : {
                  name: 'caselaw-pending-proceeding-documentNumber-preview',
                  params: {
                    documentNumber: props.documentUnit!.documentNumber,
                  },
                }
          "
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
