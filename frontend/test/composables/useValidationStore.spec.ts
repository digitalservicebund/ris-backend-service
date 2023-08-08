import { useValidationStore } from "@/composables/useValidationStore"

describe("useValidationStore", () => {
  it("adds keeps onlye one validation for each field", () => {
    const store = useValidationStore<"field1" | "field2">()

    store.add("error1", "field1")
    store.add("error2", "field1")
    store.add("error3", "field1")

    expect(store.getByField("field1")?.message).toBe("error3")
  })

  it("keeps multiple errors for different fields", () => {
    const store = useValidationStore<"field1" | "field2">()

    store.add("error1", "field1")
    store.add("error2", "field2")

    expect(store.getByField("field1")?.message).toBe("error1")
    expect(store.getByField("field2")?.message).toBe("error2")
  })

  it("does not throw when removing from empty", () => {
    const store = useValidationStore<"field1" | "field2">()

    store.remove("field1")
  })

  it("gets validation by message", () => {
    const store = useValidationStore<"field1" | "field2">()

    store.add("Inhalt nicht valide", "field1")
    store.add("Pflichtfeld", "field2")

    expect(store.getByMessage("Inhalt nicht valide")).toHaveLength(1)
  })
})
