import { expect } from "@playwright/test"
import {
  deleteDocumentUnit,
  navigateToCategories,
  navigateToPreview,
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
        await expect(page.getByText("one").first()).toBeVisible()

        await expect(page.getByText("Abweichender ECLI>")).toBeHidden()

        await page.locator("[aria-label='Abweichender ECLI anzeigen']").click()

        await expect(page.getByText("Abweichender ECLI").first()).toBeVisible()

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
    await expect(page.getByText("two").first()).toBeVisible()
    await expect(page.getByText("three").first()).toBeVisible()

    await page.locator("[aria-label='Abweichender ECLI schließen']").click()
    await expect(page.getByText("Abweichender ECLI").first()).toBeHidden()
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

        await expect(page.getByText("one").first()).toBeVisible()
        await expect(page.getByText("two").first()).toBeVisible()

        await expect(page.getByText("Abweichendes Aktenzeichen>")).toBeHidden()

        await page
          .locator("[aria-label='Abweichendes Aktenzeichen anzeigen']")
          .click()

        await expect(
          page.getByText("Abweichendes Aktenzeichen").first(),
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
    await expect(page.getByText("three").first()).toBeVisible()
    await page
      .locator("[aria-label='Abweichendes Aktenzeichen schließen']")
      .click()
    await expect(
      page.getByText("Abweichendes Aktenzeichen").first(),
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

        await expect(page.getByText("testone").first()).toBeVisible()
        await expect(page.getByText("testtwo").first()).toBeVisible()
        await expect(page.getByText("testthree").first()).toBeVisible()

        // Navigate back and delete on enter
        await page.keyboard.press("ArrowLeft")
        await page.keyboard.press("ArrowLeft")
        await page.keyboard.press("Enter")

        await expect(page.getByText("testtwo").first()).toBeHidden()

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

        await expect(page.getByText("testone").first()).toBeHidden()
      },
      page,
      { clickSaveButton: true },
    )

    await page.reload()
    await expect(page.getByText("testthree").first()).toBeVisible()
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

    // on start: closed dropdown, no input text
    await expect(page.locator("[aria-label='Dokumenttyp']")).toHaveValue("")
    await expect(page.getByText("AnU - Anerkenntnisurteil")).toBeHidden()
    await expect(page.locator("[aria-label='dropdown-option']")).toBeHidden()

    // open dropdown
    await page
      .locator("#coreData div")
      .filter({ hasText: "Spruchkörper Dokumenttyp" })
      .getByLabel("Dropdown öffnen")
      .click()
    await expect(
      page.locator("[aria-label='dropdown-option']"),
    ).not.toHaveCount(0)
    await expect(page.getByText("Anerkenntnisurteil")).toBeVisible()
    await expect(page.getByText("Anhängiges Verfahren")).toBeVisible()

    // type search string: 3 results for "zwischen"
    await page.locator("[aria-label='Dokumenttyp']").fill("zwischen")
    await expect(page.locator("[aria-label='Dokumenttyp']")).toHaveValue(
      "zwischen",
    )
    await expect(page.locator("[aria-label='dropdown-option']")).toHaveCount(3)

    // use the clear icon
    await page.locator("[aria-label='Auswahl zurücksetzen']").click()
    await expect(page.locator("[aria-label='Dokumenttyp']")).toHaveValue("")
    await expect(
      page.locator("[aria-label='dropdown-option']"),
    ).not.toHaveCount(0)

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
      const tableView = page.getByRole("cell", {
        name: "Dateiname",
        exact: true,
      })

      await page
        .getByRole("button", {
          name: "Neue Dokumentationseinheit",
          exact: true,
        })
        .click()

      await expect(tableView).toBeHidden()

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

      const tableView = pageWithBghUser.getByRole("cell", {
        name: "Dateiname",
        exact: true,
      })

      await expect(tableView).toBeHidden()

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
    const nswChipTag = page.getByText(CITATION)
    await expect(nswInput).toBeHidden()

    await waitForSaving(
      async () => {
        await page.locator("[aria-label='Gericht']").fill("BGH")
        await page.getByText("BGH").click()
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
    await page.getByText("AG Aalen").click()
    await expect(
      nswInput,
      "NSW Fundstelle is visible for other courts",
    ).toBeHidden()
  })

  //RISDEV-3918
  test.describe("years of dispute", () => {
    //RISDEV-4197
    test("adding, navigating, deleting multiple years of dispute", async ({
      page,
      documentNumber,
    }) => {
      await navigateToCategories(page, documentNumber)

      await waitForSaving(
        async () => {
          await page.locator("[aria-label='Streitjahr']").fill("2020")
          await page.keyboard.press("Enter")
          await page.locator("[aria-label='Streitjahr']").fill("2021")
          await page.keyboard.press("Enter")

          await page.locator("[aria-label='Streitjahr']").fill("2022")
          await page.keyboard.press("Enter")

          await expect(page.getByText("2020")).toBeVisible()
          await expect(page.getByText("2021")).toBeVisible()
          await expect(page.getByText("2022")).toBeVisible()

          // Navigate back and delete on enter
          await page.keyboard.press("ArrowLeft")
          await page.keyboard.press("Enter")

          await expect(page.getByText("2022")).toBeHidden()

          // Tab out and in
          await page.keyboard.press("Tab")
          await page.keyboard.press("Tab")
          await page.keyboard.down("Shift")
          await page.keyboard.press("Tab")

          await page.keyboard.press("ArrowLeft")

          //Navigate back and delete on backspace
          await page.keyboard.press("Enter")

          await expect(page.getByText("2021")).toBeHidden()
        },
        page,
        { clickSaveButton: true },
      )

      await page.reload()
      await expect(page.getByText("2020")).toBeVisible()
    })

    //RISDEV-4198
    test("years of dispute visible in preview", async ({
      page,
      documentNumber,
    }) => {
      await navigateToCategories(page, documentNumber)

      await waitForSaving(
        async () => {
          await page.locator("[aria-label='Streitjahr']").fill("2020")
          await page.keyboard.press("Enter")
          await page.locator("[aria-label='Streitjahr']").fill("2021")
          await page.keyboard.press("Enter")

          await expect(page.getByText("2020")).toBeVisible()
          await expect(page.getByText("2021")).toBeVisible()
        },
        page,
        { clickSaveButton: true },
      )

      await navigateToPreview(page, documentNumber)
      await expect(page.getByText("2020")).toBeVisible()
      await expect(page.getByText("2021")).toBeVisible()
    })

    //RISDEV-4199
    test("years of dispute are exported", async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)

      await waitForSaving(
        async () => {
          await page.locator("[aria-label='Streitjahr']").fill("2020")
          await page.keyboard.press("Enter")
          await page.locator("[aria-label='Streitjahr']").fill("2021")
          await page.keyboard.press("Enter")

          await expect(page.getByText("2020")).toBeVisible()
          await expect(page.getByText("2021")).toBeVisible()
        },
        page,
        { clickSaveButton: true },
      )
      await expect(page.getByText("2020")).toBeVisible()
      //Todo: test that year of dispute visible in xml preview
    })

    //RISDEV-4200
    test("validating years of dispute input", async ({
      page,
      documentNumber,
    }) => {
      await navigateToCategories(page, documentNumber)

      await waitForSaving(
        async () => {
          //Only unique values
          await page.locator("[aria-label='Streitjahr']").fill("2022")
          await page.keyboard.press("Enter")
          await expect(page.getByText("2022")).toBeVisible()

          await page.locator("[aria-label='Streitjahr']").fill("2022")
          await page.keyboard.press("Enter")
          await expect(page.getByText("2022 bereits vorhanden")).toBeVisible()

          //Only valid year
          await page.locator("[aria-label='Streitjahr']").fill("999")
          await page.keyboard.press("Enter")
          await expect(page.getByText("2022 bereits vorhanden")).toBeHidden()
          await expect(page.getByText("Kein valides Jahr")).toBeVisible()

          //Only years in past
          await page.locator("[aria-label='Streitjahr']").fill("2030")
          await page.keyboard.press("Enter")
          await expect(page.getByText("Kein valides Jahr")).toBeHidden()
          await expect(
            page.getByText("Streitjahr darf nicht in der Zukunft liegen"),
          ).toBeVisible()

          //On blur
          await page.locator("[aria-label='Streitjahr']").fill("20")
          await page.keyboard.press("Tab")
          await expect(
            page.getByText("Streitjahr darf nicht in der Zukunft liegen"),
          ).toBeHidden()
          await expect(page.getByText("Kein valides Jahr")).toBeVisible()
        },
        page,
        { clickSaveButton: true },
      )

      await page.reload()
      await expect(page.getByText("2022")).toBeVisible()
    })
  })
})
