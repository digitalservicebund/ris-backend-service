<script lang="ts" setup>
import Button from "primevue/button"
import InputSelect from "primevue/select"
import { onMounted, ref, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import ChipsBorderNumberInput from "@/components/input/ChipsBorderNumberInput.vue"
import ChipsInput from "@/components/input/ChipsInput.vue"
import InputField from "@/components/input/InputField.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import { translationType } from "@/data/translationType"
import OriginOfTranslation from "@/domain/originOfTranslation"
import ComboboxItemService from "@/services/comboboxItemService"

const props = defineProps<{
  modelValue?: OriginOfTranslation
  modelValueList?: OriginOfTranslation[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: OriginOfTranslation]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

const validationStore = useValidationStore<["borderNumber"][number]>()

const lastSavedModelValue = ref(
  new OriginOfTranslation({ ...props.modelValue }),
)
const originOfTranslation = ref(
  new OriginOfTranslation({ ...props.modelValue }),
)

async function addOriginOfTranslation() {
  emit("update:modelValue", originOfTranslation.value as OriginOfTranslation)
  emit("addEntry")
}

watch(
  () => props.modelValue,
  () => {
    originOfTranslation.value = new OriginOfTranslation({
      ...props.modelValue,
    })
    lastSavedModelValue.value = new OriginOfTranslation({
      ...props.modelValue,
    })
  },
)

onMounted(() => {
  originOfTranslation.value = new OriginOfTranslation({
    ...props.modelValue,
  })
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex flex-row gap-24">
      <div class="basis-1/2">
        <InputField
          id="originOfTranslationLanguageInput"
          v-slot="slotProps"
          data-testid="origin-of-translation-language"
          label="Originalsprache*"
        >
          <ComboboxInput
            id="originOfTranslationLanguageInputText"
            v-model="originOfTranslation.languageCode"
            aria-label="Originalsprache"
            class="w-full"
            data-testid="origin-of-translation-language-input"
            :invalid="slotProps.hasError"
            :item-service="ComboboxItemService.getLanguageCodes"
            placeholder="Sprache auswählen"
          ></ComboboxInput>
        </InputField>
      </div>
      <div class="basis-1/2">
        <InputField
          id="originOfTranslationTranslators"
          data-testid="origin-of-translation-translators"
          label="Übersetzer:innen"
        >
          <ChipsInput
            id="originOfTranslationTranslatorsInputText"
            v-model="originOfTranslation.translators"
            aria-label="Übersetzer:innen"
            class="w-full"
            data-testid="origin-of-translation-translators-input"
            placeholder="Name"
            size="small"
          ></ChipsInput>
        </InputField>
      </div>
    </div>
    <div class="flex flex-row gap-24">
      <div class="basis-1/2">
        <InputField
          id="originOfTranslationBorderNumbers"
          v-slot="slotProps"
          data-testid="origin-of-translation-border-numbers"
          label="Fundstelle: Interne Verlinkung"
        >
          <ChipsBorderNumberInput
            id="originOfTranslationBorderNumbersInputText"
            v-model="originOfTranslation.borderNumbers"
            aria-label="Fundstelle: Interne Verlinkung"
            class="w-full"
            data-testid="origin-of-translation-border-numbers-input"
            :has-error="slotProps.hasError"
            placeholder="Randnummer"
            size="small"
            @focus="validationStore.remove('borderNumber')"
            @update:validation-error="slotProps.updateValidationError"
          ></ChipsBorderNumberInput>
        </InputField>
      </div>
      <div class="basis-1/2">
        <InputField
          id="originOfTranslationUrls"
          data-testid="origin-of-translation-urls"
          label="Fundstelle: Externe Verlinkung"
        >
          <ChipsInput
            id="originOfTranslationUrlsInputText"
            v-model="originOfTranslation.urls"
            aria-label="Fundstelle: Externe Verlinkung"
            class="w-full"
            data-testid="origin-of-translation-urls-input"
            placeholder="URL Website"
            size="small"
          ></ChipsInput>
        </InputField>
      </div>
    </div>
    <div class="flex flex-row gap-24">
      <InputField
        id="originOfTranslationTranslationType"
        v-slot="{ id }"
        data-testid="origin-of-translation-translation-type"
        label="Übersetzungsart"
      >
        <InputSelect
          :id="id"
          v-model="originOfTranslation.translationType"
          aria-label="Übersetzungsart"
          fluid
          option-label="label"
          option-value="value"
          :options="translationType"
          placeholder="Übersetzungsart auswählen"
        />
      </InputField>
    </div>

    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            aria-label="Herkunft der Übersetzung speichern"
            :disabled="originOfTranslation.isEmpty"
            label="Übernehmen"
            severity="secondary"
            size="small"
            @click.stop="addOriginOfTranslation"
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
