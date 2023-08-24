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
import { DropdownItem } from "@/shared/components/input/types"

const props = defineProps<{
  modelValue?: DocumentUnitSearchInput
}>()

const emit = defineEmits<{
  search: [value: DocumentUnitSearchInput]
  resetSearchResults: [void]
}>()

const validationStore =
  useValidationStore<(typeof DocumentUnitSearchInput.fields)[number]>()

const hasValidationErrors = ref(false)
const submitButtonErrorMessage = ref(false)

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

const publishingStateModel = computed({
  get: () => searchEntry.value?.status?.publicationStatus ?? "",
  set: (data) => {
    if (data?.length === 0) {
      delete searchEntry.value.status
    } else {
      searchEntry.value.status = {
        publicationStatus: data as PublicationState,
        withError: false,
      }
    }
  },
})

const courtType = computed({
  get: () => searchEntry.value?.court?.type,
  set: (data) => {
    if (data) {
      if (searchEntry.value.court) {
        searchEntry.value.court.type = data
      } else {
        searchEntry.value.court = { type: data, label: data }
      }
    } else {
      searchEntry.value.court && delete searchEntry.value.court.type
    }
  },
})

const courtLocation = computed({
  get: () => searchEntry.value?.court?.location,
  set: (data) => {
    if (data) {
      if (searchEntry.value.court) {
        searchEntry.value.court.location = data
      } else {
        searchEntry.value.court = { location: data, label: data }
      }
    } else {
      searchEntry.value.court && delete searchEntry.value.court.location
    }
  },
})

const decisionDate = computed({
  get: () => searchEntry.value?.decisionDate,
  set: (data) => {
    searchEntry.value.decisionDate = data
    if (data) {
      validateSearchInput()
    } else {
      delete searchEntry.value.decisionDate
    }
  },
})

const decisionDateEnd = computed({
  get: () => searchEntry.value?.decisionDateEnd,
  set: (data) => {
    searchEntry.value.decisionDateEnd = data
    if (data) {
      validateSearchInput()
    } else {
      delete searchEntry.value.decisionDateEnd
    }
  },
})

const myDocOfficeOnly = computed({
  get: () => searchEntry.value?.myDocOfficeOnly,
  set: (data) => {
    data
      ? (searchEntry.value.myDocOfficeOnly = true)
      : (searchEntry.value.myDocOfficeOnly = false)
  },
})

function resetSearch() {
  searchEntry.value = {}
  emit("resetSearchResults")
}

async function validateSearchInput() {
  hasValidationErrors.value = false
  if (searchEntry.value?.decisionDateEnd && !searchEntry.value?.decisionDate) {
    validationStore.add("Startdatum fehlt", "decisionDate")
    hasValidationErrors.value = true
  }

  if (
    searchEntry.value?.decisionDateEnd &&
    searchEntry.value?.decisionDate &&
    new Date(searchEntry.value.decisionDate) >
      new Date(searchEntry.value.decisionDateEnd)
  ) {
    validationStore.add(
      "Enddatum darf nich vor Startdatum liegen",
      "decisionDateEnd",
    )
    hasValidationErrors.value = true
  }
}

function handleSearchButtonClicked() {
  validateSearchInput()

  if (!hasValidationErrors.value) {
    submitButtonErrorMessage.value = false
    emit("search", searchEntry.value)
  } else {
    submitButtonErrorMessage.value = true
  }
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
          id="documentNumber"
          label="Dokumentnummer oder Aktenzeichen"
          visually-hide-label
        >
          <TextInput
            id="documentNumber"
            v-model="searchEntry.documentNumberOrFileNumber"
            aria-label="Dokumentnummer oder Aktenzeichen Suche"
            class="ds-input-small"
            placeholder="Dokumentnummer/ Aktenzeichen"
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
          ></TextInput>
        </InputField>
        <InputField id="courtLocation" label="Gerichtsort" visually-hide-label>
          <TextInput
            id="courtLocation"
            v-model="courtLocation"
            aria-label="Gerichtsort Suche"
            class="ds-input-small"
            placeholder="Gerichtsort"
          ></TextInput>
        </InputField>
      </div>
      <div class="flex flex-row gap-10 pr-32">
        <InputField
          id="decisionDate"
          v-slot="{ id, hasError, updateValidationError }"
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
            is-future-date
            @focus="validationStore.remove('decisionDate')"
            @update:validation-error="updateValidationError"
          ></DateInput>
        </InputField>
        <span>-</span>
        <InputField
          id="decisionDateEnd"
          v-slot="{ id, hasError, updateValidationError }"
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
            is-future-date
            placeholder="TT.MM.JJJJ (optional)"
            @focus="validationStore.remove('decisionDate')"
            @update:validation-error="updateValidationError"
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
          />
        </InputField>
        <InputField
          v-if="myDocOfficeOnly"
          id="hasError"
          v-slot="{ id }"
          label="Nur Fehler"
          :label-position="LabelPosition.RIGHT"
        >
          <Checkbox
            :id="id"
            aria-label="Nur fehlerhafte Dokumentationseinheiten"
            class="ds-checkbox-mini"
          />
        </InputField>
      </div>
      <div>
        <div class="inline-grid gap-4">
          <TextButton
            aria-label="Nach Dokumentationseinheiten suchen"
            label="Ergebnisse anzeigen"
            size="small"
            @click="handleSearchButtonClicked"
          />

          <span
            v-if="submitButtonErrorMessage"
            class="ds-label-03-reg min-h-[1rem] text-red-800"
          >
            Fehler in Suchkriterien
          </span>
        </div>

        <TextButton
          v-if="!searchEntryEmpty"
          aria-label="Suche zurücksetzen"
          button-type="ghost"
          class="ml-8"
          label="Suche zurücksetzen"
          size="small"
          @click="resetSearch"
        />
      </div>
    </div>
  </div>
</template>
