<script lang="ts" setup>
import { Ref, ref } from "vue"
import AttachmentList from "@/components/AttachmentList.vue"
import AttachmentViewSidePanel from "@/components/AttachmentViewSidePanel.vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import FileUpload from "@/components/FileUpload.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import PopupModal from "@/components/PopupModal.vue"
import TitleElement from "@/components/TitleElement.vue"
import useQuery from "@/composables/useQueryFromRoute"
import Attachment from "@/domain/attachment"
import DocumentUnit from "@/domain/documentUnit"
import fileService from "@/services/attachmentService"
import documentUnitService from "@/services/documentUnitService"
import { ResponseError } from "@/services/httpClient"

const props = defineProps<{
  documentUnit: DocumentUnit
  showAttachmentPanel?: boolean
}>()
const emit = defineEmits<{
  updateDocumentUnit: [updatedDocumentUnit: DocumentUnit]
}>()

const { pushQueryToRoute } = useQuery<"showAttachmentPanel">()

console.log(props.showAttachmentPanel)
const showAttachmentPanelRef: Ref<boolean> = ref(
  props.showAttachmentPanel ? props.showAttachmentPanel : false,
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
    toggleAttachmentPanel()
  } else {
    selectedAttachmentIndex.value = index
    toggleAttachmentPanel(true)
  }
}

const handleOnDelete = (index: number) => {
  selectedAttachmentIndex.value = index
  toggleDeleteModal()
}

const deleteFile = (index: number) => {
  handleDeleteAttachment(index)
  toggleDeleteModal()
  if (showAttachmentPanelRef.value) {
    toggleAttachmentPanel()
  }
}

async function upload(files: FileList) {
  try {
    for (const file of Array.from(files)) {
      isLoading.value = true
      const response = await fileService.upload(props.documentUnit.uuid, file)
      if (response.status === 200 && response.data) {
        html.value = response.data.html
      } else {
        error.value = response.error
      }
    }
  } finally {
    isLoading.value = false
    emit("updateDocumentUnit", props.documentUnit)
  }
}

const toggleAttachmentPanel = (state?: boolean) => {
  showAttachmentPanelRef.value = state || !showAttachmentPanelRef.value
  pushQueryToRoute({
    showAttachmentPanel: showAttachmentPanelRef.value.toString(),
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
          />
          <div>
            <div class="flex flex-col items-start">
              <FileUpload
                :accept="acceptedFileFormats.toString()"
                :error="error"
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
          :is-expanded="showAttachmentPanelRef"
          @select="handleOnSelect"
          @update="toggleAttachmentPanel"
        ></AttachmentViewSidePanel>
      </FlexContainer>
    </template>
  </DocumentUnitWrapper>
</template>
