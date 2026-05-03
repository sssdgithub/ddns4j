<script setup lang="ts">
import { useToast } from '@/composables/useToast'
import Toast from './ui/Toast.vue'

const { toasts, removeToast } = useToast()
</script>

<template>
  <div class="fixed top-0 right-0 z-50 p-4 space-y-2 pointer-events-none">
    <Toast
      v-for="(toast, index) in toasts"
      :key="index"
      :message="toast.message"
      :type="toast.type"
      :duration="toast.duration"
      @close="removeToast(index)"
      class="pointer-events-auto"
    />
  </div>
</template>
