import { inject, InjectionKey, provide, ref, Ref } from "vue"

const courtTypeInjectionKey: CourtTypeInjectionKey = Symbol(
  "Injection symbol for selected court type",
)

/**
 * Provides the court type value to child components via injection.
 * This function is intended to be used in Vue composition API setup blocks
 * to provide the court type value to child components.
 *
 * @param courtType The reactive reference to the court type value.
 * @param injectionKey The injection key used to provide the court type value.
 *                     Defaults to `courtTypeInjectionKey`.
 */
export function useProvideCourtType(
  courtType: Ref<string>,
  injectionKey: CourtTypeInjectionKey = courtTypeInjectionKey,
) {
  provide(injectionKey, courtType)
}

/**
 * Provides a reactive reference to the court type obtained via injection.
 * This function is intended to be used in Vue composition API setup blocks
 * to inject and access the court type value provided by a parent component.
 * If the court type value is not provided, an empty string is returned.
 *
 * @param injectionKey The injection key used to retrieve the court type value.
 *                     Defaults to `courtTypeInjectionKey`.
 * @returns A reactive reference to the court type value obtained via injection.
 */
export function useInjectCourtType(
  injectionKey: CourtTypeInjectionKey = courtTypeInjectionKey,
) {
  return inject(injectionKey, ref(""))
}

export type CourtTypeInjectionKey = InjectionKey<Ref<string>>
