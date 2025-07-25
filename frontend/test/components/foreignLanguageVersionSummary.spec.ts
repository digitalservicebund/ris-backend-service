import { render, screen } from "@testing-library/vue"
import ForeignLanguageVersionSummary from "@/components/ForeignLanguageVersionSummary.vue"
import ForeignLanguageVersion from "@/domain/foreignLanguageVersion"

function renderComponent() {
  return render(ForeignLanguageVersionSummary, {
    props: {
      data: new ForeignLanguageVersion({
        languageCode: { id: "id", label: "Englisch" },
        link: "https://link-to-tranlsation.en",
      }),
    },
  })
}

describe("Foreign Language Version summary", () => {
  it("renders summary correctly", async () => {
    renderComponent()
    expect(
      await screen.findByText("Englisch: https://link-to-tranlsation.en"),
    ).toBeVisible()
  })
})
