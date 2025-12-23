<script lang="ts" setup>
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import Message from "primevue/message"
import { computed, onBeforeMount, ref } from "vue"
import { useRouter } from "vue-router"
import ComboboxInput from "@/components/ComboboxInput.vue"
import FlexContainer from "@/components/FlexContainer.vue"
import InputField from "@/components/input/InputField.vue"
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

const editionRef = ref<LegalPeriodicalEdition | undefined>(undefined)
const validationStore =
  useValidationStore<(typeof LegalPeriodicalEdition.fields)[number]>()

const legalPeriodical = computed({
  get: () =>
    editionRef.value?.legalPeriodical
      ? {
          label:
            editionRef.value?.legalPeriodical.abbreviation +
            " | " +
            editionRef.value?.legalPeriodical.title,
          value: editionRef.value?.legalPeriodical,
          additionalInformation: editionRef.value?.legalPeriodical.subtitle,
        }
      : undefined,
  set: (newValue) => {
    if (editionRef.value) {
      editionRef.value.legalPeriodical = newValue
        ? ({ ...newValue } as LegalPeriodical)
        : undefined
    }
  },
})

async function validateRequiredInput() {
  validationStore.reset()
  editionRef.value?.missingRequiredFields.forEach((missingField) =>
    validationStore.add("Pflichtfeld nicht befüllt", missingField),
  )
}

async function saveEdition() {
  validationStore.reset()
  saveEditionError.value = undefined

  await validateRequiredInput()
  if (validationStore.isValid() && editionRef.value) {
    editionRef.value.references = edition.value?.references
    const response = await LegalPeriodicalEditionService.save(
      editionRef.value as LegalPeriodicalEdition,
    )

    if (response.data) {
      store.edition = new LegalPeriodicalEdition({
        ...response.data,
      })
      editionRef.value = { ...store.edition }
      await router.push({
        name: "caselaw-periodical-evaluation-editionId-references",
        params: { editionId: editionRef?.value?.id },
        query: {},
      })
    } else if (response.error) {
      saveEditionError.value = response.error
    }
  }
}

/**
 * Load the editions values for a local use
 */
onBeforeMount(async () => {
  editionRef.value = new LegalPeriodicalEdition({ ...edition.value })
})
</script>

<template>
  <div class="flex h-full w-full flex-col space-y-24 bg-gray-100 p-24">
    <div v-if="editionRef" class="mb-24 flex flex-col gap-24 bg-white p-24">
      <TitleElement>Ausgabe</TitleElement>
      <div v-if="saveEditionError">
        <Message severity="error">
          <p class="ris-body1-bold">{{ saveEditionError.title }}</p>
          <p>{{ saveEditionError.description }}</p>
        </Message>
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
          :has-error="slotProps.hasError"
          :item-service="ComboboxItemService.getLegalPeriodicals"
          :read-only="editionRef?.references?.length! > 0"
        ></ComboboxInput>
      </InputField>
      <InputField
        id="name"
        v-slot="slotProps"
        label="Name der Ausgabe *"
        :validation-error="validationStore.getByField('name')"
      >
        <InputText
          id="name"
          v-model="editionRef.name"
          aria-label="Name der Ausgabe"
          fluid
          :invalid="slotProps.hasError"
          size="small"
        ></InputText>
      </InputField>

      <div class="flex-col">
        <div class="flex flex-row items-start gap-24">
          <InputField id="prefix" label="Präfix">
            <InputText
              id="prefix"
              v-model="editionRef.prefix"
              aria-label="Präfix"
              fluid
              size="small"
            ></InputText>
          </InputField>

          <InputField id="suffix" label="Suffix">
            <InputText
              id="suffix"
              v-model="editionRef.suffix"
              aria-label="Suffix"
              fluid
              size="small"
            ></InputText>
          </InputField>
        </div>

        <div v-if="editionRef.legalPeriodical" class="ris-label3-regular pt-4">
          Zitierbeispiel: {{ editionRef.legalPeriodical.citationStyle }}
        </div>
      </div>

      <FlexContainer align-items="items-center" class="gap-16">
        <Button
          aria-label="Übernehmen und fortfahren"
          label="Übernehmen und fortfahren"
          @click="saveEdition"
        ></Button>
      </FlexContainer>
    </div>
  </div>
</template>
