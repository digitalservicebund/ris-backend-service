import { expect } from "@playwright/test"
import { caselawTest as test } from "~/e2e/caselaw/fixtures"
import { navigateToSearch } from "~/e2e/caselaw/utils/e2e-utils"

test.describe("process steps", () => {
  test("rendering initial state, click on 'Weitergeben'", async ({
    pageWithBghUser,
  }) => {
    await navigateToSearch(pageWithBghUser)

    await pageWithBghUser
      .getByRole("button", { name: "Neue Dokumentationseinheit" })
      .first()
      .click()

    const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")
    await expect(infoPanel).toContainText("Neu")

    await pageWithBghUser
      .getByRole("button", { name: "Dokumentationseinheit weitergeben" })
      .click()

    const dialog = pageWithBghUser.getByRole("dialog")
    await expect(dialog).toBeVisible()
    await expect(
      dialog.getByText("Dokumentationseinheit weitergeben"),
    ).toBeVisible()
    await expect(dialog.getByText("Neuer Schritt")).toBeVisible()
    await expect(
      dialog.getByRole("combobox", { name: "Neuer Schritt" }),
    ).toContainText("Ersterfassung")

    const weitergebenButton = dialog.getByRole("button", {
      name: "Weitergeben",
    })
    await expect(weitergebenButton).toBeVisible()
    await expect(
      dialog.getByRole("button", { name: "Abbrechen" }),
    ).toBeVisible()

    await weitergebenButton.click()

    await expect(dialog).toBeHidden()

    await expect(infoPanel).toContainText("N")
    await expect(infoPanel).toContainText("Ersterfassung")
  })

  test("rendering initial state, click on 'Abbrechen'", async ({
    pageWithBghUser,
  }) => {
    await navigateToSearch(pageWithBghUser)

    await pageWithBghUser
      .getByRole("button", { name: "Neue Dokumentationseinheit" })
      .first()
      .click()

    const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")
    await expect(infoPanel).toContainText("Neu")

    await pageWithBghUser
      .getByRole("button", { name: "Dokumentationseinheit weitergeben" })
      .click()

    const dialog = pageWithBghUser.getByRole("dialog")
    await expect(dialog).toBeVisible()
    await expect(
      dialog.getByText("Dokumentationseinheit weitergeben"),
    ).toBeVisible()
    await expect(dialog.getByText("Neuer Schritt")).toBeVisible()
    await expect(
      dialog.getByRole("combobox", { name: "Neuer Schritt" }),
    ).toContainText("Ersterfassung")
    await expect(
      dialog.getByRole("button", { name: "Weitergeben" }),
    ).toBeVisible()

    const abbrechenButton = dialog.getByRole("button", { name: "Abbrechen" })
    await expect(abbrechenButton).toBeVisible()

    await abbrechenButton.click()

    await expect(dialog).toBeHidden()

    await expect(infoPanel).toContainText("Neu")
  })

  test("rendering initial state, select 'QS formal', click on 'Weitergeben'", async ({
    pageWithBghUser,
  }) => {
    await navigateToSearch(pageWithBghUser)

    await pageWithBghUser
      .getByRole("button", { name: "Neue Dokumentationseinheit" })
      .first()
      .click()

    const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")
    await expect(infoPanel).toContainText("Neu")

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
    await expect(processStepDropBox).toContainText("Ersterfassung")

    await processStepDropBox.click()
    await pageWithBghUser.getByText("QS formal").click()

    await expect(processStepDropBox).toContainText("QS formal")

    const weitergebenButton = dialog.getByRole("button", {
      name: "Weitergeben",
    })
    await expect(weitergebenButton).toBeVisible()
    await expect(
      dialog.getByRole("button", { name: "Abbrechen" }),
    ).toBeVisible()

    await weitergebenButton.click()

    await expect(dialog).toBeHidden()

    await expect(infoPanel).toContainText("N")
    await expect(infoPanel).toContainText("QS formal")
  })

  test("rendering initial state, select 'Neu', click on 'Weitergeben'", async ({
    pageWithBghUser,
  }) => {
    await navigateToSearch(pageWithBghUser)

    await pageWithBghUser
      .getByRole("button", { name: "Neue Dokumentationseinheit" })
      .first()
      .click()

    const infoPanel = pageWithBghUser.getByTestId("document-unit-info-panel")
    await expect(infoPanel).toContainText("Neu")

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
    await expect(processStepDropBox).toContainText("Ersterfassung")

    await processStepDropBox.click()
    await pageWithBghUser.getByLabel("Neu", { exact: true }).click()

    await expect(processStepDropBox).toContainText("Neu")

    const weitergebenButton = dialog.getByRole("button", {
      name: "Weitergeben",
    })
    await expect(weitergebenButton).toBeVisible()
    await expect(
      dialog.getByRole("button", { name: "Abbrechen" }),
    ).toBeVisible()

    await weitergebenButton.click()

    await expect(dialog).toBeHidden()

    await expect(infoPanel).toContainText("Neu")
  })
})
