import { NextResponse } from "next/server";

export function proxy() {
  return NextResponse.next();
}

export function toApiUrl(path: string) {
  const baseUrl = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
  return `${baseUrl}${path}`;
}
