/// <reference types="vitest/config" />
import { fileURLToPath } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// Os endpoints do backend (Spring Boot) são servidos pelo proxy do Vite para que o
// navegador fale sempre com a mesma origem — assim não é preciso configurar CORS na API.
const apiPaths = ['/users', '/bookings', '/courts']

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const target = env.VITE_API_TARGET || 'http://localhost:8080'

  return {
    plugins: [react()],
    server: {
      proxy: Object.fromEntries(apiPaths.map((path) => [path, { target, changeOrigin: true }])),
    },
    test: {
      coverage: {
        provider: 'v8',
        // lcov.info é o formato lido pelo SonarCloud (sonar.javascript.lcov.reportPaths).
        // projectRoot = raiz do repositório, para os caminhos saírem como
        // "frontend/src/..." — o mesmo caminho que o Sonar enxerga.
        reporter: ['text-summary', ['lcov', { projectRoot: fileURLToPath(new URL('..', import.meta.url)) }]],
        reportsDirectory: 'coverage',
        include: ['src/**/*.{ts,tsx}'],
        exclude: ['src/**/*.test.{ts,tsx}', 'src/main.tsx'],
      },
    },
  }
})
