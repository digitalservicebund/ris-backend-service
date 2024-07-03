<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue"
import { useRoute } from "vue-router"
import AttachmentView from "@/components/AttachmentView.vue"
import FileNavigator from "@/components/FileNavigator.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import InputField from "@/components/input/InputField.vue"
import TextAreaInput from "@/components/input/TextAreaInput.vue"
import TextButton from "@/components/input/TextButton.vue"
import DocumentUnitPreview from "@/components/preview/DocumentUnitPreview.vue"
import SideToggle, { OpeningDirection } from "@/components/SideToggle.vue"
import useQuery from "@/composables/useQueryFromRoute"
import DocumentUnit from "@/domain/documentUnit"
import IconAttachFile from "~icons/ic/baseline-attach-file"
import IconOpenInNewTab from "~icons/ic/outline-open-in-new"
import IconPreview from "~icons/ic/outline-remove-red-eye"
import IconStickyNote from "~icons/ic/outline-sticky-note-2"

interface Props {
  documentUnit: DocumentUnit
}

const props = defineProps<Props>()

const emits = defineEmits<{
  documentUnitUpdatedLocally: [DocumentUnit]
}>()

const updatedDocumentUnit = ref(props.documentUnit)

watch(
  updatedDocumentUnit,
  () => {
    emits(
      "documentUnitUpdatedLocally",
      updatedDocumentUnit.value as DocumentUnit,
    )
  },
  { deep: true },
)

watch(
  () => props.documentUnit,
  (newValue) => {
    updatedDocumentUnit.value = newValue
  },
)

type SelectablePanelContent = "note" | "attachments" | "preview"
const selectedPanelContent = ref<SelectablePanelContent>(
  !props.documentUnit.note && props.documentUnit.hasAttachments
    ? "attachments"
    : "note",
)
const currentAttachmentIndex = ref(0)
const isExpanded = ref(false)

const route = useRoute()
const { pushQueryToRoute } = useQuery()

const hasNote = computed(() => {
  return !!props.documentUnit.note && props.documentUnit.note.length > 0
})

const hasAttachments = computed(() => {
  return (
    !!props.documentUnit.attachments &&
    props.documentUnit.attachments.length > 0
  )
})

const handleOnSelect = (index: number) => {
  currentAttachmentIndex.value = index
}

function selectNotes() {
  selectedPanelContent.value = "note"
}

function selectAttachments(selectedIndex?: number) {
  if (selectedIndex !== undefined) currentAttachmentIndex.value = selectedIndex
  selectedPanelContent.value = "attachments"
}

function selectPreview() {
  selectedPanelContent.value = "preview"
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
      class="sticky top-[4rem] z-20 max-h-fit"
      :is-expanded="isExpanded"
      label="Seitenpanel"
      :opening-direction="OpeningDirection.LEFT"
      tabindex="0"
      @update:is-expanded="togglePanel"
    >
      <FlexContainer class="m-24 ml-16 items-center -space-x-2 px-8">
        <TextButton
          id="note"
          aria-label="Notiz anzeigen"
          button-type="tertiary"
          :class="selectedPanelContent === 'note' ? 'bg-blue-200' : ''"
          :icon="IconStickyNote"
          size="small"
          @click="() => selectNotes()"
        />

        <TextButton
          id="attachments"
          aria-label="Dokumente anzeigen"
          button-type="tertiary"
          :class="selectedPanelContent === 'attachments' ? 'bg-blue-200' : ''"
          :icon="IconAttachFile"
          size="small"
          @click="() => selectAttachments()"
        />

        <TextButton
          id="preview"
          aria-label="Vorschau anzeigen"
          button-type="tertiary"
          :class="selectedPanelContent === 'preview' ? 'bg-blue-200' : ''"
          :icon="IconPreview"
          size="small"
          @click="() => selectPreview()"
        />

        <div class="flex-grow" />

        <FileNavigator
          v-if="selectedPanelContent === 'attachments'"
          :attachments="documentUnit.attachments"
          :current-index="currentAttachmentIndex"
          @select="handleOnSelect"
        ></FileNavigator>
        <router-link
          v-if="selectedPanelContent === 'preview'"
          aria-label="Vorschau in neuem Tab öffnen"
          target="_blank"
          :to="{
            name: 'caselaw-documentUnit-documentNumber-preview',
            params: { documentNumber: documentUnit.documentNumber },
          }"
        >
          <TextButton
            button-type="ghost"
            :icon="IconOpenInNewTab"
            size="small"
          />
        </router-link>
      </FlexContainer>

      <div class="m-24">
        <div v-if="selectedPanelContent === 'note'">
          <InputField id="notesInput" v-slot="{ id }" label="Notiz">
            <TextAreaInput
              :id="id"
              v-model="updatedDocumentUnit.note"
              aria-label="Notiz Eingabefeld"
              autosize
              custom-classes="max-h-[65vh]"
            />
          </InputField>
        </div>
        <div v-if="selectedPanelContent === 'attachments'">
          <AttachmentView
            v-if="
              documentUnit.uuid &&
              documentUnit.attachments &&
              documentUnit.attachments[currentAttachmentIndex]?.s3path
            "
            :document-unit-uuid="documentUnit.uuid"
            :s3-path="documentUnit.attachments[currentAttachmentIndex].s3path"
          />
          <div v-else class="ds-label-01-reg">
            Wenn Sie eine Datei hochladen, können Sie die Datei hier sehen.
          </div>
        </div>
        <FlexContainer
          v-if="selectedPanelContent === 'preview'"
          class="max-h-[70vh] overflow-auto"
        >
          <DocumentUnitPreview :document-unit="documentUnit" />
        </FlexContainer>
      </div>
    </SideToggle>
  </FlexItem>
</template>
