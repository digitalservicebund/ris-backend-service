import { setActivePinia, createPinia } from "pinia"
import {
  GlobalValidationError,
  useGlobalValidationErrorStore,
} from "@/stores/useGlobalValidationErrorStore"

describe("validationErrorStore", () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it("is empty on initialization", () => {
    const store = useGlobalValidationErrorStore()
    expect(store.validationErrors).toEqual([])
  })

  it("adds all errors", () => {
    const store = useGlobalValidationErrorStore()
    const error1: GlobalValidationError = {
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "foo/bar",
    }

    const error2: GlobalValidationError = {
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "foo/die",
    }

    store.add(error1, error2)

    const error3: GlobalValidationError = {
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "other",
    }

    store.add(error3)

    expect(store.validationErrors).toEqual([error1, error2, error3])
    expect(store.getByScope("foo").value).toEqual([error1, error2])
    expect(store.getByInstance("foo/bar").value).toEqual([error1])
    expect(store.getByInstance("foo/die").value).toEqual([error2])
  })

  it("retrieve errors after adding single errors by instance", () => {
    const store = useGlobalValidationErrorStore()
    const error: GlobalValidationError = {
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "norms/1234/title",
    }

    store.add(error)

    store.add({
      code: "ERROR_CODE_OTHER",
      message: "Some other error message",
      instance: "norms/1234/sub",
    })

    expect(store.getByInstance("norms/1234/title").value).toEqual([error])
    expect(store.getByInstance("otherInstance").value).toEqual([])
  })

  it("retrieve errors by scope", () => {
    const store = useGlobalValidationErrorStore()
    const fooBar: GlobalValidationError = {
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "foo/bar",
    }

    store.add(fooBar)

    const fooBarBaz: GlobalValidationError = {
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "foo/bar/baz",
    }

    store.add(fooBarBaz)

    // instance 'foobar' should not be in the same scope as 'foo' (same prefix)
    const foobar: GlobalValidationError = {
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "foobar",
    }

    store.add(foobar)

    expect(store.getByScope("foo").value).toEqual([fooBar, fooBarBaz])
    expect(store.getByScope("foobar").value).toEqual([foobar])
  })

  it("retrieve children", () => {
    const store = useGlobalValidationErrorStore()
    const fooBar: GlobalValidationError = {
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "foo/bar",
    }

    store.add(fooBar)

    const fooBarBaz: GlobalValidationError = {
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "foo/bar/baz",
    }

    store.add(fooBarBaz)

    // instance 'foobar' should not be in the same scope as 'foo' (same prefix)
    const foobar: GlobalValidationError = {
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "foobar",
    }

    store.add(foobar)

    expect(store.children("foo/bar").value).toEqual([fooBarBaz])
  })

  it("remove errors by scope", () => {
    const store = useGlobalValidationErrorStore()

    store.add({
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "norms",
    })

    store.add({
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "norms/1234",
    })

    store.add({
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "norms/1234/title",
    })

    store.add({
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "norms/1234/sub",
    })

    const unrelatedError: GlobalValidationError = {
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "norms/9999/title",
    }

    store.add(unrelatedError)

    store.removeByScope("norms/1234")

    expect(store.getByScope("norms/1234").value).toEqual([])
    expect(store.getByScope("norms/9999").value).toEqual([unrelatedError])
  })

  it("reset removes all entries", () => {
    const store = useGlobalValidationErrorStore()

    store.add({
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "norms/1234/title",
    })

    store.add({
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "norms/1234/sub",
    })

    store.add({
      code: "ERROR_CODE",
      message: "Some error message",
      instance: "norms/9999/title",
    })

    store.reset()

    expect(store.validationErrors).toEqual([])
    expect(store.getByScope("norms").value).toEqual([])
    expect(store.getByInstance("norms/1234/title").value).toEqual([])
  })
})
