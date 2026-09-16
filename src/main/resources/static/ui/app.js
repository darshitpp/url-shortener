const form = document.querySelector("#shorten-form");
const urlInput = document.querySelector("#original-url");
const customPathInput = document.querySelector("#custom-path");
const originPrefix = document.querySelector("#origin-prefix");
const shortenButton = document.querySelector("#shorten-button");
const result = document.querySelector("#result");
const shortUrlLink = document.querySelector("#short-url");
const copyButton = document.querySelector("#copy-button");
const status = document.querySelector("#status");

const MAX_URL_LENGTH = 2048;
const MAX_CUSTOM_PATH_LENGTH = 64;
const REQUEST_TIMEOUT_MS = 10000;

originPrefix.textContent = `${window.location.origin}/`;

function setStatus(message, kind = "") {
    status.textContent = message;
    status.dataset.kind = kind;
}

function isHttpUrl(value) {
    if (value.length > MAX_URL_LENGTH) {
        return false;
    }

    try {
        const parsedUrl = new URL(value);
        return parsedUrl.protocol === "http:" || parsedUrl.protocol === "https:";
    } catch {
        return false;
    }
}

function normalizeShortUrl(value) {
    if (/^https?:\/\//i.test(value)) {
        return value;
    }

    return new URL(value.replace(/^\/+/, ""), `${window.location.origin}/`).toString());
}

function showResult(shortUrl) {
    shortUrlLink.href = shortUrl;
    shortUrlLink.textContent = shortUrl;
    result.hidden = false;
    shortUrlLink.focus();
}

function retryAfterMessage(response) {
    const retryAfter = response.headers.get("Retry-After");
    const seconds = Number.parseInt(retryAfter || "", 10);
    if (Number.isInteger(seconds) && seconds > 0) {
        return `Too many requests. Try again in ${seconds} seconds.`;
    }
    return "Too many requests. Try again shortly.";
}

urlInput.addEventListener("input", () => {
    urlInput.setCustomValidity("");
});

copyButton.addEventListener("click", async () => {
    try {
        await navigator.clipboard.writeText(shortUrlLink.href);
        setStatus("Copied to clipboard.", "success");
    } catch {
        setStatus("Could not copy the link. Select it manually.", "error");
    }
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const url = urlInput.value.trim();
    const customPath = customPathInput.value.trim();

    if (!isHttpUrl(url)) {
        urlInput.setCustomValidity(
            `Enter a complete http:// or https:// URL up to ${MAX_URL_LENGTH} characters.`
        );
        urlInput.reportValidity();
        return;
    }

    urlInput.setCustomValidity("");

    if (customPath.length > MAX_CUSTOM_PATH_LENGTH || !form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const payload = {url};

    if (customPath) {
        payload.strategy = "custom";
        payload.options = {customPath};
    }

    result.hidden = true;
    shortenButton.disabled = true;
    setStatus("Shortening...");

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

    try {
        const response = await fetch("/shorten", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload),
            signal: controller.signal
        });

        let data = {};

        try {
            data = await response.json();
        } catch {
            data = {};
        }

        if (!response.ok) {
            if (response.status === 400) {
                throw new Error(data.error || "The request could not be processed.");
            }
            if (response.status === 413) {
                throw new Error(
                    "The request is too large. Use a shorter URL or custom path."
                );
            }
            if (response.status === 429) {
                throw new Error(retryAfterMessage(response));
            }

            throw new Error(`Shortening failed (${response.status}).`);
        }

        if (!data.shortUrl) {
            throw new Error(
                customPath
                    ? `The path "${customPath}" is already in use.`
                    : "The server did not return a short link."
            );
        }

        showResult(normalizeShortUrl(data.shortUrl));
        setStatus("Short link ready.", "success");
    } catch (error) {
        if (error.name === "AbortError" || error instanceof TypeError) {
            setStatus(
                "Could not reach the service. Check your connection and try again.",
                "error"
            );
        } else {
            setStatus(error.message || "Something went wrong. Try again.", "error");
        }
    } finally {
        clearTimeout(timeoutId);
        shortenButton.disabled = false;
    }
});
