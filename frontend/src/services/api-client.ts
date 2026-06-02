const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export async function withMockLatency<T>(data: T): Promise<T> {
  await delay(180);
  return data;
}
