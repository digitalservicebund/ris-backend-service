<script setup lang="ts">
import { storeToRefs } from "pinia"
import { computed, onMounted } from "vue"
import { useRoute } from "vue-router"
import AttachmentView from "@/components/AttachmentView.vue"
import CategoryImport from "@/components/category-import/CategoryImport.vue"
import ExtraContentExtraContentSidePanelMenu from "@/components/ExtraContentSidePanelMenu.vue"
import FlexItem from "@/components/FlexItem.vue"
import InputField from "@/components/input/InputField.vue"
import TextAreaInput from "@/components/input/TextAreaInput.vue"
import { ExtraContentSidePanelProps } from "@/components/input/types"
import OtherAttachments from "@/components/OtherAttachments.vue"
import DecisionPreview from "@/components/preview/DecisionPreview.vue"
import PendingProceedingPreview from "@/components/preview/PendingProceedingPreview.vue"
import SideToggle, { OpeningDirection } from "@/components/SideToggle.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useInternalUser } from "@/composables/useInternalUser"
import { useExtraContentSidePanelStore } from "@/stores/extraContentSidePanelStore"
import { SelectablePanelContent } from "@/types/panelContentMode"
import { isDecision, isPendingProceeding } from "@/utils/typeGuards"

const props = defineProps<ExtraContentSidePanelProps>()

const store = useExtraContentSidePanelStore()

const { panelMode, currentAttachmentIndex, importDocumentNumber } =
  storeToRefs(store)

const route = useRoute()

const hasNote = computed(() => {
  return (
    isDecision(props.documentUnit) &&
    !!props.documentUnit!.note &&
    props.documentUnit!.note.length > 0
  )
})

const hasAttachments = computed(() => {
  return (
    isDecision(props.documentUnit) &&
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
    setSidePanelMode(props.sidePanelMode)
    return
  }

  if (
    isDecision(props.documentUnit) &&
    !props.documentUnit?.note &&
    props.documentUnit?.hasAttachments
  ) {
    setSidePanelMode("original-document")
    return
  }

  if (isPendingProceeding(props.documentUnit)) {
    setSidePanelMode("preview")
    return
  }

  setSidePanelMode("note")
}

const isInternalUser = useInternalUser()

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
        <div v-if="panelMode === 'note' && isDecision(props.documentUnit)">
          <TitleElement class="mb-24">Notiz</TitleElement>
          <InputField id="notesInput" v-slot="{ id }" label="">
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
        <div
          v-else-if="
            panelMode === 'original-document' && isDecision(props.documentUnit)
          "
        >
          <AttachmentView
            v-if="
              props.documentUnit.uuid &&
              props.documentUnit.attachments &&
              props.documentUnit.attachments[currentAttachmentIndex]?.format
            "
            :document-unit-uuid="props.documentUnit.uuid"
            :format="
              props.documentUnit.attachments[currentAttachmentIndex].format
            "
            :s3-path="
              props.documentUnit.attachments[currentAttachmentIndex].s3path
            "
          />
          <div v-else class="ris-label1-regular">
            Wenn eine Datei hochgeladen ist, können Sie die Datei hier sehen.
          </div>
        </div>
        <div
          v-else-if="panelMode === 'preview'"
          id="preview-container"
          class="flex max-h-[70vh] flex-col overflow-auto"
        >
          <TitleElement>Vorschau</TitleElement>
          <DecisionPreview
            v-if="isDecision(props.documentUnit)"
            layout="narrow"
          />
          <PendingProceedingPreview
            v-if="isPendingProceeding(props.documentUnit)"
            :document-number="props.documentUnit.documentNumber"
            layout="narrow"
          />
        </div>

        <CategoryImport
          v-else-if="panelMode === 'category-import'"
          :document-number="importDocumentNumber"
        />
        <OtherAttachments
          v-else-if="panelMode === 'other-attachments' && isInternalUser"
        />
      </div>
    </SideToggle>
  </FlexItem>
</template>
