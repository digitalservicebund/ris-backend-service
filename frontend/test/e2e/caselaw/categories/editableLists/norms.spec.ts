import { expect, Locator, Page } from "@playwright/test"
import SingleNorm from "@/domain/singleNorm"
import {
  clearInput,
  fillInput,
  fillNormInputs,
  navigateToCategories,
  waitForSaving,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

test.describe("norm", () => {
  test("renders all fields", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    await expect(page.getByRole("heading", { name: "Normen" })).toBeVisible()
    await expect(page.getByLabel("RIS-Abkürzung")).toBeVisible()
    await expect(page.getByLabel("Einzelnorm der Norm")).toBeHidden()
    await expect(page.getByLabel("Fassungsdatum")).toBeHidden()
    await expect(page.getByLabel("Jahr der Norm")).toBeHidden()
    await expect(page.getByLabel("Norm speichern")).toBeHidden()
  })

  test("added items are saved, can be edited and deleted", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const container = page.getByLabel("Norm")

    // adding empty entry not possible
    await expect(page.getByLabel("Norm speichern")).toBeHidden()

    // add entry
    await fillNormInputs(page, {
      normAbbreviation: "PBefG",
    })

    await container.getByLabel("Norm speichern").click()
    await expect(container.getByText("PBefG")).toBeVisible()

    // edit entry
    await container.getByLabel("Listen Eintrag").first().click()
    await fillNormInputs(page, {
      normAbbreviation: "PBefGRVZustBehV NW",
    })

    await container.getByLabel("Norm speichern").click()
    await expect(container.getByText("PBefGRVZustBehV NW")).toBeVisible()

    // the second list item is a default list entry
    await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)

    // add second entry
    await waitForSaving(
      async () => {
        await fillNormInputs(page, {
          normAbbreviation: "PBefG",
        })
        await container.getByLabel("Norm speichern").click()
      },
      page,
      { clickSaveButton: true },
    )

    // the third list item is a default list entry
    await expect(container.getByLabel("Listen Eintrag")).toHaveCount(3)
    await page.reload()
    // the default list entry is not shown on reload page
    await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)
    await container.getByLabel("Weitere Angabe").isVisible()

    const listEntries = container.getByLabel("Listen Eintrag")
    await expect(listEntries).toHaveCount(2)

    await listEntries.first().click()
    await container.getByLabel("Eintrag löschen").click()
    // the default list entry is not shown on delete item
    await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)
    await container.getByLabel("Weitere Angabe").isVisible()
  })

  test("single norm validation", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    const container = page.getByLabel("Norm")

    // add entry with invalid norm
    await fillNormInputs(page, {
      normAbbreviation: "PBefG",
      singleNorms: [
        { singleNorm: "123", dateOfRelevance: "2020" } as SingleNorm,
      ],
    })

    await expect(container.getByText("Inhalt nicht valide")).toBeVisible()
    await expect(container.getByLabel("Norm speichern")).toBeVisible()

    // edit entry to fix invalid norm
    await fillNormInputs(page, {
      singleNorms: [{ singleNorm: "§ 123" } as SingleNorm],
    })

    await container.getByLabel("Norm speichern").click()
    await expect(container.getByText("PBefG")).toBeVisible()
    await expect(container.getByText("§ 123, 2020")).toBeVisible()
    await expect(container.getByText("Norm speichern")).toBeHidden()
  })

  test("invalid date format is not saved", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    const container = page.getByLabel("Norm")

    await fillNormInputs(page, {
      normAbbreviation: "PBefG",
      singleNorms: [
        {
          singleNorm: "§ 123",
          dateOfVersion: "29.02.2021",
          dateOfRelevance: "0023",
        } as SingleNorm,
      ],
    })

    await expect(container.getByText("Kein valides Jahr")).toBeVisible()
    await expect(container.getByText("Kein valides Datum")).toBeVisible()
    await expect(container.getByLabel("Norm speichern")).toBeVisible()

    await container.getByLabel("Norm speichern").click()
    // does not save, does not close the edit mode
    await expect(container.getByLabel("Norm speichern")).toBeVisible()
  })

  test("adding and deleting multiple single norms", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const container = page.getByLabel("Norm")

    await fillNormInputs(page, {
      normAbbreviation: "PBefG",
    })

    await container.getByLabel("Weitere Einzelnorm").click()
    await expect(
      container.getByLabel("Einzelnorm", { exact: true }),
    ).toHaveCount(2)

    await fillNormInputs(page, {
      singleNorms: [
        { singleNorm: "§ 123" } as SingleNorm,
        { singleNorm: "§ 456", dateOfRelevance: "2022" } as SingleNorm,
      ],
    })

    await container.getByLabel("Norm speichern").click()
    await expect(container.getByText("PBefG", { exact: true })).toBeVisible()
    await expect(container.getByText("PBefG, § 123")).toBeVisible()
    await expect(container.getByText("PBefG, § 456,")).toBeVisible()

    const listEntries = container.getByLabel("Listen Eintrag")
    await expect(listEntries).toHaveCount(2)
    await listEntries.first().click()

    await expect(
      container.getByLabel("Einzelnorm löschen", { exact: true }),
    ).toHaveCount(2)

    await container
      .getByLabel("Einzelnorm löschen", { exact: true })
      .last()
      .click()

    await expect(
      container.getByLabel("Einzelnorm", { exact: true }),
    ).toHaveCount(1)

    await container.getByLabel("Norm speichern").click()
    // with only one singlenorm, the ris abkürzung is not shown as headline
    await expect(container.getByText("PBefG", { exact: true })).toBeHidden()
    await expect(container.getByText("PBefG, § 123")).toBeVisible()
    await expect(container.getByText("§ 456, 2022")).toBeHidden()
  })

  test("cancel editing does not save anything", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const container = page.getByLabel("Norm")

    await fillNormInputs(page, {
      normAbbreviation: "PBefG",
      singleNorms: [{ singleNorm: "§ 123" } as SingleNorm],
    })

    await container.getByLabel("Norm speichern").click()
    await expect(container.getByText("PBefG")).toBeVisible()
    await expect(container.getByText("§ 123")).toBeVisible()

    const listEntries = container.getByLabel("Listen Eintrag")
    await listEntries.first().click()

    //cancel editing existing input falls back to last saved value
    await fillNormInputs(page, {
      normAbbreviation: "PBefG",
      singleNorms: [{ singleNorm: "§ 456" } as SingleNorm],
    })

    await container.getByLabel("Abbrechen").click()
    await expect(container.getByText("PBefG")).toBeVisible()
    await expect(container.getByText("§ 123")).toBeVisible()
    await expect(container.getByText("§ 456")).toBeHidden()

    await expect(listEntries).toHaveCount(1)
    await container.getByLabel("Weitere Angabe").click()

    //cancel editing new input deletes entry
    await fillNormInputs(page, {
      normAbbreviation: "AEG",
    })
    await container.getByLabel("Abbrechen").click()
    await expect(listEntries).toHaveCount(1)
  })

  test("validates agaist duplicate norm abbreviations", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const container = page.getByLabel("Norm")

    await fillNormInputs(page, {
      normAbbreviation: "PBefG",
      singleNorms: [{ singleNorm: "§ 123" } as SingleNorm],
    })

    await container.getByLabel("Norm speichern").click()
    await expect(container.getByText("PBefG")).toBeVisible()
    await expect(container.getByText("§ 123")).toBeVisible()

    await page.getByLabel("RIS-Abkürzung").fill("PBefG")
    await page.getByText("PBefG", { exact: true }).click()

    await expect(
      container.getByText("RIS-Abkürzung bereits eingegeben"),
    ).toBeVisible()

    await fillNormInputs(page, {
      normAbbreviation: "AEG",
    })

    await container.getByLabel("Norm speichern").click()
    const listEntries = container.getByLabel("Listen Eintrag")
    await listEntries.nth(1).click()
    await page.getByLabel("RIS-Abkürzung").fill("PBefG")
    await page.getByText("PBefG", { exact: true }).click()

    await expect(
      container.getByText("RIS-Abkürzung bereits eingegeben"),
    ).toBeVisible()
    await container.getByLabel("Norm speichern").click()
    await expect(
      container.getByText("RIS-Abkürzung bereits eingegeben"),
    ).toBeVisible()
  })

  test.describe("legal force", () => {
    test("display legal force feature only when the right court type is selected", async ({
      page,
      documentNumber,
    }) => {
      await navigateToCategories(page, documentNumber)

      await page.locator("[aria-label='Gericht']").fill("aalen")
      await page.locator("text=AG Aalen").click()
      await expect(page.locator("[aria-label='Gericht']")).toHaveValue(
        "AG Aalen",
      )

      await fillNormInputs(page, {
        normAbbreviation: "PBefG",
        singleNorms: [{ singleNorm: "§ 123" } as SingleNorm],
      })

      const normContainer = page.getByLabel("Norm")

      await expect(normContainer.getByText("Mit Gesetzeskraft")).toBeHidden()

      await page.locator("[aria-label='Gericht']").fill("VerfG")
      await page.locator("text=VerfG Dessau").last().click()
      await expect(normContainer.getByText("Mit Gesetzeskraft")).toBeVisible()
    })

    async function addNewLegalForce(page: Page, saveNormButton: Locator) {
      await fillInput(page, "Gesetzeskraft Typ", "Nichtig")
      await page.getByText("Nichtig").click()

      await fillInput(page, "Gesetzeskraft Geltungsbereich", "Brandenburg")
      await page.getByText("Brandenburg").click()

      await saveNormButton.click()

      await page.getByText("Speichern").click()
      await expect(page.locator("text=Nichtig (Brandenburg)")).toBeVisible()
    }

    test("legal forces should be deleted when court was changed to court without 'Gesetzeskraft'", async ({
      page,
      documentNumber,
    }) => {
      await navigateToCategories(page, documentNumber)

      await page.locator("[aria-label='Gericht']").fill("VerfG")
      await page.locator("text=VerfG Dessau-Roßlau").click()
      await expect(page.locator("[aria-label='Gericht']")).toHaveValue(
        "VerfG Dessau-Roßlau",
      )

      await fillNormInputs(page, {
        normAbbreviation: "PBefG",
        singleNorms: [{ singleNorm: "§ 123" } as SingleNorm],
      })

      const normContainer = page.getByLabel("Norm")
      const checkbox = normContainer.getByTestId("legal-force-checkbox")
      await expect(checkbox).toBeVisible()
      await expect(normContainer.getByText("Mit Gesetzeskraft")).toBeVisible()

      await checkbox.click()

      // add new legal force
      const saveNormButton = normContainer.getByLabel("Norm speichern")
      await addNewLegalForce(page, saveNormButton)

      // change court to court without "Gesetzeskraft"
      await page.locator("[aria-label='Gericht']").fill("BGH")
      await page.locator("text=BGH").click()
      await expect(page.locator("[aria-label='Gericht']")).toHaveValue("BGH")
      await page.getByText("Speichern").click()

      // Gesetzeskraft should be gone
      await expect(page.locator("text=Nichtig (Brandenburg)")).toBeHidden()
    })

    test("add new legal force, edit and remove it", async ({
      page,
      documentNumber,
    }) => {
      await navigateToCategories(page, documentNumber)
      const normContainer = page.getByLabel("Norm")

      await page.locator("[aria-label='Gericht']").fill("VerfG")
      await page.locator("text=VerfG Dessau").last().click()

      await fillNormInputs(page, {
        normAbbreviation: "PBefG",
        singleNorms: [{ singleNorm: "§ 123" } as SingleNorm],
      })

      const checkbox = normContainer.getByTestId("legal-force-checkbox")
      await expect(checkbox).toBeVisible()

      const legalForceTypeCombobox = normContainer.getByTestId(
        "legal-force-type-combobox",
      )
      const legalForceRegionCombobox = normContainer.getByTestId(
        "legal-force-region-combobox",
      )
      await expect(legalForceTypeCombobox).toBeHidden()
      await expect(legalForceRegionCombobox).toBeHidden()

      await checkbox.click()

      await expect(legalForceTypeCombobox).toBeVisible()
      await expect(legalForceRegionCombobox).toBeVisible()

      // add new legal force
      const saveNormButton = normContainer.getByLabel("Norm speichern")
      await addNewLegalForce(page, saveNormButton)

      // edit legal force
      const listEntries = normContainer.getByLabel("Listen Eintrag")
      await expect(listEntries).toHaveCount(2)
      await listEntries.first().click()

      await fillInput(page, "Gesetzeskraft Typ", "Vereinbar")
      await page.getByText("Vereinbar").click()

      await fillInput(page, "Gesetzeskraft Geltungsbereich", "Berlin")
      await page.getByText("Berlin (Ost)").click()

      await saveNormButton.click()

      await page.getByText("Speichern").click()
      await expect(page.locator("text=Vereinbar (Berlin (Ost))")).toBeVisible()

      // remove legal force
      await listEntries.first().click()

      await clearInput(page, "Gesetzeskraft Typ")
      await clearInput(page, "Gesetzeskraft Geltungsbereich")

      await saveNormButton.click()
      await page.getByText("Speichern").click()
      await expect(page.locator("text=Vereinbar (Berlin (Ost))")).toBeHidden()
    })

    test("legal force field validation", async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)
      const normContainer = page.getByLabel("Norm")

      await page.locator("[aria-label='Gericht']").fill("VerfG")
      await page.locator("text=VerfG Dessau").last().click()

      await fillNormInputs(page, {
        normAbbreviation: "PBefG",
        singleNorms: [{ singleNorm: "§ 123" } as SingleNorm],
      })

      const checkbox = normContainer.getByTestId("legal-force-checkbox")
      await expect(checkbox).toBeVisible()

      const legalForceTypeCombobox = normContainer.getByTestId(
        "legal-force-type-combobox",
      )
      const legalForceRegionCombobox = normContainer.getByTestId(
        "legal-force-region-combobox",
      )

      await checkbox.click()

      await expect(legalForceTypeCombobox).toBeVisible()
      await expect(legalForceRegionCombobox).toBeVisible()

      // add empty legal force
      const saveNormButton = normContainer.getByLabel("Norm speichern")
      await saveNormButton.click()

      await page.getByText("Speichern").click()
      await expect(page.locator("text=Fehlende Daten")).toBeVisible()

      // enter edit mode
      const listEntries = normContainer.getByLabel("Listen Eintrag")
      await expect(listEntries).toHaveCount(2)
      await listEntries.first().click()

      // check that both fields display error message
      await expect(page.locator("text=Pflichtfeld nicht befüllt")).toHaveCount(
        2,
      )
      await expect(
        page.locator("text=Pflichtfeld nicht befüllt").first(),
      ).toBeVisible()
      await expect(
        page.locator("text=Pflichtfeld nicht befüllt").last(),
      ).toBeVisible()

      await page.locator(`[aria-label='Gesetzeskraft Typ']`).fill("Vereinbar")
      await page.getByText("Vereinbar").click()

      // check that only one field displays error message
      await expect(page.locator("text=Pflichtfeld nicht befüllt")).toBeVisible()

      await page
        .locator(`[aria-label='Gesetzeskraft Geltungsbereich']`)
        .fill("Berlin")
      await page.getByText("Berlin (Ost)").click()

      await expect(page.locator("text=Pflichtfeld nicht befüllt")).toHaveCount(
        0,
      )
      await expect(page.locator("text=Pflichtfeld nicht befüllt.")).toBeHidden()

      await saveNormButton.click()

      await page.getByText("Speichern").click()
      await expect(page.locator("text=Vereinbar (Berlin (Ost))")).toBeVisible()
    })
  })
})
