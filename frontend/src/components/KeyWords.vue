<script lang="ts" setup>
import { ref, watch } from "vue"
import { ResponseError } from "@/services/httpClient"
import KeywordsService from "@/services/keywordsService"
import KeywordsChipsInput from "@/shared/components/input/KeywordsChipsInput.vue"

const props = defineProps<{
  documentUnitUuid: string
}>()

const keywords = ref<string[]>([])
const errorMessage = ref<ResponseError>()

const addKeyword = async (keyword: string | undefined) => {
  if (keyword !== undefined) {
    const response = await KeywordsService.addKeyword(
      props.documentUnitUuid,
      keyword
    )
    if (response.error) {
      errorMessage.value = response.error
      return
    }
    if (response.data) {
      keywords.value = response.data
    }
  }
}

const deleteKeyword = async (keyword: string | undefined) => {
  if (keyword !== undefined) {
    const response = await KeywordsService.deleteKeyword(
      props.documentUnitUuid,
      keyword
    )
    if (response.error) {
      errorMessage.value = response.error
      return
    }
    if (response.data) {
      keywords.value = response.data
    }
  }
}

//Todo: get keywords via documentunit
watch(
  props,
  async () => {
    const response = await KeywordsService.getKeywords(props.documentUnitUuid)
    if (response.error) {
      errorMessage.value = response.error
      return
    }
    if (response.data) {
      keywords.value = response.data
    }
  },
  {
    immediate: true,
  }
)
</script>

<template>
  <div class="bg-white mb-[2rem] p-16">
    <h2 class="label-02-bold mb-[1rem]">Schlagwörter</h2>
    <div class="flex flex-row">
      <div class="flex-1">
        <KeywordsChipsInput
          id="keywords"
          aria-label="Schlagwörter"
          :error="errorMessage"
          :model-value="keywords"
          @add-chip="addKeyword"
          @delete-chip="deleteKeyword"
        ></KeywordsChipsInput>
      </div>
    </div>
  </div>
</template>
