import { expect, Page } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { navigateToSearch } from "~/e2e/caselaw/utils/e2e-utils"

test.describe("process steps", { tag: ["@RISDEV-8565"] }, () => {
  test("rendering initial state, click on 'Weitergeben'", async ({
    pageWithBghUser,
  }) => {
    const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")

    await test.step("Create a new decision with BGH court", async () => {
      await navigateToSearch(pageWithBghUser)
      await pageWithBghUser
        .getByRole("button", { name: "Neue Entscheidung" })
        .first()
        .click()

      await expect(infoPanel).toContainText("Ersterfassung")
    })

    await test.step("Open process step dialog", async () => {
      await openProcessStepDialog(pageWithBghUser)
    })

    await test.step("Save changes and close dialog", async () => {
      await saveChangesAndCloseDialog(pageWithBghUser)
    })

    await test.step("Validate process step is displayed in info panel", async () => {
      await expect(infoPanel).toContainText("EE")
      await expect(infoPanel).toContainText("QS formal")
    })
  })

  test("rendering initial state, click on 'Abbrechen'", async ({
    pageWithBghUser,
  }) => {
    const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")

    await test.step("Create a new decision with BGH court", async () => {
      await navigateToSearch(pageWithBghUser)
      await pageWithBghUser
        .getByRole("button", { name: "Neue Entscheidung" })
        .first()
        .click()

      await expect(infoPanel).toContainText("Ersterfassung")
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
      await expect(infoPanel).toContainText("Ersterfassung")
    })
  })

  test("rendering initial state, select 'Fachdokumentation', click on 'Weitergeben'", async ({
    pageWithBghUser,
  }) => {
    await navigateToSearch(pageWithBghUser)

    await pageWithBghUser
      .getByRole("button", { name: "Neue Entscheidung" })
      .first()
      .click()

    const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")
    await expect(infoPanel).toContainText("Ersterfassung")

    await pageWithBghUser
      .getByRole("button", { name: "Dokumentationseinheit weitergeben" })
      .click()

    const dialog = pageWithBghUser.getByRole("dialog")
    await expect(dialog).toBeVisible()
    await expect(
      dialog.getByText("Dokumentationseinheit weitergeben"),
    ).toBeVisible()
    await expect(dialog.getByText("Neuer Schritt")).toBeVisible()
    const processStepDropBox = dialog.getByRole("combobox", {
      name: "Neuer Schritt",
    })
    await expect(processStepDropBox).toContainText("QS formal")

    await processStepDropBox.click()
    await pageWithBghUser.getByText("Fachdokumentation").click()

    await expect(processStepDropBox).toContainText("Fachdokumentation")

    const weitergebenButton = dialog.getByRole("button", {
      name: "Weitergeben",
    })
    await expect(weitergebenButton).toBeVisible()
    await expect(
      dialog.getByRole("button", { name: "Abbrechen" }),
    ).toBeVisible()

    await weitergebenButton.click()

    await expect(dialog).toBeHidden()

    await expect(infoPanel).toContainText("EE")
    await expect(infoPanel).toContainText("Fachdokumentation")
  })

  test("rendering initial state, select 'Ersterfassung', click on 'Weitergeben'", async ({
    pageWithBghUser,
  }) => {
    const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")
    await test.step("Create a new decision with BGH court", async () => {
      await navigateToSearch(pageWithBghUser)
      await pageWithBghUser
        .getByRole("button", { name: "Neue Entscheidung" })
        .first()
        .click()

      await expect(infoPanel).toContainText("Ersterfassung")
    })

    await test.step("Open process step dialog", async () => {
      await openProcessStepDialog(pageWithBghUser)
    })

    await test.step("Select 'Ersterfassung' process step", async () => {
      await selectProcessStep(pageWithBghUser, "Ersterfassung")
    })

    await test.step("Save changes and close dialog", async () => {
      await saveChangesAndCloseDialog(pageWithBghUser)
    })

    await test.step("Validate process step is displayed in info panel", async () => {
      await expect(infoPanel).toContainText("Ersterfassung")
    })

    await test.step("Open process step dialog", async () => {
      await openProcessStepDialog(pageWithBghUser)
    })

    await test.step("Validate process step is in process steps history logs", async () => {
      const firstRow = pageWithBghUser.locator("tbody tr").first()

      await expect(firstRow).toContainText("Ersterfassung")
      await expect(firstRow).toContainText("-")
    })
  })

  test("rendering initial state, select 'Ersterfassung', select user, click on 'Weitergeben'", async ({
    pageWithBghUser,
  }) => {
    const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")

    await test.step("Create a new decision with BGH court", async () => {
      await navigateToSearch(pageWithBghUser)
      await pageWithBghUser
        .getByRole("button", { name: "Neue Entscheidung" })
        .first()
        .click()

      await expect(infoPanel).toContainText("Ersterfassung")
    })

    await test.step("Open process step dialog", async () => {
      await openProcessStepDialog(pageWithBghUser)
    })

    await test.step("Select bgh test user", async () => {
      await selectUser(pageWithBghUser, "BgH", "BGH  testUser", "BT")
    })

    await test.step("Select 'QS fachlich' process step", async () => {
      await selectProcessStep(pageWithBghUser, "QS fachlich")
    })

    await test.step("Save changes and close dialog", async () => {
      await saveChangesAndCloseDialog(pageWithBghUser)
    })

    await test.step("Validate process step is displayed in info panel", async () => {
      await expect(infoPanel).toContainText("QS fachlich")
    })

    await test.step("Open process step dialog", async () => {
      await openProcessStepDialog(pageWithBghUser)
    })

    await test.step("Validate process step is in process steps history logs", async () => {
      const firstRow = pageWithBghUser.locator("tbody tr").first()

      await expect(firstRow).toContainText("QS fachlich")
      await expect(firstRow).toContainText("BT")
    })

    await test.step("Close process step dialog", async () => {
      await closeProcessStepDialog(pageWithBghUser)
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

  async function selectProcessStep(page: Page, processStepName: string) {
    const dialog = page.getByRole("dialog")

    await expect(dialog.getByText("Neuer Schritt")).toBeVisible()
    const processStepDropBox = dialog.getByRole("combobox", {
      name: "Neuer Schritt",
    })

    await processStepDropBox.click()

    await page.getByRole("option", { name: processStepName }).click()

    await expect(processStepDropBox).toContainText(processStepName)
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
