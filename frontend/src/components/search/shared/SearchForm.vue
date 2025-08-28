<script lang="ts" setup>
import Button from "primevue/button"
import Checkbox from "primevue/checkbox"
import InputText from "primevue/inputtext"
import InputSelect from "primevue/select"
import { computed, onMounted, ref, watch } from "vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import { DropdownItem, ValidationError } from "@/components/input/types"
import useQuery, { Query } from "@/composables/useQueryFromRoute"
import { useValidationStore } from "@/composables/useValidationStore"
import { Kind } from "@/domain/documentationUnitKind"
import ProcessStep from "@/domain/processStep"
import { PublicationState } from "@/domain/publicationStatus"
import processStepService from "@/services/processStepService"
import { DocumentationUnitSearchParameter } from "@/types/documentationUnitSearchParameter"

const props = defineProps<{
  kind: Kind
  isLoading?: boolean
}>()

const emit = defineEmits<{
  search: [value: Query<DocumentationUnitSearchParameter>]
  resetSearchResults: [void]
}>()

const isPendingProceeding = computed(
  () => props.kind === Kind.PENDING_PROCEEDING,
)
const isDecision = computed(() => props.kind === Kind.DECISION)

const validationStore = useValidationStore<DocumentationUnitSearchParameter>()
const { route, getQueryFromRoute, pushQueryToRoute } =
  useQuery<DocumentationUnitSearchParameter>()
const query = ref(getQueryFromRoute())

const isEmptySearch = computed(() => {
  return Object.keys(query.value).length === 0
})

const submitButtonError = ref<string | undefined>()

const dropdownItems: DropdownItem[] = [
  { label: "Alle", value: undefined },
  { label: "Veröffentlicht", value: PublicationState.PUBLISHED },
  { label: "Unveröffentlicht", value: PublicationState.UNPUBLISHED },
  { label: "In Veröffentlichung", value: PublicationState.PUBLISHING },
  { label: "Dublette", value: PublicationState.DUPLICATED },
  { label: "Gesperrt", value: PublicationState.LOCKED },
  { label: "Löschen", value: PublicationState.DELETING },
  { label: "Fremdanlage", value: PublicationState.EXTERNAL_HANDOVER_PENDING },
]

const isResolved = computed({
  get: () =>
    isPendingProceeding.value && query.value?.isResolved
      ? JSON.parse(query.value.isResolved)
      : false,
  set: (data) => {
    if (isPendingProceeding.value) {
      query.value.isResolved = data
    }
  },
})

const publicationStatus = computed({
  get: () => query.value?.publicationStatus,
  set: (data) => {
    if (!data) {
      delete query.value.publicationStatus
    } else {
      query.value.publicationStatus = data
    }
  },
})

const myDocOfficeOnly = computed({
  get: () =>
    isDecision.value && query.value?.myDocOfficeOnly
      ? JSON.parse(query.value.myDocOfficeOnly)
      : false,
  set: (data) => {
    if (isDecision.value) {
      if (!data) {
        // Clear related fields when myDocOfficeOnly is unchecked
        delete query.value.withError
        delete query.value.withDuplicateWarning
        delete query.value.myDocOfficeOnly
        delete query.value.scheduledOnly
        delete query.value.publicationDate
        delete query.value.processStepId
        resetErrors("publicationDate") // Clear validation for publicationDate
      } else {
        query.value.myDocOfficeOnly = "true"
      }
    }
  },
})

const scheduledOnly = computed({
  get: () =>
    isDecision.value && query.value?.scheduledOnly
      ? JSON.parse(query.value.scheduledOnly)
      : false,
  set: (data) => {
    if (isDecision.value) {
      if (!data) {
        delete query.value.scheduledOnly
      } else {
        query.value.scheduledOnly = "true"
      }
    }
  },
})

const withError = computed({
  get: () =>
    isDecision.value && query.value?.withError
      ? JSON.parse(query.value.withError)
      : false,
  set: (data) => {
    if (isDecision.value) {
      if (!data) {
        delete query.value.withError
      } else {
        query.value.withError = "true"
      }
    }
  },
})

const withDuplicateWarning = computed({
  get: () =>
    isDecision.value && query.value?.withDuplicateWarning
      ? JSON.parse(query.value.withDuplicateWarning)
      : false,
  set: (data) => {
    if (isDecision.value) {
      if (!data) {
        delete query.value.withDuplicateWarning
      } else {
        query.value.withDuplicateWarning = "true"
      }
    }
  },
})

const processStepId = computed({
  get: () => query.value.processStepId,
  set: (data) => {
    if (data) {
      query.value.processStepId = data
    } else {
      delete query.value.processStepId
    }
  },
})

const processSteps = ref<ProcessStep[]>()

/**
 * Resets the search form, validation errors, and clears the query.
 */
function resetSearch() {
  validationStore.reset()
  submitButtonError.value = undefined
  query.value = {}
  pushQueryToRoute(query.value)
  emit("resetSearchResults")
}

/**
 * Resets specific validation errors by ID or clears the submit button error.
 * @param {DocumentationUnitSearchParameter} id - The ID of the field to reset errors for.
 */
function resetErrors(id?: DocumentationUnitSearchParameter) {
  if (id) validationStore.remove(id)
  submitButtonError.value = undefined
}

/**
 * Checks if the search input is invalid (empty or has validation errors).
 * @returns {boolean} - True if the input is invalid, false otherwise.
 */
function isSearchInputInvalid(): boolean {
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

/**
 * Checks if there are any active validation errors.
 * @returns {boolean} - True if there are validation errors, false otherwise.
 */
function hasValidationErrors(): boolean {
  return validationStore.getAll().length > 0
}

/**
 * Validates the decision dates (for pending proceeding 'Mitteilungsdatum').
 * This logic is common to both forms for decisionDate and decisionDateEnd.
 */
function validateDecisionDates() {
  // Check if end date is present but start date is missing
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

  // Check if end date is before start date
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

/**
 * Validates the resolution dates (Erledigungsmitteilung).
 */
function validateResolutionDates() {
  if (isPendingProceeding.value) {
    // Check if end date is present but start date is missing
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

    // Check if end date is before start date
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
}

/**
 * Main validation function that calls specific validation based on form type.
 */
function validateSearchInput() {
  validateDecisionDates() // Always validate decision dates as they are common
  if (isPendingProceeding.value) {
    validateResolutionDates()
  }
}

/**
 * Handles local input errors from child components and updates the validation store.
 * @param {ValidationError | undefined} error - The validation error object.
 * @param {string} id - The ID of the input field.
 */
function handleLocalInputError(error: ValidationError | undefined, id: string) {
  if (error) {
    validationStore.add(
      error.message,
      error.instance as DocumentationUnitSearchParameter,
    )
  } else {
    validationStore.remove(id as DocumentationUnitSearchParameter)
  }
  validateSearchInput() // Re-validate after error change
}

/**
 * Checks if the current search query is identical to the previous query in the route.
 * @returns {boolean} - `true` if the current query matches the previous query, otherwise `false`.
 */
function isIdenticalSearch(): boolean {
  const previousQuery = getQueryFromRoute()
  const newQuery = query.value
  return JSON.stringify(previousQuery) === JSON.stringify(newQuery)
}

/**
 * Handles the search button click event.
 * Performs validation and triggers search if valid.
 */
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

/**
 * Emits the search event with the current query or resets if empty.
 */
function handleSearch() {
  if (!isEmptySearch.value) {
    emit("search", getQueryFromRoute())
  } else {
    resetSearch()
  }
}

onMounted(async () => {
  const processStepsResponse = await processStepService.getProcessSteps()
  if (!processStepsResponse.error) {
    processSteps.value = processStepsResponse.data
  }
})

// Watch for route changes to update query and trigger search
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
    class="mb-32 flex flex-col bg-blue-200"
    data-testid="document-unit-search-entry-form"
  >
    <div
      class="m-32 grid grid-cols-[150px_1fr_150px_1fr] gap-x-40 gap-y-16"
      :class="{
        'grid-layout-decision': isDecision,
        'grid-layout-pending-proceeding': isPendingProceeding,
        'is-own-docoffice': myDocOfficeOnly,
      }"
    >
      <!-- Common Fields for Decisions and Pending Proceeding-->
      <div
        class="ris-body1-regular ml-3 flex flex-row items-center [grid-area:az-label]"
      >
        Aktenzeichen
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

      <div
        class="ris-body1-regular items-center[grid-area:docnumber-label] flex flex-row"
      >
        Dokument&shy;nummer
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

      <div
        class="ris-body1-regular flex flex-row items-center [grid-area:court-label]"
      >
        Gericht
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
        class="ris-body1-regular flex flex-row items-center [grid-area:status-label]"
      >
        Status
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
            v-model="publicationStatus"
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
        {{ isDecision ? "Datum" : "Mitteilungs&shy;datum" }}
      </div>
      <div class="flex flex-row gap-10 [grid-area:date-input]">
        <InputField
          id="decisionDate"
          v-slot="{ id, hasError }"
          data-testid="decision-date-input"
          :label="isDecision ? 'Datum' : 'Mitteilungs&shy;datum'"
          :validation-error="validationStore.getByField('decisionDate')"
          visually-hide-label
        >
          <DateInput
            :id="id"
            v-model="query.decisionDate"
            :aria-label="
              isDecision ? 'Entscheidungsdatum Suche' : 'Mitteilungsdatum Suche'
            "
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
          :label="isDecision ? 'Datum Ende' : 'Mitteilungsdatum Ende'"
          :validation-error="validationStore.getByField('decisionDateEnd')"
          visually-hide-label
        >
          <DateInput
            :id="id"
            v-model="query.decisionDateEnd"
            :aria-label="
              isDecision
                ? 'Entscheidungsdatum Suche Ende'
                : 'Mitteilungsdatum Suche Ende'
            "
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
      <!-- Decision Specific Fields -->
      <template v-if="isDecision">
        <div class="flex flex-row gap-10 py-12 [grid-area:own-docoffice]">
          <InputField
            id="documentationOffice"
            v-slot="{ id }"
            label="Nur meine Dokstelle"
            label-class="ris-label1-regular"
            :label-position="LabelPosition.RIGHT"
          >
            <Checkbox
              v-model="myDocOfficeOnly"
              aria-label="Nur meine Dokstelle Filter"
              binary
              :input-id="id"
              @focus="resetErrors(id as DocumentationUnitSearchParameter)"
            />
          </InputField>
        </div>

        <template v-if="myDocOfficeOnly">
          <div
            class="ris-body1-regular flex flex-row items-center [grid-area:process-step-label]"
          >
            Schritt
          </div>

          <div class="flex flex-row gap-20 [grid-area:process-step-input]">
            <InputField
              id="processStep"
              data-testid="process-step-input"
              label="Prozessschritt"
              visually-hide-label
            >
              <InputSelect
                v-model="processStepId"
                aria-label="Prozessschritt"
                class="w-full"
                option-label="name"
                option-value="uuid"
                :options="processSteps"
              ></InputSelect>
            </InputField>
          </div>

          <div
            class="ris-body1-regular flex flex-row items-center [grid-area:jdv-label]"
          >
            jDV Übergabe
          </div>

          <div class="flex flex-row gap-20 [grid-area:jdv-input]">
            <InputField
              id="publicationDate"
              v-slot="{ id, hasError }"
              data-testid="publication-date-input"
              label="jDV Übergabedatum"
              :validation-error="validationStore.getByField('publicationDate')"
              visually-hide-label
            >
              <DateInput
                :id="id"
                v-model="query.publicationDate"
                aria-label="jDV Übergabedatum Suche"
                :has-error="hasError"
                is-future-date
                @blur="validateSearchInput"
                @focus="resetErrors(id as DocumentationUnitSearchParameter)"
                @update:validation-error="
                  (validationError: ValidationError | undefined) =>
                    handleLocalInputError(validationError, id)
                "
              ></DateInput>
            </InputField>
            <InputField
              id="scheduled"
              v-slot="{ id }"
              label="Nur terminiert"
              label-class="ris-label1-regular"
              :label-position="LabelPosition.RIGHT"
            >
              <Checkbox
                v-model="scheduledOnly"
                aria-label="Terminiert Filter"
                binary
                :input-id="id"
              />
            </InputField>
          </div>

          <div
            class="ris-body1-regular flex flex-row items-center [grid-area:checkbox-label]"
          >
            Fehler
          </div>

          <div class="flex flex-row gap-20 [grid-area:checkbox-group]">
            <InputField
              id="withErrorsOnly"
              v-slot="{ id }"
              label="Nur Fehler"
              label-class="ris-label1-regular"
              :label-position="LabelPosition.RIGHT"
            >
              <Checkbox
                v-model="withError"
                aria-label="Nur fehlerhafte Dokumentationseinheiten"
                binary
                :input-id="id"
                @focus="resetErrors(id as DocumentationUnitSearchParameter)"
              />
            </InputField>
            <InputField
              id="withDuplicateWaring"
              v-slot="{ id }"
              label="Dubletten&shy;verdacht"
              label-class="ris-label1-regular"
              :label-position="LabelPosition.RIGHT"
            >
              <Checkbox
                v-model="withDuplicateWarning"
                aria-label="Dokumentationseinheiten mit Dublettenverdacht"
                binary
                :input-id="id"
                @focus="resetErrors(id as DocumentationUnitSearchParameter)"
              />
            </InputField>
          </div>
        </template>
      </template>

      <!-- Pending Proceeding Specific Fields -->
      <template v-if="isPendingProceeding">
        <div
          class="ris-body1-regular 8 flex flex-row items-center [grid-area:resolution-date-label]"
        >
          Erledigungs&shy;mitteilung
        </div>
        <div class="flex flex-row gap-10 [grid-area:resolution-date-input]">
          <InputField
            id="resolutionDate"
            v-slot="{ id, hasError }"
            data-testid="resolution-date-input"
            label="Erledigungs&shy;mitteilung"
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
        <div class="flex flex-row py-12 [grid-area:resolved-input]">
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
      </template>

      <!-- Common Search Button -->

      <div class="flex flex-col gap-8 [grid-area:search-button]">
        <div class="flex flex-row justify-end gap-x-16">
          <Button
            v-if="!isEmptySearch"
            aria-label="Suche zurücksetzen"
            class="ml-8 self-start"
            label="Suche zurücksetzen"
            text
            @click="resetSearch"
          ></Button>
          <Button
            :aria-label="
              isDecision
                ? 'Nach Dokumentationseinheiten suchen'
                : 'Nach Anhängigen Verfahren suchen'
            "
            class="self-start"
            :disabled="isLoading"
            label="Ergebnisse zeigen"
            @click="handleSearchButtonClicked"
          ></Button>
        </div>
        <div class="flex flex-row justify-end">
          <span
            v-if="submitButtonError"
            class="ris-label3-regular min-h-[1rem] text-red-800"
          >
            {{ submitButtonError }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.grid-layout-decision {
  grid-template-areas:
    "az-label az-input docnumber-label docnumber-input"
    "court-label court-input status-label status-input"
    "date-label date-input . ."
    "own-docoffice own-docoffice . ."
    ". . search-button search-button";
  grid-template-columns:
    fit-content(150px) minmax(200px, 1fr) fit-content(150px)
    minmax(200px, 1fr);
}

.grid-layout-decision.is-own-docoffice {
  grid-template-areas:
    "az-label az-input docnumber-label docnumber-input"
    "court-label court-input status-label status-input"
    "date-label date-input . ."
    "own-docoffice own-docoffice . ."
    "process-step-label process-step-input jdv-label jdv-input"
    ". . checkbox-label checkbox-group"
    ". . search-button search-button";
  grid-template-columns:
    fit-content(150px) minmax(200px, 1fr) fit-content(150px)
    minmax(200px, 1fr);
}

.grid-layout-pending-proceeding {
  grid-template-areas:
    "az-label az-input docnumber-label docnumber-input"
    "court-label court-input status-label status-input"
    "date-label date-input resolution-date-label resolution-date-input"
    ". . . resolved-input"
    ". . search-button search-button";
  grid-template-columns:
    fit-content(150px) minmax(200px, 1fr) fit-content(150px)
    minmax(200px, 1fr);
}
</style>
