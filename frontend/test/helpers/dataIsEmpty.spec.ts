import { dataIsEmpty } from "@/helpers/dataIsEmpty"

describe("dataIsEmpty()", () => {
  it("returns true if data is undefined", () => {
    expect(dataIsEmpty(undefined)).toBe(true)
  })

  it("returns true if data is null", () => {
    expect(dataIsEmpty(null)).toBe(true)
  })

  it("returns true if data is the boolean value false", () => {
    expect(dataIsEmpty(false)).toBe(true)
  })

  it("returns false if data is the boolean value true", () => {
    expect(dataIsEmpty(true)).toBe(false)
  })

  it("returns true if data is an empty string", () => {
    expect(dataIsEmpty("")).toBe(true)
  })

  it("returns true if data is a blank string", () => {
    expect(dataIsEmpty("    ")).toBe(true)
  })

  it("returns false if data is a non blank string", () => {
    expect(dataIsEmpty("foo")).toBe(false)
  })

  it("returns false if data is any number", () => {
    expect(dataIsEmpty(-Number.MAX_VALUE)).toBe(false)
    expect(dataIsEmpty(0)).toBe(false)
    expect(dataIsEmpty(+Number.MAX_VALUE)).toBe(false)
  })

  it("returns true if data is an empty array", () => {
    expect(dataIsEmpty([])).toBe(true)
  })

  it("returns true if data is an array of empty values only", () => {
    expect(dataIsEmpty([null, undefined, "", "   ", false])).toBe(true)
  })

  it("returns false if data is an array with any non empty value", () => {
    expect(dataIsEmpty([null, "foo", undefined])).toBe(false)
  })

  it("returns true if data is an empty object", () => {
    expect(dataIsEmpty({})).toBe(true)
  })

  it("returns true if data is an object with empty values only ", () => {
    expect(dataIsEmpty({ foo: null, bar: undefined, baz: "" })).toBe(true)
  })

  it("returns false if data is an object with any non empty value", () => {
    expect(dataIsEmpty({ foo: null, bar: "bar", baz: "" })).toBe(false)
  })

  it("returns true if data is a deeply nestd but empty data structure", () => {
    expect(
      dataIsEmpty({
        foo: null,
        bar: [undefined, { foo: "", bar: [] }],
        baz: { foo: [false, "", {}] },
      }),
    ).toBe(true)
  })
})
