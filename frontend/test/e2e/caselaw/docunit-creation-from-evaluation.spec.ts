import { expect } from "@playwright/test"
import {
  fillInput,
  navigateToPeriodicalReferences,
  waitForInputValue,
} from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

test.describe(
  "Creation of new documentation units from periodical evaluation",
  {
    tag: "@RISDEV-4562",
    annotation: {
      type: "epic",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4562",
    },
  },
  () => {
    test(
      "Documentation office is automatically selected based on court",
      {
        tag: "@RISDEV-4831",
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4829",
        },
      },
      async ({ page, edition }) => {
        await navigateToPeriodicalReferences(page, edition.id ?? "")

        await fillInput(page, "Zitatstelle *", "12")
        await fillInput(page, "Klammernzusatz", "L")

        await test.step("DocOffice is automatically selected based on court", async () => {
          // AG Aachen is a BGH court
          await fillInput(page, "Gericht", "AG Aachen")
          await page.getByText("AG Aachen").click()
          await waitForInputValue(page, "[aria-label='Gericht']", "AG Aachen")

          await expect(
            page.getByText("Nicht die passende Entscheidung gefunden?"),
          ).toBeHidden()
          await page.getByText("Suchen").click()
          await expect(
            page.getByText("Nicht die passende Entscheidung gefunden?"),
          ).toBeVisible()
          await waitForInputValue(
            page,
            "[aria-label='zuständige Dokumentationsstelle']",
            "BGH",
          )

          // Verwaltungsgericht Aarau is a BVerwG court
          await fillInput(page, "Gericht", "Verwaltungsgericht Aarau")
          await page.getByText("Verwaltungsgericht Aarau").click()
          await page.getByText("Suchen").click()
          await waitForInputValue(
            page,
            "[aria-label='zuständige Dokumentationsstelle']",
            "BVerwG",
          )
        })

        await test.step("Foregn courts are not assigned to a responsible doc office", async () => {
          await fillInput(page, "Gericht", "Arbeits- und Sozialgericht Wien")
          await page.getByText("Arbeits- und Sozialgericht Wien").click()
          await page.getByText("Suchen").click()
          await waitForInputValue(
            page,
            "[aria-label='zuständige Dokumentationsstelle']",
            "",
          )
        })

        await test.step("DocOffice can be changed manually", async () => {
          await page.getByTestId("documentation-office-combobox").click()

          await expect(
            page.locator("[aria-label='dropdown-option'] >> nth=6"),
          ).toBeVisible()

          await expect(page.getByText("BAG")).toBeVisible()
          await expect(page.getByText("BFH")).toBeVisible()

          await fillInput(page, "zuständige Dokumentationsstelle", "bv")
          await waitForInputValue(
            page,
            "[aria-label='zuständige Dokumentationsstelle']",
            "bv",
          )

          await expect(page.getByText("BAG")).toBeHidden()
          await expect(page.getByText("BFH")).toBeHidden()
          await expect(page.getByText("BVerwG")).toBeVisible()
          await expect(page.getByText("BVerfG")).toBeVisible()

          await page.getByText("BVerfG").click()

          await waitForInputValue(
            page,
            "[aria-label='zuständige Dokumentationsstelle']",
            "BVerfG",
          )
        })
      },
    )
  },
)
