import { expect } from "@playwright/test"
import { testWithImportedNorm } from "./fixtures"

const officialLongTitle =
  "Gesetz über die Beschaffung sauberer Straßenfahrzeuge"

// eslint-disable-next-line playwright/no-skipped-test
testWithImportedNorm.skip(
  "Check norm can be retrieved by search in long title",
  async ({ request }) => {
    const response = await request.get(`/api/v1/norms?q=über die Beschaffung`)
    await assertResponseOk(response)
  }
)

// eslint-disable-next-line playwright/no-skipped-test
testWithImportedNorm.skip(
  "Check norm can be retrieved by search in short title",
  async ({ request }) => {
    const response = await request.get(
      `/api/v1/norms?q=Saubere-Fahrzeuge-Beschaffungs-Gesetz`
    )
    await assertResponseOk(response)
  }
)

// eslint-disable-next-line playwright/no-skipped-test
testWithImportedNorm.skip(
  "Check norm can be retrieved by search in unofficial short title",
  async ({ request }) => {
    const response = await request.get(`/api/v1/norms?q=Saubere-Fahrzeuge`)
    await assertResponseOk(response)
  }
)

// eslint-disable-next-line playwright/no-skipped-test
testWithImportedNorm.skip(
  "Check 404 is returned if no articles found",
  async ({ request }) => {
    const response = await request.get(`/api/v1/norms?q=invalidSearchQuery`)
    expect(response.ok()).toBeTruthy()
    const norms = await response.json()
    expect(norms.data.length).toBe(0)
  }
)

async function assertResponseOk(response) {
  expect(response.ok()).toBeTruthy()
  const norms = await response.json()
  expect(norms.data[0].officialLongTitle).toBe(officialLongTitle)
}
