<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import Reference, { LegalPeriodical } from "@/domain/reference"
import ComboboxItemService from "@/services/comboboxItemService"

const props = defineProps<{
  modelValue?: Reference
  modelValueList?: Reference[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: Reference]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

const reference = ref(new Reference({ ...props.modelValue }))
const lastSavedModelValue = ref(new Reference({ ...props.modelValue }))

const legalPeriodical = computed({
  get: () =>
    reference?.value?.legalPeriodical
      ? {
          label: reference?.value?.legalPeriodical.legalPeriodicalAbbreviation,
          value: reference?.value?.legalPeriodical,
        }
      : undefined,
  set: (newValue) => {
    const legalPeriodical = { ...newValue } as LegalPeriodical
    if (newValue) {
      reference.value.legalPeriodical = legalPeriodical
    } else {
      reference.value.legalPeriodical = undefined
    }
    console.log(reference.value)
  },
})

async function addReference() {
  emit("update:modelValue", reference.value as Reference)
  emit("addEntry")
}

/**
 * This updates the local reference with the updated model value from the props. It also stores a copy of the last saved
 * model value, because the local reference might change in between.
 */
watch(
  () => props.modelValue,
  () => {
    console.log(props.modelValue)
    reference.value = new Reference({ ...props.modelValue })
    lastSavedModelValue.value = new Reference({ ...props.modelValue })
  },
  { immediate: true },
)
</script>

<template>
  <div class="flex flex-col gap-24">
    <InputField id="legalPeriodical" label="Periodikum *">
      <ComboboxInput
        id="legalPeriodical"
        v-model="legalPeriodical"
        aria-label="Periodikum"
        clear-on-choosing-item
        :item-service="ComboboxItemService.getLegalPeriodicals"
      ></ComboboxInput>
    </InputField>
    <div class="flex flex-col gap-24">
      <div class="flex justify-between gap-24">
        <InputField id="citation" label="Zitatstelle *">
          <TextInput
            id="citation"
            v-model="reference.citation"
            aria-label="Zitatstelle"
            size="medium"
          ></TextInput>
        </InputField>
        <InputField id="citation" label="Klammernzusatz *">
          <TextInput
            id="citation"
            v-model="reference.referenceSupplement"
            aria-label="Klammernzusatz"
            size="medium"
          ></TextInput>
        </InputField>
      </div>
    </div>
    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <TextButton
            aria-label="Fundstelle speichern"
            button-type="tertiary"
            :disabled="reference.isEmpty"
            label="Übernehmen"
            size="small"
            @click.stop="addReference"
          />
          <TextButton
            v-if="!lastSavedModelValue.isEmpty"
            aria-label="Abbrechen"
            button-type="ghost"
            label="Abbrechen"
            size="small"
            @click.stop="emit('cancelEdit')"
          />
        </div>
      </div>
      <TextButton
        v-if="!lastSavedModelValue.isEmpty"
        aria-label="Eintrag löschen"
        button-type="destructive"
        label="Eintrag löschen"
        size="small"
        @click.stop="emit('removeEntry', true)"
      />
    </div>
  </div>
</template>
