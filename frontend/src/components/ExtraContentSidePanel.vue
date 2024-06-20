<script setup lang="ts">
import { computed, onMounted, ref } from "vue"
import { useRoute } from "vue-router"
import AttachmentView from "@/components/AttachmentView.vue"
import FileNavigator from "@/components/FileNavigator.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextEditor from "@/components/input/TextEditor.vue"
import SideToggle, { OpeningDirection } from "@/components/SideToggle.vue"
import useQuery from "@/composables/useQueryFromRoute"
import DocumentUnit from "@/domain/documentUnit"
import IconAttachFile from "~icons/ic/baseline-attach-file"
import IconStickyNote from "~icons/ic/outline-sticky-note-2"

interface Props {
  documentUnit: DocumentUnit
}

const props = defineProps<Props>()

const notesSelected = ref<boolean>(!!props.documentUnit.note)
const attachmentsSelected = ref<boolean>(
  !props.documentUnit.note && props.documentUnit.hasAttachments,
)
const currentAttachmentIndex = ref(0)
const isExpanded = ref(false)

const route = useRoute()
const { pushQueryToRoute } = useQuery()

const hasNote = computed(() => {
  return props.documentUnit.note && props.documentUnit.note.length > 0
})

const hasAttachments = computed(() => {
  return (
    props.documentUnit.attachments && props.documentUnit.attachments.length > 0
  )
})

const handleOnSelect = (index: number) => {
  currentAttachmentIndex.value = index
}

function selectNotes() {
  notesSelected.value = true
  attachmentsSelected.value = false
}

function selectAttachments(selectedIndex?: number) {
  if (selectedIndex !== undefined) currentAttachmentIndex.value = selectedIndex
  notesSelected.value = false
  attachmentsSelected.value = true
}

function togglePanel(expand?: boolean) {
  isExpanded.value = expand === undefined ? !isExpanded.value : expand
  pushQueryToRoute({
    ...route.query,
    showAttachmentPanel: isExpanded.value.toString(),
  })
}

function onAttachmentDeleted(index: number) {
  if (currentAttachmentIndex.value >= index) {
    currentAttachmentIndex.value = props.documentUnit.attachments.length - 1
  }
  if (props.documentUnit.attachments.length === 0) {
    selectNotes()
  }
}

defineExpose({ togglePanel, selectAttachments, onAttachmentDeleted })

onMounted(() => {
  if (route.query.showAttachmentPanel) {
    isExpanded.value = route.query.showAttachmentPanel === "true"
  } else {
    isExpanded.value = hasNote.value || hasAttachments.value
  }
})
</script>

<template>
  <FlexItem
    class="h-full flex-col border-l-1 border-solid border-gray-400 bg-white"
    :class="[isExpanded ? 'flex-1' : '', isExpanded ? 'w-1/2' : '']"
    data-testid="attachment-view-side-panel"
  >
    <SideToggle
      class="sticky top-[8rem] z-20"
      :is-expanded="isExpanded"
      label="Dokumentansicht"
      :opening-direction="OpeningDirection.LEFT"
      size="medium"
      tabindex="0"
      @keydown.enter="togglePanel"
      @update:is-expanded="togglePanel"
    >
      <FlexContainer class="m-16 ml-20 items-center space-x-8 px-8">
        <TextButton
          id="note"
          aria-label="Notiz anzeigen"
          :button-type="notesSelected ? 'primary' : 'tertiary'"
          :disabled="!hasNote"
          :icon="IconStickyNote"
          size="small"
          @click="selectNotes"
        />

        <TextButton
          id="attachments"
          aria-label="Dokumente anzeigen"
          :button-type="attachmentsSelected ? 'primary' : 'tertiary'"
          :disabled="!hasAttachments"
          :icon="IconAttachFile"
          size="small"
          @click="() => selectAttachments()"
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
            :current-index="currentAttachmentIndex"
            @select="handleOnSelect"
          ></FileNavigator>
          <AttachmentView
            v-if="
              documentUnit.uuid &&
              documentUnit.attachments &&
              currentAttachmentIndex != null &&
              documentUnit.attachments[currentAttachmentIndex] &&
              documentUnit.attachments[currentAttachmentIndex]?.s3path
            "
            :document-unit-uuid="documentUnit.uuid"
            :s3-path="documentUnit.attachments[currentAttachmentIndex].s3path"
          />
        </div>
      </div>
    </SideToggle>
  </FlexItem>
</template>
