<script lang="ts" setup>
import Button from "primevue/button"
import TokenizeText from "@/components/TokenizeText.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import MaterialSymbolsClose from "~icons/material-symbols/close"

defineProps<{
  fieldsOfLaw: FieldOfLaw[]
}>()

const emit = defineEmits<{
  "node:remove": [node: FieldOfLaw]
  "node:clicked": [node: FieldOfLaw]
}>()

function removeFieldOfLaw(fieldOfLaw: FieldOfLaw) {
  emit("node:remove", fieldOfLaw)
}

function fieldOfLawClicked(fieldOfLaw: FieldOfLaw) {
  emit("node:clicked", fieldOfLaw)
}
</script>

<template>
  <div
    v-if="fieldsOfLaw && fieldsOfLaw.length > 0"
    class="flex w-full flex-col pb-16"
  >
    <div
      v-for="fieldOfLaw in fieldsOfLaw"
      :key="fieldOfLaw.identifier"
      class="field-of-law flex w-full flex-row items-center border-b-1 border-blue-300 py-16 first:mt-16 first:border-t-1"
    >
      <div class="ris-label1-regular mr-8 flex-grow">
        <Button
          :aria-label="
            fieldOfLaw.identifier +
            ' ' +
            fieldOfLaw.text +
            ' im Sachgebietsbaum anzeigen'
          "
          class="mr-8"
          text
          @click="fieldOfLawClicked(fieldOfLaw)"
        >
          {{ fieldOfLaw.identifier }}
        </Button>

        <TokenizeText
          :keywords="fieldOfLaw.linkedFields ?? []"
          :text="fieldOfLaw.text"
          @linked-field:clicked="fieldOfLawClicked"
        />
      </div>

      <Button
        v-tooltip.bottom="'Entfernen'"
        :aria-label="
          fieldOfLaw.identifier + ' ' + fieldOfLaw.text + ' aus Liste entfernen'
        "
        class="!p-4"
        severity="secondary"
        text
        @click="removeFieldOfLaw(fieldOfLaw)"
      >
        <template #icon>
          <MaterialSymbolsClose class="text-blue-800" />
        </template>
      </Button>
    </div>
  </div>
</template>
