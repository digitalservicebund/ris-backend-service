<script lang="ts" setup>
import dayjs from "dayjs"
import { computed, ref } from "vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextEditor from "@/shared/components/input/TextEditor.vue"
import LoadingSpinner from "@/shared/components/LoadingSpinner.vue"
import PopupModal from "@/shared/components/PopupModal.vue"
import PropertyInfo from "@/shared/components/PropertyInfo.vue"
import IconDelete from "~icons/ic/outline-delete"

const props = defineProps<{
  uuid: string
  fileName?: string
  fileType?: string
  uploadTimeStamp?: string
  html?: string
}>()

defineEmits<{
  deleteFile: []
}>()

const showModal = ref(false)

const popupModalText = computed(
  () => `Möchten Sie die ausgewählte Datei ${props.fileName} wirklich löschen?`,
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
</script>

<template>
  <div class="flex grow flex-col gap-32">
    <div class="flex flex-col items-start bg-white px-[2rem] py-[1.5rem]">
      <div class="flex w-full pb-[1rem]">
        <div v-for="entry in fileInfos" :key="entry.label" class="grow">
          <PropertyInfo
            direction="column"
            :label="entry.label"
            :value="entry.value"
          ></PropertyInfo>
        </div>
      </div>

      <TextButton
        :icon="IconDelete"
        label="Datei löschen"
        @click="toggleModal"
      />
    </div>
    <div v-if="!html" class="text-center"><LoadingSpinner /></div>

    <TextEditor v-else class="grow bg-white" :value="html" />

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
