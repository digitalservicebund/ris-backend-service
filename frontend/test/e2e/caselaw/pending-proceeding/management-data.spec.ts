import { expect, Page } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToManagementData,
  expectHistoryCount,
  expectHistoryLogRow,
  navigateToCategories,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe("Verwaltungsdaten", { tag: ["@RISDEV-8004"] }, () => {
  test("Verwaltungsdaten für anhängige Verfahren", async ({
    page,
    pendingProceeding,
  }) => {
    await test.step("Metadaten eines anhängigen Verfahrens werden korrekt für die eigene Dokstelle angezeigt", async () => {
      await navigateToManagementData(page, pendingProceeding.documentNumber)
      await expectCreatedAt(page, /^\d{2}\.\d{2}\.\d{4} um \d{2}:\d{2} Uhr$/)
      // Todo: uncomment again when user data caching is done
      // await expectCreatedBy(page, "DS (e2e_tests DigitalService)")
      await expectSource(page, "–")
      await expectLastUpdatedAt(page, "–")
      await expectLastUpdatedBy(page, "–")
      await expectFirstPublishedAt(page, "–")
      await expect(
        page.locator('[data-testid="management-data-procedure"] dd'),
      ).toBeHidden()
    })

    await test.step("Die Historie hat ein 'Dokeinheit angelegt'-Event", async () => {
      await navigateToManagementData(page, pendingProceeding.documentNumber)
      await expectHistoryCount(page, 1)
      await expectHistoryLogRow(
        page,
        0,
        // "DS (e2e_tests DigitalService)",
        `Dokeinheit angelegt`,
      )
    })

    await test.step("Dublettencheck und Zuweisen werden nicht angezeigt für anhängige Verfahren", async () => {
      await expect(page.getByText("Dublettenverdacht")).toBeHidden()
      await expect(page.getByText("Zuweisen")).toBeHidden()
    })

    await test.step("Anhängige Verfahren können über Verwaltungsdaten gelöscht werden", async () => {
      const deleteButton = page.getByLabel("Dokumentationseinheit löschen")
      await expect(deleteButton).toBeVisible()
      await deleteButton.click()
      await expect(
        page.getByRole("dialog").getByText("Dokumentationseinheit löschen"),
      ).toBeVisible()
    })

    await test.step("Erledigung eines anhängigen Verfahren erstellt einen Log in den Historiendaten, aktualisiert die Metadaten", async () => {
      await navigateToCategories(page, pendingProceeding.documentNumber, {
        type: "pending-proceeding",
      })
      const isResolved = page.getByLabel("Erledigt")

      await isResolved.check()
      await expect(isResolved).toBeChecked()
      await save(page)

      await navigateToManagementData(page, pendingProceeding.documentNumber, {
        type: "pending-proceeding",
      })
      await expectLastUpdatedAt(
        page,
        /^\d{2}\.\d{2}\.\d{4} um \d{2}:\d{2} Uhr$/,
      )
      await expectLastUpdatedBy(page, "DS (e2e_tests DigitalService)")
      await expectHistoryCount(page, 3)
      await expectHistoryLogRow(
        page,
        0,
        // "DS (e2e_tests DigitalService)",
        `Verfahren als "Erledigt" markiert`,
      )

      await expectHistoryLogRow(
        page,
        1,
        // "DS (e2e_tests DigitalService)",
        `Dokeinheit bearbeitet`,
      )
    })
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

  // async function expectCreatedBy(page: Page, createdBy: string) {
  //   await test.step(`Angelegt von ist '${createdBy}'`, async () => {
  //     const label = page.locator(
  //       '[data-testid="management-data-created-by"] dt',
  //     )
  //     await expect(label).toHaveText("Von")
  //     const createdByElement = page.locator(
  //       '[data-testid="management-data-created-by"] dd',
  //     )
  //     await expect(createdByElement).toHaveText(createdBy)
  //   })
  // }

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
