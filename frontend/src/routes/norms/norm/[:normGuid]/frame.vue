<script lang="ts" setup>
import { storeToRefs } from "pinia"
import { computed, toRefs } from "vue"
import { useRoute } from "vue-router"
import ExpandableContent from "@/components/ExpandableContent.vue"
import InputGroup from "@/components/InputGroup.vue"
import SaveButton from "@/components/SaveButton.vue"
import { useScrollToHash } from "@/composables/useScrollToHash"
import { FrameData } from "@/domain/Norm"
import { ageIndication } from "@/fields/norms/ageIndication"
import { ageOfMajorityIndication } from "@/fields/norms/ageOfMajorityIndication"
import { announcementDate } from "@/fields/norms/announcementDate"
import { categorizedReference } from "@/fields/norms/categorizedReference"
import { celexNumber } from "@/fields/norms/celexNumber"
import { citationDate } from "@/fields/norms/citationDate"
import { completeCitation } from "@/fields/norms/completeCitation"
import { definition } from "@/fields/norms/definition"
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
import { lead } from "@/fields/norms/lead"
import { normProvider } from "@/fields/norms/normProvider"
import { otherDocumentNote } from "@/fields/norms/otherDocumentNote"
import { otherFootnote } from "@/fields/norms/otherFootnote"
import { otherOfficialReferences } from "@/fields/norms/otherOfficialReferences"
import { otherStatusNote } from "@/fields/norms/otherStatusNote"
import { participatingInstitutions } from "@/fields/norms/participatingInstitutions"
import { printAnnouncement } from "@/fields/norms/printAnnouncement"
import { referenceNumber } from "@/fields/norms/referenceNumber"
import { reissue } from "@/fields/norms/reissue"
import { repeal } from "@/fields/norms/repeal"
import { status } from "@/fields/norms/status"
import { subjectArea } from "@/fields/norms/subjectArea"
import { text } from "@/fields/norms/text"
import { unofficialReference } from "@/fields/norms/unofficialReference"
import { validityRule } from "@/fields/norms/validityRule"
import { useLoadedNormStore } from "@/stores/loadedNorm"
import {
  applyToFrameData,
  getFrameDataFromNorm,
  NullableBoolean,
  NullableString,
} from "@/utilities/normUtilities"

const route = useRoute()
const { hash: routeHash } = toRefs(route)
useScrollToHash(routeHash)

const store = useLoadedNormStore()
const { loadedNorm } = storeToRefs(store)
const convertToEmptyString = (value: NullableString) => {
  return value === undefined ? "" : value
}
const convertToFalse = (value: NullableBoolean) => {
  return value === undefined ? false : value
}

const frameData = computed({
  get: () => {
    return applyToFrameData(
      loadedNorm.value ? getFrameDataFromNorm(loadedNorm.value) : undefined,
      convertToEmptyString,
      convertToFalse,
      convertToEmptyString
    ) as FrameData
  },
  set: (data: FrameData) => {
    if (loadedNorm.value !== undefined) {
      loadedNorm.value.documentTemplateName = data.documentTemplateName
      loadedNorm.value.leadUnit = data.leadUnit
      loadedNorm.value.participationInstitution = data.participationInstitution
      loadedNorm.value.subjectBgb3 = data.subjectBgb3
      loadedNorm.value.ageIndicationEnd = data.ageIndicationEnd
      loadedNorm.value.ageIndicationStart = data.ageIndicationStart
      loadedNorm.value.ageOfMajorityIndication = data.ageOfMajorityIndication
      loadedNorm.value.announcementDate = data.announcementDate
      loadedNorm.value.applicationScopeArea = data.applicationScopeArea
      loadedNorm.value.applicationScopeEndDate = data.applicationScopeEndDate
      loadedNorm.value.applicationScopeStartDate =
        data.applicationScopeStartDate
      loadedNorm.value.categorizedReference = data.categorizedReference
      loadedNorm.value.celexNumber = data.celexNumber
      loadedNorm.value.citationDate = data.citationDate
      loadedNorm.value.completeCitation = data.completeCitation
      loadedNorm.value.definition = data.definition
      loadedNorm.value.digitalAnnouncementDate = data.digitalAnnouncementDate
      loadedNorm.value.digitalAnnouncementArea = data.digitalAnnouncementArea
      loadedNorm.value.digitalAnnouncementAreaNumber =
        data.digitalAnnouncementAreaNumber
      loadedNorm.value.digitalAnnouncementEdition =
        data.digitalAnnouncementEdition
      loadedNorm.value.digitalAnnouncementExplanations =
        data.digitalAnnouncementExplanations
      loadedNorm.value.digitalAnnouncementInfo = data.digitalAnnouncementInfo
      loadedNorm.value.digitalAnnouncementMedium =
        data.digitalAnnouncementMedium
      loadedNorm.value.digitalAnnouncementPage = data.digitalAnnouncementPage
      loadedNorm.value.digitalAnnouncementYear = data.digitalAnnouncementYear
      loadedNorm.value.digitalEvidenceAppendix = data.digitalEvidenceAppendix
      loadedNorm.value.digitalEvidenceExternalDataNote =
        data.digitalEvidenceExternalDataNote
      loadedNorm.value.digitalEvidenceLink = data.digitalEvidenceLink
      loadedNorm.value.digitalEvidenceRelatedData =
        data.digitalEvidenceRelatedData
      loadedNorm.value.divergentDocumentNumber = data.divergentDocumentNumber
      loadedNorm.value.divergentEntryIntoForceDate =
        data.divergentEntryIntoForceDate
      loadedNorm.value.divergentEntryIntoForceDateState =
        data.divergentEntryIntoForceDateState
      loadedNorm.value.divergentExpirationDate = data.divergentExpirationDate
      loadedNorm.value.divergentExpirationDateState =
        data.divergentExpirationDateState
      loadedNorm.value.documentCategory = data.documentCategory
      loadedNorm.value.documentNormCategory = data.documentNormCategory
      loadedNorm.value.documentNumber = data.documentNumber
      loadedNorm.value.documentStatusDate = data.documentStatusDate
      loadedNorm.value.documentStatusDescription =
        data.documentStatusDescription
      loadedNorm.value.documentStatusEntryIntoForceDate =
        data.documentStatusEntryIntoForceDate
      loadedNorm.value.documentStatusProof = data.documentStatusProof
      loadedNorm.value.documentStatusReference = data.documentStatusReference
      loadedNorm.value.documentStatusWorkNote = data.documentStatusWorkNote
      loadedNorm.value.documentTextProof = data.documentTextProof
      loadedNorm.value.documentTypeName = data.documentTypeName
      loadedNorm.value.entryIntoForceDate = data.entryIntoForceDate
      loadedNorm.value.entryIntoForceDateState = data.entryIntoForceDateState
      loadedNorm.value.entryIntoForceNormCategory =
        data.entryIntoForceNormCategory
      loadedNorm.value.euAnnouncementExplanations =
        data.euAnnouncementExplanations
      loadedNorm.value.euAnnouncementGazette = data.euAnnouncementGazette
      loadedNorm.value.euAnnouncementInfo = data.euAnnouncementInfo
      loadedNorm.value.euAnnouncementNumber = data.euAnnouncementNumber
      loadedNorm.value.euAnnouncementPage = data.euAnnouncementPage
      loadedNorm.value.euAnnouncementSeries = data.euAnnouncementSeries
      loadedNorm.value.euAnnouncementYear = data.euAnnouncementYear
      loadedNorm.value.eli = data.eli
      loadedNorm.value.expirationDate = data.expirationDate
      loadedNorm.value.expirationDateState = data.expirationDateState
      loadedNorm.value.expirationNormCategory = data.expirationNormCategory
      loadedNorm.value.frameKeywords = data.frameKeywords
      loadedNorm.value.isExpirationDateTemp = data.isExpirationDateTemp
      loadedNorm.value.leadJurisdiction = data.leadJurisdiction
      loadedNorm.value.officialAbbreviation = data.officialAbbreviation
      loadedNorm.value.officialLongTitle = data.officialLongTitle
      loadedNorm.value.officialShortTitle = data.officialShortTitle
      loadedNorm.value.otherDocumentNote = data.otherDocumentNote
      loadedNorm.value.otherFootnote = data.otherFootnote
      loadedNorm.value.footnoteChange = data.footnoteChange
      loadedNorm.value.footnoteComment = data.footnoteComment
      loadedNorm.value.footnoteDecision = data.footnoteDecision
      loadedNorm.value.footnoteStateLaw = data.footnoteStateLaw
      loadedNorm.value.footnoteEuLaw = data.footnoteEuLaw
      loadedNorm.value.otherOfficialAnnouncement =
        data.otherOfficialAnnouncement
      loadedNorm.value.otherStatusNote = data.otherStatusNote
      loadedNorm.value.participationType = data.participationType
      loadedNorm.value.principleEntryIntoForceDate =
        data.principleEntryIntoForceDate
      loadedNorm.value.principleEntryIntoForceDateState =
        data.principleEntryIntoForceDateState
      loadedNorm.value.principleExpirationDate = data.principleExpirationDate
      loadedNorm.value.principleExpirationDateState =
        data.principleExpirationDateState
      loadedNorm.value.printAnnouncementExplanations =
        data.printAnnouncementExplanations
      loadedNorm.value.printAnnouncementGazette = data.printAnnouncementGazette
      loadedNorm.value.printAnnouncementInfo = data.printAnnouncementInfo
      loadedNorm.value.printAnnouncementNumber = data.printAnnouncementNumber
      loadedNorm.value.printAnnouncementPage = data.printAnnouncementPage
      loadedNorm.value.printAnnouncementYear = data.printAnnouncementYear
      loadedNorm.value.providerEntity = data.providerEntity
      loadedNorm.value.providerDecidingBody = data.providerDecidingBody
      loadedNorm.value.providerIsResolutionMajority =
        data.providerIsResolutionMajority
      loadedNorm.value.publicationDate = data.publicationDate
      loadedNorm.value.referenceNumber = data.referenceNumber
      loadedNorm.value.reissueArticle = data.reissueArticle
      loadedNorm.value.reissueDate = data.reissueDate
      loadedNorm.value.reissueNote = data.reissueNote
      loadedNorm.value.reissueReference = data.reissueReference
      loadedNorm.value.repealArticle = data.repealArticle
      loadedNorm.value.repealDate = data.repealDate
      loadedNorm.value.repealNote = data.repealNote
      loadedNorm.value.repealReferences = data.repealReferences
      loadedNorm.value.risAbbreviation = data.risAbbreviation
      loadedNorm.value.risAbbreviationInternationalLaw =
        data.risAbbreviationInternationalLaw
      loadedNorm.value.statusDate = data.statusDate
      loadedNorm.value.statusDescription = data.statusDescription
      loadedNorm.value.statusNote = data.statusNote
      loadedNorm.value.statusReference = data.statusReference
      loadedNorm.value.subjectFna = data.subjectFna
      loadedNorm.value.subjectGesta = data.subjectGesta
      loadedNorm.value.subjectPreviousFna = data.subjectPreviousFna
      loadedNorm.value.text = data.text
      loadedNorm.value.unofficialAbbreviation = data.unofficialAbbreviation
      loadedNorm.value.unofficialLongTitle = data.unofficialLongTitle
      loadedNorm.value.unofficialReference = data.unofficialReference
      loadedNorm.value.unofficialShortTitle = data.unofficialShortTitle
      loadedNorm.value.validityRule = data.validityRule
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
      <InputGroup v-model="frameData" :column-count="1" :fields="generalData" />
    </fieldset>

    <fieldset>
      <legend id="documentTypeFields" class="heading-02-regular mb-[1rem]">
        Dokumenttyp
      </legend>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="documentType"
      />
    </fieldset>

    <fieldset>
      <legend id="normProviderFields" class="heading-02-regular mb-[1rem]">
        Normgeber
      </legend>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="normProvider"
      />
    </fieldset>

    <fieldset>
      <legend
        id="participatingInstitutionsFields"
        class="heading-02-regular mb-[1rem]"
      >
        Mitwirkende Organe
      </legend>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="participatingInstitutions"
      />
    </fieldset>

    <fieldset>
      <legend id="leadFields" class="heading-02-regular mb-[1rem]">
        Federführung
      </legend>
      <InputGroup v-model="frameData" :column-count="1" :fields="lead" />
    </fieldset>

    <fieldset>
      <legend id="subjectAreaFields" class="heading-02-regular mb-[1rem]">
        Sachgebiet
      </legend>
      <InputGroup v-model="frameData" :column-count="1" :fields="subjectArea" />
    </fieldset>

    <fieldset>
      <legend
        id="headingsAndAbbreviations"
        class="heading-02-regular mb-[1rem]"
      >
        Überschriften und Abkürzungen
      </legend>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="headingsAndAbbreviations"
      />
    </fieldset>

    <ExpandableContent header-id="headingsAndAbbreviationsUnofficial">
      <template #header>
        <h2
          id="headingsAndAbbreviationsUnofficial"
          class="link-01-bold mb-[1rem]"
        >
          Nichtamtliche Überschriften und Abkürzungen
        </h2>
      </template>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="headingsAndAbbreviationsUnofficial"
      />
    </ExpandableContent>

    <fieldset>
      <legend id="entryIntoForceFields" class="heading-02-regular mb-[1rem]">
        Inkrafttreten
      </legend>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="entryIntoForce"
      />
    </fieldset>

    <fieldset>
      <legend id="expirationFields" class="heading-02-regular mb-[1rem]">
        Außerkrafttreten
      </legend>
      <InputGroup v-model="frameData" :column-count="1" :fields="expiration" />
    </fieldset>

    <fieldset>
      <legend id="announcementDateFields" class="heading-02-regular mb-[1rem]">
        Verkündungsdatum
      </legend>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="announcementDate"
      />
    </fieldset>

    <fieldset>
      <legend id="citationDateFields" class="heading-02-regular mb-[1rem]">
        Zitierdatum
      </legend>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="citationDate"
      />
    </fieldset>

    <h2 id="officialAnnouncementFields" class="heading-02-regular mb-[1rem]">
      Amtliche Fundstelle
    </h2>
    <fieldset>
      <legend id="printAnnouncementFields" class="heading-03-regular mb-[1rem]">
        Papierverkündung
      </legend>
      <InputGroup
        v-model="frameData"
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
        v-model="frameData"
        :column-count="1"
        :fields="digitalAnnouncement"
      />
    </fieldset>
    <fieldset>
      <legend id="euAnnouncementFields" class="heading-03-regular mb-[1rem]">
        Amtsblatt der EU
      </legend>
      <InputGroup
        v-model="frameData"
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
        v-model="frameData"
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
        v-model="frameData"
        :column-count="1"
        :fields="unofficialReference"
      />
    </fieldset>

    <fieldset>
      <legend id="completeCitationFields" class="heading-02-regular mb-[1rem]">
        Vollzitat
      </legend>
      <InputGroup
        v-model="frameData"
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
      <InputGroup v-model="frameData" :column-count="1" :fields="status" />
    </fieldset>
    <fieldset>
      <legend id="repealFields" class="heading-03-regular mb-[1rem]">
        Aufhebung
      </legend>
      <InputGroup v-model="frameData" :column-count="1" :fields="repeal" />
    </fieldset>
    <fieldset>
      <legend id="reissueFields" class="heading-03-regular mb-[1rem]">
        Neufassung
      </legend>
      <InputGroup v-model="frameData" :column-count="1" :fields="reissue" />
    </fieldset>
    <fieldset>
      <legend id="otherStatusNoteFields" class="heading-03-regular mb-[1rem]">
        Sonstiger Hinweis
      </legend>
      <InputGroup
        v-model="frameData"
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
        v-model="frameData"
        :column-count="1"
        :fields="documentStatus"
      />
    </fieldset>
    <fieldset>
      <legend id="documentTextProofFields" class="heading-03-regular mb-[1rem]">
        Textnachweis
      </legend>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="documentTextProof"
      />
    </fieldset>
    <fieldset>
      <legend id="otherDocumentNoteFields" class="heading-03-regular mb-[1rem]">
        Sonstiger Hinweis
      </legend>
      <InputGroup
        v-model="frameData"
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
        v-model="frameData"
        :column-count="1"
        :fields="categorizedReference"
      />
    </fieldset>

    <fieldset>
      <legend id="otherFootnoteFields" class="heading-02-regular mb-[1rem]">
        Fußnote
      </legend>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="otherFootnote"
      />
    </fieldset>

    <fieldset>
      <legend id="validityRuleFields" class="heading-02-regular mb-[1rem]">
        Gültigkeitsregelung
      </legend>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="validityRule"
      />
    </fieldset>

    <fieldset>
      <legend id="digitalEvidenceFields" class="heading-02-regular mb-[1rem]">
        Elektronischer Nachweis
      </legend>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="digitalEvidence"
      />
    </fieldset>

    <fieldset>
      <legend id="referenceNumberFields" class="heading-02-regular mb-[1rem]">
        Aktenzeichen
      </legend>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="referenceNumber"
      />
    </fieldset>

    <fieldset>
      <legend id="eliFields" class="heading-02-regular mb-[1rem]">ELI</legend>
      <InputGroup v-model="frameData" :column-count="1" :fields="eli" />
    </fieldset>

    <fieldset>
      <legend id="celexNumberFields" class="heading-02-regular mb-[1rem]">
        CELEX-Nummer
      </legend>
      <InputGroup v-model="frameData" :column-count="1" :fields="celexNumber" />
    </fieldset>

    <fieldset>
      <legend id="ageIndicationFields" class="heading-02-regular mb-[1rem]">
        Altersangabe
      </legend>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="ageIndication"
      />
    </fieldset>

    <fieldset>
      <legend id="definitionFields" class="heading-02-regular mb-[1rem]">
        Definition
      </legend>
      <InputGroup v-model="frameData" :column-count="1" :fields="definition" />
    </fieldset>

    <fieldset>
      <legend
        id="ageOfMajorityIndicationFields"
        class="heading-02-regular mb-[1rem]"
      >
        Angaben zur Volljährigkeit
      </legend>
      <InputGroup
        v-model="frameData"
        :column-count="1"
        :fields="ageOfMajorityIndication"
      />
    </fieldset>

    <fieldset>
      <legend id="textFields" class="heading-02-regular mb-[1rem]">Text</legend>
      <InputGroup v-model="frameData" :column-count="1" :fields="text" />
    </fieldset>

    <SaveButton
      aria-label="Rahmendaten Speichern Button"
      class="mt-8"
      :service-callback="store.update"
    />
  </div>
</template>
