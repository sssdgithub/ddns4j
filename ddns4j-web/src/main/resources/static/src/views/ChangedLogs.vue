<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { changedLogApi } from '@/api/changedLog'
import { useToast } from '@/composables/useToast'
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'

const { error } = useToast()

interface LogEntry {
  timestamp: string
  content: string
}

const logs = ref<LogEntry[]>([])
const loading = ref(false)
const rawContent = ref('')

const fetchLogs = async () => {
  loading.value = true
  try {
    const result = await changedLogApi.getLogs()
    rawContent.value = result
    
    // Parse the log content (format: "yyyy-MM-dd HH:mm:ss:content")
    const lines = result.split('\n').filter(line => line.trim())
    logs.value = lines.map(line => {
      const match = line.match(/^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}):(.*)$/)
      if (match) {
        return {
          timestamp: match[1],
          content: match[2],
        }
      }
      return {
        timestamp: '',
        content: line,
      }
    })
  } catch (err: any) {
    error(err.message || '获取日志失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchLogs()
})
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
      <div>
        <h2 class="text-2xl font-bold text-gray-900">变更日志</h2>
        <p class="text-sm text-gray-500 mt-1">查看最近1天的DNS解析变更记录</p>
      </div>
      <Button @click="fetchLogs" :loading="loading">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2">
          <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2" />
        </svg>
        刷新
      </Button>
    </div>

    <!-- Logs Content -->
    <Card class="overflow-hidden">
      <div v-if="loading" class="flex items-center justify-center py-12">
        <svg class="animate-spin h-8 w-8 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
      </div>
      <div v-else-if="logs.length === 0" class="text-center py-12">
        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round" class="mx-auto mb-4 opacity-50">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14 2 14 8 20 8" />
          <line x1="16" y1="13" x2="8" y2="13" />
          <line x1="16" y1="17" x2="8" y2="17" />
          <polyline points="10 9 9 9 8 9" />
        </svg>
        <p class="text-gray-500">暂无变更日志</p>
        <p class="text-xs text-gray-400 mt-2">当DNS解析记录发生变化时，这里会显示变更历史</p>
      </div>
      <div v-else class="max-h-[600px] overflow-y-auto bg-gray-900 p-4 font-mono text-sm">
        <div v-for="(log, index) in logs" :key="index" class="mb-2 last:mb-0">
          <div class="flex items-start gap-3">
            <span class="text-green-400 whitespace-nowrap">{{ log.timestamp }}</span>
            <span class="text-gray-300 break-all">{{ log.content }}</span>
          </div>
        </div>
      </div>
    </Card>

    <!-- Info Card -->
    <Card class="p-4 bg-blue-50 border-blue-200">
      <div class="flex items-start gap-3">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-blue-600 mt-0.5 flex-shrink-0">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="16" x2="12" y2="12" />
          <line x1="12" y1="8" x2="12.01" y2="8" />
        </svg>
        <div>
          <h4 class="text-sm font-medium text-blue-900">说明</h4>
          <p class="text-xs text-blue-700 mt-1">
            变更日志记录了所有DNS解析记录的自动更新操作。当日志中显示的IP地址与当前配置的IP不一致时，系统会自动更新DNS解析记录。
          </p>
        </div>
      </div>
    </Card>
  </div>
</template>
