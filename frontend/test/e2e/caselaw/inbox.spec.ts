import { expect, Page } from "@playwright/test"
import dayjs from "dayjs"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"

import {
  fillInput,
  navigateToInbox,
  navigateToPeriodicalReferences,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

test.describe("inbox", () => {
  test(
    "pending handover",
    { tag: ["@RISDEV-7374"] },
    async ({ page, pageWithBghUser, edition }) => {
      const today = dayjs().format("DD.MM.YYYY")

      let documentNumber1 = ""
      let documentNumber2 = ""

      await test.step("create pending handover documentation units for docoffice", async () => {
        documentNumber1 = await createPendingHandoverDecisionForBGH(
          page,
          edition,
          "12",
          "AG Aachen",
          dayjs("2025-01-01").format("DD.MM.YYYY"),
          "fileNumber1",
          "AnU",
        )

        documentNumber2 = await createPendingHandoverDecisionForBGH(
          page,
          edition,
          "13",
          "AG Aalen",
          dayjs("2001-01-01").format("DD.MM.YYYY"),
          "fileNumber2",
          "Bes",
        )
      })

      await test.step("pending handover inbox can be accessed via top navbar", async () => {
        const tab = pageWithBghUser.getByTestId("external-handover-tab")
        await navigateToInbox(pageWithBghUser)
        await expect(tab).toBeVisible()
        await tab.click()
        await expect(pageWithBghUser.getByTestId("inbox-list")).toBeVisible()
      })

      await test.step("shows all pending handover documentation units for docoffice", async () => {
        const rows = pageWithBghUser.locator("tr")
        const rowWithDocNumber1 = rows.filter({ hasText: documentNumber1 })
        await expect(rowWithDocNumber1).toHaveCount(1)
        await expect(rowWithDocNumber1).toContainText(documentNumber1)

        // Icons
        await expect(
          rowWithDocNumber1.locator('[data-testid="file-attached-icon"]'),
        ).toBeVisible()
        await expect(
          rowWithDocNumber1.getByTestId("headnote-principle-icon"),
        ).toBeVisible()
        await expect(rowWithDocNumber1.getByTestId("note-icon")).toBeVisible()
        await expect(
          rowWithDocNumber1.getByTestId("scheduling-icon"),
        ).toBeVisible()

        await expect(rowWithDocNumber1).toContainText("AG") // Gerichtstyp
        await expect(rowWithDocNumber1).toContainText("Aachen") // Gerichtsort
        await expect(rowWithDocNumber1).toContainText("01.01.2025") // Datum
        await expect(rowWithDocNumber1).toContainText("fileNumber1") // Aktenzeichen
        await expect(rowWithDocNumber1).toContainText(
          `${edition.legalPeriodical?.abbreviation} ${edition.prefix}12${edition.suffix} (DS)`,
        ) // Quelle
        await expect(rowWithDocNumber1).toContainText(today) // Angelegt am
        await expect(rowWithDocNumber1).toContainText("Fremdanlage") // Status
      })

      await test.step("sorted by decision date, latest first", async () => {
        const rows = pageWithBghUser.locator("tr")
        const regex = new RegExp(`${documentNumber1}|${documentNumber2}`)

        const matchingRows = rows.filter({ hasText: regex })

        await expect(matchingRows).toHaveCount(2)

        const firstRowText = await matchingRows.nth(0).textContent()
        const secondRowText = await matchingRows.nth(1).textContent()

        // Assert documentNumber1 comes before documentNumber2
        expect(firstRowText).toContain(documentNumber1)
        expect(secondRowText).toContain(documentNumber2)
      })

      await test.step("user can filter pending handover documentation units", async () => {
        const tab = pageWithBghUser.getByTestId("pending-handover-inbox")
        const fileNumberInput = tab.getByLabel("Aktenzeichen Suche")
        const courtType = tab.getByLabel("Gerichtstyp Suche")
        const courtLocation = tab.getByLabel("Gerichtsort Suche")
        const docNumber = tab.getByLabel("Dokumentnummer Suche")
        const decisionDate = tab.getByLabel("Entscheidungsdatum Suche", {
          exact: true,
        })
        const decisionDateEnd = tab.getByLabel(
          "Entscheidungsdatum Suche Ende",
          {
            exact: true,
          },
        )

        await expect(fileNumberInput).toBeVisible()
        await expect(courtType).toBeVisible()
        await expect(courtLocation).toBeVisible()
        await expect(docNumber).toBeVisible()
        await expect(decisionDate).toBeVisible()
        await expect(decisionDateEnd).toBeVisible()

        // search input filters only one docunit
        await fileNumberInput.fill("fileNumber1")
        await courtType.fill("AG")
        await courtLocation.fill("Aachen")
        await docNumber.fill(documentNumber1)
        await decisionDate.fill("01.01.2025")

        await pageWithBghUser
          .getByTestId("pending-handover-inbox")
          .getByLabel("Nach Dokumentationseinheiten suchen")
          .click()
        await expect(
          pageWithBghUser.getByText("Fremdanlage", { exact: true }),
        ).toHaveCount(1)
      })

      await test.step("switching tabs, resets the search form", async () => {
        await pageWithBghUser
          .getByRole("tab", { name: "EU-Rechtsprechung" })
          .click()

        // Wait for queries in url to be resetted
        await pageWithBghUser.waitForFunction(() => {
          return !window.location.href.includes("fileNumber1")
        })
        await pageWithBghUser.getByRole("tab", { name: "Fremdanlagen" }).click()

        await expect(
          pageWithBghUser.getByLabel("Aktenzeichen Suche"),
        ).toHaveValue("")
        await expect(
          pageWithBghUser.getByLabel("Gerichtstyp Suche"),
        ).toHaveValue("")
        await expect(
          pageWithBghUser.getByLabel("Gerichtsort Suche"),
        ).toHaveValue("")
        await expect(
          pageWithBghUser.getByLabel("Dokumentnummer Suche"),
        ).toHaveValue("")
        await expect(
          pageWithBghUser
            .getByTestId("decision-date-input")
            .getByLabel("Entscheidungsdatum Suche"),
        ).toHaveValue("")
      })

      await test.step("pending handover documentation units can be taken over and deleted", async () => {
        const rows = pageWithBghUser.locator("tr")
        const doc1Row = rows.filter({ hasText: documentNumber1 })
        const doc2Row = rows.filter({ hasText: documentNumber2 })

        const takeOverButton = doc1Row.getByRole("button", {
          name: "Dokumentationseinheit übernehmen",
        })
        const editButton = doc1Row.getByRole("button", {
          name: "Dokumentationseinheit bearbeiten",
        })
        await expect(takeOverButton).toBeVisible()
        await expect(editButton).toBeHidden()

        await takeOverButton.click()

        await expect(takeOverButton).toBeHidden()
        await expect(editButton).toBeVisible()

        // after takeover, the status is unpublished, docunit can be deleted

        await expect(doc1Row).toContainText("Unveröffentlicht")

        await doc1Row
          .getByRole("button", {
            name: "Dokumentationseinheit löschen",
          })
          .click()

        await pageWithBghUser
          .getByRole("button", { name: "Löschen", exact: true })
          .click()

        await expect(doc1Row).toBeHidden()

        // docunits can be deleted without takeover

        await doc2Row
          .getByRole("button", {
            name: "Dokumentationseinheit löschen",
          })
          .click()

        await pageWithBghUser
          .getByRole("button", { name: "Löschen", exact: true })
          .click()

        await expect(doc2Row).toBeHidden()
      })
    },
  )

  test("eu caselaw", { tag: ["@RISDEV-7375"] }, async ({ page }) => {
    const documentNumber = "YYTestDoc0012"

    await test.step("eu caselaw inbox can be accessed via top navbar", async () => {
      const tab = page.getByTestId("eu-tab")
      await navigateToInbox(page)
      await expect(tab).toBeVisible()
      await tab.click()
      await expect(page.getByTestId("inbox-list")).toBeVisible()
    })

    await test.step("shows all eu documentation units for docoffice", async () => {
      const rows = page.locator("tr")
      const row = rows.filter({ hasText: documentNumber })
      await expect(row).toHaveCount(1)

      // Icons
      await expect(
        row.locator('[data-testid="file-attached-icon"]'),
      ).toBeVisible()
      await expect(row.getByTestId("headnote-principle-icon")).toBeVisible()
      await expect(row.getByTestId("note-icon")).toBeVisible()
      await expect(row.getByTestId("scheduling-icon")).toBeVisible()

      await expect(row).toContainText("BVerwG") // Gerichtstyp
      await expect(row).toContainText("09.09.1987") // Datum
      await expect(row).toContainText("fileNumber4") // Aktenzeichen
      await expect(row).toContainText("Veröffentlicht") // Status
    })

    await test.step("user can filter eu documentation units", async () => {
      const tab = page.getByTestId("eu-inbox")
      const fileNumberInput = tab.getByLabel("Aktenzeichen Suche")
      const courtType = tab.getByLabel("Gerichtstyp Suche")
      const courtLocation = tab.getByLabel("Gerichtsort Suche")
      const docNumber = tab.getByLabel("Dokumentnummer Suche")
      const decisionDate = tab.getByLabel("Entscheidungsdatum Suche", {
        exact: true,
      })
      const decisionDateEnd = tab.getByLabel("Entscheidungsdatum Suche Ende", {
        exact: true,
      })

      await expect(fileNumberInput).toBeVisible()
      await expect(courtType).toBeVisible()
      await expect(courtLocation).toBeVisible()
      await expect(docNumber).toBeVisible()
      await expect(decisionDate).toBeVisible()
      await expect(decisionDateEnd).toBeVisible()

      // search input filters only one docunit
      await fileNumberInput.fill("fileNumber4")
      await courtType.fill("BVerwG")
      await courtLocation.fill("")
      await docNumber.fill("YYTestDoc0012")
      await decisionDate.fill("09.09.1987")

      await page
        .getByTestId("eu-inbox")
        .getByLabel("Nach Dokumentationseinheiten suchen")
        .click()

      // header row and result row
      await expect(page.locator("tr")).toHaveCount(2)
    })

    await test.step("switching tabs, resets the search form", async () => {
      await page.getByRole("tab", { name: "Fremdanlagen" }).click()

      // Wait for queries in url to be resetted
      await page.waitForFunction(() => {
        return !window.location.href.includes("fileNumber1")
      })
      await page.getByRole("tab", { name: "EU-Rechtsprechung" }).click()

      await expect(page.getByLabel("Aktenzeichen Suche")).toHaveValue("")
      await expect(page.getByLabel("Gerichtstyp Suche")).toHaveValue("")
      await expect(page.getByLabel("Gerichtsort Suche")).toHaveValue("")
      await expect(page.getByLabel("Dokumentnummer Suche")).toHaveValue("")
      await expect(
        page
          .getByTestId("decision-date-input")
          .getByLabel("Entscheidungsdatum Suche"),
      ).toHaveValue("")
    })

    await test.step("eu documentation units can be edited and deleted", async () => {
      const pagePromise = page.context().waitForEvent("page")
      const rows = page.locator("tr")
      const row = rows.filter({ hasText: documentNumber })
      const editButton = row.getByRole("button", {
        name: "Dokumentationseinheit bearbeiten",
      })

      await editButton.click()

      const newTab = await pagePromise

      await expect(newTab).toHaveURL(
        /\/caselaw\/documentunit\/[A-Za-z0-9]{13}\/categories$/,
      )

      await row
        .getByRole("button", {
          name: "Dokumentationseinheit löschen",
        })
        .click()
    })
  })
})

async function createPendingHandoverDecisionForBGH(
  page: Page,
  edition: LegalPeriodicalEdition,
  citation: string,
  court: string,
  date: string,
  fileNumber: string,
  doctype: string,
) {
  await navigateToPeriodicalReferences(page, edition.id ?? "")
  const addReferenceButton = page.getByLabel("Weitere Angabe")

  if (await addReferenceButton.isVisible()) {
    await addReferenceButton.click()
  }

  await fillInput(page, "Zitatstelle *", citation)
  await fillInput(page, "Klammernzusatz", "L")
  await searchForDocUnit(page, court, date, fileNumber, doctype)

  await expect(page.getByText("Übernehmen und weiter bearbeiten")).toBeVisible()

  await expect(page.getByLabel("Zuständige Dokumentationsstelle")).toHaveValue(
    "BGH",
  )

  const pagePromise = page.context().waitForEvent("page")
  await page.getByText("Übernehmen und weiter bearbeiten").click()
  const newTab = await pagePromise
  await expect(newTab).toHaveURL(
    /\/caselaw\/documentunit\/[A-Z0-9]{13}\/categories$/,
  )
  const documentNumber = /caselaw\/documentunit\/(.*)\/categories/g.exec(
    newTab.url(),
  )?.[1] as string
  return documentNumber
}

async function searchForDocUnit(
  page: Page,
  court?: string,
  date?: string,
  fileNumber?: string,
  documentType?: string,
) {
  if (fileNumber) {
    await fillInput(page, "Aktenzeichen", fileNumber)
  }
  if (court) {
    await fillInput(page, "Gericht", court)
    await page.getByText(court, { exact: true }).click()
  }
  if (date) {
    await fillInput(page, "Entscheidungsdatum", date)
  }
  if (documentType) {
    await fillInput(page, "Dokumenttyp", documentType)
    await page.getByText("Anerkenntnisurteil", { exact: true }).click()
  }

  await page.getByText("Suchen").click()
}
