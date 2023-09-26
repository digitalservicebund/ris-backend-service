import { expect } from "@playwright/test"
import { testWithImportedNorm } from "./fixtures"

testWithImportedNorm(
  "Check 404 is returned if no articles found",
  async ({ request }) => {
    const response = await request.get(`/api/v1/norms?q=invalidSearchQuery`)
    await expect(response).toBeOK()
    const norms = await response.json()
    expect(norms.data.length).toBe(0)
  },
)
