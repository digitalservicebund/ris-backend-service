import { expect } from "@playwright/test"
import {
  fillProceedingDecisionInputs,
  navigateToCategories,
  navigateToFiles,
  navigateToPublication,
  toggleNormsSection,
  toggleProceedingDecisionsSection,
  waitForSaving,
  uploadTestfile,
} from "../e2e-utils"
import { testWithDocumentUnit as test } from "../fixtures"

test.describe("ensuring the publishing of documentunits works as expected", () => {
  test("publication page shows all possible missing required fields when no fields filled", async ({
    page,
    documentNumber,
  }) => {
    await navigateToPublication(page, documentNumber)

    await expect(page.locator("li:has-text('Aktenzeichen')")).toBeVisible()
    await expect(page.locator("li:has-text('Gericht')")).toBeVisible()
    await expect(
      page.locator("li:has-text('Entscheidungsdatum')")
    ).toBeVisible()
    await expect(page.locator("li:has-text('Dokumenttyp')")).toBeVisible()
  })

  test("publication page shows missing required fields of proceeding decisions", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await toggleProceedingDecisionsSection(page)

    await fillProceedingDecisionInputs(page, {
      court: "AG Aalen",
    })

    await page.getByText("Manuell Hinzufügen").click()

    await expect(
      page.getByText(`AG Aalen`, {
        exact: true,
      })
    ).toBeVisible()
    await navigateToPublication(page, documentNumber)

    await expect(page.locator("li:has-text('Rechtszug')")).toBeVisible()

    await expect(
      page.locator("li:has-text('Rechtszug')").getByText("Entscheidungsdatum")
    ).toBeVisible()
    await expect(
      page.locator("li:has-text('Rechtszug')").getByText("Aktenzeichen")
    ).toBeVisible()
  })

  test("publication page updates missing required fields after fields were updated", async ({
    page,
    documentNumber,
  }) => {
    await navigateToPublication(page, documentNumber)
    await expect(page.locator("li:has-text('Aktenzeichen')")).toBeVisible()
    await expect(page.locator("li:has-text('Gericht')")).toBeVisible()
    await expect(
      page.locator("li:has-text('Entscheidungsdatum')")
    ).toBeVisible()
    await expect(page.locator("li:has-text('Dokumenttyp')")).toBeVisible()
    await page.locator("[aria-label='Rubriken bearbeiten']").click()

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Aktenzeichen']").fill("abc")
        await page.keyboard.press("Enter")

        await page.locator("[aria-label='Gericht']").fill("aalen")
        await page.locator("text=AG Aalen").click()
      },
      page,
      { clickSaveButton: true }
    )

    expect(await page.inputValue("[aria-label='Gericht']")).toBe("AG Aalen")

    await navigateToPublication(page, documentNumber)
    await expect(page.locator("li:has-text('Aktenzeichen')")).toBeHidden()
    await expect(page.locator("li:has-text('Gericht')")).toBeHidden()
    await expect(
      page.locator("li:has-text('Entscheidungsdatum')")
    ).toBeVisible()
    await expect(page.locator("li:has-text('Dokumenttyp')")).toBeVisible()
  })

  test("publication not possible if required fields missing", async ({
    page,
    documentNumber,
  }) => {
    await navigateToPublication(page, documentNumber)

    await page
      .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
      .click()
    await expect(
      page.locator("text=Es sind noch nicht alle Pflichtfelder befüllt.")
    ).toBeVisible()

    await expect(page.locator("text=unveröffentlicht")).toBeVisible()
  })

  test("publication possible when all required fields filled", async ({
    page,
    documentNumber,
  }) => {
    await navigateToPublication(page, documentNumber)
    await page.locator("[aria-label='Rubriken bearbeiten']").click()

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Aktenzeichen']").fill("abc")
        await page.keyboard.press("Enter")
        await expect(page.getByText("abc").first()).toBeVisible()
      },
      page,
      { clickSaveButton: true }
    )

    await waitForSaving(
      async () => {
        await page
          .locator("[aria-label='Entscheidungsdatum']")
          .fill("03.02.2022")
        expect(
          await page.locator("[aria-label='Entscheidungsdatum']").inputValue()
        ).toBe("03.02.2022")
        await page.keyboard.press("Tab")
      },
      page,
      { clickSaveButton: true, reload: true }
    )

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Gericht']").fill("vgh mannheim")
        await page.locator("text=VGH Mannheim").click()
        expect(await page.inputValue("[aria-label='Gericht']")).toBe(
          "VGH Mannheim"
        )
      },
      page,
      { clickSaveButton: true, reload: true }
    )

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Dokumenttyp']").fill("AnU")
        await page.locator("text=Anerkenntnisurteil").click()
      },
      page,
      { clickSaveButton: true, reload: true }
    )

    await waitForSaving(
      async () => {
        await page
          .locator("[aria-label='Rechtskraft'] + button.input-expand-icon")
          .click()
        await page.getByText("Ja", { exact: true }).click()
      },
      page,
      { clickSaveButton: true, reload: true }
    )

    await navigateToPublication(page, documentNumber)

    await expect(
      page.locator("text=Alle Pflichtfelder sind korrekt ausgefüllt")
    ).toBeVisible()

    await expect(
      page.locator(
        "text=Diese Dokumentationseinheit wurde bisher nicht veröffentlicht"
      )
    ).toBeVisible()

    await page
      .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
      .click()

    await expect(page.locator("text=Email wurde versendet")).toBeVisible()

    await expect(page.locator("text=Letzte Veröffentlichung am")).toBeVisible()

    await expect(page.locator("text=veröffentlicht")).toBeVisible()
  })

  test("publication not possible when required norm abbreviation missing", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await toggleNormsSection(page)
    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Einzelnorm']").fill("abc")
      },
      page,
      { clickSaveButton: true }
    )

    await navigateToPublication(page, documentNumber)

    await expect(
      page.locator(
        "text=Die folgenden Rubriken-Pflichtfelder sind nicht befüllt:"
      )
    ).toBeVisible()

    await expect(page.locator("text=Normen abc - RIS-Abkürzung")).toBeVisible()
  })

  test("expect read access from a user of a different documentationOffice to be restricted", async ({
    documentNumber,
    browser,
  }) => {
    const bghContext = await browser.newContext({
      storageState: `test/e2e/shared/.auth/user_bgh.json`,
    })
    const bghPage = await bghContext.newPage()

    await bghPage.goto(`/caselaw/documentunit/${documentNumber}/categories`)
    await expect(
      bghPage.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung"
      )
    ).toBeVisible()

    await bghPage.goto(`/caselaw/documentunit/${documentNumber}/files`)
    await expect(
      bghPage.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung"
      )
    ).toBeVisible()

    await bghPage.goto(`/caselaw/documentunit/${documentNumber}/publication`)
    await expect(
      bghPage.locator(
        "text=Diese Dokumentationseinheit existiert nicht oder sie haben keine Berechtigung"
      )
    ).toBeVisible()

    bghContext.close()
  })

  test("expect write access from a user of a different documentationOffice to be restricted for a published documentunit", async ({
    page,
    documentNumber,
    bghPage,
  }) => {
    await test.step("fill all data to be able to publish", async () => {
      await navigateToCategories(page, documentNumber)
      await waitForSaving(
        async () => {
          await page.locator("[aria-label='Aktenzeichen']").fill("abc")
          await page.keyboard.press("Enter")
          await page
            .locator("[aria-label='Entscheidungsdatum']")
            .fill("03.02.2022")
          await page.keyboard.press("Tab")
          await page.locator("[aria-label='Gericht']").fill("vgh mannheim")
          await page.locator("text=VGH Mannheim").click()
          await page
            .locator("[aria-label='Rechtskraft'] + button.input-expand-icon")
            .click()
          await page.getByText("Ja", { exact: true }).click()
          await page.locator("[aria-label='Dokumenttyp']").fill("AnU")
          await page.locator("text=Anerkenntnisurteil").click()
        },
        page,
        { clickSaveButton: true, reload: true }
      )
    })

    await test.step("publish as authorized user", async () => {
      await navigateToPublication(page, documentNumber)
      await page
        .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
        .click()
      await expect(page.locator("text=Email wurde versendet")).toBeVisible()
    })

    await test.step("attempt to edit categories as unauthorized user", async () => {
      await navigateToCategories(bghPage, documentNumber)
      await bghPage
        .locator("[aria-label='Entscheidungsdatum']")
        .fill("03.01.2022")
      await bghPage.keyboard.press("Tab")
      await bghPage.locator("[aria-label='Speichern Button']").click()

      // saving should be forbidden
      await expect(
        bghPage.locator("text=Fehler beim Speichern: Keine Berechtigung")
      ).toBeVisible()

      // expect the old date
      await bghPage.reload()
      expect(
        await bghPage.locator("[aria-label='Entscheidungsdatum']").inputValue()
      ).toBe("03.02.2022")
    })

    await test.step("attempt to upload a file as unauthorized user", async () => {
      await navigateToFiles(bghPage, documentNumber)
      await uploadTestfile(bghPage, "sample.docx")
      await expect(
        bghPage.locator("text=Leider ist ein Fehler aufgetreten.")
      ).toBeVisible()
    })

    await test.step("attempt to publish as unauthorized user", async () => {
      await navigateToPublication(bghPage, documentNumber)
      await bghPage
        .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
        .click()
      await expect(
        bghPage.locator("text=Leider ist ein Fehler aufgetreten.")
      ).toBeVisible()
    })
  })
})
