/* eslint-disable testing-library/no-node-access */
import { render, screen, fireEvent } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import TextEditor from "../../src/components/TextEditor.vue"

describe("text editor", async () => {
  global.ResizeObserver = require("resize-observer-polyfill")
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "caselaw-documentUnit-:documentNumber-categories#coreData",
        component: {},
      },
    ],
  })

  test("renders text editor with default props", async () => {
    render(TextEditor, {
      props: {},
      global: { plugins: [router] },
    })

    expect(screen.getAllByLabelText("Editor Feld").length).toBe(1)

    expect(
      screen.queryByLabelText("Editor Feld Button Leiste")
    ).not.toBeInTheDocument()
  })

  test("renders text editor with props", async () => {
    render(TextEditor, {
      props: {
        value: "Test Value",
        fieldSize: "large",
        ariaLabel: "Test Editor Feld",
      },
      global: { plugins: [router] },
    })

    screen.queryByText("Test Value")
    expect(
      screen.queryByLabelText("Test Editor Feld Button Leiste")
    ).not.toBeInTheDocument()
    screen.getByLabelText("Test Editor Feld")
  })

  test("show buttons on focus", async () => {
    render(TextEditor, {
      props: {
        value: "Test Value",
        ariaLabel: "Test Editor Feld",
        editable: true,
      },
      global: { plugins: [router] },
    })
    await screen.findByText("Test Value")

    const editorField = screen.getByLabelText("Test Editor Feld")

    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
    }

    expect(
      screen.getByLabelText("Test Editor Feld Button Leiste")
    ).toBeInTheDocument()
  })

  test("hide buttons on blur", async () => {
    render(TextEditor, {
      props: { value: "Test Value", ariaLabel: "Test Editor Feld" },
      global: { plugins: [router] },
    })

    await screen.findByText("Test Value")

    const editorField = screen.getByLabelText("Test Editor Feld")

    if (editorField.firstElementChild !== null) {
      await fireEvent.blur(editorField.firstElementChild)
    }

    expect(
      screen.queryByLabelText("Test Editor Feld Button Leiste")
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
    render(TextEditor, {
      props: {
        value: "Test Value",
        ariaLabel: "Test Editor Feld",
        editable: true,
      },
      global: { plugins: [router] },
    })
    await screen.findByText("Test Value")

    const editorField = screen.getByLabelText("Test Editor Feld")

    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
    }

    expect(screen.getByText("undo")).toBeInTheDocument()
    expect(screen.getByText("redo")).toBeInTheDocument()
    expect(screen.getByText("format_bold")).toBeInTheDocument()
    expect(screen.getByText("format_italic")).toBeInTheDocument()
    expect(screen.getByText("format_underlined")).toBeInTheDocument()
    expect(screen.getByText("strikethrough_s")).toBeInTheDocument()
    expect(screen.getByText("format_align_left")).toBeInTheDocument()
    expect(screen.getByText("format_align_center")).toBeInTheDocument()
    expect(screen.getByText("format_align_right")).toBeInTheDocument()
    expect(screen.getByText("format_align_justify")).toBeInTheDocument()
    expect(screen.getByText("superscript")).toBeInTheDocument()
    expect(screen.getByText("subscript")).toBeInTheDocument()
    expect(screen.getByText("format_list_numbered")).toBeInTheDocument()
    expect(screen.getByText("format_list_bulleted")).toBeInTheDocument()
    expect(screen.getAllByText("vertical_split")).toHaveLength(2)
    expect(screen.getByText("table_chart")).toBeInTheDocument()
    expect(screen.getByText("123")).toBeInTheDocument()
    expect(screen.getByText("open_in_full")).toBeInTheDocument()
  })
})
