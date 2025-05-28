<script lang="ts" setup>
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import { computed, ref, watch } from "vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField from "@/components/input/InputField.vue"
import { ValidationError } from "@/components/input/types"
import useQuery, { Query } from "@/composables/useQueryFromRoute"
import { useValidationStore } from "@/composables/useValidationStore"
import { DocumentUnitSearchParameter } from "@/domain/documentUnit"

defineProps<{
  isLoading?: boolean
}>()

const emit = defineEmits<{
  search: [value: Query<DocumentUnitSearchParameter>]
  resetSearchResults: [void]
}>()

const validationStore = useValidationStore<DocumentUnitSearchParameter>()
const { route, getQueryFromRoute, pushQueryToRoute } = useQuery()
const query = ref(getQueryFromRoute())

const isEmptySearch = computed(() => {
  return Object.keys(query.value).length === 0
})

const submitButtonError = ref()

function resetSearch() {
  validationStore.reset()
  submitButtonError.value = undefined
  query.value = {}
  pushQueryToRoute(query.value)
  emit("resetSearchResults")
}

function resetErrors(id?: DocumentUnitSearchParameter) {
  if (id) validationStore.remove(id)
  submitButtonError.value = undefined
}

function isSearchInputInvalid() {
  if (hasValidationErrors()) {
    submitButtonError.value = "Fehler in Suchkriterien"
    return true
  }

  return false
}

function hasValidationErrors() {
  return validationStore.getAll().length > 0
}

function validateSearchInput() {
  //Startdatum fehlt
  if (
    query.value?.decisionDateEnd &&
    !query.value?.decisionDate &&
    !validationStore.getByField("decisionDate")
  ) {
    validationStore.add("Startdatum fehlt", "decisionDate")
  } else if (
    !query.value.decisionDateEnd &&
    validationStore.getByMessage("Startdatum fehlt").length === 1
  ) {
    validationStore.remove("decisionDate")
  }

  //Enddatum darf nicht vor Startdatum liegen
  if (
    query.value?.decisionDateEnd &&
    query.value?.decisionDate &&
    new Date(query.value.decisionDate) > new Date(query.value.decisionDateEnd)
  ) {
    if (!validationStore.getByField("decisionDateEnd")) {
      validationStore.add(
        "Enddatum darf nicht vor Startdatum liegen",
        "decisionDateEnd",
      )
    }
  } else if (
    validationStore.getByMessage("Enddatum darf nicht vor Startdatum liegen")
      .length === 1
  ) {
    validationStore.remove("decisionDateEnd")
  }
}

function handleLocalInputError(error: ValidationError | undefined, id: string) {
  if (error) {
    validationStore.add(
      error.message,
      error.instance as DocumentUnitSearchParameter,
    )
  } else validationStore.remove(id as DocumentUnitSearchParameter)
  validateSearchInput()
}

/**
 * Checks if the current search query is identical to the previous query.
 * @returns {boolean} - `true` if the current query matches the previous query, otherwise `false`.
 */
function isIdenticalSearch(): boolean {
  const previousQuery = getQueryFromRoute()
  const newQuery = query.value
  return JSON.stringify(previousQuery) === JSON.stringify(newQuery)
}

function handleSearchButtonClicked() {
  validateSearchInput()

  if (isSearchInputInvalid()) {
    return
  }

  if (isIdenticalSearch()) {
    handleSearch()
  }
  pushQueryToRoute(query.value)
}

function handleSearch() {
  emit("search", getQueryFromRoute())
}

watch(
  route,
  () => {
    query.value = getQueryFromRoute()
    handleSearch()
  },
  { deep: true, immediate: true },
)
</script>

<template>
  <div
    v-ctrl-enter="handleSearchButtonClicked"
    class="flex flex-col bg-blue-200"
  >
    <div
      class="m-40 grid grid-cols-[100px_1fr_200px_1fr] grid-rows-[auto_auto_auto] gap-x-12 gap-y-20 [grid-template-areas:'az-label_az-input_docnumber-label_docnumber-input''court-label_court-input_date-label_date-input''._._._search-button'] lg:gap-x-32"
    >
      <div
        class="ris-body1-regular ml-3 flex flex-row items-center [grid-area:az-label]"
      >
        Aktenzeichen
      </div>
      <div
        class="ris-body1-regular flex flex-row items-center [grid-area:court-label]"
      >
        Gericht
      </div>

      <div class="[grid-area:az-input]">
        <InputField
          id="fileNumber"
          v-slot="{ id }"
          label="Aktenzeichen"
          visually-hide-label
        >
          <InputText
            :id="id"
            v-model="query.fileNumber"
            aria-label="Aktenzeichen Suche"
            fluid
            size="small"
            @focus="resetErrors(id as DocumentUnitSearchParameter)"
          ></InputText>
        </InputField>
      </div>
      <div class="flex flex-row gap-10 [grid-area:court-input]">
        <InputField
          id="courtType"
          v-slot="{ id }"
          label="Gerichtstyp"
          visually-hide-label
        >
          <InputText
            :id="id"
            v-model="query.courtType"
            aria-label="Gerichtstyp Suche"
            fluid
            placeholder="Gerichtstyp"
            size="small"
            @focus="resetErrors(id as DocumentUnitSearchParameter)"
          ></InputText>
        </InputField>
        <span class="pt-6">-</span>
        <InputField
          id="courtLocation"
          v-slot="{ id }"
          label="Gerichtsort"
          visually-hide-label
        >
          <InputText
            :id="id"
            v-model="query.courtLocation"
            aria-label="Gerichtsort Suche"
            fluid
            placeholder="Ort"
            size="small"
            @focus="resetErrors(id as DocumentUnitSearchParameter)"
          ></InputText>
        </InputField>
      </div>
      <div
        class="ris-body1-regular flex flex-row items-center pl-24 [grid-area:docnumber-label] lg:pl-48"
      >
        Dokumentnummer
      </div>
      <div
        class="ris-body1-regular flex flex-row items-center pl-24 [grid-area:date-label] lg:pl-48"
      >
        Datum
      </div>
      <div class="[grid-area:docnumber-input]">
        <InputField
          id="documentNumber"
          v-slot="{ id }"
          label="Dokumentnummer"
          visually-hide-label
        >
          <InputText
            :id="id"
            v-model="query.documentNumber"
            aria-label="Dokumentnummer Suche"
            fluid
            size="small"
            @focus="resetErrors(id as DocumentUnitSearchParameter)"
          ></InputText>
        </InputField>
      </div>
      <div class="flex flex-row gap-10 [grid-area:date-input]">
        <InputField
          id="decisionDate"
          v-slot="{ id, hasError }"
          data-testid="decision-date-input"
          label="Entscheidungsdatum"
          :validation-error="validationStore.getByField('decisionDate')"
          visually-hide-label
        >
          <DateInput
            :id="id"
            v-model="query.decisionDate"
            aria-label="Entscheidungsdatum Suche"
            :has-error="hasError"
            @blur="validateSearchInput"
            @focus="resetErrors(id as DocumentUnitSearchParameter)"
            @update:validation-error="
              (validationError: ValidationError | undefined) =>
                handleLocalInputError(validationError, id)
            "
          ></DateInput>
        </InputField>
        <span class="pt-6">-</span>
        <InputField
          id="decisionDateEnd"
          v-slot="{ id, hasError }"
          data-testid="decision-date-end-input"
          label="Entscheidungsdatum Ende"
          :validation-error="validationStore.getByField('decisionDateEnd')"
          visually-hide-label
        >
          <DateInput
            :id="id"
            v-model="query.decisionDateEnd"
            aria-label="Entscheidungsdatum Suche Ende"
            :has-error="hasError"
            placeholder="TT.MM.JJJJ (optional)"
            @blur="validateSearchInput"
            @focus="resetErrors(id as DocumentUnitSearchParameter)"
            @update:validation-error="
              (validationError: ValidationError | undefined) =>
                handleLocalInputError(validationError, id)
            "
          ></DateInput>
        </InputField>
      </div>

      <div class="flex flex-row [grid-area:search-button]">
        <div class="flex flex-col gap-8">
          <Button
            aria-label="Nach Dokumentationseinheiten suchen"
            class="self-start"
            label="Ergebnisse anzeigen"
            :loading="isLoading"
            size="small"
            @click="handleSearchButtonClicked"
          ></Button>

          <span
            v-if="submitButtonError"
            class="ris-label3-regular min-h-[1rem] text-red-800"
          >
            {{ submitButtonError }}
          </span>
        </div>

        <Button
          v-if="!isEmptySearch"
          aria-label="Suche zurücksetzen"
          class="ml-8 self-start"
          label="Suche zurücksetzen"
          size="small"
          text
          @click="resetSearch"
        ></Button>
      </div>
    </div>
  </div>
</template>
