import { InputField, InputType } from "@/shared/components/input/types"

export const euAnnouncement: InputField[] = [
  {
    name: "euAnnouncementGazette",
    type: InputType.TEXT,
    label: "Amtsblatt der EU",
    inputAttributes: {
      ariaLabel: "Amtsblatt der EU",
    },
  },
  {
    name: "euAnnouncementYear",
    type: InputType.TEXT,
    label: "Jahresangabe",
    inputAttributes: {
      ariaLabel: "Jahresangabe",
    },
  },
  {
    name: "euAnnouncementSeries",
    type: InputType.TEXT,
    label: "Reihe",
    inputAttributes: {
      ariaLabel: "Reihe",
    },
  },
  {
    name: "euAnnouncementNumber",
    type: InputType.TEXT,
    label: "Nummer des Amtsblatts",
    inputAttributes: {
      ariaLabel: "Nummer des Amtsblatts",
    },
  },
  {
    name: "euAnnouncementPage",
    type: InputType.TEXT,
    label: "Seitenzahl",
    inputAttributes: {
      ariaLabel: "Seitenzahl",
    },
  },
  {
    name: "euAnnouncementInfo",
    type: InputType.TEXT,
    label: "Zusatzangaben",
    inputAttributes: {
      ariaLabel: "Zusatzangaben",
    },
  },
  {
    name: "euAnnouncementExplanations",
    type: InputType.TEXT,
    label: "Erläuterungen",
    inputAttributes: {
      ariaLabel: "Erläuterungen",
    },
  },
]
