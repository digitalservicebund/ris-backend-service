import { computed } from "vue"
import ProcessStep from "@/domain/processStep"

export interface StepBadge {
  borderColor: string
  backgroundColor: string
}

export function useProcessStepBadge(step: ProcessStep) {
  const badge: StepBadge = {
    borderColor: "black",
    backgroundColor: "white",
  }

  return computed(() => {
    if (!step) return badge

    switch (step.name) {
      case "Neu":
        badge.backgroundColor = "white"
        badge.borderColor = "gray-800"
        break
      case "Dokumentationswürdigkeit":
        badge.backgroundColor = "gray-400"
        badge.borderColor = "gray-800"
        break
      case "Ersterfassung":
        badge.backgroundColor = "gray-300"
        badge.borderColor = "gray-800"
        break
      case "Fachdokumentation":
        badge.backgroundColor = "gray-100"
        badge.borderColor = "gray-800"
        break
      case "QS formal":
        badge.backgroundColor = "blue-200"
        badge.borderColor = "blue-800"
        break
      case "QS fachlich":
        badge.backgroundColor = "blue-400"
        badge.borderColor = "blue-800"
        break
      case "Blockiert":
        badge.backgroundColor = "red-200"
        badge.borderColor = "red-600"
        break
      case "Terminiert":
      case "Abgabe":
        badge.backgroundColor = "green-100"
        badge.borderColor = "green-800"
        break
      case "Wiedervorlage":
        badge.backgroundColor = "yellow-200"
        badge.borderColor = "yellow-900"
        break
      case "Fertig":
        badge.backgroundColor = "green-300"
        badge.borderColor = "green-800"
        break
    }
    return badge
  })
}
