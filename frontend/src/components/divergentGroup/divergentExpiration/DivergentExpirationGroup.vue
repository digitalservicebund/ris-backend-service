<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, ref, watch } from "vue"
import DivergentDefinedInputGroup from "@/components/divergentGroup/DivergentDefinedInputGroup.vue"
import DivergentUndefinedInputGroup from "@/components/divergentGroup/DivergentUndefinedInputGroup.vue"
import { Metadata, MetadataSectionName, MetadataSections } from "@/domain/Norm"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: MetadataSections]
}>()

const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)

const isDivergentExpirationUndefined = computed(() => {
  return (
    loadedNorm.value?.metadataSections?.DIVERGENT_EXPIRATION?.some(
      (entry) => entry.DIVERGENT_EXPIRATION_UNDEFINED,
    ) ?? false
  )
})

interface Props {
  modelValue: MetadataSections
}

type ChildSectionName =
  | MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED
  | MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED

const childSection = ref<Metadata>({})
const selectedChildSectionName = ref<ChildSectionName>(
  MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED,
)

watch(
  childSection,
  () =>
    emit("update:modelValue", {
      [selectedChildSectionName.value]: [childSection.value],
    }),
  {
    deep: true,
  },
)

watch(
  () => props.modelValue,
  (modelValue) => {
    if (modelValue.DIVERGENT_EXPIRATION_DEFINED) {
      selectedChildSectionName.value =
        MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED
      childSection.value = modelValue.DIVERGENT_EXPIRATION_DEFINED[0]
    } else if (modelValue.DIVERGENT_EXPIRATION_UNDEFINED) {
      selectedChildSectionName.value =
        MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED
      childSection.value = modelValue.DIVERGENT_EXPIRATION_UNDEFINED[0]
    }
  },
  {
    immediate: true,
    deep: true,
  },
)

watch(selectedChildSectionName, () => (childSection.value = {}))
</script>

<template>
  <div>
    <div class="mb-24 flex w-320 justify-between">
      <InputField
        id="divergentExpirationDefinedSelection"
        v-slot="{ id }"
        label="bestimmt"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedChildSectionName"
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
          v-model="selectedChildSectionName"
          :disabled="
            isDivergentExpirationUndefined &&
            !(
              selectedChildSectionName ===
              MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED
            )
          "
          name="divergentExpiration"
          size="medium"
          :value="MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED"
        />
      </InputField>
    </div>

    <DivergentDefinedInputGroup
      v-if="
        selectedChildSectionName ===
        MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED
      "
      id="divergentExpirationDefinedDate"
      v-model="childSection"
      label="Bestimmtes abweichendes Außerkrafttretedatum"
      :section-name="MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED"
    />

    <DivergentUndefinedInputGroup
      v-if="
        selectedChildSectionName ===
        MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED
      "
      id="divergentExpirationUndefinedDate"
      v-model="childSection"
      label="Unbestimmtes abweichendes Außerkrafttretedatum"
      :section-name="MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED"
    />
  </div>
</template>
