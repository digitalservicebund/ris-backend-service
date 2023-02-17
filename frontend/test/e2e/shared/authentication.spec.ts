import { expect } from "@playwright/test"
import { testWithDocumentUnit as test } from "../caselaw/fixtures"

test.describe("authentication", () => {
  test("should not be able to access with invalid session and redirect to login", async ({
    page,
    documentNumber,
  }) => {
    await page.goto("/")
    await expect(page.getByText("Neue Dokumentationseinheit")).toBeVisible()

    await page.context().clearCookies()

    await page.goto(`/caselaw/documentunit/${documentNumber}/categories`)
    await expect(page.locator("text=Spruchkörper")).toBeHidden()
    await expect(page.getByLabel("E-Mailadresse")).toBeVisible()
  })

  test("should get new session ID without new login", async ({
    page,
    documentNumber,
  }) => {
    await page.goto("/")
    await expect(page.getByText("Neue Dokumentationseinheit")).toBeVisible()

    await page.context().clearCookies()

    await page.goto(`/caselaw/documentunit/${documentNumber}/categories`)
    await expect(page.locator("text=Spruchkörper")).toBeHidden()
    await expect(page.getByLabel("E-Mailadresse")).toBeVisible()
  })

  test("should should remember location after new login", async ({
    page,
    documentNumber,
  }) => {
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
  })
})
