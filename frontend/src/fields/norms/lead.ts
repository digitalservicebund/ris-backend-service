import { InputField, InputType } from "@/shared/components/input/types"

export const lead: InputField[] = [
  {
    name: "leadJurisdiction",
    id: "leadJurisdiction",
    type: InputType.TEXT,
    label: "Ressort",
    inputAttributes: {
      ariaLabel: "Ressort",
    },
  },
  {
    name: "leadUnit",
    id: "leadUnit",
    type: InputType.TEXT,
    label: "Organisationseinheit",
    inputAttributes: {
      ariaLabel: "Organisationseinheit",
    },
  },
]
