/*
 * Caipora — client-side behaviour.
 *
 * No inline event handlers are used anywhere (the Content Security Policy forbids inline scripts); behaviour is
 * attached here through delegated listeners and data-* attributes, so it keeps working after htmx swaps the page.
 */
(function () {
    'use strict';

    const THEME_KEY = 'caipora-theme';

    /** Registers a delegated listener that survives DOM swaps. */
    function on(eventType, selector, handler, options) {
        document.addEventListener(eventType, function (event) {
            const target = event.target instanceof Element ? event.target.closest(selector) : null;
            if (target) {
                handler.call(target, event, target);
            }
        }, options);
    }

    function currentTheme() {
        return document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'dark' : 'light';
    }

    /* ------------------------------------------------------------------ theme */

    function updateThemeToggle() {
        const dark = currentTheme() === 'dark';
        document.querySelectorAll('[data-theme-toggle]').forEach((button) => {
            const icon = button.querySelector('.bi');
            if (icon) {
                icon.className = 'bi ' + (dark ? 'bi-sun' : 'bi-moon-stars');
            }
            const label = dark ? 'Switch to light mode' : 'Switch to dark mode';
            button.setAttribute('aria-label', label);
            button.setAttribute('title', label);
        });
    }

    on('click', '[data-theme-toggle]', function () {
        const next = currentTheme() === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-bs-theme', next);
        try {
            localStorage.setItem(THEME_KEY, next);
        } catch (e) {
            // storage unavailable: the choice simply won't persist
        }
        updateThemeToggle();
        initEditors(true);
    });

    /* ----------------------------------------------------- confirmation dialog */

    /**
     * Replaces the browser's window.confirm() used by hx-confirm with a Bootstrap modal.
     * htmx 4 fires "htmx:confirm"; preventing it hands control over to issueRequest()/dropRequest().
     */
    document.addEventListener('htmx:confirm', function (event) {
        const detail = event.detail || {};
        const question = detail.ctx && detail.ctx.confirm;
        const modalElement = document.getElementById('confirmModal');
        if (!question || !modalElement || !window.bootstrap) {
            return; // fall back to the native dialog
        }
        event.preventDefault();

        modalElement.querySelector('[data-confirm-message]').textContent = question;
        const accept = modalElement.querySelector('[data-confirm-accept]');
        const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
        let confirmed = false;

        const onAccept = () => {
            confirmed = true;
            modal.hide();
        };
        const onHidden = () => {
            accept.removeEventListener('click', onAccept);
            if (confirmed) {
                detail.issueRequest();
            } else {
                detail.dropRequest();
            }
        };
        const open = () => {
            accept.addEventListener('click', onAccept);
            modalElement.addEventListener('hidden.bs.modal', onHidden, {once: true});
            modal.show();
        };
        // Bootstrap ignores show() while the dialog is still closing: wait for it to be fully hidden
        if (modalElement.classList.contains('show')) {
            modalElement.addEventListener('hidden.bs.modal', () => setTimeout(open, 0), {once: true});
        } else {
            open();
        }
    });

    /* -------------------------------------------------- submit feedback state */

    on('submit', 'form', function (event, form) {
        if (event.defaultPrevented || form.method.toLowerCase() === 'get' || form.hasAttribute('hx-confirm')) {
            return;
        }
        const buttons = [
            ...form.querySelectorAll('button[type="submit"]'),
            ...(form.id ? document.querySelectorAll('button[type="submit"][form="' + CSS.escape(form.id) + '"]') : [])
        ];
        // defer so the browser/htmx has already collected the form data
        setTimeout(() => buttons.forEach((button) => {
            button.classList.add('is-loading');
            button.setAttribute('aria-busy', 'true');
        }), 0);
    });

    // restore buttons when the page is shown again from the back/forward cache
    window.addEventListener('pageshow', () => {
        document.querySelectorAll('.btn.is-loading').forEach((button) => {
            button.classList.remove('is-loading');
            button.removeAttribute('aria-busy');
        });
    });

    /* ---------------------------------------------------- password visibility */

    on('click', '[data-toggle-password]', function (event, button) {
        const input = document.getElementById(button.getAttribute('data-toggle-password'));
        if (!input) {
            return;
        }
        const show = input.type === 'password';
        input.type = show ? 'text' : 'password';
        button.setAttribute('aria-pressed', String(show));
        button.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
        const icon = button.querySelector('.bi');
        if (icon) {
            icon.className = 'bi ' + (show ? 'bi-eye-slash' : 'bi-eye');
        }
    });

    /* --------------------------------------------------------------- widgets */

    function initDatepickers() {
        if (!window.flatpickr) {
            return;
        }
        document.querySelectorAll('.js-datepicker, .js-timepicker, .js-datetimepicker').forEach((item) => {
            if (item._flatpickr || item.disabled) {
                return;
            }
            const config = {
                allowInput: true,
                time_24hr: true,
                enableSeconds: true
            };
            if (item.classList.contains('js-datepicker')) {
                config.dateFormat = 'Y-m-d';
                config.altInput = true;
                config.altFormat = 'd M Y';
                config.onReady = function () {
                    // keep the <label for> pointing to the visible input
                    const id = this.input.id;
                    this.input.removeAttribute('id');
                    this.altInput.id = id;
                    if (this.input.classList.contains('is-invalid')) {
                        this.altInput.classList.add('is-invalid');
                    }
                };
            } else if (item.classList.contains('js-timepicker')) {
                config.enableTime = true;
                config.noCalendar = true;
                config.dateFormat = 'H:i:S';
            } else {
                config.enableTime = true;
                config.altInput = true;
                config.altFormat = 'Y-m-d H:i:S';
                config.dateFormat = 'Y-m-dTH:i:S';
                config.onReady = function () {
                    const id = this.input.id;
                    this.input.removeAttribute('id');
                    this.altInput.id = id;
                };
            }
            flatpickr(item, config);
        });
    }

    /**
     * (Re)creates the rich-text editors. Editors whose textarea was swapped out by htmx are removed first,
     * so instances never pile up. The textarea is kept in sync on every change, so any kind of form submission
     * (native or htmx) sends the current content.
     */
    function initEditors(force) {
        if (!window.tinymce) {
            return;
        }
        if (force) {
            tinymce.remove();
        } else {
            tinymce.get().forEach((editor) => {
                if (!document.body.contains(editor.getElement())) {
                    editor.remove();
                }
            });
        }
        const pending = [...document.querySelectorAll('textarea.tinymce-editor')]
            .filter((textarea) => !tinymce.get(textarea.id));
        if (pending.length === 0) {
            return;
        }
        const dark = currentTheme() === 'dark';
        tinymce.init({
            license_key: 'gpl',
            selector: pending.map((textarea) => '#' + CSS.escape(textarea.id)).join(','),
            skin: dark ? 'oxide-dark' : 'oxide',
            content_css: dark ? 'dark' : 'default',
            menubar: false,
            branding: false,
            promotion: false,
            statusbar: true,
            elementpath: false,
            min_height: 260,
            plugins: 'lists link table code autoresize wordcount',
            toolbar: 'undo redo | blocks | bold italic underline strikethrough | bullist numlist | link table | removeformat | code',
            link_default_protocol: 'https',
            convert_urls: false,
            setup: (editor) => {
                editor.on('change input undo redo', () => editor.save());
            }
        });
    }

    function initTooltips() {
        if (!window.bootstrap) {
            return;
        }
        document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach((element) => {
            bootstrap.Tooltip.getOrCreateInstance(element);
        });
    }

    function initAlerts() {
        document.querySelectorAll('.alert[data-autodismiss]').forEach((alert) => {
            alert.removeAttribute('data-autodismiss');
            setTimeout(() => {
                if (window.bootstrap && document.body.contains(alert)) {
                    bootstrap.Alert.getOrCreateInstance(alert).close();
                }
            }, 6000);
        });
    }

    function closeMobileSidebar() {
        const sidebar = document.getElementById('sidebar');
        if (window.bootstrap && sidebar) {
            const instance = bootstrap.Offcanvas.getInstance(sidebar);
            if (instance) {
                instance.hide();
            }
        }
        // a boosted navigation replaces the body: remove any orphan backdrop
        document.querySelectorAll('.offcanvas-backdrop, .modal-backdrop').forEach((backdrop) => backdrop.remove());
        document.body.classList.remove('modal-open');
        document.body.style.removeProperty('overflow');
        document.body.style.removeProperty('padding-right');
    }

    function initPage() {
        updateThemeToggle();
        initDatepickers();
        initEditors(false);
        initTooltips();
        initAlerts();
    }

    document.addEventListener('DOMContentLoaded', initPage);
    document.addEventListener('htmx:after:settle', () => {
        closeMobileSidebar();
        initPage();
    });
})();
