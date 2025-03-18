<script setup lang="ts">
import FileNavigator from "@/components/FileNavigator.vue"
import TextButton from "@/components/input/TextButton.vue"
import Tooltip from "@/components/Tooltip.vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import DocumentUnit from "@/domain/documentUnit"
import { SelectablePanelContent } from "@/types/panelContentMode"
import IconAttachFile from "~icons/ic/baseline-attach-file"
import IconEdit from "~icons/ic/outline-edit"
import IconOpenInNewTab from "~icons/ic/outline-open-in-new"
import IconPreview from "~icons/ic/outline-remove-red-eye"
import IconStickyNote from "~icons/ic/outline-sticky-note-2"
import IconSpellCheck from "~icons/material-symbols/spellcheck"
import IconImportCategories from "~icons/material-symbols/text-select-move-back-word"

const props = defineProps<{
  panelMode?: SelectablePanelContent
  documentUnit?: DocumentUnit
  showEditButton?: boolean
  hidePanelModeBar?: boolean
  currentAttachmentIndex: number
}>()

const emit = defineEmits<{
  "panelMode:update": [value: SelectablePanelContent]
  "attachmentIndex:update": [value: number]
}>()

const textCheckAll = useFeatureToggle("neuris.text-check-all")

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
      <Tooltip shortcut="n" text="Notiz">
        <TextButton
          id="note"
          aria-label="Notiz anzeigen"
          button-type="tertiary"
          class="flex"
          :class="panelMode === 'note' ? 'bg-blue-200' : ''"
          data-testid="note-button"
          :icon="IconStickyNote"
          size="small"
          @click="() => emitSidePanelMode('note')"
        />
      </Tooltip>
      <Tooltip shortcut="d" text="Datei">
        <TextButton
          id="attachments"
          aria-label="Dokumente anzeigen"
          button-type="tertiary"
          :class="panelMode === 'attachments' ? 'bg-blue-200' : ''"
          data-testid="attachments-button"
          :icon="IconAttachFile"
          size="small"
          @click="() => emitSidePanelMode('attachments')"
        />
      </Tooltip>

      <Tooltip shortcut="v" text="Vorschau">
        <TextButton
          id="preview"
          aria-label="Vorschau anzeigen"
          button-type="tertiary"
          :class="panelMode === 'preview' ? 'bg-blue-200' : ''"
          data-testid="preview-button"
          :icon="IconPreview"
          size="small"
          @click="() => emitSidePanelMode('preview')"
        />
      </Tooltip>

      <Tooltip shortcut="r" text="Rubriken-Import">
        <TextButton
          id="category-import"
          aria-label="Rubriken-Import anzeigen"
          button-type="tertiary"
          :class="panelMode === 'category-import' ? 'bg-blue-200' : ''"
          data-testid="category-import-button"
          :icon="IconImportCategories"
          size="small"
          @click="() => emitSidePanelMode('category-import')"
        />
      </Tooltip>
      <Tooltip v-if="textCheckAll" shortcut="t" text="Rechtschreibprüfung">
        <TextButton
          id="text-check"
          aria-label="Rechtschreibprüfung"
          button-type="tertiary"
          :class="panelMode === 'text-check' ? 'bg-blue-200' : ''"
          data-testid="text-check-button"
          :icon="IconSpellCheck"
          size="small"
          @click="() => emitSidePanelMode('text-check')"
        />
      </Tooltip>
    </div>

    <FileNavigator
      v-if="panelMode === 'attachments'"
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
          :to="{
            name: 'caselaw-documentUnit-documentNumber-categories',
            params: {
              documentNumber: props.documentUnit!.documentNumber,
            },
          }"
        >
          <TextButton button-type="ghost" :icon="IconEdit" size="small" />
        </router-link>
      </Tooltip>
      <Tooltip text="In neuem Tab öffnen">
        <router-link
          aria-label="Vorschau in neuem Tab öffnen"
          target="_blank"
          :to="{
            name: 'caselaw-documentUnit-documentNumber-preview',
            params: {
              documentNumber: props.documentUnit!.documentNumber,
            },
          }"
        >
          <TextButton
            button-type="ghost"
            :icon="IconOpenInNewTab"
            size="small"
          />
        </router-link>
      </Tooltip>
    </div>
  </div>
</template>
