import { InjectionKey } from "vue"

export type PreviewLayout = "wide" | "narrow"
export const previewLayoutInjectionKey = Symbol() as InjectionKey<PreviewLayout>
