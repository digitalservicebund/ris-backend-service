import { expect, Page } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillCombobox,
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe("court", () => {
  test("input value in court field, press enter and reload", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.getByLabel("Gericht", { exact: true }).fill("BGH")
    await expect(page.getByTestId("combobox-spinner")).toBeHidden()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue("BGH")
    await expect(page.getByText("BGH")).toBeVisible()
    await page.getByText("BGH").click()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue("BGH")

    await save(page)

    await page.reload()
    await page.getByLabel("Gericht", { exact: true }).focus()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue("BGH")
  })

  test("open deviating court field, input one, save and reload", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page
      .getByLabel("Fehlerhaftes Gericht anzeigen", { exact: true })
      .click()

    await page
      .getByLabel("Fehlerhaftes Gericht")
      .getByRole("textbox")
      .fill("abc")

    await expect(
      page.getByText("Fehlerhaftes Gericht", { exact: true }),
    ).toHaveValue("abc")

    await page.keyboard.press("Enter")
    await save(page)

    await page.reload()

    // If deviating data is available, it is automatically expanded
    await expect(page.getByText("abc").first()).toBeVisible()
  })

  test("open deviating court field, input two, save, reload, remove first, save and reload", async ({
    page,
    documentNumber,
  }) => {
    test.slow()
    await navigateToCategories(page, documentNumber)

    await page
      .getByLabel("Fehlerhaftes Gericht anzeigen", { exact: true })
      .click()
    await page
      .getByText("Fehlerhaftes Gericht", { exact: true })
      .fill("incorrectCourt1")
    await page.keyboard.press("Enter")
    await page
      .getByText("Fehlerhaftes Gericht", { exact: true })
      .fill("incorrectCourt2")
    await page.keyboard.press("Enter")

    await save(page)
    await page.reload()

    // If deviating data is available, it is automatically expanded
    await expect(page.getByText("IncorrectCourt1")).toBeVisible()
    await expect(page.getByText("IncorrectCourt2")).toBeVisible()

    await page
      .getByRole("listitem", { name: "incorrectCourt1" })
      .getByLabel("Eintrag löschen")
      .click()

    await save(page)
    await page.reload()

    // If deviating data is available, it is automatically expanded
    await expect(page.getByText("IncorrectCourt1")).toHaveCount(0)
    await expect(page.getByText("IncorrectCourt2")).toBeVisible()
  })

  test("court dropdown", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    // on start: closed dropdown, no input text
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue("")
    await expect(page.getByText("AG Aachen")).toBeHidden()
    await expect(page.getByLabel("dropdown-option")).toBeHidden()

    // open dropdown
    await page
      .locator("#coreData div")
      .filter({ hasText: "Gericht * Fehlerhaftes" })
      .getByLabel("Dropdown öffnen")
      .click()
    await expect(page.getByLabel("dropdown-option")).toHaveCount(200)
    await expect(page.getByText("AG Aachen")).toBeVisible()
    await expect(page.getByText("AG Aalen")).toBeVisible()

    // type search string: 3 results for "bayern"
    await page.getByLabel("Gericht", { exact: true }).fill("bayern")
    await expect(page.getByTestId("combobox-spinner")).toBeHidden()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
      "bayern",
    )
    await expect(page.getByLabel("dropdown-option")).toHaveCount(3)

    // use the clear icon
    await page.getByLabel("Auswahl zurücksetzen", { exact: true }).click()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue("")
    await expect(page.getByLabel("dropdown-option")).toHaveCount(200)

    // close dropdown
    await page.getByLabel("Dropdown schließen").click()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue("")
    await expect(page.getByLabel("dropdown-option")).toBeHidden()

    // open dropdown again by focussing
    await page.getByLabel("Gericht", { exact: true }).focus()
    await expect(page.getByLabel("dropdown-option")).toHaveCount(200)

    // close dropdown using the esc key, user input text gets removed and last saved value restored
    await page.keyboard.down("Escape")
    await expect(page.getByLabel("dropdown-option")).toBeHidden()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue("")
  })

  test("correct esc/tab behaviour in court dropdown", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue("")
    await expect(page.getByLabel("dropdown-option")).toBeHidden()

    await page.getByLabel("Gericht", { exact: true }).fill("BVerfG")
    await expect(page.getByTestId("combobox-spinner")).toBeHidden()
    await page.getByText("BVerfG").click()

    await expect(page.getByLabel("dropdown-option")).toBeHidden()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
      "BVerfG",
    )

    await page.getByLabel("Gericht", { exact: true }).fill("BGH")

    await expect(page.getByLabel("dropdown-option")).toHaveCount(1)

    await page.keyboard.press("Escape") // reset to last saved value

    await expect(page.getByLabel("dropdown-option")).toBeHidden()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
      "BVerfG",
    )

    await page.getByLabel("Gericht", { exact: true }).fill("BGH")
    await page.keyboard.press("Tab") // reset to last saved value

    await expect(page.getByLabel("dropdown-option")).toBeHidden()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
      "BVerfG",
    )
  })

  test("that setting a court sets the region automatically", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    await page.getByLabel("Gericht", { exact: true }).fill("aalen")
    await expect(page.getByTestId("combobox-spinner")).toBeHidden()
    await page.getByText("AG Aalen").click()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
      "AG Aalen",
    )

    await save(page)

    await expect(page.getByText("Region")).toBeVisible()

    // region was set by the backend based on state database table
    await expect(page.getByLabel("Region", { exact: true })).toHaveValue("BW")
    await page.reload()
    // clear the court
    await page.getByLabel("Auswahl zurücksetzen", { exact: true }).click()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue("")

    await save(page)

    await expect(page.getByText("Region")).toBeVisible()
    // region was cleared by the backend
    await expect(page.getByLabel("Region", { exact: true })).toHaveValue("")
  })

  test(
    "setting a court with multiple regions displays all regions",
    { tag: ["@RISDEV-3081"] },
    async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)

      await fillCombobox(page, "Gericht", "LSG Celle-Bremen")

      await save(page)

      await expect(page.getByText("Region")).toBeVisible()

      // region was set by the backend based on state database table
      await expect(page.getByLabel("Region", { exact: true })).toHaveValue(
        "BR, ND",
      )
    },
  )

  test("that setting a special court sets legal effect to yes, but it can be changed afterwards", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const dropdown = page.getByRole("combobox", { name: "Rechtskraft" })
    await expect(dropdown).toHaveText("Keine Angabe")

    await page.getByLabel("Gericht", { exact: true }).fill("bgh")
    await expect(page.getByTestId("combobox-spinner")).toBeHidden()
    await page.getByText("BGH").click()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue("BGH")

    // Rechtskraft wird beim Speichern durch Backend gesetzt
    await save(page)

    await expect(dropdown).toHaveText("Ja")
    await dropdown.click()
    await page
      .getByRole("option", {
        name: "Nein",
      })
      .click()

    await expect(dropdown).toHaveText("Nein")
  })

  test("that setting a non-special court leaves legal effect unchanged", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const dropdown = page.getByRole("combobox", { name: "Rechtskraft" })
    await expect(dropdown).toHaveText("Keine Angabe")

    await page.getByLabel("Gericht", { exact: true }).fill("aachen")
    await expect(page.getByTestId("combobox-spinner")).toBeHidden()
    await page.getByText("AG Aachen").click()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
      "AG Aachen",
    )
    await save(page)

    await expect(dropdown).toHaveText("Keine Angabe")
  })

  test(
    "court branch location",
    { tag: ["@RISDEV-3081"] },
    async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)

      await test.step("select court with branch location options", async () => {
        await selectCourt(page, "ArbG Bremen-Bremerhaven")
      })

      await test.step("pick branch location from dropdown", async () => {
        await page
          .getByLabel("Fehlerhaftes Gericht anzeigen", { exact: true })
          .click()

        await page.getByLabel("Sitz der Außenstelle").click()

        await expect(page.getByText("Kammern Bremen")).toBeVisible()
        await expect(page.getByText("Kammern Bremerhaven")).toBeVisible()
        await page.getByText("Kammern Bremen").click()
        await expect(page.getByLabel("Sitz der Außenstelle")).toHaveText(
          "Kammern Bremen",
        )
        await expect(page.getByText("Kammern Bremerhaven")).toBeHidden()

        await save(page)
      })

      await test.step("persists after reload", async () => {
        await page.reload()
        await expect(page.getByText("Kammern Bremen")).toBeVisible()
      })

      await test.step("show in preview", async () => {
        await navigateToPreview(page, documentNumber)
        await expect(
          page.getByText("Sitz der AußenstelleKammern Bremen"),
        ).toBeVisible()
      })

      await test.step("show warning in handover page", async () => {
        await navigateToHandover(page, documentNumber)

        await expect(
          page.getByText(
            "Folgende Rubriken sind befüllt und können nicht an die jDV exportiert werden",
          ),
        ).toBeVisible()
        await expect(page.getByText("Sitz der Außenstelle")).toBeVisible()
      })

      await test.step("changing court removes court branch location and disables dropdown", async () => {
        await navigateToCategories(page, documentNumber)
        await selectCourt(page, "BGH")
        await save(page)

        await expect(page.getByLabel("Sitz der Außenstelle")).toHaveText(
          "Bitte auswählen",
        )
        await expect(page.getByLabel("Sitz der Außenstelle")).toBeDisabled()
      })

      await test.step("remove court branch location", async () => {
        await selectCourt(page, "FG München")
        await page.getByLabel("Sitz der Außenstelle").click()
        await expect(page.getByText("Augsburg")).toBeVisible()
        await page.getByText("Augsburg").click()
        await expect(page.getByLabel("Sitz der Außenstelle")).toHaveText(
          "Augsburg",
        )

        await save(page)

        await page.locator("#branchLocation svg").first().click() // delete icon
        await expect(page.getByLabel("Sitz der Außenstelle")).toHaveText(
          "Bitte auswählen",
        )
      })
    },
  )

  async function selectCourt(page: Page, courtName: string) {
    await page.getByLabel("Gericht", { exact: true }).fill(courtName)
    await expect(page.getByTestId("combobox-spinner")).toBeHidden()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
      courtName,
    )
    await expect(page.getByText(courtName)).toBeVisible()
    await page.getByText(courtName).click()
    await expect(page.getByLabel("Gericht", { exact: true })).toHaveValue(
      courtName,
    )

    await save(page)
  }
})
