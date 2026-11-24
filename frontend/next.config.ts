import path from "path";
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: process.env.NEXT_OUTPUT_STANDALONE === "true" ? "standalone" : undefined,
  outputFileTracingRoot: path.join(__dirname),
  reactStrictMode: true,
};

export default nextConfig;

