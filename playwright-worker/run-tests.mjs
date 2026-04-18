import fs from "node:fs/promises";
import path from "node:path";
import { chromium } from "playwright";

const [inputPath, outputPath] = process.argv.slice(2);

if (!inputPath || !outputPath) {
  console.error("Usage: node run-tests.mjs <input.json> <output.json>");
  process.exit(1);
}

const inputRaw = await fs.readFile(inputPath, "utf-8");
const input = JSON.parse(inputRaw);

const browser = await chromium.launch({ headless: true });
const context = await browser.newContext();
const artifactsDir = path.resolve("./artifacts/screenshots");
await fs.mkdir(artifactsDir, { recursive: true });

const results = [];

for (const testCase of input.testCases ?? []) {
  const start = Date.now();
  let status = "PASSED";
  let errorMessage = null;
  let screenshotPath = null;

  try {
    const page = await context.newPage();
    const url = new URL(testCase.path ?? "/", input.targetUrl).toString();
    await page.goto(url, { waitUntil: "domcontentloaded", timeout: 25000 });

    const safeName = (testCase.testName ?? "test").toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)/g, "") || "test";
    screenshotPath = path.join(artifactsDir, `${safeName}-${Date.now()}.png`);
    await page.screenshot({ path: screenshotPath, fullPage: true });

    if (testCase.expectedText && testCase.expectedText.trim().length > 0) {
      const bodyText = await page.locator("body").innerText();
      if (!bodyText.includes(testCase.expectedText)) {
        throw new Error(`Expected text not found: ${testCase.expectedText}`);
      }
    }

    await page.close();
  } catch (error) {
    status = "FAILED";
    errorMessage = error instanceof Error ? error.message : "Unknown Playwright error";
  }

  results.push({
    testName: testCase.testName,
    status,
    errorMessage,
    durationMs: Date.now() - start,
    screenshotPath,
  });
}

await context.close();
await browser.close();

await fs.writeFile(outputPath, JSON.stringify({ results }, null, 2), "utf-8");
