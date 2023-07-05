import { expect } from "@playwright/test"
import {
  navigateToCategories,
  navigateToFiles,
  navigateToPublication,
  uploadTestfile,
} from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

test.describe("ensuring the publishing of documentunits works as expected", () => {
  test("expect read access from a user of a different documentationOffice to be restricted", async ({
    documentNumber,
    pageWithBghUser,
  }) => {
    await pageWithBghUser.goto(
      `/caselaw/documentunit/${documentNumber}/categories`
    )
    await expect(
      pageWithBghUser.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung"
      )
    ).toBeVisible()

    await pageWithBghUser.goto(`/caselaw/documentunit/${documentNumber}/files`)
    await expect(
      pageWithBghUser.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung"
      )
    ).toBeVisible()

    await pageWithBghUser.goto(
      `/caselaw/documentunit/${documentNumber}/publication`
    )
    await expect(
      pageWithBghUser.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung"
      )
    ).toBeVisible()
  })

  test("expect write access from a user of a different documentationOffice to be restricted for a published documentunit", async ({
    page,
    prefilledDocumentUnit,
    pageWithBghUser,
  }) => {
    await test.step("publish as authorized user", async () => {
      await navigateToPublication(
        page,
        prefilledDocumentUnit.documentNumber as string
      )
      await page
        .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
        .click()
      await expect(page.locator("text=Email wurde versendet")).toBeVisible()
    })

    await test.step("attempt to edit categories as unauthorized user", async () => {
      await navigateToCategories(
        pageWithBghUser,
        prefilledDocumentUnit.documentNumber as string
      )
      await pageWithBghUser
        .locator("[aria-label='Entscheidungsdatum']")
        .fill("03.01.2022")
      await pageWithBghUser.keyboard.press("Tab")
      await pageWithBghUser.locator("[aria-label='Speichern Button']").click()

      // saving should be forbidden
      await expect(
        pageWithBghUser.locator(
          "text=Fehler beim Speichern: Keine Berechtigung"
        )
      ).toBeVisible()

      // expect the old date
      await pageWithBghUser.reload()
      expect(
        await pageWithBghUser
          .locator("[aria-label='Entscheidungsdatum']")
          .inputValue()
      ).toBe("01.01.2020")
    })

    await test.step("attempt to upload a file as unauthorized user", async () => {
      await navigateToFiles(
        pageWithBghUser,
        prefilledDocumentUnit.documentNumber as string
      )
      await uploadTestfile(pageWithBghUser, "sample.docx")
      await expect(
        pageWithBghUser.locator("text=Leider ist ein Fehler aufgetreten.")
      ).toBeVisible()
    })

    await test.step("attempt to publish as unauthorized user", async () => {
      await navigateToPublication(
        pageWithBghUser,
        prefilledDocumentUnit.documentNumber as string
      )
      await pageWithBghUser
        .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
        .click()
      await expect(
        pageWithBghUser.locator("text=Leider ist ein Fehler aufgetreten.")
      ).toBeVisible()
    })
  })
})
