<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed } from "vue"
import TextButton from "@/components/TextButton.vue"
import { getFileUrl } from "@/services/normsService"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)

const fileReference = computed(() => {
  const files = loadedNorm.value?.files ?? []
  return files.length > 0 ? files[0] : undefined
})

const fileName = computed(() => fileReference.value?.name)

const downloadUrl = computed(() =>
  loadedNorm.value && fileReference.value
    ? getFileUrl(loadedNorm.value.guid, fileReference.value.hash)
    : undefined
)

const downloadIsPossible = computed(() => downloadUrl.value != undefined)
</script>

<template>
  <div class="max-w-screen-lg">
    <div>
      <h1 class="heading-02-regular mb-[1rem]">Export</h1>
      <p>Exportieren Sie die Dokumentationseinheit zur Abgabe an die jDV.</p>
    </div>
    <div class="bg-white mt-[2rem] p-[2rem]">
      <h2 class="heading-03-regular mb-32">
        Zip-Datei steht zum Download bereit.
      </h2>

      <TextButton
        :disabled="!downloadIsPossible"
        :download="fileName"
        :href="downloadUrl"
        label="Zip Datei speichern"
        target="_blank"
      />
    </div>
  </div>
</template>
