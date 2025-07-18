<script lang="ts" setup>
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import { onMounted, ref, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import ForeignLanguageVersion from "@/domain/foreignLanguageVersion"
import ComboboxItemService from "@/services/comboboxItemService"

const props = defineProps<{
  modelValue?: ForeignLanguageVersion
  modelValueList?: ForeignLanguageVersion[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: ForeignLanguageVersion]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

const lastSavedModelValue = ref(
  new ForeignLanguageVersion({ ...props.modelValue }),
)
const foreignLanguageVersion = ref(
  new ForeignLanguageVersion({ ...props.modelValue }),
)

async function addForeignInputVersion() {
  emit(
    "update:modelValue",
    foreignLanguageVersion.value as ForeignLanguageVersion,
  )
  emit("addEntry")
}

watch(
  () => props.modelValue,
  () => {
    foreignLanguageVersion.value = new ForeignLanguageVersion({
      ...props.modelValue,
    })
    lastSavedModelValue.value = new ForeignLanguageVersion({
      ...props.modelValue,
    })
  },
)

onMounted(() => {
  foreignLanguageVersion.value = new ForeignLanguageVersion({
    ...props.modelValue,
  })
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex flex-row gap-24">
      <div class="basis-1/2">
        <InputField
          id="foreignLanguageVersionLanguageInput"
          v-slot="slotProps"
          data-testid="foreign-language-version-language"
          label="Sprache"
        >
          <ComboboxInput
            id="foreignLanguageVersionLanguageInputText"
            v-model="foreignLanguageVersion.languageCode"
            aria-label="Sprache"
            class="w-full"
            data-testid="foreign-language-version-language-input"
            :invalid="slotProps.hasError"
            :item-service="ComboboxItemService.getLanguageCodes"
          ></ComboboxInput>
        </InputField>
      </div>
      <div class="basis-1/2">
        <InputField
          id="foreignLanguageVersionLinkInput"
          data-testid="foreign-lanuage-version-link"
          label="Link"
        >
          <InputText
            id="foreignLanguageVersionLinkTextInputText"
            v-model="foreignLanguageVersion.link"
            aria-label="Link"
            class="w-full"
            data-testid="foreign-language-version-link-input"
            size="small"
          ></InputText>
        </InputField>
      </div>
    </div>

    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            aria-label="Fremdsprachige Fassung speichern"
            :disabled="foreignLanguageVersion.isEmpty"
            label="Übernehmen"
            severity="secondary"
            size="small"
            @click.stop="addForeignInputVersion"
          ></Button>
          <Button
            v-if="!lastSavedModelValue.isEmpty"
            aria-label="Abbrechen"
            label="Abbrechen"
            size="small"
            text
            @click.stop="emit('cancelEdit')"
          ></Button>
        </div>
      </div>
      <Button
        v-if="!lastSavedModelValue.isEmpty"
        aria-label="Eintrag löschen"
        label="Eintrag löschen"
        severity="danger"
        size="small"
        @click.stop="emit('removeEntry', true)"
      ></Button>
    </div>
  </div>
</template>
