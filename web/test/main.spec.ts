import { describe, it, expect } from 'vitest'

const exampleDiv = document.createElement("h1")
exampleDiv.innerHTML = "Hello DigitalService"

describe("App", () => {
  it("shows Hello ds", () => {
    expect(exampleDiv.innerHTML).toBe("Hello DigitalService")
  })
})
