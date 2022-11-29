import { expect } from "@playwright/test"

import { openNorm } from "./e2e-utils"
import { testWithImportedNorm } from "./fixtures"
import normCleanCars from "./testdata/norm_clean_cars.json"

testWithImportedNorm(
  "Check if fields can be edited",
  async ({ page, createdGuid }) => {
    await openNorm(page, normCleanCars.longTitle, createdGuid)

    const locatorFrameButton = page.locator("a:has-text('Rahmen')")
    await expect(locatorFrameButton).toBeVisible()
    await locatorFrameButton.click()
    await expect(page).toHaveURL(`/norms/norm/${createdGuid}/frame`)

    // Update category Allgemeine Angaben
    await expect(page.locator('label:text-is("Aktenzeichen")')).toBeVisible()
    const selectInputReferencenumber = 'role=textbox[name="Aktenzeichen"]'
    await expect(page.locator(selectInputReferencenumber)).toBeEmpty()
    const locatorInputReferencenumber = page.locator(selectInputReferencenumber)
    await expect(locatorInputReferencenumber).toBeEditable()
    await locatorInputReferencenumber.fill("referenceNumber")

    await expect(
      page.locator('label:text-is("Verkündungsdatum")')
    ).toBeVisible()
    const selectorInputAnnouncementDate =
      'role=textbox[name="Verkündungsdatum"]'
    expect(await page.inputValue(selectorInputAnnouncementDate)).toBe(
      normCleanCars.announcementDate
    )
    const locatorInputAnnouncementDate = page.locator(
      selectorInputAnnouncementDate
    )
    await expect(locatorInputAnnouncementDate).toBeEditable()
    await locatorInputAnnouncementDate.fill("2022-11-29")

    // Update category Dokumenttyp
    await expect(page.locator('label:text-is("Art der Norm")')).toBeVisible()
    const selectInputNormType = 'role=textbox[name="Art der Norm"]'
    await expect(page.locator(selectInputNormType)).toBeEmpty()
    const locatorInputArtType = page.locator(selectInputNormType)
    await expect(locatorInputArtType).toBeEditable()
    await locatorInputArtType.fill("norm type")

    // Update category Überschriften und Abkürzungen
    await expect(
      page.locator('label:text-is("Amtliche Langüberschrift")')
    ).toBeVisible()
    const selectorInputLongTitle =
      'role=textbox[name="Amtliche Langüberschrift"]'
    expect(await page.inputValue(selectorInputLongTitle)).toBe(
      normCleanCars.longTitle
    )
    const locatorInputLongTitle = page.locator(selectorInputLongTitle)
    await expect(locatorInputLongTitle).toBeEditable()
    await locatorInputLongTitle.fill("longTitle")

    // Update category Normgeber
    await expect(
      page.locator('label:text-is("Beschließendes Organ")')
    ).toBeVisible()
    const selectorInputDecidingBody =
      'role=textbox[name="Beschließendes Organ"]'
    expect(await page.inputValue(selectorInputDecidingBody)).toBe(
      normCleanCars.authorDecidingBody
    )
    const locatorInputDecidingBody = page.locator(selectorInputDecidingBody)
    await expect(locatorInputDecidingBody).toBeEditable()
    await locatorInputDecidingBody.fill("deciding body")

    await expect(
      page.locator(
        'label:text-is("Beschlussfassung mit qualifizierter Mehrheit")'
      )
    ).toBeVisible()

    const selectorCheckboxAuthorIsResolutionMajority =
      'role=checkbox[name="Beschlussfassung mit qualifizierter Mehrheit"]'
    expect(
      await page.isChecked(selectorCheckboxAuthorIsResolutionMajority)
    ).toBeTruthy()
    await page.uncheck(selectorCheckboxAuthorIsResolutionMajority)

    // Update category Federführung
    await expect(page.locator('label:text-is("Ressort")')).toBeVisible()
    const selectorInputLeadJurisdiction = 'role=textbox[name="Ressort"]'
    expect(await page.inputValue(selectorInputLeadJurisdiction)).toBe(
      normCleanCars.leadJurisdiction
    )
    const locatorInputLeadJurisdiction = page.locator(
      selectorInputLeadJurisdiction
    )
    await expect(locatorInputLeadJurisdiction).toBeEditable()
    await locatorInputLeadJurisdiction.fill("lead jurisdiction")

    // Update category Sachgebiet
    await expect(page.locator('label:text-is("GESTA-Nummer")')).toBeVisible()
    const selectorInputGestaNumber = 'role=textbox[name="GESTA-Nummer"]'
    expect(await page.inputValue(selectorInputGestaNumber)).toBe(
      normCleanCars.subjectGesta
    )
    const locatorInputGestaNumber = page.locator(selectorInputGestaNumber)
    await expect(locatorInputGestaNumber).toBeEditable()
    await locatorInputGestaNumber.fill("gesta number")

    // Update category Mitwirkende Organe
    await expect(
      page.locator('label:text-is("Mitwirkendes Organ")')
    ).toBeVisible()
    const selectorInputParticipationInstitution =
      'role=textbox[name="Mitwirkendes Organ"]'
    await expect(
      page.locator(selectorInputParticipationInstitution)
    ).toBeEmpty()
    const locatorInputParticipationInstitution = page.locator(
      selectorInputParticipationInstitution
    )
    await expect(locatorInputParticipationInstitution).toBeEditable()
    await locatorInputParticipationInstitution.fill("participation institution")

    await page.locator("[aria-label='Rahmendaten Speichern Button']").click()
    await expect(
      page.locator("text=Zuletzt gespeichert um").first()
    ).toBeVisible()
    await page.reload()
    expect(await page.inputValue(selectorInputLongTitle)).toBe("longTitle")
    expect(await page.inputValue(selectInputReferencenumber)).toBe(
      "referenceNumber"
    )
    expect(await page.inputValue(selectorInputAnnouncementDate)).toBe(
      "2022-11-29"
    )
    expect(await page.inputValue(selectInputNormType)).toBe("norm type")
    expect(await page.inputValue(selectorInputDecidingBody)).toBe(
      "deciding body"
    )
    expect(
      await page.isChecked(selectorCheckboxAuthorIsResolutionMajority)
    ).toBeFalsy()
    expect(await page.inputValue(selectorInputLeadJurisdiction)).toBe(
      "lead jurisdiction"
    )
    expect(await page.inputValue(selectorInputGestaNumber)).toBe("gesta number")
    expect(await page.inputValue(selectorInputParticipationInstitution)).toBe(
      "participation institution"
    )
  }
)
