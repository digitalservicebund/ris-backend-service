import { InputField, InputType } from "@/shared/components/input/types"

export const euAnnouncement: InputField[] = [
  {
    name: "euAnnouncementGazette",
    id: "euAnnouncementGazette",
    type: InputType.TEXT,
    label: "Amtsblatt der EU",
    inputAttributes: {
      ariaLabel: "Amtsblatt der EU",
    },
  },
  {
    name: "euAnnouncementYear",
    id: "euAnnouncementYear",
    type: InputType.TEXT,
    label: "Jahresangabe",
    inputAttributes: {
      ariaLabel: "Jahresangabe",
    },
  },
  {
    name: "euAnnouncementSeries",
    id: "euAnnouncementSeries",
    type: InputType.TEXT,
    label: "Reihe",
    inputAttributes: {
      ariaLabel: "Reihe",
    },
  },
  {
    name: "euAnnouncementNumber",
    id: "euAnnouncementNumber",
    type: InputType.TEXT,
    label: "Nummer des Amtsblatts",
    inputAttributes: {
      ariaLabel: "Nummer des Amtsblatts",
    },
  },
  {
    name: "euAnnouncementPage",
    id: "euAnnouncementPage",
    type: InputType.TEXT,
    label: "Seitenzahl",
    inputAttributes: {
      ariaLabel: "Seitenzahl",
    },
  },
  {
    name: "euAnnouncementInfo",
    id: "euAnnouncementInfo",
    type: InputType.TEXT,
    label: "Zusatzangaben",
    inputAttributes: {
      ariaLabel: "Zusatzangaben",
    },
  },
  {
    name: "euAnnouncementExplanations",
    id: "euAnnouncementExplanations",
    type: InputType.TEXT,
    label: "Erläuterungen",
    inputAttributes: {
      ariaLabel: "Erläuterungen",
    },
  },
]
