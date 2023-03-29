<script lang="ts" setup>
import dayjs from "dayjs"
import { computed, onMounted, ref } from "vue"
import fileService from "@/services/fileService"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextEditor from "@/shared/components/input/TextEditor.vue"
import PopupModal from "@/shared/components/PopupModal.vue"
import PropertyInfo from "@/shared/components/PropertyInfo.vue"

const props = defineProps<{
  s3Path: string
  fileName?: string
  fileType?: string
  uploadTimeStamp?: string
}>()

defineEmits<{ (e: "deleteFile"): void }>()

const showModal = ref(false)

const popupModalText = computed(
  () => `Möchten Sie die ausgewählte Datei ${props.fileName} wirklich löschen?`
)

const fileInfos = computed(() => [
  {
    label: "Hochgeladen am",
    value: dayjs(props.uploadTimeStamp).format("DD.MM.YYYY"),
  },
  {
    label: "Format",
    value: props.fileType,
  },
  {
    label: "Von",
    value: "USER NAME",
  },
  {
    label: "Dateiname",
    value: props.fileName,
  },
])

const toggleModal = () => {
  showModal.value = !showModal.value
  if (showModal.value) {
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

const fileAsHtml = ref("Dokument wird geladen.")
onMounted(async () => {
  const fileResponse = await fileService.getDocxFileAsHtml(props.s3Path)
  if (fileResponse.error) {
    console.error(fileResponse.error)
  } else {
    fileAsHtml.value = fileResponse.data
  }
})
</script>

<template>
  <div class="flex flex-col gap-32 grow">
    <div class="bg-white flex flex-col items-start px-[2rem] py-[1.5rem]">
      <div class="flex pb-[1rem] w-full">
        <div v-for="entry in fileInfos" :key="entry.label" class="grow">
          <PropertyInfo
            direction="column"
            :label="entry.label"
            :value="entry.value"
          ></PropertyInfo>
        </div>
      </div>

      <TextButton icon="delete" label="Datei löschen" @click="toggleModal" />
    </div>

    <TextEditor class="bg-white grow" field-size="max" :value="fileAsHtml" />

    <PopupModal
      v-if="showModal"
      aria-label="Dokument löschen"
      cancel-button-type="ghost"
      confirm-button-type="secondary"
      confirm-text="Löschen"
      :content-text="popupModalText"
      @close-modal="toggleModal"
      @confirm-action="toggleModal(), $emit('deleteFile')"
    />
  </div>
</template>
