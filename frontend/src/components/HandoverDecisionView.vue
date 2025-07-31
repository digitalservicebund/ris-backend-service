<script lang="ts" setup>
import { storeToRefs } from "pinia"
import Button from "primevue/button"
import { computed, onMounted, Ref, ref } from "vue"
import { RouterLink } from "vue-router"
import ExpandableContent from "./ExpandableContent.vue"
import CodeSnippet from "@/components/CodeSnippet.vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import HandoverDuplicateCheckView from "@/components/HandoverDuplicateCheckView.vue"
import InfoModal from "@/components/InfoModal.vue"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import PopupModal from "@/components/PopupModal.vue"
import ScheduledPublishingDateTime from "@/components/ScheduledPublishingDateTime.vue"
import HandoverTextCheckView from "@/components/text-check/HandoverTextCheckView.vue"
import TitleElement from "@/components/TitleElement.vue"
import { useFeatureToggle } from "@/composables/useFeatureToggle"
import ActiveCitation, { activeCitationLabels } from "@/domain/activeCitation"
import {
  contentRelatedIndexingLabels,
  Decision,
  longTextLabels,
  shortTextLabels,
} from "@/domain/decision"
import EnsuingDecision, {
  ensuingDecisionFieldLabels,
} from "@/domain/ensuingDecision"
import EventRecord, {
  EventRecordType,
  HandoverMail,
  Preview,
} from "@/domain/eventRecord"
import { DuplicateRelationStatus } from "@/domain/managementData"
import PreviousDecision, {
  previousDecisionFieldLabels,
} from "@/domain/previousDecision"
import { fieldLabels } from "@/fields/caselaw"
import borderNumberService from "@/services/borderNumberService"
import handoverDocumentationUnitService from "@/services/handoverDocumentationUnitService"
import { ResponseError } from "@/services/httpClient"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"
import useSessionStore from "@/stores/sessionStore"
import IconCheck from "~icons/ic/baseline-check"
import IconErrorOutline from "~icons/ic/baseline-error-outline"
import IconInfoOutline from "~icons/ic/baseline-info"
import IconKeyboardArrowDown from "~icons/ic/baseline-keyboard-arrow-down"
import IconKeyboardArrowUp from "~icons/ic/baseline-keyboard-arrow-up"

const props = defineProps<{
  eventLog?: EventRecord[]
  errorMessage?: ResponseError
  succeedMessage?: { title: string; description: string }
}>()

const emits = defineEmits<{
  handoverDocument: []
}>()

const store = useDocumentUnitStore()
const { documentUnit: decision } = storeToRefs(store) as {
  documentUnit: Ref<Decision | undefined>
}
const sessionStore = useSessionStore()
const { env } = storeToRefs(sessionStore)

const categoriesRoute = computed(() => ({
  name: "caselaw-documentUnit-documentNumber-categories",
  params: { documentNumber: decision.value!.documentNumber },
}))
const isFirstTimeHandover = computed(() => {
  return !props.eventLog || props.eventLog.length === 0
})

const textCheckAllToggle = useFeatureToggle("neuris.text-check-handover")

const preview = ref<Preview>()
const frontendError = ref()
const previewError = ref()
const errorMessage = computed(
  () => frontendError.value ?? previewError.value ?? props.errorMessage,
)

onMounted(async () => {
  // Save doc unit in case there are any unsaved local changes before fetching xml preview
  await store.updateDocumentUnit()

  await fetchPreview()
})

async function fetchPreview() {
  if (
    fieldsMissing.value ||
    isOutlineInvalid.value ||
    isCaseFactsInvalid.value ||
    isDecisionReasonsInvalid.value
  )
    return

  const previewResponse = await handoverDocumentationUnitService.getPreview(
    decision.value!.uuid,
  )
  if (previewResponse.error) {
    previewError.value = previewResponse.error
  } else if (previewResponse.data?.xml) {
    preview.value = previewResponse.data
  }
}

function handoverDocumentUnit() {
  if (fieldsMissing.value) {
    frontendError.value = {
      title: "Es sind noch nicht alle Pflichtfelder befüllt.",
      description: "Die Dokumentationseinheit kann nicht übergeben werden.",
    }
  } else if (isOutlineInvalid.value) {
    frontendError.value = {
      title: "Gliederung und Sonstiger Orientierungssatz sind befüllt.",
      description: "Die Dokumentationseinheit kann nicht übergeben werden.",
    }
  } else if (isCaseFactsInvalid.value) {
    frontendError.value = {
      title: "Gründe und Tatbestand sind befüllt.",
      description: "Die Dokumentationseinheit kann nicht übergeben werden.",
    }
  } else if (isDecisionReasonsInvalid.value) {
    frontendError.value = {
      title: "Gründe und Entscheidungsgründe sind befüllt.",
      description: "Die Dokumentationseinheit kann nicht übergeben werden.",
    }
  } else if (
    pendingDuplicates.value?.length ||
    !areBorderNumbersAndLinksValid.value ||
    fieldsWithoutJdvExport.value.length > 0
  ) {
    // With active warnings, you need to confirm a modal before handing over
    const warnings: string[] = []
    if (pendingDuplicates.value?.length)
      warnings.push("Es besteht Dublettenverdacht.")
    if (!areBorderNumbersAndLinksValid.value)
      warnings.push("Die Randnummern sind nicht korrekt.")
    if (fieldsWithoutJdvExport.value.length > 0)
      warnings.push(
        "Die folgenden Rubriken können nicht an die jDV exportiert werden: \n" +
          fieldsWithoutJdvExport.value.map((field) => `- ${field}`).join(", "),
      )
    warnings.push("Wollen Sie das Dokument dennoch übergeben?")
    warningModalReasons.value = warnings.join("\n\n")
    showHandoverWarningModal.value = true
  } else {
    emits("handoverDocument")
  }
}

//Required Core Data fields
const missingCoreDataFields = ref(
  decision.value!.missingRequiredFields.map((field) => fieldLabels[field]),
)

const pendingDuplicates = ref(
  decision.value!.managementData.duplicateRelations.filter(
    (relation) => relation.status === DuplicateRelationStatus.PENDING,
  ),
)

// Labels of non-empty fields that won't be exported to the jDV
const fieldsWithoutJdvExport = computed<string[]>(() => {
  const fieldLabels: string[] = []
  if (decision.value?.contentRelatedIndexing.evsf)
    fieldLabels.push(contentRelatedIndexingLabels.evsf)
  return fieldLabels
})

//Required Previous Decision fields
const missingPreviousDecisionFields = ref(
  decision.value && decision.value.previousDecisions
    ? decision.value.previousDecisions
        .filter((previousDecision) => {
          return (
            getMissingPreviousDecisionFields(
              previousDecision as PreviousDecision,
            ).length > 0
          )
        })
        .map((previousDecision) => {
          return {
            identifier: previousDecision.renderSummary,
            missingFields: getMissingPreviousDecisionFields(
              previousDecision as PreviousDecision,
            ),
          }
        })
    : [],
)

function getMissingPreviousDecisionFields(previousDecision: PreviousDecision) {
  return previousDecision.missingRequiredFields.map(
    (field) => previousDecisionFieldLabels[field],
  )
}

//Required Ensuing Decision fields
const missingEnsuingDecisionFields = ref(
  decision.value && decision.value.ensuingDecisions
    ? store
        .documentUnit!.ensuingDecisions?.filter((ensuingDecision) => {
          return (
            getMissingEnsuingDecisionFields(ensuingDecision as EnsuingDecision)
              .length > 0
          )
        })
        .map((ensuingDecision) => {
          return {
            identifier: ensuingDecision.renderSummary,
            missingFields: getMissingEnsuingDecisionFields(
              ensuingDecision as EnsuingDecision,
            ),
          }
        })
    : [],
)

function getMissingEnsuingDecisionFields(ensuingDecision: EnsuingDecision) {
  return ensuingDecision.missingRequiredFields.map(
    (field) => ensuingDecisionFieldLabels[field],
  )
}

function getHeader(item: EventRecord) {
  switch (item.type) {
    case EventRecordType.HANDOVER_REPORT:
      return "Juris Protokoll - " + item.getDate()
    case EventRecordType.HANDOVER:
      return "Xml Email Abgabe - " + item.getDate()
    case EventRecordType.MIGRATION:
      return "Letzter Import/Delta Migration - " + item.getDate()
    default:
      return "Unbekanntes Ereignis - " + item.getDate()
  }
}

//Required Norms fields
const missingNormsFields = ref(
  store
    .documentUnit!.contentRelatedIndexing?.norms?.filter((normReference) => {
      return normReference.hasMissingFieldsInLegalForce
    })
    .map((normReference) => {
      return {
        identifier: normReference.renderSummary,
        missingFields: ["Gesetzeskraft"],
      }
    }),
)

const warningModalReasons = ref("")
const showHandoverWarningModal = ref(false)

function confirmHandoverDialog() {
  emits("handoverDocument")
  showHandoverWarningModal.value = false
}

const borderNumberValidationResult = ref(
  borderNumberService.validateBorderNumbers(),
)
const borderNumberLinkValidationResult = ref(
  borderNumberService.validateBorderNumberLinks(),
)
const areBorderNumbersAndLinksValid = computed<boolean>(
  () =>
    borderNumberValidationResult.value.isValid &&
    borderNumberLinkValidationResult.value.isValid,
)

// We want to display the action with a fake delay (recalculation only takes a couple of ms)
const showRecalculatingBorderNumbersFakeDelay = ref(false)

async function recalculateBorderNumbers() {
  showRecalculatingBorderNumbersFakeDelay.value = true
  borderNumberService.makeBorderNumbersSequential()

  borderNumberValidationResult.value =
    borderNumberService.validateBorderNumbers()
  borderNumberLinkValidationResult.value =
    borderNumberService.validateBorderNumberLinks()

  setTimeout(
    () => (showRecalculatingBorderNumbersFakeDelay.value = false),
    3_000,
  )
  // Preview is generated by the backend, so the updated border numebers need to be saved first.
  await store.updateDocumentUnit()
  await fetchPreview()
}

//Required Active Citation fields
const missingActiveCitationFields = ref(
  decision.value &&
    decision.value.contentRelatedIndexing &&
    decision.value.contentRelatedIndexing.activeCitations
    ? store
        .documentUnit!.contentRelatedIndexing?.activeCitations?.filter(
          (activeCitation) => {
            return (
              getActiveCitationsFields(activeCitation as ActiveCitation)
                .length > 0
            )
          },
        )
        .map((activeCitation) => {
          return {
            identifier: activeCitation.renderSummary,
            missingFields: getActiveCitationsFields(
              activeCitation as ActiveCitation,
            ),
          }
        })
    : [],
)

function getActiveCitationsFields(activeCitation: ActiveCitation) {
  return activeCitation.missingRequiredFields.map(
    (field) => activeCitationLabels[field],
  )
}

const fieldsMissing = computed(() => {
  return (
    !!missingCoreDataFields.value.length ||
    !!missingPreviousDecisionFields.value?.length ||
    !!missingEnsuingDecisionFields.value?.length ||
    !!missingNormsFields.value?.length ||
    !!missingActiveCitationFields.value?.length
  )
})

const isOutlineInvalid = computed<boolean>(
  () =>
    // Outline is written into otherHeadnote in jdv -> Only one of the fields may be filled at the same time
    !!decision.value?.longTexts.outline &&
    !!decision.value.shortTexts.otherHeadnote,
)

const isCaseFactsInvalid = computed<boolean>(
  () =>
    !!decision.value?.longTexts.reasons &&
    !!decision.value?.longTexts.caseFacts,
)
const isDecisionReasonsInvalid = computed<boolean>(
  () =>
    !!decision.value?.longTexts.reasons &&
    !!decision.value?.longTexts.decisionReasons,
)

const isScheduled = computed<boolean>(
  () => !!decision.value!.managementData.scheduledPublicationDateTime,
)

const hasImages = computed<boolean>(
  () => !!preview.value?.xml?.includes("<img"),
)

const isPublishable = computed<boolean>(
  () =>
    !isOutlineInvalid.value &&
    !fieldsMissing.value &&
    !isCaseFactsInvalid.value &&
    !isDecisionReasonsInvalid.value &&
    !!preview.value?.success &&
    !hasImages.value,
)
</script>

<template>
  <div v-if="decision">
    <div class="flex flex-col gap-24 bg-white p-24">
      <TitleElement>Übergabe an jDV</TitleElement>

      <div aria-label="Plausibilitätsprüfung" class="flex flex-col">
        <h2 class="ris-label1-bold mb-16">Plausibilitätsprüfung</h2>

        <div
          v-if="
            fieldsMissing ||
            isOutlineInvalid ||
            isCaseFactsInvalid ||
            isDecisionReasonsInvalid
          "
        >
          <div class="flex flex-row gap-8">
            <IconErrorOutline class="text-red-800" />

            <div class="ris-body1-regular flex flex-col">
              <div v-if="fieldsMissing" class="flex flex-col gap-24">
                <div>
                  <p>
                    Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:
                  </p>
                  <ul class="list-disc">
                    <li v-for="field in missingCoreDataFields" :key="field">
                      {{ field }}
                    </li>
                    <li
                      v-if="
                        missingPreviousDecisionFields &&
                        missingPreviousDecisionFields.length > 0
                      "
                    >
                      Vorgehende Entscheidungen
                      <ul>
                        <li
                          v-for="fields in missingPreviousDecisionFields"
                          :key="missingPreviousDecisionFields.indexOf(fields)"
                          class="ris-body1-regular ml-[1rem]"
                        >
                          <div v-if="fields && fields.missingFields.length > 0">
                            <span>{{ fields.identifier }}</span>
                            -
                            <span class="ris-label2-bold">{{
                              fields.missingFields.join(", ")
                            }}</span>
                          </div>
                        </li>
                      </ul>
                    </li>
                    <li
                      v-if="
                        missingEnsuingDecisionFields &&
                        missingEnsuingDecisionFields.length > 0
                      "
                      class="ris-body1-regular ml-[1rem]"
                    >
                      Nachgehende Entscheidungen
                      <ul>
                        <li
                          v-for="fields in missingEnsuingDecisionFields"
                          :key="missingEnsuingDecisionFields.indexOf(fields)"
                          class="ris-body1-regular ml-[1rem]"
                        >
                          <div v-if="fields && fields.missingFields.length > 0">
                            <span>{{ fields.identifier }}</span>
                            -
                            <span class="ris-label2-bold">{{
                              fields.missingFields.join(", ")
                            }}</span>
                          </div>
                        </li>
                      </ul>
                    </li>
                    <li
                      v-if="missingNormsFields && missingNormsFields.length > 0"
                      class="ris-body1-regular ml-[1rem]"
                    >
                      Normen
                      <ul>
                        <li
                          v-for="fields in missingNormsFields"
                          :key="missingNormsFields.indexOf(fields)"
                          class="ris-body1-regular ml-[1rem]"
                        >
                          <div v-if="fields && fields.missingFields.length > 0">
                            <span>{{ fields.identifier }}</span>
                            -
                            <span class="ris-label2-bold">{{
                              fields.missingFields.join(", ")
                            }}</span>
                          </div>
                        </li>
                      </ul>
                    </li>
                    <li
                      v-if="
                        missingActiveCitationFields &&
                        missingActiveCitationFields.length > 0
                      "
                      class="ris-body1-regular ml-[1rem]"
                    >
                      Aktivzitierung
                      <ul>
                        <li
                          v-for="fields in missingActiveCitationFields"
                          :key="missingActiveCitationFields.indexOf(fields)"
                          class="ris-body1-regular ml-[1rem]"
                        >
                          <div v-if="fields && fields.missingFields.length > 0">
                            <span>{{ fields.identifier }}</span>
                            -
                            <span class="ris-label2-bold">{{
                              fields.missingFields.join(", ")
                            }}</span>
                          </div>
                        </li>
                      </ul>
                    </li>
                  </ul>
                </div>
              </div>
              <div
                v-if="
                  isOutlineInvalid ||
                  isCaseFactsInvalid ||
                  isDecisionReasonsInvalid
                "
                class="mb-24 flex flex-col gap-8"
              >
                <div v-if="isOutlineInvalid">
                  Die Rubriken "Gliederung" und "Sonstiger Orientierungssatz"
                  sind befüllt.<br />
                  Es darf nur eine der beiden Rubriken befüllt sein.
                </div>
                <div v-if="isCaseFactsInvalid">
                  Die Rubriken "Gründe" und "Tatbestand" sind befüllt.<br />
                  Es darf nur eine der beiden Rubriken befüllt sein.
                </div>
                <div v-if="isDecisionReasonsInvalid">
                  Die Rubriken "Gründe" und "Entscheidungsgründe" sind
                  befüllt.<br />
                  Es darf nur eine der beiden Rubriken befüllt sein.
                </div>
              </div>
            </div>
          </div>
          <RouterLink class="inline-block" :to="categoriesRoute">
            <Button
              aria-label="Rubriken bearbeiten"
              class="w-fit"
              label="Rubriken bearbeiten"
              severity="secondary"
              size="small"
            ></Button>
          </RouterLink>
        </div>
        <div v-else class="flex flex-row gap-8">
          <IconCheck class="text-green-700" />
          <p>Alle Pflichtfelder sind korrekt ausgefüllt.</p>
        </div>

        <div v-if="fieldsWithoutJdvExport.length > 0" class="mt-16">
          <div class="flex flex-row gap-8">
            <IconInfoOutline class="text-blue-800" />

            <div class="ris-body1-regular">
              Folgende Rubriken sind befüllt und können nicht an die jDV
              exportiert werden:
              <ul class="list-disc">
                <li v-for="field in fieldsWithoutJdvExport" :key="field">
                  {{ field }}
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
      <div aria-label="Randnummernprüfung" class="flex flex-col">
        <h2 class="ris-label1-bold mb-16">Randnummernprüfung</h2>

        <div v-if="!areBorderNumbersAndLinksValid">
          <div class="flex flex-row gap-8">
            <IconErrorOutline class="text-red-800" />

            <div class="ris-body1-regular flex flex-col gap-24">
              <div
                v-if="
                  !borderNumberValidationResult.isValid &&
                  !borderNumberValidationResult.hasError
                "
              >
                Die Reihenfolge der Randnummern ist nicht korrekt.
                <dl class="my-16">
                  <div class="grid grid-cols-2 gap-24 px-0">
                    <dt class="ris-label2-bold self-center">Rubrik</dt>
                    <dd class="ris-body2-regular">
                      {{
                        longTextLabels[
                          borderNumberValidationResult.invalidCategory
                        ]
                      }}
                    </dd>
                  </div>
                  <div class="grid grid-cols-2 gap-24 px-0">
                    <dt class="ris-label2-bold self-center">
                      Erwartete Randnummer
                    </dt>
                    <dd class="ris-body2-regular">
                      {{ borderNumberValidationResult.expectedBorderNumber }}
                    </dd>
                  </div>
                  <div class="grid grid-cols-2 gap-24 px-0">
                    <dt class="ris-label2-bold self-center">
                      Tatsächliche Randnummer
                    </dt>
                    <dd class="ris-body2-regular">
                      {{
                        borderNumberValidationResult.firstInvalidBorderNumber
                      }}
                    </dd>
                  </div>
                </dl>
              </div>
              <div v-if="!borderNumberLinkValidationResult.isValid">
                Es gibt ungültige Randnummern-Verweise in folgenden Rubriken:
                <ul class="list-disc">
                  <li
                    v-for="key in borderNumberLinkValidationResult.invalidCategories"
                    :key="key"
                  >
                    {{
                      shortTextLabels[key as keyof typeof shortTextLabels] ??
                      longTextLabels[key as keyof typeof longTextLabels]
                    }}
                  </li>
                </ul>
              </div>
              <div v-if="borderNumberValidationResult.hasError">
                Bei der Randnummernprüfung ist ein Fehler aufgetreten. Bitte
                prüfen Sie die Randnummern manuell und informieren Sie den
                Support.
              </div>
            </div>
          </div>
          <Button
            v-if="
              !borderNumberValidationResult.isValid &&
              !borderNumberValidationResult.hasError &&
              !showRecalculatingBorderNumbersFakeDelay
            "
            aria-label="Randnummern neu berechnen"
            button-type="tertiary"
            class="mt-8 w-fit"
            label="Randnummern neu berechnen"
            size="small"
            @click="recalculateBorderNumbers"
          />
        </div>
        <div
          v-else-if="!showRecalculatingBorderNumbersFakeDelay"
          class="flex flex-row gap-8"
        >
          <IconCheck class="text-green-700" />
          <p>Die Reihenfolge der Randnummern ist korrekt.</p>
        </div>
        <div
          v-if="showRecalculatingBorderNumbersFakeDelay"
          class="flex flex-row gap-8"
        >
          <LoadingSpinner size="small" />
          <p>Die Randnummern werden neu berechnet</p>
        </div>
      </div>
      <HandoverDuplicateCheckView
        :document-number="decision!.documentNumber"
        :pending-duplicates="pendingDuplicates"
      />

      <HandoverTextCheckView
        v-if="textCheckAllToggle"
        :document-id="decision!.uuid"
        :document-number="decision!.documentNumber"
      />

      <div class="border-b-1 border-b-gray-400"></div>
      <ExpandableContent
        v-if="
          !fieldsMissing &&
          !isOutlineInvalid &&
          !isCaseFactsInvalid &&
          !isDecisionReasonsInvalid &&
          preview?.success &&
          !!preview?.xml
        "
        as-column
        class="border-b-1 border-gray-400 pb-24"
        :data-set="preview"
        header="XML Vorschau"
        header-class="ris-body1-bold"
        :is-expanded="false"
        title="XML Vorschau"
      >
        <CodeSnippet title="" :xml="preview.xml" />
      </ExpandableContent>
      <InfoModal
        v-if="errorMessage"
        aria-label="Fehler bei jDV Übergabe"
        class="mt-8"
        :description="errorMessage.description"
        :title="errorMessage.title"
      />
      <InfoModal
        v-else-if="succeedMessage"
        aria-label="Erfolg der jDV Übergabe"
        class="mt-8"
        v-bind="succeedMessage"
        :status="InfoStatus.SUCCEED"
      />
      <PopupModal
        v-if="showHandoverWarningModal"
        aria-label="Bestätigung für Übergabe bei Fehlern"
        :content-text="warningModalReasons"
        header-text="Prüfung hat Warnungen ergeben"
        primary-button-text="Trotzdem übergeben"
        primary-button-type="primary"
        @close-modal="showHandoverWarningModal = false"
        @primary-action="confirmHandoverDialog"
      />

      <InfoModal
        v-if="env === 'uat'"
        :description="[
          'Dokumentationseinheiten werden in der jDV ohne Dokumentnummer erstellt',
          'Diese sind auffindbar über Gericht=VGH Mannheim und Aktenzeichen und/oder Entscheidungsdatum der Entscheidung',
          'Die Dokumentationseinheiten müssen manuell in der jDV gelöscht werden',
        ]"
        :status="InfoStatus.INFO"
        title="UAT Testmodus für die Übergabe an die jDV"
      />

      <InfoModal
        v-if="hasImages"
        aria-label="Übergabe an die jDV nicht möglich"
        class="mt-8"
        description="Diese Entscheidung enthält Bilder und kann deshalb nicht an die jDV übergeben werden"
        :status="InfoStatus.INFO"
        title="Übergabe an die jDV nicht möglich"
      />

      <div>
        <Button
          aria-label="Dokumentationseinheit an jDV übergeben"
          :disabled="!isPublishable || isScheduled"
          label="Dokumentationseinheit an jDV übergeben"
          size="small"
          @click="handoverDocumentUnit"
        >
          <template #icon>
            <IconCheck />
          </template>
        </Button>
      </div>

      <ScheduledPublishingDateTime :is-publishable="isPublishable" />

      <div aria-label="Letzte Ereignisse">
        <h2 class="ris-label1-bold mb-16">Letzte Ereignisse</h2>
        <div class="flex flex-col gap-24">
          <p v-if="isFirstTimeHandover">
            Diese Dokumentationseinheit wurde bisher nicht an die jDV übergeben
          </p>
          <div v-else class="flex flex-col gap-24">
            <div v-for="(item, index) in eventLog" :key="index">
              <ExpandableContent
                as-column
                :data-set="item"
                :header="getHeader(item)"
                :is-expanded="index == 0"
                :title="item.type"
              >
                <template #open-icon>
                  <IconKeyboardArrowDown />
                </template>

                <template #close-icon>
                  <IconKeyboardArrowUp />
                </template>

                <!-- eslint-disable vue/no-v-html -->
                <div
                  v-if="item.type == EventRecordType.HANDOVER_REPORT"
                  class="p-20"
                  v-html="item.getContent()"
                />
                <div
                  v-else-if="item instanceof HandoverMail"
                  class="flex flex-col gap-24 pt-24"
                >
                  <div class="ris-label2-regular">
                    <div>
                      <span class="ris-label2-bold">E-Mail an:</span>
                      {{ (item as HandoverMail).receiverAddress }}
                    </div>
                    <div>
                      <span class="ris-label2-bold"> Betreff: </span>
                      {{ (item as HandoverMail).mailSubject }}
                    </div>
                  </div>

                  <CodeSnippet
                    v-if="(item as HandoverMail).attachments?.[0]"
                    data-testid="xml-handover-code-snippet-preview"
                    title="XML"
                    :xml="(item as HandoverMail).attachments?.[0].fileContent!"
                  />
                </div>
                <div
                  v-else-if="item.type == EventRecordType.MIGRATION"
                  class="p-20"
                >
                  <CodeSnippet
                    v-if="item.getContent()"
                    title="XML"
                    :xml="item.getContent()"
                  />
                </div>
              </ExpandableContent>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
@/services/handoverDocumentationUnitService @/domain/eventRecord
