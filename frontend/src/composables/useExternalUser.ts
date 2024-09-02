import useSessionStore from "@/stores/sessionStore"

export function useExternalUser() {
  const session = useSessionStore()
  return session.user?.roles?.includes("External") ?? false
}
