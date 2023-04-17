<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, toRefs } from "vue"
import { useRoute } from "vue-router"
import CitationDateInput from "@/components/CitationDateInput.vue"
import ExpandableContent from "@/components/ExpandableContent.vue"
import ExpandableDataSet from "@/components/ExpandableDataSet.vue"
import LeadInputGroup from "@/components/LeadInputGroup.vue"
import ParticipatingInstitutionInputGroup from "@/components/ParticipatingInstitutionInputGroup.vue"
import SubjectAreaInputGroup from "@/components/SubjectAreaInputGroup.vue"
import { useScrollToHash } from "@/composables/useScrollToHash"
import { Metadata, MetadataSections } from "@/domain/Norm"
import { ageIndication } from "@/fields/norms/ageIndication"
import { announcementDate } from "@/fields/norms/announcementDate"
import { categorizedReference } from "@/fields/norms/categorizedReference"
import { celexNumber } from "@/fields/norms/celexNumber"
import { completeCitation } from "@/fields/norms/completeCitation"
import { digitalAnnouncement } from "@/fields/norms/digitalAnnouncement"
import { digitalEvidence } from "@/fields/norms/digitalEvidence"
import { documentStatus } from "@/fields/norms/documentStatus"
import { documentTextProof } from "@/fields/norms/documentTextProof"
import { documentType } from "@/fields/norms/documentType"
import { eli } from "@/fields/norms/eli"
import { entryIntoForce } from "@/fields/norms/entryIntoForce"
import { euAnnouncement } from "@/fields/norms/euAnnouncement"
import { expiration } from "@/fields/norms/expiration"
import { generalData } from "@/fields/norms/generalData"
import { headingsAndAbbreviations } from "@/fields/norms/headingsAndAbbreviations"
import { headingsAndAbbreviationsUnofficial } from "@/fields/norms/headingsAndAbbreviationsUnofficial"
import { normProvider } from "@/fields/norms/normProvider"
import { otherDocumentNote } from "@/fields/norms/otherDocumentNote"
import { otherFootnote } from "@/fields/norms/otherFootnote"
import { otherOfficialReferences } from "@/fields/norms/otherOfficialReferences"
import { otherStatusNote } from "@/fields/norms/otherStatusNote"
import { printAnnouncement } from "@/fields/norms/printAnnouncement"
import { reissue } from "@/fields/norms/reissue"
import { repeal } from "@/fields/norms/repeal"
import { status } from "@/fields/norms/status"
import { text } from "@/fields/norms/text"
import { unofficialReference } from "@/fields/norms/unofficialReference"
import EditableList from "@/shared/components/EditableList.vue"
import ChipsInput from "@/shared/components/input/ChipsInput.vue"
import InputField from "@/shared/components/input/InputField.vue"
import InputGroup from "@/shared/components/input/InputGroup.vue"
import SaveButton from "@/shared/components/input/SaveButton.vue"
import { ModelType } from "@/shared/components/input/types"
import { useLoadedNormStore } from "@/stores/loadedNorm"

const route = useRoute()
const { hash: routeHash } = toRefs(route)
useScrollToHash(routeHash)

const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)

const metadataSections = computed({
  get: () => loadedNorm.value?.metadataSections ?? {},
  set: (sections: MetadataSections) =>
    loadedNorm.value && (loadedNorm.value.metadataSections = sections),
})

const normSection = computed({
  get: () => metadataSections.value.NORM?.[0] ?? {},
  set: (section: Metadata) => (metadataSections.value.NORM = [section]),
})

const flatMetadata = computed({
  get: () => {
    const { guid, articles, files, metadataSections, ...flatMetadata } =
      loadedNorm.value ?? {}

    return {
      ...flatMetadata,
      // Intermediate metadata that is still mixed and used in `InputGroup`s.
      frameKeywords: normSection.value.KEYWORD,
      unofficialShortTitle: normSection.value.UNOFFICIAL_SHORT_TITLE,
      unofficialReference: normSection.value.UNOFFICIAL_REFERENCE,
      unofficialLongTitle: normSection.value.UNOFFICIAL_LONG_TITLE,
      unofficialAbbreviation: normSection.value.UNOFFICIAL_ABBREVIATION,
      risAbbreviationInternationalLaw:
        normSection.value.RIS_ABBREVIATION_INTERNATIONAL_LAW,
      divergentDocumentNumber: normSection.value.DIVERGENT_DOCUMENT_NUMBER,
    } as Record<string, ModelType>
  },
  set: (data: Record<string, ModelType>) => {
    if (loadedNorm.value !== undefined) {
      loadedNorm.value.documentTemplateName =
        data.documentTemplateName as string
      loadedNorm.value.ageIndicationEnd = data.ageIndicationEnd as string
      loadedNorm.value.ageIndicationStart = data.ageIndicationStart as string
      loadedNorm.value.announcementDate = data.announcementDate as string
      loadedNorm.value.applicationScopeArea =
        data.applicationScopeArea as string
      loadedNorm.value.applicationScopeEndDate =
        data.applicationScopeEndDate as string
      loadedNorm.value.applicationScopeStartDate =
        data.applicationScopeStartDate as string
      loadedNorm.value.categorizedReference =
        data.categorizedReference as string
      loadedNorm.value.celexNumber = data.celexNumber as string
      loadedNorm.value.citationDate = data.citationDate as string
      loadedNorm.value.completeCitation = data.completeCitation as string
      loadedNorm.value.digitalAnnouncementDate =
        data.digitalAnnouncementDate as string
      loadedNorm.value.digitalAnnouncementArea =
        data.digitalAnnouncementArea as string
      loadedNorm.value.digitalAnnouncementAreaNumber =
        data.digitalAnnouncementAreaNumber as string
      loadedNorm.value.digitalAnnouncementEdition =
        data.digitalAnnouncementEdition as string
      loadedNorm.value.digitalAnnouncementExplanations =
        data.digitalAnnouncementExplanations as string
      loadedNorm.value.digitalAnnouncementInfo =
        data.digitalAnnouncementInfo as string
      loadedNorm.value.digitalAnnouncementMedium =
        data.digitalAnnouncementMedium as string
      loadedNorm.value.digitalAnnouncementPage =
        data.digitalAnnouncementPage as string
      loadedNorm.value.digitalAnnouncementYear =
        data.digitalAnnouncementYear as string
      loadedNorm.value.digitalEvidenceAppendix =
        data.digitalEvidenceAppendix as string
      loadedNorm.value.digitalEvidenceExternalDataNote =
        data.digitalEvidenceExternalDataNote as string
      loadedNorm.value.digitalEvidenceLink = data.digitalEvidenceLink as string
      loadedNorm.value.digitalEvidenceRelatedData =
        data.digitalEvidenceRelatedData as string
      loadedNorm.value.divergentEntryIntoForceDate =
        data.divergentEntryIntoForceDate as string
      loadedNorm.value.divergentEntryIntoForceDateState =
        data.divergentEntryIntoForceDateState as string
      loadedNorm.value.divergentExpirationDate =
        data.divergentExpirationDate as string
      loadedNorm.value.divergentExpirationDateState =
        data.divergentExpirationDateState as string
      loadedNorm.value.documentCategory = data.documentCategory as string
      loadedNorm.value.documentNormCategory =
        data.documentNormCategory as string
      loadedNorm.value.documentNumber = data.documentNumber as string
      loadedNorm.value.documentStatusDate = data.documentStatusDate as string
      loadedNorm.value.documentStatusDescription =
        data.documentStatusDescription as string
      loadedNorm.value.documentStatusEntryIntoForceDate =
        data.documentStatusEntryIntoForceDate as string
      loadedNorm.value.documentStatusProof = data.documentStatusProof as string
      loadedNorm.value.documentStatusReference =
        data.documentStatusReference as string
      loadedNorm.value.documentStatusWorkNote =
        data.documentStatusWorkNote as string
      loadedNorm.value.documentTextProof = data.documentTextProof as string
      loadedNorm.value.documentTypeName = data.documentTypeName as string
      loadedNorm.value.entryIntoForceDate = data.entryIntoForceDate as string
      loadedNorm.value.entryIntoForceDateState =
        data.entryIntoForceDateState as string
      loadedNorm.value.entryIntoForceNormCategory =
        data.entryIntoForceNormCategory as string
      loadedNorm.value.euAnnouncementExplanations =
        data.euAnnouncementExplanations as string
      loadedNorm.value.euAnnouncementGazette =
        data.euAnnouncementGazette as string
      loadedNorm.value.euAnnouncementInfo = data.euAnnouncementInfo as string
      loadedNorm.value.euAnnouncementNumber =
        data.euAnnouncementNumber as string
      loadedNorm.value.euAnnouncementPage = data.euAnnouncementPage as string
      loadedNorm.value.euAnnouncementSeries =
        data.euAnnouncementSeries as string
      loadedNorm.value.euAnnouncementYear = data.euAnnouncementYear as string
      loadedNorm.value.eli = data.eli as string
      loadedNorm.value.expirationDate = data.expirationDate as string
      loadedNorm.value.expirationDateState = data.expirationDateState as string
      loadedNorm.value.expirationNormCategory =
        data.expirationNormCategory as string
      loadedNorm.value.isExpirationDateTemp =
        data.isExpirationDateTemp as boolean
      loadedNorm.value.officialAbbreviation =
        data.officialAbbreviation as string
      loadedNorm.value.officialLongTitle = data.officialLongTitle as string
      loadedNorm.value.officialShortTitle = data.officialShortTitle as string
      loadedNorm.value.otherDocumentNote = data.otherDocumentNote as string
      loadedNorm.value.otherFootnote = data.otherFootnote as string
      loadedNorm.value.footnoteChange = data.footnoteChange as string
      loadedNorm.value.footnoteComment = data.footnoteComment as string
      loadedNorm.value.footnoteDecision = data.footnoteDecision as string
      loadedNorm.value.footnoteStateLaw = data.footnoteStateLaw as string
      loadedNorm.value.footnoteEuLaw = data.footnoteEuLaw as string
      loadedNorm.value.otherOfficialAnnouncement =
        data.otherOfficialAnnouncement as string
      loadedNorm.value.otherStatusNote = data.otherStatusNote as string
      loadedNorm.value.principleEntryIntoForceDate =
        data.principleEntryIntoForceDate as string
      loadedNorm.value.principleEntryIntoForceDateState =
        data.principleEntryIntoForceDateState as string
      loadedNorm.value.principleExpirationDate =
        data.principleExpirationDate as string
      loadedNorm.value.principleExpirationDateState =
        data.principleExpirationDateState as string
      loadedNorm.value.printAnnouncementExplanations =
        data.printAnnouncementExplanations as string
      loadedNorm.value.printAnnouncementGazette =
        data.printAnnouncementGazette as string
      loadedNorm.value.printAnnouncementInfo =
        data.printAnnouncementInfo as string
      loadedNorm.value.printAnnouncementNumber =
        data.printAnnouncementNumber as string
      loadedNorm.value.printAnnouncementPage =
        data.printAnnouncementPage as string
      loadedNorm.value.printAnnouncementYear =
        data.printAnnouncementYear as string
      loadedNorm.value.providerEntity = data.providerEntity as string
      loadedNorm.value.providerDecidingBody =
        data.providerDecidingBody as string
      loadedNorm.value.providerIsResolutionMajority =
        data.providerIsResolutionMajority as boolean
      loadedNorm.value.publicationDate = data.publicationDate as string
      loadedNorm.value.reissueArticle = data.reissueArticle as string
      loadedNorm.value.reissueDate = data.reissueDate as string
      loadedNorm.value.reissueNote = data.reissueNote as string
      loadedNorm.value.reissueReference = data.reissueReference as string
      loadedNorm.value.repealArticle = data.repealArticle as string
      loadedNorm.value.repealDate = data.repealDate as string
      loadedNorm.value.repealNote = data.repealNote as string
      loadedNorm.value.repealReferences = data.repealReferences as string
      loadedNorm.value.risAbbreviation = data.risAbbreviation as string
      loadedNorm.value.statusDate = data.statusDate as string
      loadedNorm.value.statusDescription = data.statusDescription as string
      loadedNorm.value.statusNote = data.statusNote as string
      loadedNorm.value.statusReference = data.statusReference as string
      loadedNorm.value.text = data.text as string

      // Intermediate metadata that is still mixed and used in `InputGroup`s.
      normSection.value.KEYWORD = data.frameKeywords as string[]
      normSection.value.UNOFFICIAL_SHORT_TITLE =
        data.unofficialShortTitle as string[]
      normSection.value.UNOFFICIAL_REFERENCE =
        data.unofficialReference as string[]
      normSection.value.UNOFFICIAL_LONG_TITLE =
        data.unofficialLongTitle as string[]
      normSection.value.UNOFFICIAL_ABBREVIATION =
        data.unofficialAbbreviation as string[]
      normSection.value.RIS_ABBREVIATION_INTERNATIONAL_LAW =
        data.risAbbreviationInternationalLaw as string[]
      normSection.value.DIVERGENT_DOCUMENT_NUMBER =
        data.divergentDocumentNumber as string[]
    }
  },
})

const citationData = computed({
  get: () => ({
    date: loadedNorm.value?.citationDate,
    year: loadedNorm.value?.citationYear,
  }),
  set(value) {
    if (loadedNorm.value) {
      loadedNorm.value.citationDate = value.date
      loadedNorm.value.citationYear = value.year
    }
  },
})
</script>

<template>
  <div class="max-w-screen-lg">
    <h1 class="h-[1px] overflow-hidden w-[1px]">
      Dokumentation des Rahmenelements
    </h1>
    <fieldset>
      <legend id="generalDataFields" class="heading-02-regular mb-[1rem]">
        Allgemeine Angaben
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="generalData"
      />
    </fieldset>

    <fieldset>
      <legend id="documentTypeFields" class="heading-02-regular mb-[1rem]">
        Dokumenttyp
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="documentType"
      />
    </fieldset>

    <fieldset>
      <legend id="normProviderFields" class="heading-02-regular mb-[1rem]">
        Normgeber
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="normProvider"
      />
    </fieldset>

    <ExpandableDataSet
      id="participatingInstitutionsFields"
      class="mt-40"
      :data-set="metadataSections.PARTICIPATION"
      title="Mitwirkende Organe"
    >
      <EditableList
        v-model="metadataSections.PARTICIPATION"
        :default-value="{}"
        :edit-component="ParticipatingInstitutionInputGroup"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="leadFields"
      class="-mt-40"
      :data-set="metadataSections.LEAD"
      title="Federführung"
    >
      <EditableList
        v-model="metadataSections.LEAD"
        :default-value="{}"
        :edit-component="LeadInputGroup"
      />
    </ExpandableDataSet>

    <ExpandableDataSet
      id="subjectAreaFields"
      class="-mt-40"
      :data-set="metadataSections.SUBJECT_AREA"
      title="Sachgebiet"
    >
      <EditableList
        v-model="metadataSections.SUBJECT_AREA"
        :default-value="{}"
        :edit-component="SubjectAreaInputGroup"
      />
    </ExpandableDataSet>

    <fieldset>
      <legend
        id="headingsAndAbbreviations"
        class="heading-02-regular mb-[1rem]"
      >
        Überschriften und Abkürzungen
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="headingsAndAbbreviations"
      />
    </fieldset>

    <ExpandableContent header-id="headingsAndAbbreviationsUnofficial">
      <template #header>
        <legend
          id="headingsAndAbbreviationsUnofficial"
          class="link-01-bold mb-[1rem]"
        >
          Nichtamtliche Überschriften und Abkürzungen
        </legend>
      </template>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="headingsAndAbbreviationsUnofficial"
      />
    </ExpandableContent>

    <fieldset>
      <legend id="entryIntoForceFields" class="heading-02-regular mb-[1rem]">
        Inkrafttreten
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="entryIntoForce"
      />
    </fieldset>

    <fieldset>
      <legend id="expirationFields" class="heading-02-regular mb-[1rem]">
        Außerkrafttreten
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="expiration"
      />
    </fieldset>

    <fieldset>
      <legend id="announcementDateFields" class="heading-02-regular mb-[1rem]">
        Verkündungsdatum
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="announcementDate"
      />
    </fieldset>

    <fieldset>
      <legend id="citationDateFields" class="heading-02-regular mb-[2rem]">
        Zitierdatum
      </legend>
      <CitationDateInput v-model="citationData" />
    </fieldset>

    <h2 id="officialAnnouncementFields" class="heading-02-regular mb-[1rem]">
      Amtliche Fundstelle
    </h2>
    <fieldset>
      <legend id="printAnnouncementFields" class="heading-03-regular mb-[1rem]">
        Papierverkündung
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="printAnnouncement"
      />
    </fieldset>

    <fieldset>
      <legend
        id="digitalAnnouncementFields"
        class="heading-03-regular mb-[1rem]"
      >
        Elektronisches Verkündungsblatt
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="digitalAnnouncement"
      />
    </fieldset>

    <fieldset>
      <legend id="euAnnouncementFields" class="heading-03-regular mb-[1rem]">
        Amtsblatt der EU
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="euAnnouncement"
      />
    </fieldset>
    <fieldset>
      <legend
        id="otherOfficialReferencesFields"
        class="heading-03-regular mb-[1rem]"
      >
        Sonstige amtliche Fundstelle
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="otherOfficialReferences"
      />
    </fieldset>

    <fieldset>
      <legend
        id="unofficialReferenceFields"
        class="heading-02-regular mb-[1rem]"
      >
        Nichtamtliche Fundstelle
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="unofficialReference"
      />
    </fieldset>

    <fieldset>
      <legend id="completeCitationFields" class="heading-02-regular mb-[1rem]">
        Vollzitat
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="completeCitation"
      />
    </fieldset>

    <h2 id="statusIndicationFields" class="heading-02-regular mb-[1rem]">
      Stand-Angabe
    </h2>
    <fieldset>
      <legend id="statusFields" class="heading-03-regular mb-[1rem]">
        Stand
      </legend>
      <InputGroup v-model="flatMetadata" :column-count="1" :fields="status" />
    </fieldset>
    <fieldset>
      <legend id="repealFields" class="heading-03-regular mb-[1rem]">
        Aufhebung
      </legend>
      <InputGroup v-model="flatMetadata" :column-count="1" :fields="repeal" />
    </fieldset>
    <fieldset>
      <legend id="reissueFields" class="heading-03-regular mb-[1rem]">
        Neufassung
      </legend>
      <InputGroup v-model="flatMetadata" :column-count="1" :fields="reissue" />
    </fieldset>
    <fieldset>
      <legend id="otherStatusNoteFields" class="heading-03-regular mb-[1rem]">
        Sonstiger Hinweis
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="otherStatusNote"
      />
    </fieldset>

    <h2
      id="documentProcessingStatusFields"
      class="heading-02-regular mb-[1rem]"
    >
      Stand der dokumentarischen Bearbeitung
    </h2>
    <fieldset>
      <legend id="documentStatusFields" class="heading-03-regular mb-[1rem]">
        Stand der dokumentarischen Bearbeitung
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="documentStatus"
      />
    </fieldset>
    <fieldset>
      <legend id="documentTextProofFields" class="heading-03-regular mb-[1rem]">
        Textnachweis
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="documentTextProof"
      />
    </fieldset>
    <fieldset>
      <legend id="otherDocumentNoteFields" class="heading-03-regular mb-[1rem]">
        Sonstiger Hinweis
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="otherDocumentNote"
      />
    </fieldset>

    <fieldset>
      <legend
        id="categorizedReferenceFields"
        class="heading-02-regular mb-[1rem]"
      >
        Aktivverweisung
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="categorizedReference"
      />
    </fieldset>

    <fieldset>
      <legend id="otherFootnoteFields" class="heading-02-regular mb-[1rem]">
        Fußnote
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="otherFootnote"
      />
    </fieldset>

    <fieldset class="mb-40">
      <legend id="validityRuleFields" class="heading-02-regular mb-[1rem]">
        Gültigkeitsregelung
      </legend>

      <InputField
        id="validityRule"
        aria-label="Gültigkeitsregelung"
        label="Gültigkeitsregelung"
      >
        <ChipsInput
          id="validityRyle"
          v-model="normSection.VALIDITY_RULE"
          aria-label="Gültigkeitsregelung"
        />
      </InputField>
    </fieldset>

    <fieldset>
      <legend id="digitalEvidenceFields" class="heading-02-regular mb-[1rem]">
        Elektronischer Nachweis
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="digitalEvidence"
      />
    </fieldset>

    <fieldset class="mb-40">
      <legend id="referenceNumberFields" class="heading-02-regular mb-[1rem]">
        Aktenzeichen
      </legend>

      <InputField
        id="referenceNumber"
        aria-label="Aktenzeichen"
        label="Aktenzeichen"
      >
        <ChipsInput
          id="referenceNumber"
          v-model="normSection.REFERENCE_NUMBER"
          aria-label="Aktenzeichen"
        />
      </InputField>
    </fieldset>

    <fieldset>
      <legend id="eliFields" class="heading-02-regular mb-[1rem]">ELI</legend>
      <InputGroup v-model="flatMetadata" :column-count="1" :fields="eli" />
    </fieldset>

    <fieldset>
      <legend id="celexNumberFields" class="heading-02-regular mb-[1rem]">
        CELEX-Nummer
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="celexNumber"
      />
    </fieldset>

    <fieldset>
      <legend id="ageIndicationFields" class="heading-02-regular mb-[1rem]">
        Altersangabe
      </legend>
      <InputGroup
        v-model="flatMetadata"
        :column-count="1"
        :fields="ageIndication"
      />
    </fieldset>

    <fieldset class="mb-40">
      <legend id="definitionFields" class="heading-02-regular mb-[1rem]">
        Definition
      </legend>

      <InputField id="definition" aria-label="Definition" label="Definition">
        <ChipsInput
          id="definition"
          v-model="normSection.DEFINITION"
          aria-label="Definition"
        />
      </InputField>
    </fieldset>

    <fieldset class="mb-40">
      <legend
        id="ageOfMajorityIndicationFields"
        class="heading-02-regular mb-[1rem]"
      >
        Angaben zur Volljährigkeit
      </legend>

      <InputField
        id="ageOfMajorityIndication"
        aria-label="Angaben zur Volljährigkeit"
        label="Angaben zur Volljährigkeit"
      >
        <ChipsInput
          id="ageOfMajorityIndication"
          v-model="normSection.AGE_OF_MAJORITY_INDICATION"
          aria-label="Angaben zur Volljährigkeit"
        />
      </InputField>
    </fieldset>

    <fieldset>
      <legend id="textFields" class="heading-02-regular mb-[1rem]">Text</legend>
      <InputGroup v-model="flatMetadata" :column-count="1" :fields="text" />
    </fieldset>

    <SaveButton
      aria-label="Rahmendaten Speichern Button"
      class="mt-8"
      :service-callback="store.update"
    />
  </div>
</template>
