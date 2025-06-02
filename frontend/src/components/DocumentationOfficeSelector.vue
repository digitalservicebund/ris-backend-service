<script lang="ts" setup>
import { computed, Ref, shallowRef, watchEffect } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { ComboboxItem } from "@/components/input/types"
import InputErrorMessages from "@/components/InputErrorMessages.vue"
import DocumentationOffice from "@/domain/documentationOffice"
import errorMessages from "@/i18n/errors.json"
import ComboboxItemService from "@/services/comboboxItemService"

type DocumentationOfficeComboboxInputType = {
  label: string
  value: DocumentationOffice
}
const props = defineProps<{
  modelValue: DocumentationOffice | undefined
  excludeOfficeAbbreviations?: string[] | null
  hasError?: boolean
  styleClass?: string
}>()

const emit = defineEmits<{
  (e: "update:modelValue", value: DocumentationOffice | undefined): void
  (e: "update:hasError", value: boolean): void
}>()

const modelValueAdapter = computed({
  get: () =>
    props.modelValue?.abbreviation
      ? {
          label: props.modelValue.abbreviation,
          value: props.modelValue,
        }
      : undefined,
  set: (newValue: DocumentationOffice) => {
    if (newValue) {
      emit("update:modelValue", newValue)
      emit("update:hasError", false)
    } else {
      emit("update:modelValue", undefined)
      emit("update:hasError", false)
    }
  },
})

/**
 * @summary Provides a dynamically filtered list of documentation offices for a combobox.
 * @description
 * Fetches documentation offices and then applies a client-side exclusion
 * based on the provided exclusion list. It uses `watchEffect` to ensure the returned
 * `data` property reactively updates if the fetched list or exclusion criteria change.
 */
const getFilteredItems = (filter: Ref<string | undefined>) => {
  const serviceCallResult = ComboboxItemService.getDocumentationOffices(filter)
  const filteredData = shallowRef<ComboboxItem[] | null>(null)

  watchEffect(() => {
    const allItems = serviceCallResult.data.value
    if (allItems) {
      if (
        props.excludeOfficeAbbreviations &&
        props.excludeOfficeAbbreviations.length > 0
      ) {
        const exclusionSet = new Set(props.excludeOfficeAbbreviations)

        filteredData.value = allItems.filter(
          (item) => !exclusionSet.has(item.label),
        )
      } else {
        filteredData.value = allItems
      }
    } else {
      filteredData.value = null
    }
  })

  return {
    ...serviceCallResult,
    data: filteredData,
  }
}
</script>

<template>
  <div>
    <ComboboxInput
      id="documentationOfficeSelector"
      aria-label="Dokumentationsstelle auswählen"
      :class="styleClass"
      data-testid="documentation-office-combobox"
      :has-error="props.hasError"
      :item-service="getFilteredItems"
      :model-value="modelValueAdapter"
      placeholder="Dokumentationsstelle auswählen"
      @update:model-value="
        modelValueAdapter = $event as
          | DocumentationOfficeComboboxInputType
          | undefined
      "
    />
    <InputErrorMessages
      v-if="props.hasError"
      :error-message="errorMessages.NO_DOCUMENTATION_OFFICE_SELECTED.title"
    />
  </div>
</template>
