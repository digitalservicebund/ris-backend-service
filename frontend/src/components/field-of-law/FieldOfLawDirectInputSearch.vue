<script lang="ts" setup>
import { nextTick, ref } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import ComboboxItemServices from "@/services/comboboxItemService"

const emit = defineEmits<{
  "add-to-list": [item: FieldOfLaw]
}>()

const value = ref<FieldOfLaw | undefined>(undefined)

const handleUpdateModelValue = async (newValue: unknown | undefined) => {
  // Clearing the Dropdown can emit undefined, so we only add to list if the value is set.
  if (newValue != null) emit("add-to-list", newValue as FieldOfLaw)

  // Clearing the dropdown again, we need to first set a value and then remove it a tick later so the
  // reference is fully updated once so the component realises it needs to reset the input.
  value.value = newValue as FieldOfLaw
  await nextTick()
  value.value = undefined
}
</script>

<template>
  <div class="flex w-full flex-col">
    <p class="ris-label2-regular pb-4">Direkteingabe Sachgebiet</p>
    <ComboboxInput
      id="directInputCombobox"
      aria-label="Direkteingabe-Sachgebietssuche eingeben"
      :item-service="ComboboxItemServices.getFieldOfLawSearchByIdentifier"
      :model-value="value"
      placeholder="Sachgebiet"
      @update:model-value="handleUpdateModelValue"
    >
    </ComboboxInput>
  </div>
</template>
