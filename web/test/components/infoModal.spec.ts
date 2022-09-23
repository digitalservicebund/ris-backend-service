import { render } from "@testing-library/vue"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import { createVuetify } from "vuetify/lib/framework.mjs"
import InfoModal from "@/components/InfoModal.vue"
import { InfoStatus } from "@/enum/enumInfoStatus"

describe("InfoModal", () => {
  const SUCCEED_ICON_TEXT = "done"
  const ERROR_ICON_TEXT = "error"
  const ICON_CLASS_NAME = "material-icons"
  test("renders infomodal: error", () => {
    const vuetify = createVuetify({ components, directives })

    const { getByText, container } = render(InfoModal, {
      global: { plugins: [vuetify] },
      props: {
        title: "foo",
        description: "bar",
      },
    })

    getByText("foo")
    getByText("bar")
    const icon = container.getElementsByClassName(ICON_CLASS_NAME)[0]
    expect(icon).toHaveTextContent(ERROR_ICON_TEXT)
  })

  test("renders infomodal: succeed", () => {
    const vuetify = createVuetify({ components, directives })

    const { getByText, container } = render(InfoModal, {
      global: { plugins: [vuetify] },
      props: {
        title: "foo",
        description: "bar",
        status: InfoStatus.SUCCEED,
      },
    })

    getByText("foo")
    getByText("bar")
    const icon = container.getElementsByClassName(ICON_CLASS_NAME)[0]
    expect(icon).toHaveTextContent(SUCCEED_ICON_TEXT)
  })
})
