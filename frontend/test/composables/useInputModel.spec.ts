import { nextTick, reactive } from "vue"
import { useInputModel } from "@/composables/useInputModel"

describe("useInputModel", () => {
  it("sets initial input value based on model value property", () => {
    const props = reactive({ modelValue: "foo" })
    const { inputValue } = useInputModel(props, vi.fn())

    expect(inputValue.value).toBe("foo")
  })

  it("sets initial input value based on value property", () => {
    const props = reactive({ value: "bar" })
    const { inputValue } = useInputModel(props, vi.fn())

    expect(inputValue.value).toBe("bar")
  })

  it("model value takes precedence over value property", () => {
    const props = reactive({ value: "bar", modelValue: "foo" })
    const { inputValue } = useInputModel(props, vi.fn())

    expect(inputValue.value).toBe("foo")
  })

  it("updates input value when mode value property changes", async () => {
    const props = reactive({ modelValue: "foo" })
    const { inputValue } = useInputModel(props, vi.fn())
    expect(inputValue.value).toBe("foo")

    props.modelValue = "bar"
    await nextTick()

    expect(inputValue.value).toBe("bar")
  })

  it("updates input value when value property changes", async () => {
    const props = reactive({ value: "foo" })
    const { inputValue } = useInputModel(props, vi.fn())
    expect(inputValue.value).toBe("foo")

    props.value = "bar"
    await nextTick()

    expect(inputValue.value).toBe("bar")
  })

  it("emits model update event when input value changes", async () => {
    const emit = vi.fn()
    const { inputValue } = useInputModel(reactive({}), emit)

    inputValue.value = "new value"
    await nextTick()

    expect(emit).toHaveBeenCalledOnce()
    expect(emit).toHaveBeenLastCalledWith("update:modelValue", "new value")
  })

  it("provides method to emit input events", () => {
    const event = new InputEvent("input")
    const emit = vi.fn()
    const { emitInputEvent } = useInputModel(reactive({}), emit)

    emitInputEvent(event)

    expect(emit).toHaveBeenCalledOnce()
    expect(emit).toHaveBeenLastCalledWith("input", event)
  })
})
