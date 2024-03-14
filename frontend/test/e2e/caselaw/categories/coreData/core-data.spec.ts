import { expect } from "@playwright/test"
import {
  deleteDocumentUnit,
  navigateToCategories,
  waitForSaving,
} from "../../e2e-utils"
import { caselawTest as test } from "../../fixtures"

test.describe("core data", () => {
  test("core data change", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Aktenzeichen']").fill("abc")
        await page.keyboard.press("Enter")
        await page.locator("[aria-label='ECLI']").fill("abc123")
        await page.keyboard.press("Enter")
      },
      page,
      { clickSaveButton: true },
    )

    await page.reload()
    await expect(page.locator("[aria-label='Aktenzeichen']")).toHaveValue("")
    await expect(page.locator("[aria-label='ECLI']")).toHaveValue("abc123")
  })

  test("nested 'ECLI' input toggles child input and correctly saves and displays data", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='ECLI']").type("one")
        await expect(page.locator("text=one").first()).toBeVisible()

        await expect(page.locator("text=Abweichender ECLI>")).toBeHidden()

        await page.locator("[aria-label='Abweichender ECLI anzeigen']").click()

        await expect(
          page.locator("text=Abweichender ECLI").first(),
        ).toBeVisible()

        await page.locator("[aria-label='Abweichender ECLI']").type("two")
        await page.keyboard.press("Enter")
        await page.locator("[aria-label='Abweichender ECLI']").type("three")
        await page.keyboard.press("Enter")
      },
      page,
      { clickSaveButton: true },
    )

    await page.reload()

    await page.locator("[aria-label='Abweichender ECLI anzeigen']").click()
    await expect(page.locator("text=two").first()).toBeVisible()
    await expect(page.locator("text=three").first()).toBeVisible()

    await page.locator("[aria-label='Abweichender ECLI schließen']").click()
    await expect(page.locator("text=Abweichender ECLI").first()).toBeHidden()
  })

  test("nested fileNumbers input toggles child input and correctly saves and displays data", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Aktenzeichen']").type("one")
        await page.keyboard.press("Enter")

        await page.locator("[aria-label='Aktenzeichen']").type("two")
        await page.keyboard.press("Enter")

        await expect(page.locator("text=one").first()).toBeVisible()
        await expect(page.locator("text=two").first()).toBeVisible()

        await expect(
          page.locator("text=Abweichendes Aktenzeichen>"),
        ).toBeHidden()

        await page
          .locator("[aria-label='Abweichendes Aktenzeichen anzeigen']")
          .click()

        await expect(
          page.locator("text=Abweichendes Aktenzeichen").first(),
        ).toBeVisible()

        await page
          .locator("[aria-label='Abweichendes Aktenzeichen']")
          .type("three")
        await page.keyboard.press("Enter")
      },
      page,
      { clickSaveButton: true },
    )

    await page.reload()

    await page
      .locator("[aria-label='Abweichendes Aktenzeichen anzeigen']")
      .click()
    await expect(page.locator("text=three").first()).toBeVisible()
    await page
      .locator("[aria-label='Abweichendes Aktenzeichen schließen']")
      .click()
    await expect(
      page.locator("text=Abweichendes Aktenzeichen").first(),
    ).toBeHidden()
  })

  test("adding, navigating, deleting multiple fileNumbers", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Aktenzeichen']").type("testone")
        await page.keyboard.press("Enter")

        await page.locator("[aria-label='Aktenzeichen']").type("testtwo")
        await page.keyboard.press("Enter")

        await page.locator("[aria-label='Aktenzeichen']").type("testthree")
        await page.keyboard.press("Enter")

        await expect(page.locator("text=testone").first()).toBeVisible()
        await expect(page.locator("text=testtwo").first()).toBeVisible()
        await expect(page.locator("text=testthree").first()).toBeVisible()

        // Navigate back and delete on enter
        await page.keyboard.press("ArrowLeft")
        await page.keyboard.press("ArrowLeft")
        await page.keyboard.press("Enter")

        await expect(page.locator("text=testtwo").first()).toBeHidden()

        // Tab out and in
        await page.keyboard.press("Tab")
        await page.keyboard.press("Tab")

        await page.keyboard.down("Shift")
        await page.keyboard.press("Tab")
        await page.keyboard.up("Shift")

        await page.keyboard.down("Shift")
        await page.keyboard.press("Tab")
        await page.keyboard.up("Shift")

        await page.keyboard.press("ArrowLeft")

        //Navigate back and delete on backspace
        await page.keyboard.press("Enter")

        await expect(page.locator("text=testone").first()).toBeHidden()
      },
      page,
      { clickSaveButton: true },
    )

    await page.reload()
    await expect(page.locator("text=testthree").first()).toBeVisible()
  })

  test("legal effect dropdown", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    const dropdown = page.getByRole("combobox", { name: "Rechtskraft" })
    await expect(dropdown).toHaveValue("Keine Angabe")

    await expect(dropdown.getByRole("option", { name: "Ja" })).toHaveCount(1)
    await expect(dropdown.getByRole("option", { name: "Nein" })).toHaveCount(1)
    await expect(
      dropdown.getByRole("option", { name: "Keine Angabe" }),
    ).toHaveCount(1)
  })

  test("document type dropdown", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    const totalCaselawDocumentTypes = 43

    // on start: closed dropdown, no input text
    await expect(page.locator("[aria-label='Dokumenttyp']")).toHaveValue("")
    await expect(page.locator("text=AnU - Anerkenntnisurteil")).toBeHidden()
    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()

    // open dropdown
    await page
      .locator("#coreData div")
      .filter({ hasText: "Spruchkörper Dokumenttyp" })
      .getByLabel("Dropdown öffnen")
      .click()
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(
      totalCaselawDocumentTypes,
    )
    await expect(page.locator("text=Anerkenntnisurteil")).toBeVisible()
    await expect(page.locator("text=Anhängiges Verfahren")).toBeVisible()

    // type search string: 3 results for "zwischen"
    await page.locator("[aria-label='Dokumenttyp']").fill("zwischen")
    await expect(page.locator("[aria-label='Dokumenttyp']")).toHaveValue(
      "zwischen",
    )
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(3)

    // use the clear icon
    await page.locator("[aria-label='Auswahl zurücksetzen']").click()
    await expect(page.locator("[aria-label='Dokumenttyp']")).toHaveValue("")
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(
      totalCaselawDocumentTypes,
    )

    // close dropdown
    await page.getByLabel("Dropdown schließen").click()
    await expect(page.locator("[aria-label='Dokumenttyp']")).toHaveValue("")
    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()

    // open dropdown again by focussing
    await page.locator("[aria-label='Dokumenttyp']").focus()

    // close dropdown using the esc key, user input text gets removed and last saved value restored
    await page.keyboard.down("Escape")
    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()
    await expect(page.locator("[aria-label='Dokumenttyp']")).toHaveValue("")
  })

  test("ensure new docUnit has correct documentationOffice for DS user", async ({
    page,
  }) => {
    await test.step("create new docUnit with logged in user", async () => {
      await page.goto("/caselaw")
      await page
        .getByRole("button", {
          name: "Neue Dokumentationseinheit",
          exact: true,
        })
        .click()

      await expect(
        page.getByText("Aktuell ist keine Datei hinterlegt"),
      ).toBeVisible()

      await page.getByText("Rubriken").click()
      await expect(page.getByText("DOKUMENTATIONSSTELLEDS")).toBeVisible()

      const documentNumber = page
        .url()
        .match(/documentunit\/([A-Z0-9]{13})\/files/)![1]

      await deleteDocumentUnit(page, documentNumber)
    })
  })

  test("ensure new docUnit has correct documentationOffice for BGH user", async ({
    pageWithBghUser,
  }) => {
    await test.step("create new docUnit with logged in user", async () => {
      await pageWithBghUser.goto("/caselaw")
      await pageWithBghUser
        .getByRole("button", {
          name: "Neue Dokumentationseinheit",
          exact: true,
        })
        .click()

      await expect(
        pageWithBghUser.getByText("Aktuell ist keine Datei hinterlegt"),
      ).toBeVisible()

      await pageWithBghUser.getByText("Rubriken").click()
      await expect(
        pageWithBghUser.getByText("DOKUMENTATIONSSTELLEBGH"),
      ).toBeVisible()

      const documentNumber = pageWithBghUser
        .url()
        .match(/documentunit\/([A-Z0-9]{13})\/files/)![1]

      await deleteDocumentUnit(pageWithBghUser, documentNumber)
    })
  })

  test("ensure that leadingDecisionNormReferences are visible, savable, and deletable for the BGH and not for others", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const nswInput = page.locator("[aria-label='BGH Nachschlagewerk']")
    const CITATION = "1968, 249-252 (ST)"
    const nswChipTag = page.locator(`text=${CITATION}`)
    await expect(nswInput).toBeHidden()

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Gericht']").fill("BGH")
        await page.locator("text=BGH").click()
        await expect(nswInput).toBeVisible()
        await nswInput.fill(CITATION)
        await page.keyboard.press("Enter")
      },
      page,
      { clickSaveButton: true },
    )
    await expect(
      nswChipTag,
      "NSW Fundstelle is not visible for BGH",
    ).toBeVisible()

    await waitForSaving(
      async () => {
        await page
          .getByTestId("chips-input_leadingDecisionNormReferences")
          .getByLabel("Löschen")
          .click()
      },
      page,
      { clickSaveButton: true },
    )
    await expect(nswChipTag, "Citation was not deleted").toBeHidden()

    await page.locator("[aria-label='Gericht']").fill("AG Aalen")
    await page.locator("text=AG Aalen").click()
    await expect(
      nswInput,
      "NSW Fundstelle is visible for other courts",
    ).toBeHidden()
  })
})
