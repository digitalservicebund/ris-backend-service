<script lang="ts" setup>
import { computed, ref, watch } from "vue"
import Checkbox from "@/components/input/CheckboxInput.vue"
import DateInput from "@/components/input/DateInput.vue"
import DropdownInput from "@/components/input/DropdownInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import { DropdownItem, ValidationError } from "@/components/input/types"
import useQuery, { Query } from "@/composables/useQueryFromRoute"
import { useValidationStore } from "@/composables/useValidationStore"
import { PublicationState } from "@/domain/publicationStatus"

defineProps<{
  isLoading?: boolean
}>()

const emit = defineEmits<{
  search: [value: Query<DocumentUnitSearchParameter>]
  resetSearchResults: [void]
}>()

const validationStore = useValidationStore<DocumentUnitSearchParameter>()
const { route, getQueryFromRoute, pushQueryToRoute } =
  useQuery<DocumentUnitSearchParameter>()
const query = ref(getQueryFromRoute())

const isEmptySearch = computed(() => {
  return Object.keys(query.value).length === 0
})

const submitButtonError = ref()

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

const myDocOfficeOnly = computed({
  get: () =>
    query.value?.myDocOfficeOnly
      ? JSON.parse(query.value.myDocOfficeOnly)
      : false,
  set: (data) => {
    if (!data) {
      delete query.value.withError
      delete query.value.withDuplicateWarning
      delete query.value.myDocOfficeOnly
      delete query.value.scheduledOnly
      delete query.value.publicationDate
      resetErrors("publicationDate")
    } else {
      query.value.myDocOfficeOnly = "true"
    }
  },
})

const scheduledOnly = computed({
  get: () =>
    query.value?.scheduledOnly ? JSON.parse(query.value.scheduledOnly) : false,
  set: (data) => {
    if (!data) {
      delete query.value.scheduledOnly
    } else {
      query.value.scheduledOnly = "true"
    }
  },
})

const withError = computed({
  get: () =>
    query.value?.withError ? JSON.parse(query.value.withError) : false,
  set: (data) => {
    if (!data) {
      delete query.value.withError
    } else {
      query.value.withError = "true"
    }
  },
})

const withDuplicateWarning = computed({
  get: () =>
    query.value?.withDuplicateWarning
      ? JSON.parse(query.value.withDuplicateWarning)
      : false,
  set: (data) => {
    if (!data) {
      delete query.value.withDuplicateWarning
    } else {
      query.value.withDuplicateWarning = "true"
    }
  },
})

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
  if (!isEmptySearch.value) {
    emit("search", getQueryFromRoute())
  } else {
    resetSearch()
  }
}

watch(
  route,
  () => {
    handleSearch()
  },
  { deep: true, immediate: true },
)
</script>

<script lang="ts">
export type DocumentUnitSearchParameter =
  | "documentNumber"
  | "fileNumber"
  | "publicationStatus"
  | "publicationDate"
  | "scheduledOnly"
  | "courtType"
  | "courtLocation"
  | "decisionDate"
  | "decisionDateEnd"
  | "withError"
  | "myDocOfficeOnly"
  | "withDuplicateWarning"
</script>

<template>
  <div
    v-ctrl-enter="handleSearchButtonClicked"
    class="pyb-24 mb-32 flex flex-col bg-blue-200"
  >
    <div
      class="m-40 grid grid-flow-col grid-cols-[auto_1fr_auto_1fr] grid-rows-[auto_auto_auto_auto_auto] gap-x-12 gap-y-20 lg:gap-x-32"
    >
      <!-- Column 1 -->
      <div class="ds-body-01-reg ml-3 flex flex-row items-center">
        Aktenzeichen
      </div>
      <div class="ds-body-01-reg flex flex-row items-center">Gericht</div>
      <div class="ds-body-01-reg flex flex-row items-center">Datum</div>
      <div
        v-if="myDocOfficeOnly"
        class="ds-body-01-reg flex flex-row items-center"
      >
        jDV Übergabe
      </div>
      <div v-if="!myDocOfficeOnly" />
      <div></div>
      <!-- Column 2 -->
      <div>
        <InputField id="fileNumber" label="Aktenzeichen" visually-hide-label>
          <TextInput
            id="fileNumber"
            v-model="query.fileNumber"
            aria-label="Aktenzeichen Suche"
            class="ds-input-small"
            @focus="resetErrors"
          ></TextInput>
        </InputField>
      </div>
      <div class="flex flex-row gap-10">
        <InputField id="courtType" label="Gerichtstyp" visually-hide-label>
          <TextInput
            id="courtType"
            v-model="query.courtType"
            aria-label="Gerichtstyp Suche"
            class="ds-input-small"
            placeholder="Gerichtstyp"
            @focus="resetErrors"
          ></TextInput>
        </InputField>
        <span class="pt-6">-</span>
        <InputField id="courtLocation" label="Gerichtsort" visually-hide-label>
          <TextInput
            id="courtLocation"
            v-model="query.courtLocation"
            aria-label="Gerichtsort Suche"
            class="ds-input-small"
            placeholder="Ort"
            @focus="resetErrors"
          ></TextInput>
        </InputField>
      </div>
      <div class="flex flex-row gap-10">
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
            class="ds-input-small"
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
            class="ds-input-small"
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
      <div v-if="myDocOfficeOnly" class="flex flex-row gap-20">
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
            class="ds-input-small"
            :has-error="hasError"
            is-future-date
            @blur="validateSearchInput"
            @focus="resetErrors(id as DocumentUnitSearchParameter)"
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
          label-class="ds-label-01-reg"
          :label-position="LabelPosition.RIGHT"
        >
          <Checkbox
            :id="id"
            v-model="scheduledOnly"
            aria-label="Terminiert Filter"
            class="ds-checkbox-mini bg-white"
            @focus="resetErrors"
          />
        </InputField>
      </div>
      <div v-if="!myDocOfficeOnly" />
      <div class="pl-32"></div>
      <!-- Column 3 -->
      <div class="ds-body-01-reg flex flex-row items-center pl-24 lg:pl-48">
        Dokumentnummer
      </div>
      <div class="ds-body-01-reg flex flex-row items-center pl-24 lg:pl-48">
        Status
      </div>
      <div></div>
      <div></div>
      <div></div>
      <!-- Column 4 -->
      <div class="">
        <InputField
          id="documentNumber"
          label="Dokumentnummer"
          visually-hide-label
        >
          <TextInput
            id="documentNumber"
            v-model="query.documentNumber"
            aria-label="Dokumentnummer Suche"
            class="ds-input-small"
            @focus="resetErrors"
          ></TextInput>
        </InputField>
      </div>
      <div class="">
        <InputField id="status" label="Status" visually-hide-label>
          <DropdownInput
            id="status"
            v-model="query.publicationStatus"
            aria-label="Status Suche"
            class="ds-select-small"
            :items="dropdownItems"
            @focus="resetErrors"
          />
        </InputField>
      </div>
      <div class="flex flex-row gap-20">
        <InputField
          id="documentationOffice"
          v-slot="{ id }"
          label="Nur meine Dokstelle"
          label-class="ds-label-01-reg"
          :label-position="LabelPosition.RIGHT"
        >
          <Checkbox
            :id="id"
            v-model="myDocOfficeOnly"
            aria-label="Nur meine Dokstelle Filter"
            class="ds-checkbox-mini bg-white"
            @focus="resetErrors"
          />
        </InputField>
        <InputField
          v-if="myDocOfficeOnly"
          id="withErrorsOnly"
          v-slot="{ id }"
          label="Nur Fehler"
          label-class="ds-label-01-reg"
          :label-position="LabelPosition.RIGHT"
        >
          <Checkbox
            :id="id"
            v-model="withError"
            aria-label="Nur fehlerhafte Dokumentationseinheiten"
            class="ds-checkbox-mini bg-white"
            @focus="resetErrors"
          />
        </InputField>
        <InputField
          v-if="myDocOfficeOnly"
          id="withDuplicateWaring"
          v-slot="{ id }"
          label="Dublettenverdacht"
          label-class="ds-label-01-reg"
          :label-position="LabelPosition.RIGHT"
        >
          <Checkbox
            :id="id"
            v-model="withDuplicateWarning"
            aria-label="Dokumentationseinheiten mit Dublettenverdacht"
            class="ds-checkbox-mini bg-white"
            @focus="resetErrors"
        /></InputField>
      </div>
      <div class="flex flex-row">
        <div class="flex flex-col gap-4">
          <TextButton
            aria-label="Nach Dokumentationseinheiten suchen"
            class="self-start"
            :disabled="isLoading"
            label="Ergebnisse anzeigen"
            size="small"
            @click="handleSearchButtonClicked"
          />

          <span
            v-if="submitButtonError"
            class="ds-label-03-reg min-h-[1rem] text-red-800"
          >
            {{ submitButtonError }}
          </span>
        </div>

        <TextButton
          v-if="!isEmptySearch"
          aria-label="Suche zurücksetzen"
          button-type="ghost"
          class="ml-8 self-start"
          label="Suche zurücksetzen"
          size="small"
          @click="resetSearch"
        />
      </div>
    </div>
  </div>
</template>
