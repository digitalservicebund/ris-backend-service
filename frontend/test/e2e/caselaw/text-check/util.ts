import { expect, Locator, Page } from "@playwright/test"

export async function triggerCheckCategory(
  page: Page,
  textCategoryLabel: string,
): Promise<void> {
  await page.getByTestId(textCategoryLabel).click()

  await page
    .getByLabel(`${textCategoryLabel} Button`)
    .getByRole("button", { name: "Rechtschreibprüfung" })
    .click()

  await expect(page.getByTestId("text-check-loading-status")).toHaveText(
    "Rechtschreibprüfung läuft",
  )

  await page
    .getByTestId(textCategoryLabel)
    .locator("text-check")
    .first()
    .waitFor({ state: "visible" })
}

export const defaultText =
  "LanguageTool ist ist Ihr intelligenter Schreibassistent für alle gängigen Browser und Textverarbeitungsprogramme. Schreiben sie in diesem Textfeld oder fügen Sie einen Text ein. Rechtshcreibfehler werden rot markirt, Grammatikfehler werden gelb hervor gehoben und Stilfehler werden, anders wie die anderen Fehler, blau unterstrichen. wussten Sie dass Synonyme per Doppelklick auf ein Wort aufgerufen werden können? Nutzen Sie LanguageTool in allen Lebenslagen, z. B. wenn Sie am Donnerstag, dem 13. Mai 2022, einen Basketballkorb in 10 Fuß Höhe montieren möchten. Testgnorierteswort ist zB. grün markiert"

export async function getMarkId(tag: Locator): Promise<string | null> {
  return await tag.evaluate((el) => el.getAttribute("id"))
}

export const textCheckUnderlinesColors = {
  error: "#cd5038",
  ignored: "#66add3",
} as const

// see text-check.scss for the expected values
export const ignoredColorStyle = "2px solid rgb(102, 173, 211)"
export const textMistakeColor = "rgb(205, 80, 56)"
