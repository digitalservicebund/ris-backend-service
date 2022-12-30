<script lang="ts" setup>
import { ref } from "vue"
import DropdownInput from "@/components/DropdownInput.vue"
import type { DropdownItem } from "@/domain/types"

const comboboxModelValue1 = ref<string>()
const comboboxModelValue2 = ref<string>("I1")

const dropdownItems: DropdownItem[] = [
  { label: "Dropdown item 1", value: "I1" },
  { label: "Dropdown item 2", value: "I2" },
  { label: "Dropdown item 3", value: "I3" },
]
</script>

<template>
  <div class="flex flex-col gap-y-20 h-auto w-1/2">
    <h1 class="font-bold text-24">Dropdown Input ohne Vorauswahl</h1>
    <div class="pb-4">
      <DropdownInput
        id="comboboxInput"
        aria-label="combobox input"
        :items="dropdownItems"
        :model-value="comboboxModelValue1"
        placeholder="Bitte auswählen"
        :value="comboboxModelValue1"
      />
    </div>
    <h1 class="font-bold text-24">Dropdown Input mit Vorauswahl</h1>
    <div class="pb-4">
      <DropdownInput
        id="comboboxInput"
        aria-label="combobox input"
        :items="dropdownItems"
        :model-value="comboboxModelValue2"
        placeholder="Bitte auswählen"
      />
    </div>
  </div>
</template>
