<script lang="ts" setup>
import { ref, computed } from "vue"
import { RouterLink } from "vue-router"
import CodeSnippet from "@/components/CodeSnippet.vue"
import DocumentUnit from "@/domain/documentUnit"
import NormReference, { normFieldLabels } from "@/domain/normReference"
import ProceedingDecision, {
  proceedingDecisionFieldLabels,
} from "@/domain/proceedingDecision"
import XmlMail from "@/domain/xmlMail"
import { fieldLabels } from "@/fields/caselaw"
import { ResponseError } from "@/services/httpClient"
import { InfoStatus } from "@/shared/components/enumInfoStatus"
import InfoModal from "@/shared/components/InfoModal.vue"
import TextButton from "@/shared/components/input/TextButton.vue"

const props = defineProps<{
  documentUnit: DocumentUnit
  publishResult?: XmlMail
  lastPublishedXmlMail?: XmlMail
  errorMessage?: ResponseError
  succeedMessage?: { title: string; description: string }
}>()

const emits = defineEmits<{
  (e: "publishADocument"): void
}>()

const categoriesRoute = computed(() => ({
  name: "caselaw-documentUnit-:documentNumber-categories",
  params: { documentNumber: props.documentUnit.documentNumber },
}))
const isFirstTimePublication = computed(() => {
  return !props.lastPublishedXmlMail
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
  props.documentUnit.missingRequiredFields.map((field) => fieldLabels[field])
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
    })
)

function getMissingProceedingDecisionFields(
  proceedingDecision: ProceedingDecision
) {
  return proceedingDecision.missingRequiredFields.map(
    (field) => proceedingDecisionFieldLabels[field]
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
    })
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
      (field) => normFieldLabels[field]
    )
  }
}

const fieldsMissing = computed(() =>
  missingCoreDataFields.value.length ||
  missingProceedingDecisionFields.value?.length ||
  missingNormsFields.value?.length
    ? true
    : false
)
</script>

<template>
  <div class="flex flex-col flex-start gap-40 justify-start max-w-[42rem]">
    <h1 class="heading-02-regular">Veröffentlichen</h1>
    <div aria-label="Plausibilitätsprüfung" class="flex flex-row gap-16">
      <div class="w-[15.625rem]">
        <p class="subheading">Plausibilitätsprüfung</p>
      </div>
      <div v-if="fieldsMissing" class="flex flex-row gap-8">
        <div>
          <span class="bg-red-800 material-icons rounded-full text-white">
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
                class="body-01-reg list-item ml-[1rem]"
              >
                {{ field }}
              </li>
              <li
                v-if="
                  missingProceedingDecisionFields &&
                  missingProceedingDecisionFields.length > 0
                "
                class="body-01-reg list-item ml-[1rem]"
              >
                Rechtszug
                <ul>
                  <li
                    v-for="fields in missingProceedingDecisionFields"
                    :key="missingProceedingDecisionFields.indexOf(fields)"
                    class="body-01-reg list-item ml-[1rem]"
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
                class="body-01-reg list-item ml-[1rem]"
              >
                Normen
                <ul>
                  <li
                    v-for="fields in missingNormsFields"
                    :key="missingNormsFields.indexOf(fields)"
                    class="body-01-reg list-item ml-[1rem]"
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
        <div class="label-02-regular">
          Letzte Veröffentlichung am
          {{ props.lastPublishedXmlMail?.publishDate }}
          (Zustellung:
          {{ props.lastPublishedXmlMail?.publishStateDisplayText }})
        </div>
        <div class="label-section text-gray-900">ÜBER</div>
        <div class="label-02-regular">
          <div>
            <span class="label-02-bold">E-Mail an:</span>
            {{ props.lastPublishedXmlMail?.receiverAddress }}
          </div>
          <div>
            <span class="label-02-bold"> Betreff: </span>
            {{ props.lastPublishedXmlMail?.mailSubject }}
          </div>
        </div>
        <div class="label-section text-gray-900">ALS</div>
        <CodeSnippet
          v-if="!!props.lastPublishedXmlMail?.xml"
          title="XML"
          :xml="props.lastPublishedXmlMail.xml"
        />
      </div>
    </div>
  </div>
</template>
