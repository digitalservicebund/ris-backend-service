import { Ref, ref, watch } from "vue"
import { useRouter, useRoute } from "vue-router"

function truncateQuery(query: Query<string>): Query<string> {
  const truncatedQuery: Query<string> = {}

  for (const key in query) {
    if (query[key] != "") truncatedQuery[key] = query[key]
  }
  return truncatedQuery
}

export type Query<T extends string> = { [key in T]?: string }

export default function useQuery<T extends string>(
  searchCallback?: (page: number, query: Query<T>) => Promise<void>,
) {
  const route = useRoute()
  const router = useRouter()

  function getQueriesFromRoute(): Query<T> {
    const query: Query<T> = {}

    for (const parameter in route.query) {
      query[parameter as T] = route.query[parameter] as string
    }

    return query
  }

  const query = ref(getQueriesFromRoute()) as Ref<Query<T>>

  const debouncedRouterPush = (() => {
    let timeoutId: number | null = null

    return (currentQuerry: Query<T>) => {
      if (timeoutId !== null) window.clearTimeout(timeoutId)

      timeoutId = window.setTimeout(
        () => void router.push({ query: truncateQuery(currentQuerry) }),
        300,
      )
    }
  })()

  watch(
    query,
    async () => {
      if (searchCallback) await searchCallback(0, query.value)
      debouncedRouterPush(query.value)
    },
    { deep: true },
  )

  watch(route, () => (query.value = getQueriesFromRoute()))

  return query
}
