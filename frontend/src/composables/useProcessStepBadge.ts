import { computed } from "vue"
import ProcessStep from "@/domain/processStep"

export interface StepBadge {
  borderColor: string
  backgroundColor: string
  class: string
  label: string
}

export function useProcessStepBadge(step: ProcessStep) {
  const badge: StepBadge = {
    borderColor: "black",
    backgroundColor: "white",
    class: "px-8",
    label: step.name,
  }

  return computed(() => {
    if (!step) return badge

    switch (step.name) {
      case "Neu":
        badge.backgroundColor = "bg-white"
        badge.borderColor = "border-gray-800"
        break
      case "Dokumentationswürdigkeit":
        badge.backgroundColor = "bg-gray-400"
        badge.borderColor = "border-gray-800"
        break
      case "Ersterfassung":
        badge.backgroundColor = "bg-gray-300"
        badge.borderColor = "border-gray-800"
        break
      case "Fachdokumentation":
        badge.backgroundColor = "bg-gray-100"
        badge.borderColor = "border-gray-800"
        break
      case "QS formal":
        badge.backgroundColor = "bg-blue-300"
        badge.borderColor = "border-blue-800"
        break
      case "QS fachlich":
        badge.backgroundColor = "bg-blue-400"
        badge.borderColor = "border-blue-800"
        break
      case "Blockiert":
        badge.backgroundColor = "bg-red-200"
        badge.borderColor = "border-red-600"
        break
      case "Terminiert":
        badge.backgroundColor = "bg-green-200"
        badge.borderColor = "border-green-800"
        break
      case "Abgabe":
        badge.backgroundColor = "bg-green-100"
        badge.borderColor = "border-green-800"
        break
      case "Wiedervorlage":
        badge.backgroundColor = "bg-yellow-200"
        badge.borderColor = "border-yellow-900"
        break
      case "Fertig":
        badge.backgroundColor = "bg-green-300"
        badge.borderColor = "border-green-800"
        break
      case "Fundstelle":
        badge.backgroundColor = "bg-blue-100"
        badge.borderColor = "border-blue-800"
        break
    }
    return badge
  })
}
