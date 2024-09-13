/**
 * Register an event at the document for the specified selector,
 * so events are still catched after DOM changes.
 */
function handleEvent(eventType, selector, handler) {
  document.addEventListener(eventType, function(event) {
    if (event.target.matches(selector + ', ' + selector + ' *')) {
      handler.apply(event.target.closest(selector), arguments);
    }
  });
}

handleEvent('change', '.js-selectlinks', function(event) {
  htmx.ajax('get', this.value, document.body);
  history.pushState({ htmx: true }, '', this.value);
});

function initDatepicker() {
  document.querySelectorAll('.js-datepicker, .js-timepicker, .js-datetimepicker').forEach(($item) => {
    const flatpickrConfig = {
      allowInput: true,
      time_24hr: true,
      enableSeconds: true
    };
    if ($item.classList.contains('js-datepicker')) {
      flatpickrConfig.dateFormat = 'Y-m-d';
    } else if ($item.classList.contains('js-timepicker')) {
      flatpickrConfig.enableTime = true;
      flatpickrConfig.noCalendar = true;
      flatpickrConfig.dateFormat = 'H:i:S';
    } else { // datetimepicker
      flatpickrConfig.enableTime = true;
      flatpickrConfig.altInput = true;
      flatpickrConfig.altFormat = 'Y-m-d H:i:S';
      flatpickrConfig.dateFormat = 'Y-m-dTH:i:S';
      // workaround label issue
      flatpickrConfig.onReady = function() {
        const id = this.input.id;
        this.input.id = null;
        this.altInput.id = id;
      };
    }
    flatpickr($item, flatpickrConfig);
  });
}
document.addEventListener('htmx:afterSwap', initDatepicker);
initDatepicker();

function initializeTinyMCE() {
  tinymce.remove('textarea.tinymce-editor');

  setTimeout(() => {
    tinymce.init({
      license_key: 'gpl',
      selector: 'textarea.tinymce-editor',
      plugins: 'lists link image table code help wordcount',
      toolbar: 'undo redo | formatselect | bold italic | alignleft aligncenter alignright | bullist numlist outdent indent | link image table | code | help | wordcount',
    });
  }, 100);

  const forms = document.querySelectorAll('form');
  forms.forEach(form => {
    form.addEventListener('submit', (event) => {
      tinymce.triggerSave();
    });
  });
}

// Initialize on initial page load
document.addEventListener('DOMContentLoaded', initializeTinyMCE);

// Initialize on htmx:afterSwap
document.body.addEventListener('htmx:afterSwap', initializeTinyMCE);

function clearForm() {
  const form = document.getElementById('searchForm');
  const inputElements = form.querySelectorAll('input, select, textarea');

  inputElements.forEach(input => {
    if (input.type === 'checkbox' || input.type === 'radio') {
      input.checked = false;
    } else if (input.tagName.toLowerCase() === 'select') {
      // Deselect all options first
      for (let i = 0; i < input.options.length; i++) {
        input.options[i].selected = false;
      }

      const emptyOption = input.querySelector('option[value=""]');
      if (emptyOption) {
        emptyOption.selected = true;
      } else {
        input.selectedIndex = -1;
      }
    } else if (input.type !== 'button' && input.type !== 'submit' && input.type !== 'reset') {
      input.value = '';
    }
  });
  return false;
}