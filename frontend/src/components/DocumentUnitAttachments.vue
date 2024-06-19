<script lang="ts" setup>
import { computed, onMounted, Ref, ref } from "vue"
import { useRoute } from "vue-router"
import AttachmentList from "@/components/AttachmentList.vue"
import FileUpload from "@/components/FileUpload.vue"
import FlexItem from "@/components/FlexItem.vue"
import InfoModal from "@/components/InfoModal.vue"
import PopupModal from "@/components/PopupModal.vue"
import TitleElement from "@/components/TitleElement.vue"
import useQuery from "@/composables/useQueryFromRoute"
import Attachment from "@/domain/attachment"
import DocumentUnit from "@/domain/documentUnit"
import attachmentService from "@/services/attachmentService"
import useSessionStore from "@/stores/sessionStore"

const props = defineProps<{
  documentUnit: DocumentUnit
  showNavigationPanel?: boolean
}>()
const emit = defineEmits<{
  updateDocumentUnit: [void]
}>()

const { pushQueryToRoute } = useQuery<"showAttachmentPanel">()
const route = useRoute()

const showExtraContentPanelRef: Ref<boolean> = ref(false)

const errors = ref<string[]>([])
const isLoading = ref(false)
const acceptedFileFormats = [".docx"]
const selectedAttachmentIndex: Ref<number> = ref(0)
const { featureToggles } = useSessionStore()
const notesFeatureToggle = computed(
  () => featureToggles["neuris.note"] ?? false,
)

const showDeleteModal = ref(false)
const deleteModalHeaderText = "Anhang löschen"

const errorTitle = computed(() => {
  if (errors.value.length === 1) {
    return "1 Datei konnte nicht hochgeladen werden."
  } else if (errors.value.length > 1) {
    return `${errors.value.length} Dateien konnten nicht hochgeladen werden.`
  } else return ""
})

const getAttachments = (): Attachment[] => {
  return props.documentUnit.attachments
}

const getAttachment = (index: number): Attachment => {
  return getAttachments()[index]
}

const handleDeleteAttachment = async (index: number) => {
  const fileToDelete = getAttachment(index)
  if (fileToDelete.s3path == undefined) {
    console.error("file path is undefined", index)
    return
  }

  if (
    (
      await attachmentService.delete(
        props.documentUnit.uuid,
        fileToDelete.s3path,
      )
    ).status < 300
  ) {
    if (getAttachments().length > 1) {
      selectedAttachmentIndex.value = index == 0 ? 0 : index - 1
    } else {
      toggleExtraContentPanel(false)
    }
    emit("updateDocumentUnit")
  }
}

const handleOnSelect = (index: number) => {
  if (index >= 0 && index < getAttachments().length) {
    if (selectedAttachmentIndex.value == index) {
      toggleExtraContentPanel()
    } else {
      selectedAttachmentIndex.value = index
      toggleExtraContentPanel(true)
    }
  }
}

const handleOnDelete = (index: number) => {
  errors.value = []
  selectedAttachmentIndex.value = index
  toggleDeleteModal()
}

const deleteFile = (index: number) => {
  handleDeleteAttachment(index)
  toggleDeleteModal()
}

async function upload(files: FileList) {
  errors.value = []
  let anySuccessful = false
  try {
    for (const file of Array.from(files)) {
      isLoading.value = true
      const response = await attachmentService.upload(
        props.documentUnit.uuid,
        file,
      )
      if (response.status === 200 && response.data) {
        anySuccessful = true
      } else if (response.error?.description) {
        errors.value.push(
          file.name +
            " " +
            response.error?.title +
            " " +
            response.error?.description,
        )
      }
    }
  } finally {
    isLoading.value = false
    emit("updateDocumentUnit")
    if (!showExtraContentPanelRef.value && anySuccessful) {
      toggleExtraContentPanel(true)
    }
  }
}

const toggleExtraContentPanel = (state?: boolean) => {
  showExtraContentPanelRef.value = state || !showExtraContentPanelRef.value
  pushQueryToRoute({
    showAttachmentPanel: showExtraContentPanelRef.value.toString(),
  })
}

function toggleDeleteModal() {
  showDeleteModal.value = !showDeleteModal.value
  if (showDeleteModal.value) {
    const scrollLeft = document.documentElement.scrollLeft
    const scrollTop = document.documentElement.scrollTop
    window.onscroll = () => {
      window.scrollTo(scrollLeft, scrollTop)
    }
  } else {
    window.onscroll = () => {
      return
    }
  }
}

onMounted(async () => {
  if (route.query.showAttachmentPanel) {
    showExtraContentPanelRef.value = route.query.showAttachmentPanel === "true"
  } else if (notesFeatureToggle.value) {
    showExtraContentPanelRef.value =
      !!props.documentUnit.note || props.documentUnit.hasAttachments
  }
})
</script>

<template>
  <FlexItem class="w-full flex-1 grow space-y-20 p-24">
    <PopupModal
      v-if="
        showDeleteModal &&
        props.documentUnit.attachments[selectedAttachmentIndex] !== undefined &&
        props.documentUnit.attachments[selectedAttachmentIndex] !== null &&
        props.documentUnit.attachments[selectedAttachmentIndex].name != null
      "
      :aria-label="deleteModalHeaderText"
      cancel-button-type="tertiary"
      confirm-button-type="destructive"
      confirm-text="Löschen"
      :content-text="`Möchten Sie den Anhang ${getAttachment(selectedAttachmentIndex).name} wirklich dauerhaft löschen?`"
      :header-text="deleteModalHeaderText"
      @close-modal="toggleDeleteModal"
      @confirm-action="deleteFile(selectedAttachmentIndex)"
    />
    <TitleElement class="mb-0">Dokumente</TitleElement>
    <AttachmentList
      v-if="props.documentUnit.hasAttachments"
      id="file-table"
      :files="getAttachments()"
      @delete="handleOnDelete"
      @select="handleOnSelect"
    />
    <InfoModal
      v-if="errors.length > 0 && isLoading === false"
      class="mt-8"
      :description="errors"
      :title="errorTitle"
    />
    <div class="flex-grow">
      <div class="flex h-full flex-col items-start">
        <FileUpload
          :accept="acceptedFileFormats.toString()"
          :is-loading="isLoading"
          @files-selected="(files) => upload(files)"
        />
      </div>
    </div>
    <div class="flex flex-row justify-between">
      <div class="ds-label-02-reg text-gray-900">
        Zulässige Dateiformate:
        {{ acceptedFileFormats.toString().replace(/\./g, " ") }}
      </div>
      <div class="ds-label-02-reg text-gray-900">
        Maximale Dateigröße: 20 MB
      </div>
    </div>
  </FlexItem>
</template>
