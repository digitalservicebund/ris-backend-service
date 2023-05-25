<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, ref, watch } from "vue"
import DivergentExpirationDefinedInputGroup from "@/components/DivergentExpirationDefinedInputGroup.vue"
import DivergentExpirationUndefinedInputGroup from "@/components/DivergentExpirationUndefinedInputGroup.vue"
import { Metadata, MetadataSectionName, MetadataSections } from "@/domain/Norm"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)

const isDivergentExpirationUndefined =
  loadedNorm.value?.metadataSections?.DIVERGENT_EXPIRATION?.some(
    (entry) => entry.DIVERGENT_EXPIRATION_UNDEFINED
  ) ?? false

interface Props {
  modelValue: MetadataSections
}

interface Emits {
  (event: "update:modelValue", value: MetadataSections): void
}

type ChildSectionName =
  | MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED
  | MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED

const childSection = ref<Metadata>({})
const selectedChildSectionName = ref<ChildSectionName>(
  MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED
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
  }
)

watch(selectedChildSectionName, () => (childSection.value = {}))

const component = computed(() => {
  switch (selectedChildSectionName.value) {
    case MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED:
      return DivergentExpirationDefinedInputGroup
    case MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED:
      return DivergentExpirationUndefinedInputGroup
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
          id="divergentExpirationDefinedSelection"
          v-model="selectedChildSectionName"
          aria-label="Bestimmtes grundsätzliches Außerkrafttretedatum Radio"
          name="DivergentExpirationDefined"
          type="radio"
          :value="MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED"
        />
        bestimmt
      </label>
      <label class="form-control">
        <input
          id="divergentExpirationUndefinedSelection"
          v-model="selectedChildSectionName"
          aria-label="Unbestimmtes abweichendes Außerkrafttretedatum Radio"
          :disabled="isDivergentExpirationUndefined"
          name="DivergentExpirationUndefined"
          type="radio"
          :value="MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED"
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

input[type="radio"]:disabled {
  color: #717a88;
}

input[type="radio"]:disabled:hover {
  border-width: 0.15em;
  border-color: #717a88;
  outline: none;
}
</style>
