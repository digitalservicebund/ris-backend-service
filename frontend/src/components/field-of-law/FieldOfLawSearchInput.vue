<script setup lang="ts">
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import InputField from "@/components/input/InputField.vue"
import IconSearch from "~icons/ic/baseline-search"

defineProps<{
  errorLabel?: string
}>()

const emit = defineEmits<{
  search: [void]
}>()

const identifier = defineModel<string>("identifier")
const description = defineModel<string>("description")
const norm = defineModel<string>("norm")
</script>

<template>
  <div class="flex w-full flex-col">
    <div class="flex w-full flex-row items-end gap-16">
      <InputField id="fieldOfLawIdentifierInput" label="Sachgebiet">
        <InputText
          id="fieldOfLawIdentifierInput"
          v-model="identifier"
          aria-label="Sachgebietskürzel"
          fluid
          size="small"
          @keyup.enter="emit('search')"
        />
      </InputField>
      <InputField id="fieldOfLawDescriptionInput" label="Bezeichnung">
        <InputText
          id="fieldOfLawDescriptionInput"
          v-model="description"
          aria-label="Sachgebietsbezeichnung"
          fluid
          size="small"
          @keyup.enter="emit('search')"
        />
      </InputField>
      <InputField id="fieldOfLawNormInput" label="Norm">
        <InputText
          id="fieldOfLawNormInput"
          v-model="norm"
          aria-label="Sachgebietsnorm"
          fluid
          size="small"
          @keyup.enter="emit('search')"
        />
      </InputField>

      <Button
        aria-label="Sachgebietssuche ausführen"
        class="min-w-48"
        @click="emit('search')"
      >
        <template #icon>
          <IconSearch />
        </template>
      </Button>
    </div>

    <span
      v-if="errorLabel"
      class="ris-label3-regular min-h-[1rem] text-red-800"
      >{{ errorLabel }}</span
    >
  </div>
</template>
