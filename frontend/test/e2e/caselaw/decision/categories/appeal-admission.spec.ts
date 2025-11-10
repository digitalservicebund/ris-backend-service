import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillCombobox,
  fillSelect,
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Rechtsmittelzulassung",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-8629",
    },
    tag: ["@RISDEV-8629"],
  },
  () => {
    test("Rechtsmittelzulassung", async ({ page, prefilledDocumentUnit }) => {
      const documentNumber = prefilledDocumentUnit.documentNumber
      await navigateToCategories(page, documentNumber)

      const apealAdmissionHeadline = page.getByRole("heading", {
        name: "Rechtsmittelzulassung",
      })
      await test.step("Rechtsmittelzulassung ist ohne Finanzgericht nicht sichtbar", async () => {
        await expect(apealAdmissionHeadline).toBeHidden()
      })

      await test.step("Rechtsmittelzulassung ist mit Finanzgericht sichtbar", async () => {
        await fillCombobox(page, "Gericht", "BFH")
        await expect(apealAdmissionHeadline).toBeVisible()
      })

      await test.step('"Rechtsmittel zugelassen" kann editiert werden', async () => {
        await fillSelect(page, "Rechtsmittel zugelassen", "Nein")
      })

      await test.step('"Rechtsmittel zugelassen durch" wird nicht angezeigt wenn "Nein" ausgewählt ist', async () => {
        await expect(
          page.getByRole("combobox", { name: "Rechtsmittel zugelassen durch" }),
        ).toBeHidden()
      })

      await test.step('"Rechtsmittel zugelassen durch" wird angezeigt wenn "Ja" ausgewählt ist', async () => {
        await fillSelect(page, "Rechtsmittel zugelassen", "Ja")
        await expect(
          page.getByRole("combobox", { name: "Rechtsmittel zugelassen durch" }),
        ).toBeVisible()
      })

      await test.step('"Rechtsmittel zugelassen durch" kann editiert werden', async () => {
        await fillSelect(page, "Rechtsmittel zugelassen durch", "BFH")
      })

      await test.step("Befülltes Rechtsmittelzulassung ist mit anderem Gericht sichtbar", async () => {
        await fillCombobox(page, "Gericht", "BGH")
        await expect(
          page.getByLabel("Rechtsmittel zugelassen", { exact: true }),
        ).toHaveText("Ja")
        await expect(
          page.getByLabel("Rechtsmittel zugelassen durch", { exact: true }),
        ).toHaveText("BFH")
        await save(page)
      })

      await test.step("Rechtsmittelzulassung erscheint in der Vorschau", async () => {
        await navigateToPreview(page, documentNumber)
        await expect(page.getByText("Rechtsmittelzulassung")).toBeVisible()
        await expect(page.getByText("Ja, durch BFH")).toBeVisible()
      })

      await test.step("Auf Übergabeseite erscheint Warnung, dass Rechtsmittelzulassung nicht übergeben wird", async () => {
        await navigateToHandover(page, documentNumber)
        await expect(
          page.getByText(
            "Folgende Rubriken sind befüllt und können nicht an die jDV exportiert werden",
          ),
        ).toBeVisible()
        await expect(page.getByText("Rechtsmittelzulassung")).toBeVisible()
      })
    })
  },
)
