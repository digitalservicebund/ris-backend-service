/* eslint-disable testing-library/no-node-access */
import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen, waitFor } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import { beforeAll, vi } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import TextEditor from "@/components/input/TextEditor.vue"
import { clickOrderedListSubButton } from "~/test-helper/listUtils"
import { mockDocumentForProsemirror } from "~/test-helper/prosemirror-document-mock"
import {
  clickTableBorderSubButton,
  clickTableCellAlignmentSubButton,
  getFirstCellHTML,
  insertTable,
  getOrderedListHTML,
  getOrderedListType,
  hasOrderedList,
} from "~/test-helper/tableUtil"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"
import routes from "~pages"

const DEFAULT_BORDER_STYLE = "1px solid black"

beforeAll(() => {
  mockDocumentForProsemirror()
  useFeatureToggleServiceMock()

  vi.mock("@/composables/useInternalUser", () => {
    return {
      useInternalUser: () => true,
    }
  })
})

describe("text editor toolbar", async () => {
  const renderComponent = async () => {
    userEvent.setup()
    render(TextEditor, {
      props: {
        value: "Test Value",
        ariaLabel: "Gründe",
        editable: true,
      },
      global: { plugins: [router, createTestingPinia()] },
    })

    await flushPromises()
  }

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })

  describe("keyboard navigation", () => {
    test("shift tab in text editor should focus first button in menu", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await userEvent.tab({ shift: true })
      const firstButton = screen.getByLabelText("Erweitern")
      expect(firstButton).toHaveFocus()
    })

    test("arrow right should move focus to next button until last button", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await userEvent.tab({ shift: true })
      const firstButton = screen.getByLabelText("Erweitern")
      expect(firstButton).toHaveFocus()

      await userEvent.keyboard("{ArrowRight}")
      const secondButton = screen.getByLabelText("Nicht-druckbare Zeichen")
      expect(secondButton).toHaveFocus()

      // navigate to last button (arrow right 25 times)
      await userEvent.keyboard("{ArrowRight>25/}")
      const lastButton = screen.getByLabelText("Wiederherstellen")
      expect(lastButton).toHaveFocus()

      // go one step further to the right --> should do nothing
      await userEvent.keyboard("{ArrowRight}")
      expect(lastButton).toHaveFocus()

      // navigate to the left --> should go to previous button
      await userEvent.keyboard("{ArrowLeft}")
      const secondLastButton = screen.getByLabelText("Rückgängig machen")
      expect(secondLastButton).toHaveFocus()
    })

    test("arrow left should leave focus on first button", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await userEvent.tab({ shift: true })
      const firstButton = screen.getByLabelText("Erweitern")
      expect(firstButton).toHaveFocus()

      // When the first button is focused, ArrowLeft does not move focus
      await userEvent.keyboard("{ArrowLeft>5}")
      expect(firstButton).toHaveFocus()

      // From the first button you can move immediately to the next one
      await userEvent.keyboard("{ArrowRight}")
      const secondButton = screen.getByLabelText("Nicht-druckbare Zeichen")
      expect(secondButton).toHaveFocus()

      await userEvent.keyboard("{ArrowLeft}")
      expect(firstButton).toHaveFocus()
    })

    test("enter should jump back to the editor input", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      await userEvent.tab({ shift: true })
      await userEvent.keyboard("{ArrowRight}")
      await userEvent.keyboard("{ArrowRight}")
      const thirdButton = screen.getByLabelText("Fett")
      expect(thirdButton).toHaveFocus()

      // When clicking enter on a text edit button, the focus moves to the editor
      await userEvent.keyboard("{Enter}")
      await waitFor(() => expect(editorField.firstElementChild).toHaveFocus(), {
        timeout: 100,
      })
    })

    test("tab into the editor should skip the menu tool bar", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      // Add external input field to be focused first
      const inputField = editorField.ownerDocument.createElement("input")
      editorField.ownerDocument.body.prepend(inputField)
      await userEvent.click(inputField)

      // Tab skips the menu toolbar and focuses the editor content directly
      await userEvent.tab()
      expect(editorField.firstElementChild).toHaveFocus()

      // ProseMirror needs to be focused explicitly as it does not trigger button enabling otherwise in the unit test
      await userEvent.click(editorField.firstElementChild!)

      // Tab back focuses the toolbar buttons
      await userEvent.tab({ shift: true })
      const firstButton = screen.getByLabelText("Erweitern")
      expect(firstButton).toHaveFocus()
    })
  })

  describe("border number options", () => {
    test("should display border number options", async () => {
      await renderComponent()
      await router.push({
        name: "caselaw-documentUnit-documentNumber-categories",
        params: { documentNumber: "documentNumber" },
      })
      expect(
        screen.getByLabelText("Randnummern neu erstellen"),
      ).toBeInTheDocument()
      expect(screen.getByLabelText("Randnummern entfernen")).toBeInTheDocument()
    })

    test("should hide border number options for pending proceedings", async () => {
      await renderComponent()
      await router.push({
        name: "caselaw-pending-proceeding-documentNumber-categories",
        params: { documentNumber: "documentNumber" },
      })
      expect(
        screen.queryByLabelText("Randnummern neu erstellen"),
      ).not.toBeInTheDocument()
      expect(
        screen.queryByLabelText("Randnummern entfernen"),
      ).not.toBeInTheDocument()
    })
  })

  describe("table border commands", () => {
    test("should set borderTop attribute on single cell when 'Rahmen oben' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await insertTable()
      await clickTableBorderSubButton("Rahmen oben")
      const cellStyle = getFirstCellHTML()

      expect(cellStyle).toContain(`border-top: ${DEFAULT_BORDER_STYLE}`)
      expect(cellStyle).not.toContain(`border-left:`)
    })

    test("should set all four border attributes on cell when 'Rahmen' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await insertTable()
      await clickTableBorderSubButton("Alle Rahmen")
      const cellStyle = getFirstCellHTML()

      expect(cellStyle).toContain(`border-top: ${DEFAULT_BORDER_STYLE}`)
      expect(cellStyle).toContain(`border-right: ${DEFAULT_BORDER_STYLE}`)
      expect(cellStyle).toContain(`border-bottom: ${DEFAULT_BORDER_STYLE}`)
      expect(cellStyle).toContain(`border-left: ${DEFAULT_BORDER_STYLE}`)
    })

    test("should clear all border attributes when 'Kein Rahmen' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await insertTable()

      await clickTableBorderSubButton("Kein Rahmen")
      const cellStyle = getFirstCellHTML()

      expect(cellStyle).not.toContain(`border-top`)
      expect(cellStyle).not.toContain(`border-right`)
      expect(cellStyle).not.toContain(`border-bottom`)
      expect(cellStyle).not.toContain(`border-left`)
    })

    test("should set left border attribute when 'Rahmen links' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await insertTable()
      await clickTableBorderSubButton("Rahmen links")
      const cellStyle = getFirstCellHTML()

      expect(cellStyle).toContain(`border-left: ${DEFAULT_BORDER_STYLE}`)
      expect(cellStyle).not.toContain(`border-top:`)
    })

    test("should set right border attribute when 'Rahmen rechts' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await insertTable()
      await clickTableBorderSubButton("Rahmen rechts")
      const cellStyle = getFirstCellHTML()

      expect(cellStyle).toContain(`border-right: ${DEFAULT_BORDER_STYLE}`)
      expect(cellStyle).not.toContain(`border-top:`)
    })

    test("should set bottom border attribute when 'Rahmen unten' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await insertTable()
      await clickTableBorderSubButton("Rahmen unten")
      const cellStyle = getFirstCellHTML()

      expect(cellStyle).toContain(`border-bottom: ${DEFAULT_BORDER_STYLE}`)
      expect(cellStyle).not.toContain(`border-top:`)
    })
  })

  describe("table cell alignemnt commands", () => {
    test("should set vertical-align attribute on single cell when 'Oben ausrichten' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await insertTable()
      await clickTableCellAlignmentSubButton("Oben ausrichten")
      const cellStyle = getFirstCellHTML()

      expect(cellStyle).toContain(`vertical-align: top`)
    })

    test("should set vertical-align attribute on single cell when 'Mittig ausrichten' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await insertTable()
      await clickTableCellAlignmentSubButton("Mittig ausrichten")
      const cellStyle = getFirstCellHTML()

      expect(cellStyle).toContain(`vertical-align: middle`)
    })

    test("should set vertical-align attribute on single cell when 'Unten ausrichten' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await insertTable()
      await clickTableCellAlignmentSubButton("Unten ausrichten")
      const cellStyle = getFirstCellHTML()

      expect(cellStyle).toContain(`vertical-align: bottom`)
    })
  })

  describe("ordered list commands", () => {
    test("should create numeric list when 'Numerisch (1, 2, 3)' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await clickOrderedListSubButton("Numerisch (1, 2, 3)")

      expect(hasOrderedList()).toBe(true)
      const listStyle = getOrderedListHTML()
      const listType = getOrderedListType()

      expect(listStyle).toContain("list-style-type: decimal")
      expect(listType).toBe("1")
    })

    test("should create lowercase Latin list when 'Lateinisch klein (a, b, c)' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await clickOrderedListSubButton("Lateinisch klein (a, b, c)")

      expect(hasOrderedList()).toBe(true)
      const listStyle = getOrderedListHTML()
      const listType = getOrderedListType()

      expect(listStyle).toContain("list-style-type: lower-alpha")
      expect(listType).toBe("a")
    })

    test("should create uppercase Latin list when 'Lateinisch groß (A, B, C)' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await clickOrderedListSubButton("Lateinisch groß (A, B, C)")

      expect(hasOrderedList()).toBe(true)
      const listStyle = getOrderedListHTML()
      const listType = getOrderedListType()

      expect(listStyle).toContain("list-style-type: upper-alpha")
      expect(listType).toBe("A")
    })

    test("should create lowercase Roman list when 'Römisch klein (i, ii, iii)' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await clickOrderedListSubButton("Römisch klein (i, ii, iii)")

      expect(hasOrderedList()).toBe(true)
      const listStyle = getOrderedListHTML()
      const listType = getOrderedListType()

      expect(listStyle).toContain("list-style-type: lower-roman")
      expect(listType).toBe("i")
    })

    test("should create uppercase Roman list when 'Römisch groß (I, II, III)' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await clickOrderedListSubButton("Römisch groß (I, II, III)")

      expect(hasOrderedList()).toBe(true)
      const listStyle = getOrderedListHTML()
      const listType = getOrderedListType()

      expect(listStyle).toContain("list-style-type: upper-roman")
      expect(listType).toBe("I")
    })

    test("should change list style when switching from numeric to lowercase Latin", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()

      await clickOrderedListSubButton("Numerisch (1, 2, 3)")
      expect(getOrderedListType()).toBe("1")

      await clickOrderedListSubButton("Lateinisch klein (a, b, c)")

      expect(hasOrderedList()).toBe(true)
      const listStyle = getOrderedListHTML()
      const listType = getOrderedListType()

      expect(listStyle).toContain("list-style-type: lower-alpha")
      expect(listType).toBe("a")
    })

    test("should remove list when clicking the same list style twice", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()

      await clickOrderedListSubButton("Numerisch (1, 2, 3)")
      expect(hasOrderedList()).toBe(true)

      await clickOrderedListSubButton("Numerisch (1, 2, 3)")

      expect(hasOrderedList()).toBe(false)
    })

    test("should change list style when switching between different Roman numerals", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()

      await clickOrderedListSubButton("Römisch klein (i, ii, iii)")
      expect(getOrderedListType()).toBe("i")

      await clickOrderedListSubButton("Römisch groß (I, II, III)")

      expect(hasOrderedList()).toBe(true)
      const listStyle = getOrderedListHTML()
      const listType = getOrderedListType()

      expect(listStyle).toContain("list-style-type: upper-roman")
      expect(listType).toBe("I")
    })

    test("should create lowercase Greek list when 'Griechisch klein (α, β, γ)' is clicked", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()
      await clickOrderedListSubButton("Griechisch klein (α, β, γ)")

      expect(hasOrderedList()).toBe(true)
      const listStyle = getOrderedListHTML()
      const listType = getOrderedListType()

      expect(listStyle).toContain("list-style-type: lower-greek")
      expect(listType).toBe("α")
    })

    test("should change list style when switching from Latin to Greek", async () => {
      await renderComponent()
      const editorField = screen.getByTestId("Gründe")

      await userEvent.click(editorField.firstElementChild!)
      expect(editorField.firstElementChild).toHaveFocus()

      await clickOrderedListSubButton("Lateinisch klein (a, b, c)")
      expect(getOrderedListType()).toBe("a")

      await clickOrderedListSubButton("Griechisch klein (α, β, γ)")

      expect(hasOrderedList()).toBe(true)
      const listStyle = getOrderedListHTML()
      const listType = getOrderedListType()

      expect(listStyle).toContain("list-style-type: lower-greek")
      expect(listType).toBe("α")
    })
  })
})
