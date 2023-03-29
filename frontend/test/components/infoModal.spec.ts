import { render, screen } from "@testing-library/vue"
import { InfoStatus } from "@/shared/components/enumInfoStatus"
import InfoModal from "@/shared/components/InfoModal.vue"

describe("InfoModal", () => {
  const SUCCEED_ICON_TEXT = "done"
  const ERROR_ICON_TEXT = "error"
  test("renders infomodal: error", () => {
    render(InfoModal, {
      props: {
        title: "foo",
        description: "bar",
      },
    })

    screen.getByText("foo")
    screen.getByText("bar")
    const icon = screen.getByLabelText("foo icon")
    expect(icon).toHaveTextContent(ERROR_ICON_TEXT)
  })

  test("renders infomodal: succeed", () => {
    render(InfoModal, {
      props: {
        title: "foo",
        description: "bar",
        status: InfoStatus.SUCCEED,
      },
    })

    screen.getByText("foo")
    screen.getByText("bar")
    const icon = screen.getByLabelText("foo icon")
    expect(icon).toHaveTextContent(SUCCEED_ICON_TEXT)
  })
})
