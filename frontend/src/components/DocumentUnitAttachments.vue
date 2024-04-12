<script lang="ts" setup>
import { Ref, ref } from "vue"
import { useRoute, useRouter } from "vue-router"
import AttachmentList from "@/components/AttachmentList.vue"
import AttachmentViewSidePanel from "@/components/AttachmentViewSidePanel.vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import FileUpload from "@/components/FileUpload.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import PopupModal from "@/components/PopupModal.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useToggleStateInRouteQuery } from "@/composables/useToggleStateInRouteQuery"
import Attachment from "@/domain/attachment"
import DocumentUnit from "@/domain/documentUnit"
import fileService from "@/services/attachmentService"
import documentUnitService from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"

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
const error = ref<ResponseError>()
const html = ref<string>()
const isLoading = ref(false)
const acceptedFileFormats = [".docx"]
const selectedAttachmentIndex: Ref<number> = ref(0)

const showDeleteModal = ref(false)
const deleteModalHeaderText = "Anhang löschen"

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
    (await fileService.delete(props.documentUnit.uuid, fileToDelete.s3path))
      .status < 300
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

async function upload(file: File) {
  isLoading.value = false

  try {
    const response = await fileService.upload(props.documentUnit.uuid, file)
    if (response.status === 200 && response.data) {
      html.value = response.data.html
    } else {
      error.value = response.error
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
          <AttachmentList
            v-if="props.documentUnit.hasAttachments"
            id="file-table"
            :files="getAttachments()"
            @delete="handleOnDelete"
            @select="handleOnSelect"
          ></AttachmentList>
          <div>
            <div class="flex flex-col items-start">
              <FileUpload
                :accept="acceptedFileFormats.toString()"
                :error="error"
                :is-loading="false"
                @file-selected="(file) => upload(file)"
              />
            </div>
          </div>
          <div>
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
        ></AttachmentViewSidePanel>
      </FlexContainer>
    </template>
  </DocumentUnitWrapper>
</template>
