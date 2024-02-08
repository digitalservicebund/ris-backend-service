import { render, screen } from "@testing-library/vue"
import InfoModal from "@/components/InfoModal.vue"
import { InfoStatus } from "@/components/utils/enumInfoStatus"

describe("InfoModal", () => {
  test("renders infomodal: error", () => {
    render(InfoModal, {
      props: {
        title: "foo",
        description: "bar",
      },
    })

    screen.getByText("foo")
    screen.getByText("bar")
    expect(screen.getByLabelText("foo icon")).toBeInTheDocument()
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
    expect(screen.getByLabelText("foo icon")).toBeInTheDocument()
  })
})
