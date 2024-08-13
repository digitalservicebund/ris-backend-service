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
    <div class="flex flex-row justify-between gap-24">
      <h1 class="ds-heading-02-reg">Fundstellen</h1>
      <TextButton
        class="ds-button-02-reg"
        label="Neue Periodikaauswertung"
        @click="
          router.push({ name: 'caselaw-legal-periodical-references-new' })
        "
      ></TextButton>
    </div>

    <div class="flex flex-row items-end gap-24">
      <div class="flex-grow" role="search">
        <InputField id="legalPeriodical" label="Periodikum suchen">
          <ComboboxInput
            id="legalPeriodical"
            aria-label="Periodikum"
            clear-on-choosing-item
            :has-error="false"
            :item-service="ComboboxItemService.getLegalPeriodicals"
          ></ComboboxInput>
        </InputField>
      </div>
    </div>
  </div>
</template>
