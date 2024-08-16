<script lang="ts" setup>
import { computed, ref } from "vue"
import { useRouter } from "vue-router"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import LegalPeriodical from "@/domain/legalPeriodical"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import ComboboxItemService from "@/services/comboboxItemService"
import LegalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"

const router = useRouter()
const legalPeriodicalEdition = ref<LegalPeriodicalEdition>(
  new LegalPeriodicalEdition(),
)

const validationStore =
  useValidationStore<(typeof LegalPeriodicalEdition.fields)[number]>()

const legalPeriodical = computed({
  get: () =>
    legalPeriodicalEdition.value?.legalPeriodical
      ? {
          label: legalPeriodicalEdition.value?.legalPeriodical.abbreviation,
          value: legalPeriodicalEdition.value?.legalPeriodical,
          additionalInformation:
            legalPeriodicalEdition.value?.legalPeriodical.subtitle,
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

async function validateRequiredInput() {
  validationStore.reset()

  const conditaioalGroupFields = ["name", "prefix"]

  legalPeriodicalEdition.value.missingRequiredFields
    .filter((missingField) => !conditaioalGroupFields.includes(missingField))
    .forEach((missingField) =>
      validationStore.add("Pflichtfeld nicht befüllt", missingField),
    )

  const missingConditionalfields =
    legalPeriodicalEdition.value.missingRequiredFields.filter((missingField) =>
      conditaioalGroupFields.includes(missingField),
    )

  // Add validation error only if all fields are missing
  if (missingConditionalfields.length == conditaioalGroupFields.length) {
    missingConditionalfields.forEach((missingField) =>
      validationStore.add("Name oder Präfix sind nicht befüllt", missingField),
    )
  }
}

async function saveEdition() {
  await validateRequiredInput()
  if (validationStore.isValid()) {
    const response = await LegalPeriodicalEditionService.save(
      legalPeriodicalEdition.value as LegalPeriodicalEdition,
    )

    if (response.data)
      await router.replace({
        name: "caselaw-legal-periodical-editions-uuid",
        params: { uuid: response.data.uuid },
      })
  }
}
</script>

<template>
  <div class="flex h-full flex-col space-y-24 bg-gray-100 px-16 py-16">
    <h2 class="ds-heading-03-reg">Neue Periodikaauswertung</h2>
    <InputField
      id="legalPeriodical"
      label="Periodikum *"
      :validation-error="validationStore.getByField('legalPeriodical')"
    >
      <ComboboxInput
        id="legalPeriodical"
        v-model="legalPeriodical"
        aria-label="Periodikum"
        clear-on-choosing-item
        :has-error="false"
        :item-service="ComboboxItemService.getLegalPeriodicals"
      ></ComboboxInput>
    </InputField>

    <div class="flex flex-row items-start gap-24">
      <InputField
        id="prefix"
        label="Präfix"
        :validation-error="validationStore.getByField('prefix')"
      >
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

    <InputField
      id="name"
      label="Name der Ausgabe (optional)"
      :validation-error="validationStore.getByField('name')"
    >
      <TextInput
        id="name"
        v-model="name"
        aria-label="Name der Ausgabe"
        class="ds-input-medium"
        size="medium"
      ></TextInput>
    </InputField>

    <TextButton
      aria-label="Auswertung starten"
      class="ds-button-02-reg"
      label="Auswertung starten"
      @click="saveEdition"
    ></TextButton>
  </div>
</template>
