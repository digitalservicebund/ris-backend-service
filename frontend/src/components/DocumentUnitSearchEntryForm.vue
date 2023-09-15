<script lang="ts" setup>
import { computed, onMounted, ref } from "vue"
import { useValidationStore } from "@/composables/useValidationStore"
import { PublicationState } from "@/domain/documentUnit"
import DocumentUnitSearchInput from "@/domain/documentUnitSearchInput"
import Checkbox from "@/shared/components/input/CheckboxInput.vue"
import DateInput from "@/shared/components/input/DateInput.vue"
import DropdownInput from "@/shared/components/input/DropdownInput.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import TextButton from "@/shared/components/input/TextButton.vue"
import TextInput from "@/shared/components/input/TextInput.vue"
import { DropdownItem, ValidationError } from "@/shared/components/input/types"

const props = defineProps<{
  modelValue?: DocumentUnitSearchInput
  isLoading?: boolean
}>()

const emit = defineEmits<{
  search: [value: DocumentUnitSearchInput]
  resetSearchResults: [void]
}>()

const validationStore =
  useValidationStore<(typeof DocumentUnitSearchInput.fields)[number]>()

const submitButtonError = ref()

const searchEntry = ref<DocumentUnitSearchInput>(props.modelValue ?? {})

const searchEntryEmpty = computed(() => {
  return Object.keys(searchEntry.value).length === 0
})

const dropdownItems: DropdownItem[] = [
  { label: "Alle", value: "" },
  { label: "Veröffentlicht", value: PublicationState.PUBLISHED },
  { label: "Unveröffentlicht", value: PublicationState.UNPUBLISHED },
  { label: "In Veröffentlichung", value: PublicationState.PUBLISHING },
]

const documentNumberOrFileNumber = computed({
  get: () => searchEntry.value?.documentNumberOrFileNumber,
  set: (data) => {
    if (data?.length === 0) {
      delete searchEntry.value.documentNumberOrFileNumber
    } else {
      searchEntry.value.documentNumberOrFileNumber = data
    }
  },
})

const publishingStateModel = computed({
  get: () => searchEntry.value?.status?.publicationStatus ?? "",
  set: (data) => {
    if (data?.length === 0) {
      delete searchEntry.value.status
    } else {
      searchEntry.value.status = {
        publicationStatus: data as PublicationState,
        ...searchEntry.value.status,
      }
    }
  },
})

const courtType = computed({
  get: () => searchEntry.value?.court?.type,
  set: (data) => {
    if (data?.length === 0) {
      searchEntry.value?.court?.location
        ? delete searchEntry.value.court.type
        : delete searchEntry.value.court
    } else {
      searchEntry.value.court = {
        ...searchEntry.value.court,
        type: data,
        label: "",
      }
    }
  },
})

const courtLocation = computed({
  get: () => searchEntry.value?.court?.location,
  set: (data) => {
    if (data?.length === 0) {
      searchEntry.value?.court?.type
        ? delete searchEntry.value.court.location
        : delete searchEntry.value.court
    } else {
      searchEntry.value.court = {
        ...searchEntry.value.court,
        location: data,
        label: "",
      }
    }
  },
})

const decisionDate = computed({
  get: () => searchEntry.value?.decisionDate,
  set: (data) => {
    if (data?.length === 0 || !data) {
      delete searchEntry.value.decisionDate
    } else {
      searchEntry.value.decisionDate = data
    }
    validateSearchInput()
  },
})

const decisionDateEnd = computed({
  get: () => searchEntry.value?.decisionDateEnd,
  set: (data) => {
    if (data?.length === 0 || !data) {
      delete searchEntry.value.decisionDateEnd
      if (validationStore.getByMessage("Startdatum fehlt").length === 1) {
        validationStore.remove("decisionDate")
      }
    } else {
      searchEntry.value.decisionDateEnd = data
    }
    validateSearchInput()
  },
})

const myDocOfficeOnly = computed({
  get: () => searchEntry.value?.myDocOfficeOnly,
  set: (data) => {
    searchEntry.value.myDocOfficeOnly = data
    //should also reset with errors only filter
    if (!data)
      searchEntry.value.status = {
        ...searchEntry.value.status,
        withError: false,
      }
  },
})

const withErrorsOnly = computed({
  get: () => searchEntry.value?.status?.withError,
  set: (data) => {
    searchEntry.value.status = {
      ...searchEntry.value.status,
      withError: data ?? false,
    }
  },
})

function resetSearch() {
  validationStore.reset()
  submitButtonError.value = undefined
  searchEntry.value = {}
  emit("resetSearchResults")
}

function resetErrors(id?: (typeof DocumentUnitSearchInput.fields)[number]) {
  if (id) validationStore.remove(id)
  submitButtonError.value = undefined
}

async function validateSearchInput() {
  if (
    searchEntry.value?.decisionDateEnd &&
    !searchEntry.value?.decisionDate &&
    !validationStore.getByField("decisionDate")
  ) {
    validationStore.add("Startdatum fehlt", "decisionDate")
  }

  if (
    searchEntry.value?.decisionDateEnd &&
    searchEntry.value?.decisionDate &&
    new Date(searchEntry.value.decisionDate) >
      new Date(searchEntry.value.decisionDateEnd)
  ) {
    !validationStore.getByField("decisionDateEnd") &&
      validationStore.add(
        "Enddatum darf nich vor Startdatum liegen",
        "decisionDateEnd",
      )
  } else if (
    validationStore.getByMessage("Enddatum darf nich vor Startdatum liegen")
      .length === 1
  ) {
    validationStore.remove("decisionDateEnd")
  }
}

function handleSearchButtonClicked() {
  validateSearchInput()

  if (searchEntryEmpty.value) {
    submitButtonError.value = "Geben Sie mindestens ein Suchkriterium ein"
  } else if (validationStore.getAll().length > 0) {
    submitButtonError.value = "Fehler in Suchkriterien"
  } else emit("search", searchEntry.value)
}

function handleLocalInputError(error: ValidationError | undefined, id: string) {
  if (error) {
    validationStore.add(
      error.message,
      error.instance as (typeof DocumentUnitSearchInput.fields)[number],
    )
  } else
    validationStore.remove(
      id as (typeof DocumentUnitSearchInput.fields)[number],
    )

  validateSearchInput()
}

onMounted(async () => {
  searchEntry.value = props.modelValue ?? {}
  validateSearchInput()
})
</script>

<template>
  <div class="mb-32 flex flex-col bg-blue-200 py-24">
    <div
      class="m-32 grid grid-flow-col grid-cols-[180px_1fr_180px_1fr] grid-rows-[66px_66px_66px] gap-x-20 gap-y-4"
    >
      <!-- Column 1 -->
      <div class="ds-body-01-reg pl-32">Dokumentnummer/ Aktenzeichen</div>
      <div class="ds-body-01-reg pl-32">Gericht</div>
      <div class="ds-body-01-reg pl-32">Entscheidungsdatum</div>
      <!-- Column 2 -->
      <div class="pr-32">
        <InputField
          id="documentNumberOrFileNumber"
          label="Dokumentnummer oder Aktenzeichen"
          visually-hide-label
        >
          <TextInput
            id="documentNumberOrFileNumber"
            v-model="documentNumberOrFileNumber"
            aria-label="Dokumentnummer oder Aktenzeichen Suche"
            class="ds-input-small"
            placeholder="Dokumentnummer/ Aktenzeichen"
            @focus="resetErrors"
          ></TextInput>
        </InputField>
      </div>
      <div class="flex flex-row gap-24 pr-32">
        <InputField id="courtType" label="Gerichtstyp" visually-hide-label>
          <TextInput
            id="courtType"
            v-model="courtType"
            aria-label="Gerichtstyp Suche"
            class="ds-input-small"
            placeholder="Gerichtstyp"
            @focus="resetErrors"
          ></TextInput>
        </InputField>
        <InputField id="courtLocation" label="Gerichtsort" visually-hide-label>
          <TextInput
            id="courtLocation"
            v-model="courtLocation"
            aria-label="Gerichtsort Suche"
            class="ds-input-small"
            placeholder="Gerichtsort"
            @focus="resetErrors"
          ></TextInput>
        </InputField>
      </div>
      <div class="flex flex-row gap-10 pr-32">
        <InputField
          id="decisionDate"
          v-slot="{ id, hasError }"
          data-testid="decisionDateInput"
          label="Entscheidungsdatum"
          :validation-error="validationStore.getByField('decisionDate')"
          visually-hide-label
        >
          <DateInput
            :id="id"
            v-model="decisionDate"
            aria-label="Entscheidungsdatum Suche"
            class="ds-input-small"
            :has-error="hasError"
            @blur="validateSearchInput"
            @focus="
              resetErrors(id as (typeof DocumentUnitSearchInput.fields)[number])
            "
            @update:validation-error="
              (validationError) => handleLocalInputError(validationError, id)
            "
          ></DateInput>
        </InputField>
        <span>-</span>
        <InputField
          id="decisionDateEnd"
          v-slot="{ id, hasError }"
          data-testid="decisionDateEndInput"
          label="Entscheidungsdatum Ende"
          :validation-error="validationStore.getByField('decisionDateEnd')"
          visually-hide-label
        >
          <DateInput
            :id="id"
            v-model="decisionDateEnd"
            aria-label="Entscheidungsdatum Suche Ende"
            class="ds-input-small"
            :has-error="hasError"
            placeholder="TT.MM.JJJJ (optional)"
            @blur="validateSearchInput"
            @focus="
              resetErrors(id as (typeof DocumentUnitSearchInput.fields)[number])
            "
            @update:validation-error="
              (validationError) => handleLocalInputError(validationError, id)
            "
          ></DateInput>
        </InputField>
      </div>
      <!-- Column 3 -->
      <div class="ds-body-01-reg pl-32">Status</div>
      <div class="pl-32"></div>
      <div class="pl-32"></div>
      <!-- Column 4 -->
      <div class="pr-32">
        <InputField id="status" label="Status" visually-hide-label>
          <DropdownInput
            id="status"
            v-model="publishingStateModel"
            aria-label="Status Suche"
            class="ds-select-small"
            :items="dropdownItems"
            @focus="resetErrors"
          />
        </InputField>
      </div>
      <div class="flex flex-row gap-20 pr-32">
        <InputField
          id="documentationOffice"
          v-slot="{ id }"
          label="Nur meine Dokstelle"
          :label-position="LabelPosition.RIGHT"
        >
          <Checkbox
            :id="id"
            v-model="myDocOfficeOnly"
            aria-label="Nur meine Dokstelle Filter"
            class="ds-checkbox-mini"
            @focus="resetErrors"
          />
        </InputField>
        <InputField
          v-if="myDocOfficeOnly"
          id="withErrorsOnly"
          v-slot="{ id }"
          label="Nur Fehler"
          :label-position="LabelPosition.RIGHT"
        >
          <Checkbox
            :id="id"
            v-model="withErrorsOnly"
            aria-label="Nur fehlerhafte Dokumentationseinheiten"
            class="ds-checkbox-mini"
            @focus="resetErrors"
          />
        </InputField>
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
          v-if="!searchEntryEmpty"
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
