import { expect, Page } from "@playwright/test"
import dayjs from "dayjs"
import {
  fillInput,
  navigateToPeriodicalReferences,
  waitForInputValue,
  deleteDocumentUnit,
} from "./e2e-utils"
import { caselawTest as test } from "./fixtures"
import { generateString } from "~/test-helper/dataGenerators"

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

        await test.step("After searching, a documentation unit can be created", async () => {
          await expect(
            page.getByText("Dokumentationsstelle zuweisen *"),
          ).toBeHidden()
          await page.getByText("Suchen").click()
          await expect(
            page.getByText("Dokumentationsstelle zuweisen *"),
          ).toBeVisible()
        })

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

        await test.step("Foreign courts are not assigned to a responsible doc office", async () => {
          await fillInput(page, "Gericht", "Arbeits- und Sozialgericht Wien")
          await page.getByText("Arbeits- und Sozialgericht Wien").click()
          await page.getByText("Suchen").click()
          await waitForInputValue(
            page,
            "[aria-label='Zuständige Dokumentationsstelle']",
            "",
          )
        })

        await test.step("Documentation Office is a mandatory field for doc unit creation", async () => {
          await expect(
            page.getByText("Übernehmen und weiter bearbeiten"),
          ).toBeDisabled()
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

          await expect(
            page.getByText("Übernehmen und weiter bearbeiten"),
          ).toBeEnabled()
        })
      },
    )

    test(
      "Allow creation from periodical evaluation for own docunit",
      {
        tag: "@RISDEV-4829",
        annotation: {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4829",
        },
      },
      async ({ pageWithBghUser, edition }) => {
        await navigateToPeriodicalReferences(pageWithBghUser, edition.id ?? "")
        const randomFileNumber = generateString()
        let documentNumber = ""
        await searchForDocUnitWithFileNumber(
          pageWithBghUser,
          randomFileNumber,
          formattedDate,
          "AG Aachen",
        )

        await test.step("Mandatory fields citation (Zitatstelle) and reference Supplement (Klammernzusatz) are being validated before creation of new documentation unit", async () => {
          await pageWithBghUser
            .getByText("Übernehmen und weiter bearbeiten")
            .click()

          await expect(
            pageWithBghUser.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(1)

          await expect(
            pageWithBghUser.getByText("Pflichtfeld nicht befüllt"),
          ).toHaveCount(2)

          await fillInput(pageWithBghUser, "Zitatstelle *", "12")
          await expect(
            pageWithBghUser.getByText("Pflichtfeld nicht befüllt"),
          ).toHaveCount(1)

          await pageWithBghUser
            .getByText("Übernehmen und weiter bearbeiten")
            .click()

          await expect(
            pageWithBghUser.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(1)

          await fillInput(pageWithBghUser, "Klammernzusatz", "L")

          await expect(
            pageWithBghUser.getByText("Pflichtfeld nicht befüllt"),
          ).toBeHidden()
        })

        await test.step("The new documentation unit can be created and opened in a new tab with correct data", async () => {
          const pagePromise = pageWithBghUser.context().waitForEvent("page")
          await pageWithBghUser
            .getByText("Übernehmen und weiter bearbeiten")
            .click()
          const newTab = await pagePromise
          await expect(newTab).toHaveURL(
            /\/caselaw\/documentunit\/[A-Z0-9]{13}\/categories$/,
          )
          documentNumber = /caselaw\/documentunit\/(.*)\/categories/g.exec(
            newTab.url(),
          )?.[1] as string
          await expect(newTab.locator("[aria-label='Gericht']")).toHaveValue(
            "AG Aachen",
          )
          await expect(
            newTab.locator("[aria-label='Entscheidungsdatum']"),
          ).toHaveValue(formattedDate)
          await expect(newTab.getByTestId("chip-value")).toHaveText(
            randomFileNumber,
          )
          await expect(
            newTab.locator("[aria-label='Dokumenttyp']"),
          ).toHaveValue("Anerkenntnisurteil")

          // Todo: RISDEV-4999
          // await expect(
          //   newTab.locator("[aria-label='Rechtskraft']"),
          // ).toHaveValue("Ja")

          await newTab.keyboard.down("v")

          await expect(newTab.getByText("Sekundäre Fundstellen")).toBeVisible()
          await expect(
            newTab.getByText(
              edition.legalPeriodical?.abbreviation +
                " " +
                edition.prefix +
                "12" +
                edition.suffix +
                " (L)",
              { exact: true },
            ),
          ).toBeVisible()

          await newTab.locator("[aria-label='Fundstellen']").click()
          await expect(newTab.getByText("Fundstellen bearbeiten")).toBeVisible()
          await expect(
            newTab
              .getByLabel("Listen Eintrag")
              .getByText(
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

        await test.step("The new documentation unit is added to the list of references", async () => {
          await expect(
            pageWithBghUser.locator("[aria-label='Listen Eintrag']"),
          ).toHaveCount(2)

          await expect(
            pageWithBghUser.getByText(
              "AG Aachen, " +
                formattedDate +
                ", " +
                randomFileNumber +
                ", Anerkenntnisurteil, Unveröffentlicht",
            ),
          ).toBeVisible()

          await expect(
            pageWithBghUser.getByText(
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

        await deleteDocumentUnit(pageWithBghUser, documentNumber)
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
