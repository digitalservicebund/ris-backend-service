import { expect } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

test.describe("category import", () => {
  test("display category import", async ({ page, prefilledDocumentUnit }) => {
    await navigateToCategories(
      page,
      prefilledDocumentUnit.documentNumber as string,
    )
    await page.getByLabel("Seitenpanel öffnen").click()
    await page.getByLabel("Rubriken-Import anzeigen").click()

    await expect(page.getByText("Rubriken importieren")).toBeVisible()
    await expect(page.getByLabel("Dokumentnummer Eingabefeld")).toBeVisible()
    await expect(
      page.getByRole("button", { name: "Dokumentationseinheit laden" }),
    ).toBeVisible()
    await expect(
      page.getByRole("button", { name: "Dokumentationseinheit laden" }),
    ).toBeDisabled()
  })

  test("search for non-existent document unit displays error", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    await navigateToCategories(
      page,
      prefilledDocumentUnit.documentNumber as string,
    )
    await page.getByLabel("Seitenpanel öffnen").click()
    await page.getByLabel("Rubriken-Import anzeigen").click()

    await expect(page.getByText("Rubriken importieren")).toBeVisible()
    await expect(page.getByLabel("Dokumentnummer Eingabefeld")).toBeVisible()
    await page.getByLabel("Dokumentnummer Eingabefeld").click()
    await page.keyboard.type("invalidnumber")
    await expect(
      page.getByRole("button", { name: "Dokumentationseinheit laden" }),
    ).toBeEnabled()
    await page
      .getByRole("button", { name: "Dokumentationseinheit laden" })
      .click()
    await expect(
      page.getByText("Keine Dokumentationseinheit gefunden."),
    ).toBeVisible()
  })

  test("search fordocument unit displays core data", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    await navigateToCategories(
      page,
      prefilledDocumentUnit.documentNumber as string,
    )
    await page.getByLabel("Seitenpanel öffnen").click()
    await page.getByLabel("Rubriken-Import anzeigen").click()

    await expect(page.getByText("Rubriken importieren")).toBeVisible()
    await expect(page.getByLabel("Dokumentnummer Eingabefeld")).toBeVisible()
    await page.getByLabel("Dokumentnummer Eingabefeld").click()
    await page.keyboard.type("invalidnumber")
    await expect(
      page.getByRole("button", { name: "Dokumentationseinheit laden" }),
    ).toBeEnabled()
    await page
      .getByRole("button", { name: "Dokumentationseinheit laden" })
      .click()
    await expect(
      page.getByText("Keine Dokumentationseinheit gefunden."),
    ).toBeVisible()
  })
})
