import { ref, watch } from "vue"
import { useRouter, useRoute } from "vue-router"

export type Query<T extends string> = { [key in T]?: string }

export default function useQueries<T extends string>(
  searchCallback: (page: number, query: Query<T>) => Promise<void>,
) {
  const route = useRoute()
  const router = useRouter()

  function getQueriesFromRoute(): Query<T> {
    const query: Partial<Query<T>> = {}

    for (const parameter in route.query) {
      query[parameter as T] = route.query[parameter] as string
    }

    return query
  }

  const query = ref<Query<T>>(getQueriesFromRoute())

  const debouncedRouterPush = (() => {
    let timeoutId: number | null = null

    return (currentQuerry: Query<T>) => {
      if (timeoutId !== null) window.clearTimeout(timeoutId)

      timeoutId = window.setTimeout(
        async () =>
          await router.push(
            Object.values(currentQuerry).some((value) => value != "")
              ? { query: currentQuerry }
              : {},
          ),
        300,
      )
    }
  })()

  watch(
    query,
    async () => {
      await searchCallback(0, query.value as Query<T>)
      debouncedRouterPush(query.value as Query<T>)
    },
    { deep: true },
  )

  watch(route, () => ((query.value as Query<T>) = getQueriesFromRoute()))

  return query
}
