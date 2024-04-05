<script lang="ts" setup>
import { computed, onBeforeUnmount, ref, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import SingleNormInput from "@/components/SingleNormInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import NormReference from "@/domain/normReference"
import SingleNorm from "@/domain/singleNorm"
import ComboboxItemService from "@/services/comboboxItemService"
import IconAdd from "~icons/ic/baseline-add"

const props = defineProps<{ modelValue?: NormReference }>()
const emit = defineEmits<{
  "update:modelValue": [value: NormReference]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [void]
}>()

const validationStore =
  useValidationStore<(typeof NormReference.fields)[number]>()

const norm = ref(new NormReference({ ...props.modelValue }))
const singleNorms = ref(
  props.modelValue?.singleNorms
    ? props.modelValue.singleNorms
    : ([] as SingleNorm[]),
)
const lastSavedModelValue = ref(new NormReference({ ...props.modelValue }))

const normAbbreviation = computed({
  get: () =>
    props.modelValue?.normAbbreviation
      ? {
          label: props.modelValue.normAbbreviation.abbreviation,
          value: norm.value.normAbbreviation,
          additionalInformation:
            props.modelValue.normAbbreviation.officialLongTitle,
        }
      : undefined,
  set: (newValue) => {
    const newNormAbbreviation = { ...newValue } as NormAbbreviation
    const normRef = new NormReference({
      normAbbreviation: newNormAbbreviation,
    })
    emit("update:modelValue", normRef)
  },
})

async function addNormReference() {
  if (!validationStore.getByMessage("Inhalt nicht valide").length) {
    const normRef = new NormReference({
      ...norm.value,
      singleNorms: singleNorms.value.map((norm) => new SingleNorm({ ...norm })),
    })
    emit("update:modelValue", normRef)
    emit("addEntry")
  }
}

function addNewSingleNormEntry() {
  singleNorms.value.push(new SingleNorm())
}

async function removeNormReference() {
  emit("removeEntry")
  singleNorms.value = []
}

watch(
  () => props.modelValue,
  () => {
    norm.value = new NormReference({ ...props.modelValue })
    lastSavedModelValue.value = new NormReference({ ...props.modelValue })
    if (lastSavedModelValue.value.isEmpty) validationStore.reset()
    //when list is empty, add new emptry single norm entry
    if (singleNorms.value?.length == 0 || !singleNorms.value)
      addNewSingleNormEntry()
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  if (norm.value.isEmpty) emit("removeEntry")
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <InputField
      id="norm-reference-abbreviation-field"
      v-slot="slotProps"
      label="RIS-Abkürzung *"
      :validation-error="validationStore.getByField('normAbbreviation')"
    >
      <ComboboxInput
        id="norm-reference-abbreviation"
        v-model="normAbbreviation"
        aria-label="RIS-Abkürzung"
        :has-error="slotProps.hasError"
        :item-service="ComboboxItemService.getRisAbbreviations"
        no-clear
        placeholder="Abkürzung, Kurz-oder Langtitel oder Region eingeben ..."
        @click="validationStore.remove('normAbbreviation')"
      ></ComboboxInput>
    </InputField>
    <div v-if="normAbbreviation">
      <SingleNormInput
        v-for="(entry, index) in singleNorms"
        :key="index"
        v-model="singleNorms[index] as SingleNorm"
        norm-abbreviation="normAbbreviation.abbreviation"
      />

      <div class="flex w-full flex-row justify-between">
        <div>
          <div class="flex gap-24">
            <TextButton
              aria-label="Weitere Einzelnorm"
              button-type="tertiary"
              :icon="IconAdd"
              label="Weitere Einzelnorm"
              size="small"
              @click.stop="addNewSingleNormEntry"
            />
            <TextButton
              aria-label="Norm speichern"
              button-type="primary"
              label="Übernehmen"
              size="small"
              @click.stop="addNormReference"
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
          @click.stop="removeNormReference"
        />
      </div>
    </div>
  </div>
</template>
