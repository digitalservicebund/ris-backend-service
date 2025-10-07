import { expect, Page } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToCategories,
  navigateToPreview,
  navigateToHandover,
  save,
  openSearchWithFileNumberPrefix,
  fillInput,
  triggerSearch,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Datum der mündlichen Verhandlung",
  { tag: ["@RISDEV-8556"] },
  () => {
    test.use({
      decisionToBeCreated: [
        { coreData: { oralHearingDates: ["2021-02-01", "2020-02-01"] } },
        { scope: "test" },
      ],
    })

    test("Anzeigen und Editieren in Rubriken", async ({
      page,
      documentNumber,
    }) => {
      await navigateToCategories(page, documentNumber)

      await addTwoDateValues(page)

      await test.step("Lösche einen Datumswert", async () => {
        await page.keyboard.press("ArrowLeft")
        await page.keyboard.press("Enter")

        await expect(page.getByText("01.02.2021")).toBeHidden()
      })
      await save(page)

      await test.step("Prüfe, dass der Wert gespeichert wird und beim Reload vorhanden ist", async () => {
        await page.reload()
        await expect(page.getByText("01.02.2020")).toBeVisible()
      })
    })

    test("Vorschau", async ({
      page,
      decision: {
        createdDecision: { documentNumber },
      },
    }) => {
      await test.step("Die Datumswerte werden in der Vorschau angezeigt", async () => {
        await navigateToPreview(page, documentNumber)
        await expect(
          page.getByText("Datum der mündlichen Verhandlung"),
        ).toBeVisible()
        await expect(page.getByText("01.02.2020")).toBeVisible()
        await expect(page.getByText("01.02.2021")).toBeVisible()
      })
    })

    test("Suchbar über Datum in Große Suche", async ({
      page,
      decision: {
        fileNumber,
        createdDecision: { documentNumber },
      },
    }) => {
      await openSearchWithFileNumberPrefix(fileNumber, page)
      await test.step("Wähle Entscheidungsdatum '02.01.2023' in Suche", async () => {
        await fillInput(page, "Entscheidungsdatum Suche", "01.02.2021")
      })
      await triggerSearch(page)
      await test.step("Entscheidung mit 'Datum der mündlichen Verhandlung' ist in Ergebnisliste enthalten", async () => {
        await expect(page.getByText(documentNumber)).toBeVisible()
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

      await addTwoDateValues(page)

      await save(page)

      await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)

      await test.step("Übergabeseite warnt, dass Datum nicht übergeben wird", async () => {
        await expect(
          page.getByText(
            "Folgende Rubriken sind befüllt und können nicht an die jDV exportiert werden",
          ),
        ).toBeVisible()
        await expect(
          page.getByText("Datum der mündlichen Verhandlung"),
        ).toBeVisible()
      })
    })

    test("Validierung: Valide Datumswerte in der Vergangenheit", async ({
      page,
      documentNumber,
    }) => {
      await navigateToCategories(page, documentNumber)

      await test.step("Zwei identische Datumswerte erzeugen Fehler", async () => {
        await page
          .getByText("Datum der mündlichen Verhandlung", { exact: true })
          .fill("01.02.2020")
        await page.keyboard.press("Enter")
        await page
          .getByText("Datum der mündlichen Verhandlung", { exact: true })
          .fill("01.02.2020")
        await page.keyboard.press("Enter")
        await expect(
          page.getByText("01.02.2020 bereits vorhanden"),
        ).toBeVisible()
      })

      await test.step("Invalider Datumswert erzeugt Fehler", async () => {
        await page
          .getByText("Datum der mündlichen Verhandlung", { exact: true })
          .fill("29.02.2021")
        await page.keyboard.press("Enter")
        await expect(
          page.getByText("01.02.2020 bereits vorhanden"),
        ).toBeHidden()
        await expect(page.getByText("Kein valides Datum")).toBeVisible()
      })

      await test.step("Datum in der Zukunft erzeugt Fehler", async () => {
        await page
          .getByText("Datum der mündlichen Verhandlung", { exact: true })
          .fill("01.02.2040")
        await page.keyboard.press("Enter")
        await expect(page.getByText("Kein valides Datum")).toBeHidden()
        await expect(
          page.getByText(
            "Datum der mündlichen Verhandlung darf nicht in der Zukunft liegen",
          ),
        ).toBeVisible()
      })
    })
  },
)

async function addTwoDateValues(page: Page) {
  await test.step("Füge zwei Datumswerte hinzu", async () => {
    await page
      .getByText("Datum der mündlichen Verhandlung", { exact: true })
      .fill("01.02.2020")
    await page.keyboard.press("Enter")
    await page
      .getByText("Datum der mündlichen Verhandlung", { exact: true })
      .fill("01.02.2021")
    await page.keyboard.press("Enter")

    await expect(page.getByText("01.02.2020")).toBeVisible()
    await expect(page.getByText("01.02.2021")).toBeVisible()
  })
}
