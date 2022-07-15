import userEvent from "@testing-library/user-event"
import { render } from "@testing-library/vue"
import { describe, expect, test, vi } from "vitest"
import { h } from "vue"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import DocUnit from "../../src/domain/docUnit"
import DocUnitTexts from "@/components/DocUnitTexts.vue"

describe("Texts", () => {
  const vuetify = createVuetify({ components, directives })

  test("renders all text fields with labels", async () => {
    const { getByText } = render(DocUnitTexts, {
      global: { plugins: [vuetify] },
      props: {
        texts: new DocUnit("foo").texts,
      },
    })

    getByText("Entscheidungsname")
    getByText("Titelzeile")
    getByText("Leitsatz")
    getByText("Orientierungssatz")
    getByText("Tenor")
    getByText("Gründe")
    getByText("Tatbestand")
    getByText("Entscheidungsgründe")
  })

  test("emits update Doc Unit event", async () => {
    const user = userEvent.setup()
    const spy = vi.fn()

    const { getByLabelText } = render(
      h(DocUnitTexts, { onUpdateDocUnit: spy }),
      {
        global: { plugins: [vuetify] },
        props: {
          texts: new DocUnit("foo").texts,
        },
      }
    )

    await user.click(getByLabelText("Kurz- und Langtexte Speichern Button"))
    expect(spy).toHaveBeenCalledTimes(1)
  })

  test("emits updateDocUnit event", async () => {
    const user = userEvent.setup()
    const spy = vi.fn()

    const { getByLabelText } = render(
      h(DocUnitTexts, { onUpdateDocUnit: spy }),
      {
        global: { plugins: [vuetify] },
        props: {
          texts: new DocUnit("foo").texts,
        },
      }
    )

    await user.click(getByLabelText("Kurz- und Langtexte Speichern Button"))
    expect(spy).toHaveBeenCalledTimes(1)
  })
})
