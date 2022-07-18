import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import TextEditor from "../../src/components/TextEditor.vue"

describe("text editor", () => {
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
    expect(queryByLabelText("Testzeile Editor Button Leiste")).toBeNull()
  })

  test("renders text editor with props", async () => {
    const { queryByLabelText, queryByText, getByLabelText, container } = render(
      TextEditor,
      {
        props: {
          value: "Test Value",
          fieldSize: "large",
          editable: false,
          ariaLabel: "Testlabel",
        },
        global: { plugins: [vuetify, router] },
      }
    )

    queryByText("Test Value")
    expect(container.getElementsByClassName("ProseMirror__large").length).toBe(
      1
    )
    expect(queryByLabelText("Testlabel Editor Button Leiste")).toBeNull()
    getByLabelText("Testlabel Editor Feld")
  })

  test("update value emits event", async () => {
    const { getByLabelText } = render(TextEditor, {
      props: { value: "Test Value", ariaLabel: "Testlabel" },
      global: { plugins: [vuetify, router] },
    })

    const editorField = await getByLabelText("Testlabel Editor Feld")

    await userEvent.click(editorField)

    //TBD: Change the editor value text to emit updateValue
    // expect(emitted().updateValue).toBeTruthy()
  })

  test("show buttons on focus", async () => {
    const { getByLabelText, queryByText } = render(TextEditor, {
      props: { value: "Test Value", ariaLabel: "Testlabel" },
      global: { plugins: [vuetify, router] },
    })

    queryByText("Test Value")

    const editorField = await getByLabelText("Testlabel Editor Feld")

    await userEvent.click(editorField)

    //TBD: The click event should trigger the focus event, which should render the buttons. For some reason this is not working.

    // expect(getByLabelText("Testlabel Editor Button Leiste")).not.toBeNull()
  })

  test("change text attributes via buttons", async () => {
    const { getByLabelText, queryByText } = render(TextEditor, {
      props: { value: "Test Value", ariaLabel: "Testlabel" },
      global: { plugins: [vuetify, router] },
    })

    queryByText("Test Value")

    const editorField = await getByLabelText("Testlabel Editor Feld")
    editorField.focus()

    //TBD: Select the text to make bold. The buttons will only get visible on focus

    // let boldButton = (await getByLabelText("Testlabel Editor Button Leiste")
    //   ?.firstChild) as HTMLElement | null

    // if (boldButton != null) {
    //   boldButton.click()
    // }

    // if (editorField != null) {
    //   expect(await editorField.innerHTML).toBe(
    //     "<p><strong>Test Value</strong></p>"
    //   )
    // }
  })
})
