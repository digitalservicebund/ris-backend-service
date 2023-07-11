import dayjs from "dayjs"
import { Metadata, MetadataSections } from "@/domain/Norm"

function documentStatusSummary(data: Metadata): string {
  const PROOF_INDICATION_TRANSLATIONS = {
    NOT_YET_CONSIDERED: "noch nicht berücksichtigt",
    CONSIDERED: "ist berücksichtigt",
  }

  if (!data) return ""

  const workNote = data?.WORK_NOTE ?? []
  const description = data?.DESCRIPTION?.[0]
  const date = data.DATE?.[0]
    ? dayjs(data.DATE[0]).format("DD.MM.YYYY")
    : undefined
  const year = data?.YEAR?.[0]
  const reference = data?.REFERENCE?.[0]
  const entryIntoForceDateState = data?.ENTRY_INTO_FORCE_DATE_NOTE ?? []
  const proofIndication =
    data?.PROOF_INDICATION?.filter((category) => category != null) ?? []

  const translatedProofIndication = proofIndication.map(
    (indication) => PROOF_INDICATION_TRANSLATIONS[indication] || indication
  )
  const resultArray = []

  if (workNote) resultArray.push(...workNote)
  if (description) resultArray.push(description)
  if (date) resultArray.push(date)
  if (year) resultArray.push(year)
  if (reference) resultArray.push(reference)
  if (entryIntoForceDateState) resultArray.push(...entryIntoForceDateState)

  resultArray.push(...translatedProofIndication)

  return resultArray.join(" ")
}

function documentTextProofSummary(data: Metadata): string {
  const PROOF_TYPE_TRANSLATIONS = {
    TEXT_PROOF_FROM: "Textnachweis ab",
    TEXT_PROOF_VALIDITY_FROM: "Textnachweis Geltung ab",
  }

  if (!data) return ""

  const proofType =
    data?.PROOF_TYPE?.filter((category) => category != null) ?? []
  const text = data?.TEXT?.[0]

  const translatedProofType = proofType.map(
    (type) => PROOF_TYPE_TRANSLATIONS[type] || type
  )
  const resultArray = [...translatedProofType]

  if (text) {
    resultArray.push(text)
  }

  return resultArray.join(" ")
}

function documentOtherSummary(data: Metadata): string {
  const OTHER_TYPE_TRANSLATIONS = {
    TEXT_IN_PROGRESS: "Text in Bearbeitung",
    TEXT_PROOFED_BUT_NOT_DONE:
      "Nachgewiesener Text dokumentarisch noch nicht abschließend bearbeitet",
  }

  if (!data) return ""

  const otherType =
    data?.OTHER_TYPE?.filter((category) => category != null) ?? []

  const translatedOtherType = otherType.map(
    (type) => OTHER_TYPE_TRANSLATIONS[type] || type
  )

  return translatedOtherType.join(" ")
}

export function documentStatusSectionSummarizer(
  data: MetadataSections
): string {
  if (!data) return ""

  if (data.DOCUMENT_STATUS) {
    return documentStatusSummary(data.DOCUMENT_STATUS[0])
  } else if (data.DOCUMENT_TEXT_PROOF) {
    return documentTextProofSummary(data.DOCUMENT_TEXT_PROOF[0])
  } else if (data.DOCUMENT_OTHER) {
    return documentOtherSummary(data.DOCUMENT_OTHER[0])
  } else return ""
}
