import { InputField, InputType } from "@/domain"

export const ageIndication: InputField[] = [
  {
    name: "ageIndicationStart",
    type: InputType.TEXT,
    label: "Anfang",
    inputAttributes: {
      ariaLabel: "Anfang",
    },
  },
  {
    name: "ageIndicationEnd",
    type: InputType.TEXT,
    label: "Ende",
    inputAttributes: {
      ariaLabel: "Ende",
    },
  },
]
