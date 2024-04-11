<script lang="ts" setup>
import { onMounted, Ref, ref, watch } from "vue"
import { useRoute, useRouter } from "vue-router"
import FilePreview from "@/components/AttachementView.vue"
import DocumentUnitWrapper from "@/components/DocumentUnitWrapper.vue"
import DocumentUnitFileList from "@/components/FileList.vue"
import FileNavigator from "@/components/FileNavigator.vue"
import FileUpload from "@/components/FileUpload.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import FlexItem from "@/components/FlexItem.vue"
import PopupModal from "@/components/PopupModal.vue"
import SideToggle, { OpeningDirection } from "@/components/SideToggle.vue"
import { useToggleStateInRouteQuery } from "@/composables/useToggleStateInRouteQuery"
import Attachment from "@/domain/attachment"
import DocumentUnit from "@/domain/documentUnit"
import { Docx2HTML } from "@/domain/docx2html"
import documentUnitService from "@/services/documentUnitService"
import fileService from "@/services/fileService"
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
const fileAsHTML = ref<Docx2HTML>()

const showDeleteModal = ref(false)
const deleteModalHeaderText = "Anhang löschen"

const getAttachments = (): Attachment[] => {
  return props.documentUnit.attachments
}

const getAttachment = (index: number): Attachment => {
  return getAttachments()[index]
}

watch(
  showDocPanel,
  async () => {
    if (showDocPanel.value) {
      await getOriginalDocumentUnit()
    }
  },
  { immediate: true },
)

async function handleDeleteFile(index: number) {
  console.log(index)
  const fileToDelete = getAttachment(index)
  if (fileToDelete.s3path == undefined) return
  //TODO: Error handling

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

console.log(props.documentUnit)

const handleOnSelect = (index: number) => {
  selectedAttachmentIndex.value = index
  togglePanel()
}

const handleOnDelete = (index: number) => {
  selectedAttachmentIndex.value = index
  toggleDeleteModal()
}

const deleteFile = (index: number) => {
  handleDeleteFile(index)
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

async function getOriginalDocumentUnit() {
  if (fileAsHTML.value?.html && fileAsHTML.value.html.length > 0) return
  if (getAttachment(selectedAttachmentIndex.value).s3path) {
    const htmlResponse = await fileService.getDocxFileAsHtml(
      props.documentUnit.uuid,
    )
    if (htmlResponse.error === undefined) fileAsHTML.value = htmlResponse.data
  }
}

onMounted(async () => {
  isLoading.value = true
  try {
    const fileResponse = await fileService.getDocxFileAsHtml(
      props.documentUnit.uuid,
    )

    if (fileResponse.error) {
      console.error(JSON.stringify(fileResponse.error))
    } else {
      html.value = fileResponse.data.html
    }
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <DocumentUnitWrapper :document-unit="documentUnit">
    <template #default="{ classes }">
      <FlexContainer class="w-full flex-row">
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
        <FlexItem class="space-y-20" :class="classes">
          <h1 class="ds-heading-02-reg mb-[1rem]">Dokumente</h1>
          <DocumentUnitFileList
            v-if="props.documentUnit.hasAttachments"
            id="file-table"
            :files="getAttachments()"
            @delete="handleOnDelete"
            @select="handleOnSelect"
          ></DocumentUnitFileList>
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
        <FlexItem v-show="props.documentUnit.hasAttachments">
          <div
            class="flex h-full flex-col border-l-1 border-solid border-gray-400 bg-white"
            :class="{ full: showDocPanel }"
          >
            <SideToggle
              v-if="props.documentUnit.hasAttachments"
              class="sticky top-[8rem] z-20"
              :is-expanded="showDocPanel"
              label="Originaldokument"
              :opening-direction="OpeningDirection.LEFT"
              @update:is-expanded="togglePanel"
            >
              <FileNavigator
                :current-index="selectedAttachmentIndex"
                :files="getAttachments()"
                @select="handleOnSelect"
              ></FileNavigator>
              <FilePreview
                v-if="props.documentUnit.hasAttachments && fileAsHTML?.html"
                id="odoc-panel-element"
                v-model:open="showDocPanel"
                class="bg-white"
                :class="classes"
                :content="fileAsHTML.html"
              />
            </SideToggle>
          </div>
        </FlexItem>
      </FlexContainer>
    </template>
  </DocumentUnitWrapper>
</template>
