import { ref } from 'vue'

interface ToastOptions {
  message: string
  type?: 'success' | 'error' | 'info'
  duration?: number
}

const toasts = ref<ToastOptions[]>([])

export function useToast() {
  const showToast = (options: ToastOptions) => {
    toasts.value.push(options)
  }

  const removeToast = (index: number) => {
    toasts.value.splice(index, 1)
  }

  return {
    toasts,
    showToast,
    removeToast,
    success: (message: string) => showToast({ message, type: 'success' }),
    error: (message: string) => showToast({ message, type: 'error' }),
    info: (message: string) => showToast({ message, type: 'info' }),
  }
}
