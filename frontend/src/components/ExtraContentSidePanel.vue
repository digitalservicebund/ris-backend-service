<script setup lang="ts">
import { storeToRefs } from "pinia"
import type { Component } from "vue"
import { computed, onMounted } from "vue"
import { useRoute } from "vue-router"
import AttachmentView from "@/components/AttachmentView.vue"
import CategoryImport from "@/components/category-import/CategoryImport.vue"
import ExtraContentExtraContentSidePanelMenu from "@/components/ExtraContentSidePanelMenu.vue"
import FlexItem from "@/components/FlexItem.vue"
import InputField from "@/components/input/InputField.vue"
import TextAreaInput from "@/components/input/TextAreaInput.vue"
import DocumentUnitPreview from "@/components/preview/DocumentUnitPreview.vue"
import SideToggle, { OpeningDirection } from "@/components/SideToggle.vue"
import DocumentationUnitTextCheckSummary from "@/components/text-check/DocumentationUnitTextCheckSummary.vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import DocumentUnit from "@/domain/documentUnit"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"
import { SelectablePanelContent } from "@/types/panelContentMode"
import { Match } from "@/types/textCheck"

const props = defineProps<{
  documentUnit?: DocumentUnit
  showEditButton?: boolean
  hidePanelModeBar?: boolean
  sidePanelMode?: SelectablePanelContent
  sidePanelShortcut?: string
  icon?: Component
  jumpToMatch?: (match: Match) => void
}>()

const store = useExtraContentSidePanelStore()

const { panelMode, currentAttachmentIndex, importDocumentNumber } =
  storeToRefs(store)

const route = useRoute()

const textCheckAll = useFeatureToggle("neuris.text-side-panel")

const hasNote = computed(() => {
  return !!props.documentUnit!.note && props.documentUnit!.note.length > 0
})

const hasAttachments = computed(() => {
  return (
    !!props.documentUnit!.attachments &&
    props.documentUnit!.attachments.length > 0
  )
})

const shortCut = computed(() => props.sidePanelShortcut ?? "<")

/**
 * Updates the local attachment index reference, which is used to display the selected attachment in the panel,
 * if the panel content is set to "attachments".
 * @param index
 */
const handleOnSelectAttachment = (index: number) => {
  store.currentAttachmentIndex = index
}

/**
 * Sets the panel content to "attachments", so that the attachment view is displayed in the panel.
 * If a selected attachment index is provided, the local attachment index reference is updated accordingly,
 * so that the selected attachment is displayed in the attachment view.
 * @param selectedIndex (optional) selected attachment index
 */
function selectAttachments(selectedIndex?: number) {
  store.selectAttachments(selectedIndex)
}

/**
 * Sets the panel content to selected mode
 */
function setSidePanelMode(panelMode: SelectablePanelContent) {
  store.setSidePanelMode(panelMode)
}

/**
 * Expands or collapses the panel.
 * Can be forced by passing a boolean parameter. Otherwise, it will collapse when expanded and expand when collapsed.
 * Pushes the state to the route as a query parameter.
 * @param expand optional boolean to enforce expanding or collapsing
 */
function togglePanel(expand?: boolean): boolean {
  return store.togglePanel(expand)
}

function setDefaultState() {
  if (props.sidePanelMode) {
    store.setSidePanelMode(props.sidePanelMode)
  } else if (!props.documentUnit!.note && props.documentUnit!.hasAttachments) {
    selectAttachments()
  } else {
    setSidePanelMode("note")
  }
}

/**
 * Checks whether the panel should be expanded when it is mounted.
 * If the showAttachmentPanel query parameter is present in the route, its value is taken. This parameter is only present,
 * after the user first interacts with the panel, by expanding or collapsing it manually.
 * This ensures that their selection does not get overridden.
 * If the query is not present, the panel is expanded by default if either a note, an attachment or both are present.
 * Otherwise, it is collapsed by default.
 */
onMounted(() => {
  setDefaultState()
  if (route.query.showAttachmentPanel) {
    store.isExpanded = route.query.showAttachmentPanel === "true"
  } else {
    store.isExpanded = hasNote.value || hasAttachments.value
  }
})
</script>

<template>
  <FlexItem
    class="h-full flex-col border-l-1 border-solid border-gray-400 bg-white"
    :class="[store.isExpanded ? 'flex-1' : '', store.isExpanded ? 'w-1/2' : '']"
    data-testid="attachment-view-side-panel"
  >
    <SideToggle
      class="sticky top-[4rem] z-20 max-h-fit"
      custom-button-classes="top-24 pt-4"
      :icon="icon"
      :is-expanded="store.isExpanded"
      label="Seitenpanel"
      :opening-direction="OpeningDirection.LEFT"
      :shortcut="shortCut"
      tabindex="0"
      @update:is-expanded="togglePanel"
    >
      <ExtraContentExtraContentSidePanelMenu
        :current-attachment-index="currentAttachmentIndex"
        :document-unit="props.documentUnit"
        :hide-panel-mode-bar="props.hidePanelModeBar"
        :panel-mode="panelMode"
        :show-edit-button="props.showEditButton"
        @attachment-index:update="handleOnSelectAttachment"
        @panel-mode:update="setSidePanelMode"
      />
      <div class="m-24">
        <div v-if="panelMode === 'note'">
          <InputField id="notesInput" v-slot="{ id }" label="Notiz">
            <TextAreaInput
              :id="id"
              v-model="props.documentUnit!.note"
              aria-label="Notiz Eingabefeld"
              autosize
              class="w-full"
              custom-classes="max-h-[65vh]"
            />
          </InputField>
        </div>
        <div v-else-if="panelMode === 'attachments'">
          <AttachmentView
            v-if="
              props.documentUnit!.uuid &&
              props.documentUnit!.attachments &&
              props.documentUnit!.attachments[currentAttachmentIndex]?.format
            "
            :document-unit-uuid="props.documentUnit!.uuid"
            :format="
              props.documentUnit!.attachments[currentAttachmentIndex].format
            "
            :s3-path="
              props.documentUnit!.attachments[currentAttachmentIndex].s3path
            "
          />
          <div v-else class="ris-label1-regular">
            Wenn eine Datei hochgeladen ist, können Sie die Datei hier sehen.
          </div>
        </div>
        <div
          v-else-if="panelMode === 'preview'"
          id="preview-container"
          class="flex max-h-[70vh] overflow-auto"
        >
          <DocumentUnitPreview
            :document-unit="props.documentUnit!"
            layout="narrow"
          />
        </div>

        <CategoryImport
          v-else-if="panelMode === 'category-import'"
          :document-number="importDocumentNumber"
        />

        <DocumentationUnitTextCheckSummary
          v-else-if="panelMode === 'text-check' && textCheckAll"
          v-bind="{ jumpToMatch }"
        />
      </div>
    </SideToggle>
  </FlexItem>
</template>
