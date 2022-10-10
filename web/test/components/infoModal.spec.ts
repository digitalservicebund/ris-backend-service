import { render } from "@testing-library/vue"
import InfoModal from "@/components/InfoModal.vue"
import { InfoStatus } from "@/enum/enumInfoStatus"

describe("InfoModal", () => {
  const SUCCEED_ICON_TEXT = "done"
  const ERROR_ICON_TEXT = "error"
  const ICON_CLASS_NAME = "material-icons"
  test("renders infomodal: error", () => {
    const { getByText, container } = render(InfoModal, {
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
    const { getByText, container } = render(InfoModal, {
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
