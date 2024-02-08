import { expect } from "@playwright/test"
import {
  fillActiveCitationInputs,
  fillEnsuingDecisionInputs,
  fillPreviousDecisionInputs,
  navigateToCategories,
  publishDocumentationUnit,
} from "~/e2e/caselaw/e2e-utils"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"

/* eslint-disable playwright/no-conditional-in-test */
test("search for documentunits and link decision", async ({
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
  const containers = [
    activeCitationContainer,
    previousDecisionContainer,
    ensuingDecisionContainer,
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

        if (container === activeCitationContainer) {
          await fillActiveCitationInputs(page, inputs)
        }
        if (container === previousDecisionContainer) {
          await fillPreviousDecisionInputs(page, inputs)
        }
        if (container === ensuingDecisionContainer) {
          await fillEnsuingDecisionInputs(page, inputs)
        }

        await container.getByLabel("Nach Entscheidung suchen").click()

        await expect(container.getByText("1 Ergebnis gefunden")).toBeVisible()

        let summary = `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`

        const result = container.getByText(summary)
        await expect(result).toBeVisible()
        await container.getByLabel("Treffer übernehmen").click()

        //make sure to have type in list
        if (container === ensuingDecisionContainer) {
          summary = `nachgehend, ` + summary
        }

        const listItem = container.getByLabel("Listen Eintrag").last()
        await expect(listItem).toBeVisible()
        await expect(listItem).toContainText(summary, { useInnerText: true })

        // search for same parameters gives same result, indication that decision is already added
        await container.getByLabel("Weitere Angabe").click()

        if (container === activeCitationContainer) {
          await fillActiveCitationInputs(page, inputs)
        }
        if (container === previousDecisionContainer) {
          await fillPreviousDecisionInputs(page, inputs)
        }
        if (container === ensuingDecisionContainer) {
          await fillEnsuingDecisionInputs(page, inputs)
        }

        await container.getByLabel("Nach Entscheidung suchen").click()

        await expect(container.getByText("1 Ergebnis gefunden")).toBeVisible()
        await expect(container.getByText("Bereits hinzugefügt")).toBeVisible()
      },
    )
  }
})

test("search with changed parameters resets the page to 0", async ({
  page,
  prefilledDocumentUnit,
}) => {
  await navigateToCategories(page, prefilledDocumentUnit.documentNumber || "")
  await expect(
    page.getByText(prefilledDocumentUnit.documentNumber || ""),
  ).toBeVisible()

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
        await container.getByLabel("Nach Entscheidung suchen").click()
        await expect(container.getByText("Seite 1")).toBeVisible()

        await container.getByLabel("nächste Ergebnisse").click()
        await expect(container.getByText("Seite 2")).toBeVisible()

        const input = { fileNumber: "I do not exist" }

        if (container === activeCitationContainer) {
          await fillActiveCitationInputs(page, input)
        }
        if (container === previousDecisionContainer) {
          await fillPreviousDecisionInputs(page, input)
        }
        if (container === ensuingDecisionContainer) {
          await fillEnsuingDecisionInputs(page, input)
        }

        await container.getByLabel("Nach Entscheidung suchen").click()

        await expect(
          container.getByText("Keine Ergebnisse gefunden."),
        ).toBeVisible()
      },
    )
  }
})

test("search for documentunits does not return current documentation unit", async ({
  page,
  prefilledDocumentUnit,
}) => {
  await publishDocumentationUnit(
    page,
    prefilledDocumentUnit.documentNumber || "",
  )

  await navigateToCategories(page, prefilledDocumentUnit.documentNumber || "")

  const activeCitationContainer = page.getByLabel("Aktivzitierung", {
    exact: true,
  })
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
        const inputs = {
          fileNumber: prefilledDocumentUnit.coreData.fileNumbers?.[0],
          documentType: prefilledDocumentUnit.coreData.documentType?.label,
          decisionDate: "31.12.2019",
        }

        if (container === activeCitationContainer) {
          await fillActiveCitationInputs(page, inputs)
        }
        if (container === previousDecisionContainer) {
          await fillPreviousDecisionInputs(page, inputs)
        }
        if (container === ensuingDecisionContainer) {
          await fillEnsuingDecisionInputs(page, inputs)
        }

        await container.getByLabel("Nach Entscheidung suchen").click()
        await expect(
          container.getByText("Keine Ergebnisse gefunden."),
        ).toBeVisible()
      },
    )
  }
})

test("clicking on link of referenced documentation unit added by search opens new tab, does not enter edit mode", async ({
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
  const containers = [
    activeCitationContainer,
    previousDecisionContainer,
    ensuingDecisionContainer,
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

        if (container === activeCitationContainer) {
          await fillActiveCitationInputs(page, inputs)
        }
        if (container === previousDecisionContainer) {
          await fillPreviousDecisionInputs(page, inputs)
        }
        if (container === ensuingDecisionContainer) {
          await fillEnsuingDecisionInputs(page, inputs)
        }

        await container.getByLabel("Nach Entscheidung suchen").click()

        await expect(container.getByText("1 Ergebnis gefunden")).toBeVisible()

        const summary = `AG Aachen, 31.12.2019, ${prefilledDocumentUnit.coreData.fileNumbers?.[0]}, Anerkenntnisurteil`

        const result = container.getByText(summary)
        await expect(result).toBeVisible()
        await container.getByLabel("Treffer übernehmen").click()

        //check if summary has link

        const referencedDocumentNumber = container.getByText(
          `${prefilledDocumentUnit.documentNumber}`,
        )
        await expect(referencedDocumentNumber).toBeVisible()

        // clicking the link opens new tab but not the edit mode
        const newTabPromise = page.waitForEvent("popup")
        await referencedDocumentNumber.click()

        const newTab = await newTabPromise
        await newTab.waitForLoadState()

        await expect(newTab.url()).toContain(
          `${prefilledDocumentUnit.documentNumber}`,
        )

        await expect(
          container.getByLabel("Nach Entscheidung suchen"),
        ).toBeHidden()
      },
    )
  }
})
