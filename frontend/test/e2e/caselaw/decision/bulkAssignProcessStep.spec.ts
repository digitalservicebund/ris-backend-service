import { expect } from "@playwright/test"
import dayjs from "dayjs"
import {
  createPendingHandoverDecisionForBGH,
  navigateToSearch,
  openSearchWithFileNumberPrefix,
  selectUser,
  triggerSearch,
} from "../utils/e2e-utils"
import errorMessages from "@/i18n/errors.json" with { type: "json" }
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { generateString } from "~/test-helper/dataGenerators"

test.describe(
  "Multi-Edit in der großen Suche",
  {
    tag: "@RISDEV-8883",
  },
  () => {
    test.use({
      decisionsToBeCreated: [
        // Reverse sorting: date DESC, docNumber DESC
        [
          { coreData: { decisionDate: "2021-01-01", court: { label: "BFH" } } },
          {
            coreData: {
              decisionDate: "2022-01-01",
              court: { label: "AG Aachen" },
            },
          },
          { coreData: { decisionDate: "2023-01-01" } },
          { coreData: { decisionDate: "2023-01-02" } },
          { coreData: { decisionDate: "2023-01-31" } },
        ],
        { scope: "test" },
      ],
    })

    test("Dokumentationseinheiten können in der Suche einem Prozessschritt zugewiesen werden", async ({
      page,
      decisions,
    }) => {
      const { fileNumberPrefix, createdDecisions } = decisions
      // all new doc units are created with step Ersterfassung
      await test.step(`Wähle Schritt 'Ersterfassung'`, async () => {
        await openSearchWithFileNumberPrefix(fileNumberPrefix, page)
        await page.getByLabel("Nur meine Dokstelle Filter").click()
        await triggerSearch(page)
      })

      await test.step(`Prüfe, dass alle Ergebnisse gefunden wurden`, async () => {
        await expect(
          page.getByText(createdDecisions.length + " Ergebnisse gefunden"),
        ).toBeVisible()
        await expect(
          page.getByText(createdDecisions[0].documentNumber),
        ).toBeVisible()
      })

      await test.step(`Selektiere alle Dokumentationseinheiten über Tabellenheader`, async () => {
        const selectAllCheckbox = page.getByRole("checkbox", {
          name: "Alle Elemente abgewählt",
        })
        await expect(selectAllCheckbox).toBeVisible()
        await selectAllCheckbox.click()
      })

      await test.step("Prüfe, dass alle Dokeinheiten selektiert sind", async () => {
        for (const decision of createdDecisions) {
          const documentNumber = decision.documentNumber
          const rowLocator = page.locator("tr", { hasText: documentNumber })
          // Find the checkbox within the specific row and check if it's checked
          const rowCheckbox = rowLocator.getByRole("checkbox")
          await expect(rowCheckbox).toBeChecked()
        }
      })

      await test.step("'Aktionen' Button öffnet Prozesschritt Modal bei valider Auswahl", async () => {
        await page.getByRole("button", { name: "Aktionen" }).click()
        const weitergebenMenuItem = page.getByRole("menuitem", {
          name: "Weitergeben",
        })
        await weitergebenMenuItem.click()

        const newProcessStep = page.getByLabel("Neuer Schritt")

        await expect(newProcessStep).toBeVisible()
        // no prefilled step
        await expect(newProcessStep).toBeEmpty()
        // no history of process steps like when assigning process step for single docunit
        await expect(page.getByRole("dialog").getByRole("table")).toBeHidden()
      })

      await test.step("Dokeinheiten können einem neuen Prozesschritt zugewiesen werden", async () => {
        const newProcessStep = page.getByLabel("Neuer Schritt")
        await newProcessStep.click()
        await page.getByRole("option", { name: "Fachdokumentation" }).click()
        await expect(newProcessStep).toHaveText("Fachdokumentation")
        await page.getByRole("button", { name: "Weitergeben" }).click()
        await expect(page.getByRole("alert")).toBeVisible()
        await expect(page.getByText("Weitergeben erfolgreich")).toBeVisible()
        await expect(
          page.getByText(
            "Die Dokumentationseinheiten sind jetzt im Schritt Fachdokumentation",
          ),
        ).toBeVisible()
      })

      await test.step(`Neue Suche wurde getriggert und zeigt Suchergebnisse mit neuem Schritt an`, async () => {
        await expect(
          page.getByText(createdDecisions.length + " Ergebnisse gefunden"),
        ).toBeVisible()
        await expect(
          page.getByText(createdDecisions[0].documentNumber),
        ).toBeVisible()
        for (const decision of createdDecisions) {
          const documentNumber = decision.documentNumber
          const rowLocator = page.locator("tr", { hasText: documentNumber })
          await expect(rowLocator).toContainText("Fachdokumentation")
        }
      })

      await test.step(`Selektiere alle Dokumentationseinheiten über Tabellenheader`, async () => {
        const selectAllCheckbox = page.getByRole("checkbox", {
          name: "Alle Elemente abgewählt",
        })
        await expect(selectAllCheckbox).toBeVisible()
        await selectAllCheckbox.click()
      })

      await test.step("Dokeinheiten können einer neuen Person zugewiesen werden", async () => {
        await page.getByRole("button", { name: "Aktionen" }).click()
        const weitergebenMenuItem = page.getByRole("menuitem", {
          name: "Weitergeben",
        })
        await weitergebenMenuItem.click()
        const newPerson = page.getByLabel("Neue Person")
        await expect(newPerson).toBeVisible()
        await selectUser(
          page,
          "e2e_tests DigitalService",
          "e2e_tests DigitalService",
        )
      })

      await test.step("Weitergeben ohne Prozessschritt zeigt Fehler an", async () => {
        await page.getByRole("button", { name: "Weitergeben" }).click()
        await expect(page.getByText("Wählen Sie einen Schritt")).toBeVisible()
      })

      await test.step("Wähle gleichen Prozesschritt aus", async () => {
        const newProcessStep = page.getByLabel("Neuer Schritt")
        await newProcessStep.click()
        await page.getByRole("option", { name: "Fachdokumentation" }).click()
        await expect(newProcessStep).toHaveText("Fachdokumentation")
      })

      await test.step("Success Meldung zeigt aktuellen Prozesschritt und User an", async () => {
        await page.getByRole("button", { name: "Weitergeben" }).click()
        await expect(page.getByRole("alert")).toBeVisible()
        await expect(page.getByText("Weitergeben erfolgreich")).toBeVisible()
        await expect(
          page.getByText(
            "Die Dokumentationseinheiten sind jetzt im Schritt Fachdokumentation und der Person e2e_tests DigitalService zugewiesen.",
          ),
        ).toBeVisible()
      })
    })

    // Error Handling

    // User Input Errors

    test("Zeigt Fehlermeldung, wenn keine Dokumentationseinheiten zum Weitergeben selektiert worden sind", async ({
      page,
      decisions,
    }) => {
      const { fileNumberPrefix, createdDecisions } = decisions
      // all new doc units are created with step Ersterfassung
      await test.step(`Wähle Schritt 'Ersterfassung'`, async () => {
        await openSearchWithFileNumberPrefix(fileNumberPrefix, page)
        await page.getByLabel("Nur meine Dokstelle Filter").click()
        await triggerSearch(page)
      })

      await test.step(`Prüfe, dass alle Ergebnisse gefunden wurden`, async () => {
        await expect(
          page.getByText(createdDecisions.length + " Ergebnisse gefunden"),
        ).toBeVisible()
        await expect(
          page.getByText(createdDecisions[0].documentNumber),
        ).toBeVisible()
      })

      await test.step("Prozessschritt zuweisen ohne selektierte Dokumentationseinheiten zeigt Fehler", async () => {
        await page.getByLabel("Aktionen").click()

        const weitergebenMenuItem = page.getByRole("menuitem", {
          name: "Weitergeben",
        })

        await expect(weitergebenMenuItem).toBeVisible()

        await weitergebenMenuItem.click()

        await expect(
          page.getByText(
            "Wählen Sie mindestens eine Dokumentationsseinheit aus.",
          ),
        ).toBeVisible()

        await expect(weitergebenMenuItem).toBeHidden()
      })
    })

    test("Zeigt Fehlermeldung, dass Dokumentationseinheiten keinem Prozessschritt zugewiesen werden können, solange sie noch Status Fremdanlage haben", async ({
      page,
      pageWithBghUser,
      edition,
    }) => {
      const fileNumber = generateString()
      let documentNumber: string

      await test.step("Fremdanlage für BGH anlegen", async () => {
        documentNumber = await createPendingHandoverDecisionForBGH(
          page,
          edition,
          "12",
          "AG Aachen",
          dayjs("2025-01-01").format("DD.MM.YYYY"),
          fileNumber,
          "AnU",
        )
      })

      await test.step("Nach Fremdanlage suchen", async () => {
        await navigateToSearch(pageWithBghUser)
        await pageWithBghUser
          .getByLabel("Dokumentnummer Suche")
          .fill(documentNumber)
        await pageWithBghUser
          .getByLabel("Nach Dokumentationseinheiten suchen")
          .click()
        await expect(
          pageWithBghUser.getByRole("row").getByText(documentNumber),
        ).toBeVisible()
      })

      await test.step("Fremdanlage können selektiert werden", async () => {
        const rowLocator = pageWithBghUser.locator("tr", {
          hasText: documentNumber,
        })
        const rowCheckbox = rowLocator.getByRole("checkbox")
        await rowCheckbox.click()
        await expect(rowCheckbox).toBeChecked()
      })

      await test.step("Fremdanlagen können nicht weitergegeben werden", async () => {
        await pageWithBghUser.getByRole("button", { name: "Aktionen" }).click()
        const weitergebenMenuItem = pageWithBghUser.getByRole("menuitem", {
          name: "Weitergeben",
        })
        await weitergebenMenuItem.click()

        await expect(
          pageWithBghUser.getByText(
            "Nehmen Sie die Fremdanlage(n) im Eingang an, um sie bearbeiten zu können.",
          ),
        ).toBeVisible()
      })
    })

    test("Zeigt Fehlermeldung, dass Dokumentationseinheiten anderer Dokstellen können keinem Prozessschritt zugewiesen werden können", async ({
      pageWithBghUser,
    }) => {
      const publishedDocnumberDS = "YYTestDoc0001"
      await test.step("Nach Dokeinheit einer fremden Dokstelle suchen", async () => {
        await navigateToSearch(pageWithBghUser)
        await pageWithBghUser
          .getByLabel("Dokumentnummer Suche")
          .fill(publishedDocnumberDS)
        await pageWithBghUser
          .getByLabel("Nach Dokumentationseinheiten suchen")
          .click()
        await expect(
          pageWithBghUser.getByRole("row").getByText(publishedDocnumberDS),
        ).toBeVisible()
      })

      await test.step("Dokeinheit anderer Dokstelle kann selektiert werden", async () => {
        const rowLocator = pageWithBghUser.locator("tr", {
          hasText: publishedDocnumberDS,
        })
        const rowCheckbox = rowLocator.getByRole("checkbox")
        await rowCheckbox.click()
        await expect(rowCheckbox).toBeChecked()
      })

      await test.step("Dokeinheit anderer Dokstelle können nicht weitergegeben werden", async () => {
        await pageWithBghUser.getByRole("button", { name: "Aktionen" }).click()
        const weitergebenMenuItem = pageWithBghUser.getByRole("menuitem", {
          name: "Weitergeben",
        })
        await weitergebenMenuItem.click()

        await expect(
          pageWithBghUser.getByText(
            "Dokumentationseinheiten von fremden Dokstellen können nicht bearbeitet werden.",
          ),
        ).toBeVisible()
      })
    })

    test("Zeigt Fehlermeldung, wenn kein Prozessschritt zum Weitergeben ausgewählt wurde", async ({
      page,
      decisions,
    }) => {
      const { fileNumberPrefix, createdDecisions } = decisions
      // all new doc units are created with step Ersterfassung
      await test.step(`Starte Suche`, async () => {
        await openSearchWithFileNumberPrefix(fileNumberPrefix, page)
        await page.getByLabel("Nur meine Dokstelle Filter").click()
        await triggerSearch(page)
        await expect(
          page.getByText(createdDecisions.length + " Ergebnisse gefunden"),
        ).toBeVisible()
        await expect(
          page.getByText(createdDecisions[0].documentNumber),
        ).toBeVisible()
      })

      await test.step(`Selektiere alle Dokumentationseinheiten über Tabellenheader`, async () => {
        const selectAllCheckbox = page.getByRole("checkbox", {
          name: "Alle Elemente abgewählt",
        })
        await expect(selectAllCheckbox).toBeVisible()
        await selectAllCheckbox.click()
      })

      await test.step("'Aktionen' Button öffnet Prozesschritt Modal bei valider Auswahl", async () => {
        await page.getByRole("button", { name: "Aktionen" }).click()
        const weitergebenMenuItem = page.getByRole("menuitem", {
          name: "Weitergeben",
        })
        await weitergebenMenuItem.click()

        const newProcessStep = page.getByLabel("Neuer Schritt")
        await expect(newProcessStep).toBeVisible()
      })

      await test.step("Dokeinheiten können keinem neuen Prozesschritt zugewiesen werden, solange kein neuer Schritt ausgewählt wurde", async () => {
        await page.getByRole("button", { name: "Weitergeben" }).click()
        await expect(page.getByText("Wählen Sie einen Schritt")).toBeVisible()
        await expect(page.getByRole("alert")).toBeHidden()
        await expect(page.getByText("Weitergeben erfolgreich")).toBeHidden()
      })
    })

    // Backend Errors

    test("Zeigt Fehlermeldung, wenn beim Laden der möglichen Prozessschritte aus dem Backend ein Fehler zurückkommt", async ({
      page,
      decisions,
    }) => {
      const { fileNumberPrefix, createdDecisions } = decisions
      // all new doc units are created with step Ersterfassung
      await test.step(`Starte Suche`, async () => {
        await openSearchWithFileNumberPrefix(fileNumberPrefix, page)
        await page.getByLabel("Nur meine Dokstelle Filter").click()
        await triggerSearch(page)
        await expect(
          page.getByText(createdDecisions.length + " Ergebnisse gefunden"),
        ).toBeVisible()
        await expect(
          page.getByText(createdDecisions[0].documentNumber),
        ).toBeVisible()
      })

      await test.step(`Selektiere alle Dokumentationseinheiten über Tabellenheader`, async () => {
        const selectAllCheckbox = page.getByRole("checkbox", {
          name: "Alle Elemente abgewählt",
        })
        await expect(selectAllCheckbox).toBeVisible()
        await selectAllCheckbox.click()
      })

      await test.step("'Aktionen' Button öffnet Prozesschritt Modal", async () => {
        await page.route("**/caselaw/processsteps", async (route) => {
          await route.fulfill({
            status: 500,
            contentType: "application/json",
            body: JSON.stringify({
              message: "Internal Server Error",
            }),
          })
        })
        await page.getByRole("button", { name: "Aktionen" }).click()
        const weitergebenMenuItem = page.getByRole("menuitem", {
          name: "Weitergeben",
        })
        await weitergebenMenuItem.click()
      })

      await test.step("Zeigt Fehler an wenn Prozessschritte nicht geladen werden können", async () => {
        await expect(
          page.getByText(
            errorMessages
              .PROCESS_STEPS_OF_DOCUMENTATION_OFFICE_COULD_NOT_BE_LOADED.title,
          ),
        ).toBeVisible()
        await expect(
          page.getByText(
            errorMessages
              .PROCESS_STEPS_OF_DOCUMENTATION_OFFICE_COULD_NOT_BE_LOADED
              .description,
          ),
        ).toBeVisible()
      })
    })

    test("Zeigt Fehlermeldung, wenn beim Weitergeben der Dokeinheiten ein Fehler auftritt", async ({
      page,
      decisions,
    }) => {
      const { fileNumberPrefix, createdDecisions } = decisions
      // all new doc units are created with step Ersterfassung
      await test.step(`Starte Suche`, async () => {
        await openSearchWithFileNumberPrefix(fileNumberPrefix, page)
        await page.getByLabel("Nur meine Dokstelle Filter").click()
        await triggerSearch(page)
        await expect(
          page.getByText(createdDecisions.length + " Ergebnisse gefunden"),
        ).toBeVisible()
        await expect(
          page.getByText(createdDecisions[0].documentNumber),
        ).toBeVisible()
      })

      await test.step(`Selektiere alle Dokumentationseinheiten über Tabellenheader`, async () => {
        const selectAllCheckbox = page.getByRole("checkbox", {
          name: "Alle Elemente abgewählt",
        })
        await expect(selectAllCheckbox).toBeVisible()
        await selectAllCheckbox.click()
      })

      await test.step("'Aktionen' Button öffnet Prozesschritt Modal, wähle neuen Prozessschritt aus", async () => {
        await page.route(
          "**/caselaw/documentunits/bulk-assign-process-step",
          async (route) => {
            await route.fulfill({
              status: 500,
              contentType: "application/json",
              body: JSON.stringify({
                message: "Internal Server Error",
              }),
            })
          },
        )
        await page.getByRole("button", { name: "Aktionen" }).click()

        const weitergebenMenuItem = page.getByRole("menuitem", {
          name: "Weitergeben",
        })
        await weitergebenMenuItem.click()

        const newProcessStep = page.getByLabel("Neuer Schritt")
        await newProcessStep.click()
        await page.getByRole("option", { name: "Fachdokumentation" }).click()
        await expect(newProcessStep).toHaveText("Fachdokumentation")

        await page.getByRole("button", { name: "Weitergeben" }).click()
      })

      await test.step("Zeigt Fehler an wenn Dokumentationseinheiten nicht weitergeleitet werden können", async () => {
        await expect(
          page.getByText(errorMessages.BULK_ASSIGN_PROCESS_STEP_FAILED.title),
        ).toBeVisible()
        await expect(
          page.getByText(
            errorMessages.BULK_ASSIGN_PROCESS_STEP_FAILED.description,
          ),
        ).toBeVisible()
      })
    })
  },
)
