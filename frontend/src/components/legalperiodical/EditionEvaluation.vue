<script lang="ts" setup>
import { computed, onMounted, ref } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import LegalPeriodical from "@/domain/legalPeriodical"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import ComboboxItemService from "@/services/comboboxItemService"
import LegalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"

const legalPeriodicalEdition = ref()

const legalPeriodical = computed({
  get: () =>
    legalPeriodicalEdition.value?.legalPeriodical
      ? {
          label:
            legalPeriodicalEdition.value?.legalPeriodical
              .legalPeriodicalAbbreviation,
          value: legalPeriodicalEdition.value?.legalPeriodical,
          additionalInformation:
            legalPeriodicalEdition.value?.legalPeriodical
              .legalPeriodicalSubtitle,
        }
      : undefined,
  set: (newValue) => {
    const legalPeriodical = { ...newValue } as LegalPeriodical
    if (newValue) {
      legalPeriodicalEdition.value.legalPeriodical = legalPeriodical
    } else {
      legalPeriodicalEdition.value.legalPeriodical = undefined
    }
  },
})

const prefix = computed({
  get: () => legalPeriodicalEdition.value?.prefix ?? undefined,
  set: (newValue) => {
    legalPeriodicalEdition.value.prefix = newValue
  },
})
const suffix = computed({
  get: () => legalPeriodicalEdition.value?.suffix ?? undefined,
  set: (newValue) => {
    legalPeriodicalEdition.value.suffix = newValue
  },
})

const name = computed({
  get: () => legalPeriodicalEdition.value?.name ?? undefined,
  set: (newValue) => {
    legalPeriodicalEdition.value.name = newValue
  },
})

async function saveEdition() {
  await LegalPeriodicalEditionService.save(legalPeriodicalEdition.value)
}

onMounted(() => {
  legalPeriodicalEdition.value = new LegalPeriodicalEdition()
})
</script>

<template>
  <div class="flex h-full flex-col space-y-24 bg-gray-100 px-16 py-16">
    <h2 class="ds-heading-03-reg">Periodikaauswertung</h2>

    <InputField id="legalPeriodical" label="Periodikum *">
      <ComboboxInput
        id="legalPeriodical"
        v-model="legalPeriodical"
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
          v-model="prefix"
          aria-label="Präfix"
          class="ds-input-medium"
          size="medium"
        ></TextInput>
      </InputField>

      <InputField id="suffix" label="Suffix">
        <TextInput
          id="suffix"
          v-model="suffix"
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
        v-model="name"
        aria-label="Name der Ausgabe"
        class="ds-input-medium"
        size="medium"
      ></TextInput>
    </InputField>

    <TextButton
      class="ds-button-02-reg"
      label="Auswertung starten"
      @click="saveEdition"
    ></TextButton>
  </div>
</template>
