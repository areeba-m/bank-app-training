// src/tests/e2e/global-teardown.js

import { request } from "@playwright/test";

const API_URL = "http://localhost:8080";

export default async function globalTeardown() {
    const apiContext = await request.newContext();

    try {
        const response = await apiContext.delete(
            `${API_URL}/api/test/cleanup`,
        );

        if (!response.ok()) {
            throw new Error(
                `Test cleanup failed: ${response.status()} ${await response.text()}`
            );
        }

        console.log("E2E test data cleaned successfully.");
    } finally {
        await apiContext.dispose();
    }
}