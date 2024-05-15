<script lang="ts" setup>
import { ref, computed, onMounted } from "vue"
import { RouterLink } from "vue-router"
import ExpandableContent from "./ExpandableContent.vue"
import CodeSnippet from "@/components/CodeSnippet.vue"
import { InfoStatus } from "@/components/enumInfoStatus"
import InfoModal from "@/components/InfoModal.vue"
import TextButton from "@/components/input/TextButton.vue"
import ActiveCitation, { activeCitationLabels } from "@/domain/activeCitation"
import DocumentUnit from "@/domain/documentUnit"
import EnsuingDecision, {
  ensuingDecisionFieldLabels,
} from "@/domain/ensuingDecision"
import PreviousDecision, {
  previousDecisionFieldLabels,
} from "@/domain/previousDecision"
import XmlMail, { PublicationHistoryRecordType } from "@/domain/xmlMail"
import { fieldLabels } from "@/fields/caselaw"
import { ResponseError } from "@/services/httpClient"
import publishService from "@/services/publishService"
import IconCheck from "~icons/ic/baseline-check"
import IconErrorOutline from "~icons/ic/baseline-error-outline"
import IconKeyboardArrowDown from "~icons/ic/baseline-keyboard-arrow-down"
import IconKeyboardArrowUp from "~icons/ic/baseline-keyboard-arrow-up"
import IconPublish from "~icons/ic/outline-campaign"

const props = defineProps<{
  documentUnit: DocumentUnit
  publishResult?: XmlMail
  publicationLog?: XmlMail[]
  errorMessage?: ResponseError
  succeedMessage?: { title: string; description: string }
}>()

const emits = defineEmits<{
  publishDocument: []
}>()

const categoriesRoute = computed(() => ({
  name: "caselaw-documentUnit-documentNumber-categories",
  params: { documentNumber: props.documentUnit.documentNumber },
}))
const isFirstTimePublication = computed(() => {
  return !props.publicationLog || props.publicationLog.length === 0
})

const preview = ref<XmlMail>()
const frontendError = ref()
const previewError = ref()
const errorMessage = computed(
  () => frontendError.value ?? previewError.value ?? props.errorMessage,
)

onMounted(async () => {
  if (fieldsMissing.value) return
  const previewResponse = await publishService.getPreview(
    props.documentUnit.uuid,
  )
  if (previewResponse.error) {
    previewError.value = previewResponse.error
  } else if (previewResponse.data?.xml) {
    preview.value = previewResponse.data
  }
})

function publishDocumentUnit() {
  if (fieldsMissing.value) {
    frontendError.value = {
      title: "Es sind noch nicht alle Pflichtfelder befüllt.",
      description:
        "Die Dokumentationseinheit kann nicht veröffentlicht werden.",
    }
  } else {
    emits("publishDocument")
  }
}

//Required Core Data fields
const missingCoreDataFields = ref(
  props.documentUnit.missingRequiredFields.map((field) => fieldLabels[field]),
)

//Required Previous Decision fields
const missingPreviousDecisionFields = ref(
  props.documentUnit.previousDecisions
    ?.filter((previousDecision) => {
      return getMissingPreviousDecisionFields(previousDecision).length > 0
    })
    .map((previousDecision) => {
      return {
        identifier: previousDecision.renderDecision,
        missingFields: getMissingPreviousDecisionFields(previousDecision),
      }
    }),
)

function getMissingPreviousDecisionFields(previousDecision: PreviousDecision) {
  return previousDecision.missingRequiredFields.map(
    (field) => previousDecisionFieldLabels[field],
  )
}

//Required Ensuing Decision fields
const missingEnsuingDecisionFields = ref(
  props.documentUnit.ensuingDecisions
    ?.filter((ensuingDecision) => {
      return getMissingEnsuingDecisionFields(ensuingDecision).length > 0
    })
    .map((ensuingDecision) => {
      return {
        identifier: ensuingDecision.renderDecision,
        missingFields: getMissingEnsuingDecisionFields(ensuingDecision),
      }
    }),
)

function getMissingEnsuingDecisionFields(ensuingDecision: EnsuingDecision) {
  return ensuingDecision.missingRequiredFields.map(
    (field) => ensuingDecisionFieldLabels[field],
  )
}

//Required Norms fields
const missingNormsFields = ref(
  props.documentUnit.contentRelatedIndexing?.norms
    ?.filter((normReference) => {
      return normReference.hasMissingRequiredFields
    })
    .map((normReference) => {
      return {
        identifier: normReference.renderDecision,
        missingFields: ["Gesetzeskraft"],
      }
    }),
)

//Required Active Citation fields
const missingActiveCitationFields = ref(
  props.documentUnit.contentRelatedIndexing?.activeCitations
    ?.filter((activeCitation) => {
      return getActiveCitationsFields(activeCitation).length > 0
    })
    .map((activeCitation) => {
      return {
        identifier: activeCitation.renderDecision,
        missingFields: getActiveCitationsFields(activeCitation),
      }
    }),
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
</script>

<template>
  <div class="flex-start flex max-w-[80rem] flex-col justify-start gap-40">
    <h1 class="ds-heading-02-reg">Veröffentlichen</h1>
    <div aria-label="Plausibilitätsprüfung" class="flex flex-row gap-16">
      <div class="w-[15.625rem]">
        <p class="ds-subhead">Plausibilitätsprüfung</p>
      </div>
      <div v-if="fieldsMissing" class="flex flex-row gap-8">
        <div>
          <IconErrorOutline class="text-red-800" />
        </div>
        <div class="flex flex-col gap-32">
          <div>
            <p class="ds-body-01-reg">
              Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:
            </p>
            <ul class="list-disc">
              <li
                v-for="field in missingCoreDataFields"
                :key="field"
                class="ds-body-01-reg ml-[1rem] list-item"
              >
                {{ field }}
              </li>
              <li
                v-if="
                  missingPreviousDecisionFields &&
                  missingPreviousDecisionFields.length > 0
                "
                class="ds-body-01-reg ml-[1rem] list-item"
              >
                Vorgehende Entscheidungen
                <ul>
                  <li
                    v-for="fields in missingPreviousDecisionFields"
                    :key="missingPreviousDecisionFields.indexOf(fields)"
                    class="ds-body-01-reg ml-[1rem] list-item"
                  >
                    <div v-if="fields && fields.missingFields.length > 0">
                      <span>{{ fields.identifier }}</span>
                      -
                      <span class="ds-label-02-bold">{{
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
                class="ds-body-01-reg ml-[1rem] list-item"
              >
                Nachgehende Entscheidungen
                <ul>
                  <li
                    v-for="fields in missingEnsuingDecisionFields"
                    :key="missingEnsuingDecisionFields.indexOf(fields)"
                    class="ds-body-01-reg ml-[1rem] list-item"
                  >
                    <div v-if="fields && fields.missingFields.length > 0">
                      <span>{{ fields.identifier }}</span>
                      -
                      <span class="ds-label-02-bold">{{
                        fields.missingFields.join(", ")
                      }}</span>
                    </div>
                  </li>
                </ul>
              </li>
              <li
                v-if="missingNormsFields && missingNormsFields.length > 0"
                class="ds-body-01-reg ml-[1rem] list-item"
              >
                Normen
                <ul>
                  <li
                    v-for="fields in missingNormsFields"
                    :key="missingNormsFields.indexOf(fields)"
                    class="ds-body-01-reg ml-[1rem] list-item"
                  >
                    <div v-if="fields && fields.missingFields.length > 0">
                      <span>{{ fields.identifier }}</span>
                      -
                      <span class="ds-label-02-bold">{{
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
                class="ds-body-01-reg ml-[1rem] list-item"
              >
                Aktivzitierung
                <ul>
                  <li
                    v-for="fields in missingActiveCitationFields"
                    :key="missingActiveCitationFields.indexOf(fields)"
                    class="ds-body-01-reg ml-[1rem] list-item"
                  >
                    <div v-if="fields && fields.missingFields.length > 0">
                      <span>{{ fields.identifier }}</span>
                      -
                      <span class="ds-label-02-bold">{{
                        fields.missingFields.join(", ")
                      }}</span>
                    </div>
                  </li>
                </ul>
              </li>
            </ul>
          </div>

          <RouterLink :to="categoriesRoute"
            ><TextButton
              aria-label="Rubriken bearbeiten"
              button-type="tertiary"
              class="w-fit"
              label="Rubriken bearbeiten"
          /></RouterLink>
        </div>
      </div>
      <div v-else class="flex flex-row gap-8">
        <IconCheck class="text-green-700" />
        <p class="ds-body-01-reg">Alle Pflichtfelder sind korrekt ausgefüllt</p>
      </div>
    </div>
    <div class="border-b-1 border-b-gray-400"></div>

    <ExpandableContent
      v-if="!fieldsMissing && preview?.statusCode === '200' && !!preview?.xml"
      as-column
      class="border-b-1 border-r-1 border-gray-400 bg-white p-10"
      :data-set="preview"
      header="XML Vorschau der Veröffentlichung"
      header-class="font-bold"
      :is-expanded="false"
      title="XML Vorschau der Veröffentlichung"
    >
      <CodeSnippet title="" :xml="preview.xml" />
    </ExpandableContent>
    <InfoModal
      v-if="errorMessage"
      aria-label="Fehler bei Veröffentlichung"
      class="mt-8"
      :description="errorMessage.description"
      :title="errorMessage.title"
    />
    <InfoModal
      v-if="succeedMessage"
      aria-label="Erfolg der Veröffentlichung"
      class="mt-8"
      v-bind="succeedMessage"
      :status="InfoStatus.SUCCEED"
    />
    <TextButton
      aria-label="Dokumentationseinheit veröffentlichen"
      button-type="secondary"
      class="w-fit"
      :icon="IconPublish"
      label="Dokumentationseinheit veröffentlichen"
      @click="publishDocumentUnit"
    />
    <div aria-label="Letzte Veröffentlichungen" class="flex flex-col gap-24">
      <h2 class="ds-heading-03-reg">Letzte Veröffentlichungen</h2>
      <p v-if="isFirstTimePublication">
        Diese Dokumentationseinheit wurde bisher nicht veröffentlicht
      </p>
      <div v-else class="flex flex-col gap-24">
        <div v-for="(item, index) in publicationLog" :key="index">
          <ExpandableContent
            as-column
            class="border-b-1 border-r-1 border-gray-400 bg-white p-10"
            :data-set="item"
            :header="
              item.type == PublicationHistoryRecordType.PUBLICATION_REPORT
                ? 'Juris Protokoll - ' + item.date
                : 'Xml Email Abgabe - ' + item.date
            "
            header-class="font-bold"
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
              v-if="
                item.type == PublicationHistoryRecordType.PUBLICATION_REPORT
              "
              class="p-20"
              v-html="item.content"
            />
            <div
              v-else-if="item.type == PublicationHistoryRecordType.PUBLICATION"
            >
              <div class="ds-label-section pt-20 text-gray-900">ÜBER</div>
              <div class="ds-label-02-reg">
                <div>
                  <span class="ds-label-02-bold">E-Mail an:</span>
                  {{ item.receiverAddress }}
                </div>
                <div>
                  <span class="ds-label-02-bold"> Betreff: </span>
                  {{ item.mailSubject }}
                </div>
              </div>
              <div class="ds-label-section text-gray-900">ALS</div>
              <CodeSnippet v-if="!!item?.xml" title="XML" :xml="item.xml" />
            </div>
          </ExpandableContent>
        </div>
      </div>
    </div>
  </div>
</template>
