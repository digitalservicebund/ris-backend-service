<script lang="ts" setup>
import { useRouter } from "vue-router"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import ComboboxItemService from "@/services/comboboxItemService"

const router = useRouter()
</script>

<template>
  <div class="flex h-full flex-col space-y-24 bg-gray-100 px-16 py-16">
    <h1 class="ds-heading-02-reg">Periodikaauswertung</h1>
    <div class="flex flex-row items-end gap-24">
      <div class="flex-grow" role="search">
        <InputField id="legalPeriodical" label="Periodikum *">
          <ComboboxInput
            id="legalPeriodical"
            aria-label="Periodikum"
            clear-on-choosing-item
            :has-error="false"
            :item-service="ComboboxItemService.getLegalPeriodicals"
          ></ComboboxInput>
        </InputField>
      </div>
      <TextButton
        class="ds-button-02-reg"
        label="Neue Auswertung"
        @click="
          router.push({ name: 'caselaw-legal-periodical-evaluation-new' })
        "
      ></TextButton>
    </div>
  </div>
</template>
