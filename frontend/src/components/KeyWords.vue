<script lang="ts" setup>
import { ref, computed } from "vue"
import { ResponseError } from "@/services/httpClient"
import KeywordsChipsInput from "@/shared/components/input/KeywordsChipsInput.vue"

const props = defineProps<{
  modelValue: string[] | undefined
}>()

const emit = defineEmits<{ "update:modelValue": [value?: string[]] }>()
const errorMessage = ref<ResponseError>()

const keywords = computed({
  get: () => {
    return props.modelValue
  },
  set: (value) => {
    if (value) emit("update:modelValue", value)
  },
})
</script>

<template>
  <div class="bg-white p-16">
    <h2 class="ds-label-02-bold mb-[1rem]">Schlagwörter</h2>
    <div class="flex flex-row">
      <div class="flex-1">
        <KeywordsChipsInput
          id="keywords"
          v-model="keywords"
          aria-label="Schlagwörter"
          :error="errorMessage"
        ></KeywordsChipsInput>
      </div>
    </div>
  </div>
</template>
