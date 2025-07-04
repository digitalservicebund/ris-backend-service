<script lang="ts" setup>
import Button from "primevue/button"
import Checkbox from "primevue/checkbox"
import InputText from "primevue/inputtext"
import InputSelect from "primevue/select"
import { computed, ref, watch } from "vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import { DropdownItem, ValidationError } from "@/components/input/types"
import useQuery, { Query } from "@/composables/useQueryFromRoute"
import { useValidationStore } from "@/composables/useValidationStore"
import { PublicationState } from "@/domain/publicationStatus"
import { DocumentationUnitSearchParameter } from "@/types/documentationUnitSearchParameter"

defineProps<{
  isLoading?: boolean
}>()

const emit = defineEmits<{
  search: [value: Query<DocumentationUnitSearchParameter>]
  resetSearchResults: [void]
}>()

const validationStore = useValidationStore<DocumentationUnitSearchParameter>()
const { route, getQueryFromRoute, pushQueryToRoute } =
  useQuery<DocumentationUnitSearchParameter>()
const query = ref(getQueryFromRoute())

const isEmptySearch = computed(() => {
  return Object.keys(query.value).length === 0
})

const submitButtonError = ref()

const isResolved = computed({
  get: () =>
    query.value?.isResolved ? JSON.parse(query.value.isResolved) : false,
  set: (data) => {
    query.value.isResolved = data
  },
})

const dropdownItems: DropdownItem[] = [
  { label: "Alle", value: "" },
  { label: "Veröffentlicht", value: PublicationState.PUBLISHED },
  { label: "Unveröffentlicht", value: PublicationState.UNPUBLISHED },
  { label: "In Veröffentlichung", value: PublicationState.PUBLISHING },
  { label: "Dublette", value: PublicationState.DUPLICATED },
  { label: "Gesperrt", value: PublicationState.LOCKED },
  { label: "Löschen", value: PublicationState.DELETING },
  { label: "Fremdanlage", value: PublicationState.EXTERNAL_HANDOVER_PENDING },
]

function resetSearch() {
  validationStore.reset()
  submitButtonError.value = undefined
  query.value = {}
  pushQueryToRoute(query.value)
  emit("resetSearchResults")
}

function resetErrors(id?: DocumentationUnitSearchParameter) {
  if (id) validationStore.remove(id)
  submitButtonError.value = undefined
}

function isSearchInputInvalid() {
  if (isEmptySearch.value) {
    submitButtonError.value = "Geben Sie mindestens ein Suchkriterium ein"
    return true
  }

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
  //Startdatum fehlt für Mitteilungsdatum
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

  //Enddatum darf nicht vor Startdatum liegen für Mitteilungsdatum
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

  //Startdatum fehlt für Erledigungsvermerk
  if (
    query.value?.resolutionDateEnd &&
    !query.value?.resolutionDate &&
    !validationStore.getByField("resolutionDate")
  ) {
    validationStore.add("Startdatum fehlt", "resolutionDate")
  } else if (
    !query.value.resolutionDateEnd &&
    validationStore.getByMessage("Startdatum fehlt").length === 1
  ) {
    validationStore.remove("resolutionDate")
  }

  //Enddatum darf nicht vor Startdatum liegen für Mitteilungsdatum
  if (
    query.value?.resolutionDateEnd &&
    query.value?.resolutionDate &&
    new Date(query.value.resolutionDate) >
      new Date(query.value.resolutionDateEnd)
  ) {
    if (!validationStore.getByField("resolutionDateEnd")) {
      validationStore.add(
        "Enddatum darf nicht vor Startdatum liegen",
        "resolutionDateEnd",
      )
    }
  } else if (
    validationStore.getByMessage("Enddatum darf nicht vor Startdatum liegen")
      .length === 1
  ) {
    validationStore.remove("resolutionDateEnd")
  }
}

function handleLocalInputError(error: ValidationError | undefined, id: string) {
  if (error) {
    validationStore.add(
      error.message,
      error.instance as DocumentationUnitSearchParameter,
    )
  } else validationStore.remove(id as DocumentationUnitSearchParameter)
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
  if (!isEmptySearch.value) {
    emit("search", getQueryFromRoute())
  } else {
    resetSearch()
  }
}

watch(
  route,
  () => {
    query.value = getQueryFromRoute()
    handleSearch()
  },
  { deep: true },
)
</script>

<template>
  <div
    v-ctrl-enter="handleSearchButtonClicked"
    class="flex flex-col bg-blue-200"
  >
    <div
      class="m-40 grid grid-cols-[100px_1fr_200px_1fr] grid-rows-[auto_auto_auto_auto_auto] gap-x-12 gap-y-20 [grid-template-areas:'az-label_az-input_docnumber-label_docnumber-input''court-label_court-input_status-label_status-input''date-label_date-input_resolution-date-label_resolution-date-input''publication-label_publication-input_._resolution-input''._._._search-button'] lg:gap-x-32"
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
            @focus="resetErrors(id as DocumentationUnitSearchParameter)"
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
            @focus="resetErrors(id as DocumentationUnitSearchParameter)"
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
            @focus="resetErrors(id as DocumentationUnitSearchParameter)"
          ></InputText>
        </InputField>
      </div>
      <div
        class="ris-body1-regular flex flex-row items-center pl-24 [grid-area:docnumber-label] lg:pl-48"
      >
        Dokumentnummer
      </div>
      <div
        class="ris-body1-regular flex flex-row items-center pl-24 [grid-area:status-label] lg:pl-48"
      >
        Status
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
            @focus="resetErrors(id as DocumentationUnitSearchParameter)"
          ></InputText>
        </InputField>
      </div>
      <div class="flex flex-row gap-10 [grid-area:status-input]">
        <InputField
          id="status"
          v-slot="{ id }"
          label="Status"
          visually-hide-label
        >
          <InputSelect
            :id="id"
            v-model="query.publicationStatus"
            aria-label="Status Suche"
            fluid
            option-label="label"
            option-value="value"
            :options="dropdownItems"
            placeholder="Bitte auswählen"
            @focus="resetErrors(id as DocumentationUnitSearchParameter)"
          />
        </InputField>
      </div>
      <div
        class="ris-body1-regular flex flex-row items-center [grid-area:date-label]"
      >
        Mitteilungsdatum
      </div>
      <div
        class="ris-body1-regular flex flex-row items-center pl-24 [grid-area:resolution-date-label] lg:pl-48"
      >
        Erledigungsmitteilung
      </div>
      <div class="flex flex-row gap-10 [grid-area:date-input]">
        <InputField
          id="decisionDate"
          v-slot="{ id, hasError }"
          data-testid="decision-date-input"
          label="Mitteilungsdatum"
          :validation-error="validationStore.getByField('decisionDate')"
          visually-hide-label
        >
          <DateInput
            :id="id"
            v-model="query.decisionDate"
            aria-label="Mitteilungsdatum Suche"
            :has-error="hasError"
            @blur="validateSearchInput"
            @focus="resetErrors(id as DocumentationUnitSearchParameter)"
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
          label="Mitteilungsdatum Ende"
          :validation-error="validationStore.getByField('decisionDateEnd')"
          visually-hide-label
        >
          <DateInput
            :id="id"
            v-model="query.decisionDateEnd"
            aria-label="Mitteilungsdatum Suche Ende"
            :has-error="hasError"
            placeholder="TT.MM.JJJJ (optional)"
            @blur="validateSearchInput"
            @focus="resetErrors(id as DocumentationUnitSearchParameter)"
            @update:validation-error="
              (validationError: ValidationError | undefined) =>
                handleLocalInputError(validationError, id)
            "
          ></DateInput>
        </InputField>
      </div>
      <div class="flex flex-row gap-10 [grid-area:resolution-date-input]">
        <InputField
          id="resolutionDate"
          v-slot="{ id, hasError }"
          data-testid="resolution-date-input"
          label="Erledigungsmitteilung"
          :validation-error="validationStore.getByField('resolutionDate')"
          visually-hide-label
        >
          <DateInput
            :id="id"
            v-model="query.resolutionDate"
            aria-label="Erledigungsmitteilung Suche"
            :has-error="hasError"
            @blur="validateSearchInput"
            @focus="resetErrors(id as DocumentationUnitSearchParameter)"
            @update:validation-error="
              (validationError: ValidationError | undefined) =>
                handleLocalInputError(validationError, id)
            "
          ></DateInput>
        </InputField>
        <span class="pt-6">-</span>
        <InputField
          id="resolutionDateEnd"
          v-slot="{ id, hasError }"
          data-testid="resolution-date-end-input"
          label="Erledigungsmitteilung Ende"
          :validation-error="validationStore.getByField('resolutionDateEnd')"
          visually-hide-label
        >
          <DateInput
            :id="id"
            v-model="query.resolutionDateEnd"
            aria-label="Erledigungsmitteilung Suche Ende"
            :has-error="hasError"
            placeholder="TT.MM.JJJJ (optional)"
            @blur="validateSearchInput"
            @focus="resetErrors(id as DocumentationUnitSearchParameter)"
            @update:validation-error="
              (validationError: ValidationError | undefined) =>
                handleLocalInputError(validationError, id)
            "
          ></DateInput>
        </InputField>
      </div>
      <div class="flex flex-row [grid-area:resolution-input]">
        <InputField
          id="resolved"
          v-slot="{ id }"
          label="Erledigt"
          label-class="ris-label1-regular"
          :label-position="LabelPosition.RIGHT"
        >
          <Checkbox
            v-model="isResolved"
            aria-label="Erledigt Filter"
            binary
            :input-id="id"
          />
        </InputField>
      </div>
      <div class="flex flex-row [grid-area:search-button]">
        <div class="flex flex-col gap-8">
          <!-- ":loading" disables button while request is running. Needed as long as we cannot cancel requests -->
          <Button
            aria-label="Nach Anhängigen Verfahren suchen"
            class="self-start"
            :disabled="isLoading"
            label="Ergebnisse anzeigen"
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
