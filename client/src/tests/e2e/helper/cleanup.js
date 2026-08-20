const API_URL = "http://localhost:8080";

export async function cleanupTestData(request) {
    const response = await request.delete(
        `${API_URL}/api/test/cleanup`,
    );

    if (!response.ok()) {
        throw new Error(
            `Test cleanup failed: ${response.status()} ${await response.text()}`
        );
    }
}