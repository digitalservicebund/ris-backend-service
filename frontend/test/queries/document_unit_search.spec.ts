import { expect, test, Request, Page, TestInfo } from "@playwright/test"
import { DocumentUnitSearchParameter } from "../../src/components/DocumentUnitSearchEntryForm.vue"
import DocumentUnit from "../../src/domain/documentUnit"

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
        documentNumber: "KORE",
        courtType: "BGH",
      },
      maxDuration: 500,
      minResults: 5,
    },
    {
      title: "vague documentNumber",
      parameter: {
        documentNumber: "KO",
      },
      maxDuration: 500,
      minResults: 5,
    },
    {
      title: "not existing documentNumber",
      parameter: {
        documentNumber: "notExistingFoo",
      },
      maxDuration: 500,
    },
    {
      title: "vague fileNumber",
      parameter: {
        fileNumber: "VI",
      },
      maxDuration: 500,
      minResults: 5,
    },
    {
      title: "not existing fileNumber",
      parameter: {
        fileNumber: "notExistingFoo",
      },
      maxDuration: 500,
    },
    {
      title: "only unpublished",
      parameter: {
        publicationStatus: "UNPUBLISHED",
      },
      maxDuration: 500,
      minResults: 5,
    },
    {
      title: "of all time",
      parameter: {
        decisionDate: "1900-01-01",
        decisionDateEnd: " 2024-01-15",
      },
      maxDuration: 500,
      minResults: 5,
    },
    {
      title: "one day",
      parameter: {
        decisionDate: "2021-04-22",
      },
      maxDuration: 200,
      minResults: 5,
    },
    {
      title: "only court location",
      parameter: {
        courtLocation: "Berlin",
      },
      maxDuration: 200,
      minResults: 5,
    },
    {
      title: "only court type",
      parameter: {
        courtType: "BGH",
      },
      maxDuration: 200,
      minResults: 5,
    },
    {
      title: "only my doc office",
      parameter: {
        myDocOfficeOnly: "true",
      },
      maxDuration: 200,
      minResults: 5,
    },
  ]

  testConfigurations.forEach((search) =>
    test(search.title, async ({ page }, testInfo) =>
      runTestMultipleTimes(10, search, page, testInfo),
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
      body: Buffer.from(durations),
      contentType: "application/json",
    })
    return
  }

  const request = await getRequest(
    "/api/v1/caselaw/documentunits/search?pg=0&sz=30" +
      getUrlParams(search.parameter),
    page,
  )

  const duration = request.timing().responseStart
  expect(duration).not.toBe(-1)
  expect(duration).toBeLessThan(search.maxDuration)
  if (search.minResults) {
    const documentUnits =
      ((await (await request.response())?.json())?.content as DocumentUnit[]) ||
      []
    expect(documentUnits.length).toBeGreaterThan(search.minResults)
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
