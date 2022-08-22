import { expect } from "@playwright/test"
import { navigateToCategories } from "./e2e-utils"
import { testWithDocUnit as test } from "./fixtures"

test.beforeEach(async ({ page, documentNumber }) => {
  await navigateToCategories(page, documentNumber)
})

test("test save button status change", async ({ page }) => {
  await page.locator("[aria-label='Stammdaten Speichern Button']").click()
  let coreDataSaveButtonStatus = await page
    .locator("text='Daten werden gespeichert'")
    .nth(0)
  let textSaveButtonStatus = await page
    .locator("text='Daten werden gespeichert'")
    .nth(1)
  expect(coreDataSaveButtonStatus).toBeVisible()
  expect(textSaveButtonStatus).toBeVisible()

  await page.waitForTimeout(1000)
  coreDataSaveButtonStatus = await page
    .locator("text='Zuletzt gespeichert um'")
    .nth(0)
  textSaveButtonStatus = await page
    .locator("text='Zuletzt gespeichert um'")
    .nth(1)
  const regEx = /^Zuletzt gespeichert um.*Uhr$/
  expect(coreDataSaveButtonStatus).toBeVisible()
  expect(textSaveButtonStatus).toBeVisible()
  expect((await coreDataSaveButtonStatus.innerText()).match(regEx)?.index).toBe(
    0
  )
  expect((await textSaveButtonStatus.innerText()).match(regEx)?.index).toBe(0)
})

test("test could not update docunit", async ({ page }) => {
  await page.route("**/api/v1/docunits/*/docx", async (route) => {
    route.fulfill({
      status: 400,
      contentType: "text/plain",
      body: "Not Found!",
    })
  })
  await page.locator("[aria-label='Stammdaten Speichern Button']").click()
  let coreDataSaveButtonStatus = await page
    .locator("text='Daten werden gespeichert'")
    .nth(0)
  let textSaveButtonStatus = await page
    .locator("text='Daten werden gespeichert'")
    .nth(1)
  expect(coreDataSaveButtonStatus).toBeVisible()
  expect(textSaveButtonStatus).toBeVisible()

  await page.waitForTimeout(1000)
  coreDataSaveButtonStatus = await page
    .locator("text='Fehler beim Speichern'")
    .nth(0)
  textSaveButtonStatus = await page
    .locator("text='Fehler beim Speichern'")
    .nth(1)

  expect(coreDataSaveButtonStatus).toBeVisible()
  expect(textSaveButtonStatus).toBeVisible()
})

test("test automatic save docunit", async ({ page, editorField }) => {
  test.setTimeout(50000)
  await editorField.click()
  await editorField.type("this is a change")
  await page.waitForTimeout(30000)
  let coreDataSaveButtonStatus = await page
    .locator("text='Daten werden gespeichert'")
    .nth(0)
  let textSaveButtonStatus = await page
    .locator("text='Daten werden gespeichert'")
    .nth(1)
  expect(coreDataSaveButtonStatus).toBeVisible()
  expect(textSaveButtonStatus).toBeVisible()

  await page.waitForTimeout(1000)
  coreDataSaveButtonStatus = await page
    .locator("text='Zuletzt gespeichert um'")
    .nth(0)
  textSaveButtonStatus = await page
    .locator("text='Zuletzt gespeichert um'")
    .nth(1)
  const regEx = /^Zuletzt gespeichert um.*Uhr$/
  expect(coreDataSaveButtonStatus).toBeVisible()
  expect(textSaveButtonStatus).toBeVisible()
  expect((await coreDataSaveButtonStatus.innerText()).match(regEx)?.index).toBe(
    0
  )
  expect((await textSaveButtonStatus.innerText()).match(regEx)?.index).toBe(0)
})
