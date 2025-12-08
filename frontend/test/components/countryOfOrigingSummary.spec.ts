import { render, screen } from "@testing-library/vue"
import CountryOfOriginSummary from "@/components/CountryOfOriginSummary.vue"
import CountryOfOrigin from "@/domain/countryOfOrigin"

function renderComponent(countryOfOrigin: CountryOfOrigin) {
  return render(CountryOfOriginSummary, {
    props: {
      data: countryOfOrigin,
    },
  })
}

describe("Countries of Origin summary", () => {
  it("renders summary correctly", async () => {
    renderComponent(
      new CountryOfOrigin({
        id: "ebd262c0-343d-48fe-bdbc-e3d2da391aa1",
        fieldOfLaw: {
          identifier: "AR-01-01-01",
          text: "Verschulden bei Vertragsschluss (culpa in contrahendo)",
          hasChildren: false,
          children: [],
          norms: [],
        },
        country: {
          identifier: "RE-07-DEU",
          text: "Deutschland",
          hasChildren: false,
          children: [],
          norms: [],
        },
      }),
    )
    expect(
      screen.getByText(
        "RE-07-DEU Deutschland, AR-01-01-01 Verschulden bei Vertragsschluss (culpa in contrahendo)",
      ),
    ).toBeVisible()
  })

  it("renders legacy values correctly", async () => {
    renderComponent(
      new CountryOfOrigin({
        id: "49729f24-26a6-4a19-821e-a9554250133d",
        legacyValue: "legacy value",
      }),
    )
    expect(screen.getByText("Altwert")).toBeVisible()
    expect(screen.getByText("legacy value")).toBeVisible()
  })
})
