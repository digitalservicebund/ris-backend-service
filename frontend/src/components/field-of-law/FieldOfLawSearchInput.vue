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
  errorLabel?: string
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
  <div class="flex w-full flex-col">
    <div class="flex w-full flex-row items-end gap-16">
      <InputField id="fieldOfLawIdentifierInput" label="Sachgebiet">
        <TextInput
          id="fieldOfLawIdentifierInput"
          v-model="identifier"
          aria-label="Sachgebietskürzel"
          size="medium"
          @enter-released="emit('search')"
        />
      </InputField>
      <InputField id="fieldOfLawDescriptionInput" label="Bezeichnung">
        <TextInput
          id="fieldOfLawDescriptionInput"
          v-model="description"
          aria-label="Sachgebietsbezeichnung"
          size="medium"
          @enter-released="emit('search')"
        />
      </InputField>
      <InputField id="fieldOfLawNormInput" label="Norm">
        <TextInput
          id="fieldOfLawNormInput"
          v-model="norm"
          aria-label="Sachgebietsnorm"
          size="medium"
          @enter-released="emit('search')"
        />
      </InputField>

      <TextButton
        aria-label="Sachgebietssuche ausführen"
        button-type="primary"
        :icon="IconSearch"
        @click="emit('search')"
      />
    </div>

    <span v-if="errorLabel" class="ds-label-03-reg min-h-[1rem] text-red-800">{{
      errorLabel
    }}</span>
  </div>
</template>
