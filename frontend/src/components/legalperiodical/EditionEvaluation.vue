<script lang="ts" setup>
import { onMounted, ref, computed } from "vue"
import { useRoute } from "vue-router"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextInput from "@/components/input/TextInput.vue"
import LegalPeriodical from "@/domain/legalPeriodical"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import Reference from "@/domain/reference"
import ComboboxItemService from "@/services/comboboxItemService"
import LegalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"

const route = useRoute()
const edition = ref<LegalPeriodicalEdition>(new LegalPeriodicalEdition())
const reference = ref<Reference>(new Reference())

const legalPeriodical = computed({
  get: () =>
    edition?.value?.legalPeriodical
      ? {
          // Todo: fix ts issue, by removing duplicate of LegalPeriodical type
          label: edition?.value?.legalPeriodical.legalPeriodicalAbbreviation,
          value: edition?.value?.legalPeriodical,
        }
      : undefined,
  set: (newValue) => {
    const legalPeriodical = { ...newValue } as LegalPeriodical
    if (newValue) {
      edition.value.legalPeriodical = legalPeriodical
    } else {
      edition.value.legalPeriodical = undefined
    }
  },
})

// Define a function to check if a string matches the UUID pattern
function isUuid(
  value: string,
): value is `${string}-${string}-${string}-${string}-${string}` {
  const uuidRegex =
    /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i
  return uuidRegex.test(value)
}

onMounted(async () => {
  const uuid = route.params.uuid
  if (typeof uuid === "string" && isUuid(uuid)) {
    try {
      const response = await LegalPeriodicalEditionService.get(uuid)
      if (response.data) edition.value = response.data
    } catch (error) {
      console.error("Error fetching edition:", error)
      // Todo: Show error
    }
  } else {
    console.error("Invalid UUID:", uuid)
    // Todo: Handle the error
  }
})
</script>

<template>
  <div class="flex h-full flex-col space-y-24 bg-gray-100 px-16 py-16">
    <h2 class="ds-heading-03-reg">Periodikaauswertung</h2>

    <div v-if="edition" class="flex flex-col gap-24">
      <InputField id="legalPeriodical" label="Periodikum *">
        <ComboboxInput
          id="legalPeriodical"
          v-model="legalPeriodical"
          aria-label="Periodikum"
          clear-on-choosing-item
          :item-service="ComboboxItemService.getLegalPeriodicals"
          read-only
        ></ComboboxInput>
      </InputField>
      <div class="flex flex-col gap-24">
        <div class="flex justify-between gap-24">
          <div class="flex-1">
            <InputField id="citation" label="Zitatstelle *">
              <TextInput
                id="citation"
                v-model="edition.prefix"
                aria-label="Zitatstelle Präfix"
                read-only
                size="medium"
              ></TextInput>
              <TextInput
                id="citation"
                v-model="reference.citation"
                aria-label="Zitatstelle *"
                size="medium"
              ></TextInput>
              <TextInput
                id="citation"
                v-model="edition.suffix"
                aria-label="Zitatstelle Suffix"
                read-only
                size="medium"
              ></TextInput>
            </InputField>
            <span v-if="legalPeriodical" class="ds-label-03-reg"
              >Zitierbeispiel: {{ legalPeriodical.value.citationStyle }}</span
            >
          </div>
          <InputField
            id="citation"
            v-slot="slotProps"
            class="flex-1"
            label="Klammernzusatz"
          >
            <TextInput
              id="citation"
              v-model="reference.referenceSupplement"
              aria-label="Klammernzusatz"
              :has-error="slotProps.hasError"
              size="medium"
            ></TextInput>
          </InputField>
        </div>
      </div>
    </div>
  </div>
</template>
