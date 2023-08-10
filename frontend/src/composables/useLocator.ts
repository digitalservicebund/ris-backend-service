import {
  ComputedRef,
  InjectionKey,
  MaybeRefOrGetter,
  Ref,
  computed,
  inject,
  provide,
  ref,
  toValue,
  watch,
} from "vue"

const LocatorProvider: InjectionKey<{
  segments: Ref<string[]>
}> = Symbol()

export function useLocator() {
  // Inherit previous locators or start a new locator
  const provider = inject(LocatorProvider, {
    segments: ref([]),
  })

  /**
   * Any segments we've added in the current usage of `useLocator`. This is an
   * array of arrays because `addSegmnet` can be called multiple times in the
   * same component. And because the segments can be reactive (and therefore
   * change), we need to keep track of them individually so we can also update
   * them individually.
   */
  const localSegments = ref<string[][]>([])

  /**
   * Merges all local segments as well as all segments we've inherited from
   * previous usage of `useLocator`.
   */
  const combinedSegments = computed(() => [
    ...provider.segments.value,
    ...localSegments.value.flat(),
  ])

  /**
   * Appends a list of segments to the locator. They will be part of the
   * locator in the current component, as well as all its children. The segments
   * can be either static strings, or reactive values, so the following are all
   * valid:
   *
   * - `["foo", "bar"]`
   * - `ref(["foo", "bar"])`
   * - `() => ["foo", "bar"]`
   */
  function addSegment(segments: MaybeRefOrGetter<string[]>): void {
    const insertAt = localSegments.value.length

    watch(
      () => toValue(segments),
      (val) => {
        localSegments.value[insertAt] = [...val]
      },
      { immediate: true },
    )
  }

  /**
   * Returns the current locator. If you pass in an array of segments, they will
   * be appended to the locator just like `addSegment` would. The difference is
   * that the segments will only be part of the locator returned by this function,
   * and not affect any other locators in this component or its children.
   *
   * @see {@link addSegment} for more information about the segments
   */
  function getLocator(
    leaf: MaybeRefOrGetter<string[]> = [],
  ): ComputedRef<string> {
    return computed(() =>
      [...combinedSegments.value, ...toValue(leaf)].join("/"),
    )
  }

  // Overwrite the inherited locator with the new one
  provide(LocatorProvider, { segments: combinedSegments })

  return { addSegment, getLocator }
}
