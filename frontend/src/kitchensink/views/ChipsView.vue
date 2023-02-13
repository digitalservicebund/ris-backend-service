<script lang="ts" setup>
import { ref } from "vue"
import ChipsInput from "@/components/ChipsInput.vue"
import ChipsInputList from "@/components/ChipsInputList.vue"
import type { ChipsInputModelType } from "@/domain/types"

const chipsValue1 = ref<ChipsInputModelType>(["one", "two"])
const chipsValue2 = ref<ChipsInputModelType>(["one", "two"])
const chipsValue3 = ref<ChipsInputModelType>(["one", "two"])
</script>

<template>
  <div class="flex flex-col gap-y-20 h-auto w-1/2">
    <h1 class="font-bold text-24">Variante 1</h1>
    <ChipsInput
      id="ChipsInput"
      aria-label="chips input"
      :model-value="chipsValue1"
    ></ChipsInput>

    <h1 class="font-bold text-24">Variante 2</h1>
    <ChipsInputList
      id="ChipsInput"
      aria-label="chips input"
      in-line
      :model-value="chipsValue2"
    ></ChipsInputList>

    <h1 class="font-bold text-24">Variante 3</h1>
    <ChipsInputList
      id="ChipsInput"
      aria-label="chips input"
      :model-value="chipsValue3"
    ></ChipsInputList>
  </div>
</template>
