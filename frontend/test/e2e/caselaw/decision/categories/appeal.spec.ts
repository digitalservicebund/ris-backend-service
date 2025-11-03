import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillCombobox,
  navigateToCategories,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Rechtsmittelzulassung",
  {
    tag: ["@RISDEV-8627"],
  },
  () => {
    test("Rechtsmittel", async ({ page, prefilledDocumentUnit }) => {
      const documentNumber = prefilledDocumentUnit.documentNumber
      await navigateToCategories(page, documentNumber)

      await test.step("Rechtsmittel ist ohne Finanzgericht nicht sichtbar", async () => {
        await expect(page.getByText("Rechtsmittel")).toBeHidden()
      })

      await test.step("Rechtsmittel ist mit Finanzgericht sichtbar", async () => {
        await fillCombobox(page, "Gericht", "BFH")
        await expect(
          page.getByRole("button", { name: "Rechtsmittel" }),
        ).toBeVisible()
      })

      await test.step("Rechtsmittel Teileinträge werden angezeigt", async () => {
        await page.getByRole("button", { name: "Rechtsmittel" }).click()

        await expect(page.getByTestId("appellants")).toBeVisible()
        await expect(page.getByTestId("revision-defendant")).toBeVisible()
        await expect(page.getByTestId("revision-plaintiff")).toBeVisible()
        await expect(page.getByTestId("joint-revision-defendant")).toBeVisible()
        await expect(page.getByTestId("joint-revision-plaintiff")).toBeVisible()
        await expect(page.getByTestId("nzb-defendant")).toBeVisible()
        await expect(page.getByTestId("nzb-plaintiff")).toBeVisible()
        await expect(page.getByTestId("appeal-withdrawal")).toBeVisible()
        await expect(page.getByTestId("pkh-plaintiff")).toBeVisible()
      })

      await test.step("Rechtsmittel Teileinträge eintragen", async () => {
        await page.getByTestId("appellants").click()
        await page.getByLabel("Kläger").click()
        await page.getByLabel("Keine Angabe").click()
        await page.getByTestId("appellants").click()

        await page.getByTestId("revision-defendant").click()
        await page.getByLabel("unzulässig", { exact: true }).click()
        await page.getByTestId("revision-defendant").click()

        await page.getByTestId("revision-plaintiff").click()
        await page.getByLabel("zulässig", { exact: true }).click()
        await page.getByTestId("revision-plaintiff").click()

        await page.getByTestId("joint-revision-defendant").click()
        await page.getByLabel("teilweise unzulässig").click()
        await page.getByTestId("joint-revision-defendant").click()

        await page.getByTestId("joint-revision-plaintiff").click()
        await page.getByLabel("unbegründet", { exact: true }).click()
        await page.getByTestId("joint-revision-plaintiff").click()

        await page.getByTestId("nzb-defendant").click()
        await page.getByLabel("begründet", { exact: true }).click()
        await page.getByTestId("nzb-defendant").click()

        await page.getByTestId("nzb-plaintiff").click()
        await page.getByLabel("teilweise unbegründet").click()
        await page.getByTestId("nzb-plaintiff").click()

        await page.getByTestId("appeal-withdrawal").click()
        await page.getByLabel("Ja", { exact: true }).click()

        await page.getByTestId("pkh-plaintiff").click()
        await page.getByLabel("Nein").click()

        await expect(page.getByTestId("appellants")).toHaveText(
          "KlägerKeine Angabe",
        )
        await expect(page.getByTestId("revision-defendant")).toHaveText(
          "unzulässig",
        )
        await expect(page.getByTestId("revision-plaintiff")).toHaveText(
          "zulässig",
        )
        await expect(page.getByTestId("joint-revision-defendant")).toHaveText(
          "teilweise unzulässig",
        )
        await expect(page.getByTestId("joint-revision-plaintiff")).toHaveText(
          "unbegründet",
        )
        await expect(page.getByTestId("nzb-defendant")).toHaveText("begründet")
        await expect(page.getByTestId("nzb-plaintiff")).toHaveText(
          "teilweise unbegründet",
        )
        await expect(page.getByTestId("appeal-withdrawal")).toHaveText("Ja")
        await expect(page.getByTestId("pkh-plaintiff")).toHaveText("Nein")
      })

      await test.step("Ausgefüllte Rubrik wird bei Wechsel zu Nicht-Finanzgericht weiterhin angezeigt", async () => {
        await fillCombobox(page, "Gericht", "BGH")
        await expect(page.getByTestId("appellants")).toBeVisible()
      })
    })
  },
)
