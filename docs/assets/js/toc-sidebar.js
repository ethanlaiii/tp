// Copies the page's {:toc} list into a sticky sidebar and highlights the section being read.
(function () {
  var toc = document.getElementById('markdown-toc');
  var sidebar = document.querySelector('.toc-sidebar');
  if (!toc || !sidebar) {
    return;
  }

  var title = document.createElement('p');
  title.className = 'toc-sidebar-title';
  title.textContent = 'On this page';
  var list = toc.cloneNode(true);
  list.removeAttribute('id');
  sidebar.appendChild(title);
  sidebar.appendChild(list);
  sidebar.hidden = false;
  document.body.classList.add('has-toc-sidebar');

  var links = Array.prototype.slice.call(sidebar.querySelectorAll('a[href^="#"]'));
  var headings = links.map(function (link) {
    return document.getElementById(decodeURIComponent(link.getAttribute('href').slice(1)));
  });
  var activeLink = null;

  function highlightCurrentSection() {
    var current = null;
    for (var i = 0; i < headings.length; i++) {
      if (headings[i] && headings[i].getBoundingClientRect().top <= 100) {
        current = links[i];
      }
    }
    if (current === activeLink) {
      return;
    }
    if (activeLink) {
      activeLink.classList.remove('active');
    }
    activeLink = current;
    if (activeLink) {
      activeLink.classList.add('active');
      var top = activeLink.offsetTop - sidebar.clientHeight / 2;
      sidebar.scrollTop = Math.max(0, top);
    }
  }

  var isScheduled = false;
  window.addEventListener('scroll', function () {
    if (!isScheduled) {
      isScheduled = true;
      window.requestAnimationFrame(function () {
        isScheduled = false;
        highlightCurrentSection();
      });
    }
  }, { passive: true });
  highlightCurrentSection();
})();
