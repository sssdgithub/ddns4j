<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { parsingRecordApi, type ParsingRecord, type NetWorkSelectResponse } from '@/api/parsingRecord'
import { useToast } from '@/composables/useToast'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Select from '@/components/ui/Select.vue'

interface Props {
  mode: 'add' | 'edit' | 'copy'
  initialData?: ParsingRecord | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  submit: []
  cancel: []
}>()

const { success, error } = useToast()

// Form state
const form = ref<ParsingRecord>({
  serviceProvider: 1,
  serviceProviderId: '',
  serviceProviderSecret: '',
  recordType: 2,
  getIpMode: 2,
  getIpModeValue: '',
  domain: '',
  updateFrequency: 5,
})

const submitting = ref(false)
const ipModeOptions = ref<NetWorkSelectResponse[]>([])

// Options
const serviceProviderOptions = [
  { label: '阿里云', value: 1 },
  { label: '腾讯云', value: 2 },
  { label: 'Cloudflare', value: 3 },
  { label: '华为云', value: 4 },
]

const recordTypeOptions = [
  { label: 'AAAA (IPv6)', value: 1 },
  { label: 'A (IPv4)', value: 2 },
]

const getIpModeOptions = [
  { label: '通过网络接口获取', value: 1 },
  { label: '通过本地网卡获取', value: 2 },
]

const updateFrequencyOptions = [
  { label: '1分钟', value: 1 },
  { label: '2分钟', value: 2 },
  { label: '5分钟', value: 5 },
  { label: '10分钟', value: 10 },
]

// Load IP mode options
const loadIpModeOptions = async () => {
  if (!form.value.getIpMode || !form.value.recordType) return
  
  try {
    const result = await parsingRecordApi.getIpModeValue(form.value.getIpMode, form.value.recordType)
    ipModeOptions.value = result
    // Auto-select first option if available
    if (result.length > 0 && !form.value.getIpModeValue) {
      form.value.getIpModeValue = String(result[0].value)
    }
  } catch (err: any) {
    error(err.message || '获取IP选项失败')
  }
}

// Watch for changes in getIpMode or recordType
watch([() => form.value.getIpMode, () => form.value.recordType], () => {
  form.value.getIpModeValue = ''
  loadIpModeOptions()
})

// Initialize form with initial data
onMounted(() => {
  if (props.initialData && props.mode !== 'add') {
    form.value = { ...props.initialData }
    // Clear ID for copy mode
    if (props.mode === 'copy') {
      form.value.id = undefined
    }
    // Load IP mode options
    loadIpModeOptions()
  } else if (props.mode === 'add') {
    // Load initial IP mode options
    loadIpModeOptions()
  }
})

// Validate form
const validateForm = (): boolean => {
  if (!form.value.serviceProvider) {
    error('请选择服务商')
    return false
  }
  if (!form.value.serviceProviderId) {
    error('请输入密钥ID')
    return false
  }
  if (!form.value.serviceProviderSecret) {
    error('请输入密钥Secret')
    return false
  }
  if (!form.value.recordType) {
    error('请选择记录类型')
    return false
  }
  if (!form.value.getIpMode) {
    error('请选择IP获取方式')
    return false
  }
  if (!form.value.getIpModeValue) {
    error('请选择IP获取值')
    return false
  }
  if (!form.value.domain) {
    error('请输入域名')
    return false
  }
  if (!form.value.updateFrequency) {
    error('请选择更新频率')
    return false
  }
  return true
}

// Submit form
const handleSubmit = async () => {
  if (!validateForm()) return

  submitting.value = true
  try {
    if (props.mode === 'add') {
      await parsingRecordApi.add(form.value)
      success('新增成功')
    } else if (props.mode === 'edit') {
      await parsingRecordApi.modify(form.value)
      success('修改成功')
    } else if (props.mode === 'copy') {
      await parsingRecordApi.copy(form.value)
      success('复制成功')
    }
    emit('submit')
  } catch (err: any) {
    error(err.message || '操作失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <form @submit.prevent="handleSubmit" class="space-y-6">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Service Provider -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          服务商 <span class="text-red-500">*</span>
        </label>
        <Select v-model="form.serviceProvider" :options="serviceProviderOptions" placeholder="选择服务商" />
      </div>

      <!-- Record Type -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          记录类型 <span class="text-red-500">*</span>
        </label>
        <Select v-model="form.recordType" :options="recordTypeOptions" placeholder="选择记录类型" />
      </div>

      <!-- Service Provider ID -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          密钥ID <span class="text-red-500">*</span>
        </label>
        <Input v-model="form.serviceProviderId" placeholder="输入AccessKey ID或API Token" />
      </div>

      <!-- Service Provider Secret -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          密钥Secret <span class="text-red-500">*</span>
        </label>
        <Input v-model="form.serviceProviderSecret" type="password" placeholder="输入AccessKey Secret或API Key" />
      </div>

      <!-- Get IP Mode -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          IP获取方式 <span class="text-red-500">*</span>
        </label>
        <Select v-model="form.getIpMode" :options="getIpModeOptions" placeholder="选择IP获取方式" />
      </div>

      <!-- Get IP Mode Value -->
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-2">
          IP获取值 <span class="text-red-500">*</span>
        </label>
        <Select v-model="form.getIpModeValue" :options="ipModeOptions" placeholder="选择IP获取值" :disabled="!ipModeOptions.length" />
      </div>

      <!-- Domain -->
      <div class="md:col-span-2">
        <label class="block text-sm font-medium text-gray-700 mb-2">
          域名 <span class="text-red-500">*</span>
        </label>
        <Input v-model="form.domain" placeholder="例如: example.com 或 www.example.com" />
      </div>

      <!-- Update Frequency -->
      <div class="md:col-span-2">
        <label class="block text-sm font-medium text-gray-700 mb-2">
          更新频率 <span class="text-red-500">*</span>
        </label>
        <Select v-model="form.updateFrequency" :options="updateFrequencyOptions" placeholder="选择更新频率" />
        <p class="mt-1 text-xs text-gray-500">定时任务将按此频率检查并更新DNS解析记录</p>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex justify-end gap-3 pt-4 border-t">
      <Button type="button" variant="outline" @click="emit('cancel')">取消</Button>
      <Button type="submit" :loading="submitting">
        {{ mode === 'add' ? '新增' : mode === 'edit' ? '保存' : '复制' }}
      </Button>
    </div>
  </form>
</template>
