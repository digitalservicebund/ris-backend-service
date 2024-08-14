import { expect } from "@playwright/test"
import {
  navigateToPreview,
  navigateToHandover,
  save,
  navigateToReferences,
  fillInput,
  waitForInputValue,
  navigateToLegalPeriodicalEvaluation,
} from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

test.describe(
  "references",
  {
    tag: "@RISDEV-4264",
    annotation: {
      type: "epic",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4264",
    },
  },
  () => {
    test(
      "Reference overview with a list of editions per periodical",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4497",
        },
      },
      async ({ page }) => {
        await test.step("References is a new selectable menu item in the top navigation", async () => {
          await navigateToLegalPeriodicalEvaluation(page)
        })

        await test.step("A periodical can be selected using a combo box.", async () => {
          await fillInput(page, "Periodikum", "MM")
          await expect(
            page.getByText("Magazin des Berliner Mieterverein e.V.", {
              exact: true,
            }),
          ).toBeVisible()
          await page.getByText("MM | Mieter Magazin", { exact: true }).click()
          await waitForInputValue(page, "[aria-label='Periodikum']", "MM")
        })

        await test.step("All related references appear in the results, grouped by edition/issue.", async () => {
          // todo
        })

        await test.step("By clicking on an issue, additional references can be attributed to this issue or existing references can be edited.", async () => {
          // todo
        })
        await test.step("If references are not assigned to an edition, they will not appear as a result in the list.", async () => {
          // todo
        })
        await test.step("A new evaluation is started using the “Neue Periodikaauswertung” button.", async () => {
          const promise = page.context().waitForEvent("page")
          await page.getByLabel("Neue Periodikaauswertung").click()
          const newPage = await promise
          expect(newPage.url()).toContain("legal-periodical-references/new")
        })
      },
    )

    test(
      "Periodical evaluation",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4499",
        },
      },
      async ({ page, prefilledDocumentUnit }) => {
        await navigateToReferences(
          page,
          prefilledDocumentUnit.documentNumber ?? "",
        )

        await test.step("Add reference with only 'Klammerzusatz' shows error in 'Periodikum' & 'Zitatstelle'", async () => {
          await fillInput(page, "Klammernzusatz", "LT")
          await page.locator("[aria-label='Fundstelle speichern']").click()
          await expect(page.getByText("Pflichtfeld nicht befüllt")).toHaveCount(
            2,
          )
        })
        await test.step("Add 'Periodikum' removes error there, but still shows error for 'Zitatstelle'", async () => {
          await fillInput(page, "Periodikum", "wdg")
          await page
            .getByText("WdG | Welt der Gesundheitsversorgung", {
              exact: true,
            })
            .click()
          await page.locator("[aria-label='Fundstelle speichern']").click()
          await expect(page.getByText("Pflichtfeld nicht befüllt")).toHaveCount(
            1,
          )
        })
        await test.step("Add 'Zitatstelle' removes error, reference can be saved", async () => {
          await fillInput(page, "Zitatstelle", "2024, 10-12")
          await page.locator("[aria-label='Fundstelle speichern']").click()
          await expect(page.getByText("Pflichtfeld nicht befüllt")).toBeHidden()
        })
      },
    )

    test(
      "References are visible in preview",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4335",
        },
      },
      async ({ page, documentNumber }) => {
        await test.step("References are not rendered in preview when empty", async () => {
          await navigateToPreview(page, documentNumber)
          await expect(page.getByText("Fundstellen")).toBeHidden()
        })

        await navigateToReferences(page, documentNumber)

        await test.step("Add primary and secondary references with all data, verify remdering in preview", async () => {
          await fillInput(page, "Periodikum", "wdg")
          await page
            .getByText("WdG | Welt der Gesundheitsversorgung", {
              exact: true,
            })
            .click()
          await fillInput(page, "Zitatstelle", "2024, 10-12")
          await fillInput(page, "Klammernzusatz", "LT")
          await page.locator("[aria-label='Fundstelle speichern']").click()
          await expect(page.getByText("WdG 2024, 10-12 (LT)")).toBeVisible()
          await expect(
            page.getByText("sekundär", {
              exact: true,
            }),
          ).toBeVisible()
          await fillInput(page, "Periodikum", "GVBl BB")
          await page
            .getByText(
              "GVBl BB | Gesetz- und Verordnungsblatt für das Land Brandenburg",
              { exact: true },
            )
            .click()
          await fillInput(page, "Zitatstelle", "2020, 01-99")
          await fillInput(page, "Klammernzusatz", "L")
          await page.locator("[aria-label='Fundstelle speichern']").click()
          await expect(page.getByText("GVBl BB 2020, 01-99 (L)")).toBeVisible()
          await expect(
            page.getByText("primär", {
              exact: true,
            }),
          ).toBeVisible()
        })
        await save(page)

        await navigateToPreview(page, documentNumber)

        await expect(
          page.getByText("Primäre FundstellenGVBl BB 2020, 01-99 (L)"),
        ).toBeVisible()
        await expect(
          page.getByText("Sekundäre FundstellenWdG 2024, 10-12 (LT)"),
        ).toBeVisible()
      },
    )

    test(
      "References can be exported",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4337",
        },
      },
      async ({ page, prefilledDocumentUnit }) => {
        await navigateToReferences(
          page,
          prefilledDocumentUnit.documentNumber ?? "",
        )

        await test.step("Add primary and secondary references with all data", async () => {
          await fillInput(page, "Periodikum", "wdg")
          await page
            .getByText("WdG | Welt der Gesundheitsversorgung", {
              exact: true,
            })
            .click()
          await fillInput(page, "Zitatstelle", "2024, 10-12")
          await fillInput(page, "Klammernzusatz", "LT")
          await page.locator("[aria-label='Fundstelle speichern']").click()
          await expect(page.getByText("WdG 2024, 10-12 (LT)")).toBeVisible()
          await expect(
            page.getByText("sekundär", { exact: true }),
          ).toBeVisible()
          await fillInput(page, "Periodikum", "GVBl BB")
          await page
            .getByText(
              "GVBl BB | Gesetz- und Verordnungsblatt für das Land Brandenburg",
              { exact: true },
            )
            .click()
          await fillInput(page, "Zitatstelle", "2020, 01-99")
          await fillInput(page, "Klammernzusatz", "L")
          await page.locator("[aria-label='Fundstelle speichern']").click()
          await expect(page.getByText("GVBl BB 2020, 01-99 (L)")).toBeVisible()
          await expect(page.getByText("primär", { exact: true })).toBeVisible()
        })
        await save(page)

        await test.step("Navigate to handover, click in 'XML-Vorschau', check references are visible in correct order", async () => {
          await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)
          await expect(page.getByText("XML Vorschau")).toBeVisible()
          await page.getByText("XML Vorschau").click()

          const referencesNodes = await page
            .locator('code:has-text("<fundstelle ")')
            .all()
          await expect(referencesNodes[0]).toHaveText(
            '<fundstelle typ="nichtamtlich">',
          )
          await expect(referencesNodes[1]).toHaveText(
            '<fundstelle typ="amtlich">',
          )

          const legalPeriodicalNodes = await page
            .locator('code:has-text("<periodikum>")')
            .all()
          await expect(legalPeriodicalNodes[0]).toHaveText(
            "<periodikum>WdG</periodikum>",
          )
          await expect(legalPeriodicalNodes[1]).toHaveText(
            "<periodikum>GVBl BB</periodikum>",
          )

          const citationNodes = await page
            .locator('code:has-text("<zitstelle>")')
            .all()
          await expect(citationNodes[0]).toHaveText(
            "<zitstelle>2024, 10-12 (LT)</zitstelle>",
          )
          await expect(citationNodes[1]).toHaveText(
            "<zitstelle>2020, 01-99 (L)</zitstelle>",
          )
        })
      },
    )
  },
)
