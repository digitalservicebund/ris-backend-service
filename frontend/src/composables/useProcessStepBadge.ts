import { computed } from "vue"
import ProcessStep from "@/domain/processStep"

const stepConfigMap: { [key: string]: { bg: string; border: string } } = {
  Neu: { bg: "bg-white", border: "border-gray-800" },
  Dokumentationswürdigkeit: { bg: "bg-gray-400", border: "border-gray-800" },
  Ersterfassung: { bg: "bg-gray-300", border: "border-gray-800" },
  Fachdokumentation: { bg: "bg-gray-100", border: "border-gray-800" },
  "QS formal": { bg: "bg-blue-300", border: "border-blue-800" },
  "QS fachlich": { bg: "bg-blue-400", border: "border-blue-800" },
  Blockiert: { bg: "bg-red-200", border: "border-red-600" },
  Terminiert: { bg: "bg-green-200", border: "border-green-800" },
  Abgabe: { bg: "bg-green-100", border: "border-green-800" },
  Wiedervorlage: { bg: "bg-yellow-200", border: "border-yellow-900" },
  Fertig: { bg: "bg-green-300", border: "border-green-800" },
  Fundstelle: { bg: "bg-blue-100", border: "border-blue-800" },
}

const defaultStyles = {
  bg: "bg-white",
  border: "border-black",
}

export function useProcessStepBadge(step: ProcessStep) {
  return computed(() => {
    const styles = step?.name
      ? stepConfigMap[step.name] || defaultStyles
      : defaultStyles

    return {
      borderColor: styles.border,
      backgroundColor: styles.bg,
      class: "px-8",
      label: step?.name || "",
    }
  })
}
