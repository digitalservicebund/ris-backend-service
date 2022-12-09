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
import { applicationScope } from "@/fields/norms/applicationScope"
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
import { entryIntoForce } from "@/fields/norms/entryIntoForce"
import { euAnnouncement } from "@/fields/norms/euAnnouncement"
import { europeanLegalIdentifier } from "@/fields/norms/europeanLegalIdentifier"
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
      loadedNorm.value = {
        guid: loadedNorm.value.guid,
        articles: loadedNorm.value.articles,
        ...data,
      }
    }
  },
})
</script>

<template>
  <div class="max-w-screen-lg">
    <h1 class="h-[1px] overflow-hidden w-[1px]">
      Dokumentation des Rahmenelements
    </h1>
    <h2 id="generalDataFields" class="heading-02-regular mb-[1rem]">
      Allgemeine Angaben
    </h2>
    <InputGroup v-model="frameData" :column-count="1" :fields="generalData" />

    <h2 id="documentTypeFields" class="heading-02-regular mb-[1rem]">
      Dokumenttyp
    </h2>
    <InputGroup v-model="frameData" :column-count="1" :fields="documentType" />

    <h2 id="normProviderFields" class="heading-02-regular mb-[1rem]">
      Normgeber
    </h2>
    <InputGroup v-model="frameData" :column-count="1" :fields="normProvider" />

    <h2
      id="participatingInstitutionsFields"
      class="heading-02-regular mb-[1rem]"
    >
      Mitwirkende Organe
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="participatingInstitutions"
    />

    <h2 id="leadFields" class="heading-02-regular mb-[1rem]">Federführung</h2>
    <InputGroup v-model="frameData" :column-count="1" :fields="lead" />

    <h2 id="subjectAreaFields" class="heading-02-regular mb-[1rem]">
      Sachgebiet
    </h2>
    <InputGroup v-model="frameData" :column-count="1" :fields="subjectArea" />

    <h2 id="headingsAndAbbreviations" class="heading-02-regular mb-[1rem]">
      Überschriften und Abkürzungen
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="headingsAndAbbreviations"
    />

    <ExpandableContent>
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

    <h2 id="entryIntoForceFields" class="heading-02-regular mb-[1rem]">
      Inkrafttreten
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="entryIntoForce"
    />

    <h2 id="expirationFields" class="heading-02-regular mb-[1rem]">
      Außerkrafttreten
    </h2>
    <InputGroup v-model="frameData" :column-count="1" :fields="expiration" />

    <h2 id="announcementDateFields" class="heading-02-regular mb-[1rem]">
      Verkündungsdatum
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="announcementDate"
    />

    <h2 id="citationDateFields" class="heading-02-regular mb-[1rem]">
      Zitierdatum
    </h2>
    <InputGroup v-model="frameData" :column-count="1" :fields="citationDate" />

    <h2 id="officialAnnouncementFields" class="heading-02-regular mb-[1rem]">
      Amtliche Fundstelle
    </h2>
    <h3 id="printAnnouncementFields" class="heading-03-regular mb-[1rem]">
      Papierverkündung
    </h3>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="printAnnouncement"
    />
    <h3 id="digitalAnnouncementFields" class="heading-03-regular mb-[1rem]">
      Elektronisches Verkündungsblatt
    </h3>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="digitalAnnouncement"
    />
    <h3 id="euAnnouncementFields" class="heading-03-regular mb-[1rem]">
      Amtsblatt der EU
    </h3>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="euAnnouncement"
    />
    <h3 id="otherOfficialReferencesFields" class="heading-03-regular mb-[1rem]">
      Sonstige amtliche Fundstelle
    </h3>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="otherOfficialReferences"
    />

    <h2 id="unofficialReferenceFields" class="heading-02-regular mb-[1rem]">
      Nichtamtliche Fundstelle
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="unofficialReference"
    />

    <h2 id="completeCitationFields" class="heading-02-regular mb-[1rem]">
      Vollzitat
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="completeCitation"
    />

    <h2 id="statusIndicationFields" class="heading-02-regular mb-[1rem]">
      Stand-Angabe
    </h2>
    <h3 id="statusFields" class="heading-03-regular mb-[1rem]">Stand</h3>
    <InputGroup v-model="frameData" :column-count="1" :fields="status" />
    <h3 id="repealFields" class="heading-03-regular mb-[1rem]">Aufhebung</h3>
    <InputGroup v-model="frameData" :column-count="1" :fields="repeal" />
    <h3 id="reissueFields" class="heading-03-regular mb-[1rem]">Neufassung</h3>
    <InputGroup v-model="frameData" :column-count="1" :fields="reissue" />
    <h3 id="otherStatusNoteFields" class="heading-03-regular mb-[1rem]">
      Sonstiger Hinweis
    </h3>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="otherStatusNote"
    />

    <h2
      id="documentProcessingStatusFields"
      class="heading-02-regular mb-[1rem]"
    >
      Stand der dokumentarischen Bearbeitung
    </h2>
    <h3 id="documentStatusFields" class="heading-03-regular mb-[1rem]">
      Stand der dokumentarischen Bearbeitung
    </h3>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="documentStatus"
    />
    <h3 id="documentTextProofFields" class="heading-03-regular mb-[1rem]">
      Textnachweis
    </h3>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="documentTextProof"
    />
    <h3 id="otherDocumentNoteFields" class="heading-03-regular mb-[1rem]">
      Sonstiger Hinweis
    </h3>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="otherDocumentNote"
    />

    <h2 id="applicationScopeFields" class="heading-02-regular mb-[1rem]">
      Räumlicher Geltungsbereich
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="applicationScope"
    />

    <h2 id="categorizedReferenceFields" class="heading-02-regular mb-[1rem]">
      Aktivverweisung
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="categorizedReference"
    />

    <h2 id="otherFootnoteFields" class="heading-02-regular mb-[1rem]">
      Fußnote
    </h2>
    <InputGroup v-model="frameData" :column-count="1" :fields="otherFootnote" />

    <h2 id="validityRuleFields" class="heading-02-regular mb-[1rem]">
      Gültigkeitsregelung
    </h2>
    <InputGroup v-model="frameData" :column-count="1" :fields="validityRule" />

    <h2 id="digitalEvidenceFields" class="heading-02-regular mb-[1rem]">
      Elektronischer Nachweis
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="digitalEvidence"
    />

    <h2 id="referenceNumberFields" class="heading-02-regular mb-[1rem]">
      Aktenzeichen
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="referenceNumber"
    />

    <h2 id="europeanLegalIdentifierFields" class="heading-02-regular mb-[1rem]">
      ELI
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="europeanLegalIdentifier"
    />

    <h2 id="celexNumberFields" class="heading-02-regular mb-[1rem]">
      CELEX-Nummer
    </h2>
    <InputGroup v-model="frameData" :column-count="1" :fields="celexNumber" />

    <h2 id="ageIndicationFields" class="heading-02-regular mb-[1rem]">
      Altersangabe
    </h2>
    <InputGroup v-model="frameData" :column-count="1" :fields="ageIndication" />

    <h2 id="definitionFields" class="heading-02-regular mb-[1rem]">
      Definition
    </h2>
    <InputGroup v-model="frameData" :column-count="1" :fields="definition" />

    <h2 id="ageOfMajorityIndicationFields" class="heading-02-regular mb-[1rem]">
      Angaben zur Volljährigkeit
    </h2>
    <InputGroup
      v-model="frameData"
      :column-count="1"
      :fields="ageOfMajorityIndication"
    />

    <h2 id="textFields" class="heading-02-regular mb-[1rem]">Text</h2>
    <InputGroup v-model="frameData" :column-count="1" :fields="text" />

    <SaveButton
      aria-label="Rahmendaten Speichern Button"
      class="mt-8"
      :service-callback="store.update"
    />
  </div>
</template>
