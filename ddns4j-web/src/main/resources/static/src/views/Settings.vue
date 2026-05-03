<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { publicAccessApi } from '@/api/publicAccess'
import { useToast } from '@/composables/useToast'
import Card from '@/components/ui/Card.vue'

const { success, error } = useToast()

const publicAccessDisabled = ref(false)
const loading = ref(false)
const saving = ref(false)

const fetchSettings = async () => {
  loading.value = true
  try {
    const result = await publicAccessApi.getPublicAccessDisabled()
    publicAccessDisabled.value = result.publicAccessDisabled
  } catch (err: any) {
    error(err.message || '获取设置失败')
  } finally {
    loading.value = false
  }
}

const togglePublicAccess = async () => {
  saving.value = true
  try {
    await publicAccessApi.setPublicAccessDisabled(!publicAccessDisabled.value)
    publicAccessDisabled.value = !publicAccessDisabled.value
    success(publicAccessDisabled.value ? '已禁用公网访问' : '已启用公网访问')
  } catch (err: any) {
    error(err.message || '设置失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchSettings()
})
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h2 class="text-2xl font-bold text-gray-900">系统设置</h2>
      <p class="text-sm text-gray-500 mt-1">管理系统配置和安全选项</p>
    </div>

    <!-- Settings Cards -->
    <div class="grid gap-6">
      <!-- Public Access Control -->
      <Card class="p-6">
        <div v-if="loading" class="flex items-center justify-center py-8">
          <svg class="animate-spin h-8 w-8 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
        </div>
        <div v-else class="space-y-4">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-3">
                <div :class="['w-12 h-12 rounded-lg flex items-center justify-center', publicAccessDisabled ? 'bg-red-100' : 'bg-green-100']">
                  <svg v-if="publicAccessDisabled" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-red-600">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                    <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                  </svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-green-600">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                    <path d="M7 11V7a5 5 0 0 1 9.9-1" />
                  </svg>
                </div>
                <div>
                  <h3 class="text-lg font-semibold text-gray-900">公网访问控制</h3>
                  <p class="text-sm text-gray-500 mt-1">
                    {{ publicAccessDisabled ? '当前已禁用公网访问，仅允许内网访问管理界面' : '当前允许公网访问管理界面' }}
                  </p>
                </div>
              </div>
            </div>
            <button
              :disabled="saving"
              :class="[
                'relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2',
                publicAccessDisabled ? 'bg-red-500' : 'bg-green-500',
                saving && 'opacity-50 cursor-not-allowed'
              ]"
              @click="togglePublicAccess"
            >
              <span
                :class="[
                  'pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out',
                  publicAccessDisabled ? 'translate-x-5' : 'translate-x-0'
                ]"
              />
            </button>
          </div>

          <div class="bg-gray-50 rounded-lg p-4">
            <div class="flex items-start gap-3">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-yellow-600 mt-0.5 flex-shrink-0">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
                <line x1="12" y1="9" x2="12" y2="13" />
                <line x1="12" y1="17" x2="12.01" y2="17" />
              </svg>
              <div class="text-sm text-gray-700">
                <p class="font-medium mb-1">安全提示</p>
                <ul class="list-disc list-inside space-y-1 text-xs text-gray-600">
                  <li>禁用公网访问后，只有内网IP可以访问管理界面</li>
                  <li>建议在生产环境中禁用公网访问以提高安全性</li>
                  <li>此设置不影响DNS解析记录的自动更新功能</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </Card>

      <!-- System Info -->
      <Card class="p-6">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">系统信息</h3>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="space-y-3">
            <div>
              <p class="text-xs text-gray-500">应用版本</p>
              <p class="text-sm font-medium text-gray-900">v1.6.5-RELEASE</p>
            </div>
            <div>
              <p class="text-xs text-gray-500">技术栈</p>
              <p class="text-sm font-medium text-gray-900">Spring Boot + Vue 3</p>
            </div>
          </div>
          <div class="space-y-3">
            <div>
              <p class="text-xs text-gray-500">支持的DNS服务商</p>
              <p class="text-sm font-medium text-gray-900">阿里云、腾讯云、Cloudflare、华为云</p>
            </div>
            <div>
              <p class="text-xs text-gray-500">数据库</p>
              <p class="text-sm font-medium text-gray-900">H2 / MySQL</p>
            </div>
          </div>
        </div>
      </Card>
    </div>
  </div>
</template>
