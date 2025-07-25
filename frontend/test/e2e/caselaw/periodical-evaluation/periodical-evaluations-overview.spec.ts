import { expect } from "@playwright/test"
import dayjs from "dayjs"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  fillInput,
  navigateToPeriodicalEvaluation,
} from "~/e2e/caselaw/utils/e2e-utils"
import { generateString } from "~/test-helper/dataGenerators"

const formattedDate = dayjs().format("DD.MM.YYYY")

/* eslint-disable playwright/no-conditional-in-test */

test.describe(
  "Periodical evaluations overview",
  {
    tag: "@RISDEV-4264",
  },
  () => {
    test("Periodicals overview with a list of editions", async ({
      page,
      edition,
    }) => {
      await test.step("References is a new selectable menu item in the top navigation", async () => {
        await navigateToPeriodicalEvaluation(page)
      })

      await test.step("Initially, an empty table with a hint is shown", async () => {
        await expect(
          page.getByText(
            "Wählen Sie ein Periodikum um die Ausgaben anzuzeigen.",
            {
              exact: true,
            },
          ),
        ).toBeVisible()
        await expect(page.locator(".table > td")).toHaveCount(1) // only header
        await expect(
          page.getByText(
            "Ausgabe" + "Periodikum" + "Anzahl der Fundstellen" + "Hinzugefügt",
          ),
        ).toBeVisible()
        await expect(page.locator(".table > tr")).toHaveCount(0)
      })

      await test.step("The dropdown indicates the periodical type (amtlich/nichtamtlich)", async () => {
        await fillInput(page, "Periodikum", "ABl AHK")
        await expect(
          page.getByText("ABl AHK | ABl AHK" + "amtlich", {
            exact: true,
          }),
        ).toBeVisible()
      })

      await test.step("A periodical can be selected using a combo box.", async () => {
        await fillInput(page, "Periodikum", "MMG")
        const periodical = page.getByText(
          "MMG | Medizin Mensch Gesellschaft" + "nicht amtlich",
          {
            exact: true,
          },
        )
        await expect(periodical).toBeVisible()

        const requestPromise = page.waitForRequest((request) =>
          request.url().includes("api/v1/caselaw/legalperiodicaledition"),
        )
        await periodical.click()

        await requestPromise

        await expect(
          page.getByLabel("Periodikum", { exact: true }),
        ).toHaveValue("MMG")
      })

      await test.step("An existing periodical edition appears in the results", async () => {
        const line = page.locator(`tr:has(td:has-text("${edition.name}"))`)
        await line.waitFor({ state: "visible" })
        await expect(line).toBeVisible()
      })

      await test.step("The table is cleared when filter is deleted", async () => {
        await page.getByLabel("Auswahl zurücksetzen", { exact: true }).click()
        await expect(
          page.getByText(
            "Wählen Sie ein Periodikum um die Ausgaben anzuzeigen.",
            {
              exact: true,
            },
          ),
        ).toBeVisible()
        await expect(page.locator(".table > tr")).toHaveCount(0)
      })

      await test.step("The table is cleared when a periodical without edtions is selected", async () => {
        await fillInput(page, "Periodikum", "ZAU")
        await page
          .getByText("ZAU | Zeitschrift für angewandte Umweltforschung", {
            exact: true,
          })
          .click()
        await expect(
          page.getByLabel("Periodikum", { exact: true }),
        ).toHaveValue("ZAU")
        await expect(
          page.getByText("Keine Suchergebnisse gefunden", { exact: true }),
        ).toBeVisible()
        await expect(page.locator(".table > tr")).toHaveCount(0)
      })

      await test.step("By clicking on an edition, the detail view is opened.", async () => {
        await fillInput(page, "Periodikum", "MMG")

        const response = page.waitForResponse((response) =>
          response.url().includes("api/v1/caselaw/legalperiodicaledition"),
        )
        await page
          .getByText("MMG | Medizin Mensch Gesellschaft", { exact: true })
          .click()
        await response

        const line = page.locator(`tr:has(td:has-text("${edition.name}"))`)
        await line.waitFor({ state: "visible" })
        await expect(line).toBeVisible()

        const pagePromise = page.context().waitForEvent("page")

        await line.locator("a").click()
        const newTab = await pagePromise
        await expect(newTab).toHaveURL(
          `/caselaw/periodical-evaluation/${edition.id}/references`,
        )
      })
    })

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
        await navigateToPeriodicalEvaluation(page)

        await test.step("A legal periodical can be selected", async () => {
          await fillInput(page, "Periodikum", "wdg")
          await page
            .getByText("WdG | Welt der Gesundheitsversorgung", {
              exact: true,
            })
            .click()
        })

        await test.step("A new evaluation is started using the “Neue Periodikumsauswertung button.", async () => {
          const newLegalPeriodicalEvaluation = page.getByLabel(
            "Neue Periodikumsauswertung",
          )
          await expect(newLegalPeriodicalEvaluation).toBeVisible()
          await newLegalPeriodicalEvaluation.click()

          // Wait until the page is fully loaded
          await page.waitForLoadState("load")
          await page.waitForURL(
            /\/caselaw\/periodical-evaluation\/[0-9a-fA-F-]{36}\/edition/,
          )
        })

        await test.step("The inputs are correctly validated (name have to be chosen)", async () => {
          await expect(page.getByText("Pflichtfeld nicht befüllt")).toBeHidden()
          await page.getByLabel("Fortfahren").click()
          await expect(
            page.locator(`text="Pflichtfeld nicht befüllt"`),
          ).toHaveCount(1)
        })

        const name = generateString()

        await test.step("Prefix, suffix and name can be set", async () => {
          await fillInput(page, "Präfix", "präfix")
          await fillInput(page, "Suffix", "suffix")
          await fillInput(page, "Name der Ausgabe", name)
          await expect(page.getByLabel("Präfix")).toHaveValue("präfix")
          await expect(page.getByLabel("Suffix")).toHaveValue("suffix")
          await expect(page.getByLabel("Name der Ausgabe")).toHaveValue(name)
        })

        try {
          await test.step("'Übernehmen und Fortfahren' saved the edition and replaces url with new edition id", async () => {
            const requestPromise = page.waitForRequest((request) =>
              request.url().includes("api/v1/caselaw/legalperiodicaledition"),
            )
            await page.getByLabel("Übernehmen und Fortfahren").click()
            await requestPromise

            await page.waitForURL(
              /\/caselaw\/periodical-evaluation\/[0-9a-fA-F-]{36}\/references/,
            )

            await expect(
              page.getByText("Periodikumsauswertung | WdG " + name, {
                exact: true,
              }),
            ).toBeVisible()
          })

          await test.step("The edition can be deleted", async () => {
            await navigateToPeriodicalEvaluation(page)
            await fillInput(page, "Periodikum", "wdg")
            await page
              .getByText("WdG | Welt der Gesundheitsversorgung", {
                exact: true,
              })
              .click()
            await expect(
              page.getByLabel("Periodikum", { exact: true }),
            ).toHaveValue("WdG")
            await expect(page.locator(".table > tr >> nth=0")).toBeVisible()
            const line = page.getByText(name + "WdG0" + formattedDate)

            await line.getByLabel("Ausgabe löschen", { exact: true }).click()
            await expect(
              page.getByText(name + "WdG0" + formattedDate),
            ).toBeHidden()
            await page.reload()
            await expect(
              page.getByTestId("periodical-evaluation-title"),
            ).toBeVisible()
            await expect(
              page.getByText(name + "WdG0" + formattedDate),
            ).toBeHidden()
          })

          // make sure the edition is deleted also if the test fails
        } finally {
          await navigateToPeriodicalEvaluation(page)
          await fillInput(page, "Periodikum", "wdg")
          await page
            .getByText("WdG | Welt der Gesundheitsversorgung", { exact: true })
            .click()
          if (await page.locator(".table > tr >> nth=0").isVisible()) {
            const line = page.getByText(name + "WdG0" + formattedDate)
            await line.getByLabel("Ausgabe löschen", { exact: true }).click()
          }
        }
      },
    )

    test("An edition can't be deleted as long as it has references", async ({
      page,
      editionWithReferences,
    }) => {
      await navigateToPeriodicalEvaluation(page)

      await fillInput(page, "Periodikum", "MMG")

      const response = page.waitForResponse((response) =>
        response.url().includes("api/v1/caselaw/legalperiodicaledition"),
      )
      await page
        .getByText("MMG | Medizin Mensch Gesellschaft", { exact: true })
        .click()
      await response

      const line = page.locator(
        `tr:has(td:has-text("${editionWithReferences.name}"))`,
      )
      await line.waitFor({ state: "visible" })
      await expect(line).toBeVisible()
      // delete button should not be clickable
      await expect(
        line.getByLabel("Ausgabe löschen", { exact: true }),
      ).toBeHidden()

      await expect(
        line
          .getByLabel("Ausgabe kann nicht gelöscht werden", { exact: true })
          .first(),
      ).toBeVisible()
    })
  },
)
