import { expect } from "@playwright/test"
import {
  fillInput,
  waitForInputValue,
  navigateToPeriodicalEvaluation,
} from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

test.describe(
  "periodical evaluation",
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
      "Periodicals overview with a list of editions",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4497",
        },
      },
      async ({ page }) => {
        await test.step("References is a new selectable menu item in the top navigation", async () => {
          await navigateToPeriodicalEvaluation(page)
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
      },
    )

    test(
      "New periodical evaluation",
      {
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4499",
        },
      },
      async ({ page }) => {
        await test.step("A new evaluation is started using the “Neue Periodikaauswertung” button.", async () => {
          await navigateToPeriodicalEvaluation(page)
          await expect(
            page.getByLabel("Neue Periodikaauswertung"),
          ).toBeVisible()
          await page.getByLabel("Neue Periodikaauswertung").click()

          await expect(page).toHaveURL(/periodical-evaluation\/new/)
        })

        await test.step("A legal periodical can be selected", async () => {
          await fillInput(page, "Periodikum", "wdg")
          await page
            .getByText("WdG | Welt der Gesundheitsversorgung", {
              exact: true,
            })
            .click()
        })
        await test.step("Add prefix and suffix can be chosen", async () => {
          await fillInput(page, "Präfix", "präfix")
          await fillInput(page, "Suffix", "suffix")
          await expect(page.getByLabel("Präfix")).toHaveValue("präfix")
          await expect(page.getByLabel("Suffix")).toHaveValue("suffix")
        })

        await test.step("A name can be chosen", async () => {
          await fillInput(page, "Name der Ausgabe", "name")
          await expect(page.getByLabel("Name der Ausgabe")).toHaveValue("name")
        })

        await test.step("'Auswertung starten' saved the edition and replaces url with new edition id", async () => {
          await page.getByLabel("Auswertung starten").click()
          await expect(page).toHaveURL(
            /\/caselaw\/periodical-evaluation\/[0-9a-fA-F\-]{36}/,
          )
        })
      },
    )
  },
)
