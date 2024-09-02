<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import { useRouter } from "vue-router"
import ComboboxInput from "@/components/ComboboxInput.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import LegalPeriodical from "@/domain/legalPeriodical"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import ComboboxItemService from "@/services/comboboxItemService"
import LegalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"
import { useEditionStore } from "@/stores/editionStore"

const router = useRouter()
const store = useEditionStore()
const legalPeriodicalEdition = ref<LegalPeriodicalEdition>(
  store.edition
    ? (store.edition as LegalPeriodicalEdition)
    : new LegalPeriodicalEdition(),
)

const legalPeriodicalIsEditionIsEmpty = ref(false)

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

async function validateRequiredInput() {
  validationStore.reset()

  legalPeriodicalEdition.value.missingRequiredFields.forEach((missingField) =>
    validationStore.add("Pflichtfeld nicht befüllt", missingField),
  )
}

async function saveEdition() {
  await validateRequiredInput()
  if (validationStore.isValid()) {
    const response = await LegalPeriodicalEditionService.save(
      legalPeriodicalEdition.value as LegalPeriodicalEdition,
    )

    if (response.data)
      await router.replace({
        name: "caselaw-periodical-evaluation-uuid",
        params: { uuid: response.data.id },
      })
  }
}

function reset() {
  legalPeriodicalEdition.value = new LegalPeriodicalEdition()
}

watch(
  legalPeriodicalEdition,
  () => {
    legalPeriodicalIsEditionIsEmpty.value = legalPeriodicalEdition.value.isEmpty
  },
  { deep: true },
)
</script>

<template>
  <div class="flex h-full w-full flex-col space-y-24 bg-gray-100 px-16 py-16">
    <h1 class="ds-heading-02-reg">Ausgabe</h1>
    <div class="mb-24 flex flex-col gap-24 bg-white p-32">
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

    <div class="flex-col">
      <div class="flex flex-row items-start gap-24">
        <InputField id="prefix" label="Präfix">
            <TextInput
              id="prefix"
              v-model="legalPeriodicalEdition.prefix"
              aria-label="Präfix"
              class="ds-input-medium"
              size="medium"
            ></TextInput>
          </InputField>

          <InputField id="suffix" label="Suffix">
            <TextInput
              id="suffix"
              v-model="legalPeriodicalEdition.suffix"
              aria-label="Suffix"
              class="ds-input-medium"
              size="medium"
            ></TextInput>
          </InputField>
        </div>

        <div v-if="legalPeriodical" class="ds-label-03-reg pt-4">
          Zitierbeispiel: {{ legalPeriodical.value.citationStyle }}
        </div>
      </div>
    </div>
    <InputField
      id="name"
      label="Name der Ausgabe"
      :validation-error="validationStore.getByField('name')"
    >
      <TextInput
        id="name"
        v-model="legalPeriodicalEdition.name"
        aria-label="Name der Ausgabe"
        class="ds-input-medium"
        size="medium"
      ></TextInput>
    </InputField>

    <FlexContainer align-items="items-center">
      <TextButton
        aria-label="Auswertung starten"
        class="ds-button-02-reg"
        label="Auswertung starten"
        @click="saveEdition"
      ></TextButton>

      <TextButton
        v-if="legalPeriodicalIsEditionIsEmpty"
        aria-label="Zurücksetzen"
        button-type="ghost"
        class="ml-8"
        label="Zurücksetzen"
        size="medium"
        @click="reset"
      />
    </FlexContainer>
  </div>
</template>
