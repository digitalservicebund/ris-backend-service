<script lang="ts" setup>
import { computed, toRefs } from "vue"
import { useRoute } from "vue-router"
import ExpandableContent from "@/components/ExpandableContent.vue"
import InputGroup from "@/components/InputGroup.vue"
import SaveButton from "@/components/SaveButton.vue"
import { useScrollToHash } from "@/composables/useScrollToHash"
import { Norm } from "@/domain/Norm"
import { normCoredataFields } from "@/domain/normCoredataFields"
import { normDocumentTypeFields } from "@/domain/normDocumentTypeFields"
import { normHeadlineFields } from "@/domain/normHeadlineFields"
import { normLegalBodyFields } from "@/domain/normLegalBodyFields"
import { normOrganisationalUnitFields } from "@/domain/normOrganisationalUnitFields"
import { normOrgansFields } from "@/domain/normOrgansFields"
import { normSubjectFields } from "@/domain/normSubjectFields"
import { unofficialNormHeadlineFields } from "@/domain/unofficialNormHeadlineFields"
import { editNormFrame } from "@/services/normsService"

interface Props {
  norm: Norm
}

interface Emits {
  (event: "fetchNorm"): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const frameData = computed({
  get: () => ({
    announcementDate: props.norm.announcementDate ?? "",
    authorDecidingBody: props.norm.authorDecidingBody ?? "",
    authorEntity: props.norm.authorEntity ?? "",
    authorIsResolutionMajority: props.norm.authorIsResolutionMajority ?? false,
    citationDate: props.norm.citationDate ?? "",
    documentNormCategory: props.norm.documentNormCategory ?? "",
    documentTemplateName: props.norm.documentTemplateName ?? "",
    documentTypeName: props.norm.documentTypeName ?? "",
    frameKeywords: props.norm.frameKeywords ?? "",
    leadJurisdiction: props.norm.leadJurisdiction ?? "",
    leadUnit: props.norm.leadUnit ?? "",
    longTitle: props.norm.longTitle ?? "",
    risAbbreviation: props.norm.risAbbreviation ?? "",
    officialAbbreviation: props.norm.officialAbbreviation ?? "",
    officialShortTitle: props.norm.officialShortTitle ?? "",
    participationInstitution: props.norm.participationInstitution ?? "",
    participationType: props.norm.participationType ?? "",
    publicationDate: props.norm.publicationDate ?? "",
    referenceNumber: props.norm.referenceNumber ?? "",
    subjectBgb3: props.norm.subjectBgb3 ?? "",
    subjectFna: props.norm.subjectFna ?? "",
    subjectGesta: props.norm.subjectGesta ?? "",
    subjectPreviousFna: props.norm.subjectPreviousFna ?? "",
  }),
  set: (data) => console.log(data),
})

const route = useRoute()
const { hash: routeHash } = toRefs(route)
useScrollToHash(routeHash)
</script>

<template>
  <div class="max-w-screen-lg">
    <h1 id="coreData" class="heading-02-regular mb-[1rem]">
      Allgemeine Angaben
    </h1>
    <InputGroup
      v-model="frameData"
      :column-count="2"
      :fields="normCoredataFields"
    />
    <h1 id="documentType" class="heading-02-regular mb-[1rem]">Dokumenttyp</h1>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="normDocumentTypeFields"
    />
    <h1 id="headings_abbreviations" class="heading-02-regular mb-[1rem]">
      Überschriften und Abkürzungen
    </h1>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="normHeadlineFields"
    />
    <ExpandableContent>
      <template #header>
        <h1 class="link-01-bold mb-[1rem]">
          Nichtamtliche Überschriften und Abkürzungen
        </h1>
      </template>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="unofficialNormHeadlineFields"
      />
    </ExpandableContent>
    <h1 id="normOriginator" class="heading-02-regular mb-[1rem]">Normgeber</h1>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="normLegalBodyFields"
    />
    <h1 id="leadManagement" class="heading-02-regular mb-[1rem]">
      Federführung
    </h1>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="normOrganisationalUnitFields"
    />
    <h1 id="subjectArea" class="heading-02-regular mb-[1rem]">Sachgebiet</h1>
    <InputGroup
      v-model="frameData"
      :column-count="2"
      :fields="normSubjectFields"
    />
    <h1 id="participatingInstitutions" class="heading-02-regular mb-[1rem]">
      Mitwirkende Organe
    </h1>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="normOrgansFields"
    />
    <SaveButton
      aria-label="Rahmendaten Speichern Button"
      class="mt-8"
      :service-callback="() => editNormFrame(props.norm.guid, frameData)"
      @fetch-norm="emit('fetchNorm')"
    />
  </div>
</template>
