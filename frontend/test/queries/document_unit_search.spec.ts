import { expect, Page, TestInfo } from "@playwright/test"
import { Decision } from "@/domain/decision"
import { DocumentationUnitSearchParameter } from "@/types/documentationUnitSearchParameter"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { getRequest } from "~/e2e/caselaw/utils/e2e-utils"

// This is a performance test for the backend search endpoint
// We run it sequentially not to skew the results
test.describe("document unit search queries", () => {
  const testConfigurations: {
    title: string
    parameter: { [K in DocumentationUnitSearchParameter]?: string }
    maxDuration: number
    minResults?: number
    isRelevantForExternalUser?: boolean
  }[] = [
    {
      title: "documentNumber and courtType",
      parameter: {
        documentNumber: "BVRE",
        courtType: "VerfGH",
        kind: "DECISION",
      },
      maxDuration: 1500, // last max 776, average 365, min 259
      minResults: 5,
    },
    {
      title: "vague documentNumber",
      parameter: {
        documentNumber: "BV",
        kind: "DECISION",
      },
      maxDuration: 2000, // last max 1570 average 1370, min 1311
      minResults: 5,
    },
    {
      title: "vague documentNumber (pending proceeding)",
      parameter: {
        documentNumber: "ST",
        kind: "PENDING_PROCEEDING",
      },
      maxDuration: 800, // last max 686 average 344, min 299
      minResults: 5,
    },
    {
      title: "not existing documentNumber",
      parameter: {
        documentNumber: "notExistingFoo",
        kind: "DECISION",
      },
      maxDuration: 900, // last max 512, average 214, min 179
    },
    {
      title: "not existing documentNumber (pending proceeding)",
      parameter: {
        documentNumber: "notExistingFoo",
        kind: "PENDING_PROCEEDING",
      },
      maxDuration: 900, // last max 519, average 215, min 182
    },
    {
      title: "vague fileNumber",
      parameter: {
        fileNumber: "Bv",
        kind: "DECISION",
      },
      maxDuration: 1500, // last max 970, average 536, min 470
      minResults: 5,
      isRelevantForExternalUser: true,
    },
    {
      title: "vague fileNumber (pending proceeding)",
      parameter: {
        fileNumber: "T-",
        kind: "PENDING_PROCEEDING",
      },
      maxDuration: 1500, // last max 286, average 210, min 191
      minResults: 5,
    },
    {
      title: "not existing fileNumber",
      parameter: {
        fileNumber: "notExistingFoo",
        kind: "DECISION",
      },
      maxDuration: 1200, // last max 749, average 460, min 420
    },
    {
      title: "not existing fileNumber (pending proceeding)",
      parameter: {
        fileNumber: "notExistingFoo",
        kind: "PENDING_PROCEEDING",
      },
      maxDuration: 1200, // last max 206, average 167, min 143
    },
    {
      title: "only unpublished",
      parameter: {
        publicationStatus: "UNPUBLISHED",
        kind: "DECISION",
      },
      maxDuration: 900, // last max 1784, average 512, min 422
      minResults: 5,
    },
    {
      title: "only unpublished (pending proceeding)",
      parameter: {
        publicationStatus: "UNPUBLISHED",
        kind: "PENDING_PROCEEDING",
      },
      maxDuration: 700, // last max 535, average 230 , min 202
    },
    {
      title: "of all time",
      parameter: {
        decisionDate: "1900-01-01",
        decisionDateEnd: "2024-01-15",
        kind: "DECISION",
      },
      maxDuration: 900, // last max 736, average 336, min 270
      minResults: 5,
      isRelevantForExternalUser: true,
    },
    {
      title: "of all time (pending proceeding)",
      parameter: {
        decisionDate: "1900-01-01",
        decisionDateEnd: "2024-01-15",
        kind: "PENDING_PROCEEDING",
      },
      maxDuration: 700, // last max 683, average 246, min 195
      minResults: 5,
    },
    {
      title: "resolution date of all time (pending proceeding)",
      parameter: {
        resolutionDate: "1900-01-01",
        resolutionDateEnd: "2025-01-15",
        kind: "PENDING_PROCEEDING",
      },
      maxDuration: 700, // last max 418, average 208, min 172
      minResults: 5,
    },
    {
      title: "only resolved (pending proceeding)",
      parameter: {
        isResolved: "true",
        kind: "PENDING_PROCEEDING",
      },
      maxDuration: 800, // last max 231, average 183, min 162
      minResults: 5,
    },
    {
      title: "one day",
      parameter: {
        decisionDate: "1975-06-16",
        kind: "DECISION",
      },
      maxDuration: 800, // last max 249, average 243, min 179
      minResults: 1,
    },
    {
      title: "only court location",
      parameter: {
        courtLocation: "München",
        kind: "DECISION",
      },
      maxDuration: 3000, // last max 1808, average 1727, min 1688
      minResults: 5,
    },
    {
      title: "only court location (pending proceeding)",
      parameter: {
        courtLocation: "München",
        kind: "PENDING_PROCEEDING",
      },
      maxDuration: 3000, // last max 7815, average 2339, min 1650
      minResults: 5,
    },
    {
      title: "only court type",
      parameter: {
        courtType: "VerfGH",
        kind: "DECISION",
      },
      maxDuration: 1000, // last max 609, average 356, min 289
      minResults: 5,
    },
    {
      title: "only court type (pending proceeding)",
      parameter: {
        courtType: "BFH",
        kind: "PENDING_PROCEEDING",
      },
      maxDuration: 1000, // last max 602, average 502, min 463
      minResults: 5,
    },
    {
      title: "only my doc office",
      parameter: {
        myDocOfficeOnly: "true",
        kind: "DECISION",
      },
      maxDuration: 900, // last max 798, average 505, min 460
      minResults: 5,
    },
    {
      title: "scheduled only",
      parameter: {
        myDocOfficeOnly: "true",
        scheduledOnly: "true",
        kind: "DECISION",
      },
      maxDuration: 600,
      minResults: 3,
    },
    {
      title: "publication date",
      parameter: {
        myDocOfficeOnly: "true",
        publicationDate: "2100-11-21",
        kind: "DECISION",
      },
      maxDuration: 600,
      minResults: 3,
    },
    {
      title: "with duplicate warning",
      parameter: {
        myDocOfficeOnly: "true",
        withDuplicateWarning: "true",
        kind: "DECISION",
      },
      maxDuration: 800, // last max 799, average 505, min 463
      minResults: 3,
    },
    {
      title: "with process step",
      parameter: {
        myDocOfficeOnly: "true",
        processStep: "Ersterfassung",
        kind: "DECISION",
      },
      maxDuration: 800, // last max 799, average 505, min 463
      minResults: 3,
    },
  ]

  const externalConfigurations = testConfigurations.filter(
    (config) => config.isRelevantForExternalUser,
  )

  testConfigurations.forEach((search) =>
    test(search.title, async ({ page }, testInfo) =>
      runTestMultipleTimes(10, search, page, testInfo),
    ),
  )
  externalConfigurations.forEach((search) =>
    test(`${search.title} (external user)`, async ({
      pageWithExternalUser,
    }, testInfo) =>
      runTestMultipleTimes(10, search, pageWithExternalUser, testInfo)),
  )
})

async function runTestMultipleTimes(
  runs: number,
  search: {
    title: string
    parameter: { [K in DocumentationUnitSearchParameter]?: string }
    maxDuration: number
    minResults?: number
  },
  page: Page,
  testInfo: TestInfo,
  durations: number[] = [],
) {
  if (runs === 0) {
    await testInfo.attach("durations", {
      body: Buffer.from(JSON.stringify(durations)),
      contentType: "application/json",
    })
    await testInfo.attach("maxDuration", {
      body: search.maxDuration.toString(),
      contentType: "application/text",
    })
    const meanDuration = durations.reduce((a, b) => a + b, 0) / durations.length
    expect(meanDuration).toBeLessThan(search.maxDuration)
    return
  }

  const url =
    "/api/v1/caselaw/documentunits/search?pg=0&sz=100" +
    getUrlParams(search.parameter)
  const request = await getRequest(url, page)

  const duration = request.timing().responseStart
  expect(duration).not.toBe(-1)

  const response = await request.response()
  expect(response?.ok()).toBe(true)

  if (search.minResults) {
    const documentUnits =
      ((await response?.json())?.content as Decision[]) || []
    expect(documentUnits.length).toBeGreaterThanOrEqual(search.minResults)
  }

  await runTestMultipleTimes(runs - 1, search, page, testInfo, [
    ...durations,
    duration,
  ])
}

function getUrlParams(parameter: {
  [K in DocumentationUnitSearchParameter]?: string
}): string {
  return (
    parameter &&
    "&" +
      Object.entries(parameter)
        .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
        .join("&")
  )
}
