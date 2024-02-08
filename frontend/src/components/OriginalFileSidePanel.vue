<script setup lang="ts">
import { computed, ref, watch } from "vue"
import { useRoute } from "vue-router"
import LoadingSpinner from "@/components/LoadingSpinner.vue"
import TextEditor from "@/components/TextEditor.vue"
import IconArrowForward from "~icons/ic/baseline-arrow-forward"

const props = defineProps<{
  open?: boolean
  hasFile: boolean
  file?: string
}>()

const emit = defineEmits<{
  "update:open": [value: boolean]
}>()

const localOpen = ref(false)

watch(
  () => props.open,
  () => (localOpen.value = props.open ?? false),
  { immediate: true },
)

watch(localOpen, () => emit("update:open", localOpen.value))

const route = useRoute()

const uploadFileRoute = computed(() =>
  route.params?.documentNumber
    ? {
        name: "caselaw-documentUnit-documentNumber-files",
        params: { documentNumber: route.params.documentNumber },
        query: route.query,
      }
    : undefined,
)
</script>

<template>
  <div v-bind="$attrs">
    <div class="sticky top-0 flex w-full flex-col gap-40">
      <div class="flex items-center">
        <h2 class="ds-heading-02-reg grow">Originaldokument</h2>
      </div>

      <div v-if="!hasFile" class="flex flex-col gap-24">
        Es wurde noch kein Originaldokument hochgeladen.

        <router-link
          v-if="uploadFileRoute"
          class="ds-link-01-bold flex items-center gap-2"
          :to="uploadFileRoute"
        >
          <IconArrowForward />
          <span>Zum Upload</span>
        </router-link>
      </div>

      <div v-else-if="!file" class="text-center"><LoadingSpinner /></div>

      <div
        v-else
        class="h-[65vh] overflow-scroll border-1 border-solid border-gray-400"
      >
        <TextEditor element-id="odoc" field-size="max" :value="file" />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.odoc-open {
  display: flex;
  height: 65px;
  align-items: center; // align vertical
  justify-content: center; // align horizontal
  border-radius: 10px;
  margin-right: 40px;
  transform: rotate(-90deg);
  transform-origin: right;
}

.odoc-open-text {
  margin-left: 30px;
}

.odoc-open-icon-background {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: white;
  transform: rotate(90deg) translateY(-25px);
}

.odoc-open-icon {
  margin-top: 8px;
  margin-right: 9px;
}
</style>
