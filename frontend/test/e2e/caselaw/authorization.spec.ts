import { expect } from "@playwright/test"
import {
  navigateToCategories,
  navigateToFiles,
  navigateToPublication,
  publishDocumentationUnit,
  uploadTestfile,
  waitForSaving,
} from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

test.describe("ensuring the publishing of documentunits works as expected", () => {
  test("expect read access from a user of a different documentationOffice to be restricted", async ({
    documentNumber,
    pageWithBghUser,
  }) => {
    await pageWithBghUser.goto(
      `/caselaw/documentunit/${documentNumber}/categories`,
    )
    await expect(
      pageWithBghUser.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung",
      ),
    ).toBeVisible()

    await pageWithBghUser.goto(`/caselaw/documentunit/${documentNumber}/files`)
    await expect(
      pageWithBghUser.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung",
      ),
    ).toBeVisible()

    await pageWithBghUser.goto(
      `/caselaw/documentunit/${documentNumber}/publication`,
    )
    await expect(
      pageWithBghUser.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung",
      ),
    ).toBeVisible()
  })

  // We can't test this as NeuRIS doesn't publish documentUnits anymore
  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("expect write access from a user of a different documentationOffice to be restricted for a published documentunit", async ({
    page,
    prefilledDocumentUnit,
    pageWithBghUser,
  }) => {
    await test.step("publish as authorized user", async () => {
      await publishDocumentationUnit(
        page,
        prefilledDocumentUnit.documentNumber as string,
      )
    })

    await test.step("attempt to edit categories as unauthorized user", async () => {
      await navigateToCategories(
        pageWithBghUser,
        prefilledDocumentUnit.documentNumber as string,
      )

      await waitForSaving(
        async () => {
          await pageWithBghUser
            .locator("[aria-label='Entscheidungsdatum']")
            .fill("03.01.2022")
        },
        pageWithBghUser,
        {
          clickSaveButton: true,
          error: "Fehler beim Speichern: Keine Berechtigung",
        },
      )

      // expect the old date
      await pageWithBghUser.reload()
      await expect(
        pageWithBghUser.locator("[aria-label='Entscheidungsdatum']"),
      ).toHaveValue("31.12.2019")
    })

    await test.step("attempt to upload a file as unauthorized user", async () => {
      await navigateToFiles(
        pageWithBghUser,
        prefilledDocumentUnit.documentNumber as string,
      )
      await uploadTestfile(pageWithBghUser, "sample.docx")
      await expect(
        pageWithBghUser.getByText("Leider ist ein Fehler aufgetreten."),
      ).toBeVisible()
    })

    await test.step("attempt to publish as unauthorized user", async () => {
      await navigateToPublication(
        pageWithBghUser,
        prefilledDocumentUnit.documentNumber as string,
      )
      await pageWithBghUser
        .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
        .click()
      await expect(
        pageWithBghUser.getByText("Leider ist ein Fehler aufgetreten."),
      ).toBeVisible()
    })
  })
})
