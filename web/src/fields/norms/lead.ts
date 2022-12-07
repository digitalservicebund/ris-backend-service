import { InputField, InputType } from "@/domain"

export const lead: InputField[] = [
  {
    name: "leadJurisdiction",
    type: InputType.TEXT,
    label: "Ressort",
    inputAttributes: {
      ariaLabel: "Ressort",
    },
  },
  {
    name: "leadUnit",
    type: InputType.TEXT,
    label: "Organisationseinheit",
    inputAttributes: {
      ariaLabel: "Organisationseinheit",
    },
  },
]
