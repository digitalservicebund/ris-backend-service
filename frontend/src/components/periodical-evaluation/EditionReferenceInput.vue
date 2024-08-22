<script lang="ts" setup>
import { ref, computed, watch } from "vue"
import { ValidationError } from "../input/types"
import ComboboxInput from "@/components/ComboboxInput.vue"
import DateInput from "@/components/input/DateInput.vue"
import InputField from "@/components/input/InputField.vue"
import TextButton from "@/components/input/TextButton.vue"
import TextInput from "@/components/input/TextInput.vue"
import Pagination, { Page } from "@/components/Pagination.vue"
import SearchResultList, {
  SearchResults,
} from "@/components/SearchResultList.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import DocumentUnit from "@/domain/documentUnit"
import Reference from "@/domain/reference"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import { useEditionStore } from "@/stores/editionStore"

const props = defineProps<{
  modelValue?: Reference
  modelValueList?: Reference[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: Reference]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value: Reference]
}>()
const store = useEditionStore()
const lastSearchInput = ref(new Reference())
const lastSavedModelValue = ref(new Reference({ ...props.modelValue }))
const reference = ref(new Reference({ ...props.modelValue }))
const validationStore = useValidationStore<(typeof Reference.fields)[number]>()
const pageNumber = ref<number>(0)
const itemsPerPage = ref<number>(15)
const isLoading = ref(false)

const searchResultsCurrentPage = ref<Page<RelatedDocumentation>>()
const searchResults = ref<SearchResults<RelatedDocumentation>>()

const legalPeriodical = computed(() =>
  store.edition && store.edition?.legalPeriodical
    ? {
        label: store.edition.legalPeriodical.abbreviation,
        value: store.edition.legalPeriodical,
      }
    : undefined,
)

const prefix = computed({
  get: () =>
    store.edition && store.edition?.prefix ? store.edition.prefix : undefined,
  set: (newValue) => {
    store.edition!.prefix = newValue
  },
})
const suffix = computed({
  get: () =>
    store.edition && store.edition?.suffix ? store.edition.suffix : undefined,
  set: (newValue) => {
    store.edition!.suffix = newValue
  },
})

const citation = computed(() =>
  [
    ...(prefix.value ? [prefix.value] : []),
    ...(reference.value.citation ? [reference.value.citation] : []),
    ...(suffix.value ? [suffix.value] : []),
  ].join(" "),
)

function updateDateFormatValidation(
  validationError: ValidationError | undefined,
) {
  if (validationError)
    validationStore.add(validationError.message, "decisionDate")
  else validationStore.remove("decisionDate")
}

async function search() {
  isLoading.value = true
  const documentationUnitRef = new RelatedDocumentation({
    ...reference.value,
  })

  //Reset page number to 0, when search input changes
  if (
    reference.value.court != lastSearchInput.value.court ||
    reference.value.decisionDate != lastSearchInput.value.decisionDate ||
    reference.value.fileNumber != lastSearchInput.value.fileNumber ||
    reference.value.documentType != lastSearchInput.value.documentType
  ) {
    pageNumber.value = 0
  }

  const response = await documentUnitService.searchByRelatedDocumentation(
    documentationUnitRef,
    {
      ...(pageNumber.value != undefined
        ? { pg: pageNumber.value.toString() }
        : {}),
      ...(itemsPerPage.value != undefined
        ? { sz: itemsPerPage.value.toString() }
        : {}),
    },
  )
  if (response.data) {
    searchResultsCurrentPage.value = {
      ...response.data,
      content: response.data.content.map(
        (decision) => new RelatedDocumentation({ ...decision }),
      ),
    }
    searchResults.value = response.data.content.map((searchResult) => {
      return {
        decision: new RelatedDocumentation({ ...searchResult }),
        isLinked: searchResult.isLinkedWith(props.modelValueList),
      }
    })
  }
  isLoading.value = false
}

async function updatePage(page: number) {
  pageNumber.value = page
  search()
}
async function validateRequiredInput() {
  validationStore.reset()
  if (reference.value.missingRequiredFields?.length) {
    reference.value.missingRequiredFields.forEach((missingField) => {
      console.log(missingField)
      validationStore.add("Pflichtfeld nicht befüllt", missingField)
    })
  }
}

async function addReference(decision: RelatedDocumentation) {
  await validateRequiredInput()
  if (!validationStore.getByMessage("Pflichtfeld nicht befüllt").length) {
    // Merge the decision from search with the current reference input values
    emit(
      "update:modelValue",
      new Reference({
        id: reference.value.uuid,
        citation: citation.value,
        referenceSupplement: reference.value.referenceSupplement,
        footnote: reference.value.footnote,
        legalPeriodical: reference.value.legalPeriodical,
        legalPeriodicalRawValue: reference.value.legalPeriodicalRawValue,
        documentationUnit: new DocumentUnit(decision.uuid as string),
      }),
    )
    emit("addEntry")
  }
}

watch(
  () => store.edition?.legalPeriodical,
  (legalPeriodical) => {
    if (legalPeriodical) {
      reference.value = new Reference({
        ...props.modelValue,
        legalPeriodical: legalPeriodical,
      })
    }
  },
  { immediate: true },
)
</script>

<template>
  <div class="flex h-full flex-col space-y-24 px-16 py-16">
    <div class="flex flex-col gap-24">
      <InputField id="legalPeriodical" label="Periodikum *">
        <ComboboxInput
          id="legalPeriodical"
          v-model="legalPeriodical"
          aria-label="Periodikum"
          clear-on-choosing-item
          :item-service="ComboboxItemService.getLegalPeriodicals"
          read-only
        ></ComboboxInput>
      </InputField>
      <div class="flex flex-col gap-24">
        <div class="flex justify-between gap-24">
          <div class="flex-1">
            <InputField
              id="citation"
              v-slot="slotProps"
              label="Zitatstelle *"
              :validation-error="validationStore.getByField('citation')"
            >
              <div class="flex flex-row gap-4">
                <TextInput
                  id="citation prefix"
                  v-model="prefix"
                  aria-label="Zitatstelle Präfix"
                  size="medium"
                ></TextInput>
                <TextInput
                  id="citation"
                  v-model="reference.citation"
                  aria-label="Zitatstelle *"
                  :has-error="slotProps.hasError"
                  size="medium"
                  @focus="validationStore.remove('citation')"
                ></TextInput>
                <TextInput
                  id="citation suffix"
                  v-model="suffix"
                  aria-label="Zitatstelle Suffix"
                  size="medium"
                ></TextInput>
              </div>
            </InputField>

            <span v-if="legalPeriodical" class="ds-label-03-reg"
              >Zitierbeispiel: {{ legalPeriodical.value.citationStyle }}</span
            >
          </div>
          <InputField
            id="citation"
            v-slot="slotProps"
            class="flex-1"
            label="Klammernzusatz"
          >
            <TextInput
              id="citation"
              v-model="reference.referenceSupplement"
              aria-label="Klammernzusatz"
              :has-error="slotProps.hasError"
              size="medium"
            ></TextInput>
          </InputField>
        </div>
      </div>
    </div>
    <div class="flex flex-col gap-24">
      <div class="flex justify-between gap-24">
        <InputField
          id="courtInput"
          v-slot="slotProps"
          label="Gericht"
          :validation-error="validationStore.getByField('court')"
        >
          <ComboboxInput
            id="courtInput"
            v-model="reference.court"
            aria-label="Gericht Aktivzitierung"
            clear-on-choosing-item
            :has-error="slotProps.hasError"
            :item-service="ComboboxItemService.getCourts"
            :read-only="reference.hasForeignSource"
            @focus="validationStore.remove('court')"
          >
          </ComboboxInput>
        </InputField>
        <InputField
          id="decisionDate"
          v-slot="slotProps"
          label="Entscheidungsdatum"
          :validation-error="validationStore.getByField('decisionDate')"
          @update:validation-error="
            (validationError: any) =>
              updateDateFormatValidation(validationError)
          "
        >
          <DateInput
            id="decisionDate"
            v-model="reference.decisionDate"
            aria-label="Entscheidungsdatum"
            class="ds-input-medium"
            :has-error="slotProps.hasError"
            :read-only="reference.hasForeignSource"
            @focus="validationStore.remove('decisionDate')"
            @update:validation-error="slotProps.updateValidationError"
          ></DateInput>
        </InputField>
      </div>
      <div class="flex justify-between gap-24">
        <InputField
          id="activeCitationFileNumber"
          v-slot="slotProps"
          label="Aktenzeichen"
          :validation-error="validationStore.getByField('fileNumber')"
        >
          <TextInput
            id="activeCitationDocumentType"
            v-model="reference.fileNumber"
            aria-label="Aktenzeichen Aktivzitierung"
            :has-error="slotProps.hasError"
            :read-only="reference.hasForeignSource"
            size="medium"
            @focus="validationStore.remove('fileNumber')"
          ></TextInput>
        </InputField>
        <InputField id="activeCitationDecisionDocumentType" label="Dokumenttyp">
          <ComboboxInput
            id="activeCitationDecisionDocumentType"
            v-model="reference.documentType"
            aria-label="Dokumenttyp Aktivzitierung"
            :item-service="ComboboxItemService.getDocumentTypes"
            :read-only="reference.hasForeignSource"
          ></ComboboxInput>
        </InputField>
      </div>
    </div>

    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <TextButton
            v-if="!modelValue?.hasForeignSource"
            aria-label="Nach Entscheidung suchen"
            button-type="primary"
            label="Suchen"
            size="small"
            @click="search"
          />
          <TextButton
            v-if="!lastSavedModelValue.isEmpty"
            aria-label="Fundstelle vermerken"
            button-type="tertiary"
            data-testid="previous-decision-save-button"
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
        @click.stop="emit('removeEntry', modelValue)"
      />
    </div>
    <div class="bg-blue-200">
      <Pagination
        navigation-position="bottom"
        :page="searchResultsCurrentPage"
        @update-page="updatePage"
      >
        <SearchResultList
          :is-loading="isLoading"
          :search-results="searchResults"
          @link-decision="addReference"
        />
      </Pagination>
    </div>
  </div>
</template>
@/stores/editionStore
