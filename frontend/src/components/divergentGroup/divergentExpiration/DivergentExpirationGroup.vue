<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed } from "vue"
import DivergentDefinedInputGroup from "@/components/divergentGroup/DivergentDefinedInputGroup.vue"
import DivergentUndefinedInputGroup from "@/components/divergentGroup/DivergentUndefinedInputGroup.vue"
import { MetadataSectionName, MetadataSections } from "@/domain/norm"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"
import { useLoadedNormStore } from "@/stores/loadedNorm"

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
  DIVERGENT_EXPIRATION_DEFINED: props.modelValue.DIVERGENT_EXPIRATION_DEFINED,
  DIVERGENT_EXPIRATION_UNDEFINED:
    props.modelValue.DIVERGENT_EXPIRATION_UNDEFINED,
}

const selectedChildSection = computed<
  | MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED
  | MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED
>({
  get: () => {
    if (props.modelValue.DIVERGENT_EXPIRATION_DEFINED?.[0]) {
      return MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED
    } else if (props.modelValue.DIVERGENT_EXPIRATION_UNDEFINED?.[0]) {
      return MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED
    } else {
      return MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED
    }
  },
  set(value) {
    emit("update:modelValue", { [value]: initialValue[value] ?? [{}] })
  },
})

const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)

const isDivergentExpirationUndefined = computed(() => {
  const hasUndefinedEntry =
    loadedNorm.value?.metadataSections?.DIVERGENT_EXPIRATION?.some(
      (entry) => entry.DIVERGENT_EXPIRATION_UNDEFINED,
    ) ?? false

  return (
    hasUndefinedEntry &&
    selectedChildSection.value !==
      MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED
  )
})

/* -------------------------------------------------- *
 * Section data                                       *
 * -------------------------------------------------- */

const definedDateSection = computed({
  get: () => props.modelValue.DIVERGENT_EXPIRATION_DEFINED?.[0] ?? {},
  set: (data) => {
    const effectiveData = data ? [data] : undefined
    initialValue.DIVERGENT_EXPIRATION_DEFINED = effectiveData

    const next: MetadataSections = {
      DIVERGENT_EXPIRATION_DEFINED: effectiveData,
    }
    emit("update:modelValue", next)
  },
})

const undefinedDateSection = computed({
  get: () => props.modelValue.DIVERGENT_EXPIRATION_UNDEFINED?.[0] ?? {},
  set: (data) => {
    const effectiveData = data ? [data] : undefined
    initialValue.DIVERGENT_EXPIRATION_UNDEFINED = effectiveData

    const next: MetadataSections = {
      DIVERGENT_EXPIRATION_UNDEFINED: effectiveData,
    }
    emit("update:modelValue", next)
  },
})
</script>

<template>
  <div>
    <div class="mb-8 flex w-320 justify-between">
      <InputField
        id="divergentExpirationDefinedSelection"
        v-slot="{ id }"
        label="bestimmt"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedChildSection"
          name="divergentExpiration"
          size="medium"
          :value="MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED"
        />
      </InputField>

      <InputField
        id="divergentExpirationUndefinedSelection"
        v-slot="{ id }"
        label="unbestimmt"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedChildSection"
          :disabled="isDivergentExpirationUndefined"
          name="divergentExpiration"
          size="medium"
          :value="MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED"
        />
      </InputField>
    </div>

    <DivergentDefinedInputGroup
      v-if="
        selectedChildSection ===
        MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED
      "
      id="divergentExpirationDefinedDate"
      v-model="definedDateSection"
      label="Bestimmtes abweichendes Außerkrafttretedatum"
      :section-name="MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED"
    />

    <DivergentUndefinedInputGroup
      v-if="
        selectedChildSection ===
        MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED
      "
      id="divergentExpirationUndefinedDate"
      v-model="undefinedDateSection"
      label="Unbestimmtes abweichendes Außerkrafttretedatum"
      :section-name="MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED"
    />
  </div>
</template>
