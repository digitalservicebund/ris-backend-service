import { expect } from "@playwright/test"
import SingleNorm from "@/domain/singleNorm"
import {
  clearInput,
  fillCombobox,
  fillNormInputs,
  navigateToCategories,
  save,
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
    await container.getByTestId("list-entry-0").click()
    await fillNormInputs(page, {
      normAbbreviation: "PBefGZustV HE",
    })

    await container.getByLabel("Norm speichern").click()
    await expect(container.getByText("PBefGZustV HE")).toBeVisible()

    // the second list item is a default list entry
    await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)

    // add second entry

    await fillNormInputs(page, {
      normAbbreviation: "PBefG",
    })
    await container.getByLabel("Norm speichern").click()

    // the third list item is a default list entry
    await expect(container.getByLabel("Listen Eintrag")).toHaveCount(3)
    await save(page)
    await page.reload()
    // the default list entry is not shown on reload page
    await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)
    await container.getByLabel("Weitere Angabe").isVisible()

    const listEntries = container.getByLabel("Listen Eintrag")
    await expect(listEntries).toHaveCount(2)

    await container.getByTestId("list-entry-0").click()
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
        { singleNorm: " § 123" } as SingleNorm, // check trimming
        { singleNorm: "§ 456", dateOfRelevance: "2022" } as SingleNorm,
      ],
    })

    await container.getByLabel("Norm speichern").click()
    await expect(container.getByText("PBefG", { exact: true })).toBeVisible()
    await expect(container.getByText("PBefG, § 123")).toBeVisible()
    await expect(container.getByText("PBefG, § 456,")).toBeVisible()

    const listEntries = container.getByLabel("Listen Eintrag")
    await expect(listEntries).toHaveCount(2)
    await container.getByTestId("list-entry-0").click()

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
    await container.getByTestId("list-entry-0").click()

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

    await fillCombobox(page, "RIS-Abkürzung", "PBefG")

    await expect(
      container.getByText("RIS-Abkürzung bereits eingegeben"),
    ).toBeVisible()

    await fillNormInputs(page, {
      normAbbreviation: "AEG",
    })

    await container.getByLabel("Norm speichern").click()
    await container.getByTestId("list-entry-1").click()
    await fillCombobox(page, "RIS-Abkürzung", "PBefG")

    await expect(
      container.getByText("RIS-Abkürzung bereits eingegeben"),
    ).toBeVisible()
    await container.getByLabel("Norm speichern").click()
    await expect(
      container.getByText("RIS-Abkürzung bereits eingegeben"),
    ).toBeVisible()
  })

  test("does not add duplicate single norms", async ({
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
    await expect(container.getByText("PBefG, § 123")).toBeVisible()

    await container.getByTestId("list-entry-0").click()
    await container.getByLabel("Weitere Einzelnorm").click()

    await fillNormInputs(page, {
      singleNorms: [
        { singleNorm: "§ 123", dateOfRelevance: "2022" } as SingleNorm,
        { singleNorm: "§ 123", dateOfRelevance: "2022" } as SingleNorm,
      ],
    })

    await container.getByLabel("Norm speichern").click()
    await expect(container.getByText("PBefG, § 123, 2022")).toBeVisible()
  })

  test.describe("legal force", () => {
    test("display legal force feature only when the right court type is selected", async ({
      page,
      documentNumber,
    }) => {
      await navigateToCategories(page, documentNumber)

      await fillCombobox(page, "Gericht", "AG Aalen")

      await fillNormInputs(page, {
        normAbbreviation: "PBefG",
        singleNorms: [{ singleNorm: "§ 123" } as SingleNorm],
      })

      const normContainer = page.getByLabel("Norm")

      await expect(normContainer.getByText("Mit Gesetzeskraft")).toBeHidden()

      await fillCombobox(page, "Gericht", "VerfG Dessau")
      await expect(normContainer.getByText("Mit Gesetzeskraft")).toBeVisible()
    })

    test("add new legal force, edit and remove it", async ({
      page,
      documentNumber,
    }) => {
      await navigateToCategories(page, documentNumber)
      const normContainer = page.getByLabel("Norm")

      await fillCombobox(page, "Gericht", "VerfG Dessau")

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
      await fillCombobox(page, "Gesetzeskraft Typ", "Nichtig")

      await fillCombobox(page, "Gesetzeskraft Geltungsbereich", "Brandenburg")

      const saveNormButton = normContainer.getByLabel("Norm speichern")
      await saveNormButton.click()

      await expect(page.getByText("Nichtig (Brandenburg)")).toBeVisible()

      // edit legal force
      const listEntries = normContainer.getByLabel("Listen Eintrag")
      await expect(listEntries).toHaveCount(2)
      await normContainer.getByTestId("list-entry-0").click()

      await fillCombobox(page, "Gesetzeskraft Typ", "Vereinbar")

      await fillCombobox(page, "Gesetzeskraft Geltungsbereich", "Berlin (Ost)")

      await saveNormButton.click()

      await expect(page.getByText("Vereinbar (Berlin (Ost))")).toBeVisible()

      // remove legal force
      await normContainer.getByTestId("list-entry-0").click()

      await clearInput(page, "Gesetzeskraft Typ")
      // If the combobox is cleared directly without a click action,
      // the result list of the first combobox will stay open blocking the save button
      page
        .getByTestId("legal-force-region-combobox")
        .getByRole("button", { name: "Auswahl zurücksetzen" })

      await saveNormButton.click()
      await expect(page.getByText("Vereinbar (Berlin (Ost))")).toBeHidden()
    })

    test("legal force field validation", async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)
      const normContainer = page.getByLabel("Norm")

      await fillCombobox(page, "Gericht", "VerfG Dessau")

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

      await expect(page.getByText("Fehlende Daten")).toBeVisible()

      // enter edit mode
      const listEntries = normContainer.getByLabel("Listen Eintrag")
      await expect(listEntries).toHaveCount(2)
      await normContainer.getByTestId("list-entry-0").click()

      // check that both fields display error message
      await expect(page.getByText("Pflichtfeld nicht befüllt")).toHaveCount(2)

      await fillCombobox(page, "Gesetzeskraft Typ", "Vereinbar")

      // check that only one field displays error message
      await expect(page.getByText("Pflichtfeld nicht befüllt")).toBeVisible()

      await fillCombobox(page, "Gesetzeskraft Geltungsbereich", "Berlin (Ost)")

      await expect(page.getByText("Pflichtfeld nicht befüllt.")).toBeHidden()

      await saveNormButton.click()

      await expect(page.getByText("Vereinbar (Berlin (Ost))")).toBeVisible()
    })
  })

  test("add three norms and remove the first", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const normContainer = page.getByLabel("Norm")

    await fillCombobox(page, "Gericht", "VerfG Dessau")

    await fillNormInputs(page, {
      normAbbreviation: "PBefG",
      singleNorms: [{ singleNorm: "§ 123" } as SingleNorm],
    })

    let saveNormButton = normContainer.getByLabel("Norm speichern")
    await saveNormButton.click()

    await fillNormInputs(page, {
      normAbbreviation: "BGB",
      singleNorms: [{ singleNorm: "§ 1" } as SingleNorm],
    })

    saveNormButton = normContainer.getByLabel("Norm speichern")
    await saveNormButton.click()

    await fillNormInputs(page, {
      normAbbreviation: "KBErrG",
      singleNorms: [{ singleNorm: "§ 8" } as SingleNorm],
    })

    saveNormButton = normContainer.getByLabel("Norm speichern")
    await saveNormButton.click()

    await expect(page.getByText("PBefG, § 123", { exact: true })).toBeVisible()
    await expect(page.getByText("BGB, § 1", { exact: true })).toBeVisible()
    await expect(page.getByText("KBErrG, § 8", { exact: true })).toBeVisible()

    await normContainer.getByTestId("list-entry-0").click()

    await expect(
      page.getByText("Mit Gesetzeskraft", { exact: true }),
    ).toBeVisible()

    await normContainer.getByLabel("Eintrag löschen").click()

    await expect(page.getByText("PBefG, § 123", { exact: true })).toBeHidden()
    await expect(page.getByText("BGB, § 1", { exact: true })).toBeVisible()
    await expect(page.getByText("KBErrG, § 8", { exact: true })).toBeVisible()

    await normContainer.getByTestId("list-entry-0").click()

    await expect(
      normContainer.getByText("Mit Gesetzeskraft", { exact: true }),
    ).toBeVisible()
    await expect(
      normContainer.getByLabel("Einzelnorm der Norm", { exact: true }),
    ).toHaveValue("§ 1")

    await expect(
      normContainer.getByLabel("RIS-Abkürzung", { exact: true }),
    ).toHaveValue("BGB")

    await expect(page.getByText("PBefG, § 123", { exact: true })).toBeHidden()
    await expect(page.getByText("KBErrG, § 8", { exact: true })).toBeVisible()
  })
})
