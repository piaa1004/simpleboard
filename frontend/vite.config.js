import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

const BACKEND = 'http://localhost:8080'

export default defineConfig({
  plugins: [react(), tailwindcss()],

  resolve: {
    alias: [{ find: '@', replacement: fileURLToPath(new URL('./src', import.meta.url)) }],
  },

  server: {
    // 백엔드 application.yaml 의 client.url 과 같은 포트를 쓴다.
    port: 5173,
    strictPort: true,
    proxy: {
      // 앱의 모든 API 호출은 /api 로 나가고, 여기서 접두사를 떼어 백엔드 루트로 넘긴다.
      // 같은 오리진으로 보이게 되므로 쿠키 인증에 CORS 설정이 필요 없다.
      '/api': {
        target: BACKEND,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },

  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/setupTests.js',
  },
})
