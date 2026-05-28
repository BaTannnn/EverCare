export const makeAvatarDataUri = (name, colors = ["#0059bb", "#20c997"]) => {
  const initials = name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() || "")
    .join("");

  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 240 240">
      <defs>
        <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="${colors[0]}" />
          <stop offset="100%" stop-color="${colors[1]}" />
        </linearGradient>
      </defs>
      <rect width="240" height="240" rx="48" fill="url(#g)" />
      <text x="50%" y="54%" text-anchor="middle" dominant-baseline="middle" fill="#ffffff" font-family="Inter, Arial, sans-serif" font-size="64" font-weight="700">${initials}</text>
    </svg>
  `;

  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
};

export const patientInvoices = [
  {
    id: "INV-2026-089",
    invoiceCode: "INV-2026-089",
    createdAt: "2026-04-12",
    serviceAmount: 450000,
    medicineAmount: 120000,
    testAmount: 180000,
    totalAmount: 750000,
    status: "UNPAID",
  },
  {
    id: "INV-2026-072",
    invoiceCode: "INV-2026-072",
    createdAt: "2026-03-28",
    serviceAmount: 350000,
    medicineAmount: 90000,
    testAmount: 0,
    totalAmount: 440000,
    status: "PAID",
  },
];
