import { getByText } from "@testing-library/dom"

describe("App", () => {
  const exampleDiv = document.createElement("div")
  exampleDiv.innerHTML = `
  <h1>Hello DigitalService<h1>
  `

  it("shows Hello ds", () => {
    expect(getByText(exampleDiv, "Hello DigitalService")).toBeTruthy()
  })
})
