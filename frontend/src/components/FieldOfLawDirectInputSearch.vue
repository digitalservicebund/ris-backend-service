<script lang="ts" setup>
import { ref } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { ComboboxInputModelType } from "@/components/input/types"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import ComboboxItemService from "@/services/comboboxItemService"

const emit = defineEmits<{
  "add-to-list": [item: FieldOfLawNode]
}>()
const fieldOfLawNode = ref()

function handleUpdateModelValue(item: ComboboxInputModelType | undefined) {
  if (!item) return
  fieldOfLawNode.value = item
  emit("add-to-list", item as FieldOfLawNode)
}
</script>

<template>
  <p class="heading-04-regular pb-8 pt-24">Direkteingabe Sachgebiet</p>
  <div class="flex w-1/3 flex-col">
    <div class="flex flex-row items-stretch">
      <div class="grow">
        <ComboboxInput
          id="directInputCombobox"
          aria-label="Direkteingabe-Sachgebietssuche eingeben"
          clear-on-choosing-item
          :item-service="ComboboxItemService.getFieldOfLawSearchByIdentifier"
          :model-value="fieldOfLawNode"
          placeholder="Sachgebiet"
          @update:model-value="handleUpdateModelValue"
        >
        </ComboboxInput>
      </div>
    </div>
  </div>
</template>
