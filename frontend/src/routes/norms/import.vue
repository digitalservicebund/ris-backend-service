<script lang="ts" setup>
import { ref } from "vue"
import { useRouter } from "vue-router"
import { ResponseError } from "@/services/httpClient"
import { importNorm } from "@/services/norms"
import FileUpload from "@/shared/components/FileUpload.vue"

const error = ref<ResponseError>()
const isUploading = ref(false)
const router = useRouter()
const accept = "application/zip"

async function upload(file: File) {
  isUploading.value = true

  try {
    const response = await importNorm(file)
    if (response.status === 201 && response.data) {
      await router.replace({
        name: "norms-norm-normGuid-content",
        params: { normGuid: response.data },
      })
    } else {
      error.value = response.error
    }
  } finally {
    isUploading.value = false
  }
}
</script>

<template>
  <div class="flex justify-center bg-gray-100">
    <div>
      <div class="pb-[5rem] pt-[4rem]">
        <h1 class="ds-heading-02-reg">Neue Dokumentationseinheit erstellen</h1>
        <p>
          Importieren Sie eine Zip-Datei, um die Dokumentationseinheit zu
          erstellen.
        </p>
      </div>
      <FileUpload
        :accept="accept"
        :error="error"
        :is-loading="isUploading"
        @file-selected="(file) => upload(file)"
      />
    </div>
  </div>
</template>
