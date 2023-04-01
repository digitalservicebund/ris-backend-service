<script lang="ts" setup>
import ComboboxInput from "@/components/ComboboxInput.vue"
import { FieldOfLawComboboxItem } from "@/domain/fieldOfLaw"
import ComboboxItemService from "@/services/comboboxItemService"
import { ComboboxInputModelType } from "@/shared/components/input/types"

const emit = defineEmits<{
  (event: "add-to-list", identifier: string): void
}>()

function handleUpdateModelValue(item: ComboboxInputModelType | undefined) {
  if (!item) return
  emit("add-to-list", (item as FieldOfLawComboboxItem).label)
}
</script>

<template>
  <p class="heading-04-regular pb-8">Direkteingabe Sachgebiet</p>
  <div class="flex flex-col">
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
