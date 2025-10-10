import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { deleteAllProcedures } from "~/e2e/caselaw/utils/documentation-unit-api-util"
import {
  assignProcedureToDocUnit,
  assignUserGroupToProcedure,
  clickCategoryButton,
  fillInput,
  navigateToCategories,
  navigateToProcedures,
  navigateToSearch,
  save,
  unassignUserGroupFromProcedure,
} from "~/e2e/caselaw/utils/e2e-utils"
import { generateString } from "~/test-helper/dataGenerators"

test.describe(
  "external user",
  {
    annotation: {
      description:
        "https://digitalservicebund.atlassian.net/browse/RISDEV-2075",
      type: "epic",
    },
    tag: ["@RISDEV-2075"],
  },
  () => {
    // If tests run in parallel, we do not want to delete other procedures -> random prefix
    const procedurePrefix = generateString({ length: 10 })

    test(
      "In doc unit search, external user can not edit unassigned doc units, but preview them",
      { tag: ["@RISDEV-4518", "@RISDEV-4519"] },
      async ({ pageWithExternalUser, documentNumber }) => {
        await test.step("Filter for unassigned doc unit and view in search results as external", async () => {
          await navigateToSearch(pageWithExternalUser)
          await pageWithExternalUser
            .getByLabel("Dokumentnummer Suche")
            .fill(documentNumber)
          await pageWithExternalUser
            .getByLabel("Nach Dokumentationseinheiten suchen")
            .click()
          await expect(
            pageWithExternalUser.getByRole("row").getByText(documentNumber),
          ).toBeVisible()
        })

        await test.step("Check edit/preview buttons for doc unit in search result", async () => {
          // Preview is allowed
          await expect(
            pageWithExternalUser.getByLabel("Dokumentationseinheit ansehen"),
          ).toHaveRole("button")
          await expect(
            pageWithExternalUser.getByLabel("Dokumentationseinheit ansehen"),
          ).toBeEnabled()

          // Edit is not allowed
          await expect(
            pageWithExternalUser.getByLabel("Dokumentationseinheit bearbeiten"),
          ).toBeDisabled()
        })
      },
    )

    test(
      "In doc unit search, external user can select edit for doc units, that were previously assigned to their user group by an internal user",
      { tag: ["@RISDEV-4518", "@RISDEV-4519"] },
      async ({ page, pageWithExternalUser, documentNumber }) => {
        const procedureName = await assignProcedureToDocUnit(
          page,
          documentNumber,
          procedurePrefix,
        )

        await assignUserGroupToProcedure(page, procedureName)

        await test.step("Filter for assigned doc unit and view in search results as external", async () => {
          await navigateToSearch(pageWithExternalUser)
          await pageWithExternalUser
            .getByLabel("Dokumentnummer Suche")
            .fill(documentNumber)
          await pageWithExternalUser
            .getByLabel("Nach Dokumentationseinheiten suchen")
            .click()
          await expect(
            pageWithExternalUser.getByRole("row").getByText(documentNumber),
          ).toBeVisible()
        })

        await test.step("Check edit/preview buttons for doc unit in search result", async () => {
          // Edit/Preview is allowed
          await expect(
            pageWithExternalUser.getByLabel("Dokumentationseinheit bearbeiten"),
          ).toBeEnabled()
          await expect(
            pageWithExternalUser.getByLabel("Dokumentationseinheit ansehen"),
          ).toBeEnabled()
        })

        await unassignUserGroupFromProcedure(page, procedureName)
      },
    )

    test(
      "External user can only see procedures they are assigned to",
      { tag: ["@RISDEV-4519"] },
      async ({
        page,
        pageWithExternalUser,
        documentNumber,
        prefilledDocumentUnit,
        secondPrefilledDocumentUnit,
      }) => {
        const testCaseInfix = generateString({
          length: 10,
          prefix: procedurePrefix,
        })

        const procedureName = await assignProcedureToDocUnit(
          page,
          documentNumber,
          testCaseInfix,
        )
        await assignUserGroupToProcedure(page, procedureName)

        // Procedures are created but not assigned
        await assignProcedureToDocUnit(
          page,
          prefilledDocumentUnit.documentNumber!,
          testCaseInfix,
        )
        await assignProcedureToDocUnit(
          page,
          secondPrefilledDocumentUnit.documentNumber!,
          testCaseInfix,
        )

        await test.step("External user sees only the single procedure they are assigned to", async () => {
          await navigateToProcedures(pageWithExternalUser, testCaseInfix)
          await expect(
            pageWithExternalUser.getByLabel("Vorgang Listenelement"),
          ).toHaveCount(1)
          await expect(
            pageWithExternalUser.getByLabel("Vorgang Listenelement"),
          ).toContainText(procedureName)
        })

        await test.step("External user can edit doc units from procedure view", async () => {
          await pageWithExternalUser.getByLabel("Vorgang Listenelement").click()
          await expect(
            pageWithExternalUser.getByLabel("Dokumentationseinheit bearbeiten"),
          ).toBeEnabled()
        })
      },
    )

    test(
      "External user cannot edit a doc unit after they are unassigned",
      { tag: ["@RISDEV-4519"] },
      async ({ pageWithExternalUser, page, documentNumber }) => {
        await navigateToCategories(pageWithExternalUser, documentNumber)

        const procedureName = await assignProcedureToDocUnit(
          page,
          documentNumber,
          procedurePrefix,
        )
        await assignUserGroupToProcedure(page, procedureName)

        await test.step("Assigned external user can edit and save Entscheidungsnamen", async () => {
          await clickCategoryButton("Entscheidungsnamen", pageWithExternalUser)
          await pageWithExternalUser
            .getByLabel("Entscheidungsnamen")
            .fill("ein Name")
          await save(pageWithExternalUser)
        })

        await unassignUserGroupFromProcedure(page, procedureName)

        await test.step("Unassigned external user gets error when editing Entscheidungsname", async () => {
          await pageWithExternalUser
            .getByLabel("Entscheidungsname")
            .fill("ein anderer Name")
          await pageWithExternalUser
            .getByLabel("Speichern Button", { exact: true })
            .click()
          await expect(
            pageWithExternalUser
              .getByTestId("document-unit-save-button")
              .locator("p"),
          ).toHaveText(`Fehler beim Speichern: Keine Berechtigung`)
        })
      },
    )

    test(
      "External user cannot edit a doc unit after the doc unit is unassigned",
      { tag: ["@RISDEV-4519", "@RISDEV-4523"] },
      async ({ pageWithExternalUser, page, documentNumber }) => {
        await navigateToCategories(pageWithExternalUser, documentNumber)

        const procedureName = await assignProcedureToDocUnit(
          page,
          documentNumber,
          procedurePrefix,
        )
        await assignUserGroupToProcedure(page, procedureName)

        await test.step("Assigned external user can edit and save Notiz", async () => {
          await pageWithExternalUser.getByLabel("Seitenpanel öffnen").click()
          await fillInput(
            pageWithExternalUser,
            "Notiz Eingabefeld",
            "some text",
          )
          await save(pageWithExternalUser)
        })

        await test.step("Assigned external user cannot access Dokumente or Fundstellen", async () => {
          await expect(
            pageWithExternalUser.getByTestId(
              "caselaw-documentUnit-documentNumber-attachments",
            ),
          ).toBeHidden()
          await expect(
            pageWithExternalUser.getByTestId(
              "caselaw-documentUnit-documentNumber-references",
            ),
          ).toBeHidden()
        })

        // Assignment to previous procedure is overwritten -> user (group) is not assigned to doc unit anymore
        await assignProcedureToDocUnit(page, documentNumber, procedurePrefix)

        await test.step("External user gets error when editing Notiz of unassigned doc unit", async () => {
          await fillInput(
            pageWithExternalUser,
            "Notiz Eingabefeld",
            "some other text",
          )
          await pageWithExternalUser
            .getByLabel("Speichern Button", { exact: true })
            .click()
          await expect(
            pageWithExternalUser
              .getByTestId("document-unit-save-button")
              .locator("p"),
          ).toHaveText(`Fehler beim Speichern: Keine Berechtigung`)
        })
      },
    )

    test(
      "Assigned external user cannot edit core data of a doc unit via API",
      { tag: ["@RISDEV-4519"] },
      async ({ page, pageWithExternalUser, prefilledDocumentUnit }) => {
        const procedureName = await assignProcedureToDocUnit(
          page,
          prefilledDocumentUnit.documentNumber!,
          procedurePrefix,
        )
        await assignUserGroupToProcedure(page, procedureName)

        const cookies = await pageWithExternalUser.context().cookies()
        const csrfToken = cookies.find((cookie) => cookie.name === "XSRF-TOKEN")

        await test.step("Assigned external user can edit allowed categories via API", async () => {
          const allowedApiResponse = await pageWithExternalUser
            .context()
            .request.patch(
              "api/v1/caselaw/documentunits/" + prefilledDocumentUnit.uuid,
              {
                data: {
                  documentationUnitVersion: 500,
                  patch: [],
                  errorPaths: [],
                },
                headers: { "X-XSRF-TOKEN": csrfToken!.value },
              },
            )
          await expect(allowedApiResponse).toBeOK()
        })

        await test.step("Assigned external user cannot edit forbidden categories via API", async () => {
          const forbiddenApiResponse = await page
            .context()
            .request.patch(
              "api/v1/caselaw/documentunits/" + prefilledDocumentUnit.uuid,
              {
                data: {
                  documentationUnitVersion: 501,
                  patch: [
                    {
                      op: "replace",
                      path: "/coreData/appraisalBody",
                      value: "321321easdasdqweqwe",
                    },
                  ],
                  errorPaths: [],
                },
                headers: { "X-XSRF-TOKEN": csrfToken!.value },
              },
            )
          expect(forbiddenApiResponse.status()).toBe(403)
        })
      },
    )

    test.afterAll(async ({ browser }) => {
      await deleteAllProcedures(browser, procedurePrefix)
    })
  },
)
