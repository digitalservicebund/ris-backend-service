<script lang="ts" setup>
import Button from "primevue/button"
import { computed, ref, watch } from "vue"
import { ValidationError } from "./input/types"
import ComboboxInput from "@/components/ComboboxInput.vue"
import InputField from "@/components/input/InputField.vue"
import SingleNormInput from "@/components/SingleNormInput.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import NonApplicationNorm from "@/domain/nonApplicationNorm"
import { NormAbbreviation } from "@/domain/normAbbreviation"
import SingleNorm from "@/domain/singleNorm"
import ComboboxItemService from "@/services/comboboxItemService"
import IconAdd from "~icons/ic/baseline-add"

const props = defineProps<{
  modelValue?: NonApplicationNorm
  modelValueList?: NonApplicationNorm[]
}>()
const emit = defineEmits<{
  "update:modelValue": [value: NonApplicationNorm]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [void]
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

const norm = ref(new NonApplicationNorm({ ...props.modelValue }))
const lastSavedModelValue = ref(new NonApplicationNorm({ ...props.modelValue }))

const singleNorms = ref(
  props.modelValue?.singleNorms
    ? props.modelValue?.singleNorms?.map((norm) => new SingleNorm({ ...norm }))
    : ([] as SingleNorm[]),
)

/**
 * Data restructuring from norm abbreviation props to combobox item. When item in combobox set, it is validated
 * against already existing norm abbreviations in the list.
 */
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
        (nonApplicationNorm) =>
          nonApplicationNorm.normAbbreviation?.abbreviation ===
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

/**
 * If there are no format validation errors, the new non-application norm is emitted
 * to the parent and a new emtpy entry is automatically added to the list
 */
async function addNonApplicationNorm() {
  if (
    !validationStore.getByField("singleNorm") &&
    !validationStore.getByField("dateOfVersion") &&
    !validationStore.getByField("dateOfRelevance") &&
    !validationStore.getByMessage("RIS-Abkürzung bereits eingegeben").length
  ) {
    const nonApplicationNorm = new NonApplicationNorm({
      ...norm.value,
      singleNorms: singleNorms.value.filter(
        (singleNorm) => singleNorm !== null,
      ) as SingleNorm[],
    })
    emit("update:modelValue", nonApplicationNorm)
    emit("addEntry")
  }
}

/**
 * Emits to the editable list to removes the current norm reference and empties the local single norm list. The truthy
 * boolean value indicates, that the edit index should be resetted to undefined, ergo show all list items in summary mode.
 */
function removeNonApplicationNorm() {
  singleNorms.value = []
  norm.value.normAbbreviation = undefined
  emit("removeEntry")
}

/**
 * Adds a new single norm entry to the local single norms list.
 */
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
  if (new NonApplicationNorm({ ...props.modelValue }).isEmpty) {
    removeNonApplicationNorm()
    addSingleNormEntry()
  }
  emit("cancelEdit")
}

/**
 * The child components emit format validations, this function updates the local validation store accordingly in order to
 * prevent the norm reference input from being saved with validation errors
 * @param validationError A validation error has either a message and an instance field or is undefined
 * @param field If the validation error is undefined, the validation store for this field needs to be resetted
 */
function updateFormatValidation(
  validationError: ValidationError | undefined,
  field?: string,
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

/**
 * This updates the local norm with the updated model value from the props. It also stores a copy of the last saved
 * model value, because the local norm might change in between. When the new model value is empty, all validation
 * errors are resetted. When the list of single norms is empty, a new empty single norm entry is added.
 */
watch(
  () => props.modelValue,
  () => {
    norm.value = new NonApplicationNorm({ ...props.modelValue })
    lastSavedModelValue.value = new NonApplicationNorm({ ...props.modelValue })
    if (lastSavedModelValue.value.isEmpty) {
      validationStore.reset()
    }
    if (singleNorms.value?.length == 0 || !singleNorms.value)
      addSingleNormEntry()
  },
  { immediate: true, deep: true },
)
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
        @focus="validationStore.remove('normAbbreviation')"
      ></ComboboxInput>
    </InputField>
    <div v-if="normAbbreviation">
      <SingleNormInput
        v-for="(_, index) in singleNorms"
        :key="index"
        v-model="singleNorms[index] as SingleNorm"
        aria-label="Einzelnorm"
        :index="index"
        norm-abbreviation="normAbbreviation.abbreviation"
        @remove-entry="removeSingleNormEntry(index)"
        @update:validation-error="
          (validationError: ValidationError | undefined, field?: string) =>
            updateFormatValidation(validationError, field)
        "
      />
      <div class="flex w-full flex-row justify-between">
        <div>
          <div class="flex gap-24">
            <Button
              aria-label="Weitere Einzelnorm"
              label="Weitere Einzelnorm"
              severity="secondary"
              size="small"
              @click.stop="addSingleNormEntry"
              ><template #icon> <IconAdd /> </template
            ></Button>
            <Button
              aria-label="Norm speichern"
              label="Übernehmen"
              size="small"
              @click.stop="addNonApplicationNorm"
            ></Button>
            <Button
              aria-label="Abbrechen"
              label="Abbrechen"
              size="small"
              text
              @click.stop="cancelEdit"
            ></Button>
          </div>
        </div>
        <Button
          v-if="!lastSavedModelValue.isEmpty"
          aria-label="Eintrag löschen"
          label="Eintrag löschen"
          severity="danger"
          size="small"
          @click.stop="removeNonApplicationNorm"
        ></Button>
      </div>
    </div>
  </div>
</template>
