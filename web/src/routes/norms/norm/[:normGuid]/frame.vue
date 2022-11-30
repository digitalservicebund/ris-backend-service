<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, toRefs } from "vue"
import { useRoute } from "vue-router"
import ExpandableContent from "@/components/ExpandableContent.vue"
import InputGroup from "@/components/InputGroup.vue"
import SaveButton from "@/components/SaveButton.vue"
import { useScrollToHash } from "@/composables/useScrollToHash"
import { normCoredataFields } from "@/domain/normCoredataFields"
import { normDocumentTypeFields } from "@/domain/normDocumentTypeFields"
import { normHeadlineFields } from "@/domain/normHeadlineFields"
import { normLegalBodyFields } from "@/domain/normLegalBodyFields"
import { normOrganisationalUnitFields } from "@/domain/normOrganisationalUnitFields"
import { normOrgansFields } from "@/domain/normOrgansFields"
import { normSubjectFields } from "@/domain/normSubjectFields"
import { unofficialNormHeadlineFields } from "@/domain/unofficialNormHeadlineFields"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const route = useRoute()
const { hash: routeHash } = toRefs(route)
useScrollToHash(routeHash)

const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)
const frameData = computed({
  get: () => ({
    announcementDate: loadedNorm.value?.announcementDate ?? "",
    authorDecidingBody: loadedNorm.value?.authorDecidingBody ?? "",
    authorEntity: loadedNorm.value?.authorEntity ?? "",
    authorIsResolutionMajority:
      loadedNorm.value?.authorIsResolutionMajority ?? false,
    citationDate: loadedNorm.value?.citationDate ?? "",
    documentNormCategory: loadedNorm.value?.documentNormCategory ?? "",
    documentTemplateName: loadedNorm.value?.documentTemplateName ?? "",
    documentTypeName: loadedNorm.value?.documentTypeName ?? "",
    frameKeywords: loadedNorm.value?.frameKeywords ?? "",
    leadJurisdiction: loadedNorm.value?.leadJurisdiction ?? "",
    leadUnit: loadedNorm.value?.leadUnit ?? "",
    longTitle: loadedNorm.value?.longTitle ?? "",
    officialAbbreviation: loadedNorm.value?.officialAbbreviation ?? "",
    officialShortTitle: loadedNorm.value?.officialShortTitle ?? "",
    participationInstitution: loadedNorm.value?.participationInstitution ?? "",
    participationType: loadedNorm.value?.participationType ?? "",
    publicationDate: loadedNorm.value?.publicationDate ?? "",
    referenceNumber: loadedNorm.value?.referenceNumber ?? "",
    subjectBgb3: loadedNorm.value?.subjectBgb3 ?? "",
    subjectFna: loadedNorm.value?.subjectFna ?? "",
    subjectGesta: loadedNorm.value?.subjectGesta ?? "",
    subjectPreviousFna: loadedNorm.value?.subjectPreviousFna ?? "",
    unofficialTitle: loadedNorm.value?.unofficialTitle ?? "",
    unofficialShortTitle: loadedNorm.value?.unofficialShortTitle ?? "",
    unofficialAbbreviation: loadedNorm.value?.unofficialAbbreviation ?? "",
    risAbbreviation: loadedNorm.value?.risAbbreviation ?? "",
  }),
  set: (data) => {
    if (loadedNorm.value !== undefined) {
      loadedNorm.value.announcementDate = data.announcementDate
      loadedNorm.value.authorDecidingBody = data.authorDecidingBody
      loadedNorm.value.authorEntity = data.authorEntity
      loadedNorm.value.authorIsResolutionMajority =
        data.authorIsResolutionMajority
      loadedNorm.value.citationDate = data.citationDate
      loadedNorm.value.documentNormCategory = data.documentNormCategory
      loadedNorm.value.documentTemplateName = data.documentTemplateName
      loadedNorm.value.documentTypeName = data.documentTypeName
      loadedNorm.value.frameKeywords = data.frameKeywords
      loadedNorm.value.leadJurisdiction = data.leadJurisdiction
      loadedNorm.value.leadUnit = data.leadUnit
      loadedNorm.value.longTitle = data.longTitle
      loadedNorm.value.officialAbbreviation = data.officialAbbreviation
      loadedNorm.value.officialShortTitle = data.officialShortTitle
      loadedNorm.value.participationInstitution = data.participationInstitution
      loadedNorm.value.participationType = data.participationType
      loadedNorm.value.publicationDate = data.publicationDate
      loadedNorm.value.referenceNumber = data.referenceNumber
      loadedNorm.value.subjectBgb3 = data.subjectBgb3
      loadedNorm.value.subjectFna = data.subjectFna
      loadedNorm.value.subjectGesta = data.subjectGesta
      loadedNorm.value.subjectPreviousFna = data.subjectPreviousFna
      loadedNorm.value.unofficialTitle = data.unofficialTitle
      loadedNorm.value.unofficialShortTitle = data.unofficialShortTitle
      loadedNorm.value.unofficialAbbreviation = data.unofficialAbbreviation
      loadedNorm.value.risAbbreviation = data.risAbbreviation
    }
  },
})
</script>

<template>
  <div class="max-w-screen-lg">
    <h1 class="h-[1px] overflow-hidden w-[1px]">
      Dokumentation des Rahmenelements
    </h1>
    <h2 id="coreData" class="heading-02-regular mb-[1rem]">
      Allgemeine Angaben
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="2"
      :fields="normCoredataFields"
    />
    <h2 id="documentType" class="heading-02-regular mb-[1rem]">Dokumenttyp</h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="normDocumentTypeFields"
    />
    <h2 id="headings_abbreviations" class="heading-02-regular mb-[1rem]">
      Überschriften und Abkürzungen
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="normHeadlineFields"
    />
    <ExpandableContent>
      <template #header>
        <h2 class="link-01-bold mb-[1rem]">
          Nichtamtliche Überschriften und Abkürzungen
        </h2>
      </template>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="unofficialNormHeadlineFields"
      />
    </ExpandableContent>
    <h2 id="normOriginator" class="heading-02-regular mb-[1rem]">Normgeber</h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="normLegalBodyFields"
    />
    <h2 id="leadManagement" class="heading-02-regular mb-[1rem]">
      Federführung
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="normOrganisationalUnitFields"
    />
    <h2 id="subjectArea" class="heading-02-regular mb-[1rem]">Sachgebiet</h2>
    <InputGroup
      v-model="frameData"
      :column-count="2"
      :fields="normSubjectFields"
    />
    <h2 id="participatingInstitutions" class="heading-02-regular mb-[1rem]">
      Mitwirkende Organe
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="normOrgansFields"
    />
    <SaveButton
      aria-label="Rahmendaten Speichern Button"
      class="mt-8"
      :service-callback="store.update"
    />
  </div>
</template>
