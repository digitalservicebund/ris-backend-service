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
        badge.backgroundColor = "#CCE3EC"
        badge.borderColor = "#338DA9"
        break
      case "Dokumentationswürdigkeit":
        badge.backgroundColor = "#F5DCD7"
        badge.borderColor = "#D77360"
        break
      case "Ersterfassung":
        badge.backgroundColor = "#FDF3B0"
        badge.borderColor = "#CD5038"
        break
      case "Fachdokumentation":
        badge.backgroundColor = "#FCE4B1"
        badge.borderColor = "#CD5038"
        break
      case "QS formal":
        badge.backgroundColor = "#99CEB7"
        badge.borderColor = "#339D6E"
        break
      case "QS fachlich":
        badge.backgroundColor = "#CCE7DB"
        badge.borderColor = "#339D6E"
        break
      case "Blockiert":
        badge.backgroundColor = "#F2CCD8"
        badge.borderColor = "#D9668A"
        break
      case "Terminiert":
        badge.backgroundColor = "#99CDD1"
        badge.borderColor = "#339AA2"
        break
      case "Abgabe":
        badge.backgroundColor = "#CCEBF7"
        badge.borderColor = "#3392C5"
        break
      case "Wiedervorlage":
        badge.backgroundColor = "#99C9E2"
        badge.borderColor = "#3392C5"
        break
      case "Fertig":
        badge.backgroundColor = "#DADEE2"
        badge.borderColor = "#6F7785"
        break
    }
    return badge
  })
}
