import { render } from "@testing-library/vue"
import NormsList from "@/components/NormsList.vue"

vi.mock("vue-router")

describe("norms list", () => {
  it("shows the title of each norm", async () => {
    const norms = [
      { officialLongTitle: "first title", guid: "first guid" },
      { officialLongTitle: "second title", guid: "second guid" },
    ]
    const { queryByText } = renderComponent({ norms })

    const firstEntry = queryByText("first title")
    const secondEntry = queryByText("second title")

    expect(firstEntry).toBeVisible()
    expect(secondEntry).toBeVisible()
  })
})

function renderComponent(options?: {
  norms?: { officialLongTitle: string; guid: string }[]
}) {
  const global = { stubs: { routerLink: { template: "<a><slot/></a>" } } }
  const props = { norms: options?.norms ?? [] }
  return render(NormsList, { props, global })
}
