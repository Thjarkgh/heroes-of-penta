import { defineConfig } from "vitest/config";

export default defineConfig({
  test: {
    include: ["tests/**/*.test.{ts,js}"],
    environment: "node",
    coverage: {
      include: [
        "src/**/*.ts",
        "src/**/*.js",
      ]
    }
  }
});