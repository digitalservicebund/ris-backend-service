<script lang="ts" setup>
import ComboboxInput from "@/components/ComboboxInput.vue"
import TextButton from "@/components/TextButton.vue"
import { ComboboxInputModelType } from "@/domain"
import { FieldOfLawComboboxItem } from "@/domain/fieldOfLaw"
import ComboboxItemService from "@/services/comboboxItemService"

const emit = defineEmits<{
  (event: "add-to-list", identifier: string): void
}>()

function handleUpdateModelValue(item: ComboboxInputModelType | undefined) {
  if (!item) return
  emit("add-to-list", (item as FieldOfLawComboboxItem).label)
}
</script>

<template>
  <h1 class="heading-03-regular pb-8">Direkteingabe</h1>
  <div class="flex flex-col">
    <div class="pb-28">
      <div class="flex flex-row items-stretch">
        <div class="grow">
          <ComboboxInput
            id="directInputCombobox"
            aria-label="Direkteingabe-Sachgebietssuche eingeben"
            clear-on-choosing-item
            :item-service="ComboboxItemService.getFieldOfLawSearchByIdentifier"
            @update:model-value="handleUpdateModelValue"
          >
          </ComboboxInput>
        </div>
        <div class="pl-8">
          <TextButton
            aria-label="Direkteingabe-Sachgebietssuche ausführen"
            button-type="secondary"
            class="w-fit"
            label="Hinzufügen"
          />
        </div>
      </div>
    </div>
  </div>
</template>
