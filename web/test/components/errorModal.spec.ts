import { render } from "@testing-library/vue"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import { createVuetify } from "vuetify/lib/framework.mjs"
import ErrorModal from "@/components/ErrorModal.vue"

describe("ErrorModal", () => {
  it("renders with title and description", () => {
    const vuetify = createVuetify({ components, directives })

    const { getByText } = render(ErrorModal, {
      global: { plugins: [vuetify] },
      props: {
        title: "foo",
        description: "bar",
      },
    })

    getByText("foo")
    getByText("bar")
  })
})
