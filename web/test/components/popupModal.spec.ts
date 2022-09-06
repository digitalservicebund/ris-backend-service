import { fireEvent, render, screen } from "@testing-library/vue"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import PopupModal from "../../src/components/PopupModal.vue"

const MODAL_HEADER_TEXT = "Dokumentationseinheit löschen"
const MODAL_CONTENT_TEXT = "Möchten Sie die Datei löschen?"
const MODAL_CONFIRM_BTN_TEXT = "Löschen"
const MODAL_CANCEL_BTN_TEXT = "Abbrechen"
describe("popup modal", () => {
  const vuetify = createVuetify({ components, directives })

  test("popup modal should be rendered without error", async () => {
    const { getByText } = render(PopupModal, {
      props: {
        headerText: MODAL_HEADER_TEXT,
        contentText: MODAL_CONTENT_TEXT,
        confirmText: MODAL_CONFIRM_BTN_TEXT,
      },
      global: { plugins: [vuetify] },
    })
    getByText(MODAL_HEADER_TEXT)
    getByText(MODAL_CONTENT_TEXT)
    getByText(MODAL_CONFIRM_BTN_TEXT)
    getByText(MODAL_CANCEL_BTN_TEXT)
  })

  test("popup modal closed by pressing Abbrechen button", async () => {
    const { emitted } = render(PopupModal, {
      props: {
        contentText: MODAL_CONTENT_TEXT,
        confirmText: MODAL_CONFIRM_BTN_TEXT,
      },
      global: { plugins: [vuetify] },
    })
    await fireEvent.click(screen.getByText(MODAL_CANCEL_BTN_TEXT))
    expect(emitted().closeModal).toBeTruthy()
  })

  test("popup modal closed by pressing escape button", async () => {
    const { emitted, getByText } = render(PopupModal, {
      props: {
        headerText: MODAL_HEADER_TEXT,
        contentText: MODAL_CONTENT_TEXT,
        confirmText: MODAL_CONFIRM_BTN_TEXT,
      },
      global: { plugins: [vuetify] },
    })
    await fireEvent.keyDown(getByText(MODAL_HEADER_TEXT), {
      key: "Escape",
      code: "Escape",
      keyCode: 27,
      charCode: 27,
    })
    expect(emitted().closeModal).toBeTruthy()
  })

  test("popup modal emitted confirm action event", async () => {
    const { emitted } = render(PopupModal, {
      props: {
        contentText: MODAL_CONTENT_TEXT,
        confirmText: MODAL_CONFIRM_BTN_TEXT,
      },
      global: { plugins: [vuetify] },
    })
    await fireEvent.click(screen.getByText(MODAL_CONFIRM_BTN_TEXT))
    expect(emitted().confirmAction).toBeTruthy()
  })
})
