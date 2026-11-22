export function formatearDistancia(metros, locale = 'es') {
    if (metros == null || !Number.isFinite(Number(metros)) || Number(metros) < 0)
        return '';

    const kilometros = Number(metros) >= 1000;

    return new Intl.NumberFormat(locale, {
        style: 'unit',
        unit: kilometros ? 'kilometer' : 'meter',
        unitDisplay: 'short',
        maximumFractionDigits: kilometros ? 1 : 0
    }).format(kilometros ? Number(metros) / 1000 : Number(metros));
}

export function formatearPrecio(precio, locale = 'es') {
    if (precio == null || !Number.isFinite(Number(precio)))
        return '—';

    return new Intl.NumberFormat(locale, {
        style: 'currency',
        currency: 'EUR',
        minimumFractionDigits: 3,
        maximumFractionDigits: 3
    }).format(Number(precio));
}

export function fechaLocal(date = new Date()) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}
