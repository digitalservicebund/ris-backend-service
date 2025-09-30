import { describe, it, expect } from "vitest"
import { ref } from "vue"
import { useProcessStepBadge } from "../composables/useProcessStepBadge"
import ProcessStep from "@/domain/processStep"

describe("useProcessStepBadge", () => {
  const createStep = (name: string): ProcessStep => ({
    name,
    uuid: "123",
    abbreviation: name.substring(0, 2).toUpperCase(),
  })

  it('should return correct styling for "Fundstelle" (FS)', () => {
    const stepRef = ref(createStep("Fundstelle"))
    const { name } = stepRef.value

    const badge = useProcessStepBadge(stepRef.value)

    expect(badge.value.label).toBe(name)
    expect(badge.value.backgroundColor).toBe("bg-blue-100")
    expect(badge.value.borderColor).toBe("border-blue-800")
  })

  it('should return correct styling for "QS formal"', () => {
    const stepRef = ref(createStep("QS formal"))
    const { name } = stepRef.value
    const badge = useProcessStepBadge(stepRef.value)

    expect(badge.value.label).toBe(name)
    expect(badge.value.backgroundColor).toBe("bg-blue-300")
    expect(badge.value.borderColor).toBe("border-blue-800")
  })

  it('should return correct styling for "Blockiert"', () => {
    const stepRef = ref(createStep("Blockiert"))
    const { name } = stepRef.value
    const badge = useProcessStepBadge(stepRef.value)

    expect(badge.value.label).toBe(name)
    expect(badge.value.backgroundColor).toBe("bg-red-200")
    expect(badge.value.borderColor).toBe("border-red-600")
  })

  it("should return default styling for an unknown step name", () => {
    const stepRef = ref(createStep("Unknown Step Name"))
    const { name } = stepRef.value
    const badge = useProcessStepBadge(stepRef.value)

    expect(badge.value.label).toBe(name)
    // default styles
    expect(badge.value.backgroundColor).toBe("bg-white")
    expect(badge.value.borderColor).toBe("border-gray-800")
  })
})
