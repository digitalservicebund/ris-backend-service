import { describe, it, expect } from "vitest"
import { ref } from "vue"
import { useProcessStepBadge } from "@/composables/useProcessStepBadge"
import ProcessStep from "@/domain/processStep"

describe("useProcessStepBadge", () => {
  const createStep = (name: string): ProcessStep => ({
    name,
    uuid: "test-id-123",
    abbreviation: name.substring(0, 2).toUpperCase(),
  })

  const testCases = [
    { name: "Neu", bg: "bg-white", border: "border-gray-800" },
    {
      name: "Dokumentationswürdigkeit",
      bg: "bg-gray-400",
      border: "border-gray-800",
    },
    { name: "Ersterfassung", bg: "bg-gray-300", border: "border-gray-800" },
    { name: "Fachdokumentation", bg: "bg-gray-100", border: "border-gray-800" },
    { name: "QS formal", bg: "bg-blue-300", border: "border-blue-800" },
    { name: "QS fachlich", bg: "bg-blue-400", border: "border-blue-800" },
    { name: "Blockiert", bg: "bg-red-200", border: "border-red-600" },
    { name: "Terminiert", bg: "bg-green-200", border: "border-green-800" },
    { name: "Abgabe", bg: "bg-green-100", border: "border-green-800" },
    { name: "Wiedervorlage", bg: "bg-yellow-200", border: "border-yellow-900" },
    { name: "Fertig", bg: "bg-green-300", border: "border-green-800" },
    { name: "Fundstelle", bg: "bg-blue-100", border: "border-blue-800" },
    { name: "Unknown Step", bg: "bg-white", border: "border-black" },
  ]

  it.each(testCases)(
    'should return correct styling for step "$name"',
    ({ name, bg, border }) => {
      const stepData = createStep(name)
      const stepRef = ref(stepData)
      const badge = useProcessStepBadge(stepRef.value)

      expect(badge.value.label).toBe(name)
      expect(badge.value.backgroundColor).toBe(bg)
      expect(badge.value.borderColor).toBe(border)
    },
  )
})
