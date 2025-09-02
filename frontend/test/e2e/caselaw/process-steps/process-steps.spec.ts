import { expect, Page } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import {
  navigateToCategories,
  navigateToManagementData,
} from "~/e2e/caselaw/utils/e2e-utils"

// eslint-disable-next-line playwright/no-skipped-test
test.skip("process steps", { tag: ["@RISDEV-8565"] }, () => {
  test("rendering initial state, click on 'Weitergeben'", async ({
    pageWithBghUser,
    decisionBgh,
  }) => {
    await test.step("Create a new decision with BGH court", async () => {
      await navigateToCategories(pageWithBghUser, decisionBgh.documentNumber)
      const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")
      // the initial step and user is set automatically
      await expect(infoPanel).toContainText("Ersterfassung")
      // await expect(infoPanel).toContainText("BT")
    })

    await test.step("Open process step dialog again, expect next logical step to be visible, save new process step", async () => {
      await openProcessStepDialog(pageWithBghUser)
      await expect(pageWithBghUser.getByText("QS formal")).toBeVisible()
      await saveChangesAndCloseDialog(pageWithBghUser)
    })

    await test.step("Validate process step is displayed in info panel", async () => {
      const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")
      await expect(infoPanel).toContainText("EE")
      await expect(infoPanel).toContainText("QS formal")
    })
  })

  test("rendering initial state, click on 'Abbrechen'", async ({
    pageWithBghUser,
    decisionBgh,
  }) => {
    await test.step("Create a new decision with BGH court", async () => {
      await navigateToCategories(pageWithBghUser, decisionBgh.documentNumber)
      const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")
      await expect(infoPanel).toContainText("Ersterfassung")
      //   await expect(infoPanel).toContainText("BT")
    })

    await test.step("Open process step dialog", async () => {
      await openProcessStepDialog(pageWithBghUser)
    })

    await test.step("Click on 'Abbrechen' should not save changes", async () => {
      const dialog = pageWithBghUser.getByRole("dialog")
      const abbrechenButton = dialog.getByRole("button", { name: "Abbrechen" })
      await expect(abbrechenButton).toBeVisible()

      await abbrechenButton.click()
      await expect(dialog).toBeHidden()
    })

    await test.step("Validate process step is displayed in info panel", async () => {
      const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")
      await expect(infoPanel).toContainText("Ersterfassung")
    })
  })

  test("rendering initial state, manually select 'Fachdokumentation', click on 'Weitergeben'", async ({
    pageWithBghUser,
    decisionBgh,
  }) => {
    await test.step("Create a new decision with BGH court", async () => {
      await navigateToCategories(pageWithBghUser, decisionBgh.documentNumber)
      const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")
      await expect(infoPanel).toContainText("Ersterfassung")
      //  await expect(infoPanel).toContainText("BT")
    })

    await test.step("Open process step dialog, manually select 'Fachdokumentation', save new process step", async () => {
      await openProcessStepDialog(pageWithBghUser)
      const processStepDropBox = pageWithBghUser.getByRole("combobox", {
        name: "Neuer Schritt",
      })
      await expect(processStepDropBox).toContainText("QS formal")

      await processStepDropBox.click()
      await pageWithBghUser.getByText("Fachdokumentation").click()

      await expect(processStepDropBox).toContainText("Fachdokumentation")

      await saveChangesAndCloseDialog(pageWithBghUser)
    })

    await test.step("Validate current and previous process step is displayed in info panel", async () => {
      const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")
      await expect(infoPanel).toContainText("EE")
      await expect(infoPanel).toContainText("Fachdokumentation")
    })
  })

  test(
    "rendering initial state, select user, click on 'Weitergeben', validate logs in dialog",
    { tag: ["@RISDEV-8566"] },
    async ({ pageWithBghUser, decisionBgh }) => {
      await test.step("Create a new decision with BGH court", async () => {
        await navigateToCategories(pageWithBghUser, decisionBgh.documentNumber)
        const infoPanel = pageWithBghUser.getByTestId(
          "document-unit-info-panel",
        )
        await expect(infoPanel).toContainText("Ersterfassung")
        // await expect(infoPanel).toContainText("BT")
      })

      await test.step("Open process step dialog", async () => {
        await openProcessStepDialog(pageWithBghUser)
      })

      await test.step("Select bgh test user", async () => {
        await selectUser(pageWithBghUser, "BgH", "BGH  testUser", "BT")
      })

      await test.step("Save changes and close dialog", async () => {
        await saveChangesAndCloseDialog(pageWithBghUser)
      })

      await test.step("Validate process step is displayed in info panel", async () => {
        const infoPanel = pageWithBghUser.getByTestId(
          "document-unit-info-panel",
        )
        await expect(infoPanel).toContainText("QS formal")
        //    await expect(infoPanel).toContainText("BT")
      })

      await test.step("Open process step dialog", async () => {
        await openProcessStepDialog(pageWithBghUser)
      })

      await test.step("Validate process step is in process steps history logs", async () => {
        const firstRow = pageWithBghUser.locator("tbody tr").first()

        await expect(firstRow).toContainText("QS formal")
        //  await expect(firstRow).toContainText("BT")
      })

      await test.step("Close process step dialog", async () => {
        await closeProcessStepDialog(pageWithBghUser)
      })
    },
  )

  test(
    "does not update the current and last process badges in info panel, if only the user changes",
    { tag: ["@RISDEV-8566"] },
    async ({ pageWithBghUser, decisionBgh }) => {
      await test.step("Create a new decision with BGH court", async () => {
        await navigateToCategories(pageWithBghUser, decisionBgh.documentNumber)
        const infoPanel = pageWithBghUser.getByTestId(
          "document-unit-info-panel",
        )
        await expect(infoPanel).toContainText("Ersterfassung")
        //  await expect(infoPanel).toContainText("BT")
      })

      await test.step("Set next process step 'QS formal' with same user", async () => {
        await openProcessStepDialog(pageWithBghUser)
        const processStepDropBox = pageWithBghUser.getByRole("combobox", {
          name: "Neuer Schritt",
        })
        await expect(processStepDropBox).toContainText("QS formal")
        await selectUser(pageWithBghUser, "BGH", "BGH  testUser", "BT")
        await saveChangesAndCloseDialog(pageWithBghUser)
      })

      await test.step("Expect info panel to contain 'Ersterfassung' as previous and 'QS formal' as current step", async () => {
        const infoPanel = pageWithBghUser.getByTestId(
          "document-unit-info-panel",
        )

        // previous step in short form
        await expect(infoPanel).toContainText("EE")
        await expect(infoPanel).toContainText("QS formal")
        //    await expect(infoPanel).toContainText("BT")
      })

      await test.step("Manually set to current step 'QS formal', remove user", async () => {
        await openProcessStepDialog(pageWithBghUser)
        const processStepDropBox = pageWithBghUser.getByRole("combobox", {
          name: "Neuer Schritt",
        })
        await expect(processStepDropBox).toContainText("Fachdokumentation")

        await processStepDropBox.click()
        await pageWithBghUser
          .getByLabel("QS formal")
          .getByText("QS formal")
          .click()

        await expect(processStepDropBox).toContainText("QS formal")

        await saveChangesAndCloseDialog(pageWithBghUser)
      })

      await test.step("Expect info panel to contain 'Ersterfassung' as previous and 'QS formal' as current step", async () => {
        const infoPanel = pageWithBghUser.getByTestId(
          "document-unit-info-panel",
        )

        // previous step in short form
        await expect(infoPanel).toContainText("EE")
        await expect(infoPanel).toContainText("QS formal")
        await expect(infoPanel).not.toContainText("BT")
      })
    },
  )

  test("writes correct history logs and shows description based on docoffice", async ({
    pageWithBghUser,
    pageWithBfhUser,
    decisionBgh,
  }) => {
    let documentNumber = ""

    await test.step("Create a new decision with BGH court and create some logs", async () => {
      await navigateToCategories(pageWithBghUser, decisionBgh.documentNumber)

      documentNumber = /caselaw\/documentunit\/(.*)\/categories/g.exec(
        pageWithBghUser.url(),
      )?.[1] as string

      const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")
      await expect(infoPanel).toContainText("Ersterfassung")
      await expect(infoPanel).toContainText("BT")
      await openProcessStepDialog(pageWithBghUser)
      const processStepDropBox = pageWithBghUser.getByRole("combobox", {
        name: "Neuer Schritt",
      })
      await expect(processStepDropBox).toContainText("QS formal")
      await selectUser(pageWithBghUser, "BGH", "BGH  testUser", "BT")
      await saveChangesAndCloseDialog(pageWithBghUser)

      await expect(infoPanel).toContainText("QS formal")
      await openProcessStepDialog(pageWithBghUser)
      await expect(processStepDropBox).toContainText("Fachdokumentation")
      await saveChangesAndCloseDialog(pageWithBghUser)
      await expect(infoPanel).toContainText("Fachdokumentation")
    })

    await test.step("Open management data page", async () => {
      await navigateToManagementData(pageWithBghUser, documentNumber)
      const loadingMask = pageWithBghUser.locator('div[data-pc-section="mask"]')
      await expect(loadingMask).toBeHidden()
    })

    await test.step("Validate process steps history logs for own docoffice", async () => {
      const expectedHistory = [
        { von: "BGH (BGH testUser)", was: "Person entfernt: BGH testUser" },
        {
          von: "BGH (BGH testUser)",
          was: "Schritt geändert: QS formal → Fachdokumentation",
        },
        {
          von: "BGH (BGH testUser)",
          was: "Schritt geändert: Ersterfassung → QS formal",
        },
        { von: "BGH (BGH testUser)", was: "Dokeinheit angelegt" },
      ]

      const rows = pageWithBghUser.locator(
        'tbody[data-pc-section="tbody"] tr[data-pc-section="bodyrow"]',
      )

      await expect(rows).toHaveCount(expectedHistory.length)

      for (let i = 0; i < expectedHistory.length; i++) {
        const row = rows.nth(i)
        const expectedData = expectedHistory[i]

        const vonCell = row.locator("td").nth(1)
        const wasCell = row.locator("td").nth(2)

        await expect(vonCell).toHaveText(expectedData.von)
        await expect(wasCell).toHaveText(expectedData.was)
      }
    })

    await test.step("Assign to new doc office", async () => {
      const dropdown = pageWithBghUser.getByLabel(
        "Dokumentationsstelle auswählen",
      )
      await dropdown.click()
      await pageWithBghUser
        .getByLabel("dropdown-option")
        .getByText("BFH")
        .click()

      await expect(
        pageWithBghUser.locator('button[aria-label="dropdown-option"]'),
      ).toBeHidden()
      await pageWithBghUser.getByRole("button", { name: "Zuweisen" }).click()
      await pageWithBghUser.waitForLoadState()
    })

    await test.step("Open management data page with new docoffice", async () => {
      await navigateToManagementData(pageWithBfhUser, documentNumber)
      const loadingMask = pageWithBfhUser.locator('div[data-pc-section="mask"]')
      await expect(loadingMask).toBeHidden()
    })

    await test.step("Validate process steps history logs for new docoffice", async () => {
      const expectedHistory = [
        {
          von: "BGH",
          was: "Schritt geändert: Fachdokumentation → Neu",
        },
        {
          von: "BGH",
          was: "Dokstelle geändert: BGH → BFH",
        },
        { von: "BGH", was: "Person geändert" },
        {
          von: "BGH",
          was: "Schritt geändert: QS formal → Fachdokumentation",
        },
        {
          von: "BGH",
          was: "Schritt geändert: Ersterfassung → QS formal",
        },
        { von: "BGH", was: "Dokeinheit angelegt" },
      ]

      const rows = pageWithBfhUser.locator(
        'tbody[data-pc-section="tbody"] tr[data-pc-section="bodyrow"]',
      )

      await expect(rows).toHaveCount(expectedHistory.length)

      for (let i = 0; i < expectedHistory.length; i++) {
        const row = rows.nth(i)
        const expectedData = expectedHistory[i]

        const vonCell = row.locator("td").nth(1)
        const wasCell = row.locator("td").nth(2)

        await expect(vonCell).toHaveText(expectedData.von)
        await expect(wasCell).toHaveText(expectedData.was)
      }
    })

    await test.step("Delete docunit manually", async () => {
      const deleteButton = pageWithBfhUser.getByLabel(
        "Dokumentationseinheit löschen",
      )
      await expect(deleteButton).toBeVisible()
      await deleteButton.click()
      await expect(
        pageWithBfhUser
          .getByRole("dialog")
          .getByText("Dokumentationseinheit löschen"),
      ).toBeVisible()

      await pageWithBfhUser
        .getByRole("button", {
          name: "Löschen",
          exact: true,
        })
        .click()
    })
  })

  async function selectUser(
    page: Page,
    searchTerm: string,
    expectedUser: string,
    expectedInitials: string,
  ) {
    const dialog = page.getByRole("dialog")

    await expect(dialog).toBeVisible()

    await expect(dialog.getByText("Neue Person")).toBeVisible()
    await page.getByLabel("Neue Person", { exact: true }).fill(searchTerm)
    await expect(page.getByTestId("combobox-spinner")).toBeVisible()
    await expect(page.getByTestId("combobox-spinner")).toBeHidden()

    await expect(dialog.getByText(expectedUser)).toBeVisible()
    await expect(dialog.getByText(expectedInitials)).toBeVisible()

    const firstItem = dialog
      .getByRole("button", { name: "dropdown-option" })
      .first()
    await expect(firstItem).toContainText(expectedUser)
    await firstItem.click()
  }

  async function openProcessStepDialog(page: Page) {
    const dialog = page.getByRole("dialog")

    await page
      .getByRole("button", { name: "Dokumentationseinheit weitergeben" })
      .click()
    await expect(dialog).toBeVisible()
    await expect(
      dialog.getByText("Dokumentationseinheit weitergeben"),
    ).toBeVisible()
    await expect(dialog.getByText("Neuer Schritt")).toBeVisible()

    const weitergebenButton = dialog.getByRole("button", {
      name: "Weitergeben",
    })
    await expect(weitergebenButton).toBeVisible()
    await expect(
      dialog.getByRole("button", { name: "Abbrechen" }),
    ).toBeVisible()
  }

  async function closeProcessStepDialog(page: Page) {
    const dialog = page.getByRole("dialog")

    await page.getByRole("button", { name: "Abbrechen" }).click()
    await expect(dialog).toBeHidden()
  }

  async function saveChangesAndCloseDialog(page: Page) {
    const dialog = page.getByRole("dialog")
    const weitergebenButton = dialog.getByRole("button", {
      name: "Weitergeben",
    })
    await weitergebenButton.click()

    await expect(dialog).toBeHidden()
  }
})
