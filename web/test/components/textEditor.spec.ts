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
        name: "jurisdiction-docUnit-:documentNumber-categories#coreData",
        component: {},
      },
    ],
  })

  test("renders text editor with default props", async () => {
    const { queryByLabelText, container } = render(TextEditor, {
      props: {},
      global: { plugins: [vuetify, router] },
    })

    expect(container.getElementsByClassName("ProseMirror__small").length).toBe(
      1
    )
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
          editable: false,
          ariaLabel: "test label",
        },
        global: { plugins: [vuetify, router] },
      }
    )

    queryByText("Test Value")
    expect(container.getElementsByClassName("ProseMirror__large").length).toBe(
      1
    )
    expect(
      queryByLabelText("test label Editor Button Leiste")
    ).not.toBeInTheDocument()
    getByLabelText("test label Editor Feld")
  })

  test("show buttons on focus", async () => {
    const { getByLabelText } = render(TextEditor, {
      props: { value: "Test Value", ariaLabel: "test label" },
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
})
