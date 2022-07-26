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

  test("show buttons on focus", async () => {
    const { getByLabelText } = render(TextEditor, {
      props: { value: "Test Value", ariaLabel: "Testlabel" },
      global: { plugins: [vuetify, router] },
    })
    await screen.findByText("Test Value")
    const editorField = await getByLabelText("Testlabel Editor Feld")
    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
    }

    expect(getByLabelText("Testlabel Editor Button Leiste")).not.toBeNull()
  })

  test("hide buttons on blur", async () => {
    const { getByLabelText, queryByLabelText } = render(TextEditor, {
      props: { value: "Test Value", ariaLabel: "Testlabel" },
      global: { plugins: [vuetify, router] },
    })

    await screen.findByText("Test Value")
    const editorField = await getByLabelText("Testlabel Editor Feld")
    if (editorField.firstElementChild !== null) {
      await fireEvent.blur(editorField.firstElementChild)
    }
    expect(queryByLabelText("Testlabel Editor Button Leiste")).toBeNull()
  })

  test.skip("copy text align right", async () => {
    const { getByLabelText, emitted } = await render(TextEditor, {
      props: { value: "Test Value", ariaLabel: "Testlabel" },
      global: { plugins: [vuetify, router] },
    })
    const editorField = await getByLabelText("Testlabel Editor Feld")
    expect(
      (screen.getByText("Test Value") as HTMLElement).outerHTML.includes(
        'style="text-align: right"'
      )
    ).not.toBeTruthy()
    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
      await fireEvent.paste(editorField.firstElementChild, {
        clipboardData: {
          getData: () => '<p style="text-align: right"></p>',
        },
      })
      expect(emitted().updateValue).toBeTruthy()
    }
    expect(
      (
        screen.getByText("Test Value", { exact: false }) as HTMLElement
      ).outerHTML.includes('style="text-align: right"')
    ).toBeTruthy()
  })

  test.skip("copy text align left", async () => {
    const { getByLabelText, emitted } = await render(TextEditor, {
      props: { value: "Test Value", ariaLabel: "Testlabel" },
      global: { plugins: [vuetify, router] },
    })
    const editorField = await getByLabelText("Testlabel Editor Feld")
    expect(
      (screen.getByText("Test Value") as HTMLElement).outerHTML.includes(
        'style="text-align: left"'
      )
    ).not.toBeTruthy()
    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
      await fireEvent.paste(editorField.firstElementChild, {
        clipboardData: {
          getData: () => '<p style="text-align: left"></p>',
        },
      })
      expect(emitted().updateValue).toBeTruthy()
    }
    expect(
      (
        screen.getByText("Test Value", { exact: false }) as HTMLElement
      ).outerHTML.includes('style="text-align: left"')
    ).not.toBeTruthy()
  })

  test.skip("copy text align center", async () => {
    const { getByLabelText, emitted } = await render(TextEditor, {
      props: { value: "Test Value", ariaLabel: "Testlabel" },
      global: { plugins: [vuetify, router] },
    })
    const editorField = await getByLabelText("Testlabel Editor Feld")
    expect(
      (screen.getByText("Test Value") as HTMLElement).outerHTML.includes(
        'style="text-align: center"'
      )
    ).not.toBeTruthy()
    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
      await fireEvent.paste(editorField.firstElementChild, {
        clipboardData: {
          getData: () => '<p style="text-align: center"></p>',
        },
      })
      expect(emitted().updateValue).toBeTruthy()
    }
    expect(
      (
        screen.getByText("Test Value", { exact: false }) as HTMLElement
      ).outerHTML.includes('style="text-align: center"')
    ).toBeTruthy()
  })

  test.skip("copy text align justify", async () => {
    const { getByLabelText, emitted } = await render(TextEditor, {
      props: { value: "Test Value", ariaLabel: "Testlabel" },
      global: { plugins: [vuetify, router] },
    })
    const editorField = await getByLabelText("Testlabel Editor Feld")
    expect(
      (screen.getByText("Test Value") as HTMLElement).outerHTML.includes(
        'style="text-align: justify"'
      )
    ).not.toBeTruthy()
    if (editorField.firstElementChild !== null) {
      await fireEvent.focus(editorField.firstElementChild)
      await fireEvent.paste(editorField.firstElementChild, {
        clipboardData: {
          getData: () => '<p style="text-align: justify"></p>',
        },
      })
      expect(emitted().updateValue).toBeTruthy()
    }
    expect(
      (
        screen.getByText("Test Value", { exact: false }) as HTMLElement
      ).outerHTML.includes('style="text-align: justify"')
    ).toBeTruthy()
  })
})
