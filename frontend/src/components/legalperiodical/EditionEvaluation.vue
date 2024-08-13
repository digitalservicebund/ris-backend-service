<script lang="ts" setup>
import { computed } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import LegalPeriodical from "@/domain/legalPeriodical.ts"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition.ts"
import ComboboxItemService from "@/services/comboboxItemService"

const modelValue = new LegalPeriodicalEdition()

const legalPeriodical = computed({
  get: () =>
    modelValue.value?.legalPeriodical
      ? {
          label: modelValue.value?.legalPeriodical.legalPeriodicalAbbreviation,
          value: modelValue.value?.legalPeriodical,
          additionalInformation:
            modelValue.value?.legalPeriodical.legalPeriodicalSubtitle,
        }
      : undefined,
  set: (newValue) => {
    const legalPeriodical = { ...newValue } as LegalPeriodical
    if (newValue) {
      modelValue.value.legalPeriodical = legalPeriodical
    } else {
      modelValue.value.legalPeriodical = undefined
    }
  },
})
</script>

<template>
  <div class="flex h-full flex-col space-y-24 bg-gray-100 px-16 py-16">
    <h2 class="ds-heading-03-reg">Periodikaauswertung</h2>

    <InputField id="legalPeriodical" label="Periodikum *">
      <ComboboxInput
        id="legalPeriodical"
        v-model="modelValue.legalPeriodical"
        aria-label="Periodikum"
        clear-on-choosing-item
        :has-error="false"
        :item-service="ComboboxItemService.getLegalPeriodicals"
      ></ComboboxInput>
    </InputField>

    <div class="flex flex-row items-end gap-24">
      <InputField id="prefix" label="Präfix">
        <TextInput
          id="prefix"
          v-model="modelValue.prefix"
          aria-label="Präfix"
          class="ds-input-medium"
          size="medium"
        ></TextInput>
      </InputField>

      <InputField id="suffix" label="Suffix">
        <TextInput
          id="suffix"
          v-model="modelValue.suffix"
          aria-label="Suffix"
          class="ds-input-medium"
          size="medium"
        ></TextInput>
      </InputField>
    </div>

    <span v-if="legalPeriodical" class="ds-label-03-reg"
      >Zitierbeispiel: {{ legalPeriodical.value.citationStyle }}</span
    >

    <InputField id="edition" label="Name der Ausgabe (optional)">
      <TextInput
        id="edition"
        v-model="modelValue.name"
        aria-label="Name der Ausgabe"
        class="ds-input-medium"
        size="medium"
      ></TextInput>
    </InputField>

    <TextButton
      class="ds-button-02-reg"
      label="Auswertung startem"
    ></TextButton>
  </div>
</template>
