import { defineStore } from "pinia"
import { ref } from "vue"
import { Norm } from "@/domain/Norm"
import { ServiceResponse } from "@/services/httpClient"
import { editNormFrame, getNormByGuid } from "@/services/normsService"

function getFrameDataOfNorm(norm: Norm) {
  return {
    longTitle: norm.longTitle,
    officialShortTitle: norm.officialShortTitle,
    officialAbbreviation: norm.officialAbbreviation,
    referenceNumber: norm.referenceNumber,
    publicationDate: norm.publicationDate,
    announcementDate: norm.announcementDate,
    citationDate: norm.citationDate,
    frameKeywords: norm.frameKeywords,
    authorEntity: norm.authorEntity,
    authorDecidingBody: norm.authorDecidingBody,
    authorIsResolutionMajority: norm.authorIsResolutionMajority,
    leadJurisdiction: norm.leadJurisdiction,
    leadUnit: norm.leadUnit,
    participationType: norm.participationType,
    participationInstitution: norm.participationInstitution,
    documentTypeName: norm.documentTypeName,
    documentNormCategory: norm.documentNormCategory,
    documentTemplateName: norm.documentTemplateName,
    subjectFna: norm.subjectFna,
    subjectPreviousFna: norm.subjectPreviousFna,
    subjectGesta: norm.subjectGesta,
    subjectBgb3: norm.subjectBgb3,
  }
}

export const useLoadedNormStore = defineStore("loaded-norm", () => {
  const loadedNorm = ref<Norm | undefined>(undefined)

  async function load(guid: string): Promise<void> {
    loadedNorm.value = undefined
    const response = await getNormByGuid(guid)
    loadedNorm.value = response.data
  }

  async function update(): Promise<ServiceResponse<void>> {
    if (loadedNorm.value) {
      const frameData = getFrameDataOfNorm(loadedNorm.value)
      return editNormFrame(loadedNorm.value.guid, frameData)
    } else {
      // TODO: Get rid of this madness after service refactoring.
      return { status: 200, data: undefined }
    }
  }

  return { loadedNorm, load, update }
})
