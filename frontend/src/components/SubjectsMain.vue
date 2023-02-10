<script lang="ts" setup>
import SubjectSelectionList from "./SubjectSelectionList.vue"
import SubjectTree from "./SubjectTree.vue"
</script>

<template>
  <div class="bg-gray-100 flex flex-col p-20">
    <h1 class="heading-02-regular mb-[1rem]">Sachgebiete</h1>
    <div class="flex flex-row">
      <div class="bg-white flex-1 p-20">
        <SubjectSelectionList></SubjectSelectionList>
      </div>
      <div class="bg-white flex-1 p-20">
        <SubjectTree></SubjectTree>
      </div>
    </div>
  </div>
</template>
