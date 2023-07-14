<script lang="ts" setup>
import { ref, computed } from "vue"
import { RouterLink } from "vue-router"
import ExpandableContent from "./ExpandableContent.vue"
import CodeSnippet from "@/components/CodeSnippet.vue"
import ActiveCitation, { activeCitationLabels } from "@/domain/activeCitation"
import DocumentUnit from "@/domain/documentUnit"
import NormReference, { normFieldLabels } from "@/domain/normReference"
import ProceedingDecision, {
  proceedingDecisionFieldLabels,
} from "@/domain/proceedingDecision"
import XmlMail, { PublicationHistoryRecordType } from "@/domain/xmlMail"
import { fieldLabels } from "@/fields/caselaw"
import { ResponseError } from "@/services/httpClient"
import { InfoStatus } from "@/shared/components/enumInfoStatus"
import InfoModal from "@/shared/components/InfoModal.vue"
import TextButton from "@/shared/components/input/TextButton.vue"

const props = defineProps<{
  documentUnit: DocumentUnit
  publishResult?: XmlMail
  publicationLog?: XmlMail[]
  errorMessage?: ResponseError
  succeedMessage?: { title: string; description: string }
}>()

const emits = defineEmits<{
  publishADocument: []
}>()

const categoriesRoute = computed(() => ({
  name: "caselaw-documentUnit-documentNumber-categories",
  params: { documentNumber: props.documentUnit.documentNumber },
}))
const isFirstTimePublication = computed(() => {
  return !props.publicationLog || props.publicationLog.length === 0
})

const frontendError = ref()
const errorMessage = computed(() => frontendError.value ?? props.errorMessage)

function publishDocumentUnit() {
  if (fieldsMissing.value) {
    frontendError.value = {
      title: "Es sind noch nicht alle Pflichtfelder befüllt.",
      description:
        "Die Dokumentationseinheit kann nicht veröffentlicht werden.",
    }
  }

  emits("publishADocument")
}

//Required Core Data fields
const missingCoreDataFields = ref(
  props.documentUnit.missingRequiredFields.map((field) => fieldLabels[field]),
)

//Required Proceeding Decision fields
const missingProceedingDecisionFields = ref(
  props.documentUnit.proceedingDecisions
    ?.filter((proceedingDecision) => {
      return getMissingProceedingDecisionFields(proceedingDecision).length > 0
    })
    .map((proceedingDecision) => {
      return {
        identifier: proceedingDecision.renderDecision,
        missingFields: getMissingProceedingDecisionFields(proceedingDecision),
      }
    }),
)

function getMissingProceedingDecisionFields(
  proceedingDecision: ProceedingDecision,
) {
  return proceedingDecision.missingRequiredFields.map(
    (field) => proceedingDecisionFieldLabels[field],
  )
}

//Required Norms fields
const missingNormsFields = ref(
  props.documentUnit.contentRelatedIndexing?.norms
    ?.filter((normReference) => {
      return getMissingNormsFields(normReference).length > 0
    })
    .map((normReference) => {
      return {
        identifier: normReference.renderDecision,
        missingFields: getMissingNormsFields(normReference),
      }
    }),
)

function getMissingNormsFields(normReference: NormReference) {
  if (
    normReference.normAbbreviation === null &&
    normReference.singleNorm === null &&
    normReference.dateOfRelevance === null &&
    normReference.dateOfVersion === null
  )
    return []
  else {
    return normReference.missingRequiredFields.map(
      (field) => normFieldLabels[field],
    )
  }
}

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

const fieldsMissing = computed(() =>
  missingCoreDataFields.value.length ||
  missingProceedingDecisionFields.value?.length ||
  missingNormsFields.value?.length ||
  missingActiveCitationFields.value?.length
    ? true
    : false,
)
</script>

<template>
  <div class="flex-start flex max-w-[80rem] flex-col justify-start gap-40">
    <h1 class="heading-02-regular">Veröffentlichen</h1>
    <div aria-label="Plausibilitätsprüfung" class="flex flex-row gap-16">
      <div class="w-[15.625rem]">
        <p class="subheading">Plausibilitätsprüfung</p>
      </div>
      <div v-if="fieldsMissing" class="flex flex-row gap-8">
        <div>
          <span class="material-icons rounded-full bg-red-800 text-white">
            error
          </span>
        </div>
        <div class="flex flex-col gap-32">
          <div>
            <p class="body-01-reg">
              Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:
            </p>
            <ul class="list-disc">
              <li
                v-for="field in missingCoreDataFields"
                :key="field"
                class="body-01-reg ml-[1rem] list-item"
              >
                {{ field }}
              </li>
              <li
                v-if="
                  missingProceedingDecisionFields &&
                  missingProceedingDecisionFields.length > 0
                "
                class="body-01-reg ml-[1rem] list-item"
              >
                Rechtszug
                <ul>
                  <li
                    v-for="fields in missingProceedingDecisionFields"
                    :key="missingProceedingDecisionFields.indexOf(fields)"
                    class="body-01-reg ml-[1rem] list-item"
                  >
                    <div v-if="fields && fields.missingFields.length > 0">
                      <span>{{ fields.identifier }}</span>
                      -
                      <span class="label-02-bold">{{
                        fields.missingFields.join(", ")
                      }}</span>
                    </div>
                  </li>
                </ul>
              </li>
              <li
                v-if="missingNormsFields && missingNormsFields.length > 0"
                class="body-01-reg ml-[1rem] list-item"
              >
                Normen
                <ul>
                  <li
                    v-for="fields in missingNormsFields"
                    :key="missingNormsFields.indexOf(fields)"
                    class="body-01-reg ml-[1rem] list-item"
                  >
                    <div v-if="fields && fields.missingFields.length > 0">
                      <span>{{ fields.identifier }}</span>
                      -
                      <span class="label-02-bold">{{
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
                class="body-01-reg ml-[1rem] list-item"
              >
                Aktivzitierung
                <ul>
                  <li
                    v-for="fields in missingActiveCitationFields"
                    :key="missingActiveCitationFields.indexOf(fields)"
                    class="body-01-reg ml-[1rem] list-item"
                  >
                    <div v-if="fields && fields.missingFields.length > 0">
                      <span>{{ fields.identifier }}</span>
                      -
                      <span class="label-02-bold">{{
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
        <span class="material-icons text-green-700"> check </span>
        <p class="body-01-reg">Alle Pflichtfelder sind korrekt ausgefüllt</p>
      </div>
    </div>
    <div class="border-b-1 border-b-gray-400"></div>
    <InfoModal
      v-if="errorMessage"
      aria-label="Fehler bei Veröffentlichung"
      class="mt-8"
      v-bind="errorMessage"
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
      icon="campaign"
      label="Dokumentationseinheit veröffentlichen"
      @click="publishDocumentUnit"
    />
    <div aria-label="Letzte Veröffentlichungen" class="flex flex-col gap-24">
      <h2 class="heading-03-regular">Letzte Veröffentlichungen</h2>
      <p v-if="isFirstTimePublication">
        Diese Dokumentationseinheit wurde bisher nicht veröffentlicht
      </p>
      <div v-else class="flex flex-col gap-24">
        <div v-for="(item, index) in publicationLog" :key="index">
          <ExpandableContent
            as-column
            class="border-b-1 border-r-1 border-gray-400 bg-white p-10"
            close-icon-name="keyboard_arrow_up"
            :data-set="item"
            :header="
              item.type == PublicationHistoryRecordType.PUBLICATION_REPORT
                ? 'Juris Protokoll - ' + item.date
                : 'Xml Email Abgabe - ' + item.date
            "
            header-class="font-bold"
            :is-expanded="index == 0"
            open-icon-name="keyboard_arrow_down"
            :title="item.type"
          >
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
              <div class="label-section pt-20 text-gray-900">ÜBER</div>
              <div class="label-02-regular">
                <div>
                  <span class="label-02-bold">E-Mail an:</span>
                  {{ item.receiverAddress }}
                </div>
                <div>
                  <span class="label-02-bold"> Betreff: </span>
                  {{ item.mailSubject }}
                </div>
                <div>
                  <span class="label-02-bold">Status:</span>
                  {{ item.publishStateDisplayText }}
                </div>
              </div>
              <div class="label-section text-gray-900">ALS</div>
              <CodeSnippet v-if="!!item?.xml" title="XML" :xml="item.xml" />
            </div>
          </ExpandableContent>
        </div>
      </div>
    </div>
  </div>
</template>
