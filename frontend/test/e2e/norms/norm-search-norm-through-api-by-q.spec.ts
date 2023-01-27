import { expect } from "@playwright/test"
import { testWithImportedNorm } from "./fixtures"

const backendHost = process.env.E2E_BASE_URL ?? "http://127.0.0.1"
const officialLongTitle =
  "Gesetz über die Beschaffung sauberer Straßenfahrzeuge"

testWithImportedNorm(
  "Check norm can be retrieved by search in long title",
  async ({ request }) => {
    const response = await request.get(
      `${backendHost}/api/v1/norms?q=über die Beschaffung`
    )
    await assertResponseOk(response)
  }
)

testWithImportedNorm(
  "Check norm can be retrieved by search in short title",
  async ({ request }) => {
    const response = await request.get(
      `${backendHost}/api/v1/norms?q=Saubere-Fahrzeuge-Beschaffungs-Gesetz`
    )
    await assertResponseOk(response)
  }
)

testWithImportedNorm(
  "Check norm can be retrieved by search in unofficial short title",
  async ({ request }) => {
    const response = await request.get(
      `${backendHost}/api/v1/norms?q=Saubere-Fahrzeuge`
    )
    await assertResponseOk(response)
  }
)

testWithImportedNorm(
  "Check 404 is returned if no articles found",
  async ({ request }) => {
    const response = await request.get(
      `${backendHost}/api/v1/norms?q=invalidSearchQuery`
    )
    expect(response.ok()).toBeTruthy()
    const norms = await response.json()
    await expect(norms.data.length).toBe(0)
  }
)

async function assertResponseOk(response) {
  expect(response.ok()).toBeTruthy()
  const norms = await response.json()
  expect(norms.data[0].officialLongTitle).toBe(officialLongTitle)
}
