import { expect } from "@playwright/test"
import {
  clickCategoryButton,
  navigateToCategories,
  navigateToHandover,
  navigateToPreview,
  save,
  waitForInputValue,
} from "../../e2e-utils"
import { caselawTest as test } from "../../fixtures"
import { DocumentUnitCatagoriesEnum } from "@/components/enumDocumentUnitCatagories"

test.describe("short and long texts", () => {
  test(
    "decision name should update",
    {
      annotation: {
        type: "issue",
        description:
          "https://digitalservicebund.atlassian.net/browse/RISDEV-4733",
      },
      tag: ["@RISDEV-4733"],
    },
    async ({ page, documentNumber }) => {
      await navigateToCategories(page, documentNumber, {
        category: DocumentUnitCatagoriesEnum.TEXTS,
      })
      await clickCategoryButton("Entscheidungsname", page)
      const inputText = "Family-Karte"
      const selector = "[aria-label='Entscheidungsname']"
      await page.locator(selector).fill(inputText)
      await waitForInputValue(page, selector, inputText)
      await save(page)
      await expect(page.locator(selector)).toHaveValue(inputText)
    },
  )

  test("text editor fields should have predefined height", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    // small
    await clickCategoryButton("Titelzeile", page)
    const smallEditor = page.locator("[data-testid='Titelzeile']")
    const smallEditorHeight = await smallEditor.evaluate((element) =>
      window.getComputedStyle(element).getPropertyValue("height"),
    )
    expect(parseInt(smallEditorHeight)).toBeGreaterThanOrEqual(60)

    //medium
    await clickCategoryButton("Leitsatz", page)
    const mediumEditor = page.locator("[data-testid='Leitsatz']")
    const mediumEditorHeight = await mediumEditor.evaluate((element) =>
      window.getComputedStyle(element).getPropertyValue("height"),
    )
    expect(parseInt(mediumEditorHeight)).toBeGreaterThanOrEqual(120)

    //large
    await clickCategoryButton("Gründe", page)
    const largeEditor = page.locator("[data-testid='Gründe']")
    const largeEditorHeight = await largeEditor.evaluate((element) =>
      window.getComputedStyle(element).getPropertyValue("height"),
    )
    expect(parseInt(largeEditorHeight)).toBeGreaterThanOrEqual(320)
  })

  test("toggle invisible characters", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await clickCategoryButton("Leitsatz", page)
    const guidingPrincipleInput = page.locator("[data-testid='Leitsatz']")
    await guidingPrincipleInput.click()
    await page.keyboard.type(`this is a test paragraph`)

    await expect(
      guidingPrincipleInput.locator("[class='ProseMirror-trailingBreak']"),
    ).toHaveCount(1)
    await page
      .locator(`[aria-label='Nicht-druckbare Zeichen']:not([disabled])`)
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

      await clickCategoryButton("Leitsatz", page)
      const guidingPrincipleInput = page.locator(
        "[data-testid='Leitsatz'] > div.tiptap",
      )
      await guidingPrincipleInput.click()
      await expect(guidingPrincipleInput).toBeFocused()

      // Write text and select all
      await page.keyboard.type("Text input")

      // Wait for text to be typed in the editor and selected via keyboard
      // eslint-disable-next-line playwright/no-wait-for-timeout
      await page.waitForTimeout(100)
      await page.keyboard.press("ControlOrMeta+A")
      // eslint-disable-next-line playwright/no-wait-for-timeout
      await page.waitForTimeout(100)

      // Navigate to toolbar -> first button is focused
      await page.keyboard.press("Shift+Tab")
      const firstButton = page
        .getByLabel("Leitsatz Button Leiste")
        .getByLabel("Erweitern")
      await expect(firstButton).toBeFocused()

      // Navigate to bold button with arrow keys
      await page.keyboard.press("ArrowRight")
      await page.keyboard.press("ArrowRight")
      await page.keyboard.press("ArrowRight")
      await page.keyboard.press("ArrowLeft")
      const boldButton = page
        .getByLabel("Leitsatz Button Leiste")
        .getByLabel("Fett")
      await expect(boldButton).toBeFocused()

      // Pressing enter moves focus to the editor
      await page.keyboard.press("Enter")
      await expect(guidingPrincipleInput).toBeFocused()

      // The timeout is needed for the focus, otherwise tabbing back to the menu is flaky. It's unclear why.
      // eslint-disable-next-line playwright/no-wait-for-timeout
      await page.waitForTimeout(300)

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
        .getByLabel("Linksbündig")
      await expect(leftButton).toBeFocused()

      // Close submenu with ESC
      await page.keyboard.press("Escape")
      await expect(leftButton).toBeHidden()
    },
  )

  test(
    "long texts are hidden for external user",
    {
      annotation: {
        type: "story",
        description:
          "https://digitalservicebund.atlassian.net/browse/RISDEV-4523",
      },
      tag: ["@RISDEV-4523"],
    },
    async ({ pageWithExternalUser, documentNumber }) => {
      await test.step("Navigiere zu Rubriken als external Nutzer", async () => {
        await navigateToCategories(pageWithExternalUser, documentNumber)
      })

      await test.step("Tenor ist nicht sichtbar", async () => {
        const button = pageWithExternalUser.getByRole("button", {
          name: "Tenor",
          exact: true,
        })
        await expect(button).toBeHidden()
      })

      await test.step("Tatbestand ist nicht sichtbar", async () => {
        const button = pageWithExternalUser.getByRole("button", {
          name: "Tatbestand",
          exact: true,
        })
        await expect(button).toBeHidden()
      })

      await test.step("Entscheidungsgründe sind nicht sichtbar", async () => {
        const button = pageWithExternalUser.getByRole("button", {
          name: "Entscheidungsgründe",
          exact: true,
        })
        await expect(button).toBeHidden()
      })

      await test.step("Gründe sind nicht sichtbar", async () => {
        const button = pageWithExternalUser.getByRole("button", {
          name: "Gründe",
          exact: true,
        })
        await expect(button).toBeHidden()
      })

      await test.step("Abweichende Meinung ist nicht sichtbar", async () => {
        const button = pageWithExternalUser.getByRole("button", {
          name: "Abweichende Meinung",
          exact: true,
        })
        await expect(button).toBeHidden()
      })

      await test.step("Mitwirkende Richter ist nicht sichtbar", async () => {
        const button = pageWithExternalUser.getByRole("button", {
          name: "Mitwirkende Richter",
          exact: true,
        })
        await expect(button).toBeHidden()
      })

      await test.step("Sonstiger Langtext ist nicht sichtbar", async () => {
        const button = pageWithExternalUser.getByRole("button", {
          name: "Sonstiger Langtext",
          exact: true,
        })
        await expect(button).toBeHidden()
      })

      await test.step("Gliederung ist nicht sichtbar", async () => {
        const button = pageWithExternalUser.getByRole("button", {
          name: "Gliederung",
          exact: true,
        })
        await expect(button).toBeHidden()
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
        await clickCategoryButton("Tenor", page)
        const tenor = page.getByTestId("Tenor")
        const inputFieldInnerHTML = await tenor.innerHTML()
        expect(inputFieldInnerHTML).toContain(editable)
      })

      await test.step("Tatbestand ist bearbeitbar", async () => {
        await clickCategoryButton("Tatbestand", page)
        const caseFacts = page.getByTestId("Tatbestand")
        const inputFieldInnerHTML = await caseFacts.innerHTML()
        expect(inputFieldInnerHTML).toContain(editable)
      })

      await test.step("Entscheidungsgründe sind bearbeitbar", async () => {
        await clickCategoryButton("Entscheidungsgründe", page)
        const decisionReasons = page.getByTestId("Entscheidungsgründe")
        const inputFieldInnerHTML = await decisionReasons.innerHTML()
        expect(inputFieldInnerHTML).toContain(editable)
      })

      await test.step("Gründe sind bearbeitbar", async () => {
        await clickCategoryButton("Gründe", page)
        const reasons = page.getByTestId("Gründe")
        const inputFieldInnerHTML = await reasons.innerHTML()
        expect(inputFieldInnerHTML).toContain(editable)
      })

      await test.step("Sonstiger Langtext ist bearbeitbar", async () => {
        await clickCategoryButton("Sonstiger Langtext", page)
        const otherLongText = page.getByTestId("Sonstiger Langtext")
        const inputFieldInnerHTML = await otherLongText.innerHTML()
        expect(inputFieldInnerHTML).toContain(editable)
      })

      await test.step("Gliederung ist bearbeitbar", async () => {
        await clickCategoryButton("Gliederung", page)
        const outline = page.getByTestId("Gliederung")
        const inputFieldInnerHTML = await outline.innerHTML()
        expect(inputFieldInnerHTML).toContain(editable)
      })

      await test.step("Abweichende Meinung ist bearbeitbar", async () => {
        await clickCategoryButton("Abweichende Meinung", page)
        const dissentingOpinion = page.getByTestId("Abweichende Meinung")
        const inputFieldInnerHTML = await dissentingOpinion.innerHTML()
        expect(inputFieldInnerHTML).toContain(editable)
      })

      await test.step("Mitwirkende Richter ist bearbeitbar", async () => {
        await clickCategoryButton("Mitwirkende Richter", page)
        const nameInput = page.getByTestId("participating-judge-name")
        const referenceOpinionsInput = page.getByTestId(
          "participating-judge-reference-opinions",
        )

        await expect(nameInput).toBeEditable()
        await expect(referenceOpinionsInput).toBeEditable()
      })
    },
  )

  test(
    "text editor input should be saved and displayed in preview and in 'XML-Vorschau'",
    {
      annotation: [
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4570",
        },
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4572",
        },
        {
          type: "story",
          description:
            "https://digitalservicebund.atlassian.net/browse/RISDEV-4573",
        },
      ],
      tag: ["@RISDEV-4570", "@RISDEV-4572", "@RISDEV-4573"],
    },
    async ({ page, prefilledDocumentUnit }) => {
      const testCases: {
        testId: string
        value: string
      }[] = [
        {
          testId: "Titelzeile",
          value: "Titelzeile Test Text",
        },
        {
          testId: "Leitsatz",
          value: "Leitsatz Test Text",
        },
        {
          testId: "Orientierungssatz",
          value: "Orientierungssatz Test Text",
        },
        {
          testId: "Sonstiger Orientierungssatz",
          value: "Sonstiger Orientierungssatz Test Text",
        },
        {
          testId: "Tenor",
          value: "Tenor Test Text",
        },
        {
          testId: "Gründe",
          value: "Gründe Test Text",
        },
        {
          testId: "Tatbestand",
          value: "Tatbestand Test Text",
        },
        {
          testId: "Entscheidungsgründe",
          value: "Entscheidungsgründe Test Text",
        },
        {
          testId: "Abweichende Meinung",
          value: "Abweichende Meinung Test Text",
        },
        {
          testId: "Sonstiger Langtext",
          value: "Sonstiger Langtext Test Text",
        },
        {
          testId: "Gliederung",
          value: "Gliederung Test Text",
        },
      ]

      for (let index = 0; index < testCases.length; index++) {
        const testId = testCases[index].testId
        const value = testCases[index].value
        const selector = `[data-testid='${testId}']`

        await navigateToCategories(page, prefilledDocumentUnit.documentNumber!)

        // Leitsatz and Orientierungssatz are prefilled. Entscheidungsgründe is filled for Tatbestand.
        // Therefore, the button doesn't need to be clicked.
        // eslint-disable-next-line playwright/no-conditional-in-test
        if (
          // eslint-disable-next-line playwright/no-conditional-in-test
          testId != "Leitsatz" &&
          testId != "Orientierungssatz" &&
          testId != "Entscheidungsgründe"
        ) {
          await clickCategoryButton(testId, page)
        }

        await test.step(`text field '${testId}' should be filled with value '${value}'`, async () => {
          const textField = page.locator(selector)
          await textField.click()
          await page.keyboard.press(`ControlOrMeta+A`)
          await page.keyboard.press(`ControlOrMeta+Backspace`)
          await page.keyboard.type(value)
          const innerText = await textField.innerText()
          expect(innerText).toContain(value)
        })

        // Tatbestand can only be filled when Entscheidungsgründe is given.
        // eslint-disable-next-line playwright/no-conditional-in-test
        if (testId == "Tatbestand") {
          await clickCategoryButton("Entscheidungsgründe", page)
          const textField = page.locator(`[data-testid='Entscheidungsgründe']`)
          await textField.click()
          await page.keyboard.type("Test")
          const innerText = await textField.innerText()
          // eslint-disable-next-line playwright/no-conditional-expect
          expect(innerText).toContain("Test")
        }

        // Outline (Gliederung) and OtherHeadLine (Sonst O-Satz) must not be filled at the same time
        // eslint-disable-next-line playwright/no-conditional-in-test
        if (testId == "Gliederung") {
          const textField = page.locator(
            `[data-testid='Sonstiger Orientierungssatz']`,
          )
          await textField.click()
          await page.keyboard.press(`ControlOrMeta+A`)
          await page.keyboard.press(`ControlOrMeta+Backspace`)
          const innerText = await textField.innerText()
          // eslint-disable-next-line playwright/no-conditional-expect
          expect(innerText).toContain("")
        }

        // Casefacts (Tatbestand) and Reasons (Gründe) must not be filled at the same time
        // eslint-disable-next-line playwright/no-conditional-in-test
        if (testId == "Tatbestand") {
          const textField = page.locator(`[data-testid='Gründe']`)
          await textField.click()
          await page.keyboard.press(`ControlOrMeta+A`)
          await page.keyboard.press(`ControlOrMeta+Backspace`)
          const innerText = await textField.innerText()
          // eslint-disable-next-line playwright/no-conditional-expect
          expect(innerText).toContain("")
        }

        await save(page)

        await test.step(`value '${value}' should be saved (be present after reload)`, async () => {
          await page.reload()
          const textField = page.locator(selector)
          const innerText = await textField.innerText()
          expect(innerText).toContain(value)
        })

        await test.step(`text field '${testId}' and value '${value}' should be visible in preview`, async () => {
          await navigateToPreview(
            page,
            prefilledDocumentUnit.documentNumber as string,
          )

          await expect(page.getByText(testId, { exact: true })).toBeVisible()
          await expect(
            page.getByText(value, {
              exact: true,
            }),
          ).toBeVisible()
        })

        await test.step(`text field '${testId}' and value '${value}' should be visible in 'XML-Vorschau'`, async () => {
          await navigateToHandover(page, prefilledDocumentUnit.documentNumber!)
          const xmlPreview = page.getByTitle("XML Vorschau")
          await expect(xmlPreview).toBeVisible()
          const button = page.getByRole("button", { name: "aufklappen" })
          await expect(button).toBeVisible()
          await button.click()
          const innerText = await xmlPreview.innerText()
          expect(innerText).toContain(value)
        })
      }
    },
  )
})
