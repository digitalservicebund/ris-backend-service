/* eslint-disable testing-library/no-node-access */
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { flushPromises } from "@vue/test-utils"
import { beforeEach } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import TextEditor from "@/components/input/TextEditor.vue"

function getBoundingClientRect() {
  const rec = {
    x: 0,
    y: 0,
    bottom: 0,
    height: 0,
    left: 0,
    right: 0,
    top: 0,
    width: 0,
  }
  return { ...rec, toJSON: () => rec }
}

class FakeDOMRectList extends DOMRect {
  item(index: any) {
    return (this as any)[index]
  }
}

document.elementFromPoint = () => null
HTMLElement.prototype.getBoundingClientRect = getBoundingClientRect
HTMLElement.prototype.getClientRects = () => new FakeDOMRectList() as any
Range.prototype.getBoundingClientRect = getBoundingClientRect
Range.prototype.getClientRects = () => new FakeDOMRectList() as any

describe("text editor", async () => {
  beforeEach(async () => {
    userEvent.setup()
    render(TextEditor, {
      props: {
        value: "Test Value",
        ariaLabel: "Test Editor Feld",
        editable: true,
      },
      global: { plugins: [router] },
    })

    await flushPromises()
  })

  global.ResizeObserver = require("resize-observer-polyfill")
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

  test("shift tab in text editor should focus first button in menu", async () => {
    const editorField = screen.getByTestId("Test Editor Feld")

    await userEvent.click(editorField.firstElementChild!)
    expect(editorField.firstElementChild).toHaveFocus()
    await userEvent.tab({ shift: true })
    const firstButton = screen.getByLabelText("fullview")
    expect(firstButton).toHaveFocus()
  })

  test("arrow right should move focus to next button until last button", async () => {
    const editorField = screen.getByTestId("Test Editor Feld")

    await userEvent.click(editorField.firstElementChild!)
    expect(editorField.firstElementChild).toHaveFocus()
    await userEvent.tab({ shift: true })
    const firstButton = screen.getByLabelText("fullview")
    expect(firstButton).toHaveFocus()

    await userEvent.keyboard("{ArrowRight}")
    const secondButton = screen.getByLabelText("invisible-characters")
    expect(secondButton).toHaveFocus()

    // navigate to last button
    await userEvent.keyboard("{ArrowRight>18/}")
    const lastButton = screen.getByLabelText("redo")
    expect(lastButton).toHaveFocus()

    // go one step further to the right --> should do nothing
    await userEvent.keyboard("{ArrowRight}")
    expect(lastButton).toHaveFocus()

    // navigate to the left --> should go to previous button
    await userEvent.keyboard("{ArrowLeft}")
    const secondLastButton = screen.getByLabelText("undo")
    expect(secondLastButton).toHaveFocus()
  })

  test("arrow left should leave focus on first button", async () => {
    const editorField = screen.getByTestId("Test Editor Feld")

    await userEvent.click(editorField.firstElementChild!)
    expect(editorField.firstElementChild).toHaveFocus()
    await userEvent.tab({ shift: true })
    const firstButton = screen.getByLabelText("fullview")
    expect(firstButton).toHaveFocus()

    await userEvent.keyboard("{ArrowLeft}")
    expect(firstButton).toHaveFocus()
  })
})
