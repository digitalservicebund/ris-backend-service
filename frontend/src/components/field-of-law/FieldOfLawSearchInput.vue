<script setup lang="ts">
import { computed } from "vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import IconSearch from "~icons/ic/baseline-search"

const props = defineProps<{
  identifier?: string
  description?: string
  norm?: string
}>()

const emit = defineEmits<{
  search: [void]
  "update:identifier": [value?: string]
  "update:description": [value?: string]
  "update:norm": [value?: string]
}>()

const identifier = computed({
  get: () => props.identifier,
  set: (newValue) => emit("update:identifier", newValue),
})

const description = computed({
  get: () => props.description,
  set: (newValue) => emit("update:description", newValue),
})
const norm = computed({
  get: () => props.norm,
  set: (newValue) => emit("update:norm", newValue),
})
</script>

<template>
  <div class="flex w-full flex-row items-end gap-16">
    <InputField id="fieldOfLawDirectInput" label="Sachgebiet">
      <TextInput
        id="fieldOfLawDirectInput"
        v-model="identifier"
        aria-label="Sachgebiet Direkteingabe"
        size="medium"
        @enter-released="emit('search')"
      />
    </InputField>
    <InputField id="fieldOfLawDirectInput" label="Bezeichnung">
      <TextInput
        id="fieldOfLawSearch"
        v-model="description"
        aria-label="Sachgebiet Suche"
        size="medium"
        @enter-released="emit('search')"
      />
    </InputField>
    <InputField id="fieldOfLawNormInput" label="Norm">
      <TextInput
        id="fieldOfLawNorm"
        v-model="norm"
        aria-label="Sachgebiet Norm"
        size="medium"
        @enter-released="emit('search')"
      />
    </InputField>

    <TextButton
      aria-label="Suchen"
      button-type="primary"
      :icon="IconSearch"
      @click="emit('search')"
    />
  </div>
</template>
