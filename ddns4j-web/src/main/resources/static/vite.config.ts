import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    open: process.env.BROWSER === 'open', // Auto-open browser when BROWSER=open
    proxy: {
      '/parsingRecord': {
        target: 'http://localhost:10000',
        changeOrigin: true,
      },
      '/changedLog': {
        target: 'http://localhost:10000',
        changeOrigin: true,
      },
      '/publicAccess': {
        target: 'http://localhost:10000',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    emptyOutDir: true,
  },
})
