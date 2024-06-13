<script setup lang="ts">
import { computed, ref } from "vue"
import AttachmentView from "@/components/AttachmentView.vue"
import FileNavigator from "@/components/FileNavigator.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextEditor from "@/components/input/TextEditor.vue"
import SideToggle, { OpeningDirection } from "@/components/SideToggle.vue"
import DocumentUnit from "@/domain/documentUnit"
import IconAttachFile from "~icons/ic/baseline-attach-file"
import IconStickyNote from "~icons/ic/outline-sticky-note-2"

interface Props {
  isExpanded: boolean
  documentUnit: DocumentUnit
  currentIndex: number
  label?: string
}

const props = withDefaults(defineProps<Props>(), {
  label: "extra content view side panel",
})

const emit = defineEmits<{
  (e: "toggle", isExpanded: boolean): void
  (e: "select", index: number): void
}>()

const notesSelected = ref<boolean>(!!props.documentUnit.note)
const attachmentsSelected = ref<boolean>(
  !props.documentUnit.note && props.documentUnit.hasAttachments,
)

const handlePanelExpanded = () => {
  emit("toggle", !props.isExpanded)
}

const handleOnSelect = (index: number) => {
  emit("select", index)
}

const hasNote = computed(() => {
  return props.documentUnit.note && props.documentUnit.note.length > 0
})

const hasAttachments = computed(() => {
  return (
    props.documentUnit.attachments && props.documentUnit.attachments.length > 0
  )
})
function selectNotes() {
  notesSelected.value = true
  attachmentsSelected.value = false
}

function selectAttachments() {
  notesSelected.value = false
  attachmentsSelected.value = true
}
</script>

<template>
  <FlexItem
    class="h-full flex-col border-l-1 border-solid border-gray-400 bg-white"
    :class="[props.isExpanded ? 'flex-1' : '', props.isExpanded ? 'w-1/2' : '']"
    data-testid="attachment-view-side-panel"
  >
    <SideToggle
      class="sticky top-[8rem] z-20"
      :is-expanded="props.isExpanded"
      label="Dokumentansicht"
      :opening-direction="OpeningDirection.LEFT"
      size="medium"
      tabindex="0"
      @keydown.enter="handlePanelExpanded"
      @update:is-expanded="handlePanelExpanded"
    >
      <FlexContainer class="m-16 ml-20 items-center space-x-8 px-8">
        <TextButton
          id="note"
          aria-label="Notiz anzeigen"
          button-type="tertiary"
          :disabled="!hasNote"
          :icon="IconStickyNote"
          size="small"
          @click="selectNotes"
        />

        <TextButton
          id="attachments"
          aria-label="Dokumente anzeigen"
          button-type="tertiary"
          :disabled="!hasAttachments"
          :icon="IconAttachFile"
          size="small"
          @click="selectAttachments"
        />
      </FlexContainer>

      <div class="p-16">
        <div v-if="notesSelected">
          <label class="ds-label-02-reg mb-4">{{ "Notiz" }}</label>

          <TextEditor
            class="ml-2 pl-2 outline outline-2 outline-blue-900"
            field-size="big"
            :value="documentUnit.note"
          />
        </div>
        <div v-if="attachmentsSelected">
          <FileNavigator
            :attachments="documentUnit.attachments"
            :current-index="props.currentIndex"
            @select="handleOnSelect"
          ></FileNavigator>
          <AttachmentView
            v-if="
              documentUnit.uuid &&
              documentUnit.attachments &&
              props.currentIndex != null &&
              documentUnit.attachments[props.currentIndex] &&
              documentUnit.attachments[props.currentIndex]?.s3path
            "
            :document-unit-uuid="documentUnit.uuid"
            :s3-path="documentUnit.attachments[props.currentIndex].s3path"
          />
        </div>
      </div>
    </SideToggle>
  </FlexItem>
</template>
