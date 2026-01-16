import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  clickCategoryButton,
  navigateToCategories,
  navigateToHandover,
  save,
} from "~/e2e/caselaw/utils/e2e-utils"

test.describe(
  "Create lists",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4112",
    },
    tag: ["@RISDEV-4112"],
  },
  () => {
    test("Enter text and make it into a bullet list, check that list style is applied", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      const bulletList = `<ul class="list-disc"><li><p>This is a bullet list</p></li><li><p>Second bullet list item</p></li></ul>`
      const bulletListXMLPreview = `<ul class="list-disc">`

      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      await clickCategoryButton("Gründe", page)
      const inputField = page.getByTestId("Gründe")
      await inputField.click()
      await page.keyboard.type("This is a bullet list")
      await page
        .locator(`[aria-label='Aufzählungsliste']:not([disabled])`)
        .click()
      await page.keyboard.press("Enter")
      await page.keyboard.type("Second bullet list item")

      // hide invisible characters
      await page
        .locator(`[aria-label='Nicht-druckbare Zeichen']:not([disabled])`)
        .click()

      const inputFieldInnerHTML = await inputField.innerHTML()
      // Check text styling
      expect(inputFieldInnerHTML).toContain(bulletList)
      await save(page)

      await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
      await expect(page.getByText("XML Vorschau")).toBeVisible()
      await page.getByText("XML Vorschau").click()

      await expect(page.getByText(bulletListXMLPreview)).toBeVisible()
    })

    test("Enter text and make it into an ordered list, check that list style is applied", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      const orderedList = `<ol style="list-style-type: decimal;"><li><p>This is an ordered list</p></li><li><p>Second ordered list item</p></li></ol>`
      const orderedListXMLPreview = `<ol style="list-style-type: decimal;">`

      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      await clickCategoryButton("Gründe", page)
      const inputField = page.getByTestId("Gründe")
      await inputField.click()
      await page.keyboard.type("This is an ordered list")
      await page
        .locator(`[aria-label='Nummerierte Liste']:not([disabled])`)
        .click()
      await page.getByLabel("Numerisch (1, 2, 3)").click()
      await page.keyboard.press("Enter")
      await page.keyboard.type("Second ordered list item")

      // hide invisible characters
      await page
        .locator(`[aria-label='Nicht-druckbare Zeichen']:not([disabled])`)
        .click()

      const inputFieldInnerHTML = await inputField.innerHTML()
      // Check text styling
      expect(inputFieldInnerHTML).toContain(orderedList)

      await save(page)

      await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
      await expect(page.getByText("XML Vorschau")).toBeVisible()
      await page.getByText("XML Vorschau").click()

      await expect(page.getByText(orderedListXMLPreview)).toBeVisible()
    })

    test("Switch between list types", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      const bulletList = `<ul class="list-disc"><li><p>This is a list</p></li></ul>`
      const orderedList = `<ol style="list-style-type: decimal;"><li><p>This is a list</p></li></ol>`
      const noList = `<p>This is a list</p>`

      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      await clickCategoryButton("Gründe", page)
      const inputField = page.getByTestId("Gründe")
      await inputField.click()
      // hide invisible characters
      await page
        .locator(`[aria-label='Nicht-druckbare Zeichen']:not([disabled])`)
        .click()

      await page.keyboard.type("This is a list")
      await page
        .locator(`[aria-label='Aufzählungsliste']:not([disabled])`)
        .click()
      let inputFieldInnerHTML = await inputField.innerHTML()
      // Check text styling
      expect(inputFieldInnerHTML).toContain(bulletList)

      await page
        .locator(`[aria-label='Nummerierte Liste']:not([disabled])`)
        .click()
      await page.getByLabel("Numerisch (1, 2, 3)").click()
      inputFieldInnerHTML = await inputField.innerHTML()
      expect(inputFieldInnerHTML).toContain(orderedList)

      await page
        .locator(`[aria-label='Nummerierte Liste']:not([disabled])`)
        .click()
      inputFieldInnerHTML = await inputField.innerHTML()
      expect(inputFieldInnerHTML).toContain(noList)
    })

    test("Toggle list types with keyboard shortcut", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      const bulletList = `<ul class="list-disc"><li><p>This is a list</p></li></ul>`
      const orderedList = `<ol style="list-style-type: decimal;"><li><p>This is a list</p></li></ol>`
      const noList = `<p>This is a list</p>`

      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      await clickCategoryButton("Gründe", page)
      const inputField = page.getByTestId("Gründe")
      await inputField.click()
      // hide invisible characters
      await page
        .locator(`[aria-label='Nicht-druckbare Zeichen']:not([disabled])`)
        .click()

      await page.keyboard.type("This is a list")
      await page.keyboard.press(`ControlOrMeta+Shift+8`)

      let inputFieldInnerHTML = await inputField.innerHTML()
      // Check text styling
      expect(inputFieldInnerHTML).toContain(bulletList)

      await page.keyboard.press(`ControlOrMeta+Shift+7`)
      inputFieldInnerHTML = await inputField.innerHTML()
      expect(inputFieldInnerHTML).toContain(orderedList)

      await page.keyboard.press(`ControlOrMeta+Shift+7`)
      inputFieldInnerHTML = await inputField.innerHTML()
      expect(inputFieldInnerHTML).toContain(noList)
    })
  },
)

test.describe(
  "Editor toolbar use",
  {
    annotation: {
      type: "story",
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-4112",
    },
    tag: ["@RISDEV-4112", "@RISDEV-6646", "@RISDEV-9334"],
  },
  () => {
    test("Enter text and make it into an ordered list with decimal numbers", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      const orderedListXMLPreview = `<ol style="list-style-type: decimal;">`

      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      await clickCategoryButton("Gründe", page)
      const inputField = page.getByTestId("Gründe")
      await inputField.click()
      await page.keyboard.type("This is an ordered list")
      await page
        .locator(`[aria-label='Nummerierte Liste']:not([disabled])`)
        .click()
      await page.getByLabel("Numerisch (1, 2, 3)").click()
      await page.keyboard.press("Enter")
      await page.keyboard.type("Second ordered list item")

      // hide invisible characters
      await page
        .locator(`[aria-label='Nicht-druckbare Zeichen']:not([disabled])`)
        .click()

      // verify list structure
      const orderedList = inputField.locator("ol").first()
      await expect(orderedList).toHaveAttribute(
        "style",
        "list-style-type: decimal;",
      )

      const listItems = inputField.locator("ol > li")
      await expect(listItems).toHaveCount(2)

      await expect(
        inputField.getByText("This is an ordered list"),
      ).toBeVisible()
      await expect(
        inputField.getByText("Second ordered list item"),
      ).toBeVisible()

      await save(page)

      await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
      await expect(page.getByText("XML Vorschau")).toBeVisible()
      await page.getByText("XML Vorschau").click()

      await expect(page.getByText(orderedListXMLPreview)).toBeVisible()
    })

    test("Enter text and make it into an ordered list with roman small enumeration", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      const orderedListXMLPreview = `<ol style="list-style-type: lower-roman;">`

      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      await clickCategoryButton("Gründe", page)
      const inputField = page.getByTestId("Gründe")
      await inputField.click()
      await page.keyboard.type("This is an ordered list")
      await page
        .locator(`[aria-label='Nummerierte Liste']:not([disabled])`)
        .click()
      await page.getByLabel("Römisch klein (i, ii, iii)").click()
      await page.keyboard.press("Enter")
      await page.keyboard.type("Second ordered list item")

      // hide invisible characters
      await page
        .locator(`[aria-label='Nicht-druckbare Zeichen']:not([disabled])`)
        .click()

      // verify list structure
      const orderedList = inputField.locator("ol").first()
      await expect(orderedList).toHaveAttribute(
        "style",
        "list-style-type: lower-roman;",
      )

      const listItems = inputField.locator("ol > li")
      await expect(listItems).toHaveCount(2)

      await expect(
        inputField.getByText("This is an ordered list"),
      ).toBeVisible()
      await expect(
        inputField.getByText("Second ordered list item"),
      ).toBeVisible()

      await save(page)

      await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
      await expect(page.getByText("XML Vorschau")).toBeVisible()
      await page.getByText("XML Vorschau").click()

      await expect(page.getByText(orderedListXMLPreview)).toBeVisible()
    })

    test("Enter text and make it into an ordered list with roman capital enumeration", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      const orderedListXMLPreview = `<ol style="list-style-type: upper-roman;">`

      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      await clickCategoryButton("Gründe", page)
      const inputField = page.getByTestId("Gründe")
      await inputField.click()
      await page.keyboard.type("This is an ordered list")
      await page
        .locator(`[aria-label='Nummerierte Liste']:not([disabled])`)
        .click()
      await page.getByLabel("Römisch groß (I, II, III)").click()
      await page.keyboard.press("Enter")
      await page.keyboard.type("Second ordered list item")

      // hide invisible characters
      await page
        .locator(`[aria-label='Nicht-druckbare Zeichen']:not([disabled])`)
        .click()

      // verify list structure
      const orderedList = inputField.locator("ol").first()
      await expect(orderedList).toHaveAttribute(
        "style",
        "list-style-type: upper-roman;",
      )

      const listItems = inputField.locator("ol > li")
      await expect(listItems).toHaveCount(2)

      await expect(
        inputField.getByText("This is an ordered list"),
      ).toBeVisible()
      await expect(
        inputField.getByText("Second ordered list item"),
      ).toBeVisible()

      await save(page)

      await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
      await expect(page.getByText("XML Vorschau")).toBeVisible()
      await page.getByText("XML Vorschau").click()

      await expect(page.getByText(orderedListXMLPreview)).toBeVisible()
    })

    test("Enter text and make it into an ordered list with latin small enumeration", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      const orderedListXMLPreview = `<ol style="list-style-type: lower-alpha;">`

      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      await clickCategoryButton("Gründe", page)
      const inputField = page.getByTestId("Gründe")
      await inputField.click()
      await page.keyboard.type("This is an ordered list")
      await page
        .locator(`[aria-label='Nummerierte Liste']:not([disabled])`)
        .click()
      await page.getByLabel("Lateinisch klein (a, b, c)").click()
      await page.keyboard.press("Enter")
      await page.keyboard.type("Second ordered list item")

      // hide invisible characters
      await page
        .locator(`[aria-label='Nicht-druckbare Zeichen']:not([disabled])`)
        .click()

      // verify list structure
      const orderedList = inputField.locator("ol").first()
      await expect(orderedList).toHaveAttribute(
        "style",
        "list-style-type: lower-alpha;",
      )

      const listItems = inputField.locator("ol > li")
      await expect(listItems).toHaveCount(2)

      await expect(
        inputField.getByText("This is an ordered list"),
      ).toBeVisible()
      await expect(
        inputField.getByText("Second ordered list item"),
      ).toBeVisible()

      await save(page)

      await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
      await expect(page.getByText("XML Vorschau")).toBeVisible()
      await page.getByText("XML Vorschau").click()

      await expect(page.getByText(orderedListXMLPreview)).toBeVisible()
    })

    test("Enter text and make it into an ordered list with latin capital enumeration", async ({
      page,
      prefilledDocumentUnit,
    }) => {
      const orderedListXMLPreview = `<ol style="list-style-type: upper-alpha;">`

      await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

      await clickCategoryButton("Gründe", page)
      const inputField = page.getByTestId("Gründe")
      await inputField.click()
      await page.keyboard.type("This is an ordered list")
      await page
        .locator(`[aria-label='Nummerierte Liste']:not([disabled])`)
        .click()
      await page.getByLabel("Lateinisch groß (A, B, C)").click()
      await page.keyboard.press("Enter")
      await page.keyboard.type("Second ordered list item")

      // hide invisible characters
      await page
        .locator(`[aria-label='Nicht-druckbare Zeichen']:not([disabled])`)
        .click()

      // verify list structure
      const orderedList = inputField.locator("ol").first()
      await expect(orderedList).toHaveAttribute(
        "style",
        "list-style-type: upper-alpha;",
      )

      const listItems = inputField.locator("ol > li")
      await expect(listItems).toHaveCount(2)

      await expect(
        inputField.getByText("This is an ordered list"),
      ).toBeVisible()
      await expect(
        inputField.getByText("Second ordered list item"),
      ).toBeVisible()

      await save(page)

      await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
      await expect(page.getByText("XML Vorschau")).toBeVisible()
      await page.getByText("XML Vorschau").click()

      await expect(page.getByText(orderedListXMLPreview)).toBeVisible()
    })
  },
)

test.describe("Nested lists", () => {
  test("Positioning on a sub list item and changing list style changes the list style of all items at the same level", async ({
    page,
    prefilledDocumentUnit,
  }) => {
    await navigateToCategories(page, prefilledDocumentUnit.documentNumber)

    await clickCategoryButton("Gründe", page)
    const inputField = page.getByTestId("Gründe")
    await inputField.click()

    // type the list with two levels
    await page.keyboard.type("Ordered list item one")
    await page
      .locator(`[aria-label='Nummerierte Liste']:not([disabled])`)
      .click()
    await page.getByLabel("Griechisch klein (α, β, γ)").click()
    await page.keyboard.press("Enter")
    await page.keyboard.press("Tab")
    await page.keyboard.type("Sub list item one")
    await page.keyboard.press("Enter")
    await page.keyboard.type("Sub list item two")
    await page.keyboard.press("Enter")
    await page.keyboard.press("Shift+Tab")
    await page.keyboard.type("Ordered list item two")

    // hide invisible characters
    await page
      .locator(`[aria-label='Nicht-druckbare Zeichen']:not([disabled])`)
      .click()

    // check first list iteration
    const outerList = inputField.locator("ol").first()
    await expect(outerList).toHaveAttribute(
      "style",
      "list-style-type: lower-greek;",
    )

    const nestedList = inputField.locator("ol ol").first()
    await expect(nestedList).toHaveAttribute(
      "style",
      "list-style-type: decimal;",
    )

    await expect(inputField.getByText("Ordered list item one")).toBeVisible()
    await expect(inputField.getByText("Sub list item one")).toBeVisible()
    await expect(inputField.getByText("Sub list item two")).toBeVisible()
    await expect(inputField.getByText("Ordered list item two")).toBeVisible()

    // position cursor on sub list item and change list style
    const subListSecondElement = inputField.getByText("Sub list item two")
    await subListSecondElement.click()
    await page
      .locator(`[aria-label='Nummerierte Liste']:not([disabled])`)
      .click()
    await page.getByLabel("Römisch klein (i, ii, iii)").click()

    // check second list iteration
    const nestedListAfterChange = inputField.locator("ol ol").first()
    await expect(nestedListAfterChange).toHaveAttribute(
      "style",
      "list-style-type: lower-roman;",
    )

    const outerListAfterChange = inputField.locator("ol").first()
    await expect(outerListAfterChange).toHaveAttribute(
      "style",
      "list-style-type: lower-greek;",
    )

    // just checking on validity of XML through button being visible
    // this is to recognize that juris XML convertion did not break
    // due to the conversion being sensitive to custom attributes
    await navigateToHandover(page, prefilledDocumentUnit.documentNumber)
    await expect(page.getByText("XML Vorschau")).toBeVisible()
  })
})
