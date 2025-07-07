import {
  APIRequestContext,
  Cookie,
  Locator,
  Page,
  test,
} from "@playwright/test"
import dayjs from "dayjs"
import utc from "dayjs/plugin/utc.js"
import { navigateToCategories } from "./e2e-utils"
import { Page as Pagination } from "@/components/Pagination.vue"
import { Decision } from "@/domain/decision"
import { Kind } from "@/domain/documentationUnitKind"
import DocumentUnitListEntry from "@/domain/documentUnitListEntry"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
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
  editorField: Locator
  pageWithBghUser: Page
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
  pendingProceeding: PendingProceeding
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
  const deleteResponse = await request.delete(
    `/api/v1/caselaw/documentunits/${uuid}`,
    {
      headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
    },
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
      {
        headers: { "X-XSRF-TOKEN": csrfToken?.value ?? "" },
      },
    )

    if (!retryDeleteResponse.ok()) {
      throw Error(`DocumentUnit with number ${documentNumber} couldn't be deleted:
      ${deleteResponse.status()} ${deleteResponse.statusText()}`)
    }
  }
}

export const caselawTest = test.extend<MyFixtures>({
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
      `api/v1/caselaw/fieldsoflaw/search-by-identifier?q=AR-01`,
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
            source: {
              value: SourceValue.AngefordertesOriginal,
            },
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
              id: crypto.randomUUID(),
              citation: "2024, 1-2, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              referenceType: "caselaw",
            },
          ],
          literatureReferences: [
            {
              id: crypto.randomUUID(),
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
      `api/v1/caselaw/documenttypesq=Ean&category=DEPENDENT_LITERATURE`,
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
              id: crypto.randomUUID(),
              citation: "2024, 1-2, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              referenceType: "caselaw",
            },
            {
              id: crypto.randomUUID(),
              citation: "2024, 1-2, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              referenceType: "caselaw",
            },
            {
              id: crypto.randomUUID(),
              citation: "2024, 1-2, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              referenceType: "caselaw",
            },
            {
              id: crypto.randomUUID(),
              citation: "2024, 1-2, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              referenceType: "caselaw",
            },
            {
              id: crypto.randomUUID(),
              citation: "2024, 1-2, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              referenceType: "caselaw",
            },
            {
              id: crypto.randomUUID(),
              citation: "2024, 1-2, Heft 1",
              referenceSupplement: "L",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              referenceType: "caselaw",
            },
          ],
          literatureReferences: [
            {
              id: crypto.randomUUID(),
              citation: "2024, 3-4, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              author: "Krümelmonster",
              documentType: literatureDocumentType?.[0],
              referenceType: "literature",
            },
            {
              id: crypto.randomUUID(),
              citation: "2024, 3-4, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              author: "Krümelmonster",
              documentType: literatureDocumentType?.[0],
              referenceType: "literature",
            },
            {
              id: crypto.randomUUID(),
              citation: "2024, 3-4, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              author: "Krümelmonster",
              documentType: literatureDocumentType?.[0],
              referenceType: "literature",
            },
            {
              id: crypto.randomUUID(),
              citation: "2024, 3-4, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              author: "Krümelmonster",
              documentType: literatureDocumentType?.[0],
              referenceType: "literature",
            },
            {
              id: crypto.randomUUID(),
              citation: "2024, 3-4, Heft 1",
              legalPeriodicalRawValue: "MMG",
              legalPeriodical: legalPeriodical?.[0],
              author: "Krümelmonster",
              documentType: literatureDocumentType?.[0],
              referenceType: "literature",
            },
            {
              id: crypto.randomUUID(),
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

    const documentTypeResponse = await request.get(
      `api/v1/caselaw/documenttypes?q=Anerkenntnisurteil`,
    )
    const documentType = await documentTypeResponse.json()

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
        },
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

  editorField: async ({ page, documentNumber }, use) => {
    await navigateToCategories(page, documentNumber)
    const editorField = page.locator("[data-testid='Entscheidungsname'] >> div")
    await editorField.click()
    await editorField.type("this is text")

    await use(editorField)
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
      `api/v1/caselaw/documentunits/search?pg=0&sz=100&documentNumber=YYTestDoc0001`,
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
      `api/v1/caselaw/documentunits/search?pg=0&sz=100&documentNumber=YYTestDoc0001`,
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
              id: crypto.randomUUID(),
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
              id: crypto.randomUUID(),
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
              id: crypto.randomUUID(),
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
              id: crypto.randomUUID(),
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
      throw Error(`References in Edition with number ${edition.id} couldn't be deleted:
      ${response.status()} ${response.statusText()}`)
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
      `api/v1/caselaw/documentunits/search?pg=0&sz=100&documentNumber=YYTestDoc0001`,
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
              id: crypto.randomUUID(),
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
              id: crypto.randomUUID(),
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
              id: crypto.randomUUID(),
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
              id: crypto.randomUUID(),
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
              id: crypto.randomUUID(),
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
      throw Error(`References in Edition with number ${edition.id} couldn't be deleted:
      ${response.status()} ${response.statusText()}`)
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

  pendingProceeding: async ({ request, context }, use) => {
    const cookies = await context.cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")

    const documentTypeResponse = await request.get(
      `api/v1/caselaw/documenttypes?q=Anh&category=CASELAW_PENDING_PROCEEDING`,
    )
    const documentType = await documentTypeResponse.json()

    const parameters = {
      documentType: documentType?.[0],
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
})
