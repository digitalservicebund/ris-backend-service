import { useValidBorderNumbers } from "@/composables/useValidBorderNumbers"

describe("useValidBorderNumbers", () => {
  it("add valid attribute true when border number is valid", () => {
    const data = useValidBorderNumbers(
      {
        guidingPrinciple:
          'lorem ipsum <border-number-link nr="1"></border-number-link>',
      },
      ["1"],
    )
    const value = data.filter((it) => it.value)[0].value
    expect(value).toEqual(
      'lorem ipsum <border-number-link nr="1" valid="true"></border-number-link>',
    )
  })

  it("add valid attribute false when border number is invalid", () => {
    const data = useValidBorderNumbers(
      {
        guidingPrinciple:
          'lorem ipsum <border-number-link nr="2"></border-number-link>',
      },
      ["1"],
    )
    const value = data.filter((it) => it.value)[0].value
    expect(value).toEqual(
      'lorem ipsum <border-number-link nr="2" valid="false"></border-number-link>',
    )
  })
})
