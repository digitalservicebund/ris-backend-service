import { expect, Page } from "@playwright/test"
import { navigateToManagementData, navigateToSearch } from "../e2e-utils"
import { caselawTest as test } from "../fixtures"

test.describe("Wichtigste Verwaltungsdaten", { tag: ["@RISDEV-8004"] }, () => {
  // Todo: new endpoint for pending proceeding missing for this test case
  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("Die wichtigsten Verwaltungsdaten eines anhängigen Verfahrens werden vollständig für die eigene Dokstelle angezeigt", async ({
    page,
  }) => {
    let documentNumber: string

    await navigateToSearch(page)
    await test.step("Erstelle neue Dokumentationseinheit", async () => {
      await page
        .getByRole("button", {
          name: "Neue Dokumentationseinheit",
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
    await expectFirstPublishedAt(page, "–")
    // expect procedure not to be visible

    // expect history logs to be visible
    // expect Duplicatecheck not to be visible
    // expect assign docoffice not to be visible
    // expect Delete Button to be visible

    // Act
    // Update pending proceeding
    await navigateToManagementData(page, documentNumber!)

    await expectLastUpdatedAt(page, /^\d{2}\.\d{2}\.\d{4} um \d{2}:\d{2} Uhr$/)
    await expectLastUpdatedBy(page, "DS (e2e_tests DigitalService)")
  })

  // Todo: new endpoint for pending proceeding missing for this test case
  // eslint-disable-next-line playwright/no-skipped-test
  test.skip("Erledigung eines anhängigen Verfahren erstellt einen Log in den Historiendaten", async ({
    page,
  }) => {
    // Todo
    let documentNumber: string

    await navigateToSearch(page)
    await test.step("Erstelle neue Dokumentationseinheit", async () => {
      await page
        .getByRole("button", {
          name: "Neue Dokumentationseinheit",
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
    // eslint-disable-next-line playwright/no-conditional-in-test
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
    // eslint-disable-next-line playwright/no-conditional-in-test
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
})
