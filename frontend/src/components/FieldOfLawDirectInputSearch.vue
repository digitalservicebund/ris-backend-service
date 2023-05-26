<script lang="ts" setup>
import ComboboxInput from "@/components/ComboboxInput.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import ComboboxItemService from "@/services/comboboxItemService"

const emit = defineEmits<{
  (event: "add-to-list", identifier: string): void
}>()

function handleUpdateModelValue(item: FieldOfLawNode) {
  if (!item) return
  emit("add-to-list", item.identifier)
}
</script>

<template>
  <p class="heading-04-regular pb-8 pt-24">Direkteingabe Sachgebiet</p>
  <div class="flex flex-col w-1/3">
    <div class="flex flex-row items-stretch">
      <div class="grow">
        <ComboboxInput
          id="directInputCombobox"
          aria-label="Direkteingabe-Sachgebietssuche eingeben"
          clear-on-choosing-item
          :item-service="ComboboxItemService.getFieldOfLawSearchByIdentifier"
          placeholder="Sachgebiet"
          @update:model-value="handleUpdateModelValue"
        >
        </ComboboxInput>
      </div>
    </div>
  </div>
</template>
