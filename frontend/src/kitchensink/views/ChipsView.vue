<script lang="ts" setup>
import { ref } from "vue"
import ChipsInput from "@/components/ChipsInput.vue"
import ChipsInputBottom from "@/components/ChipsInputBottom.vue"
import type { ChipsInputModelType } from "@/domain/types"

const chipsValue1 = ref<ChipsInputModelType>(["one", "two"])
const chipsValue2 = ref<ChipsInputModelType>(["one", "two"])
</script>

<template>
  <div class="flex flex-col gap-y-20 h-auto w-1/2">
    <h1 class="font-bold text-24">Chips Input Variante 1</h1>
    <ChipsInput
      id="ChipsInput"
      aria-label="chips input 1"
      :model-value="chipsValue1"
    ></ChipsInput>
    <h1 class="font-bold text-24">Chips Input Variante 2</h1>
    <ChipsInputBottom
      id="ChipsInput"
      aria-label="chips input 2"
      :model-value="chipsValue2"
    ></ChipsInputBottom>
  </div>
</template>
