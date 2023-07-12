import { expect } from "@playwright/test"
import {
  fillActiveCitationInputs,
  fillProceedingDecisionInputs,
  navigateToCategories,
  navigateToPublication,
  toggleProceedingDecisionsSection,
  waitForSaving,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"

test.describe("ensuring the publishing of documentunits works as expected", () => {
  test("publication page shows all possible missing required fields when no fields filled", async ({
    page,
    documentNumber,
  }) => {
    await navigateToPublication(page, documentNumber)

    await expect(page.locator("li:has-text('Aktenzeichen')")).toBeVisible()
    await expect(page.locator("li:has-text('Gericht')")).toBeVisible()
    await expect(
      page.locator("li:has-text('Entscheidungsdatum')"),
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
      }),
    ).toBeVisible()
    await navigateToPublication(page, documentNumber)

    await expect(page.locator("li:has-text('Rechtszug')")).toBeVisible()

    await expect(
      page.locator("li:has-text('Rechtszug')").getByText("Entscheidungsdatum"),
    ).toBeVisible()
    await expect(
      page.locator("li:has-text('Rechtszug')").getByText("Aktenzeichen"),
    ).toBeVisible()
  })

  test("publication page shows missing required fields of norms", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Norm Einzelnorm']").fill("abc")
      },
      page,
      { clickSaveButton: true },
    )

    await navigateToPublication(page, documentNumber)

    await expect(page.locator("li:has-text('Normen')")).toBeVisible()

    await expect(
      page.locator("li:has-text('Normen')").getByText("RIS-Abkürzung"),
    ).toBeVisible()
  })

  test("publication page shows missing required fields of active citations", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await navigateToCategories(page, documentNumber)
    await waitForSaving(
      async () => {
        await fillActiveCitationInputs(page, {
          documentType:
            prefilledDocumentUnit.coreData.documentType?.jurisShortcut,
        })
        await page.getByLabel("Aktivzitierung speichern").click()
      },
      page,
      { clickSaveButton: true },
    )

    await navigateToPublication(page, documentNumber)

    await expect(page.locator("li:has-text('Aktivzitierung')")).toBeVisible()

    await expect(
      page
        .locator("li:has-text('Aktivzitierung')")
        .getByText("Art der Zitierung"),
    ).toBeVisible()
    await expect(
      page.locator("li:has-text('Aktivzitierung')").getByText("Gericht"),
    ).toBeVisible()
    await expect(
      page
        .locator("li:has-text('Aktivzitierung')")
        .getByText("Entscheidungsdatum"),
    ).toBeVisible()
    await expect(
      page.locator("li:has-text('Aktivzitierung')").getByText("Aktenzeichen"),
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
      page.locator("li:has-text('Entscheidungsdatum')"),
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
      { clickSaveButton: true },
    )

    expect(await page.inputValue("[aria-label='Gericht']")).toBe("AG Aalen")

    await navigateToPublication(page, documentNumber)
    await expect(page.locator("li:has-text('Aktenzeichen')")).toBeHidden()
    await expect(page.locator("li:has-text('Gericht')")).toBeHidden()
    await expect(
      page.locator("li:has-text('Entscheidungsdatum')"),
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
      page.locator("text=Es sind noch nicht alle Pflichtfelder befüllt."),
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
      { clickSaveButton: true },
    )

    await waitForSaving(
      async () => {
        await page
          .locator("[aria-label='Entscheidungsdatum']")
          .fill("03.02.2022")
        expect(
          await page.locator("[aria-label='Entscheidungsdatum']").inputValue(),
        ).toBe("03.02.2022")
        await page.keyboard.press("Tab")
      },
      page,
      { clickSaveButton: true, reload: true },
    )

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Gericht']").fill("vgh mannheim")
        await page.locator("text=VGH Mannheim").click()
        expect(await page.inputValue("[aria-label='Gericht']")).toBe(
          "VGH Mannheim",
        )
      },
      page,
      { clickSaveButton: true, reload: true },
    )

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Dokumenttyp']").fill("AnU")
        await page.locator("text=Anerkenntnisurteil").click()
      },
      page,
      { clickSaveButton: true, reload: true },
    )

    await waitForSaving(
      async () => {
        await page
          .locator("[aria-label='Rechtskraft'] + button.input-expand-icon")
          .click()
        await page.getByText("Ja", { exact: true }).click()
      },
      page,
      { clickSaveButton: true, reload: true },
    )

    await navigateToPublication(page, documentNumber)

    await expect(
      page.locator("text=Alle Pflichtfelder sind korrekt ausgefüllt"),
    ).toBeVisible()

    await expect(
      page.locator(
        "text=Diese Dokumentationseinheit wurde bisher nicht veröffentlicht",
      ),
    ).toBeVisible()

    await page
      .locator("[aria-label='Dokumentationseinheit veröffentlichen']")
      .click()

    await expect(page.locator("text=Email wurde versendet")).toBeVisible()

    await expect(page.locator("text=Xml Email Abgabe -")).toBeVisible()

    await expect(page.locator("text=in Veröffentlichung")).toBeVisible()
  })
})
