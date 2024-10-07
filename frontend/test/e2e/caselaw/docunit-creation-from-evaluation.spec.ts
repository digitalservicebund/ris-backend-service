import { expect, Page } from "@playwright/test"
import dayjs from "dayjs"
import {
  fillInput,
  navigateToPeriodicalReferences,
  waitForInputValue,
} from "./e2e-utils"
import { caselawTest as test } from "./fixtures"

const formattedDate = dayjs().format("DD.MM.YYYY")

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
          await page.getByText("Suchen").click()
          await waitForInputValue(
            page,
            "[aria-label='Zuständige Dokumentationsstelle']",
            "BGH",
          )

          // Verwaltungsgericht Aarau is a BVerwG court
          await fillInput(page, "Gericht", "Verwaltungsgericht Aarau")
          await page.getByText("Verwaltungsgericht Aarau").click()
          await page.getByText("Suchen").click()
          await waitForInputValue(
            page,
            "[aria-label='Zuständige Dokumentationsstelle']",
            "BVerwG",
          )
        })

        await test.step("Foregn courts are not assigned to a responsible doc office", async () => {
          await fillInput(page, "Gericht", "Arbeits- und Sozialgericht Wien")
          await page.getByText("Arbeits- und Sozialgericht Wien").click()
          await page.getByText("Suchen").click()
          await waitForInputValue(
            page,
            "[aria-label='Zuständige Dokumentationsstelle']",
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

          await fillInput(page, "Zuständige Dokumentationsstelle", "bv")
          await waitForInputValue(
            page,
            "[aria-label='Zuständige Dokumentationsstelle']",
            "bv",
          )

          await expect(page.getByText("BAG")).toBeHidden()
          await expect(page.getByText("BFH")).toBeHidden()
          await expect(page.getByText("BVerwG")).toBeVisible()
          await expect(page.getByText("BVerfG")).toBeVisible()

          await page.getByText("BVerfG").click()

          await waitForInputValue(
            page,
            "[aria-label='Zuständige Dokumentationsstelle']",
            "BVerfG",
          )
        })
      },
    )

    // TODO cleanup after test is missing
    // eslint-disable-next-line playwright/no-skipped-test
    test.skip(
      "Allow creation from periodical evaluation",
      {
        tag: "@RISDEV-4829",
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4829",
        },
      },
      async ({ page, edition }) => {
        await navigateToPeriodicalReferences(page, edition.id ?? "")

        await test.step("After searching, a documentation unit can be created", async () => {
          await expect(
            page.getByText("Zuständige Dokumentationsstelle *"),
          ).toBeHidden()
          await searchForDocUnitWithFileNumber(page, "1C 123/45", formattedDate)
          await expect(
            page.getByText("Zuständige Dokumentationsstelle *"),
          ).toBeVisible()
        })

        await test.step("Mandatory fields citation (Zitatstelle), reference Supplement (Klammernzusatz) and documentation office are being validated before creation of new documentation unit", async () => {
          await expect(
            page.getByText("Ok und Dokumentationseinheit direkt bearbeiten"),
          ).toBeDisabled()

          await fillInput(page, "Gericht", "AG Aachen")
          await page.getByText("AG Aachen").click()
          await fillInput(page, "Zuständige Dokumentationsstelle", "DS")
          await page.getByText("DS", { exact: true }).click()
          await waitForInputValue(
            page,
            "[aria-label='Zuständige Dokumentationsstelle']",
            "DS",
          )
          await expect(
            page.getByText("Ok und Dokumentationseinheit direkt bearbeiten"),
          ).toBeEnabled()

          await page
            .getByText("Ok und Dokumentationseinheit direkt bearbeiten")
            .click()

          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(1)

          await expect(page.getByText("Pflichtfeld nicht befüllt")).toHaveCount(
            2,
          )

          await fillInput(page, "Zitatstelle *", "12")
          await expect(page.getByText("Pflichtfeld nicht befüllt")).toHaveCount(
            1,
          )

          await page
            .getByText("Ok und Dokumentationseinheit direkt bearbeiten")
            .click()

          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(1)

          await fillInput(page, "Klammernzusatz", "L")

          await expect(page.getByText("Pflichtfeld nicht befüllt")).toBeHidden()
        })

        const newtab =
          await test.step("The new documentation unit can be created and opened in a new tab", async () => {
            const pagePromise = page.context().waitForEvent("page")
            await page
              .getByText("Ok und Dokumentationseinheit direkt bearbeiten")
              .click()
            const newTab = await pagePromise
            await expect(newTab).toHaveURL(
              /\/caselaw\/documentunit\/[A-Z0-9]{13}\/categories$/,
            )
            return newTab
          })

        await test.step("The new documentation unit is added to the list of references", async () => {
          await expect(
            page.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(2)

          await expect(
            page.getByText(
              "BGH, " + formattedDate + ", 1C 123/45, Anerkenntnisurteil",
            ),
          ).toBeVisible()

          await expect(
            page.getByText(
              edition.legalPeriodical?.abbreviation +
                " " +
                edition.prefix +
                "12" +
                edition.suffix +
                " (L)",
              { exact: true },
            ),
          ).toBeVisible()
        })

        await test.step("The new documentation unit is created with search input fields court, decision date, file number and document type", async () => {
          await expect(newtab.locator("[aria-label='Gericht']")).toHaveValue(
            "BGH",
          )
          await expect(
            newtab.locator("[aria-label='Entscheidungsdatum']"),
          ).toHaveValue(formattedDate)
          await expect(
            newtab.locator("[aria-label='Aktenzeichen']"),
          ).toHaveValue("1C 123/45")
          await expect(
            newtab.locator("[aria-label='Dokumenttyp']"),
          ).toHaveValue("AnU")
        })

        await test.step("Legal effect (Rechtskraft) is initialized based on the court", async () => {
          await expect(
            newtab.locator("[aria-label='Rechtskraft']"),
          ).toHaveValue("Ja")
        })

        await test.step("Legal effect (Rechtskraft) is set to unknown without court", async () => {
          await fillInput(page, "Zitatstelle *", "18")
          await fillInput(page, "Klammernzusatz", "L")
          await page.getByText("Suchen").click()
          await fillInput(page, "Zuständige Dokumentationsstelle", "DS")
          await page.getByText("DS", { exact: true }).click()

          const pagePromise = page.context().waitForEvent("page")
          await page
            .getByText("Ok und Dokumentationseinheit direkt bearbeiten")
            .click()
          const secondTab = await pagePromise
          await expect(secondTab).toHaveURL(
            /\/caselaw\/documentunit\/[A-Z0-9]{13}\/categories$/,
          )
          await expect(
            secondTab.locator("[aria-label='Rechtskraft']"),
          ).toHaveValue("Keine Angabe")
        })

        await test.step("The new documentation unit has the status unpublished", async () => {
          await expect(newtab.getByText("Unveröffentlicht")).toBeVisible()

          await expect(
            page.getByText(
              "BGH, " +
                formattedDate +
                ", 1C 123/45, Anerkenntnisurteil, Unveröffentlicht",
            ),
          ).toBeVisible()
        })

        await test.step("The reference is visible in the documentation unit edit view and preview", async () => {
          await newtab.keyboard.down("v")

          await expect(page.getByText("Sekundäre Fundstellen")).toBeVisible()
          await expect(
            newtab.getByText(
              edition.legalPeriodical?.abbreviation +
                " " +
                edition.prefix +
                "12" +
                edition.suffix +
                " (L)",
              { exact: true },
            ),
          ).toBeVisible()

          await newtab.locator("[aria-label='Fundstellen']").click()
          await expect(page.getByText("Fundstellen bearbeiten")).toBeVisible()
          await expect(
            newtab.getByText(
              edition.legalPeriodical?.abbreviation +
                " " +
                edition.prefix +
                "12" +
                edition.suffix +
                " (L)",
              { exact: true },
            ),
          ).toBeVisible()
        })
      },
    )

    async function searchForDocUnitWithFileNumber(
      page: Page,
      fileNumber: string,
      date: string,
      court?: string,
    ) {
      if (court) {
        await fillInput(page, "Gericht", court)
        await page.getByText(court, { exact: true }).click()
      }
      await fillInput(page, "Aktenzeichen", fileNumber)
      await fillInput(page, "Entscheidungsdatum", date)
      await fillInput(page, "Dokumenttyp", "AnU")
      await page.getByText("Anerkenntnisurteil", { exact: true }).click()

      await page.getByText("Suchen").click()
    }
  },
)
