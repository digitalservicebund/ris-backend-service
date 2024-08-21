import { expect } from "@playwright/test"
import { navigateToCategories } from "../../e2e-utils"
import { caselawTest as test } from "../../fixtures"

test.describe("short and long texts", () => {
  test("text editor fields should have predefined height", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    // small
    const smallEditor = page.locator("[data-testid='Titelzeile']")
    const smallEditorHeight = await smallEditor.evaluate((element) =>
      window.getComputedStyle(element).getPropertyValue("height"),
    )
    expect(parseInt(smallEditorHeight)).toBeGreaterThanOrEqual(60)

    //medium
    const mediumEditor = page.locator("[data-testid='Leitsatz']")
    const mediumEditorHeight = await mediumEditor.evaluate((element) =>
      window.getComputedStyle(element).getPropertyValue("height"),
    )
    expect(parseInt(mediumEditorHeight)).toBeGreaterThanOrEqual(120)

    //large
    const largeEditor = page.locator("[data-testid='Gründe']")
    const largeEditorHeight = await largeEditor.evaluate((element) =>
      window.getComputedStyle(element).getPropertyValue("height"),
    )
    expect(parseInt(largeEditorHeight)).toBeGreaterThanOrEqual(320)
  })

  test("toggle invisible characters", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    const guidingPrincipleInput = page.locator("[data-testid='Leitsatz']")
    await guidingPrincipleInput.click()
    await page.keyboard.type(`this is a test paragraph`)

    await expect(
      guidingPrincipleInput.locator("[class='ProseMirror-trailingBreak']"),
    ).toHaveCount(1)
    await page
      .locator(`[aria-label='invisible-characters']:not([disabled])`)
      .click()
    await expect(
      guidingPrincipleInput.locator("[class='ProseMirror-trailingBreak']"),
    ).toHaveCount(0)
  })

  test(
    "text editor keyboard navigation",
    {
      annotation: {
        type: "story",
        description:
          "https://digitalservicebund.atlassian.net/browse/RISDEV-4432",
      },
      tag: ["@RISDEV-3682", "@RISDEV-4432"],
    },
    async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber)

      const guidingPrincipleInput = page.locator(
        "[data-testid='Leitsatz'] > div.tiptap",
      )
      await guidingPrincipleInput.click()
      await expect(guidingPrincipleInput).toBeFocused()

      // Write text and select all
      await page.keyboard.type("Text input")
      await page.keyboard.press("ControlOrMeta+A")

      // Navigate to toolbar -> first button is focused
      await page.keyboard.press("Shift+Tab")
      const firstButton = page
        .getByLabel("Leitsatz Button Leiste")
        .getByLabel("fullview")
      await expect(firstButton).toBeFocused()

      // Navigate to bold button with arrow keys
      await page.keyboard.press("ArrowRight")
      await page.keyboard.press("ArrowRight")
      await page.keyboard.press("ArrowRight")
      await page.keyboard.press("ArrowLeft")
      const boldButton = page
        .getByLabel("Leitsatz Button Leiste")
        .getByLabel("bold")
      await expect(boldButton).toBeFocused()

      // Pressing enter moves focus to the editor
      await page.keyboard.press("Enter")
      await expect(guidingPrincipleInput).toBeFocused()

      // Tiptap inserts invisible characters -> input is split into two parts
      expect(await page.getByText("Text input").innerHTML()).toContain(
        "<strong>Text</strong>",
      )
      expect(await page.getByText("Text input").innerHTML()).toContain(
        "<strong> input</strong>",
      )

      // The timeout is needed for the focus, otherwise tabbing back to the menu is flaky. It's unclear why.
      // eslint-disable-next-line playwright/no-wait-for-timeout
      await page.waitForTimeout(100)

      // Tabbing back into the toolbar sets focus to last active button
      await page.keyboard.press("Shift+Tab")
      await expect(boldButton).toBeFocused()

      // Move to alignment submenu and open it with Enter
      await page.keyboard.press("ArrowRight")
      await page.keyboard.press("ArrowRight")
      await page.keyboard.press("ArrowRight")
      await page.keyboard.press("ArrowRight")
      await page.keyboard.press("ArrowRight")
      await page.keyboard.press("ArrowRight")
      await page.keyboard.press("Enter")

      // Navigate to submenu button
      await page.keyboard.press("ArrowRight")
      const leftButton = page
        .getByLabel("Leitsatz Button Leiste")
        .getByLabel("left")
      await expect(leftButton).toBeFocused()

      // Close submenu with ESC
      await page.keyboard.press("Escape")
      await expect(leftButton).toBeHidden()
    },
  )

  test(
    "long texts are readonly for external user",
    {
      annotation: {
        type: "story",
        description:
          "https://digitalservicebund.atlassian.net/browse/RISDEV-4523",
      },
      tag: ["@RISDEV-4523"],
    },
    async ({ pageWithExternalUser, documentNumber }) => {
      const readOnly = 'contenteditable="false"'
      await test.step("Navigiere zu Rubriken als external Nutzer", async () => {
        await navigateToCategories(pageWithExternalUser, documentNumber)
      })

      await test.step("Tenor ist readonly", async () => {
        const tenor = pageWithExternalUser.getByTestId("Tenor")
        const inputFieldInnerHTML = await tenor.innerHTML()
        expect(inputFieldInnerHTML).toContain(readOnly)
      })

      await test.step("Tatbestand ist readonly", async () => {
        const caseFacts = pageWithExternalUser.getByTestId("Tatbestand")
        const inputFieldInnerHTML = await caseFacts.innerHTML()
        expect(inputFieldInnerHTML).toContain(readOnly)
      })

      await test.step("Entscheidungsgründe sind readonly", async () => {
        const decisionReasons = pageWithExternalUser.getByTestId(
          "Entscheidungsgründe",
        )
        const inputFieldInnerHTML = await decisionReasons.innerHTML()
        expect(inputFieldInnerHTML).toContain(readOnly)
      })

      await test.step("Gründe sind readonly", async () => {
        const reasons = pageWithExternalUser.getByTestId("Gründe")
        const inputFieldInnerHTML = await reasons.innerHTML()
        expect(inputFieldInnerHTML).toContain(readOnly)
      })
    },
  )

  test(
    "long texts are editable for internal user",
    {
      annotation: {
        type: "story",
        description:
          "https://digitalservicebund.atlassian.net/browse/RISDEV-4523",
      },
      tag: ["@RISDEV-4523"],
    },
    async ({ page, documentNumber }) => {
      const editable = 'contenteditable="true"'
      await test.step("Navigiere zu Rubriken als external Nutzer", async () => {
        await navigateToCategories(page, documentNumber)
      })

      await test.step("Tenor ist bearbeitbar", async () => {
        const tenor = page.getByTestId("Tenor")
        const inputFieldInnerHTML = await tenor.innerHTML()
        expect(inputFieldInnerHTML).toContain(editable)
      })

      await test.step("Tatbestand ist bearbeitbar", async () => {
        const caseFacts = page.getByTestId("Tatbestand")
        const inputFieldInnerHTML = await caseFacts.innerHTML()
        expect(inputFieldInnerHTML).toContain(editable)
      })

      await test.step("Entscheidungsgründe sind bearbeitbar", async () => {
        const decisionReasons = page.getByTestId("Entscheidungsgründe")
        const inputFieldInnerHTML = await decisionReasons.innerHTML()
        expect(inputFieldInnerHTML).toContain(editable)
      })

      await test.step("Gründe sind bearbeitbar", async () => {
        const reasons = page.getByTestId("Gründe")
        const inputFieldInnerHTML = await reasons.innerHTML()
        expect(inputFieldInnerHTML).toContain(editable)
      })
    },
  )
})
