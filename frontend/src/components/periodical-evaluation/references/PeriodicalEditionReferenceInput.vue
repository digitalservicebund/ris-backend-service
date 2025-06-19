<script lang="ts" setup>
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import RadioButton from "primevue/radiobutton"
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue"
import ComboboxInput from "@/components/ComboboxInput.vue"
import CreateNewFromSearch from "@/components/CreateNewFromSearch.vue"
import DecisionSummary from "@/components/DecisionSummary.vue"
import { DisplayMode } from "@/components/enumDisplayMode"
import DateInput from "@/components/input/DateInput.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import { ValidationError } from "@/components/input/types"
import Pagination, { Page } from "@/components/Pagination.vue"
import PopupModal from "@/components/PopupModal.vue"
import SearchResultList, {
  SearchResults,
} from "@/components/SearchResultList.vue"
import { useIsSaved } from "@/composables/useIsSaved"
import { useScroll } from "@/composables/useScroll"
import { useValidationStore } from "@/composables/useValidationStore"
import { Decision } from "@/domain/decision"
import { PublicationState } from "@/domain/publicationStatus"
import Reference from "@/domain/reference"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import ComboboxItemService from "@/services/comboboxItemService"
import documentUnitService from "@/services/documentUnitService"
import FeatureToggleService from "@/services/featureToggleService"
import { ResponseError } from "@/services/httpClient"
import { useEditionStore } from "@/stores/editionStore"
import { DocumentationUnitCreationParameters } from "@/types/documentationUnitCreationParameters"
import StringsUtil from "@/utils/stringsUtil"

const props = defineProps<{
  modelValue?: Reference
  modelValueList: Reference[]
}>()
const emit = defineEmits<{
  "update:modelValue": [value: Reference]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value: Reference]
}>()

const { scrollIntoViewportById, openSidePanelAndScrollToSection } = useScroll()
const { isSaved } = useIsSaved(props.modelValue, props.modelValueList)

const store = useEditionStore()
const reference = ref<Reference>(new Reference({ ...props.modelValue }))
const validationStore = useValidationStore()
const pageNumber = ref<number>(0)
const itemsPerPage = ref<number>(15)
const isLoading = ref(false)
const featureToggle = ref()
const showModal = ref(false)
const searchResultsCurrentPage = ref<Page<RelatedDocumentation>>()
const searchResults = ref<SearchResults<RelatedDocumentation>>()
const createNewFromSearchResponseError = ref<ResponseError | undefined>()
const parentWidth = ref(0)
const parentRef = ref<HTMLElement | null>(null)
const resizeObserver: ResizeObserver | null = new ResizeObserver((entries) => {
  for (const entry of entries) {
    parentWidth.value = entry.contentRect.width
  }
})

const layoutClass = computed(() =>
  parentWidth.value < 768 ? "flex flex-col gap-24" : "flex flex-row gap-24",
)

const legalPeriodical = computed(() =>
  store.edition && store.edition?.legalPeriodical
    ? {
        label: store.edition.legalPeriodical.abbreviation,
        value: store.edition.legalPeriodical,
      }
    : undefined,
)

const relatedDocumentationUnit = ref<RelatedDocumentation>(
  props.modelValue?.documentationUnit
    ? new RelatedDocumentation({ ...props.modelValue.documentationUnit })
    : new RelatedDocumentation(),
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

const createDocumentationUnitParameters =
  ref<DocumentationUnitCreationParameters>()

function buildCitation(): string | undefined {
  if (StringsUtil.isEmpty(reference.value.citation)) {
    return undefined
  } else {
    return StringsUtil.mergeNonBlankStrings(
      [prefix.value, reference.value.citation, suffix.value],
      "",
    )
  }
}

function updateDateFormatValidation(
  validationError: ValidationError | undefined,
) {
  if (validationError)
    validationStore.add(validationError.message, "decisionDate")
  else validationStore.remove("decisionDate")
}

async function search() {
  isLoading.value = true
  createNewFromSearchResponseError.value = undefined

  const response = await documentUnitService.searchByRelatedDocumentation(
    relatedDocumentationUnit.value,
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
        isLinked: searchResult.isLinkedWith(
          props.modelValueList.map((item) => item.documentationUnit!),
        ),
      }
    })
  }
  isLoading.value = false
}

async function updatePage(page: number) {
  pageNumber.value = page
  await search()
}

function validateRequiredInput(referenceToValidate?: Reference): boolean {
  // Use the provided referenceToValidate if available, otherwise use the local reference
  const referenceToCheck = referenceToValidate || reference.value

  // Check for missing required fields
  if (
    referenceToCheck.missingRequiredFields?.length &&
    referenceToValidate?.referenceType == "caselaw"
  ) {
    referenceToCheck.missingRequiredFields.forEach((missingField: string) => {
      validationStore.add("Pflichtfeld nicht befüllt", missingField)
    })
    return false // Validation failed
  } else if (
    referenceToCheck.missingRequiredLiteratureFields?.length &&
    referenceToValidate?.referenceType == "literature"
  ) {
    referenceToCheck.missingRequiredLiteratureFields.forEach(
      (missingField: string) => {
        validationStore.add("Pflichtfeld nicht befüllt", missingField)
      },
    )
    return false // Validation failed
  } else {
    return true // Validation passed
  }
}

async function addReference(decision: RelatedDocumentation) {
  validationStore.reset()

  const newReference: Reference = new Reference({
    id: reference.value.id,
    citation: isSaved.value ? reference.value.citation : buildCitation(),
    referenceSupplement: reference.value.referenceSupplement,
    author: reference.value.author,
    documentType: reference.value.documentType,
    referenceType: reference.value.referenceType,
    footnote: reference.value.footnote,
    legalPeriodical: reference.value.legalPeriodical,
    legalPeriodicalRawValue: reference.value.legalPeriodicalRawValue,
    documentationUnit: new RelatedDocumentation({ ...decision }),
  })

  validateRequiredInput(newReference)

  if (validationStore.isValid()) {
    emit("update:modelValue", newReference)
    emit("addEntry")
  } else {
    await scrollIntoViewportById("periodical-references")
  }
}

async function addReferenceWithCreatedDocumentationUnit(docUnit: Decision) {
  if (!docUnit) return
  await addReference(
    new RelatedDocumentation({
      uuid: docUnit.uuid,
      fileNumber: docUnit.coreData.fileNumbers
        ? docUnit.coreData.fileNumbers[0]
        : undefined,
      decisionDate: docUnit.coreData.decisionDate,
      court: docUnit.coreData.court,
      documentType: docUnit.coreData.documentType,
      documentNumber: docUnit.documentNumber,
      status: docUnit.status,
      createdByReference: reference.value.id,
      creatingDocOffice: docUnit.coreData.creatingDocOffice,
      documentationOffice: docUnit.coreData.documentationOffice,
    }),
  )
}

/**
 * Stops propagation of scrolling event, and toggles the showModal value.
 * @param {boolean} [shouldOpen] - Optional parameter to explicitly set the modal state.
 */
function toggleDeletionConfirmationModal(shouldOpen?: boolean) {
  showModal.value = shouldOpen ?? !showModal.value

  if (showModal.value) {
    const scrollLeft = document.documentElement.scrollLeft
    const scrollTop = document.documentElement.scrollTop
    window.onscroll = () => {
      window.scrollTo(scrollLeft, scrollTop)
    }
  } else {
    window.onscroll = () => {
      return
    }
  }
}

function deleteReference() {
  emit("removeEntry", reference.value)
  toggleDeletionConfirmationModal(false)
}

async function deleteReferenceAndDocUnit() {
  await documentUnitService.delete(
    reference.value.documentationUnit?.uuid || "",
  )
  deleteReference()
}

/*
Relates the legal periodical of edition to the reference
 */
watch(
  () => store.edition?.legalPeriodical,
  (legalPeriodical) => {
    if (legalPeriodical) {
      reference.value.legalPeriodical = legalPeriodical
    }
  },
  { immediate: true },
)

watch(
  () => props.modelValue,
  () => {
    reference.value = new Reference({ ...props.modelValue })
    validationStore.reset()

    relatedDocumentationUnit.value = props.modelValue?.documentationUnit
      ? new RelatedDocumentation({ ...props.modelValue.documentationUnit })
      : new RelatedDocumentation()
  },
)

watch(
  () => relatedDocumentationUnit.value,
  () => {
    createDocumentationUnitParameters.value = {
      documentationOffice:
        relatedDocumentationUnit.value.court?.responsibleDocOffice,
      documentType: relatedDocumentationUnit.value.documentType,
      decisionDate: relatedDocumentationUnit.value.decisionDate,
      fileNumber: relatedDocumentationUnit.value.fileNumber,
      court: relatedDocumentationUnit.value.court,
      reference: reference.value,
    }
  },
  { deep: true },
)

/** watches the changes of query related documentations params
 * resets the page if change took place.
 */
watch(searchResultsCurrentPage, () => {
  pageNumber.value = 0
})

/** Opens up the side panel, if only one search result found
 */
watch(searchResults, async () => {
  if (searchResults.value?.length == 1) {
    await openSidePanelAndScrollToSection(
      searchResults.value[0].decision.documentNumber,
    )
  }
})

onMounted(async () => {
  featureToggle.value = (
    await FeatureToggleService.isEnabled("neuris.new-from-search")
  ).data
  await scrollIntoViewportById("periodical-references")

  if (!parentRef.value) return
  resizeObserver.observe(parentRef.value)
})

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
})
</script>

<template>
  <div
    id="periodical-references"
    ref="parentRef"
    v-ctrl-enter="search"
    class="flex flex-col border-b-1 border-b-gray-400"
  >
    <PopupModal
      v-if="showModal"
      aria-label="Dialog zur Auswahl der Löschaktion"
      content-text="Die dazugehörige Dokumentationseinheit existiert noch. Soll sie gelöscht werden?"
      header-text="Dazugehörige Dokumentationseinheit löschen?"
      primary-button-text="Dokumentationseinheit löschen"
      primary-button-type="destructive"
      secondary-button-text="Nur Fundstelle löschen"
      secondary-button-type="tertiary"
      @close-modal="toggleDeletionConfirmationModal(false)"
      @primary-action="deleteReferenceAndDocUnit"
      @secondary-action="deleteReference"
    />
    <h2 v-if="!isSaved" id="reference-input" class="ris-label1-bold mb-16">
      Fundstelle hinzufügen
    </h2>
    <div class="flex flex-col gap-24">
      <DecisionSummary
        v-if="
          reference.documentationUnit &&
          reference.documentationUnit?.documentNumber
        "
        data-testid="reference-input-summary"
        :display-mode="DisplayMode.SIDEPANEL"
        :document-number="reference.documentationUnit.documentNumber"
        :status="reference.documentationUnit.status"
        :summary="reference.documentationUnit.renderSummary"
      />
      <div v-if="!isSaved" class="flex items-center gap-16">
        <div class="flex items-center">
          <InputField
            id="caselaw"
            class="flex items-center"
            label="Rechtsprechung"
            label-class="ris-body1-regular"
            :label-position="LabelPosition.RIGHT"
          >
            <RadioButton
              v-model="reference.referenceType"
              aria-label="Rechtsprechung Fundstelle"
              name="referenceType"
              value="caselaw"
              @click="validationStore.reset()"
            />
          </InputField>
        </div>

        <div class="flex items-center">
          <InputField
            id="literature"
            class="flex items-center"
            label="Literatur"
            label-class="ris-body1-regular"
            :label-position="LabelPosition.RIGHT"
          >
            <RadioButton
              v-model="reference.referenceType"
              aria-label="Literatur Fundstelle"
              name="referenceType"
              size="medium"
              value="literature"
              @click="validationStore.reset()"
            />
          </InputField>
        </div>
      </div>
      <div :class="layoutClass">
        <div id="citationInputField" class="flex w-full flex-col">
          <InputField
            v-if="!isSaved"
            id="citation"
            v-slot="slotProps"
            label="Zitatstelle *"
            :validation-error="validationStore.getByField('citation')"
          >
            <div class="flex flex-grow flex-row gap-16">
              <InputText
                v-if="prefix"
                id="citation prefix"
                v-model="prefix"
                aria-label="Zitatstelle Präfix"
                placeholder="Präfix"
                readonly
                size="small"
              ></InputText>
              <InputText
                id="citation"
                v-model="reference.citation"
                aria-label="Zitatstelle *"
                fluid
                :invalid="slotProps.hasError"
                placeholder="Variable"
                size="small"
                @focus="validationStore.remove('citation')"
              ></InputText>
              <InputText
                v-if="suffix"
                id="citation suffix"
                v-model="suffix"
                aria-label="Zitatstelle Suffix"
                placeholder="Suffix"
                readonly
                size="small"
              ></InputText>
            </div>
          </InputField>

          <InputField
            v-else
            id="citation"
            v-slot="slotProps"
            label="Zitatstelle *"
            :validation-error="validationStore.getByField('citation')"
          >
            <div class="flex flex-grow flex-row gap-16">
              <InputText
                id="citation"
                v-model="reference.citation"
                aria-label="Zitatstelle *"
                fluid
                :invalid="slotProps.hasError"
                size="small"
                @blur="validateRequiredInput(reference)"
                @focus="validationStore.remove('citation')"
              ></InputText>
            </div>
          </InputField>

          <div v-if="legalPeriodical" class="ris-label3-regular pt-4">
            Zitierbeispiel: {{ legalPeriodical.value.citationStyle }}
          </div>
        </div>

        <InputField
          v-if="reference.referenceType === 'caselaw'"
          id="referenceSupplement"
          v-slot="slotProps"
          label="Klammernzusatz *"
          :validation-error="validationStore.getByField('referenceSupplement')"
        >
          <InputText
            id="referenceSupplement"
            v-model="reference.referenceSupplement"
            aria-label="Klammernzusatz"
            fluid
            :invalid="slotProps.hasError"
            size="small"
            @blur="validateRequiredInput(reference)"
            @focus="validationStore.remove('referenceSupplement')"
          ></InputText>
        </InputField>
        <InputField
          v-if="reference.referenceType === 'literature'"
          id="literatureReferenceDocumentType"
          v-slot="slotProps"
          label="Dokumenttyp *"
          :validation-error="validationStore.getByField('documentType')"
        >
          <ComboboxInput
            id="literatureReferenceDocumentType"
            v-model="reference.documentType"
            aria-label="Dokumenttyp Literaturfundstelle"
            :has-error="slotProps.hasError"
            :item-service="
              ComboboxItemService.getDependentLiteratureDocumentTypes
            "
            @focus="validationStore.remove('documentType')"
          ></ComboboxInput>
        </InputField>
      </div>
      <div v-if="reference.referenceType === 'literature'">
        <InputField
          id="literatureReferenceAuthor"
          v-slot="slotProps"
          label="Autor *"
          :validation-error="validationStore.getByField('author')"
        >
          <InputText
            id="literatureReferenceAuthor"
            v-model="reference.author"
            aria-label="Autor Literaturfundstelle"
            fluid
            :invalid="slotProps.hasError"
            size="small"
            @focus="validationStore.remove('author')"
          ></InputText>
        </InputField>
      </div>

      <div
        v-if="!reference?.documentationUnit?.hasForeignSource"
        id="documentationUnit"
      >
        <h2 class="ris-label1-bold mb-16">Entscheidung hinzufügen</h2>

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
                v-model="relatedDocumentationUnit.court"
                aria-label="Gericht"
                clear-on-choosing-item
                :has-error="slotProps.hasError"
                :item-service="ComboboxItemService.getCourts"
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
                v-model="relatedDocumentationUnit.decisionDate"
                aria-label="Entscheidungsdatum"
                :has-error="slotProps.hasError"
                @focus="validationStore.remove('decisionDate')"
                @update:validation-error="slotProps.updateValidationError"
              ></DateInput>
            </InputField>
          </div>

          <div class="flex justify-between gap-24">
            <InputField
              id="fileNumber"
              v-slot="slotProps"
              label="Aktenzeichen"
              :validation-error="validationStore.getByField('fileNumber')"
            >
              <InputText
                id="fileNumber"
                v-model="relatedDocumentationUnit.fileNumber"
                aria-label="Aktenzeichen"
                fluid
                :invalid="slotProps.hasError"
                size="small"
                @focus="validationStore.remove('fileNumber')"
              ></InputText>
            </InputField>
            <InputField id="decisionDocumentType" label="Dokumenttyp">
              <ComboboxInput
                id="decisionDocumentType"
                v-model="relatedDocumentationUnit.documentType"
                aria-label="Dokumenttyp"
                :item-service="ComboboxItemService.getDocumentTypes"
              ></ComboboxInput>
            </InputField>
          </div>
        </div>
      </div>

      <div class="flex w-full flex-row justify-between">
        <div>
          <div class="flex gap-16">
            <Button
              v-if="!isSaved && !reference?.documentationUnit?.hasForeignSource"
              aria-label="Nach Entscheidung suchen"
              label="Suchen"
              size="small"
              @click="search"
            ></Button>
            <Button
              v-if="isSaved || reference?.documentationUnit?.hasForeignSource"
              aria-label="Fundstelle vermerken"
              data-testid="previous-decision-save-button"
              :disabled="reference.isEmpty"
              label="Übernehmen"
              severity="secondary"
              size="small"
              @click.stop="addReference(relatedDocumentationUnit)"
            ></Button>
            <Button
              v-if="isSaved || reference?.documentationUnit?.hasForeignSource"
              aria-label="Abbrechen"
              label="Abbrechen"
              size="small"
              text
              @click.stop="emit('cancelEdit')"
            ></Button>
          </div>
        </div>
        <div v-if="isSaved">
          <Button
            v-if="
              reference?.documentationUnit?.status?.publicationStatus ===
                PublicationState.UNPUBLISHED &&
              reference?.getIsDocumentationUnitCreatedByReference()
            "
            aria-label="Eintrag löschen"
            label="Eintrag löschen"
            severity="danger"
            size="small"
            @click.stop="toggleDeletionConfirmationModal(true)"
          ></Button>

          <Button
            v-else-if="
              reference.documentationUnit?.status?.publicationStatus ===
                PublicationState.EXTERNAL_HANDOVER_PENDING &&
              reference?.getIsDocumentationUnitCreatedByReference()
            "
            aria-label="Fundstelle und Dokumentationseinheit löschen"
            label="Fundstelle und Dokumentationseinheit löschen"
            severity="danger"
            size="small"
            @click.stop="deleteReferenceAndDocUnit"
          ></Button>
          <Button
            v-else
            aria-label="Eintrag löschen"
            label="Eintrag löschen"
            severity="danger"
            size="small"
            @click.stop="modelValue && emit('removeEntry', modelValue)"
          ></Button>
        </div>
      </div>
      <div v-if="isLoading || searchResults" class="bg-blue-200">
        <Pagination
          navigation-position="bottom"
          :page="searchResultsCurrentPage"
          @update-page="updatePage"
        >
          <SearchResultList
            allow-multiple-links
            :display-mode="DisplayMode.SIDEPANEL"
            :is-loading="isLoading"
            :search-results="searchResults"
            @link-decision="addReference"
          />
        </Pagination>
      </div>
      <CreateNewFromSearch
        v-if="searchResults && featureToggle"
        :parameters="createDocumentationUnitParameters"
        :validate-required-input="() => validateRequiredInput(reference)"
        @created-documentation-unit="addReferenceWithCreatedDocumentationUnit"
      />
    </div>
  </div>
</template>
@/stores/editionStore @/composables/useScrollTo
