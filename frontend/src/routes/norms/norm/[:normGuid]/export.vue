<script lang="ts" setup>
import dayjs from "dayjs"
import timezone from "dayjs/plugin/timezone"
import utc from "dayjs/plugin/utc"
import { storeToRefs } from "pinia"
import { computed, ref, reactive } from "vue"
import { getFileUrl, triggerFileGeneration } from "@/services/normsService"
import InfoModal from "@/shared/components/InfoModal.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import { useLoadedNormStore } from "@/stores/loadedNorm"

dayjs.extend(utc)
dayjs.extend(timezone)

const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)
const isLoading = ref(false)
const hasError = ref(false)
const error = reactive({ title: "", description: "" })

const fileReference = computed(() => {
  const files = loadedNorm.value?.files ?? []
  return files.length > 0 ? files[files.length - 1] : undefined
})

const fileName = computed(() => fileReference.value?.name)

const downloadUrl = computed(() =>
  loadedNorm.value && fileReference.value
    ? getFileUrl(loadedNorm.value.guid, fileReference.value.hash)
    : undefined
)
const downloadIsPossible = computed(() => downloadUrl.value != undefined)

const formatDateTime = function (date: string): string {
  return dayjs(date).tz("Europe/Berlin").format("DD.MM.YYYY HH:mm")
}

async function getFileLink() {
  isLoading.value = true
  const guid = loadedNorm.value?.guid || ""
  try {
    const response = await triggerFileGeneration(guid)
    await new Promise((res) => setTimeout(res, 300))
    if (response.status === 200 && response.data) {
      await store.load(guid)
    } else {
      Object.assign(error, response.error)
      hasError.value = true
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="max-w-screen-lg">
    <div>
      <h1 class="heading-02-regular mb-[1rem]">Export</h1>
      <p>Exportieren Sie die Dokumentationseinheit zur Abgabe an die jDV.</p>
    </div>
    <div class="bg-white mt-[2rem] p-[2rem]">
      <div class="mb-32">
        <h2 class="heading-03-regular">Zip-Datei steht zum Download bereit.</h2>
        <p v-if="fileReference" class="body-01-reg">
          zuletzt generiert am
          {{ formatDateTime(fileReference.createdAt) }}
        </p>
      </div>

      <TextButton
        :disabled="!downloadIsPossible || isLoading"
        :download="fileName"
        :href="downloadUrl"
        label="Zip Datei speichern"
        target="_blank"
      />

      <div class="mt-[1rem]">
        <a class="link-01-bold underline" href="#" @click.prevent="getFileLink"
          >Neue Zip-Datei generieren
          <span
            v-if="isLoading"
            class="align-middle material-icons ml-[0.5rem] text-20 text-blue-800"
          >
            refresh
          </span>
        </a>
      </div>
    </div>

    <InfoModal v-if="hasError" v-bind="error" class="mt-8" />
  </div>
</template>
