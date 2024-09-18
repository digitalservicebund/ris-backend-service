import { expect } from "@playwright/test"
import dayjs from "dayjs"
import {
  navigateToPeriodicalHandover,
  navigateToPeriodicalReferences,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"

const formattedDate = dayjs().format("YYYY-MM-DD")

test.describe("ensuring the handover of edition works as expected", () => {
  test("periodical evaluation has handover menu item", async ({
    page,
    edition,
  }) => {
    await navigateToPeriodicalReferences(page, edition.id ?? "")

    await expect(
      page.getByRole("link", { name: "Übergabe an jDV" }),
    ).toBeVisible()

    await page.getByRole("link", { name: "Übergabe an jDV" }).click()
    await expect(page.getByTestId("handover-title")).toBeVisible()
  })

  test("handover not possible if references are empty", async ({
    page,
    edition,
  }) => {
    await navigateToPeriodicalHandover(page, edition.id ?? "")

    await expect(
      page.getByText("Es wurden noch keine Fundstellen hinzugefügt"),
    ).toBeVisible()
    await page
      .locator("[aria-label='Fundstellen der Ausgabe an jDV übergeben']")
      .click()
    await expect(
      page.getByText("Die Ausgabe kann nicht übergeben werden."),
    ).toBeVisible()
    await expect(
      page.locator(
        "text=Diese Ausgabe wurde bisher nicht an die jDV übergeben",
      ),
    ).toBeVisible()
  })

  test("handover possible when all required fields filled", async ({
    page,
    editionWithReference,
  }) => {
    await navigateToPeriodicalHandover(page, editionWithReference.id ?? "")

    await expect(
      page.getByText("Die Ausgabe enthält 2 Fundstellen"),
    ).toBeVisible()

    await expect(page.getByText("XML Vorschau")).toBeVisible()

    await page.getByText("XML Vorschau").click()

    await expect(
      page.locator(
        "text='            <zitstelle>2024, 1-11, Heft 1</zitstelle>'",
      ),
    ).toBeVisible()

    await expect(
      page.locator(
        "text='            <zitstelle>2024, 12-22, Heft 1 (L)</zitstelle>'",
      ),
    ).toBeVisible()

    await page
      .locator("[aria-label='Fundstellen der Ausgabe an jDV übergeben']")
      .click()

    await expect(page.getByText("Email wurde versendet")).toBeVisible()

    await expect(page.getByText("Xml Email Abgabe -")).toBeVisible()

    await expect(
      page.getByText(
        "id=juris name=invalid-user da=R df=X dt=F mod=T ld=" +
          formattedDate +
          " vg=edition-" +
          editionWithReference.id,
      ),
    ).toBeVisible()
  })
})
