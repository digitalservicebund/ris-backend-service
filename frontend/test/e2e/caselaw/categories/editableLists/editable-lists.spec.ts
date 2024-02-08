import { expect } from "@playwright/test"
import {
  fillPreviousDecisionInputs,
  fillActiveCitationInputs,
  fillEnsuingDecisionInputs,
  navigateToCategories,
  publishDocumentationUnit,
  fillNormInputs,
  waitForSaving,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { generateString } from "~/test-helper/dataGenerators"

/* eslint-disable playwright/no-conditional-in-test */
test.describe("related documentation units", () => {
  test("renders empty list item in creation mode, when none in list", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    const normsContainer = page.getByLabel("Norm")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
      normsContainer,
    ]

    for (const container of containers) {
      await test.step(
        "for category " + (await container.first().getAttribute("aria-label")),
        async () => {
          const containerLabel = (await container
            .first()
            .getAttribute("aria-label")) as string

          //adding, deleting and cancel editing of empty item not possible
          await expect(
            container.getByLabel(`${containerLabel} speichern`),
          ).toBeDisabled()

          await expect(container.getByLabel("Abbrechen")).toBeHidden()

          await expect(container.getByLabel("Löschen")).toBeHidden()
        },
      )
    }
  })

  test("added items are saved, can be edited and deleted", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    // todo: find generic way to fill forms without too many conditionals
    // const normsContainer = page.getByLabel("Norm")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
      // normsContainer,
    ]

    for (const container of containers) {
      const section = await container.first().getAttribute("aria-label")
      const fileNumber1 = generateString()
      const fileNumber2 = generateString()
      const fileNumber3 = generateString()

      await test.step("for category " + section, async () => {
        // adding empty entry not possible
        await expect(page.getByLabel(section + " speichern")).toBeDisabled()

        // add entry
        await container
          .getByLabel("Aktenzeichen " + section, { exact: true })
          .fill(fileNumber1)

        await container.getByLabel(section + " speichern").click()
        await expect(container.getByText(fileNumber1)).toBeVisible()

        // edit entry
        await container.getByLabel("Listen Eintrag").click()
        await container
          .getByLabel("Aktenzeichen " + section, { exact: true })
          .fill(fileNumber2)

        await container.getByLabel(section + " speichern").click()
        await expect(container.getByText(fileNumber1)).toBeHidden()
        await expect(container.getByText(fileNumber2)).toBeVisible()

        await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)

        // add second entry
        await container.getByLabel("Weitere Angabe").click()
        await waitForSaving(
          async () => {
            await container
              .getByLabel("Aktenzeichen " + section, { exact: true })
              .fill(fileNumber3)
            await container.getByLabel(section + " speichern").click()
          },
          page,
          { clickSaveButton: true },
        )

        await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)
        await page.reload()
        const listEntries = container.getByLabel("Listen Eintrag")
        await expect(listEntries).toHaveCount(2)
        await listEntries.first().click()
        await container.getByLabel("Eintrag löschen").click()
        await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)
      })
    }
  })

  test("validates list item against required fields", async ({
    page,
    documentNumber,
    prefilledDocumentUnit,
  }) => {
    await publishDocumentationUnit(
      page,
      prefilledDocumentUnit.documentNumber || "",
    )
    await navigateToCategories(page, documentNumber)

    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    const normsContainer = page.getByLabel("Norm")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
      normsContainer,
    ]

    for (const container of containers) {
      await test.step(
        "for category " + (await container.first().getAttribute("aria-label")),
        async () => {
          const inputs = {
            court: prefilledDocumentUnit.coreData.court?.label,
            fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
            documentType: prefilledDocumentUnit.coreData.documentType?.label,
            decisionDate: "31.12.2019",
          }
          const containerLabel = (await container
            .first()
            .getAttribute("aria-label")) as string

          const fileNumber = generateString()
          if (container === normsContainer) {
            await fillNormInputs(page, { dateOfRelevance: "1234" })
          } else {
            await container
              .getByLabel("Aktenzeichen " + containerLabel, { exact: true })
              .fill(fileNumber)
          }

          await page.getByLabel(`${containerLabel} speichern`).click()

          await expect(page.getByLabel("Fehlerhafte Eingabe")).toBeVisible()
          await page
            .getByLabel(containerLabel, { exact: true })
            .getByLabel("Listen Eintrag")
            .click()
          if (container === activeCitationContainer) {
            await expect(
              page
                .getByLabel(containerLabel)
                .getByText("Pflichtfeld nicht befüllt"),
            ).toHaveCount(3)
            await fillActiveCitationInputs(page, {
              ...inputs,
              citationType: "Änderung",
            })
          } else if (container === normsContainer) {
            await expect(
              page
                .getByLabel(containerLabel)
                .getByText("Pflichtfeld nicht befüllt"),
            ).toHaveCount(1)
            await fillNormInputs(page, { normAbbreviation: "BayWaldNatPV BY" })
          } else if (container === previousDecisionContainer) {
            await expect(
              page
                .getByLabel(containerLabel)
                .getByText("Pflichtfeld nicht befüllt"),
            ).toHaveCount(2)
            await fillPreviousDecisionInputs(page, inputs)
          } else if (container === ensuingDecisionContainer) {
            await expect(
              page
                .getByLabel(containerLabel)
                .getByText("Pflichtfeld nicht befüllt"),
            ).toHaveCount(2)
            await fillEnsuingDecisionInputs(page, inputs)
          }

          await page.getByLabel(`${containerLabel} speichern`).click()

          await expect(page.getByLabel("Fehlerhafte Eingabe")).toBeHidden()
        },
      )
    }
  })

  test("after first save of list item, cancel and delete buttons are visible", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)

    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    const normsContainer = page.getByLabel("Norm")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
      normsContainer,
    ]

    for (const container of containers) {
      await test.step(
        "for category " + (await container.first().getAttribute("aria-label")),
        async () => {
          const fileNumber = generateString()
          const containerLabel = (await container
            .first()
            .getAttribute("aria-label")) as string

          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, { fileNumber: fileNumber })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, { fileNumber: fileNumber })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, { fileNumber: fileNumber })
          }
          if (container === normsContainer) {
            await fillNormInputs(page, { dateOfRelevance: "2022" })
          }

          await expect(container.getByLabel("Abbrechen")).toBeHidden()

          await expect(container.getByLabel("Löschen")).toBeHidden()

          await container.getByLabel(`${containerLabel} speichern`).click()

          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)

          await container.getByLabel("Listen Eintrag").click()
          await expect(container.getByLabel("Abbrechen")).toBeVisible()

          await expect(container.getByLabel("Löschen")).toBeVisible()
        },
      )
    }
  })

  test("deleting behaviour of list items", async ({ page, documentNumber }) => {
    await navigateToCategories(page, documentNumber)
    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    // todo: find generic way to fill forms without too many conditionals
    // const normsContainer = page.getByLabel("Norm")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
      // normsContainer
    ]

    for (const container of containers) {
      await test.step(
        "for category " + (await container.first().getAttribute("aria-label")),
        async () => {
          const containerLabel = (await container
            .first()
            .getAttribute("aria-label")) as string

          await navigateToCategories(page, documentNumber)

          //list item 1
          const fileNumber1 = generateString()
          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, {
              fileNumber: fileNumber1,
            })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, {
              fileNumber: fileNumber1,
            })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, {
              fileNumber: fileNumber1,
            })
          }
          await container.getByLabel(`${containerLabel} speichern`).click()
          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)
          await expect(
            container.getByLabel("Listen Eintrag").last(),
          ).toContainText(fileNumber1)

          //list item 2
          await container.getByLabel("Weitere Angabe").click()
          const fileNumber2 = generateString()
          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, {
              fileNumber: fileNumber2,
            })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, {
              fileNumber: fileNumber2,
            })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, {
              fileNumber: fileNumber2,
            })
          }
          await container.getByLabel(`${containerLabel} speichern`).click()
          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)
          await expect(
            container.getByLabel("Listen Eintrag").last(),
          ).toContainText(fileNumber2)

          //list item 3
          await container.getByLabel("Weitere Angabe").click()
          const fileNumber3 = generateString()
          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, {
              fileNumber: fileNumber3,
            })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, {
              fileNumber: fileNumber3,
            })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, {
              fileNumber: fileNumber3,
            })
          }
          await container.getByLabel(`${containerLabel} speichern`).click()
          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(3)
          await expect(
            container.getByLabel("Listen Eintrag").last(),
          ).toContainText(fileNumber3)

          // leaving an empty list item, deletes it
          await container.getByLabel("Weitere Angabe").click()
          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(4)
          await container.getByLabel("Listen Eintrag").nth(1).click()
          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(3)

          // deleting last item, sets previous list item in edit mode
          await container.getByLabel("Listen Eintrag").last().click()
          await container.getByLabel("Löschen").click()
          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(2)
          await expect(
            container.getByLabel(`Aktenzeichen ${containerLabel}`, {
              exact: true,
            }),
          ).toHaveValue(fileNumber2)

          // deleting first item, sets next list item in edit mode
          await container.getByLabel("Listen Eintrag").first().click()
          await container.getByLabel("Löschen").click()
          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)
          await expect(
            container.getByLabel(`Aktenzeichen ${containerLabel}`, {
              exact: true,
            }),
          ).toHaveValue(fileNumber2)

          expect(
            await container.getByText("Pflichtfeld nicht befüllt").count(),
          ).toBeGreaterThanOrEqual(1)

          //deleting last list item, adds a new default item
          await container.getByLabel("Löschen").click()

          await expect(container.getByLabel("Abbrechen")).toBeHidden()
          await expect(container.getByLabel("Löschen")).toBeHidden()
          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)
          await expect(
            container.getByLabel(`Aktenzeichen ${containerLabel}`, {
              exact: true,
            }),
          ).toHaveValue("")

          // resets validation errors
          await expect(
            container.getByText("Pflichtfeld nicht befüllt"),
          ).toHaveCount(0)
        },
      )
    }
  })

  test("cancel editing list item does not update values", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    const normsContainer = page.getByLabel("Norm")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
      normsContainer,
    ]

    for (const container of containers) {
      await test.step(
        "for category " + (await container.first().getAttribute("aria-label")),
        async () => {
          const containerLabel = (await container
            .first()
            .getAttribute("aria-label")) as string

          const number = "1234"
          const editedNumber = "4321"
          await navigateToCategories(page, documentNumber)

          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, { fileNumber: number })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, { fileNumber: number })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, { fileNumber: number })
          }

          if (container === normsContainer) {
            await fillNormInputs(page, { dateOfRelevance: number })
          }

          await expect(container.getByLabel("Abbrechen")).toBeHidden()

          await container.getByLabel(`${containerLabel} speichern`).click()

          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)

          await container.getByLabel("Listen Eintrag").click()

          await expect(container.getByLabel("Abbrechen")).toBeVisible()

          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, {
              fileNumber: editedNumber,
            })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, {
              fileNumber: editedNumber,
            })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, {
              fileNumber: editedNumber,
            })
          }

          if (container === normsContainer) {
            await fillNormInputs(page, { dateOfRelevance: editedNumber })
          }

          await container.getByLabel("Abbrechen").click()
          await expect(container.getByText(number)).toBeVisible()
          await expect(container.getByText(editedNumber)).toBeHidden()
        },
      )
    }
  })

  test("add new item only possible when no item in edit mode", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    const normsContainer = page.getByLabel("Norm")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
      normsContainer,
    ]

    for (const container of containers) {
      await test.step(
        "for category " + (await container.first().getAttribute("aria-label")),
        async () => {
          const containerLabel = (await container
            .first()
            .getAttribute("aria-label")) as string

          const fileNumber = generateString()
          await navigateToCategories(page, documentNumber)

          await expect(container.getByLabel("Weitere Angabe")).toBeHidden()

          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, { fileNumber: fileNumber })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, { fileNumber: fileNumber })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, { fileNumber: fileNumber })
          }
          if (container === normsContainer) {
            await fillNormInputs(page, { dateOfRelevance: "1234" })
          }

          await container.getByLabel(`${containerLabel} speichern`).click()

          await expect(container.getByLabel("Weitere Angabe")).toBeVisible()

          await expect(container.getByLabel("Listen Eintrag")).toHaveCount(1)

          await container.getByLabel("Listen Eintrag").click()

          await expect(container.getByLabel("Weitere Angabe")).toBeHidden()
        },
      )
    }
  })

  test("incomplete date input shows error message and does not persist", async ({
    page,
    documentNumber,
  }) => {
    await navigateToCategories(page, documentNumber)
    const activeCitationContainer = page.getByLabel("Aktivzitierung")
    const previousDecisionContainer = page.getByLabel("Vorgehende Entscheidung")
    const ensuingDecisionContainer = page.getByLabel("Nachgehende Entscheidung")
    const containers = [
      activeCitationContainer,
      previousDecisionContainer,
      ensuingDecisionContainer,
    ]

    for (const container of containers) {
      await test.step(
        "for category " + (await container.first().getAttribute("aria-label")),
        async () => {
          const containerLabel = (await container
            .first()
            .getAttribute("aria-label")) as string

          await navigateToCategories(page, documentNumber)

          if (container === activeCitationContainer) {
            await fillActiveCitationInputs(page, { decisionDate: "03" })
          }
          if (container === previousDecisionContainer) {
            await fillPreviousDecisionInputs(page, { decisionDate: "03" })
          }
          if (container === ensuingDecisionContainer) {
            await fillEnsuingDecisionInputs(page, { decisionDate: "03" })
          }

          await page.keyboard.press("Tab")

          await expect(
            container.getByLabel(`Entscheidungsdatum ${containerLabel}`),
          ).toHaveValue("03")

          await expect(
            container.locator("text=Unvollständiges Datum"),
          ).toBeVisible()

          await page.reload()

          await expect(
            container.getByLabel(`Entscheidungsdatum ${containerLabel}`),
          ).toHaveValue("")
        },
      )
    }
  })
})
