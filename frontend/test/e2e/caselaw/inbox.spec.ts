import { expect } from "@playwright/test"
import dayjs from "dayjs"
import {
  navigateToInbox,
  createPendingHandoverDecisionForBGH,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { deleteDocumentUnit } from "~/e2e/caselaw/utils/documentation-unit-api-util"
import { generateString } from "~/test-helper/dataGenerators"

test.describe("inbox", () => {
  test(
    "Fremdanlagen können im Eingang eingesehen und weiterverarbeitet werden",
    { tag: ["@RISDEV-7374"] },
    async ({ page, pageWithBghUser, edition, browserName }) => {
      const today = dayjs().format("DD.MM.YYYY")

      let documentNumber1 = ""
      let documentNumber2 = ""
      const fileNumber1 = generateString({ length: 10 })
      const fileNumber2 = generateString({ length: 10 })

      await test.step("create pending handover documentation units for docoffice", async () => {
        documentNumber1 = await createPendingHandoverDecisionForBGH(
          page,
          edition,
          "12",
          "AG Aachen",
          dayjs("2025-01-01").format("DD.MM.YYYY"),
          fileNumber1,
          "AnU",
        )

        // Todo: Known error in firefox (NS_BINDING_ABORTED),
        // when navigating with a concurrent navigation triggered
        // eslint-disable-next-line playwright/no-wait-for-timeout,playwright/no-conditional-in-test
        if (browserName === "firefox") await page.waitForTimeout(500)

        documentNumber2 = await createPendingHandoverDecisionForBGH(
          page,
          edition,
          "13",
          "AG Aalen",
          dayjs("2001-01-01").format("DD.MM.YYYY"),
          fileNumber2,
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

        await expect(rowWithDocNumber1).toContainText("AG") // Gerichtstyp
        await expect(rowWithDocNumber1).toContainText("Aachen") // Gerichtsort
        await expect(rowWithDocNumber1).toContainText("01.01.2025") // Datum
        await expect(rowWithDocNumber1).toContainText(fileNumber1) // Aktenzeichen
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
        await fileNumberInput.fill(fileNumber1)
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

      await test.step("switching tabs, resets the search form, updates search parameters", async () => {
        await pageWithBghUser.waitForFunction((fileNumber) => {
          return window.location.href.includes(fileNumber)
        }, fileNumber1)

        await pageWithBghUser
          .getByRole("tab", { name: "EU-Rechtsprechung" })
          .click()

        // Wait for the URL tab param to update
        await pageWithBghUser.waitForFunction(() => {
          return window.location.href.includes("eu-rechtsprechung")
        })

        // Wait for fileNumber query not to be visible anymore
        await pageWithBghUser.waitForFunction((fileNumber) => {
          return !window.location.href.includes(fileNumber)
        }, fileNumber1)

        await pageWithBghUser.getByRole("tab", { name: "Fremdanlagen" }).click()

        // Wait for the URL tab param to update
        await pageWithBghUser.waitForFunction(() => {
          return window.location.href.includes("fremdanlagen")
        })

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

  test(
    "Angenommene Fremdanlagen können einem Vorgang zugewiesen werden",
    { tag: ["@RISDEV-7374"] },
    async ({ page, pageWithBghUser, edition, browserName }) => {
      const fileNumber = generateString({ length: 10 })

      let docNumber1 = ""
      let docNumber2 = ""

      await test.step("Erstelle zwei Fremdanlagen für BGH", async () => {
        docNumber1 = await createPendingHandoverDecisionForBGH(
          page,
          edition,
          "12",
          "AG Aachen",
          dayjs("2025-01-01").format("DD.MM.YYYY"),
          fileNumber,
          "AnU",
        )

        // Todo: Known error in firefox (NS_BINDING_ABORTED),
        // when navigating with a concurrent navigation triggered
        // eslint-disable-next-line playwright/no-wait-for-timeout,playwright/no-conditional-in-test
        if (browserName === "firefox") await page.waitForTimeout(500)

        docNumber2 = await createPendingHandoverDecisionForBGH(
          page,
          edition,
          "12",
          "AG Aachen",
          dayjs("2025-01-01").format("DD.MM.YYYY"),
          fileNumber,
          "AnU",
        )
      })

      await test.step("Suche nach Aktenzeichen der Fremdanlagen", async () => {
        await navigateToInbox(pageWithBghUser)
        const fileNumberInput = pageWithBghUser.getByLabel("Aktenzeichen Suche")
        await fileNumberInput.fill(fileNumber)
        await pageWithBghUser
          .getByTestId("pending-handover-inbox")
          .getByLabel("Nach Dokumentationseinheiten suchen")
          .click()
        // 2 results + 1 header
        await expect(pageWithBghUser.getByRole("row")).toHaveCount(2 + 1)
      })

      await test.step("Nimm eine Fremdanlage an", async () => {
        const rows = pageWithBghUser.locator("tr")
        const doc1Row = rows.filter({ hasText: docNumber1 })

        const takeOverButton = doc1Row.getByRole("button", {
          name: "Dokumentationseinheit übernehmen",
        })
        await takeOverButton.click()
        await expect(doc1Row.getByText("Unveröffentlicht")).toBeVisible()
      })

      await test.step("Wähle Vorgang aus", async () => {
        const procedureName = generateString({ length: 10 })
        await pageWithBghUser
          .getByRole("textbox", { name: "Vorgang auswählen" })
          .fill(procedureName)
        await pageWithBghUser
          .getByText(`${procedureName} neu erstellen`)
          .click()
      })

      await test.step("Wähle alle Dokumentationseinheiten aus", async () => {
        await pageWithBghUser
          .getByRole("checkbox", {
            name: "Alle Elemente abgewählt",
          })
          .click()
        await expect(
          pageWithBghUser.getByRole("checkbox", {
            name: "Alle Elemente ausgewählt",
          }),
        ).toBeChecked()
      })

      await test.step("Weise den Zugang zu und erhalte einen Fehler wegen der offenen Fremdanlage", async () => {
        await pageWithBghUser
          .getByRole("button", { name: "Zu Vorgang hinzufügen" })
          .click()
        await expect(
          pageWithBghUser.getByText(
            "Nehmen Sie die Fremdanlage an, um sie zu einem Vorgang hinzuzufügen",
          ),
        ).toBeVisible()
      })

      await test.step("Nimm die zweite Fremdanlage an", async () => {
        const rows = pageWithBghUser.locator("tr")
        const doc2Row = rows.filter({ hasText: docNumber2 })

        await doc2Row
          .getByRole("button", { name: "Dokumentationseinheit übernehmen" })
          .click()

        await expect(
          doc2Row.getByRole("button", {
            name: "Dokumentationseinheit bearbeiten",
          }),
        ).toBeVisible()
      })

      await test.step("Weise den Zugang zu, so dass die Dokumentationseinheiten aus dem Eingang entfernt werden", async () => {
        await pageWithBghUser
          .getByRole("button", { name: "Zu Vorgang hinzufügen" })
          .click()

        await expect(
          pageWithBghUser.getByText("Hinzufügen erfolgreich"),
        ).toBeVisible()

        await expect(
          pageWithBghUser.getByText(
            "Es liegen keine Dokumentationseinheiten vor.",
          ),
        ).toBeVisible()
      })

      await deleteDocumentUnit(pageWithBghUser, docNumber1)
      await deleteDocumentUnit(pageWithBghUser, docNumber2)
    },
  )

  // eslint-disable-next-line playwright/no-skipped-test
  test.skip(
    "EU-Rechtsprechungsdokumente können im Eingang eingesehen und weiterverarbeitet werden",
    { tag: ["@RISDEV-7375"] },
    async ({ page }) => {
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

        await expect(row).toContainText("BVerwG") // Gerichtstyp
        await expect(row).toContainText("09.09.1987") // Datum
        await expect(row).toContainText("fileNumber4") // Aktenzeichen
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
        // Wait for queries in url to be resetted
        await page.waitForFunction(() => {
          return window.location.href.includes("fileNumber4")
        })
        await page.getByRole("tab", { name: "Fremdanlagen" }).click()

        // Wait for the URL tab param to update
        await page.waitForFunction(() => {
          return window.location.href.includes("fremdanlagen")
        })

        // Wait for queries in url to be resetted
        await page.waitForFunction(() => {
          return !window.location.href.includes("fileNumber4")
        })
        await page.getByRole("tab", { name: "EU-Rechtsprechung" }).click()

        // Wait for the URL tab param to update
        await page.waitForFunction(() => {
          return window.location.href.includes("eu-rechtsprechung")
        })

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
    },
  )
})
