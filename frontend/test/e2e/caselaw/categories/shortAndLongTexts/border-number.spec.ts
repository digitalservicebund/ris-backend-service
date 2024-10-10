import { expect, Locator, Page } from "@playwright/test"
import {
  navigateToCategories,
  navigateToAttachments,
  uploadTestfile,
  copyPasteTextFromAttachmentIntoEditor,
  clickCategoryButton,
} from "../../e2e-utils"
import { caselawTest as test } from "../../fixtures"

// eslint-disable-next-line playwright/no-skipped-test
test.skip(
  ({ browserName }) => browserName !== "chromium",
  "Skipping in engines other than chromium, reason playwright driven for firefox and safari does not support copy paste type='text/html' from clipboard",
)

test.beforeEach(async ({ page, documentNumber }) => {
  await navigateToAttachments(page, documentNumber)
})

const documentOrigin = "Headline:"
const firstParagraph = "First paragraph"
const secondParagraph = "Second paragraph"
const thirdParagraph = "Third paragraph"
const firstParagraphHtml =
  '<span style="color: rgb(0, 0, 0)">First <strong>paragraph</strong></span>'

test.describe(
  "Remove border numbers (Randnummern)",
  {
    annotation: [
      {
        type: "story",
        description:
          "https://digitalservicebund.atlassian.net/browse/RISDEV-4119",
      },
      {
        type: "story",
        description:
          "https://digitalservicebund.atlassian.net/browse/RISDEV-4973",
      },
    ],
    tag: ["@RISDEV-4119", "@RISDEV-4973"],
  },
  () => {
    /*
      1. Upload document with border numbers
      2. Copy border Numbers into reasons
      3. Select all and delete border Numbers via button
      4. Select first border number text and delete via button
      5. Delete border number via backspace in content
      6. delete border number via backspace in number
      7. Insert cursor into first border number text and delete via button
      8. Select first paragraph (without border number) and click button (only recalculates the following border numbers)
    */
    test("delete border Numbers (Randnummern) via button and backspace in 'Gründe'", async ({
      page,
      documentNumber,
    }) => {
      // eslint-disable-next-line playwright/no-conditional-in-test
      const modifier = (await page.evaluate(() => navigator.platform))
        .toLowerCase()
        .includes("mac")
        ? "Meta"
        : "Control"

      await test.step("Upload file with border Numbers", async () => {
        await uploadTestfile(page, "some-border-numbers.docx")
        await expect(page.getByText("some-border-numbers.docx")).toBeVisible()
        await expect(page.getByLabel("Datei löschen")).toBeVisible()
        await expect(page.getByText(firstParagraph)).toBeVisible()
        await expect(page.getByText(secondParagraph)).toBeVisible()
        await expect(page.getByText(thirdParagraph)).toBeVisible()
      })

      await test.step("Click on 'Rubriken' und check if original document loaded", async () => {
        await navigateToCategories(page, documentNumber)
        await expect(page.getByLabel("Ladestatus")).toBeHidden()
        await expect(page.getByText(firstParagraph)).toBeVisible()
        await expect(page.getByText(secondParagraph)).toBeVisible()
        await expect(page.getByText(thirdParagraph)).toBeVisible()
        await expect(page.getByText(documentOrigin)).toBeVisible()
      })

      const attachmentLocator = page
        .getByText(documentOrigin)
        .locator("..")
        .locator("..")
        .locator("..")

      await clickCategoryButton("Gründe", page)
      const editor = page.locator("[data-testid='Gründe']")

      await test.step("Copy border numbers from side panel into 'Gründe' to have reference data", async () => {
        await copyPasteTextFromAttachmentIntoEditor(
          page,
          attachmentLocator,
          editor,
        )
      })

      await checkAllBorderNumbersAreVisible(editor)

      await checkStyleOfFirstParagraph(editor)

      await test.step("Select all text", async () => {
        await page.keyboard.press(`${modifier}+KeyA`)
      })

      await clickRemoveBorderNumberButton(page)

      await checkAllBorderNumbersAreRemoved(editor)

      await reinsertAllBorderNumbers(page, modifier)

      await checkAllBorderNumbersAreVisible(editor)

      await checkStyleOfFirstParagraph(editor)

      await test.step("Select text of first border number", async () => {
        await editor.getByText(firstParagraph).selectText()
      })

      await clickRemoveBorderNumberButton(page)

      await checkFirstBorderNumberIsRemoved(editor)

      await checkOtherBorderNumbersAreRecalculated(editor)

      await reinsertAllBorderNumbers(page, modifier)

      await checkAllBorderNumbersAreVisible(editor)

      await checkStyleOfFirstParagraph(editor)

      await test.step("Navigate cursor to the start of the first border number content", async () => {
        await editor.getByText(firstParagraph).selectText()
        await page.keyboard.press("ArrowLeft")
      })

      await clickBackspace(page)

      await checkFirstBorderNumberIsRemoved(editor)

      await checkOtherBorderNumbersAreRecalculated(editor)

      await reinsertAllBorderNumbers(page, modifier)

      await checkAllBorderNumbersAreVisible(editor)

      await checkStyleOfFirstParagraph(editor)

      await test.step("Navigate cursor to the first border number number", async () => {
        await editor.getByText(firstParagraph).selectText()
        await page.keyboard.press("ArrowLeft")
        await page.keyboard.press("ArrowLeft")
      })

      await clickBackspace(page)

      await checkFirstBorderNumberIsRemoved(editor)

      await checkOtherBorderNumbersAreRecalculated(editor)

      await reinsertAllBorderNumbers(page, modifier)

      await checkAllBorderNumbersAreVisible(editor)

      await checkStyleOfFirstParagraph(editor)

      await test.step("Navigate cursor inside first border number content (without selection)", async () => {
        await editor.getByText(firstParagraph).click()
      })

      await clickRemoveBorderNumberButton(page)

      await checkFirstBorderNumberIsRemoved(editor)

      await checkOtherBorderNumbersAreRecalculated(editor)

      await test.step("Change number of second border number to 99", async () => {
        await editor.getByText("2").selectText()
        await page.keyboard.insertText("99")
      })

      await test.step("Select text of first border number which has been removed", async () => {
        await editor.getByText(firstParagraph).selectText()
      })

      await clickRemoveBorderNumberButton(page)

      await checkOtherBorderNumbersAreRecalculated(editor)
    })

    test("delete border Numbers (Randnummern) in 'Leitsatz'", async ({
      page,
      documentNumber,
    }) => {
      await test.step("Upload file with border Numbers", async () => {
        await uploadTestfile(page, "some-border-numbers.docx")
        await expect(page.getByText("some-border-numbers.docx")).toBeVisible()
        await expect(page.getByLabel("Datei löschen")).toBeVisible()
        await expect(page.getByText(firstParagraph)).toBeVisible()
        await expect(page.getByText(secondParagraph)).toBeVisible()
        await expect(page.getByText(thirdParagraph)).toBeVisible()
      })

      await test.step("Click on 'Rubriken' und check if original document loaded", async () => {
        await navigateToCategories(page, documentNumber)
        await expect(page.getByLabel("Ladestatus")).toBeHidden()
        await expect(page.getByText(firstParagraph)).toBeVisible()
        await expect(page.getByText(secondParagraph)).toBeVisible()
        await expect(page.getByText(thirdParagraph)).toBeVisible()
        await expect(page.getByText(documentOrigin)).toBeVisible()
      })

      const attachmentLocator = page
        .getByText(documentOrigin)
        .locator("..")
        .locator("..")
        .locator("..")

      await clickCategoryButton("Leitsatz", page)
      const editor = page.locator("[data-testid='Leitsatz']")

      await test.step("Copy border numbers from side panel into 'Leitsatz' to have reference data", async () => {
        await copyPasteTextFromAttachmentIntoEditor(
          page,
          attachmentLocator,
          editor,
        )
      })

      await checkAllBorderNumbersAreVisible(editor)

      await checkStyleOfFirstParagraph(editor)

      await test.step("Select text of first border number", async () => {
        await editor.getByText(firstParagraph).selectText()
      })

      await clickRemoveBorderNumberButton(page)

      await checkFirstBorderNumberIsRemoved(editor)

      await checkOtherBorderNumbersAreNotRecalculated(editor)
    })
  },
)

test.describe(
  "Add border numbers (Randnummern)",
  {
    annotation: [
      {
        type: "story",
        description:
          "https://digitalservicebund.atlassian.net/browse/RISDEV-4973",
      },
    ],
    tag: ["@RISDEV-4973"],
  },
  () => {
    // eslint-disable-next-line playwright/expect-expect
    test("add border Numbers (Randnummern) via button in 'Gründe'", async ({
      page,
      documentNumber,
    }) => {
      // eslint-disable-next-line playwright/no-conditional-in-test
      const modifier = (await page.evaluate(() => navigator.platform))
        .toLowerCase()
        .includes("mac")
        ? "Meta"
        : "Control"
      await navigateToCategories(page, documentNumber)

      await clickCategoryButton("Gründe", page)
      const editor = page.locator("[data-testid='Gründe']")

      await test.step("Add three paragraphs into Gründe", async () => {
        await page.keyboard.insertText(firstParagraph)
        await page.keyboard.press("Enter")
        await page.keyboard.insertText(secondParagraph)
        await page.keyboard.press("Enter")
        await page.keyboard.insertText(thirdParagraph)
        await page.keyboard.press("Enter")
      })

      await checkAllParagraphsAreVisible(editor)

      await test.step("Select all text", async () => {
        await page.keyboard.press(`${modifier}+KeyA`)
      })

      await clickAddBorderNumberButton(page)

      await checkAllBorderNumbersAreVisible(editor)

      await test.step("Change number of second border number to 99", async () => {
        await editor.getByText("2").selectText()
        await page.keyboard.insertText("99")
      })

      await test.step("Select text of first border number", async () => {
        await editor.getByText(firstParagraph).selectText()
      })

      await clickAddBorderNumberButton(page)

      await checkAllBorderNumbersAreVisible(editor)
    })
  },
)

test.describe(
  "Fuse border numbers (Randnummern)",
  {
    annotation: [
      {
        type: "story",
        description:
          "https://digitalservicebund.atlassian.net/browse/RISDEV-4973",
      },
    ],
    tag: ["@RISDEV-4973"],
  },
  () => {
    test("fuse two border Numbers (Randnummern)", async ({
      page,
      documentNumber,
    }) => {
      // eslint-disable-next-line playwright/no-conditional-in-test
      const modifier = (await page.evaluate(() => navigator.platform))
        .toLowerCase()
        .includes("mac")
        ? "Meta"
        : "Control"
      await navigateToCategories(page, documentNumber)

      await clickCategoryButton("Gründe", page)
      const editor = page.locator("[data-testid='Gründe']")

      await test.step("Add three paragraphs into Gründe", async () => {
        await page.keyboard.insertText(firstParagraph)
        await page.keyboard.press("Enter")
        await page.keyboard.insertText(secondParagraph)
        await page.keyboard.press("Enter")
        await page.keyboard.insertText(thirdParagraph)
        await page.keyboard.press("Enter")
      })

      await checkAllParagraphsAreVisible(editor)

      await test.step("Select all text", async () => {
        await page.keyboard.press(`${modifier}+KeyA`)
      })

      await clickAddBorderNumberButton(page)

      await checkAllBorderNumbersAreVisible(editor)

      await test.step("Fuse the second with the first border number", async () => {
        await editor.getByText(secondParagraph).selectText()
        await page.keyboard.press("ArrowLeft")
        await clickBackspace(page)
        await clickBackspace(page)
        await page.keyboard.press("Enter")
      })

      await test.step("Check the second border number is gone and third border number is recalculated", async () => {
        const inputFieldInnerText = await editor.innerText()
        expect(inputFieldInnerText).toContain("1\n\n" + firstParagraph)
        expect(inputFieldInnerText).not.toContain("2\n\n" + secondParagraph)
        expect(inputFieldInnerText).toContain(secondParagraph)
        expect(inputFieldInnerText).toContain("2\n\n" + thirdParagraph)
      })
    })
  },
)

async function clickBackspace(page: Page) {
  await test.step("Press Backspace", async () => {
    await page.keyboard.press("Backspace")
  })
}

async function clickRemoveBorderNumberButton(page: Page) {
  await test.step("Click remove border number button to delete border numbers from selection", async () => {
    await page
      .locator(`[aria-label='Randnummern entfernen']:not([disabled])`)
      .click()
  })
}

async function clickAddBorderNumberButton(page: Page) {
  await test.step("Click add number button to add border numbers to selection", async () => {
    await page
      .locator(`[aria-label='Randnummern neu erstellen']:not([disabled])`)
      .click()
  })
}

async function reinsertAllBorderNumbers(
  page: Page,
  modifier: "Meta" | "Control",
) {
  await test.step("Reinsert all border numbers", async () => {
    await page.keyboard.press(`${modifier}+KeyA`)
    await page.keyboard.press(`${modifier}+KeyV`)
  })
}

async function checkAllParagraphsAreVisible(editor: Locator) {
  await test.step("Check all paragraphs are visible and have correct sequence", async () => {
    const inputFieldInnerText = await editor.innerText()
    expect(inputFieldInnerText).toContain(firstParagraph)
    expect(inputFieldInnerText).toContain(secondParagraph)
    expect(inputFieldInnerText).toContain(thirdParagraph)
  })
}

async function checkAllBorderNumbersAreVisible(editor: Locator) {
  await test.step("Check all border numbers are visible and have correct sequence", async () => {
    const inputFieldInnerText = await editor.innerText()
    expect(inputFieldInnerText).toContain("1\n\n" + firstParagraph)
    expect(inputFieldInnerText).toContain("2\n\n" + secondParagraph)
    expect(inputFieldInnerText).toContain("3\n\n" + thirdParagraph)
  })
}

async function checkStyleOfFirstParagraph(editor: Locator) {
  const inputFieldInnerHtml = await editor.innerHTML()
  expect(inputFieldInnerHtml).toContain(firstParagraphHtml)
}

async function checkFirstBorderNumberIsRemoved(editor: Locator) {
  await test.step("Check the first border Number is removed", async () => {
    const inputFieldInnerText = await editor.innerText()
    const inputFieldInnerHtml = await editor.innerHTML()
    expect(inputFieldInnerText).not.toContain("1\n\n" + firstParagraph)
    expect(inputFieldInnerText).toContain(firstParagraph)
    expect(inputFieldInnerHtml).toContain(firstParagraphHtml)
  })
}

async function checkOtherBorderNumbersAreRecalculated(editor: Locator) {
  await test.step("Check the other border numbers are recalculated", async () => {
    const inputFieldInnerText = await editor.innerText()
    expect(inputFieldInnerText).toContain("1\n\n" + secondParagraph)
    expect(inputFieldInnerText).toContain("2\n\n" + thirdParagraph)
  })
}

async function checkOtherBorderNumbersAreNotRecalculated(editor: Locator) {
  await test.step("Check the other border numbers are not recalculated", async () => {
    const inputFieldInnerText = await editor.innerText()
    expect(inputFieldInnerText).toContain("2\n\n" + secondParagraph)
    expect(inputFieldInnerText).toContain("3\n\n" + thirdParagraph)
  })
}

async function checkAllBorderNumbersAreRemoved(editor: Locator) {
  await test.step("Check all border Numbers have gone", async () => {
    const inputFieldInnerText = await editor.innerText()
    expect(inputFieldInnerText).not.toContain("1\n\n" + firstParagraph)
    expect(inputFieldInnerText).not.toContain("2\n\n" + secondParagraph)
    expect(inputFieldInnerText).not.toContain("3\n\n" + thirdParagraph)
  })
}
