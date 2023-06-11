import { APIResponse, expect } from "@playwright/test"
import { testWithImportedNorm } from "./fixtures"
import { normData } from "./testdata/norm_basic"

testWithImportedNorm(
  "Check norm can be retrieved by search in long title",
  async ({ request }) => {
    const response = await request.get(
      `/api/v1/norms?q=Verordnung zur Anpassung`
    )
    await assertResponseOk(response)
  }
)

testWithImportedNorm(
  "Check norm can be retrieved by search in short title",
  async ({ request }) => {
    const response = await request.get(
      `/api/v1/norms?q=Angepasstes Tierarzneimittelrecht`
    )
    await assertResponseOk(response)
  }
)

testWithImportedNorm(
  "Check 404 is returned if no articles found",
  async ({ request }) => {
    const response = await request.get(`/api/v1/norms?q=invalidSearchQuery`)
    expect(response.ok()).toBeTruthy()
    const norms = await response.json()
    expect(norms.data.length).toBe(0)
  }
)

async function assertResponseOk(response: APIResponse) {
  expect(response.ok()).toBeTruthy()
  const norms = await response.json()
  expect(norms.data[0].officialLongTitle).toBe(normData.officialLongTitle)
}
