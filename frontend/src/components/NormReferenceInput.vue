<script lang="ts" setup>
import { computed, onBeforeUnmount, ref, watch } from "vue"
import { ValidationError } from "./input/types"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import SingleNormInput from "@/components/SingleNormInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import LegalForce from "@/domain/legalForce"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import NormReference from "@/domain/normReference"
import SingleNorm from "@/domain/singleNorm"
import ComboboxItemService from "@/services/comboboxItemService"
import IconAdd from "~icons/ic/baseline-add"

const props = defineProps<{
  modelValue?: NormReference
  modelValueList?: NormReference[]
}>()
const emit = defineEmits<{
  "update:modelValue": [value: NormReference]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

const validationStore =
  useValidationStore<
    [
      "normAbbreviation",
      "singleNorm",
      "dateOfVersion",
      "dateOfRelevance",
    ][number]
  >()

const norm = ref(new NormReference({ ...props.modelValue }))
const lastSavedModelValue = ref(new NormReference({ ...props.modelValue }))

const singleNorms = ref(
  props.modelValue?.singleNorms
    ? props.modelValue?.singleNorms?.map((norm) => new SingleNorm({ ...norm }))
    : ([] as SingleNorm[]),
)

const normAbbreviation = computed({
  get: () =>
    norm.value.normAbbreviation
      ? {
          label: norm.value.normAbbreviation.abbreviation,
          value: norm.value.normAbbreviation,
          additionalInformation: norm.value.normAbbreviation.officialLongTitle,
        }
      : undefined,
  set: (newValue) => {
    const newNormAbbreviation = { ...newValue } as NormAbbreviation
    if (newValue) {
      validationStore.remove("normAbbreviation")
      // Check if newValue.abbreviation is already in singleNorms
      const isAbbreviationPresent = props.modelValueList?.some(
        (norm) =>
          norm.normAbbreviation?.abbreviation ===
          newNormAbbreviation.abbreviation,
      )
      if (isAbbreviationPresent) {
        validationStore.add(
          "RIS-Abkürzung bereits eingegeben",
          "normAbbreviation",
        )
      } else {
        norm.value.normAbbreviation = newNormAbbreviation
      }
    }
  },
})

async function addNormReference() {
  if (
    !validationStore.getByField("singleNorm") &&
    !validationStore.getByField("dateOfVersion") &&
    !validationStore.getByField("dateOfRelevance") &&
    !validationStore.getByMessage("RIS-Abkürzung bereits eingegeben").length
  ) {
    const normRef = new NormReference({
      ...norm.value,
      singleNorms: singleNorms.value
        .map((norm) =>
          !norm.isEmpty
            ? new SingleNorm({
                ...norm,
                legalForce: norm.legalForce
                  ? new LegalForce({ ...norm.legalForce })
                  : undefined,
              })
            : null,
        )
        .filter((norm) => norm !== null) as SingleNorm[],
    })
    emit("update:modelValue", normRef)
    emit("addEntry")
  }
}

async function removeNormReference() {
  emit("removeEntry", true)
  singleNorms.value = []
}

function addSingleNormEntry() {
  singleNorms.value.push(new SingleNorm())
}

/**
 * Removes the single norm entry, with the given index.
 * @param {number} index - The index of the list item to be removed
 */
function removeSingleNormEntry(index: number) {
  singleNorms.value.splice(index, 1)
}

function cancelEdit() {
  if (new NormReference({ ...props.modelValue }).isEmpty) {
    emit("removeEntry", true)
    singleNorms.value = []
  }
  emit("cancelEdit")
}

function updateFormatValidation(
  validationError: ValidationError | undefined,
  field: string,
) {
  if (validationError) {
    validationStore.add(
      validationError.message,
      validationError.instance as [
        "singleNorm",
        "dateOfVersion",
        "dateOfRelevance",
      ][number],
    )
  } else {
    validationStore.remove(
      field as ["singleNorm", "dateOfVersion", "dateOfRelevance"][number],
    )
  }
}

watch(
  () => props.modelValue,
  () => {
    norm.value = new NormReference({ ...props.modelValue })
    lastSavedModelValue.value = new NormReference({ ...props.modelValue })
    if (lastSavedModelValue.value.isEmpty) {
      validationStore.reset()
    } else if (lastSavedModelValue.value.hasAmbiguousNormReference) {
      validationStore.add("Mehrdeutiger Verweis", "normAbbreviation")
    }
    //when list is empty, add new empty single norm entry
    if (singleNorms.value?.length == 0 || !singleNorms.value)
      addSingleNormEntry()
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
        placeholder="Abkürzung, Kurz-oder Langtitel oder Region eingeben..."
        @input="validationStore.remove('normAbbreviation')"
      ></ComboboxInput>
    </InputField>
    <div v-if="normAbbreviation || norm.normAbbreviationRawValue">
      <SingleNormInput
        v-for="(_, index) in singleNorms"
        :key="index"
        v-model="singleNorms[index] as SingleNorm"
        aria-label="Einzelnorm"
        norm-abbreviation="normAbbreviation.abbreviation"
        @remove-entry="removeSingleNormEntry(index)"
        @update:validation-error="
          (validationError, field) =>
            updateFormatValidation(validationError, field)
        "
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
              @click.stop="addSingleNormEntry"
            />
            <TextButton
              aria-label="Norm speichern"
              button-type="primary"
              label="Übernehmen"
              size="small"
              @click.stop="addNormReference"
            />
            <TextButton
              aria-label="Abbrechen"
              button-type="ghost"
              label="Abbrechen"
              size="small"
              @click.stop="cancelEdit"
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
