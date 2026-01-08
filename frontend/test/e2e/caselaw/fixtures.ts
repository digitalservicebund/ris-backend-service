import { APIRequestContext, Cookie, Page, test } from "@playwright/test"
import { mergeDeep } from "@tiptap/vue-3"
import dayjs from "dayjs"
import utc from "dayjs/plugin/utc.js"
import jsonPatch from "fast-json-patch"
import { Page as Pagination } from "@/components/Pagination.vue"
import { Addressee } from "@/domain/abuseFee"
import { AppealAdmitter } from "@/domain/appealAdmitter"
import { Decision } from "@/domain/decision"
import { Kind } from "@/domain/documentationUnitKind"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import { TypeOfIncome } from "@/domain/incomeType"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import { ProceedingType } from "@/domain/objectValue"
import { TranslationType } from "@/domain/originOfTranslation"
import PendingProceeding from "@/domain/pendingProceeding"
import RelatedDocumentation from "@/domain/relatedDocumentation"
import { SourceValue } from "@/domain/source"
import { generateString } from "~/test-helper/dataGenerators"

dayjs.extend(utc)

// Declare the types of your fixtures.
type MyFixtures = {
  documentNumber: string
  prefilledDocumentUnit: Decision
  secondPrefilledDocumentUnit: Decision
  linkedDocumentNumber: string
  pageWithBghUser: Page
  decisionBgh: Decision
  pageWithBfhUser: Page
  pageWithExternalUser: Page
  prefilledDocumentUnitBgh: Decision
  edition: LegalPeriodicalEdition
  editionWithReferences: LegalPeriodicalEdition
  editionWithManyReferences: LegalPeriodicalEdition
  foreignDocumentationUnit: DocumentUnitListEntry
  prefilledDocumentUnitWithReferences: Decision
  prefilledDocumentUnitWithTexts: Decision
  prefilledDocumentUnitWithManyReferences: Decision
  prefilledDocumentUnitWithLegacyCountryOfOrigin: Decision
  pendingProceeding: PendingProceeding
  prefilledPendingProceeding: PendingProceeding
  /** Define fixture option "decisionsToBeCreated" to define the decisions to be generated */
  decision: {
    createdDecision: Decision
    fileNumber: string
  }
  decisions: {
    createdDecisions: Decision[]
    fileNumberPrefix: string
  }
  /** Define fixture option "pendingProceedingsToBeCreated" to define the pending proceedings to be generated */
  pendingProceedings: {
    createdPendingProceedings: PendingProceeding[]
    fileNumberPrefix: string
  }
}

type MyOptions = {
  pendingProceedingsToBeCreated: Partial<PendingProceeding>[]
  decisionsToBeCreated: Partial<Decision>[]
  decisionToBeCreated: Partial<Decision>
}

/**
 * The deletion fails if two doc units are deleted at the same time that have a common relationship (e.g. duplicate relation). As we do not lock the entity, it might try to delete a stale reference and fail. This is very unlikely to happen in production. If so, it would succeed on retry as well.
 */
async function deleteWithRetry(
  request: APIRequestContext,
  uuid: string,
  csrfToken: Cookie | undefined,
  documentNumber: string,
) {
  const headers = { "X-XSRF-TOKEN": csrfToken?.value ?? "" }

  // Published doc units cannot be deleted, so we try to unpublish first. Will also work for unpublished doc units.
  await request
    .put(`/api/v1/caselaw/documentunits/${uuid}/withdraw`, { headers })
    .catch(() =>
      console.error("Failed to withdraw doc unit: " + documentNumber),
    )

  const deleteResponse = await request.delete(
    `/api/v1/caselaw/documentunits/${uuid}`,
    { headers },
  )

  if (!deleteResponse.ok()) {
    console.log(
      `Deletion for ${documentNumber} failed with status ${deleteResponse.status()}, retrying deletion...`,
    )
    // Retry after a random delay between 0.1s and 2s
    const retryWaitDuration = Math.floor(Math.random() * 1_900) + 100
    await new Promise((resolve) => setTimeout(resolve, retryWaitDuration))
    const retryDeleteResponse = await request.delete(
      `/api/v1/caselaw/documentunits/${uuid}`,
      { headers },
    )

    if (!retryDeleteResponse.ok()) {
      throw Error(`DocumentUnit with number ${documentNumber} couldn't be deleted:
      ${deleteResponse.status()} ${deleteResponse.statusText()}`)
    }
  }
}

export const caselawTest = test.extend<MyFixtures & MyOptions>({
  context: async ({ browser }, use, testInfo) => {
    const context = await browser.newContext()
    // The current test name will be added to all logs, see MdcLoggingFilter.java
    await context.setExtraHTTPHeaders({
      "X-Test-Name": testInfo.titlePath.join(" > "),
    })
    await use(context)
    await context.close()
  },
  documentNumber: async ({ request, context }, use) => {
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
    const response = await request.put(`/api/v1/caselaw/documentunits/new`, {
      headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
    })

    const { uuid, documentNumber } = await response.json()

    await use(documentNumber)
    await deleteWithRetry(request, uuid, csrfToken, documentNumber)
  },

  prefilledDocumentUnit: async ({ request, context }, use) => {
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
    const response = await request.put(`/api/v1/caselaw/documentunits/new`, {
      headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
    })

    if (!response.ok()) {
      throw new Error(
        `Failed to create prefilledDocumentUnit: ${response.status()} ${response.statusText()}`,
      )
    }
    const prefilledDocumentUnit = await response.json()

    const courtResponse = await request.get(`api/v1/caselaw/courts?q=AG+Aachen`)
    const court = await courtResponse.json()

    const normAbbreviationResponse = await request.get(
      `api/v1/caselaw/normabbreviation/search?q=BGB&sz=30&pg=0`,
    )
    const normAbbreviation = await normAbbreviationResponse.json()

    const citationTypeResponse = await request.get(
      `api/v1/caselaw/citationtypes?q=Abgrenzung`,
    )
    const citationType = await citationTypeResponse.json()

    const fieldsOfLawResponse = await request.get(
      `api/v1/caselaw/fieldsoflaw/search-by-identifier?q=AR-01&sz=200&pg=0`,
    )
    const fieldsOfLaw = await fieldsOfLawResponse.json()
    const documentTypeResponse = await request.get(
      `api/v1/caselaw/documenttypes?q=Anerkenntnisurteil`,
    )
    const documentType = await documentTypeResponse.json()

    const updateResponse = await request.put(
      `/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}`,
      {
        data: {
          ...prefilledDocumentUnit,
          coreData: {
            ...prefilledDocumentUnit.coreData,
            court: court?.[0],
            documentType: documentType?.[0],
            fileNumbers: [generateString()],
            decisionDate: "2019-12-31",
            appraisalBody: "1. Senat, 2. Kammer",
            sources: [
              {
                value: SourceValue.AngefordertesOriginal,
              },
            ],
          },
          contentRelatedIndexing: {
            keywords: ["keyword"],
            norms: [
              {
                normAbbreviation: normAbbreviation?.[0],
              },
            ],
            activeCitations: [
              {
                documentNumber: "YYTestDoc0013",
                court: court?.[0],
                documentType: documentType?.[0],
                decisionDate: "2022-02-01",
                fileNumber: "123",
                citationType: citationType?.[0],
              },
            ],
            fieldsOfLaw: [fieldsOfLaw?.[0]],
          },
          shortTexts: {
            headnote: "testHeadnote",
            guidingPrinciple: "guidingPrinciple",
            headline: "testHeadline",
          },
        },
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    if (!updateResponse.ok()) {
      throw new Error(
        `Failed to update prefilledDocumentUnit: ${response.status()} ${response.statusText()}`,
      )
    }

    await use(await updateResponse.json())

    await deleteWithRetry(
      request,
      prefilledDocumentUnit.uuid,
      csrfToken,
      prefilledDocumentUnit.documentNumber,
    )
  },

  prefilledPendingProceeding: async ({ request, context }, use) => {
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
    const response = await request.put(
      `/api/v1/caselaw/documentunits/new?kind=PENDING_PROCEEDING`,
      {
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    if (!response.ok()) {
      throw new Error(
        `Failed to create prefilledPendingProceeding: ${response.status()} ${response.statusText()}`,
      )
    }
    const prefilledPendingProceeding = await response.json()

    const courtResponse = await request.get(`api/v1/caselaw/courts?q=AG+Aachen`)
    const court = await courtResponse.json()

    const normAbbreviationResponse = await request.get(
      `api/v1/caselaw/normabbreviation/search?q=BGB&sz=30&pg=0`,
    )
    const normAbbreviation = await normAbbreviationResponse.json()

    const fieldsOfLawResponse = await request.get(
      `api/v1/caselaw/fieldsoflaw/search-by-identifier?q=AR-01&sz=200&pg=0`,
    )
    const fieldsOfLaw = await fieldsOfLawResponse.json()
    const documentTypeResponse = await request.get(
      `api/v1/caselaw/documenttypes?q=Anerkenntnisurteil`,
    )
    const documentType = await documentTypeResponse.json()

    const newPendingProceeding = {
      ...prefilledPendingProceeding,
      coreData: {
        ...prefilledPendingProceeding.coreData,
        court: court?.[0],
        documentType: documentType?.[0],
        fileNumbers: [generateString()],
        decisionDate: "2019-12-31",
        appraisalBody: "1. Senat, 2. Kammer",
        sources: [
          {
            value: SourceValue.AngefordertesOriginal,
          },
        ],
      },
      contentRelatedIndexing: {
        keywords: ["keyword"],
        norms: [
          {
            normAbbreviation: normAbbreviation?.[0],
          },
        ],
        fieldsOfLaw: [fieldsOfLaw?.[0]],
      },
      shortTexts: {
        headline: "test headline",
        resolutionNote: "test resolutionNote",
        legalIssue: "test legalIssue",
        admissionOfAppeal: "test admissionOfAppeal",
        appellant: "test appellant",
      },
    } as PendingProceeding

    const patchedPendingProceeding = mergeDeep(
      newPendingProceeding,
      prefilledPendingProceeding,
    ) as PendingProceeding

    const frontendPatch = jsonPatch.compare(
      patchedPendingProceeding,
      newPendingProceeding,
    )

    const patchResponse = await request.patch(
      `/api/v1/caselaw/documentunits/${newPendingProceeding.uuid}`,
      {
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
        data: {
          documentationUnitVersion: newPendingProceeding.version,
          patch: frontendPatch,
          errorPaths: [],
        },
      },
    )

    if (!patchResponse.ok()) {
      throw new Error(
        `Failed to patch pending proceeding: ${response.status()} ${response.statusText()}`,
      )
    }
    const getResponse = await request.get(
      `/api/v1/caselaw/documentunits/${prefilledPendingProceeding.documentNumber}`,
    )

    if (!getResponse.ok()) {
      throw new Error(
        `Failed to get pending proceeding: ${getResponse.status()} ${getResponse.statusText()}`,
      )
    }

    await use(await getResponse.json())

    await deleteWithRetry(
      request,
      prefilledPendingProceeding.uuid,
      csrfToken,
      prefilledPendingProceeding.documentNumber,
    )
  },

  prefilledDocumentUnitWithReferences: async ({ request, context }, use) => {
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
    const response = await request.put(`/api/v1/caselaw/documentunits/new`, {
      headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
    })
    const prefilledDocumentUnit = await response.json()

    const courtResponse = await request.get(`api/v1/caselaw/courts?q=AG+Aachen`)
    const court = await courtResponse.json()

    const documentTypeResponse = await request.get(
      `api/v1/caselaw/documenttypes?q=Anerkenntnisurteil`,
    )
    const documentType = await documentTypeResponse.json()

    const literatureDocumentTypeResponse = await request.get(
      `api/v1/caselaw/documenttypes?q=Ean&category=DEPENDENT_LITERATURE`,
    )
    const literatureDocumentType = await literatureDocumentTypeResponse.json()

    const legalPeriodicalResponse = await request.get(
      `api/v1/caselaw/legalperiodicals?q=MMG`,
    )
    const legalPeriodical = await legalPeriodicalResponse.json()

    const updateResponse = await request.put(
      `/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}`,
      {
        data: {
          ...prefilledDocumentUnit,
          coreData: {
            ...prefilledDocumentUnit.coreData,
            court: court?.[0],
            documentType: documentType?.[0],
            fileNumbers: [generateString()],
            decisionDate: "2019-12-31",
            appraisalBody: "1. Senat, 2. Kammer",
          },
          shortTexts: {
            headnote: "testHeadnote",
            guidingPrinciple: "guidingPrinciple",
          },
          caselawReferences: [
            {
              citation: "2024, 1-2, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              referenceType: "caselaw",
            },
          ],
          literatureReferences: [
            {
              citation: "2024, 3-4, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              author: "Krümelmonster",
              documentType: literatureDocumentType?.[0],
              referenceType: "literature",
            },
          ],
        },
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    await use(await updateResponse.json())

    await deleteWithRetry(
      request,
      prefilledDocumentUnit.uuid,
      csrfToken,
      prefilledDocumentUnit.documentNumber,
    )
  },

  prefilledDocumentUnitWithManyReferences: async (
    { request, context },
    use,
  ) => {
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
    const response = await request.put(`/api/v1/caselaw/documentunits/new`, {
      headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
    })
    const prefilledDocumentUnit = await response.json()

    const courtResponse = await request.get(`api/v1/caselaw/courts?q=AG+Aachen`)
    const court = await courtResponse.json()

    const documentTypeResponse = await request.get(
      `api/v1/caselaw/documenttypes?q=Anerkenntnisurteil`,
    )
    const documentType = await documentTypeResponse.json()

    const literatureDocumentTypeResponse = await request.get(
      `api/v1/caselaw/documenttypes?q=Ean&category=DEPENDENT_LITERATURE`,
    )
    const literatureDocumentType = await literatureDocumentTypeResponse.json()

    const legalPeriodicalResponse = await request.get(
      `api/v1/caselaw/legalperiodicals?q=MMG`,
    )
    const legalPeriodical = await legalPeriodicalResponse.json()

    const updateResponse = await request.put(
      `/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}`,
      {
        data: {
          ...prefilledDocumentUnit,
          coreData: {
            ...prefilledDocumentUnit.coreData,
            court: court?.[0],
            documentType: documentType?.[0],
            fileNumbers: [generateString()],
            decisionDate: "2019-12-31",
            appraisalBody: "1. Senat, 2. Kammer",
          },
          shortTexts: {
            headnote: "testHeadnote",
            guidingPrinciple: "guidingPrinciple",
          },
          caselawReferences: [
            {
              citation: "2024, 1-2, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              referenceType: "caselaw",
            },
            {
              citation: "2024, 1-2, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              referenceType: "caselaw",
            },
            {
              citation: "2024, 1-2, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              referenceType: "caselaw",
            },
            {
              citation: "2024, 1-2, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              referenceType: "caselaw",
            },
            {
              citation: "2024, 1-2, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              referenceType: "caselaw",
            },
            {
              citation: "2024, 1-2, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              referenceType: "caselaw",
            },
          ],
          literatureReferences: [
            {
              citation: "2024, 3-4, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              author: "Krümelmonster",
              documentType: literatureDocumentType?.[0],
              referenceType: "literature",
            },
            {
              citation: "2024, 3-4, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              author: "Krümelmonster",
              documentType: literatureDocumentType?.[0],
              referenceType: "literature",
            },
            {
              citation: "2024, 3-4, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              author: "Krümelmonster",
              documentType: literatureDocumentType?.[0],
              referenceType: "literature",
            },
            {
              citation: "2024, 3-4, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              author: "Krümelmonster",
              documentType: literatureDocumentType?.[0],
              referenceType: "literature",
            },
            {
              citation: "2024, 3-4, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              author: "Krümelmonster",
              documentType: literatureDocumentType?.[0],
              referenceType: "literature",
            },
            {
              citation: "2024, 3-4, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              author: "Krümelmonster",
              documentType: literatureDocumentType?.[0],
              referenceType: "literature",
            },
          ],
        },
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    await use(await updateResponse.json())

    await deleteWithRetry(
      request,
      prefilledDocumentUnit.uuid,
      csrfToken,
      prefilledDocumentUnit.documentNumber,
    )
  },

  prefilledDocumentUnitWithTexts: async ({ request, context }, use) => {
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
    const response = await request.put(`/api/v1/caselaw/documentunits/new`, {
      headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
    })
    const prefilledDocumentUnitWithLongTexts = await response.json()

    const courtResponse = await request.get(`api/v1/caselaw/courts?q=AG+Aachen`)
    const court = await courtResponse.json()

    const courtBFHResponse = await request.get(`api/v1/caselaw/courts?q=BFH`)
    const courtBFH = await courtBFHResponse.json()

    const documentTypeResponse = await request.get(
      `api/v1/caselaw/documenttypes?q=Anerkenntnisurteil`,
    )
    const documentType = await documentTypeResponse.json()

    const fieldsOfLawResponse = await request.get(
      `api/v1/caselaw/fieldsoflaw/search-by-identifier?q=RE-07-DEU&sz=200&pg=0`,
    )
    const country = await fieldsOfLawResponse.json()

    const normAbbreviationResponse = await request.get(
      `api/v1/caselaw/normabbreviation/search?q=BGB&sz=30&pg=0`,
    )
    const normAbbreviation = await normAbbreviationResponse.json()

    const updateResponse = await request.put(
      `/api/v1/caselaw/documentunits/${prefilledDocumentUnitWithLongTexts.uuid}`,
      {
        data: {
          ...prefilledDocumentUnitWithLongTexts,
          coreData: {
            ...prefilledDocumentUnitWithLongTexts.coreData,
            court: court?.[0],
            documentType: documentType?.[0],
            fileNumbers: [generateString()],
            decisionDate: "2019-12-31",
            appraisalBody: "1. Senat, 2. Kammer",
          },
          shortTexts: {
            decisionNames: ["Test Entscheidungsname"],
            headnote: "Test Orientierungssatz",
            guidingPrinciple: "Test Leitsatz",
            headline: "Test Titelzeile",
            otherHeadnote: "Test Sonstiger Orientierungssatz",
          },
          longTexts: {
            tenor: "Test Tenor",
            reasons: "Test Gründe",
            caseFacts: "Test Tatbestand",
            decisionReasons: "Test Entscheidungsgründe",
            dissentingOpinion: "Test Abweichende Meinung",
            participatingJudges: [{ name: "Test Richter" }],
            otherLongText: "Test Sonstiger Langtext",
            outline: "Test Gliederung",
            corrections: [
              {
                type: "Berichtigungsbeschluss",
                description: "Hauffen -> Haufen",
              },
            ],
          },
          contentRelatedIndexing: {
            evsf: "Test E-VSF",
            definitions: [
              {
                definedTerm: "Test Definition",
                definingBorderNumber: 2,
              },
              { definedTerm: "Test Definition2" },
            ],
            foreignLanguageVersions: [
              {
                link: "Test Fremdsprachige Fassung",
                languageCode: {
                  id: "fdbccbc3-0860-5c49-914a-759c31e68c85",
                  label: "Akan",
                  isoCode: "ak",
                },
              },
              {
                link: "Test Fremdsprachige Fassung2",
                languageCode: {
                  id: "0d430058-b2a5-540b-aa2d-e3627cdc1d8b",
                  label: "Afar",
                  isoCode: "aa",
                },
              },
            ],
            originOfTranslations: [
              {
                languageCode: {
                  id: "5a36047e-3b85-52a2-812b-02420b4a8499",
                  label: "Französisch",
                  isoCode: "fr",
                },
                translators: ["Maxi Muster"],
                borderNumbers: [1],
                urls: ["www.link-to-translation.fr"],
                translationType: TranslationType.AMTLICH,
              },
            ],
            dismissalTypes: ["Test Kündigungsarten"],
            dismissalGrounds: ["Test Kündigungsgründe"],
            jobProfiles: ["Test Berufsbild"],
            hasLegislativeMandate: true,
            appealAdmission: {
              admitted: true,
              by: AppealAdmitter.FG,
            },
            appeal: {
              appellants: [
                {
                  id: "37213474-a727-4d85-8cc6-309d86944132",
                  value: "Kläger",
                },
              ],
            },
            collectiveAgreements: [
              {
                name: "Stehende Bühnen",
                norm: "§ 23",
                date: "12.2002",
                industry: {
                  id: "290b39dc-9368-4d1c-9076-7f96e05cb575",
                  label: "Bühne, Theater, Orchester",
                },
              },
            ],
            objectValues: [
              {
                amount: 123,
                currencyCode: {
                  id: "c7a92695-5171-459a-bd79-5cc741064a25",
                  label: "Dollar (USD)",
                  isoCode: "USD",
                },
                proceedingType: ProceedingType.VERFASSUNGSBESCHWERDE,
              },
            ],
            abuseFees: [
              {
                amount: 223,
                currencyCode: {
                  id: "c7a92695-5171-459a-bd79-5cc741064a25",
                  label: "Dollar (USD)",
                  isoCode: "USD",
                },
                addressee: Addressee.BEVOLLMAECHTIGTER,
              },
            ],
            countriesOfOrigin: [
              {
                country: country?.[0],
              },
            ],
            incomeTypes: [
              {
                terminology: "Programmierer",
                typeOfIncome: TypeOfIncome.GEWERBEBETRIEB,
              },
            ],
            relatedPendingProceedings: [
              {
                documentNumber: "YYTestDoc0017",
                court: courtBFH?.[0],
                decisionDate: "2022-02-01",
                fileNumber: "IV R 99/99",
              },
            ],
            nonApplicationNorms: [
              {
                normAbbreviation: normAbbreviation?.[0],
                singleNorms: [{ singleNorm: "§ 1" }],
              },
            ],
          },
        },
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    await use(await updateResponse.json())

    await deleteWithRetry(
      request,
      prefilledDocumentUnitWithLongTexts.uuid,
      csrfToken,
      prefilledDocumentUnitWithLongTexts.documentNumber,
    )
  },

  prefilledDocumentUnitWithLegacyCountryOfOrigin: async (
    { request, context },
    use,
  ) => {
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
    const response = await context.request.put(
      `/api/v1/caselaw/documentunits/new`,
      {
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )
    const documentUnit = await response.json()

    const courtResponse = await context.request.get(
      `api/v1/caselaw/courts?q=AG+Aachen`,
    )
    const court = await courtResponse.json()

    const documentTypeResponse = await context.request.get(
      `api/v1/caselaw/documenttypes?q=Anerkenntnisurteil`,
    )
    const documentType = await documentTypeResponse.json()

    const updateResponse = await context.request.put(
      `/api/v1/caselaw/documentunits/${documentUnit.uuid}`,
      {
        data: {
          ...documentUnit,
          coreData: {
            ...documentUnit.coreData,
            court: court?.[0],
            documentType: documentType?.[0],
            fileNumbers: [generateString()],
            decisionDate: "2020-01-01",
          },
          contentRelatedIndexing: {
            countriesOfOrigin: [
              {
                legacyValue: "legacy value",
              },
            ],
          },
        } as Decision,
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    await use(await updateResponse.json())

    await deleteWithRetry(
      request,
      documentUnit.uuid,
      csrfToken,
      documentUnit.documentNumber,
    )
  },

  secondPrefilledDocumentUnit: async ({ request, context }, use) => {
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
    const response = await context.request.put(
      `/api/v1/caselaw/documentunits/new`,
      {
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )
    const secondPrefilledDocumentUnit = await response.json()

    const courtResponse = await context.request.get(
      `api/v1/caselaw/courts?q=AG+Aachen`,
    )
    const court = await courtResponse.json()

    const documentTypeResponse = await context.request.get(
      `api/v1/caselaw/documenttypes?q=Anerkenntnisurteil`,
    )
    const documentType = await documentTypeResponse.json()

    const updateResponse = await context.request.put(
      `/api/v1/caselaw/documentunits/${secondPrefilledDocumentUnit.uuid}`,
      {
        data: {
          ...secondPrefilledDocumentUnit,
          coreData: {
            ...secondPrefilledDocumentUnit.coreData,
            court: court?.[0],
            documentType: documentType?.[0],
            fileNumbers: [generateString()],
            decisionDate: "2020-01-01",
          },
        } as Decision,
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    await use(await updateResponse.json())

    await deleteWithRetry(
      request,
      secondPrefilledDocumentUnit.uuid,
      csrfToken,
      secondPrefilledDocumentUnit.documentNumber,
    )
  },

  // The prefilledDocumentUnit fixture is a dependant worker fixture, because it needs to be setup before and teared down after this function (in order to be deletable).
  linkedDocumentNumber: async (
    { request, context, prefilledDocumentUnit },
    use,
  ) => {
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
    const response = await context.request.put(
      `/api/v1/caselaw/documentunits/new`,
      {
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )
    const { uuid, documentNumber } = await response.json()

    await use(documentNumber)

    try {
      await deleteWithRetry(request, uuid, csrfToken, documentNumber)
    } catch (error) {
      throw Error(
        `DocumentUnit with number ${documentNumber}, linked to ${prefilledDocumentUnit} couldn't be deleted: ${error}`,
      )
    }
  },

  pageWithBghUser: async ({ browser }, use) => {
    const bghContext = await browser.newContext({
      storageState: `test/e2e/caselaw/.auth/user_bgh.json`,
    })
    const pageWithBghUser = await bghContext.newPage()

    await use(pageWithBghUser)

    await pageWithBghUser.close()
    await bghContext.close()
  },

  pageWithBfhUser: async ({ browser }, use) => {
    const bfhContext = await browser.newContext({
      storageState: `test/e2e/caselaw/.auth/user_bfh.json`,
    })

    const pageWithBfhUser = await bfhContext.newPage()

    await use(pageWithBfhUser)

    await pageWithBfhUser.close()
    await bfhContext.close()
  },

  pageWithExternalUser: async ({ browser }, use) => {
    const externalContext = await browser.newContext({
      storageState: `test/e2e/caselaw/.auth/user_external.json`,
    })
    const pageWithExternalUser = await externalContext.newPage()

    await use(pageWithExternalUser)

    await pageWithExternalUser.close()
    await externalContext.close()
  },

  prefilledDocumentUnitBgh: async ({ request, browser }, use) => {
    const context = await browser.newContext({
      storageState: `test/e2e/caselaw/.auth/user_bgh.json`,
    })
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
    const response = await context.request.put(
      `/api/v1/caselaw/documentunits/new`,
      {
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )
    const prefilledDocumentUnit = await response.json()

    const courtResponse = await request.get(`api/v1/caselaw/courts?q=AG+Aachen`)
    const court = await courtResponse.json()

    const documentTypeResponse = await request.get(
      `api/v1/caselaw/documenttypes?q=Anerkenntnisurteil`,
    )
    const documentType = await documentTypeResponse.json()

    const updateResponse = await context.request.put(
      `/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}`,
      {
        data: {
          ...prefilledDocumentUnit,
          coreData: {
            ...prefilledDocumentUnit.coreData,
            court: court?.[0],
            documentType: documentType?.[0],
            fileNumbers: [generateString()],
            decisionDate: "2019-12-31",
            appraisalBody: "1. Senat, 2. Kammer",
          },
          shortTexts: {
            headnote: "testHeadnote",
            guidingPrinciple: "guidingPrinciple",
          },
          note: "example note",
        },
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    await use(await updateResponse.json())

    const deleteResponse = await context.request.delete(
      `/api/v1/caselaw/documentunits/${prefilledDocumentUnit.uuid}`,
      { headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" } },
    )
    if (!deleteResponse.ok()) {
      throw Error(`DocumentUnit with number ${prefilledDocumentUnit.documentNumber} couldn't be deleted:
      ${deleteResponse.status()} ${deleteResponse.statusText()}`)
    }
  },

  decisionBgh: async ({ browser }, use) => {
    const context = await browser.newContext({
      storageState: `test/e2e/caselaw/.auth/user_bgh.json`,
    })
    const request = context.request
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
    const response = await request.put(`/api/v1/caselaw/documentunits/new`, {
      headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
    })

    if (!response.ok()) {
      throw new Error(
        `Failed to create prefilledDocumentUnit: ${response.status()} ${response.statusText()}`,
      )
    }

    const decision = await response.json()

    await use(decision)

    // Teardown: Attempt to delete with the BGH user context first
    const deleteResponse = await context.request.delete(
      `/api/v1/caselaw/documentunits/${decision.uuid}`,
      { headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" } },
    )

    if (!deleteResponse.ok()) {
      // If the deletion fails with a 403, probably the docoffice ownership changed, we need to handle that gracefully and must be manually deleted in the test
      if (deleteResponse.status() === 403) {
        console.warn(
          `Deletion ${decision.documentNumber} failed with 403 for BGH user. Ownership likely changed. Document must be deleted manually.`,
        )
      } else {
        throw Error(
          `DocumentUnit with number ${decision.documentNumber} couldn't be deleted:
          ${deleteResponse.status()} ${deleteResponse.statusText()}`,
        )
      }
    }
  },

  edition: async ({ request, context }, use) => {
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")

    const legalPeriodicalSearchResponse = await request.get(
      `api/v1/caselaw/legalperiodicals?q=MMG`,
    )

    const legalPeriodical = (
      (await legalPeriodicalSearchResponse.json()) as LegalPeriodicalEdition[]
    ).at(0)

    const editionResponse = await request.put(
      `api/v1/caselaw/legalperiodicaledition`,
      {
        data: {
          legalPeriodical: legalPeriodical,
          id: crypto.randomUUID(),
          prefix: "2024, ",
          suffix: ", Heft " + generateString(),
          name: "2024, " + generateString(),
        },
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    const edition = await editionResponse.json()
    await use(edition)

    const deleteResponse = await request.delete(
      `/api/v1/caselaw/legalperiodicaledition/${edition.id}`,
      {
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    if (!deleteResponse.ok()) {
      throw Error(`Edition with number ${edition.id} couldn't be deleted:
      ${deleteResponse.status()} ${deleteResponse.statusText()}`)
    }
  },

  foreignDocumentationUnit: async ({ request }, use) => {
    const foreignDocumentUnitSearchResponse = await request.get(
      `api/v1/caselaw/documentunits/search?pg=0&sz=100&documentNumber=YYTestDoc0001&kind=DECISION`,
    )

    const foreignDocumentUnitPage =
      (await foreignDocumentUnitSearchResponse.json()) as Pagination<DocumentUnitListEntry>

    const foreignDocumentUnit = (
      foreignDocumentUnitPage.content as DocumentUnitListEntry[]
    ).at(0)

    await use(foreignDocumentUnit!)
  },

  editionWithReferences: async (
    { request, context, prefilledDocumentUnit },
    use,
  ) => {
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")

    const legalPeriodicalSearchResponse = await request.get(
      `api/v1/caselaw/legalperiodicals?q=MMG`,
    )

    const foreignDocumentUnitSearchResponse = await request.get(
      `api/v1/caselaw/documentunits/search?pg=0&sz=100&documentNumber=YYTestDoc0001&kind=DECISION`,
    )

    const eanDocType = (
      (await (
        await request.get(
          `api/v1/caselaw/documenttypes?q=Ean&category=DEPENDENT_LITERATURE`,
        )
      ).json()) as DocumentType[]
    )[0]

    const foreignDocumentUnitPage =
      (await foreignDocumentUnitSearchResponse.json()) as Pagination<DocumentUnitListEntry>

    const foreignDocumentUnit = (
      foreignDocumentUnitPage.content as DocumentUnitListEntry[]
    ).at(0)

    const legalPeriodical = (
      (await legalPeriodicalSearchResponse.json()) as LegalPeriodicalEdition[]
    ).at(0)

    const editionResponse = await request.put(
      `api/v1/caselaw/legalperiodicaledition`,
      {
        data: {
          legalPeriodical: legalPeriodical,
          id: crypto.randomUUID(),
          prefix: "2024, ",
          suffix: ", Heft 1",
          name: "2024, " + generateString(),
          references: [
            {
              referenceType: "caselaw",
              citation: "2024, 12-22, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical,
              documentationUnit: new RelatedDocumentation({
                documentNumber: prefilledDocumentUnit.documentNumber,
                uuid: prefilledDocumentUnit.uuid,
              }),
            },
            {
              referenceType: "caselaw",
              citation: "2024, 1-11, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical,
              // Published foreign BAG docunit
              documentationUnit: new RelatedDocumentation({
                documentNumber: foreignDocumentUnit?.documentNumber,
                uuid: foreignDocumentUnit?.uuid,
              }),
            },
            {
              referenceType: "literature",
              citation: "2024, 23-25, Heft 1",
              author: "Picard, Jean-Luc",
              documentType: eanDocType,
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical,
              documentationUnit: new RelatedDocumentation({
                documentNumber: prefilledDocumentUnit.documentNumber,
                uuid: prefilledDocumentUnit.uuid,
              }),
            },
            {
              referenceType: "literature",
              citation: "2024, 26, Heft 1",
              author: "Janeway, Kathryn",
              documentType: eanDocType,
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical,
              // Published foreign BAG docunit
              documentationUnit: new RelatedDocumentation({
                documentNumber: foreignDocumentUnit?.documentNumber,
                uuid: foreignDocumentUnit?.uuid,
              }),
            },
          ],
        },
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    const edition = await editionResponse.json()
    if (!editionResponse.ok()) {
      throw Error(`Edition couldn't be created:
      ${editionResponse.status()} ${editionResponse.statusText()}`)
    }
    await use(edition)

    // delete references to be able to delete
    const response = await request.put(
      `api/v1/caselaw/legalperiodicaledition`,
      {
        data: {
          legalPeriodical: edition.legalPeriodical,
          id: edition.id,
          prefix: edition.prefix,
          suffix: edition.suffix,
          name: "NAME",
          references: [],
        },
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    if (!response.ok()) {
      console.log(
        `References in Edition with number ${edition.id} couldn't be deleted:
      ${response.status()} ${response.statusText()}, retrying deletion of references...`,
      )
      // Retry after a random delay between 0.1s and 2s
      const retryWaitDuration = Math.floor(Math.random() * 1_900) + 100
      await new Promise((resolve) => setTimeout(resolve, retryWaitDuration))
      const retryResponse = await request.put(
        `api/v1/caselaw/legalperiodicaledition`,
        {
          data: {
            legalPeriodical: edition.legalPeriodical,
            id: edition.id,
            prefix: edition.prefix,
            suffix: edition.suffix,
            name: "NAME",
            references: [],
          },
          headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
        },
      )

      if (!retryResponse.ok()) {
        throw Error(`References in Edition with number ${edition.id} couldn't be deleted:
      ${response.status()} ${response.statusText()}`)
      }
    }

    const deleteResponse = await request.delete(
      `/api/v1/caselaw/legalperiodicaledition/${edition.id}`,
      {
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    if (!deleteResponse.ok()) {
      throw Error(`Edition with number ${edition.id} couldn't be deleted:
      ${deleteResponse.status()} ${deleteResponse.statusText()}`)
    }
  },

  editionWithManyReferences: async (
    { request, context, prefilledDocumentUnit },
    use,
  ) => {
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")

    const legalPeriodicalSearchResponse = await request.get(
      `api/v1/caselaw/legalperiodicals?q=MMG`,
    )

    const foreignDocumentUnitSearchResponse = await request.get(
      `api/v1/caselaw/documentunits/search?pg=0&sz=100&documentNumber=YYTestDoc0001&kind=DECISION`,
    )

    const foreignDocumentUnitPage =
      (await foreignDocumentUnitSearchResponse.json()) as Pagination<DocumentUnitListEntry>

    const foreignDocumentUnit = (
      foreignDocumentUnitPage.content as DocumentUnitListEntry[]
    ).at(0)

    const legalPeriodical = (
      (await legalPeriodicalSearchResponse.json()) as LegalPeriodicalEdition[]
    ).at(0)

    const editionResponse = await request.put(
      `api/v1/caselaw/legalperiodicaledition`,
      {
        data: {
          legalPeriodical: legalPeriodical,
          id: crypto.randomUUID(),
          prefix: "2024, ",
          suffix: ", Heft 1",
          name: "2024, " + generateString(),
          references: [
            {
              referenceType: "caselaw",
              citation: "2024, 12-22, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical,
              documentationUnit: new RelatedDocumentation({
                documentNumber: prefilledDocumentUnit.documentNumber,
                uuid: prefilledDocumentUnit.uuid,
              }),
            },
            {
              referenceType: "caselaw",
              citation: "2024, 1-11, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical,
              // Published foreign BAG docunit
              documentationUnit: new RelatedDocumentation({
                documentNumber: foreignDocumentUnit?.documentNumber,
                uuid: foreignDocumentUnit?.uuid,
              }),
            },
            {
              referenceType: "caselaw",
              citation: "2024, 1-11, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical,
              // Published foreign BAG docunit
              documentationUnit: new RelatedDocumentation({
                documentNumber: foreignDocumentUnit?.documentNumber,
                uuid: foreignDocumentUnit?.uuid,
              }),
            },
            {
              referenceType: "caselaw",
              citation: "2024, 1-11, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical,
              // Published foreign BAG docunit
              documentationUnit: new RelatedDocumentation({
                documentNumber: foreignDocumentUnit?.documentNumber,
                uuid: foreignDocumentUnit?.uuid,
              }),
            },
            {
              referenceType: "caselaw",
              citation: "2024, 1-11, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical,
              // Published foreign BAG docunit
              documentationUnit: new RelatedDocumentation({
                documentNumber: foreignDocumentUnit?.documentNumber,
                uuid: foreignDocumentUnit?.uuid,
              }),
            },
          ],
        },
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    const edition = await editionResponse.json()
    if (!editionResponse.ok()) {
      throw Error(`Edition couldn't be created:
      ${editionResponse.status()} ${editionResponse.statusText()}`)
    }
    await use(edition)

    // delete references to be able to delete
    const response = await request.put(
      `api/v1/caselaw/legalperiodicaledition`,
      {
        data: {
          legalPeriodical: edition.legalPeriodical,
          id: edition.id,
          prefix: edition.prefix,
          suffix: edition.suffix,
          name: "NAME",
          references: [],
        },
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    if (!response.ok()) {
      console.log(
        `References in Edition with number ${edition.id} couldn't be deleted:
      ${response.status()} ${response.statusText()}, retrying deletion of references...`,
      )
      // Retry after a random delay between 0.1s and 2s
      const retryWaitDuration = Math.floor(Math.random() * 1_900) + 100
      await new Promise((resolve) => setTimeout(resolve, retryWaitDuration))
      const retryResponse = await request.put(
        `api/v1/caselaw/legalperiodicaledition`,
        {
          data: {
            legalPeriodical: edition.legalPeriodical,
            id: edition.id,
            prefix: edition.prefix,
            suffix: edition.suffix,
            name: "NAME",
            references: [],
          },
          headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
        },
      )

      if (!retryResponse.ok()) {
        throw Error(`References in Edition with number ${edition.id} couldn't be deleted:
      ${response.status()} ${response.statusText()}`)
      }
    }

    const deleteResponse = await request.delete(
      `/api/v1/caselaw/legalperiodicaledition/${edition.id}`,
      {
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    if (!deleteResponse.ok()) {
      throw Error(`Edition with number ${edition.id} couldn't be deleted:
      ${deleteResponse.status()} ${deleteResponse.statusText()}`)
    }
  },

  // This is the decision that will be created by the fixture decision.
  // An empty decision is created by default. Can be overwritten in the test to give specific attributes to the decisionToBeCreated
  decisionToBeCreated: [{}, { option: true }],

  decision: async ({ decisionToBeCreated, context }, use) => {
    const request = context.request
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
    const fileNumber = generateString()

    const response = await request.put(`/api/v1/caselaw/documentunits/new`, {
      params: { kind: Kind.DECISION },
      headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      data: {
        fileNumber: fileNumber,
      },
    })

    if (!response.ok()) {
      throw new Error(
        `Failed to create decision document: ${response.status()} ${response.statusText()}`,
      )
    }

    const newDecision = await response.json()

    const courtLabel = decisionToBeCreated.coreData?.court?.label
    if (courtLabel) {
      const courtResponse = await request.get(
        `api/v1/caselaw/courts?q=${courtLabel}&sz=1&pg=0`,
      )
      decisionToBeCreated.coreData!.court = await courtResponse
        .json()
        .then((json) => json?.[0])
    }

    const createdDecision = mergeDeep(
      newDecision,
      decisionToBeCreated,
    ) as Decision

    const frontendPatch = jsonPatch.compare(newDecision, createdDecision)

    const patchResponse = await request.patch(
      `/api/v1/caselaw/documentunits/${newDecision.uuid}`,
      {
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
        data: {
          documentationUnitVersion: newDecision.version,
          patch: frontendPatch,
          errorPaths: [],
        },
      },
    )

    if (!patchResponse.ok()) {
      throw new Error(
        `Failed to patch decision: ${response.status()} ${response.statusText()}`,
      )
    }

    await use({
      createdDecision,
      fileNumber: fileNumber,
    })

    await deleteWithRetry(
      request,
      createdDecision.uuid,
      csrfToken,
      createdDecision.documentNumber,
    )
  },

  // These are the decisions that will be created by the fixture decisions.
  // By default, it is empty. Can be overridden by the test to create specific decisions.
  decisionsToBeCreated: [[], { option: true }],

  decisions: async ({ decisionsToBeCreated, context }, use) => {
    const request = context.request
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")

    const commonFileNumberPrefix = generateString()
    const createdDecisions = []
    for (const targetDecision of decisionsToBeCreated) {
      const response = await request.put(`/api/v1/caselaw/documentunits/new`, {
        params: { kind: Kind.DECISION },
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
        data: {
          fileNumber: generateString({ prefix: commonFileNumberPrefix }),
        },
      })

      if (!response.ok()) {
        throw new Error(
          `Failed to create decision document: ${response.status()} ${response.statusText()}`,
        )
      }

      const newDecision = await response.json()

      const courtLabel = targetDecision.coreData?.court?.label
      if (courtLabel) {
        const courtResponse = await request.get(
          `api/v1/caselaw/courts?q=${courtLabel}&sz=1&pg=0`,
        )
        targetDecision.coreData!.court = await courtResponse
          .json()
          .then((json) => json?.[0])
      }

      const docTypeLabel = targetDecision.coreData?.documentType?.label
      if (docTypeLabel) {
        const documentTypeResponse = await request.get(
          `api/v1/caselaw/documenttypes?q=${docTypeLabel}`,
        )
        targetDecision.coreData!.documentType = await documentTypeResponse
          .json()
          .then((json) => json?.[0])
      }

      const patchedDecision = mergeDeep(newDecision, targetDecision) as Decision

      const frontendPatch = jsonPatch.compare(newDecision, patchedDecision)

      const patchResponse = await request.patch(
        `/api/v1/caselaw/documentunits/${newDecision.uuid}`,
        {
          headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
          data: {
            documentationUnitVersion: newDecision.version,
            patch: frontendPatch,
            errorPaths: [],
          },
        },
      )

      if (!patchResponse.ok()) {
        throw new Error(
          `Failed to patch decision: ${response.status()} ${response.statusText()}`,
        )
      }
      createdDecisions.push(patchedDecision)
    }

    await use({
      createdDecisions,
      fileNumberPrefix: commonFileNumberPrefix,
    })

    for (const newDecision of createdDecisions) {
      await deleteWithRetry(
        request,
        newDecision.uuid,
        csrfToken,
        newDecision.documentNumber,
      )
    }
  },

  pendingProceeding: async ({ request, context }, use) => {
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")

    const parameters = {
      fileNumber: generateString(),
    }
    const response = await request.put(`/api/v1/caselaw/documentunits/new`, {
      params: { kind: Kind.PENDING_PROCEEDING },
      headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      data: parameters,
    })

    if (!response.ok()) {
      throw new Error(
        `Failed to create pending proceeding document: ${response.status()} ${response.statusText()}`,
      )
    }

    const pendingProceeding = await response.json()

    await use(pendingProceeding)

    await deleteWithRetry(
      request,
      pendingProceeding.uuid,
      csrfToken,
      pendingProceeding.documentNumber,
    )
  },

  // These are the pending proceedings that will be created by the fixture pendingProceedings.
  // By default, it is empty. Can be overridden by the test to create specific pending proceedings.
  pendingProceedingsToBeCreated: [[], { option: true }],

  pendingProceedings: async (
    { browser, pendingProceedingsToBeCreated },
    use,
  ) => {
    const context = await browser.newContext({
      storageState: `test/e2e/caselaw/.auth/user_bfh.json`,
    })
    const request = context.request
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")

    const commonFileNumberPrefix = generateString()
    const createdPendingProceedings = []
    for (const targetPendingProceeding of pendingProceedingsToBeCreated) {
      const response = await request.put(`/api/v1/caselaw/documentunits/new`, {
        params: { kind: Kind.PENDING_PROCEEDING },
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
        data: {
          fileNumber: generateString({ prefix: commonFileNumberPrefix }),
        },
      })

      if (!response.ok()) {
        throw new Error(
          `Failed to create pending proceeding document: ${response.status()} ${response.statusText()}`,
        )
      }

      const newPendingProceeding = await response.json()

      const courtLabel = targetPendingProceeding.coreData?.court?.label
      if (courtLabel) {
        const courtResponse = await request.get(
          `api/v1/caselaw/courts?q=${courtLabel}&sz=1&pg=0`,
        )
        targetPendingProceeding.coreData!.court = await courtResponse
          .json()
          .then((json) => json?.[0])
      }

      const patchedPendingProceeding = mergeDeep(
        newPendingProceeding,
        targetPendingProceeding,
      ) as PendingProceeding

      const frontendPatch = jsonPatch.compare(
        newPendingProceeding,
        patchedPendingProceeding,
      )

      const patchResponse = await request.patch(
        `/api/v1/caselaw/documentunits/${newPendingProceeding.uuid}`,
        {
          headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
          data: {
            documentationUnitVersion: newPendingProceeding.version,
            patch: frontendPatch,
            errorPaths: [],
          },
        },
      )

      if (!patchResponse.ok()) {
        throw new Error(
          `Failed to patch pending proceeding: ${response.status()} ${response.statusText()}`,
        )
      }
      createdPendingProceedings.push(patchedPendingProceeding)
    }

    await use({
      createdPendingProceedings,
      fileNumberPrefix: commonFileNumberPrefix,
    })

    for (const newPendingProceeding of createdPendingProceedings) {
      await deleteWithRetry(
        request,
        newPendingProceeding.uuid,
        csrfToken,
        newPendingProceeding.documentNumber,
      )
    }
  },
})
