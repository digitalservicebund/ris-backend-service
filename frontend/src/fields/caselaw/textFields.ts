function defineTextEntry(name: string, label: string) {
  return { name, label }
}
export const texts = [
  defineTextEntry("decisionName", "Entscheidungsname"),
  defineTextEntry("headline", "Titelzeile"),
  defineTextEntry("guidingPrinciple", "Leitsatz"),
  defineTextEntry("headnote", "Orientierungssatz"),
  defineTextEntry("tenor", "Tenor"),
  defineTextEntry("reasons", "Gründe"),
  defineTextEntry("caseFacts", "Tatbestand"),
  defineTextEntry("decisionReasons", "Entscheidungsgründe"),
]
