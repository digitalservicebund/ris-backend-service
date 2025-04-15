import { longTextLabels, shortTextLabels } from "@/domain/documentUnit"

const germanLabels: Record<string, string | undefined> = {
  ...longTextLabels,
  ...shortTextLabels,
}

export function getCategoryLabel(
  category: string | undefined,
): string | undefined {
  if (category) {
    return germanLabels[category]
  }
  return undefined
}
