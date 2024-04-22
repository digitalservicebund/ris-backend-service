<script lang="ts" setup>
import { computed, Ref, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import AttachmentList from "@/components/AttachmentList.vue"
import AttachmentViewSidePanel from "@/components/AttachmentViewSidePanel.vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import FileUpload from "@/components/FileUpload.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import InfoModal from "@/components/InfoModal.vue"
import PopupModal from "@/components/PopupModal.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useToggleStateInRouteQuery } from "@/composables/useToggleStateInRouteQuery"
import Attachment from "@/domain/attachment"
import DocumentUnit from "@/domain/documentUnit"
import attachmentService from "@/services/attachmentService"
import documentUnitService from "@/services/documentUnitService"

const props = defineProps<{ documentUnit: DocumentUnit }>()
const emit = defineEmits<{
  updateDocumentUnit: [updatedDocumentUnit: DocumentUnit]
}>()

const router = useRouter()
const route = useRoute()
const showDocPanel = useToggleStateInRouteQuery(
  "showDocPanel",
  route,
  router.replace,
  false,
)
const errors = ref<string[]>([])
const html = ref<string>()
const isLoading = ref(false)
const acceptedFileFormats = [".docx"]
const selectedAttachmentIndex: Ref<number> = ref(0)

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
    const updateResponse = await documentUnitService.getByDocumentNumber(
      props.documentUnit.documentNumber as string,
    )

    if (updateResponse.error) {
      console.error(updateResponse.error)
    } else {
      emit("updateDocumentUnit", updateResponse.data)
      html.value = undefined
    }
  }
}

const handleOnSelect = (index: number) => {
  if (selectedAttachmentIndex.value == index) {
    showDocPanel.value = !showDocPanel.value
  } else {
    selectedAttachmentIndex.value = index
    showDocPanel.value = true
  }
}

const handleOnDelete = (index: number) => {
  selectedAttachmentIndex.value = index
  toggleDeleteModal()
}

const deleteFile = (index: number) => {
  handleDeleteAttachment(index)
  toggleDeleteModal()
  if (showDocPanel) {
    togglePanel()
  }
}

async function upload(files: FileList) {
  errors.value = []
  try {
    for (const file of Array.from(files)) {
      isLoading.value = true
      const response = await attachmentService.upload(
        props.documentUnit.uuid,
        file,
      )
      if (response.status === 200 && response.data) {
        html.value = response.data.html
      } else {
        if (response.error?.description) {
          errors.value.push(response.error?.description)
        }
      }
    }
  } finally {
    isLoading.value = false
    emit("updateDocumentUnit", props.documentUnit)
  }
}

const togglePanel = () => {
  showDocPanel.value = !showDocPanel.value
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
</script>

<template>
  <DocumentUnitWrapper :document-unit="documentUnit">
    <template #default="{ classes }">
      <FlexContainer class="w-full">
        <PopupModal
          v-if="
            showDeleteModal &&
            props.documentUnit.attachments[selectedAttachmentIndex] !==
              undefined &&
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
        <FlexItem class="flex-1 space-y-20" :class="classes">
          <TitleElement>Dokumente</TitleElement>
          <InfoModal
            v-if="errors.length > 0"
            class="mt-8"
            :description="errors"
            :title="errorTitle"
          />
          <AttachmentList
            v-if="props.documentUnit.hasAttachments"
            id="file-table"
            :files="getAttachments()"
            @delete="handleOnDelete"
            @select="handleOnSelect"
          />
          <div>
            <div class="flex flex-col items-start">
              <FileUpload
                :accept="acceptedFileFormats.toString()"
                :is-loading="isLoading"
                @files-selected="(files) => upload(files)"
              />
            </div>
          </div>
          <div class="ds-label-02-reg text-gray-900">
            Zulässige Dateiformate:
            {{ acceptedFileFormats.toString().replace(/\./g, " ") }}
          </div>
        </FlexItem>
        <AttachmentViewSidePanel
          v-if="props.documentUnit.attachments"
          :attachments="documentUnit.attachments"
          :current-index="selectedAttachmentIndex"
          :document-unit-uuid="props.documentUnit.uuid"
          :is-expanded="showDocPanel"
          @select="handleOnSelect"
          @update="togglePanel"
        />
      </FlexContainer>
    </template>
  </DocumentUnitWrapper>
</template>
