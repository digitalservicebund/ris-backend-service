import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillCombobox,
  fillInput,
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "E-VSF (Elektronische Vorschriftensammlung Bundesfinanzverwaltung)",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-8501",
    },
    tag: ["@RISDEV-8501"],
  },
  () => {
    test("E-VSF", async ({ page, prefilledDocumentUnit }) => {
      const documentNumber = prefilledDocumentUnit.documentNumber
      await navigateToCategories(page, documentNumber)

      const evsfButton = page.getByRole("button", { name: "E-VSF" })
      await test.step("E-VSF ist ohne Finanzgericht nicht sichtbar", async () => {
        await expect(evsfButton).toBeHidden()
      })

      await test.step("E-VSF Button ist mit Finanzgericht sichtbar", async () => {
        await fillCombobox(page, "Gericht", "BFH")
        await expect(evsfButton).toBeVisible()
      })

      await test.step("E-VSF kann editiert werden", async () => {
        await evsfButton.click()
        await fillInput(page, "E-VSF", "X 00 00-0-0")
      })

      await test.step("Befülltes E-VSF ist mit anderem Gericht sichtbar", async () => {
        await fillCombobox(page, "Gericht", "BGH")
        await expect(page.getByLabel("E-VSF")).toHaveValue("X 00 00-0-0")
        await save(page)
      })

      await test.step("E-VSF erscheint in der Vorschau", async () => {
        await navigateToPreview(page, documentNumber)
        await expect(page.getByText("E-VSF")).toBeVisible()
        await expect(page.getByText("X 00 00-0-0")).toBeVisible()
      })

      await test.step("Auf Übergabeseite erscheint Warnung, dass E-VSF nicht übergeben wird", async () => {
        await navigateToHandover(page, documentNumber)
        await expect(
          page.getByText(
            "Folgende Rubriken sind befüllt und können nicht an die jDV exportiert werden",
          ),
        ).toBeVisible()
        await expect(page.getByText("E-VSF")).toBeVisible()
      })
    })
  },
)
