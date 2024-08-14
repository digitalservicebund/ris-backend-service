<script lang="ts" setup>
import { ref, computed } from "vue"
import { useRouter } from "vue-router"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import { LegalPeriodical } from "@/domain/reference"
import ComboboxItemService from "@/services/comboboxItemService"

const router = useRouter()
const filter = ref<LegalPeriodical>()

const legalPeriodical = computed({
  get: () =>
    filter.value
      ? {
          label: filter.value.legalPeriodicalAbbreviation,
          value: filter.value,
        }
      : undefined,
  set: (newValue) => {
    const legalPeriodical = { ...newValue } as LegalPeriodical
    if (newValue) {
      filter.value = legalPeriodical
    } else {
      filter.value = undefined
    }
  },
})
</script>

<template>
  <div class="flex flex-col gap-16 p-16">
    <div class="flex justify-between">
      <h1 class="ds-heading-02-reg">Periodika</h1>
      <TextButton
        aria-label="Neue Periodikaauswertung"
        class="ds-button-02-reg"
        label="Neue Periodikaauswertung"
        @click="router.push({ name: 'caselaw-legal-periodical-editions-new' })"
      ></TextButton>
    </div>
    <div class="flex h-full flex-col space-y-24 bg-gray-100 px-16 py-16">
      <div class="flex flex-row items-end gap-24">
        <div class="flex-grow" role="search">
          <InputField id="legalPeriodical" label="Periodikum suchen">
            <ComboboxInput
              id="legalPeriodical"
              v-model="legalPeriodical"
              aria-label="Periodikum"
              clear-on-choosing-item
              :has-error="false"
              :item-service="ComboboxItemService.getLegalPeriodicals"
            ></ComboboxInput>
          </InputField>
        </div>
      </div>
    </div>
  </div>
</template>
