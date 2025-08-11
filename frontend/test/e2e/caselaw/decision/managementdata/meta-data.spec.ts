import { expect, Page } from "@playwright/test"
import dayjs from "dayjs"
import { caselawTest as test } from "../../fixtures"
import {
  fillInput,
  navigateToCategories,
  navigateToManagementData,
  navigateToPeriodicalReferences,
  navigateToSearch,
  save,
} from "../../utils/e2e-utils"
import { generateString } from "~/test-helper/dataGenerators"

const formattedDate = dayjs().format("DD.MM.YYYY")

test.describe("Wichtigste Verwaltungsdaten", { tag: ["@RISDEV-7247"] }, () => {
  test("Die wichtigsten Verwaltungsdaten werden vollständig für die eigene Dokstelle angezeigt", async ({
    page,
  }) => {
    let documentNumber: string
    let newProcedure: string

    await navigateToSearch(page)
    await test.step("Erstelle neue Dokumentationseinheit", async () => {
      await page
        .getByRole("button", {
          name: "Neue Entscheidung",
          exact: true,
        })
        .click()

      await expect(page).toHaveURL(
        /\/caselaw\/documentunit\/[A-Z0-9]{13}\/attachments$/,
      )

      documentNumber = /caselaw\/documentunit\/(.*)\/attachments/g.exec(
        page.url(),
      )?.[1] as string
    })

    await navigateToManagementData(page, documentNumber!)
    await expectCreatedAt(page, /^\d{2}\.\d{2}\.\d{4} um \d{2}:\d{2} Uhr$/)
    await expectCreatedBy(page, "DS (e2e_tests DigitalService)")
    await expectSource(page, "–")
    await expectLastUpdatedAt(page, "–")
    await expectLastUpdatedBy(page, "–")
    await expectProcedure(page, "–")
    await expectFirstPublishedAt(page, "–")

    await test.step("Setze die Quelle auf Z", async () => {
      await navigateToCategories(page, documentNumber!)
      const dropdown = page.getByRole("combobox", { name: "Quelle Input" })
      await expect(dropdown).toHaveText("Bitte auswählen")

      await dropdown.click()

      await page
        .getByRole("option", {
          name: "Zeitschriftenveröffentlichung (Z)",
        })
        .click()
      await expect(dropdown).toHaveText("Zeitschriftenveröffentlichung (Z)")
    })

    await test.step("Setze den Vorgang", async () => {
      const testPrefix = generateString({ length: 10 })

      await expect(async () => {
        newProcedure = testPrefix + generateString({ length: 10 })
        await page.getByLabel("Vorgang", { exact: true }).fill(newProcedure)
        await page.getByText(`${newProcedure} neu erstellen`).click()
      }).toPass()

      await save(page)
    })

    await navigateToManagementData(page, documentNumber!)

    await expectSource(page, "Z")
    await expectLastUpdatedAt(page, /^\d{2}\.\d{2}\.\d{4} um \d{2}:\d{2} Uhr$/)
    await expectLastUpdatedBy(page, "DS (e2e_tests DigitalService)")
    await expectProcedure(page, newProcedure!)
  })

  test("Der Name wird in den wichtigsten Verwaltungsdaten für andere Dokstellen ausgeblendet", async ({
    page,
    pageWithBghUser,
    edition,
  }) => {
    const randomFileNumber = generateString()
    let documentNumber = ""

    await test.step("Fremdanlage für BGH anlegen", async () => {
      await navigateToPeriodicalReferences(page, edition.id ?? "")
      await fillInput(page, "Zitatstelle *", "12")
      await expect(page.getByLabel("Zitatstelle *")).toHaveValue("12")
      await fillInput(page, "Klammernzusatz", "L")
      await searchForDocUnit(
        page,
        "AG Aachen",
        formattedDate,
        randomFileNumber,
        "AnU",
      )

      await expect(
        page.getByText("Übernehmen und weiter bearbeiten"),
      ).toBeVisible()

      await expect(
        page.getByLabel("Dokumentationsstelle auswählen"),
      ).toHaveValue("BGH")
    })

    const pagePromise = page.context().waitForEvent("page")
    await page.getByText("Übernehmen und weiter bearbeiten").click()
    const newTab = await pagePromise

    documentNumber = await verifyDocUnitOpensInNewTab(newTab, randomFileNumber)
    await test.step("Gehe als BGH-User zu Verwaltungsdaten", async () => {
      await navigateToManagementData(pageWithBghUser, documentNumber)
    })

    await expectSource(
      pageWithBghUser,
      /^Z aus MMG 2024, 12, Heft e2e-\S+ \(L\) \(DS\)$/,
    )
    await expectCreatedAt(
      pageWithBghUser,
      /^\d{2}\.\d{2}\.\d{4} um \d{2}:\d{2} Uhr$/,
    )
    await expectCreatedBy(pageWithBghUser, "DS")
    await expectLastUpdatedAt(
      pageWithBghUser,
      /^\d{2}\.\d{2}\.\d{4} um \d{2}:\d{2} Uhr$/,
    )
    await expectLastUpdatedBy(pageWithBghUser, "DS")
  })

  async function expectCreatedAt(page: Page, createdAt: RegExp) {
    await test.step("Angelegt am ist befüllt", async () => {
      const label = page.locator(
        '[data-testid="management-data-created-at"] dt',
      )
      await expect(label).toHaveText("Angelegt am")
      const createdAtElement = page.locator(
        '[data-testid="management-data-created-at"] dd',
      )
      await expect(createdAtElement).toHaveText(createdAt)
    })
  }

  async function expectCreatedBy(page: Page, createdBy: string) {
    await test.step(`Angelegt von ist '${createdBy}'`, async () => {
      const label = page.locator(
        '[data-testid="management-data-created-by"] dt',
      )
      await expect(label).toHaveText("Von")
      const createdByElement = page.locator(
        '[data-testid="management-data-created-by"] dd',
      )
      await expect(createdByElement).toHaveText(createdBy)
    })
  }

  async function expectSource(page: Page, source: string | RegExp) {
    await test.step(`Quelle ist '${source instanceof RegExp ? "Z aus MMG 2024, 12, Heft e2e-[...] (L) (DS)" : source}'`, async () => {
      const label = page.locator('[data-testid="management-data-source"] dt')
      await expect(label).toHaveText("Quelle")
      const sourceElement = page.locator(
        '[data-testid="management-data-source"] dd',
      )
      await expect(sourceElement).toHaveText(source)
    })
  }

  async function expectLastUpdatedAt(
    page: Page,
    lastUpdatedAt: string | RegExp,
  ) {
    await test.step(`Zuletzt bearbeitet am ist ${lastUpdatedAt instanceof RegExp ? "befüllt" : "'" + lastUpdatedAt + "'"}`, async () => {
      const label = page.locator(
        '[data-testid="management-data-last-updated-at"] dt',
      )
      await expect(label).toHaveText("Zuletzt bearbeitet am")
      const lastUpdatedAtElement = page.locator(
        '[data-testid="management-data-last-updated-at"] dd',
      )
      await expect(lastUpdatedAtElement).toHaveText(lastUpdatedAt)
    })
  }

  async function expectLastUpdatedBy(page: Page, lastUpdatedBy: string) {
    await test.step(`Zuletzt bearbeitet von ist '${lastUpdatedBy}'`, async () => {
      const label = page.locator(
        '[data-testid="management-data-last-updated-by"] dt',
      )
      await expect(label).toHaveText("Von")
      const lastUpdatedByElement = page.locator(
        '[data-testid="management-data-last-updated-by"] dd',
      )
      await expect(lastUpdatedByElement).toHaveText(lastUpdatedBy)
    })
  }

  async function expectProcedure(page: Page, procedure: string) {
    await test.step(`Vorgang ist '${procedure}'`, async () => {
      const label = page.locator('[data-testid="management-data-procedure"] dt')
      await expect(label).toHaveText("Vorgang")
      const procedureElement = page.locator(
        '[data-testid="management-data-procedure"] dd',
      )
      await expect(procedureElement).toHaveText(procedure)
    })
  }

  async function expectFirstPublishedAt(page: Page, firstPublishedAt: string) {
    await test.step(`Erstveröffentlichung am ist '${firstPublishedAt}'`, async () => {
      const label = page.locator(
        '[data-testid="management-data-first-published-at"] dt',
      )
      await expect(label).toHaveText("Erstveröffentlichung am")
      const firstPublishedAtElement = page.locator(
        '[data-testid="management-data-first-published-at"] dd',
      )
      await expect(firstPublishedAtElement).toHaveText(firstPublishedAt)
    })
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
      await fillInput(page, "Datum", date)
    }
    if (documentType) {
      await fillInput(page, "Dokumenttyp", documentType)
      await page.getByText("Anerkenntnisurteil", { exact: true }).click()
    }

    await page.getByText("Suchen").click()
  }

  async function verifyDocUnitOpensInNewTab(
    newTab: Page,
    randomFileNumber: string,
  ) {
    await expect(newTab).toHaveURL(
      /\/caselaw\/documentunit\/[A-Z0-9]{13}\/categories$/,
    )
    const documentNumber = /caselaw\/documentunit\/(.*)\/categories/g.exec(
      newTab.url(),
    )?.[1] as string
    await expect(newTab.getByLabel("Gericht", { exact: true })).toHaveValue(
      "AG Aachen",
    )
    await expect(
      newTab.getByLabel("Entscheidungsdatum", { exact: true }),
    ).toHaveValue(formattedDate)
    await expect(newTab.getByTestId("chip-value")).toHaveText(randomFileNumber)
    await expect(newTab.getByLabel("Dokumenttyp", { exact: true })).toHaveValue(
      "Anerkenntnisurteil",
    )

    // Can be edited and saved after creation
    await newTab
      .getByLabel("Entscheidungsdatum", { exact: true })
      .fill("01.01.2021")
    await save(newTab)
    return documentNumber
  }
})
