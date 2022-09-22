import { render, screen, fireEvent } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import TextEditor from "../../src/components/TextEditor.vue"

describe("text editor", async () => {
  const vuetify = createVuetify({ components, directives })
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "jurisdiction-documentUnit-:documentNumber-categories#coreData",
        component: {},
      },
    ],
  })

  test("renders text editor with default props", async () => {
    const { queryByLabelText, container } = render(TextEditor, {
      props: {},
      global: { plugins: [vuetify, router] },
    })

    expect(
      container.getElementsByClassName("editor-content--small").length
    ).toBe(1)
    expect(
      queryByLabelText("Testzeile Editor Button Leiste")
    ).not.toBeInTheDocument()
  })

  test("renders text editor with props", async () => {
    const { queryByLabelText, queryByText, getByLabelText, container } = render(
      TextEditor,
      {
        props: {
          value: "Test Value",
          fieldSize: "large",
          ariaLabel: "test label",
        },
        global: { plugins: [vuetify, router] },
      }
    )

    queryByText("Test Value")
    expect(
      container.getElementsByClassName("editor-content--large").length
    ).toBe(1)
    expect(
      queryByLabelText("test label Editor Button Leiste")
    ).not.toBeInTheDocument()
    getByLabelText("test label Editor Feld")
  })

  test("show buttons on focus", async () => {
    const { getByLabelText } = render(TextEditor, {
      props: { value: "Test Value", ariaLabel: "test label", editable: true },
      global: { plugins: [vuetify, router] },
    })
    await screen.findByText("Test Value")
    const editorField = getByLabelText("test label Editor Feld")
    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
    }

    expect(
      getByLabelText("test label Editor Button Leiste")
    ).toBeInTheDocument()
  })

  test("hide buttons on blur", async () => {
    const { getByLabelText, queryByLabelText } = render(TextEditor, {
      props: { value: "Test Value", ariaLabel: "test label" },
      global: { plugins: [vuetify, router] },
    })

    await screen.findByText("Test Value")
    const editorField = getByLabelText("test label Editor Feld")
    if (editorField.firstElementChild !== null) {
      await fireEvent.blur(editorField.firstElementChild)
    }
    expect(
      queryByLabelText("test label Editor Button Leiste")
    ).not.toBeInTheDocument()
  })

  /*
   * The purpose of this test is to ensure that all expected buttons are
   * rendered. Having this test helps us to ensure that we do not accidentally
   * remove any of them.
   * Unfortunately is the logic of the button bar very complex and dependents on
   * the current width of the editor element. Thereby it is very hard, or rather
   * impossible to test this end-to-end as it depends on the surrounding layout.
   * Thereby this is rather an integration test, as the button list is
   * a configuration of the component. The logic of the button bar and how it
   * collapses for certain widths, is a logic for itself that gets tested
   * separetly.
   * The test should be continuosly improved to very that all buttons exist.
   */
  it("shows all necessary editor buttons", async () => {
    const { getByLabelText, findByText, container } = render(TextEditor, {
      props: { value: "Test Value", ariaLabel: "test label", editable: true },
      global: { plugins: [vuetify, router] },
    })
    await findByText("Test Value")
    const editorField = getByLabelText("test label Editor Feld")

    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
    }

    function expectIconToExist(iconName: string) {
      expect(container.innerHTML).match(
        new RegExp(`<i.*class=".*${iconName}.*"`)
      )
    }

    expectIconToExist("undo")
    expectIconToExist("redo")
    expectIconToExist("bold")
    expectIconToExist("italic")
    expectIconToExist("underline")
    expectIconToExist("strike")
    expectIconToExist("format_align_left")
    expectIconToExist("format_align_center")
    expectIconToExist("format_align_right")
    expectIconToExist("format_align_justify")
    expectIconToExist("superscript")
    expectIconToExist("subscript")
    expectIconToExist("format_list_numbered")
    expectIconToExist("format_list_bulleted")
  })
})
