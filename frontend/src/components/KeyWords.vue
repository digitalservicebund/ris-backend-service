<script lang="ts" setup>
import { ref, watch } from "vue"
import ChipsInput from "@/components/ChipsInput.vue"
import KeywordsService from "@/services/keywordsService"

const props = defineProps<{
  documentUnitUuid: string
}>()

const keywords = ref<string[]>([])

const addKeyword = async (keyword: string | undefined) => {
  if (keyword !== undefined) {
    const response = await KeywordsService.addKeyword(
      props.documentUnitUuid,
      keyword
    )
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
  <div class="bg-gray-100 flex flex-col p-20">
    <h1 class="heading-03-regular mb-[1rem]">Schlagwörter</h1>
    <div class="flex flex-row">
      <div class="flex-1">
        <ChipsInput
          id="keywords"
          aria-label="Schlagwörter"
          as-bottom-list
          :model-value="keywords"
          @add-chip="addKeyword"
          @delete-chip="deleteKeyword"
        ></ChipsInput>
      </div>
    </div>
  </div>
</template>
