import { expect, test } from "@playwright/test"
import { testWithDocumentUnit } from "../caselaw/fixtures"

test.describe("authentication", () => {
  testWithDocumentUnit(
    "should not be able to access with invalid session and redirect to login",
    async ({ page, documentNumber }) => {
      await page.goto("/")
      await expect(page.getByText("Neue Dokumentationseinheit")).toBeVisible()

      await page.context().clearCookies()

      await page.goto(`/caselaw/documentunit/${documentNumber}/categories`)
      await expect(page.locator("text=Spruchkörper")).toBeHidden()
      await expect(page.getByLabel("E-Mailadresse")).toBeVisible()
    }
  )

  testWithDocumentUnit(
    "should get new session ID without new login",
    async ({ page, documentNumber }) => {
      await page.goto("/")
      await expect(page.getByText("Neue Dokumentationseinheit")).toBeVisible()

      await page.context().clearCookies()

      await page.goto(`/caselaw/documentunit/${documentNumber}/categories`)
      await expect(page.locator("text=Spruchkörper")).toBeHidden()
      await expect(page.getByLabel("E-Mailadresse")).toBeVisible()
    }
  )

  testWithDocumentUnit(
    "should should remember location after new login",
    async ({ page, documentNumber }) => {
      await page.goto("/")
      await expect(page.getByText("Neue Dokumentationseinheit")).toBeVisible()
      const validCookies = await page.context().cookies()

      await page.context().clearCookies()
      await page.goto(`/caselaw/documentunit/${documentNumber}/categories`)

      // expect to be on login page
      await expect(page.locator("text=Spruchkörper")).toBeHidden()
      await expect(page.getByLabel("E-Mailadresse")).toBeVisible()

      // login
      await page.context().addCookies(validCookies)
      await page.goto("/")

      await expect(page).toHaveURL(
        `/caselaw/documentunit/${documentNumber}/categories`
      )
    }
  )

  test("public endpoints (`open/`) should be restricted with basicAuth", async ({
    page,
    baseURL,
  }) => {
    // E2E_BASE_URL is only set in staging
    // eslint-disable-next-line playwright/no-skipped-test
    test.skip(!process.env.E2E_BASE_URL)

    expect((await page.request.get("/api/v1/open/norms")).status()).toEqual(401)
    const hostname = new URL(baseURL as string).hostname
    expect(
      (
        await page.request.get(
          `https://${process.env.E2E_TEST_BASIC_AUTH_USER}:${process.env.E2E_TEST_BASIC_AUTH_PASSWORD}@${hostname}/api/v1/open/norms`
        )
      ).status()
    ).toEqual(200)
  })
})
