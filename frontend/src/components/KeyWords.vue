<script lang="ts" setup>
import { ref, computed } from "vue"
import KeywordsChipsInput from "@/components/KeywordsChipsInput.vue"
import { ResponseError } from "@/services/httpClient"

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
  <div class="flex flex-col gap-24 bg-white p-32">
    <h2 class="ds-heading-03-bold">Inhaltliche Erschließung</h2>
    <div aria-label="Vorgehende Entscheidung">
      <h2 class="ds-heading-03-reg mb-24">Schlagwörter</h2>
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
  </div>
</template>
