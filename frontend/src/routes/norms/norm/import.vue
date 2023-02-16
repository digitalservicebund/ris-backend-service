<script lang="ts" setup>
import { ref } from "vue"
import { useRouter } from "vue-router"
import FileUpload from "@/components/FileUpload.vue"
import { ResponseError } from "@/services/httpClient"
import { importNorm } from "@/services/normsService"

const error = ref<ResponseError>()
const isUploading = ref(false)

const router = useRouter()

async function upload(file: File) {
  isUploading.value = true

  try {
    const response = await importNorm(file)
    if (response.status === 201 && response.data) {
      await router.replace({
        name: "norms-norm-:normGuid",
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
  <div class="bg-gray-100 flex justify-center">
    <div>
      <div class="pb-[5rem] pt-[4rem]">
        <h1 class="heading-02-regular">Neue Dokumentationseinheit erstellen</h1>
        <p>
          Importieren Sie eine Zip-Datei, um die Dokumentationseinheit zu
          erstellen.
        </p>
      </div>
      <FileUpload
        :error="error"
        :is-loading="isUploading"
        @file-selected="(file) => upload(file)"
      />
    </div>
  </div>
</template>
