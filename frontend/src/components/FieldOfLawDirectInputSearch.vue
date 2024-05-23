<script lang="ts" setup>
import { ref, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import { FieldOfLawNode } from "@/domain/fieldOfLaw"
import ComboboxItemService from "@/services/comboboxItemService"

const emit = defineEmits<{
  "add-to-list": [item: FieldOfLawNode]
}>()
const fieldOfLawNode = ref()

watch(fieldOfLawNode, () => {
  emit("add-to-list", fieldOfLawNode.value as FieldOfLawNode)
})
</script>

<template>
  <p class="heading-04-regular pb-8 pt-24">Direkteingabe Sachgebiet</p>
  <div class="flex w-1/3 flex-col">
    <div class="flex flex-row items-stretch">
      <div class="grow">
        <ComboboxInput
          id="directInputCombobox"
          v-model="fieldOfLawNode"
          aria-label="Direkteingabe-Sachgebietssuche eingeben"
          clear-on-choosing-item
          :item-service="ComboboxItemService.getFieldOfLawSearchByIdentifier"
          placeholder="Sachgebiet"
        >
        </ComboboxInput>
      </div>
    </div>
  </div>
</template>
