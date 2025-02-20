import { longTextLabels, shortTextLabels } from "@/domain/documentUnit"

const germanLabels: Record<string, string | undefined> = {
  ...longTextLabels,
  ...shortTextLabels,
}

export function getCategoryLabel(category: string): string | undefined {
  return germanLabels[category]
}
