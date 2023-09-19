<script lang="ts" setup>
import { produce } from "immer"
import { computed } from "vue"
import DivergentCategoryInputGroup from "@/components/divergentGroup/DivergentCategoryInputGroup.vue"
import { Metadata, MetadataSectionName, UndefinedDate } from "@/domain/norm"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import { DropdownItem } from "@/shared/components/input/types"

const props = defineProps<{
  modelValue: Metadata
  id: string
  label: string
  sectionName: MetadataSectionName
}>()

const emit = defineEmits<{
  "update:modelValue": [value: Metadata]
}>()

const ENTRY_INTO_FORCE_DATE_TRANSLATIONS: { [Value in UndefinedDate]: string } =
  {
    [UndefinedDate.UNDEFINED_UNKNOWN]: "unbestimmt (unbekannt)",
    [UndefinedDate.UNDEFINED_FUTURE]: "unbestimmt (zukünftig)",
    [UndefinedDate.UNDEFINED_NOT_PRESENT]: "nicht vorhanden",
  }

const dropdownItems: DropdownItem[] = Object.entries(
  ENTRY_INTO_FORCE_DATE_TRANSLATIONS,
).map(([value, label]) => {
  return { label, value }
})

const undefinedDate = computed({
  get: () => props.modelValue.UNDEFINED_DATE?.[0],
  set: (data) => {
    const next = produce(props.modelValue, (draft) => {
      draft.UNDEFINED_DATE = data ? [data] : undefined
    })
    emit("update:modelValue", next)
  },
})

const normCategory = computed({
  get: () => props.modelValue.NORM_CATEGORY ?? [],
  set: (data) => {
    const next = produce(props.modelValue, (draft) => {
      draft.NORM_CATEGORY = data
    })
    emit("update:modelValue", next)
  },
})
</script>

<template>
  <div class="flex w-384 flex-col gap-8">
    <InputField :id="`${id}Dropdown`" :aria-label="label" :label="label">
      <DropdownInput
        :id="`${id}Dropdown`"
        v-model="undefinedDate"
        :aria-label="`${label} Dropdown`"
        :items="dropdownItems"
        placeholder="Bitte auswählen"
      />
    </InputField>

    <DivergentCategoryInputGroup
      v-model="normCategory"
      :section-name="sectionName"
    />
  </div>
</template>
