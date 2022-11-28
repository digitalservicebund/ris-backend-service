import { expect } from "@playwright/test"

import { openNorm } from "./e2e-utils"
import { testWithImportedNorm } from "./fixtures"
import normCleanCars from "./testdata/norm_clean_cars.json"

testWithImportedNorm(
  "Check display of norm complex",
  async ({ page, createdGuid }) => {
    await openNorm(page, normCleanCars.longTitle, createdGuid)

    await expect(page).toHaveURL(`/norms/norm/${createdGuid}`)
    await expect(page.getByText(normCleanCars.longTitle)).toBeVisible()
    await expect(page.getByText(normCleanCars.articles[0].marker)).toBeVisible()
    await expect(page.getByText(normCleanCars.articles[0].title)).toBeVisible()
    await expect(
      page.getByText(normCleanCars.articles[0].paragraphs[0].marker)
    ).toBeVisible()
    await expect(
      page.getByText(normCleanCars.articles[0].paragraphs[0].text)
    ).toBeVisible()
    await expect(
      page.getByText(normCleanCars.articles[0].paragraphs[1].marker)
    ).toBeVisible()
    await expect(
      page.getByText(normCleanCars.articles[0].paragraphs[1].text)
    ).toBeVisible()
  }
)

testWithImportedNorm(
  "Check if frame fields are correctly displayed",
  async ({ page, createdGuid }) => {
    await openNorm(page, normCleanCars.longTitle, createdGuid)

    // Outer menu
    await expect(page.locator("a:has-text('Normenkomplex')")).toBeVisible()
    await expect(page.locator("a:has-text('Bestand')")).toBeVisible()
    await expect(page.locator("a:has-text('Abgabe')")).toBeVisible()
    const locatorFrameButton = page.locator("a:has-text('Rahmen')")
    await expect(locatorFrameButton).toBeVisible()

    await locatorFrameButton.click()
    await expect(page).toHaveURL(`/norms/norm/${createdGuid}/frame`)

    // Inner menu
    await expect(page.locator("a:has-text('Allgemeine Angaben')")).toBeVisible()
    await expect(page.locator("a:has-text('Dokumenttyp')")).toBeVisible()
    await expect(
      page.locator("a:has-text('Überschriften und Abkürzungen')")
    ).toBeVisible()
    await expect(page.locator("a:has-text('Normgeber')")).toBeVisible()
    await expect(page.locator("a:has-text('Federführung')")).toBeVisible()
    await expect(page.locator("a:has-text('Sachgebiet')")).toBeVisible()
    await expect(page.locator("a:has-text('Mitwirkende Organe')")).toBeVisible()

    // Allgemeine Angaben
    await expect(page.locator('h1:text-is("Allgemeine Angaben")')).toBeVisible()
    await expect(page.locator('label:text-is("Aktenzeichen")')).toBeVisible()
    await expect(page.locator('role=textbox[name="Aktenzeichen"]')).toBeEmpty()
    await expect(
      page.locator('label:text-is("Veröffentlichungsdatum")')
    ).toBeVisible()
    await expect(
      page.locator('role=textbox[name="Veröffentlichungsdatum"]')
    ).toBeEmpty()
    await expect(
      page.locator('label:text-is("Verkündungsdatum")')
    ).toBeVisible()
    expect(await page.inputValue('role=textbox[name="Verkündungsdatum"]')).toBe(
      normCleanCars.announcementDate
    )
    await expect(page.locator('label:text-is("Zitierdatum")')).toBeVisible()
    expect(await page.inputValue('role=textbox[name="Zitierdatum"]')).toBe(
      normCleanCars.citationDate
    )
    await expect(
      page.locator('label:text-is("Schlagwörter im Rahmenelement")')
    ).toBeVisible()
    expect(
      await page.inputValue(
        'role=textbox[name="Schlagwörter im Rahmenelement"]'
      )
    ).toBe(normCleanCars.frameKeywords)

    // Dokumenttyp
    await expect(page.locator('h1:text-is("Dokumenttyp")')).toBeVisible()
    await expect(page.locator('label:text-is("Typbezeichnung")')).toBeVisible()
    await expect(
      page.locator('role=textbox[name="Typbezeichnung"]')
    ).toBeEmpty()
    await expect(page.locator('label:text-is("Art der Norm")')).toBeVisible()
    await expect(page.locator('role=textbox[name="Art der Norm"]')).toBeEmpty()
    await expect(
      page.locator('label:text-is("Bezeichnung gemäß Vorlage")')
    ).toBeVisible()
    await expect(
      page.locator('role=textbox[name="Bezeichnung gemäß Vorlage"]')
    ).toBeEmpty()

    // Überschriften und Abkürzungen
    await expect(
      page.locator('h1:text-is("Überschriften und Abkürzungen")')
    ).toBeVisible()
    await expect(
      page.locator('label:text-is("Amtliche Langüberschrift")')
    ).toBeVisible()
    expect(
      await page.inputValue('role=textbox[name="Amtliche Langüberschrift"]')
    ).toBe(normCleanCars.longTitle)
    await expect(
      page.locator('label:text-is("Amtliche Kurzüberschrift")')
    ).toBeVisible()
    expect(
      await page.inputValue('role=textbox[name="Amtliche Kurzüberschrift"]')
    ).toBe(normCleanCars.officialShortTitle)
    await expect(
      page.locator('label:text-is("Amtliche Buchstabenabkürzung")')
    ).toBeVisible()
    expect(
      await page.inputValue('role=textbox[name="Amtliche Buchstabenabkürzung"]')
    ).toBe(normCleanCars.officialAbbreviation)
    await expect(page.locator('label:text-is("RIS-Abkürzung")')).toBeVisible()
    expect(await page.inputValue('role=textbox[name="RIS-Abkürzung"]')).toBe(
      normCleanCars.risAbbreviation
    )
    // Hidden fields
    const locatorHiddenh1 = page.locator(
      'h1:text-is("Nichtamtliche Überschriften und Abkürzungen")'
    )
    await expect(locatorHiddenh1).toBeVisible()
    await locatorHiddenh1.click()
    await expect(
      page.locator('label:text-is("Nichtamtliche Langüberschrift")')
    ).toBeVisible()
    await expect(
      page.locator('role=textbox[name="Nichtamtliche Langüberschrift"]')
    ).toBeEmpty()
    await expect(
      page.locator('label:text-is("Nichtamtliche Kurzüberschrift")')
    ).toBeVisible()
    await expect(
      page.locator('role=textbox[name="Nichtamtliche Kurzüberschrift"]')
    ).toBeEmpty()
    await expect(
      page.locator('label:text-is("Nichtamtliche Buchstabenabkürzung")')
    ).toBeVisible()
    await expect(
      page.locator('role=textbox[name="Nichtamtliche Buchstabenabkürzung"]')
    ).toBeEmpty()

    // Normgeber
    await expect(page.locator('h1:text-is("Normgeber")')).toBeVisible()
    await expect(
      page.locator(
        'label:text-is("Staat, Land, Stadt, Landkreis oder juristische Person")'
      )
    ).toBeVisible()
    expect(
      await page.inputValue(
        'role=textbox[name="Staat, Land, Stadt, Landkreis oder juristische Person"]'
      )
    ).toBe(normCleanCars.authorEntity)
    await expect(
      page.locator('label:text-is("Beschließendes Organ")')
    ).toBeVisible()
    expect(
      await page.inputValue('role=textbox[name="Beschließendes Organ"]')
    ).toBe(normCleanCars.authorDecidingBody)
    await expect(
      page.locator(
        'label:text-is("Beschlussfassung mit qualifizierter Mehrheit")'
      )
    ).toBeVisible()
    expect(
      await page.isChecked(
        'role=checkbox[name="Beschlussfassung mit qualifizierter Mehrheit"]'
      )
    ).toBeTruthy()

    // Federführung
    await expect(page.locator('h1:text-is("Federführung")')).toBeVisible()
    await expect(page.locator('label:text-is("Ressort")')).toBeVisible()
    expect(await page.inputValue('role=textbox[name="Ressort"]')).toBe(
      normCleanCars.leadJurisdiction
    )
    await expect(
      page.locator('label:text-is("Organisationseinheit")')
    ).toBeVisible()
    expect(
      await page.inputValue('role=textbox[name="Organisationseinheit"]')
    ).toBe(normCleanCars.leadUnit)

    // Sachgebiet
    await expect(page.locator('h1:text-is("Sachgebiet")')).toBeVisible()
    await expect(page.locator('label:text-is("FNA-Nummer")')).toBeVisible()
    expect(await page.inputValue('role=textbox[name="FNA-Nummer"]')).toBe(
      normCleanCars.subjectFna
    )
    await expect(
      page.locator('label:text-is("Frühere FNA-Nummer")')
    ).toBeVisible()
    await expect(
      page.locator('role=textbox[name="Frühere FNA-Nummer"]')
    ).toBeEmpty()
    await expect(page.locator('label:text-is("GESTA-Nummer")')).toBeVisible()
    expect(await page.inputValue('role=textbox[name="GESTA-Nummer"]')).toBe(
      normCleanCars.subjectGesta
    )
    await expect(
      page.locator('label:text-is("Bundesgesetzblatt Teil III")')
    ).toBeVisible()
    await expect(
      page.locator('role=textbox[name="Bundesgesetzblatt Teil III"]')
    ).toBeEmpty()

    // Mitwirkende Organe
    await expect(page.locator('h1:text-is("Mitwirkende Organe")')).toBeVisible()
    await expect(
      page.locator('label:text-is("Art der Mitwirkung")')
    ).toBeVisible()
    await expect(
      page.locator('role=textbox[name="Art der Mitwirkung"]')
    ).toBeEmpty()
    await expect(
      page.locator('label:text-is("Mitwirkendes Organ")')
    ).toBeVisible()
    await expect(
      page.locator('role=textbox[name="Mitwirkendes Organ"]')
    ).toBeEmpty()
  }
)
