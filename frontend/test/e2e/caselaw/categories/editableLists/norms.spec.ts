import { expect } from "@playwright/test"
import SingleNorm from "@/domain/singleNorm"
import {
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
    await expect(container.getByText("PBefG")).toBeVisible()
    await expect(container.getByText("§ 123")).toBeVisible()
    await expect(container.getByText("0023")).toBeHidden()
    await expect(container.getByText("29.02.2021")).toBeHidden()
    await expect(container.getByText("Norm speichern")).toBeHidden()
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

    await fillNormInputs(page, {
      normAbbreviation: "PBefG",
      singleNorms: [{ singleNorm: "§ 456" } as SingleNorm],
    })

    await container.getByLabel("Abbrechen").click()
    await expect(container.getByText("PBefG")).toBeVisible()
    await expect(container.getByText("§ 123")).toBeVisible()
    await expect(container.getByText("§ 456")).toBeHidden()
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
})
