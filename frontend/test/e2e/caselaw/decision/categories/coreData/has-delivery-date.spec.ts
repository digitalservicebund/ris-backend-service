import { expect, Page } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToCategories,
  navigateToPreview,
  navigateToHandover,
  save,
  navigateToPublication,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Zustellung an Verkündungs statt",
  { tag: ["@RISDEV-8556"] },
  () => {
    test("Anzeigen und Editieren in Rubriken", async ({
      page,
      documentNumber,
    }) => {
      await navigateToCategories(page, documentNumber)

      await checkHasDeliveryDate(page)

      await save(page)

      await test.step("Prüfe, dass der Wert gespeichert wird und beim Reload vorhanden ist", async () => {
        await page.reload()
        await expect(
          page.getByRole("checkbox", {
            name: "Zustellung an Verkündungs statt",
          }),
        ).toBeChecked()
      })
    })

    test("Vorschau", async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)

      await test.step("Befülle 'Entscheidungsdatum'", async () => {
        await page
          .getByLabel("Entscheidungsdatum", { exact: true })
          .fill("03.02.2000")
      })

      await checkHasDeliveryDate(page)
      await save(page)

      await test.step("Das Datumslabel in der Vorschau heißt 'Datum der Zustellung an Verkündungs statt'", async () => {
        await navigateToPreview(page, documentNumber)
        await expect(
          page.getByText("Datum der Zustellung an Verkündungs statt"),
        ).toBeVisible()
        await expect(page.getByText("03.02.2000")).toBeVisible()
      })
    })

    test("jDV-Übergabe zeigt Warnung", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      await navigateToCategories(
        page,
        prefilledDocumentUnit.documentNumber || "",
      )

      await checkHasDeliveryDate(page)

      await save(page)

      await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)

      await test.step("Übergabeseite warnt, dass 'Zustellung an Verkündungs statt' nicht übergeben wird", async () => {
        await expect(
          page.getByText(
            "Folgende Rubriken sind befüllt und können nicht an die jDV exportiert werden",
          ),
        ).toBeVisible()
        await expect(
          page.getByText("Zustellung an Verkündungs statt"),
        ).toBeVisible()
      })
    })

    test("Datum der mündlichen Verhandlung wird zum Pflichtfeld", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      await navigateToCategories(
        page,
        prefilledDocumentUnit.documentNumber || "",
      )

      await checkHasDeliveryDate(page)

      await navigateToPublication(page, prefilledDocumentUnit.documentNumber!, {
        navigationBy: "click",
      })

      await test.step("Plausibilitätsprüfung zeigt Fehler für Datum der mündlichen Verhandlung", async () => {
        await expect(
          page.getByText(
            "Die folgenden Rubriken-Pflichtfelder sind nicht befüllt",
          ),
        ).toBeVisible()
        await expect(
          page.getByText("Datum der mündlichen Verhandlung"),
        ).toBeVisible()
      })

      await test.step("Folge Link zu Kategorie und befülle Datum", async () => {
        await page.getByText("Datum der mündlichen Verhandlung").click()
        await page
          .getByText("Datum der mündlichen Verhandlung *", { exact: true })
          .fill("01.02.2020")
        await page.keyboard.press("Enter")
      })

      await navigateToPublication(page, prefilledDocumentUnit.documentNumber!, {
        navigationBy: "click",
      })

      await test.step("Plausibilitätsprüfung zeigt keine Fehler mehr", async () => {
        await expect(
          page.getByText("Alle Pflichtfelder sind korrekt ausgefüllt."),
        ).toBeVisible()
      })
    })
  },
)

async function checkHasDeliveryDate(page: Page) {
  await test.step("Wähle 'Zustellung an Verkündungs statt'", async () => {
    await page
      .getByRole("checkbox", {
        name: "Zustellung an Verkündungs statt",
      })
      .click()

    await expect(
      page.getByRole("checkbox", {
        name: "Zustellung an Verkündungs statt",
      }),
    ).toBeChecked()
  })
}
