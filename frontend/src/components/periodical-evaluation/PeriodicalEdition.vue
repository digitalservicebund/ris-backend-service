<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, onBeforeMount, ref } from "vue"
import { useRouter } from "vue-router"
import ComboboxInput from "@/components/ComboboxInput.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import InfoModal from "@/components/InfoModal.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import LegalPeriodical from "@/domain/legalPeriodical"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import ComboboxItemService from "@/services/comboboxItemService"
import { ResponseError } from "@/services/httpClient"
import LegalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"
import { useEditionStore } from "@/stores/editionStore"

const router = useRouter()
const store = useEditionStore()
const { edition } = storeToRefs(store)
const saveEditionError = ref<ResponseError | undefined>()

const nameRef = ref<string>()
const prefixRef = ref<string>()
const suffixRef = ref<string>()
const legalPeriodicalRef = ref<LegalPeriodical | undefined>()

const validationStore =
  useValidationStore<(typeof LegalPeriodicalEdition.fields)[number]>()

const legalPeriodical = computed({
  get: () =>
    legalPeriodicalRef.value
      ? {
          label:
            legalPeriodicalRef.value.abbreviation +
            " | " +
            legalPeriodicalRef.value.title,
          value: edition.value?.legalPeriodical,
          additionalInformation: legalPeriodicalRef.value.subtitle,
        }
      : undefined,
  set: (newValue) => {
    legalPeriodicalRef.value = newValue
      ? ({ ...newValue } as LegalPeriodical)
      : undefined
  },
})

async function validateRequiredInput() {
  validationStore.reset()
  edition.value?.missingRequiredFields.forEach((missingField) =>
    validationStore.add("Pflichtfeld nicht befüllt", missingField),
  )
}

async function saveEdition() {
  validationStore.reset()
  saveEditionError.value = undefined
  await store.loadEdition()
  if (edition.value) {
    edition.value.name = nameRef.value
    edition.value.prefix = prefixRef.value
    edition.value.suffix = suffixRef.value
    edition.value.legalPeriodical = legalPeriodicalRef.value
  }

  await validateRequiredInput()
  if (validationStore.isValid()) {
    const response = await LegalPeriodicalEditionService.save(
      edition.value as LegalPeriodicalEdition,
    )
    if (response.error) {
      saveEditionError.value = response.error
    } else if (response.data) {
      edition.value = new LegalPeriodicalEdition({ ...response.data })
      await router.push({
        name: "caselaw-periodical-evaluation-editionId-references",
        params: { editionId: edition?.value?.id },
        query: {},
      })
    }
  }
}

/**
 * Load the editions values for a local use
 */
onBeforeMount(async () => {
  await store.loadEdition()
  nameRef.value = edition.value?.name
  prefixRef.value = edition.value?.prefix
  suffixRef.value = edition.value?.suffix
  legalPeriodicalRef.value = { ...edition.value?.legalPeriodical }
})
</script>

<template>
  <div class="flex h-full w-full flex-col space-y-24 bg-gray-100 p-24">
    <div v-if="edition" class="mb-24 flex flex-col gap-24 bg-white p-24">
      <TitleElement>Ausgabe</TitleElement>
      <div v-if="saveEditionError">
        <InfoModal
          :description="saveEditionError.description"
          :title="saveEditionError.title"
        />
      </div>
      <InputField
        id="legalPeriodical"
        v-slot="slotProps"
        label="Periodikum *"
        :validation-error="validationStore.getByField('legalPeriodical')"
      >
        <ComboboxInput
          id="legalPeriodical"
          v-model="legalPeriodical"
          aria-label="Periodikum"
          clear-on-choosing-item
          :has-error="slotProps.hasError"
          :item-service="ComboboxItemService.getLegalPeriodicals"
          :read-only="edition?.references?.length! > 0"
        ></ComboboxInput>
      </InputField>
      <InputField
        id="name"
        v-slot="slotProps"
        label="Name der Ausgabe *"
        :validation-error="validationStore.getByField('name')"
      >
        <TextInput
          id="name"
          v-model="nameRef"
          aria-label="Name der Ausgabe"
          class="ds-input-medium"
          :has-error="slotProps.hasError"
          size="medium"
        ></TextInput>
      </InputField>

      <div class="flex-col">
        <div class="flex flex-row items-start gap-24">
          <InputField id="prefix" label="Präfix">
            <TextInput
              id="prefix"
              v-model="prefixRef"
              aria-label="Präfix"
              class="ds-input-medium"
              size="medium"
            ></TextInput>
          </InputField>

          <InputField id="suffix" label="Suffix">
            <TextInput
              id="suffix"
              v-model="suffixRef"
              aria-label="Suffix"
              class="ds-input-medium"
              size="medium"
            ></TextInput>
          </InputField>
        </div>

        <div v-if="legalPeriodicalRef" class="ds-label-03-reg pt-4">
          Zitierbeispiel: {{ legalPeriodicalRef.citationStyle }}
        </div>
      </div>

      <FlexContainer align-items="items-center" class="gap-16">
        <TextButton
          aria-label="Übernehmen und fortfahren"
          class="ds-button-02-reg"
          label="Übernehmen und fortfahren"
          @click="saveEdition"
        ></TextButton>
      </FlexContainer>
    </div>
  </div>
</template>
