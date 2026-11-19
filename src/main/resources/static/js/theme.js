/*
 * Applies the stored (or system) colour theme before the page is painted, avoiding a flash of the wrong theme.
 * Loaded synchronously in <head>; kept as an external file so the Content Security Policy can forbid inline scripts.
 */
(function () {
    'use strict';
    var theme = null;
    try {
        theme = localStorage.getItem('caipora-theme');
    } catch (e) {
        // storage may be unavailable (private mode, blocked cookies)
    }
    if (theme !== 'light' && theme !== 'dark') {
        theme = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    document.documentElement.setAttribute('data-bs-theme', theme);
})();
