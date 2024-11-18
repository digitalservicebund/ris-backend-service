<script lang="ts" setup>
import Tooltip from "@/components/Tooltip.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import MaterialSymbolsClose from "~icons/material-symbols/close"

defineProps<{
  data: FieldOfLaw[]
}>()

const emit = defineEmits<{
  "node:remove": [node: FieldOfLaw]
}>()

function removeFieldOfLaw(fieldOfLaw: FieldOfLaw) {
  emit("node:remove", fieldOfLaw)
}
</script>

<template>
  <div class="flex w-full justify-between">
    <div class="flex w-full flex-col">
      <div
        v-for="(fieldOfLaw, index) in data"
        :key="index"
        class="flex h-56 w-full flex-row items-center border-b-1 border-blue-300 first:mt-16 first:border-t-1"
      >
        <div class="ds-label-01-reg mr-8 flex-grow">
          <span class="ds-link-01-bold"> {{ fieldOfLaw.identifier }} </span>

          {{ fieldOfLaw.text }}
        </div>

        <Tooltip text="Entfernen">
          <button
            class="flex items-center justify-center text-blue-800 hover:bg-blue-100 focus:shadow-[inset_0_0_0_0.125rem] focus:shadow-blue-800 focus:outline-none"
            data-testid="copy-summary"
            @click="removeFieldOfLaw(fieldOfLaw)"
            @keypress.enter="removeFieldOfLaw(fieldOfLaw)"
          >
            <MaterialSymbolsClose />
          </button>
        </Tooltip>
      </div>
    </div>
  </div>
</template>
