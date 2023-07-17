<script lang="ts" setup>
import { ref, watch } from "vue"
import DivergentDefinedInputGroup from "@/components/divergentGroup/DivergentDefinedInputGroup.vue"
import DivergentUndefinedInputGroup from "@/components/divergentGroup/DivergentUndefinedInputGroup.vue"
import { Metadata, MetadataSectionName, MetadataSections } from "@/domain/Norm"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"

interface Props {
  modelValue: MetadataSections
}

const props = defineProps<Props>()

const emit = defineEmits<{
  "update:modelValue": [value: MetadataSections]
}>()

type ChildSectionName =
  | MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED
  | MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED

const childSection = ref<Metadata>({})
const selectedChildSectionName = ref<ChildSectionName>(
  MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED,
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
    if (modelValue.DIVERGENT_ENTRY_INTO_FORCE_DEFINED) {
      selectedChildSectionName.value =
        MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED
      childSection.value = modelValue.DIVERGENT_ENTRY_INTO_FORCE_DEFINED[0]
    } else if (modelValue.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED) {
      selectedChildSectionName.value =
        MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED
      childSection.value = modelValue.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED[0]
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
        id="divergentEntryIntoForceDefinedSelection"
        v-slot="{ id }"
        label="bestimmt"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          :id="id"
          v-model="selectedChildSectionName"
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
          v-model="selectedChildSectionName"
          name="divergentEntryIntoForce"
          size="medium"
          :value="MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED"
        />
      </InputField>
    </div>

    <DivergentDefinedInputGroup
      v-if="
        selectedChildSectionName ===
        MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED
      "
      id="divergentEntryIntoForceDefinedDate"
      v-model="childSection"
      label="Bestimmtes abweichendes Inkrafttretedatum"
      :section-name="MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED"
    />

    <DivergentUndefinedInputGroup
      v-if="
        selectedChildSectionName ===
        MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED
      "
      id="divergentEntryIntoForceUndefinedDate"
      v-model="childSection"
      label="Unbestimmtes abweichendes Inkrafttretedatum"
      :section-name="MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED"
    />
  </div>
</template>
