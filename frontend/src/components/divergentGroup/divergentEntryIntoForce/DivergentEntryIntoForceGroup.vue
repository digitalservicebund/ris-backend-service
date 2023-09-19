<script lang="ts" setup>
import { computed } from "vue"
import DivergentDefinedInputGroup from "@/components/divergentGroup/DivergentDefinedInputGroup.vue"
import DivergentUndefinedInputGroup from "@/components/divergentGroup/DivergentUndefinedInputGroup.vue"
import { MetadataSectionName, MetadataSections } from "@/domain/norm"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"

const props = defineProps<{
  modelValue: MetadataSections
}>()

const emit = defineEmits<{
  "update:modelValue": [value: MetadataSections]
}>()

/* -------------------------------------------------- *
 * Section type                                       *
 * -------------------------------------------------- */

const initialValue: MetadataSections = {
  DIVERGENT_ENTRY_INTO_FORCE_DEFINED:
    props.modelValue.DIVERGENT_ENTRY_INTO_FORCE_DEFINED,
  DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED:
    props.modelValue.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED,
}

const selectedChildSection = computed<
  | MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED
  | MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED
>({
  get: () => {
    if (props.modelValue.DIVERGENT_ENTRY_INTO_FORCE_DEFINED?.[0]) {
      return MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED
    } else if (props.modelValue.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED?.[0]) {
      return MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED
    } else {
      return MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED
    }
  },
  set(value) {
    emit("update:modelValue", { [value]: initialValue[value] ?? [{}] })
  },
})

/* -------------------------------------------------- *
 * Section data                                       *
 * -------------------------------------------------- */

const definedDateSection = computed({
  get: () => props.modelValue.DIVERGENT_ENTRY_INTO_FORCE_DEFINED?.[0] ?? {},
  set: (data) => {
    const effectiveData = data ? [data] : undefined
    initialValue.DIVERGENT_ENTRY_INTO_FORCE_DEFINED = effectiveData

    const next: MetadataSections = {
      DIVERGENT_ENTRY_INTO_FORCE_DEFINED: effectiveData,
    }
    emit("update:modelValue", next)
  },
})

const undefinedDateSection = computed({
  get: () => props.modelValue.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED?.[0] ?? {},
  set: (data) => {
    const effectiveData = data ? [data] : undefined
    initialValue.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED = effectiveData

    const next: MetadataSections = {
      DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED: effectiveData,
    }
    emit("update:modelValue", next)
  },
})
</script>

<template>
  <div>
    <div class="mb-8 flex w-320 justify-between">
      <InputField
        id="divergentEntryIntoForceDefinedSelection"
        v-slot="{ id }"
        label="bestimmt"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedChildSection"
          name="divergentEntryIntoForce"
          size="medium"
          :value="MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED"
        />
      </InputField>

      <InputField
        id="divergentEntryIntoForceUndefinedSelection"
        v-slot="{ id }"
        label="unbestimmt"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedChildSection"
          name="divergentEntryIntoForce"
          size="medium"
          :value="MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED"
        />
      </InputField>
    </div>

    <DivergentDefinedInputGroup
      v-if="
        selectedChildSection ===
        MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED
      "
      id="divergentEntryIntoForceDefinedDate"
      v-model="definedDateSection"
      label="Bestimmtes abweichendes Inkrafttretedatum"
      :section-name="MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED"
    />

    <DivergentUndefinedInputGroup
      v-if="
        selectedChildSection ===
        MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED
      "
      id="divergentEntryIntoForceUndefinedDate"
      v-model="undefinedDateSection"
      label="Unbestimmtes abweichendes Inkrafttretedatum"
      :section-name="MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED"
    />
  </div>
</template>
