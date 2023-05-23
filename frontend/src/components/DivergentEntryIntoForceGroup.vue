<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import DivergentEntryIntoForceInputGroup from "@/components/DivergentEntryIntoForceInputGroup.vue"
import DivergentEntryIntoForceUndefinedInputGroup from "@/components/DivergentEntryIntoForceUndefinedInputGroup.vue"
import { Metadata, MetadataSectionName, MetadataSections } from "@/domain/Norm"

interface Props {
  modelValue: MetadataSections
}

interface Emits {
  (event: "update:modelValue", value: MetadataSections): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

type ChildSectionName =
  | MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED
  | MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED

const childSection = ref<Metadata>({})
const selectedChildSectionName = ref<ChildSectionName>(
  MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED
)

watch(
  childSection,
  () =>
    emit("update:modelValue", {
      [selectedChildSectionName.value]: [childSection.value],
    }),
  {
    deep: true,
  }
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
  }
)

watch(selectedChildSectionName, () => (childSection.value = {}))

const component = computed(() => {
  switch (selectedChildSectionName.value) {
    case MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED:
      return DivergentEntryIntoForceInputGroup
    case MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED:
      return DivergentEntryIntoForceUndefinedInputGroup
    default:
      return null
  }
})
</script>

<template>
  <div class="mt-16 w-384">
    <div class="radio-group w-320">
      <label class="form-control">
        <input
          id="divergentEntryIntoForceSelection"
          v-model="selectedChildSectionName"
          aria-label="Bestimmtes abweichendes Inkrafttretedatum"
          name="DivergentEntryIntoForce"
          type="radio"
          :value="MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED"
        />
        bestimmt
      </label>
      <label class="form-control">
        <input
          id="divergentEntryIntoForceUndefinedSelection"
          v-model="selectedChildSectionName"
          aria-label="Unbestimmtes Abweichendes Inkrafttretedatum"
          name="DivergentEntryIntoForceUndefined"
          type="radio"
          :value="MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED"
        />
        unbestimmt
      </label>
    </div>
    <component :is="component" v-model="childSection" />
  </div>
  <div></div>
</template>

<style lang="scss" scoped>
.radio-group {
  display: flex;
  justify-content: space-between;
  margin-bottom: 24px;
}

.form-control {
  display: flex;
  flex-direction: row;
  align-items: center;
}

input[type="radio"] {
  display: grid;
  width: 1.5em;
  height: 1.5em;
  border: 0.15em solid currentcolor;
  border-radius: 50%;
  margin-right: 10px;
  appearance: none;
  background-color: white;
  color: #004b76;
  place-content: center;
}

input[type="radio"]:hover,
input[type="radio"]:focus {
  border: 4px solid #004b76;
  outline: none;
}

input[type="radio"]::before {
  width: 0.9em;
  height: 0.9em;
  border-radius: 50%;
  background-color: #004b76;
  content: "";
  transform: scale(0);
}

input[type="radio"]:checked::before {
  transform: scale(1);
}
</style>
