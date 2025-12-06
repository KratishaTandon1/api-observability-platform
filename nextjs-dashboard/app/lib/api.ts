const COLLECTOR_API = 'http://localhost:8080/api/v1/logs';

export interface ApiLog {
  id: string;
  serviceName: string;
  endpoint: string;
  method: string;
  status: number;
  duration: number;
  timestamp: string;
  rateLimitHit: boolean;
  resolved?: boolean;
}

export async function fetchLogs(): Promise<ApiLog[]> {
  try {
    const res = await fetch(COLLECTOR_API, { cache: 'no-store' });
    if (!res.ok) throw new Error('Failed to fetch');
    return await res.json();
  } catch (error) {
    console.error("API Error:", error);
    return [];
  }
}