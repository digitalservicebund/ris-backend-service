import { expect, Page } from "@playwright/test"
import { generateString } from "../test-helper/dataGenerators"
import { navigateToCategories } from "./e2e-utils"
import { testWithDocUnit as test } from "./fixtures"

async function clickSaveButton(page: Page): Promise<void> {
  await page.locator("[aria-label='Stammdaten Speichern Button']").click()
  await expect(
    page.locator("text=Zuletzt gespeichert um").first()
  ).toBeVisible()
}

async function togglePreviousDecisionsSection(page: Page): Promise<void> {
  await page.locator("id=previousDecisions").click()
}

async function fillPreviousDecisionInputs(
  page: Page,
  values?: {
    courtType?: string
    courtLocation?: string
    date?: string
    fileNumber?: string
  },
  decisionIndex = 0
): Promise<void> {
  const fillInput = async (ariaLabel: string, value?: string) => {
    await page
      .locator(`[aria-label='${ariaLabel}']`)
      .nth(decisionIndex)
      .fill(value ?? generateString())
  }

  await fillInput("Gerichtstyp Rechtszug", values?.courtType)
  await fillInput("Gerichtsort Rechtszug", values?.courtLocation)
  await fillInput("Datum Rechtszug", values?.date)
  await fillInput("Aktenzeichen Rechtszug", values?.fileNumber)
}

test.describe("save changes in core data and texts and verify it persists", () => {
  test("test core data change", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)

    await page.locator("[aria-label='Aktenzeichen']").fill("abc")

    await page.locator("[aria-label='Stammdaten Speichern Button']").click()

    await expect(
      page.locator("text=Zuletzt gespeichert um").first()
    ).toBeVisible()

    await page.reload()
    expect(await page.inputValue("[aria-label='Aktenzeichen']")).toBe("abc")
  })

  test("test previous decision data change", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await togglePreviousDecisionsSection(page)
    await fillPreviousDecisionInputs(page, {
      courtType: "Test Court",
      courtLocation: "Test City",
      date: "12.03.2004",
      fileNumber: "1a2b3c",
    })

    await clickSaveButton(page)
    await page.reload()
    await togglePreviousDecisionsSection(page)

    expect(await page.inputValue("[aria-label='Gerichtstyp Rechtszug']")).toBe(
      "Test Court"
    )
    expect(await page.inputValue("[aria-label='Gerichtsort Rechtszug']")).toBe(
      "Test City"
    )
    expect(await page.inputValue("[aria-label='Datum Rechtszug']")).toBe(
      "12.03.2004"
    )
    expect(await page.inputValue("[aria-label='Aktenzeichen Rechtszug']")).toBe(
      "1a2b3c"
    )
  })

  test("test add another empty previous decision", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await togglePreviousDecisionsSection(page)
    await fillPreviousDecisionInputs(page)
    await page.locator("[aria-label='weitere Entscheidung hinzufügen']").click()

    await page.pause()

    expect(page.locator("[aria-label='Gerichtstyp Rechtszug']")).toHaveCount(2)
    expect(
      await page
        .locator("[aria-label='Gerichtstyp Rechtszug']")
        .nth(1)
        .inputValue()
    ).toBe("")
    expect(page.locator("[aria-label='Gerichtsort Rechtszug']")).toHaveCount(2)
    expect(
      await page
        .locator("[aria-label='Gerichtsort Rechtszug']")
        .nth(1)
        .inputValue()
    ).toBe("")
    expect(page.locator("[aria-label='Datum Rechtszug']")).toHaveCount(2)
    expect(
      await page.locator("[aria-label='Datum Rechtszug']").nth(1).inputValue()
    ).toBe("")
    expect(page.locator("[aria-label='Aktenzeichen Rechtszug']")).toHaveCount(2)
    expect(
      await page
        .locator("[aria-label='Aktenzeichen Rechtszug']")
        .nth(1)
        .inputValue()
    ).toBe("")
  })

  test("test delete first previous decision", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    await togglePreviousDecisionsSection(page)
    await fillPreviousDecisionInputs(page, { courtType: "Type One" }, 0)
    await page.locator("[aria-label='weitere Entscheidung hinzufügen']").click()
    await fillPreviousDecisionInputs(page, { courtType: "Type Two" }, 1)

    expect(page.locator("[aria-label='Gerichtstyp Rechtszug']")).toHaveCount(2)
    expect(
      await page
        .locator("[aria-label='Gerichtstyp Rechtszug']")
        .nth(0)
        .inputValue()
    ).toBe("Type One")
    expect(
      await page
        .locator("[aria-label='Gerichtstyp Rechtszug']")
        .nth(1)
        .inputValue()
    ).toBe("Type Two")

    await page.locator("[aria-label='Entscheidung Entfernen']").click()

    expect(page.locator("[aria-label='Gerichtstyp Rechtszug']")).toHaveCount(1)
    expect(
      await page
        .locator("[aria-label='Gerichtstyp Rechtszug']")
        .nth(0)
        .inputValue()
    ).toBe("Type Two")
  })

  test("test bold text input", async ({ page, editorField }) => {
    await editorField.click()

    const boldButton = page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(2)
    await boldButton.click()
    await editorField.type("this is bold text")

    expect(await editorField.innerHTML()).toBe(
      "<p>this is text<strong>this is bold text</strong></p>"
    )
  })

  test("test italic test input", async ({ page, editorField }) => {
    await editorField.click()
    const italicButton = page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(3)
    await italicButton.click()
    await editorField.type("this is italic text")

    expect(await editorField.innerHTML()).toBe(
      "<p>this is text<em>this is italic text</em></p>"
    )
  })

  test("test underlined test input", async ({ page, editorField }) => {
    await editorField.click()

    const underlineButton = page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(4)
    await underlineButton.click()
    await editorField.type("this is underlined text")
    expect(await editorField.innerHTML()).toBe(
      "<p>this is text<u>this is underlined text</u></p>"
    )
  })

  test("test strike test input", async ({ page, editorField }) => {
    await editorField.click()

    const strikeButton = page
      .locator("[aria-label='Entscheidungsname Editor Button Leiste'] >> div")
      .nth(5)
    await strikeButton.click()
    await editorField.type("this is striked text")
    expect(await editorField.innerHTML()).toBe(
      "<p>this is text<s>this is striked text</s></p>"
    )
  })

  test("test superscript test input", async ({ page, editorField }) => {
    await editorField.click()

    const moreButton = page.locator(
      "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('more_horiz')"
    )
    await moreButton.click()
    const superScriptButton = page.locator("text=superscript").nth(1)
    await superScriptButton.click()

    await editorField.type("this is superscript text")
    expect(await editorField.innerHTML()).toBe(
      "<p>this is text<sup>this is superscript text</sup></p>"
    )
  })

  test("test subscript test input", async ({ page, editorField }) => {
    await editorField.click()

    const moreButton = page.locator(
      "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('more_horiz')"
    )
    await moreButton.click()
    const subScriptButton = page.locator("text=subscript").nth(1)
    await subScriptButton.click()
    await editorField.type("this is subscript text")
    expect(await editorField.innerHTML()).toBe(
      "<p>this is text<sub>this is subscript text</sub></p>"
    )
  })

  test("test right alignment text input", async ({ page, editorField }) => {
    const dropdownShowAlignmentButton = page
      .locator(
        "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('format_align_leftarrow_drop_down')"
      )
      .nth(0)
    await dropdownShowAlignmentButton.click()
    const rightAligmentButton = page.locator("text=format_align_right").nth(0)
    await rightAligmentButton.click()
    expect(await editorField.innerHTML()).toBe(
      '<p style="text-align: right">this is text</p>'
    )
  })

  test("test center alignment text input", async ({ page, editorField }) => {
    const dropdownShowAlignmentButton = page
      .locator(
        "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('format_align_leftarrow_drop_down')"
      )
      .nth(0)
    await dropdownShowAlignmentButton.click()
    const centerAligmentButton = page.locator("text=format_align_center").nth(0)
    await centerAligmentButton.click()

    expect(await editorField.innerHTML()).toBe(
      '<p style="text-align: center">this is text</p>'
    )
  })
  test("test left alignment text input", async ({ page, editorField }) => {
    const dropdownShowAlignmentButton = page
      .locator(
        "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('format_align_leftarrow_drop_down')"
      )
      .nth(0)
    await dropdownShowAlignmentButton.click()
    const leftAligmentButton = page.locator("text=format_align_left").nth(0)
    await leftAligmentButton.click()

    expect(await editorField.innerHTML()).toBe("<p>this is text</p>")
  })

  test("test justify alignment text input", async ({ page, editorField }) => {
    const dropdownShowAlignmentButton = page
      .locator(
        "[aria-label='Entscheidungsname Editor Button Leiste'] >> div:has-text('format_align_leftarrow_drop_down')"
      )
      .nth(0)
    await dropdownShowAlignmentButton.click()
    const justifyAligmentButton = page
      .locator("text=format_align_justify")
      .nth(0)
    await justifyAligmentButton.click()

    expect(await editorField.innerHTML()).toBe(
      '<p style="text-align: justify">this is text</p>'
    )
  })
})
