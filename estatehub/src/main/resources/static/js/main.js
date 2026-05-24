/* ============================================================
   ESTATEHUB — main.js
   Production-ready JavaScript
   ============================================================ */

(function () {
  'use strict';

  // ── Navbar scroll behaviour ──────────────────────────────────
  const navbar = document.querySelector('.eh-navbar');
  if (navbar) {
    const onScroll = () => {
      navbar.style.background = window.scrollY > 30
        ? 'rgba(13,13,20,0.98)'
        : 'rgba(13,13,20,0.95)';
    };
    window.addEventListener('scroll', onScroll, { passive: true });
  }

  // ── Auto-dismiss flash alerts ────────────────────────────────
  document.querySelectorAll('.eh-alert-success, .eh-alert-error, .eh-alert-info')
    .forEach(alert => {
      setTimeout(() => {
        alert.style.transition = 'opacity 0.5s ease';
        alert.style.opacity = '0';
        setTimeout(() => alert.remove(), 500);
      }, 5000);
    });

  // ── Password toggle ──────────────────────────────────────────
  window.togglePass = function (id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.type = el.type === 'password' ? 'text' : 'password';
    const btn = el.nextElementSibling;
    if (btn) {
      const icon = btn.querySelector('i');
      if (icon) {
        icon.className = el.type === 'password' ? 'bi bi-eye' : 'bi bi-eye-slash';
      }
    }
  };

  // ── Mobile number validation (^[6-9]\d{9}$) ─────────────────
  // Preserved from RealEstate.register()
  const mobileInput = document.querySelector('input[name="mobileNumber"]');
  if (mobileInput) {
    mobileInput.addEventListener('input', () => {
      const val = mobileInput.value.replace(/\D/g, '').slice(0, 10);
      mobileInput.value = val;
      const valid = /^[6-9]\d{9}$/.test(val);
      mobileInput.style.borderColor = val.length === 10
        ? (valid ? 'rgba(34,197,94,0.6)' : 'rgba(239,68,68,0.6)')
        : '';
    });
  }

  // ── Email @gmail.com validation ──────────────────────────────
  // Preserved from RealEstate: flag = emailID.endsWith("@gmail.com")
  document.querySelectorAll('input[type="email"]').forEach(el => {
    el.addEventListener('blur', () => {
      if (el.value && !el.value.endsWith('@gmail.com')) {
        el.style.borderColor = 'rgba(239,68,68,0.6)';
        let hint = el.parentElement.querySelector('.gmail-hint');
        if (!hint) {
          hint = document.createElement('div');
          hint.className = 'form-hint gmail-hint';
          hint.style.color = 'rgba(239,68,68,0.8)';
          hint.textContent = 'Email must end with @gmail.com';
          el.parentElement.appendChild(hint);
        }
      } else {
        el.style.borderColor = '';
        const hint = el.parentElement.querySelector('.gmail-hint');
        if (hint) hint.remove();
      }
    });
  });

  // ── OTP input auto-format ────────────────────────────────────
  const otpInput = document.querySelector('.otp-input');
  if (otpInput) {
    otpInput.addEventListener('input', () => {
      otpInput.value = otpInput.value.replace(/\D/g, '').slice(0, 6);
    });
    // Auto-submit on 6 digits
    otpInput.addEventListener('input', () => {
      if (otpInput.value.length === 6) {
        const form = otpInput.closest('form');
        if (form) {
          setTimeout(() => form.submit(), 300);
        }
      }
    });
  }

  // ── Price formatter for display ──────────────────────────────
  window.formatINR = function (amount) {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency', currency: 'INR', maximumFractionDigits: 0
    }).format(amount);
  };

  // ── Brokerage calculator (1.5%) ──────────────────────────────
  // Preserved from Seller.seller_Residential(): totalPrice = rPrice * 0.015
  const priceInputs = document.querySelectorAll('input[name="price"], input[id="priceInput"]');
  priceInputs.forEach(inp => {
    inp.addEventListener('input', () => {
      const price = parseFloat(inp.value) || 0;
      const brokerage = (price * 0.015).toFixed(2);
      const brokerageInput = document.getElementById('brokerageInput');
      const brokerageHint = document.getElementById('brokerageHint');
      if (brokerageInput) brokerageInput.value = brokerage;
      if (brokerageHint && price > 0) {
        brokerageHint.textContent = '= ₹' + parseFloat(brokerage).toLocaleString('en-IN');
      }
    });
  });

  // ── EMI Calculator ───────────────────────────────────────────
  // Preserved from Loan.createLoan():
  //   monthlyRate = (interestRate / 100) / 12
  //   emiAmount = (totalAmount * monthlyRate * (1+monthlyRate)^n) / ((1+monthlyRate)^n - 1)
  window.calculateEMI = function (principal, annualRate, tenureMonths) {
    const totalAmount = principal * 1.015; // 1.5% brokerage
    const monthlyRate = (annualRate / 100) / 12;
    if (monthlyRate === 0) return totalAmount / tenureMonths;
    return (totalAmount * monthlyRate * Math.pow(1 + monthlyRate, tenureMonths))
      / (Math.pow(1 + monthlyRate, tenureMonths) - 1);
  };

  // ── Seller property type switcher ────────────────────────────
  window.updateTypes = function () {
    const catEl = document.getElementById('catSelect');
    const typeEl = document.getElementById('typeSelect');
    if (!catEl || !typeEl) return;

    // From Buyer.java menu:
    const types = {
      Residential: ['flat', 'bungalow', 'tenement', 'villa', 'raw-house'],
      Commercial:  ['Office', 'Shop', 'mall', 'Showroom'],
      Industrial:  ['warehouse', 'factory', 'manufacturing', 'workshop']
    };

    const selected = catEl.value;
    const list = types[selected] || types.Residential;
    typeEl.innerHTML = list.map(t =>
      `<option value="${t}">${t.charAt(0).toUpperCase() + t.slice(1)}</option>`
    ).join('');
  };

  // ── Confirm delete ───────────────────────────────────────────
  document.querySelectorAll('[data-confirm]').forEach(el => {
    el.addEventListener('click', (e) => {
      if (!confirm(el.dataset.confirm)) {
        e.preventDefault();
        e.stopPropagation();
      }
    });
  });

  // ── Property search form — clear empty fields before submit ──
  document.querySelectorAll('form[action*="/properties/list"]').forEach(form => {
    form.addEventListener('submit', () => {
      form.querySelectorAll('select, input').forEach(field => {
        if (!field.value) field.disabled = true;
      });
    });
  });

  // ── Table search filter ──────────────────────────────────────
  const tableSearch = document.getElementById('tableSearch');
  if (tableSearch) {
    tableSearch.addEventListener('input', () => {
      const q = tableSearch.value.toLowerCase();
      document.querySelectorAll('.eh-table tbody tr').forEach(row => {
        row.style.display = row.textContent.toLowerCase().includes(q) ? '' : 'none';
      });
    });
  }

  // ── Sidebar active link ──────────────────────────────────────
  const path = window.location.pathname;
  document.querySelectorAll('.sidebar-link').forEach(link => {
    if (link.getAttribute('href') && path.startsWith(link.getAttribute('href')) && link.getAttribute('href') !== '/') {
      link.classList.add('active');
    }
  });

  // ── Number input: prevent non-numeric ────────────────────────
  document.querySelectorAll('input[type="number"]').forEach(input => {
    input.addEventListener('wheel', (e) => e.preventDefault());
  });

  console.log('EstateHub JS loaded ✓');
})();
