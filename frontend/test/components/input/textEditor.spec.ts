/* eslint-disable testing-library/no-node-access */
import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { fireEvent, render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import { createRouter, createWebHistory } from "vue-router"
import TextEditor from "@/components/input/TextEditor.vue"
import { TextAreaInputAttributes } from "@/components/input/types"
import { longTextLabels } from "@/domain/decision"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

useFeatureToggleServiceMock()

describe("text editor", async () => {
  const renderComponent = async (options?: {
    ariaLabel?: string
    editable?: boolean
    value?: string
    fieldSize?: TextAreaInputAttributes["fieldSize"]
  }) => {
    userEvent.setup()
    const router = createRouter({
      history: createWebHistory(),
      routes: [
        {
          path: "/",
          name: "home",
          component: {},
        },
        {
          path: "/caselaw/documentUnit/:documentNumber/categories#coreData",
          name: "caselaw-documentUnit-documentNumber-categories#coreData",
          component: {},
        },
      ],
    })
    render(TextEditor, {
      props: {
        value: options?.value,
        ariaLabel: options?.ariaLabel,
        editable: options?.editable,
        fieldSize: options?.fieldSize,
      },
      global: { plugins: [router, createTestingPinia()] },
    })

    await flushPromises()
  }

  const WARNING_TEXT = "Keine Tabellenzeile ausgewählt"

  const clickTableSubButton = async (subButtonLabel: string) => {
    const tableBorderMenu = screen.getByLabelText("Tabelle Rahmen")
    await userEvent.click(tableBorderMenu)
    const subButton = screen.getByLabelText(subButtonLabel)
    await userEvent.click(subButton)
  }

  const insertTable = async () => {
    const tableMenu = screen.getByLabelText("Tabelle", { exact: true })
    await userEvent.click(tableMenu)

    const insertButton = screen.getByLabelText("Tabelle einfügen")
    await userEvent.click(insertButton)
    await flushPromises()
  }

  test("renders text editor with default props", async () => {
    await renderComponent()

    expect(screen.getAllByTestId("Editor Feld").length).toBe(1)
  })

  test("renders text editor with props", async () => {
    await renderComponent({
      value: "Test Value",
      ariaLabel: "Gründe",
    })

    expect(screen.getByText("Test Value")).toBeInTheDocument()
    expect(screen.getByTestId("Gründe")).toBeInTheDocument()
  })

  test.each([
    ["max", "h-full"],
    ["big", "h-320"],
    ["medium", "h-160"],
    ["small", "h-96"],
    [undefined, "h-160"],
  ] as const)("renders %s field with correct class", async (a, expected) => {
    await renderComponent({ fieldSize: a })

    expect(await screen.findByTestId("Editor Feld")).toHaveClass(expected)
  })

  test("enable buttons on focus", async () => {
    await renderComponent({
      value: "Test Value",
      ariaLabel: "Gründe",
      editable: true,
    })

    const editorField = screen.getByTestId("Gründe")

    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
    }

    expect(screen.getByLabelText("Gründe Button Leiste")).toBeInTheDocument()
    expect(screen.getByLabelText("Erweitern")).toBeEnabled()
    expect(screen.getByLabelText("Rückgängig machen")).toBeEnabled()
    expect(screen.getByLabelText("Wiederherstellen")).toBeEnabled()
  })

  test("disable buttons on blur", async () => {
    await renderComponent({
      value: "Test Value",
      ariaLabel: "Gründe",
      editable: true,
    })

    const editorField = screen.getByTestId("Gründe")

    if (editorField.firstElementChild !== null) {
      await fireEvent.blur(editorField.firstElementChild)
    }

    expect(screen.getByLabelText("Gründe Button Leiste")).toBeInTheDocument()
    expect(screen.getByLabelText("Erweitern")).toBeDisabled()
    expect(screen.getByLabelText("Rückgängig machen")).toBeDisabled()
    expect(screen.getByLabelText("Wiederherstellen")).toBeDisabled()
  })

  /*
   * The purpose of this test is to ensure that all expected buttons are
   * rendered. Having this test helps us to ensure that we do not accidentally
   * remove any of them.
   * Unfortunately, the logic of the button bar is very complex and dependents on
   * the current width of the editor element. Thereby it is very hard, or rather
   * impossible to test this end-to-end as it depends on the surrounding layout.
   * Thereby this is rather an integration test, as the button list is
   * a configuration of the component. The logic of the button bar and how it
   * collapses for certain widths, is a logic for itself that gets tested
   * separately.
   * The test should be continuously improved to verify that all buttons exist.
   */
  it("shows all necessary editor buttons", async () => {
    await renderComponent({
      value: "Test Value",
      ariaLabel: "Gründe",
      editable: true,
    })

    const editorField = screen.getByTestId("Gründe")

    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
    }

    expect(screen.getByLabelText("Erweitern")).toBeInTheDocument()
    expect(screen.getByLabelText("Nicht-druckbare Zeichen")).toBeInTheDocument()
    expect(screen.getByLabelText("Fett")).toBeInTheDocument()
    expect(screen.getByLabelText("Kursiv")).toBeInTheDocument()
    expect(screen.getByLabelText("Unterstrichen")).toBeInTheDocument()
    expect(screen.getByLabelText("Durchgestrichen")).toBeInTheDocument()
    expect(screen.getByLabelText("Hochgestellt")).toBeInTheDocument()
    expect(screen.getByLabelText("Tiefgestellt")).toBeInTheDocument()
    expect(screen.getByLabelText("Linksbündig")).toBeInTheDocument()
    expect(screen.getByLabelText("Zentriert")).toBeInTheDocument()
    expect(screen.getByLabelText("Rechtsbündig")).toBeInTheDocument()
    expect(screen.getByLabelText("Aufzählungsliste")).toBeInTheDocument()
    expect(screen.getByLabelText("Nummerierte Liste")).toBeInTheDocument()
    expect(screen.getByLabelText("Einzug verringern")).toBeInTheDocument()
    expect(screen.getByLabelText("Einzug vergrößern")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Tabelle", { exact: true }),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Tabelle Rahmen", { exact: true }),
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Zitat einfügen")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Randnummern neu erstellen"),
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Randnummern entfernen")).toBeInTheDocument()
    expect(screen.getByLabelText("Rückgängig machen")).toBeInTheDocument()
    expect(screen.getByLabelText("Wiederherstellen")).toBeInTheDocument()
    expect(screen.getByLabelText("Rechtschreibprüfung")).toBeInTheDocument()
  })

  it("shows all table buttons after menu is expanded", async () => {
    await renderComponent({
      value: "Test Value",
      ariaLabel: "Gründe",
      editable: true,
    })

    const editorField = screen.getByTestId("Gründe")

    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
    }

    expect(
      screen.getByLabelText("Tabelle", { exact: true }),
    ).toBeInTheDocument()
    expect(screen.queryByLabelText("Tabelle einfügen")).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Tabelle löschen")).not.toBeInTheDocument()
    expect(
      screen.queryByLabelText("Spalte rechts einfügen"),
    ).not.toBeInTheDocument()
    expect(
      screen.queryByLabelText("Zeile darunter einfügen"),
    ).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Spalte löschen")).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Zeile löschen")).not.toBeInTheDocument()

    await fireEvent.click(screen.getByLabelText("Tabelle", { exact: true }))

    expect(screen.getByLabelText("Tabelle einfügen")).toBeInTheDocument()
    expect(screen.getByLabelText("Tabelle löschen")).toBeInTheDocument()
    expect(screen.getByLabelText("Spalte rechts einfügen")).toBeInTheDocument()
    expect(screen.getByLabelText("Zeile darunter einfügen")).toBeInTheDocument()
    expect(screen.getByLabelText("Spalte löschen")).toBeInTheDocument()
    expect(screen.getByLabelText("Zeile löschen")).toBeInTheDocument()
    expect(screen.getByLabelText("Rechtschreibprüfung")).toBeInTheDocument()
  })

  it("shows all table border buttons after menu is expanded", async () => {
    await renderComponent({
      value: "Test Value",
      ariaLabel: "Gründe",
      editable: true,
    })

    const editorField = screen.getByTestId("Gründe")

    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
    }

    expect(
      screen.getByLabelText("Tabelle Rahmen", { exact: true }),
    ).toBeInTheDocument()
    expect(screen.queryByLabelText("Alle Rahmen")).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Kein Rahmen")).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Linker Rahmen")).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Rechter Rahmen")).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Oberer Rahmen")).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Unterer Rahmen")).not.toBeInTheDocument()

    await fireEvent.click(
      screen.getByLabelText("Tabelle Rahmen", { exact: true }),
    )

    expect(
      screen.getByLabelText("Tabelle Rahmen", { exact: true }),
    ).toBeInTheDocument()
    expect(screen.getByLabelText("Alle Rahmen")).toBeInTheDocument()
    expect(screen.getByLabelText("Kein Rahmen")).toBeInTheDocument()
    expect(screen.getByLabelText("Linker Rahmen")).toBeInTheDocument()
    expect(screen.getByLabelText("Rechter Rahmen")).toBeInTheDocument()
    expect(screen.getByLabelText("Oberer Rahmen")).toBeInTheDocument()
    expect(screen.getByLabelText("Unterer Rahmen")).toBeInTheDocument()
  })

  it.each([
    longTextLabels.tenor,
    longTextLabels.participatingJudges,
    longTextLabels.outline,
  ])("hides add border number button for category %s", async (category) => {
    await renderComponent({
      value: "Test Value",
      ariaLabel: category,
      editable: true,
    })

    const editorField = screen.getByTestId(category!)

    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
    }

    expect(
      screen.queryByText("Randnummern neu erstellen"),
    ).not.toBeInTheDocument()
  })

  it.each([
    longTextLabels.reasons,
    longTextLabels.caseFacts,
    longTextLabels.decisionReasons,
    longTextLabels.dissentingOpinion,
    longTextLabels.otherLongText,
  ])("shows add border number button for category %s", async (category) => {
    await renderComponent({
      value: "Test Value",
      ariaLabel: category,
      editable: true,
    })

    const editorField = screen.getByTestId(category!)

    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
    }

    expect(
      screen.getByLabelText("Randnummern neu erstellen"),
    ).toBeInTheDocument()
  })

  describe("table selection warning", () => {
    test("should show warning when a border command is executed without cell selection", async () => {
      await renderComponent({
        value: "<p></p>",
        ariaLabel: "Gründe",
        editable: true,
      })
      const editorField = screen.getByTestId("Gründe")
      await fireEvent.focus(editorField.firstElementChild!)

      await clickTableSubButton("Alle Rahmen")
      await flushPromises()

      expect(screen.getByText(WARNING_TEXT)).toBeInTheDocument()
    })

    test("should hide warning immediately after a cell is focused", async () => {
      await renderComponent({
        value: "<p></p>",
        ariaLabel: "Gründe",
        editable: true,
      })
      const editorField = screen.getByTestId("Gründe")
      await fireEvent.focus(editorField.firstElementChild!)

      await clickTableSubButton("Alle Rahmen")
      expect(screen.getByText(WARNING_TEXT)).toBeInTheDocument()

      await insertTable()
      const firstCell =
        editorField.querySelector("th") || editorField.querySelector("td")
      await userEvent.click(firstCell!)

      await flushPromises() // Warten auf Vue-Reaktivität und onSelectionUpdate

      expect(screen.queryByText(WARNING_TEXT)).not.toBeInTheDocument()
    })
  })
})
