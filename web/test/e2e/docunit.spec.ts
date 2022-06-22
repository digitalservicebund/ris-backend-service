import { test, expect } from "@playwright/test"

test.describe("generate and delete doc units", () => {
  let docUnitId: string

  test("generate doc unit", async ({ page }) => {
    await page.goto("/")

    const newDocUnitButton = await page.waitForSelector(
      "button >> text=Neue Dokumentationseinheit"
    )
    newDocUnitButton.click()

    await page.waitForSelector("text=Rechtsprechung")
    docUnitId = await page.locator(".panel-id").innerText()
  })

  test("upload docx", async ({ page }) => {
    await page.goto("/")

    const selectDocUnit = await page.waitForSelector(
      `tr td:nth-child(1) a[href*="/rechtsprechung/${docUnitId}/dokumente"]`
    )
    selectDocUnit.click()

    const [fileChooser] = await Promise.all([
      page.waitForEvent("filechooser"),
      page.locator("text=Festplatte durchsuchen").click(),
    ])
    await fileChooser.setFiles("./test/e2e/sample.docx")

    await page.waitForSelector(
      ".fileviewer-info-panel-value >> text=sample.docx"
    )
  })

  test("delete docx", async ({ page }) => {
    await page.goto("/")

    const selectDocUnit = await page.waitForSelector(
      `tr td:nth-child(1) a[href*="/rechtsprechung/${docUnitId}/rubriken"]`
    )
    selectDocUnit.click()

    const documentLink = await page.waitForSelector(
      'a[href*="/rechtsprechung/' + docUnitId + '/dokumente"] >> text=DOKUMENTE'
    )
    documentLink.click()

    const deleteDocx = await page.waitForSelector("text=Datei löschen")
    deleteDocx.click()

    await page.waitForSelector("text=Festplatte durchsuchen")

    await page.goto("/")

    await page.waitForSelector(
      `tr td:nth-child(1) a[href*="/rechtsprechung/${docUnitId}/dokumente"]`
    )
  })

  test("delete doc unit", async ({ page }) => {
    await page.goto("/")

    const selectDocUnit = page
      .locator("tr", {
        has: page.locator(
          `td:nth-child(1) a[href*="/rechtsprechung/${docUnitId}/dokumente"]`
        ),
      })
      .locator("td:nth-child(5) i")
    await selectDocUnit.waitFor()
    selectDocUnit.click()

    await page.goto("/")

    await page.waitForTimeout(2000)

    expect(
      page.locator(`a[href*="/rechtsprechung/${docUnitId}"]`)
    ).not.toBeVisible()
  })
})
