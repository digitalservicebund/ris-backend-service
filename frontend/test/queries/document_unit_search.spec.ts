import { expect, test, Request, Page, TestInfo } from "@playwright/test"
import { DocumentUnitSearchParameter } from "../../src/components/DocumentUnitSearchEntryForm.vue"
import DocumentUnit from "../../src/domain/documentUnit"

// This is a performance test for the backend search endpoint
// We run it sequentially not to skew the results
test.describe("document unit search queries", () => {
  const testConfigurations: {
    title: string
    parameter: { [K in DocumentUnitSearchParameter]?: string }
    maxDuration: number
    minResults?: number
  }[] = [
    {
      title: "documentNumber and courtType",
      parameter: {
        documentNumber: "BVRE",
        courtType: "VerfGH",
      },
      maxDuration: 500, // last max 382
      minResults: 5,
    },
    {
      title: "vague documentNumber",
      parameter: {
        documentNumber: "BV",
      },
      maxDuration: 3000, // last max 1547
      minResults: 5,
    },
    {
      title: "not existing documentNumber",
      parameter: {
        documentNumber: "notExistingFoo",
      },
      maxDuration: 300, // last max 228
    },
    {
      title: "vague fileNumber",
      parameter: {
        fileNumber: "Bv",
      },
      maxDuration: 1700, // last max 1365
      minResults: 5,
    },
    {
      title: "not existing fileNumber",
      parameter: {
        fileNumber: "notExistingFoo",
      },
      maxDuration: 300, // last max 230
    },
    {
      title: "only unpublished",
      parameter: {
        publicationStatus: "UNPUBLISHED",
      },
      maxDuration: 1300, // last max 1041
      minResults: 5,
    },
    {
      title: "of all time",
      parameter: {
        decisionDate: "1900-01-01",
        decisionDateEnd: "2024-01-15",
      },
      maxDuration: 400, // last max 332
      minResults: 5,
    },
    {
      title: "one day",
      parameter: {
        decisionDate: "1975-06-16",
      },
      maxDuration: 350, // last max 249
      minResults: 1,
    },
    {
      title: "only court location",
      parameter: {
        courtLocation: "München",
      },
      maxDuration: 1700, // last max 1249
      minResults: 5,
    },
    {
      title: "only court type",
      parameter: {
        courtType: "VerfGH",
      },
      maxDuration: 450, // last max 364
      minResults: 5,
    },
    {
      title: "only my doc office",
      parameter: {
        myDocOfficeOnly: "true",
      },
      maxDuration: 450, // last max 332
      minResults: 5,
    },
  ]

  testConfigurations.forEach((search) =>
    test(search.title, async ({ page }, testInfo) =>
      runTestMultipleTimes(5, search, page, testInfo),
    ),
  )
})

async function runTestMultipleTimes(
  runs: number,
  search: {
    title: string
    parameter: { [K in DocumentUnitSearchParameter]?: string }
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
  if (search.minResults) {
    const documentUnits =
      ((await (await request.response())?.json())?.content as DocumentUnit[]) ||
      []
    expect(documentUnits.length).toBeGreaterThanOrEqual(search.minResults)
  }

  await runTestMultipleTimes(runs - 1, search, page, testInfo, [
    ...durations,
    duration,
  ])
}

function getUrlParams(parameter: {
  [K in DocumentUnitSearchParameter]?: string
}): string {
  return (
    parameter &&
    "&" +
      Object.entries(parameter)
        .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
        .join("&")
  )
}

async function getRequest(url: string, page: Page): Promise<Request> {
  const requestFinishedPromise = page.waitForEvent("requestfinished")
  await page.goto(url)
  return await requestFinishedPromise
}
