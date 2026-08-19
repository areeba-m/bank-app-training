import { expect } from "@playwright/test";

const MAILPIT_URL = "http://localhost:8025";

export async function waitForUserEmail(page, recipientEmail) {
    await expect
        .poll(
            async () => {
                const response = await page.request.get(
                    `${MAILPIT_URL}/api/v1/search?query=to:${encodeURIComponent(
                        recipientEmail,
                    )}`,
                );

                if (!response.ok()) {
                    return 0;
                }

                const data = await response.json();

                return data.messages?.length ?? 0;
            },
            {
                timeout: 30_000,
                intervals: [500, 1000, 2000],
                message: `Waiting for OTP email for ${recipientEmail}`,
            },
        )
        .toBeGreaterThan(0);
}

export async function getLatestMail(page, recipientEmail) {
    const response = await page.request.get(
        `${MAILPIT_URL}/api/v1/search?query=to:${encodeURIComponent(
            recipientEmail,
        )}`,
    );

    expect(
        response.ok(),
        `Mailpit search failed for ${recipientEmail}`,
    ).toBeTruthy();

    const data = await response.json();

    expect(
        data.messages?.length ?? 0,
        `No email found in Mailpit for ${recipientEmail}`,
    ).toBeGreaterThan(0);

    const message = data.messages[0];

    const messageResponse = await page.request.get(
        `${MAILPIT_URL}/api/v1/message/${message.ID}`,
    );

    expect(
        messageResponse.ok(),
        `Could not retrieve Mailpit message ${message.ID}`,
    ).toBeTruthy();

    return await messageResponse.json();
}

export function extractOtp(email) {
    const body = `${email.Text || ""}\n${email.HTML || ""}`;

    const matches = body.match(/\b\d{6}\b/g);

    expect(
        matches,
        `Could not find a 6-digit OTP in email.\n\n${body}`,
    ).not.toBeNull();

    expect(matches.length).toBeGreaterThan(0);

    return matches[0];
}

export function extractVerificationLink(email) {
    const html = email.HTML || "";
    const text = email.Text || "";

    let match = html.match(
        /href=["']([^"']*\/activate\?email=[^"']+)["']/i,
    );

    if (match) {
        return match[1]
            .replace(/&amp;/g, "&")
            .replace(/&quot;/g, '"');
    }

    match = text.match(
        /https?:\/\/[^)\s]+\/activate\?email=[^)\s]+/i,
    );

    if (match) {
        return match[0]
            .replace(/&amp;/g, "&")
            .replace(/&quot;/g, '"')
            .replace(/[)\]]+$/, "");
    }

    const body = `${text}\n${html}`;

    match = body.match(
        /https?:\/\/[^"'<>\s)]+\/activate\?email=[^"'<>\s)]+/i,
    );

    expect(
        match,
        `Could not find activation link in email.

HTML:
${html}

TEXT:
${text}`,
    ).not.toBeNull();

    return match[0]
        .replace(/&amp;/g, "&")
        .replace(/&quot;/g, '"')
        .replace(/[)\]]+$/, "");
}