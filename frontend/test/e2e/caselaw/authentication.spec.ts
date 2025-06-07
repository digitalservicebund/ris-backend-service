import { expect } from "@playwright/test"
import { caselawTest as test } from "./fixtures"

test.describe("authentication", () => {
  test("name and documentation center should be displayed", async ({
    page,
  }) => {
    await page.goto("/")

    await expect(page.getByText("e2e_tests DigitalService")).toBeVisible()
  })

  test("should not be able to access with invalid session and redirect to login", async ({
    page,
    documentNumber,
  }) => {
    await page.goto("/")
    await expect(
      page.getByRole("button", {
        name: "Neue Dokumentationseinheit",
        exact: true,
      }),
    ).toBeVisible()

    const cookies = await page.context().cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
    await page.context().clearCookies()
    await page.context().addCookies(csrfToken ? [csrfToken] : [])

    await page.goto(`/caselaw/documentunit/${documentNumber}/categories`)
    await expect(page.getByText("Spruchkörper")).toBeHidden()
    await expect(page.getByText("E-Mailadresse")).toBeVisible()
  })

  test("should get new session ID without new login", async ({
    page,
    documentNumber,
  }) => {
    await page.goto("/")
    await expect(
      page.getByRole("button", {
        name: "Neue Dokumentationseinheit",
        exact: true,
      }),
    ).toBeVisible()

    const cookies = await page.context().cookies()
    const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")
    await page.context().clearCookies()
    await page.context().addCookies(csrfToken ? [csrfToken] : [])

    await page.goto(`/caselaw/documentunit/${documentNumber}/categories`)
    await expect(page.getByText("Spruchkörper")).toBeHidden()
    await expect(page.getByText("E-Mailadresse")).toBeVisible()
  })

  test("should remember location after new login", async ({
    page,
    documentNumber,
  }) => {
    await page.goto("/")
    await expect(
      page.getByRole("button", {
        name: "Neue Dokumentationseinheit",
        exact: true,
      }),
    ).toBeVisible()
    const validCookies = await page.context().cookies()

    await page.context().clearCookies()
    await page.goto(`/caselaw/documentunit/${documentNumber}/categories`)

    // expect to be on login page
    await expect(page.getByText("Spruchkörper")).toBeHidden()
    await expect(page.getByText("E-Mailadresse")).toBeVisible()

    // login
    await page.context().addCookies(validCookies)
    await page.goto("/")

    await expect(page).toHaveURL(
      `/caselaw/documentunit/${documentNumber}/categories`,
    )
  })

  test("should see a custom error page for unknown paths", async ({ page }) => {
    await page.goto("/doesNotExists")

    await expect(
      page.getByText("Diese Internetseite existiert nicht"),
    ).toBeVisible()
  })
})
