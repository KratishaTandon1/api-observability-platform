"use client";

import { useEffect, useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { fetchLogs, ApiLog } from './lib/api';
import { 
  AlertTriangle, CheckCircle, Clock, Search, Filter, Server, Zap, LogOut, BarChart3, Calendar, Activity
} from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';

export default function Dashboard() {
  const [logs, setLogs] = useState<ApiLog[]>([]);
  const router = useRouter();
  
  // Auth Check
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      router.push('/login');
    }
  }, [router]);

  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState('ALL');
  // NEW: Date Range State
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  useEffect(() => {
    fetchLogs().then(setLogs);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    router.push('/login');
  };

  const handleResolve = async (id: string) => {
    try {
      const res = await fetch(`http://localhost:8080/api/v1/logs/${id}/resolve`, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
      });

      if (res.ok) {
        setLogs(currentLogs => 
          currentLogs.map(log => log.id === id ? { ...log, resolved: true } : log)
        );
      }
    } catch (error) {
      console.error("Error resolving:", error);
    }
  };

  // --- ANALYTICS CALCULATIONS ---
  
  // 1. Filtered Logs (Now includes Date)
  const filteredLogs = useMemo(() => {
    return logs.filter(log => {
      // Search
      const matchesSearch = 
        log.serviceName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.endpoint.toLowerCase().includes(searchTerm.toLowerCase());

      // Type Filter
      let matchesType = true;
      if (filterType === 'SLOW') matchesType = log.duration > 500;
      if (filterType === 'BROKEN') matchesType = log.status >= 500;
      if (filterType === 'RATELIMIT') matchesType = log.rateLimitHit;

      // Date Filter
      let matchesDate = true;
      if (startDate) {
        matchesDate = matchesDate && new Date(log.timestamp) >= new Date(startDate);
      }
      if (endDate) {
        // Set end date to end of day
        const end = new Date(endDate);
        end.setHours(23, 59, 59, 999);
        matchesDate = matchesDate && new Date(log.timestamp) <= end;
      }

      return matchesSearch && matchesType && matchesDate;
    });
  }, [logs, searchTerm, filterType, startDate, endDate]);

  // 2. Avg Latency per Endpoint (Requirement: "Avg latency per endpoint" & "Top 5 slow endpoints")
  const endpointStats = useMemo(() => {
    const stats: Record<string, { total: number, count: number, service: string }> = {};
    
    logs.forEach(log => {
      if (!stats[log.endpoint]) {
        stats[log.endpoint] = { total: 0, count: 0, service: log.serviceName };
      }
      stats[log.endpoint].total += log.duration;
      stats[log.endpoint].count += 1;
    });

    return Object.entries(stats)
      .map(([endpoint, data]) => ({
        endpoint,
        service: data.service,
        avgLatency: Math.round(data.total / data.count)
      }))
      .sort((a, b) => b.avgLatency - a.avgLatency) // Sort by Slowest Average
      .slice(0, 5); // Top 5
  }, [logs]);

  // 3. Status Distribution for Graph
  const statusData = [
    { name: 'Success (2xx)', count: logs.filter(l => l.status >= 200 && l.status < 300).length, color: '#10b981' },
    { name: 'Rate Limit (429)', count: logs.filter(l => l.status === 429).length, color: '#3b82f6' },
    { name: 'Error (5xx)', count: logs.filter(l => l.status >= 500).length, color: '#ef4444' },
  ];

  // 4. Counts
  const slowRequests = logs.filter(l => l.duration > 500).length;
  const errors = logs.filter(l => l.status >= 500).length;
  const rateLimitHits = logs.filter(l => l.rateLimitHit).length;

  return (
    <div className="min-h-screen bg-slate-50 font-sans text-slate-900">
      
      {/* Navbar */}
      <nav className="bg-white border-b border-slate-200 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center gap-2">
              <div className="bg-blue-600 p-2 rounded-lg">
                <Server className="h-5 w-5 text-white" />
              </div>
              <span className="font-bold text-xl tracking-tight text-slate-800">
                Obs<span className="text-blue-600">ervability</span>
              </span>
            </div>
            <button onClick={handleLogout} className="flex items-center gap-2 text-slate-500 hover:text-red-600 transition">
              <LogOut className="h-4 w-4" />
              <span className="text-sm font-medium">Logout</span>
            </button>
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        
        {/* Top Section: Widgets & Graph */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
          
          {/* Left: Key Metrics */}
          <div className="space-y-6">
            <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 flex items-center">
              <div className="p-3 bg-red-50 rounded-lg mr-4 border border-red-100"><AlertTriangle className="h-6 w-6 text-red-500" /></div>
              <div>
                <p className="text-sm font-medium text-slate-500">Broken APIs</p>
                <h3 className="text-3xl font-bold text-slate-900">{errors}</h3>
              </div>
            </div>
            <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 flex items-center">
               <div className="p-3 bg-amber-50 rounded-lg mr-4 border border-amber-100"><Clock className="h-6 w-6 text-amber-500" /></div>
               <div>
                 <p className="text-sm font-medium text-slate-500">Slow APIs</p>
                 <h3 className="text-3xl font-bold text-slate-900">{slowRequests}</h3>
               </div>
            </div>
            <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 flex items-center">
               <div className="p-3 bg-blue-50 rounded-lg mr-4 border border-blue-100"><Zap className="h-6 w-6 text-blue-500" /></div>
               <div>
                 <p className="text-sm font-medium text-slate-500">Rate Limits</p>
                 <h3 className="text-3xl font-bold text-slate-900">{rateLimitHits}</h3>
               </div>
            </div>
          </div>

          {/* Middle: Request Distribution Graph */}
          <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 flex flex-col justify-between">
            <h3 className="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
              <BarChart3 className="h-5 w-5 text-slate-400" />
              Traffic Distribution
            </h3>
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={statusData}>
                  <XAxis dataKey="name" fontSize={12} tickLine={false} axisLine={false} />
                  <YAxis fontSize={12} tickLine={false} axisLine={false} />
                  <Tooltip cursor={{fill: 'transparent'}} />
                  <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                    {statusData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Right: Top 5 Slowest Endpoints (AVG LATENCY) */}
          <div className="bg-white rounded-xl shadow-sm border border-slate-200 p-6 overflow-hidden">
            <h3 className="text-lg font-bold text-slate-800 mb-4 flex items-center gap-2">
              <Activity className="h-5 w-5 text-slate-400" />
              Avg Latency (Top 5)
            </h3>
            <div className="space-y-4">
              {endpointStats.map((stat, i) => (
                <div key={stat.endpoint} className="flex items-center justify-between p-3 bg-slate-50 rounded-lg border border-slate-100">
                   <div className="truncate pr-4">
                     <p className="text-sm font-medium text-slate-900 truncate">{stat.endpoint}</p>
                     <p className="text-xs text-slate-500">{stat.service}</p>
                   </div>
                   <div className="text-right">
                     <span className={`text-sm font-bold whitespace-nowrap ${stat.avgLatency > 500 ? 'text-red-600' : 'text-slate-700'}`}>
                       {stat.avgLatency}ms
                     </span>
                     <p className="text-[10px] text-slate-400">avg</p>
                   </div>
                </div>
              ))}
              {endpointStats.length === 0 && <p className="text-sm text-slate-400">No data available</p>}
            </div>
          </div>
        </div>

        {/* Filters & Table */}
        <div className="bg-white p-4 rounded-t-xl border border-b-0 border-slate-200 flex flex-col xl:flex-row justify-between items-center gap-4">
            
            {/* Search */}
            <div className="relative w-full xl:w-96">
              <Search className="h-5 w-5 text-slate-400 absolute left-3 top-2.5" />
              <input
                type="text"
                className="pl-10 block w-full rounded-lg border-slate-300 bg-white shadow-sm focus:ring-blue-500 focus:border-blue-500 sm:text-sm py-2.5 border placeholder-slate-400"
                placeholder="Search logs..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            <div className="flex flex-col sm:flex-row items-center gap-3 w-full xl:w-auto">
                {/* Date Filters */}
                <div className="flex items-center gap-2 bg-slate-50 px-3 py-2 rounded-lg border border-slate-200 w-full sm:w-auto">
                  <Calendar className="h-4 w-4 text-slate-500" />
                  <input 
                    type="date" 
                    className="bg-transparent border-none text-sm text-slate-600 focus:ring-0 p-0"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                  />
                  <span className="text-slate-400">-</span>
                  <input 
                    type="date" 
                    className="bg-transparent border-none text-sm text-slate-600 focus:ring-0 p-0"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                  />
                </div>

                {/* Type Filter */}
                <div className="flex items-center gap-2 bg-slate-50 px-3 py-2 rounded-lg border border-slate-200 w-full sm:w-auto">
                    <Filter className="h-4 w-4 text-slate-500" />
                    <select 
                      className="bg-transparent border-none text-sm font-medium text-slate-700 focus:ring-0 cursor-pointer w-full"
                      value={filterType}
                      onChange={(e) => setFilterType(e.target.value)}
                    >
                      <option value="ALL">All Events</option>
                      <option value="SLOW">Slow Requests</option>
                      <option value="BROKEN">Failed Requests</option>
                      <option value="RATELIMIT">Rate Limited</option>
                    </select>
                </div>
            </div>
        </div>

        <div className="bg-white border border-slate-200 rounded-b-xl shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase">Service</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase">Endpoint</th>
                  <th className="px-6 py-3 text-left text-xs font-semibold text-slate-500 uppercase">Latency</th>
                  <th className="px-6 py-3 text-right text-xs font-semibold text-slate-500 uppercase">Action</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-slate-200">
                {filteredLogs.map((log) => {
                  const isIssue = log.duration > 500 || log.status >= 500 || log.rateLimitHit;
                  return (
                    <tr key={log.id} className="hover:bg-slate-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-2.5 py-0.5 inline-flex text-xs font-bold rounded-full border ${
                          log.status >= 500 ? 'bg-red-100 text-red-700 border-red-200' : 
                          log.status >= 400 ? 'bg-amber-100 text-amber-700 border-amber-200' : 
                          'bg-emerald-100 text-emerald-700 border-emerald-200'
                        }`}>
                          {log.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm font-medium text-slate-900">{log.serviceName}</td>
                      <td className="px-6 py-4 text-sm text-slate-600">{log.endpoint}</td>
                      <td className={`px-6 py-4 text-sm font-bold ${log.duration > 500 ? 'text-red-600' : 'text-slate-600'}`}>
                        {log.duration}ms
                      </td>
                      <td className="px-6 py-4 text-sm text-right">
                         {log.resolved ? (
                            <span className="text-emerald-600 flex items-center justify-end gap-1"><CheckCircle className="w-4 h-4"/> Resolved</span>
                         ) : isIssue ? (
                            <button onClick={() => handleResolve(log.id)} className="text-blue-600 hover:text-blue-800 text-xs font-bold border border-blue-200 px-3 py-1 rounded bg-blue-50">Resolve</button>
                         ) : <span className="text-slate-300">-</span>}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
}