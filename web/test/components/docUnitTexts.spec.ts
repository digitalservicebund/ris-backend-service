import { render } from "@testing-library/vue"
import { describe, test } from "vitest"
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
})
