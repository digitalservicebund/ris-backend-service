<script lang="ts" setup>
import { onMounted, ref, watch } from "vue"
import EURLexList from "@/components/eurlex/EURLexList.vue"
import EURLexSearchForm from "@/components/eurlex/EURLexSearchForm.vue"
import InfoModal from "@/components/InfoModal.vue"
import { Page } from "@/components/Pagination.vue"
import EURLexResult from "@/domain/eurlex"
import service from "@/services/eurlexService"
import { ResponseError } from "@/services/httpClient"

const searchResults = ref<Page<EURLexResult>>()
const serviceError = ref<ResponseError | undefined>(undefined)

async function updatePage(
  pageNumber: number,
  fileNumber?: string,
  celex?: string,
  court?: string,
  startDate?: string,
  endDate?: string,
) {
  const response = await service.get(
    pageNumber,
    fileNumber,
    celex,
    court,
    startDate,
    endDate,
  )
  if (response.error) {
    serviceError.value = response.error
  } else {
    serviceError.value = undefined
    searchResults.value = response.data
  }
}

function handleServiceError(error?: ResponseError) {
  if (!error) {
    serviceError.value = undefined
    return
  }

  serviceError.value = error
}

watch(serviceError, () => {
  const defaultDescription = "Laden Sie die Seite bitte neu."
  if (serviceError.value) {
    if (!serviceError.value?.description) {
      serviceError.value.description = defaultDescription
    } else if (serviceError.value?.description != defaultDescription) {
      serviceError.value.description = [
        ...serviceError.value.description,
        defaultDescription,
      ]
    }
  }
})

onMounted(async () => {
  await updatePage(0)
})
</script>

<template>
  <EURLexSearchForm
    class="pyb-24 mb-16 flex flex-col bg-blue-200"
    @handle-service-error="handleServiceError"
    @update-page="updatePage"
  ></EURLexSearchForm>
  <InfoModal
    v-if="serviceError"
    class="my-16"
    :description="serviceError.description"
    :title="serviceError.title"
  ></InfoModal>
  <EURLexList
    :page-entries="searchResults"
    @assign="updatePage"
    @handle-service-error="handleServiceError"
    @update-page="updatePage"
  />
</template>
