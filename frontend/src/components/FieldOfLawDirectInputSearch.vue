<script lang="ts" setup>
import ComboboxInput from "@/components/ComboboxInput.vue"
import TextButton from "@/components/TextButton.vue"
import ComboboxItemService from "@/services/comboboxItemService"
</script>

<template>
  <h1 class="heading-03-regular pb-8">Direkteingabe</h1>
  <div class="flex flex-col">
    <div class="pb-28">
      <div class="flex flex-row items-stretch">
        <div class="grow">
          <ComboboxInput
            id="directInputCombobox"
            aria-label="Direkteingabe-Sachgebietssuche eingeben"
            :item-service="ComboboxItemService.getFieldOfLawSearchByIdentifier"
          >
          </ComboboxInput>
        </div>
        <div class="pl-8">
          <TextButton
            aria-label="Direkteingabe-Sachgebietssuche ausführen"
            button-type="secondary"
            class="w-fit"
            label="Hinzufügen"
          />
        </div>
      </div>
    </div>
  </div>
</template>
