import { expect, Page } from "@playwright/test"
import {
  navigateToAttachments,
  navigateToCategories,
  navigateToHandover,
  navigateToManagementData,
  save,
  uploadTestfile,
} from "../e2e-utils"
import { caselawTest as test } from "../fixtures"
import { Decision } from "@/domain/decision"
import DateUtil from "@/utils/dateUtil"

/* eslint-disable playwright/no-nested-step */
test.describe(
  "duplicate check (Dubletten Warnung)",
  { tag: ["@RISDEV-88"] },
  () => {
    test.describe(
      "Create duplicate warnings",
      { tag: ["@RISDEV-5883"] },
      () => {
        // eslint-disable-next-line playwright/expect-expect
        test("A duplicate warning appears when editing core data", async ({
          page,
          documentNumber,
          prefilledDocumentUnit,
        }) => {
          await navigateToCategories(page, documentNumber)
          await expectNoDuplicateWarning(page)
          await setFileNumberToMatchDocUnit(page, prefilledDocumentUnit)
          await setDecisionDateToMatchDocUnit(page, prefilledDocumentUnit)
          await setDocTypeToMatchDocUnit(page, prefilledDocumentUnit)
          await save(page)
          await expectDuplicateWarning(page)
          // Other doc unit displays same warning
          await navigateToCategories(page, prefilledDocumentUnit.documentNumber)
          await expectDuplicateWarning(page)
        })

        // eslint-disable-next-line playwright/expect-expect
        test("A duplicate warning appears when uploading a file", async ({
          page,
          documentNumber,
          prefilledDocumentUnit,
        }) => {
          await navigateToCategories(page, prefilledDocumentUnit.documentNumber)
          // Ecli is same as from docx file we will upload afterward
          await page
            .getByLabel("ECLI", { exact: true })
            .fill("ECLI:DE:BGH:2023:210423UVZR86.22.0")

          await navigateToAttachments(page, documentNumber)
          await uploadTestfile(page, "with_metadata.docx")

          await expectDuplicateWarning(page)
        })
      },
    )

    test("When duplicate is ignored, no warning is shown", async ({
      page,
      documentNumber,
      prefilledDocumentUnit,
    }) => {
      await test.step("Create duplicate by editing core data", async () => {
        await navigateToCategories(page, documentNumber)
        await setFileNumberToMatchDocUnit(page, prefilledDocumentUnit)
        await setDecisionDateToMatchDocUnit(page, prefilledDocumentUnit)
        await setDocTypeToMatchDocUnit(page, prefilledDocumentUnit)
        await save(page)
        await expectDuplicateWarning(page)
      })

      await test.step("Check management data and ignore warning", async () => {
        await navigateToManagementData(page, documentNumber)
        await expectDocUnitSummaryInDuplicatesList(page, prefilledDocumentUnit)
        await ignoreDuplicateWarning(page, prefilledDocumentUnit)
        await expectNoDuplicateWarning(page)
      })

      await test.step("Check and reset warning on other doc unit", async () => {
        const prefilledDocNumber = prefilledDocumentUnit.documentNumber
        await navigateToManagementData(page, prefilledDocNumber)
        await test.step("Duplicate warning should be ignored on other doc unit as well", async () => {
          await expect(page.getByLabel("Warnung ignorieren")).toBeChecked()
        })
        await expectNoDuplicateWarning(page)
        await test.step("Reset duplicate warning", async () => {
          await page.getByLabel("Warnung ignorieren").uncheck()
        })
        await expectDuplicateWarning(page)
      })
    })

    test("Handover only with warning dialog until all warnings are ignored", async ({
      page,
      documentNumber,
      prefilledDocumentUnit,
      prefilledDocumentUnitWithTexts,
    }) => {
      await test.step("Create two duplicates by editing core data", async () => {
        await navigateToCategories(page, documentNumber)
        await setFileNumberToMatchDocUnit(page, prefilledDocumentUnit)
        await setDocTypeToMatchDocUnit(page, prefilledDocumentUnit)
        await setDecisionDateToMatchDocUnit(page, prefilledDocumentUnit)
        await setDeviatingFileNumberToMatchDocUnit(
          page,
          prefilledDocumentUnitWithTexts,
        )
        await setDeviatingDecisionDateToMatchDocUnit(
          page,
          prefilledDocumentUnitWithTexts,
        )
        await setCourtToBGH(page)
        await page
          .getByLabel("Entscheidungsdatum", { exact: true })
          .fill("01.01.1990")
        await save(page)
        await expectDuplicateWarning(page)
      })

      await navigateToHandover(page, documentNumber)

      await test.step("Handover should trigger warning dialog", async () => {
        await expect(
          page.getByText("Es besteht Dublettenverdacht."),
        ).toBeVisible()

        await expectDocUnitSummaryInDuplicatesList(page, prefilledDocumentUnit)

        await expectDocUnitSummaryInDuplicatesList(
          page,
          prefilledDocumentUnitWithTexts,
        )

        // XML needs to be loaded for the button to be enabled.
        await expect(page.getByText("XML Vorschau")).toBeVisible()

        await page.getByLabel("Dokumentationseinheit an jDV übergeben").click()

        await expect(
          page.getByText("Prüfung hat Warnungen ergeben"),
        ).toBeVisible()
        await expect(
          page.getByText(
            "Es besteht Dublettenverdacht.\nWollen Sie das Dokument dennoch übergeben?",
          ),
        ).toBeVisible()

        await page.getByLabel("Abbrechen").click()

        await expect(page.getByText("Email wurde versendet")).toBeHidden()

        await page.getByLabel("Dokumentationseinheit an jDV übergeben").click()

        await page.getByLabel("Trotzdem übergeben").click()

        await expect(page.getByText("Email wurde versendet")).toBeVisible()
        await expect(page.getByText("Xml Email Abgabe -")).toBeVisible()
      })

      await test.step("Navigate to Verwaltungsdaten via handover button", async () => {
        await page.getByLabel("Dublettenwarnung prüfen").click()
      })

      await expectDocUnitSummaryInDuplicatesList(page, prefilledDocumentUnit)
      await expectDocUnitSummaryInDuplicatesList(
        page,
        prefilledDocumentUnitWithTexts,
      )
      await ignoreDuplicateWarning(page, prefilledDocumentUnit)
      await expectDuplicateWarning(page)

      await ignoreDuplicateWarning(page, prefilledDocumentUnitWithTexts)
      await expectNoDuplicateWarning(page)

      await navigateToHandover(page, documentNumber)

      await test.step("Handover should be possible without warnings", async () => {
        await expect(
          page.getByText("Es besteht kein Dublettenverdacht."),
        ).toBeVisible()
        await expect(
          page.getByLabel("Dokumentationseinheit an jDV übergeben"),
        ).toBeEnabled()
      })
    })

    test("Externals cannot see management data, but handover triggers warning dialog", async ({
      page,
      documentNumber,
      pageWithExternalUser,
      prefilledDocumentUnit,
    }) => {
      await test.step("Create two duplicates by editing core data", async () => {
        await navigateToCategories(page, documentNumber)
        await setFileNumberToMatchDocUnit(page, prefilledDocumentUnit)
        await setDecisionDateToMatchDocUnit(page, prefilledDocumentUnit)
        await setDocTypeToMatchDocUnit(page, prefilledDocumentUnit)
        await setCourtToBGH(page)
        await save(page)
        await expectDuplicateWarning(page)
      })

      await navigateToHandover(pageWithExternalUser, documentNumber)

      await test.step("Handover should trigger dialog with warnings", async () => {
        await expect(
          pageWithExternalUser.getByText("Es besteht Dublettenverdacht."),
        ).toBeVisible()

        await expectDocUnitSummaryInDuplicatesList(
          pageWithExternalUser,
          prefilledDocumentUnit,
        )

        // XML needs to be loaded for the button to be enabled.
        await expect(
          pageWithExternalUser.getByText("XML Vorschau"),
        ).toBeVisible()

        await pageWithExternalUser
          .getByLabel("Dokumentationseinheit an jDV übergeben")
          .click()

        await expect(
          pageWithExternalUser.getByText("Prüfung hat Warnungen ergeben"),
        ).toBeVisible()
        await expect(
          pageWithExternalUser.getByText(
            "Es besteht Dublettenverdacht.\nWollen Sie das Dokument dennoch übergeben?",
          ),
        ).toBeVisible()

        await pageWithExternalUser.getByLabel("Trotzdem übergeben").click()

        await expect(
          pageWithExternalUser.getByText("Email wurde versendet"),
        ).toBeVisible()
        await expect(
          pageWithExternalUser.getByText("Xml Email Abgabe -"),
        ).toBeVisible()
      })

      await test.step("Verwaltungsdaten are not visible for Externals", async () => {
        await expect(
          pageWithExternalUser.getByLabel("Dublettenwarnung prüfen"),
        ).toBeHidden()
        await expect(
          pageWithExternalUser.getByRole("link", { name: "Bitte prüfen" }),
        ).toBeHidden()
        await expect(
          pageWithExternalUser.getByTestId(
            "caselaw-documentUnit-documentNumber-managementdata",
          ),
        ).toBeHidden()
      })

      await test.step("External user can see warning in info panel", async () => {
        await expect(
          pageWithExternalUser.getByText("Dublettenverdacht", { exact: true }),
        ).toBeVisible()
      })

      await test.step("Ignore warnings as internal user", async () => {
        await navigateToManagementData(page, documentNumber)
        await ignoreDuplicateWarning(page, prefilledDocumentUnit)
      })

      await test.step("Without warnings handover is possible as external user", async () => {
        await pageWithExternalUser.reload()
        await expect(
          pageWithExternalUser.getByText("Es besteht kein Dublettenverdacht."),
        ).toBeVisible()
        await expect(
          pageWithExternalUser.getByLabel(
            "Dokumentationseinheit an jDV übergeben",
          ),
        ).toBeEnabled()
      })
    })

    test.describe(
      "Search for documents with duplicate warning",
      { tag: ["@RISDEV-5912"] },
      () => {
        test("user can filter search result for duplicate warning", async ({
          page,
          documentNumber,
          prefilledDocumentUnit,
          secondPrefilledDocumentUnit,
        }) => {
          await test.step("Create two duplicates and one non duplicate", async () => {
            await navigateToCategories(page, documentNumber)
            await setDeviatingFileNumberToMatchDocUnit(
              page,
              prefilledDocumentUnit,
            )
            await save(page)
            await navigateToCategories(
              page,
              secondPrefilledDocumentUnit.documentNumber,
            )
            await setDeviatingFileNumberToMatchDocUnit(
              page,
              prefilledDocumentUnit,
            )
            await save(page)
            await expectDuplicateWarning(page)
          })

          await test.step("Search for 3 doc units with same 'Aktenzeichen'", async () => {
            await page.goto("/")

            const fileNumber = prefilledDocumentUnit.coreData.fileNumbers![0]
            await page.getByLabel("Aktenzeichen Suche").fill(fileNumber)
            await page.getByLabel("Nach Dokumentationseinheiten suchen").click()
            //3 + table header
            await expect
              .poll(async () => page.locator(".table-row").count())
              .toBe(4)
          })

          await test.step("Apply duplicate filter to search for 2 duplicates", async () => {
            const docOfficeOnly = page.getByLabel("Nur meine Dokstelle Filter")
            await docOfficeOnly.click()
            const withDuplicateWarning = page.getByLabel("Dublettenverdacht")
            await withDuplicateWarning.click()
            await page.getByLabel("Nach Dokumentationseinheiten suchen").click()

            //2 + table header
            await expect
              .poll(async () => page.locator(".table-row").count())
              .toBe(3)

            await docOfficeOnly.click()
            await expect(withDuplicateWarning).toBeHidden()
          })
        })
      },
    )
  },
)

async function setFileNumberToMatchDocUnit(page: Page, docUnit: Decision) {
  await test.step("Set fileNumber to match existing doc", async () => {
    await page
      .getByLabel("Aktenzeichen", { exact: true })
      .fill(docUnit.coreData.fileNumbers?.[0] ?? "")
  })
}

async function setDeviatingFileNumberToMatchDocUnit(
  page: Page,
  docUnit: Decision,
) {
  await test.step("Set deviating fileNumber to match existing doc", async () => {
    await page.getByLabel("Abweichendes Aktenzeichen anzeigen").click()

    await page
      // .getByTestId("chips-input_deviatingFileNumber")
      .getByLabel("Abweichendes Aktenzeichen", { exact: true })
      .pressSequentially(docUnit.coreData.fileNumbers?.[0] ?? "")
    await page.keyboard.press("Enter")
  })
}

async function setDecisionDateToMatchDocUnit(page: Page, docUnit: Decision) {
  await test.step("Set decisionDate to match existing doc", async () => {
    const date = DateUtil.formatDate(docUnit.coreData.decisionDate)!
    await page.getByLabel("Entscheidungsdatum", { exact: true }).fill(date)
  })
}

async function setDeviatingDecisionDateToMatchDocUnit(
  page: Page,
  docUnit: Decision,
) {
  await test.step("Set deviating decisionDate to match existing doc", async () => {
    await page.getByLabel("Abweichendes Entscheidungsdatum anzeigen").click()
    const date = DateUtil.formatDate(docUnit.coreData.decisionDate)!
    await page
      .getByLabel("Abweichendes Entscheidungsdatum", { exact: true })
      .pressSequentially(date)
    await page.keyboard.press("Enter")
  })
}

async function setDocTypeToMatchDocUnit(page: Page, docUnit: Decision) {
  await test.step("Set documentType to match existing doc", async () => {
    await page
      .getByLabel("Dokumenttyp", { exact: true })
      .fill(docUnit.coreData.documentType?.label ?? "")

    await expect(
      page.getByLabel("dropdown-option", { exact: true }),
    ).toHaveCount(1)

    await page.getByLabel("dropdown-option", { exact: true }).click()
  })
}

async function setCourtToBGH(page: Page) {
  await test.step("Set court to BGH", async () => {
    await page.getByLabel("Gericht", { exact: true }).fill("BGH")

    await expect(
      page.getByLabel("dropdown-option", { exact: true }),
    ).toHaveCount(1)

    await page.getByLabel("dropdown-option", { exact: true }).click()
  })
}

async function ignoreDuplicateWarning(page: Page, docUnit: Decision) {
  await test.step("Ignore duplicate warning", async () => {
    const setStatusRequest = page.waitForRequest(
      "**/api/v1/caselaw/documentunits/*/duplicate-status/*",
      {
        timeout: 5_000,
      },
    )
    await page.locator(`#is-ignored-${docUnit.documentNumber}`).check()
    await setStatusRequest
  })
}

async function expectDocUnitSummaryInDuplicatesList(
  page: Page,
  docUnit: Decision,
) {
  await test.step("Ensure duplicate doc unit appears in duplicate warnings", async () => {
    const duplicateSummary = page.getByTestId(
      `decision-summary-${docUnit.documentNumber}`,
    )
    await expect(duplicateSummary).toBeVisible()
    const date = DateUtil.formatDate(docUnit.coreData.decisionDate)
    const court = docUnit.coreData.court?.label
    const fileNumber = docUnit.coreData.fileNumbers?.[0]
    const docType = docUnit.coreData.documentType?.label
    await expect(duplicateSummary).toHaveText(
      `${court}, ${date}, ${fileNumber}, ${docType},   Unveröffentlicht  |  ${docUnit.documentNumber}`,
    )
  })
}

async function expectDuplicateWarning(page: Page) {
  await test.step("Ensure duplicate warning is displayed", async () => {
    await expect(page.getByTestId("duplicate-icon")).toBeVisible()
    await expect(page.getByRole("link", { name: "Bitte prüfen" })).toBeVisible()
  })
}

async function expectNoDuplicateWarning(page: Page) {
  await test.step("Ensure no duplicate warning is displayed", async () => {
    await expect(page.getByTestId("duplicate-icon")).toBeHidden()
    await expect(page.getByRole("link", { name: "Bitte prüfen" })).toBeHidden()
  })
}
