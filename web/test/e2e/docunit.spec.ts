import { test, expect } from "@playwright/test"

test.describe("generate and delete doc units", () => {
  let documentNumber: string

  test("generate doc unit", async ({ page }) => {
    await page.goto("/")

    await page.locator("button >> text=Neue Dokumentationseinheit").click()

    await page.waitForSelector("text=Festplatte durchsuchen")
    const regex = /rechtsprechung\/(.*)\/dokumente/g
    const match = regex.exec(page.url())
    documentNumber = match[1] || ""
  })

  test("upload original file", async ({ page }) => {
    await page.goto("/")

    const selectDocUnit = page.locator(
      `tr td:nth-child(1) a[href*="/rechtsprechung/${documentNumber}/dokumente"]`
    )
    await selectDocUnit.click()

    const [fileChooser] = await Promise.all([
      page.waitForEvent("filechooser"),
      page.locator("text=Festplatte durchsuchen").click(),
    ])
    await fileChooser.setFiles("./test/e2e/sample.docx")

    await page.waitForSelector(
      ".fileviewer-info-panel-value >> text=sample.docx"
    )
  })

  test("delete delete original file", async ({ page }) => {
    await page.goto("/")

    await page
      .locator(`a[href*="/rechtsprechung/${documentNumber}/rubriken"]`)
      .click()

    const documentLink = page.locator(
      'a[href*="/rechtsprechung/' +
        documentNumber +
        '/dokumente"] >> text=DOKUMENTE'
    )
    await documentLink.click()

    await page.locator("text=Datei löschen").click()

    await page.waitForSelector("text=Festplatte durchsuchen")

    await page.goto("/")

    await page.waitForSelector(
      `a[href*="/rechtsprechung/${documentNumber}/dokumente"]`
    )
  })

  test("delete doc unit", async ({ page }) => {
    await page.goto("/")

    const selectDocUnit = page
      .locator("tr", {
        has: page.locator(
          `td:nth-child(1) a[href*="/rechtsprechung/${documentNumber}/dokumente"]`
        ),
      })
      .locator("td:nth-child(5) i")
    await selectDocUnit.waitFor()
    selectDocUnit.click() // an await here would break the test

    await page.goto("/")

    await page.waitForTimeout(2000)

    await expect(
      page.locator(`a[href*="/rechtsprechung/${documentNumber}"]`)
    ).not.toBeVisible()
  })
})
