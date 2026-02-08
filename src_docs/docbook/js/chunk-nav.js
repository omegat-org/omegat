/* DocBook xslTNG version 2.5.0
 *
 * This is chunk-nav.js providing support for keyboard
 * navigation between chunks.
 *
 * See https://xsltng.docbook.org/
 *
 * The stylesheets store next/prev/up/home links in the HTML head
 * using link/@rel elements. If chunk-nav.js is loaded, N/→, P/←, U,
 * and H/Home navigate to the next, previous, "up", and home pages.
 */

(function() {
  const KEY_N = 78;
  const KEY_RIGHT = 39;

  const KEY_P = 80;
  const KEY_LEFT = 37;

  const KEY_U = 85;
  const KEY_UP = 38;

  const KEY_H = 72;
  const KEY_HOME = 36;

  const KEY_SPACE = 32;

  const KEY_SHIFT = 16;

  const body = document.querySelector("body");

  const nav = function(event) {
    event = event || window.event;
    let keyCode = event.keyCode || event.which;

    if (event.srcElement && event.srcElement.classList.contains("ptoc-search")) {
      // Don't navigate if the user is typing in the persistent toc search box
      return true;
    }

    switch (keyCode) {
    case KEY_N:
    case KEY_RIGHT:
    case KEY_SPACE:
      nav_to(event, "next");
      break;
    case KEY_P:
    case KEY_LEFT:
      nav_to(event, "prev");
      break;
    case KEY_U:
        nav_to(event, "up");
      break;
    case KEY_H:
    case KEY_HOME:
      nav_to(event, "home");
      break;
    default:
      break;
    }

    return false;
  };

  const nav_to = function(event, rel) {
    event.preventDefault();
    let link = document.querySelector(`head link[rel='${rel}']`);
    if (link && link.hasAttribute("href")) {
      window.location.href = link.getAttribute("href");
    }
  };

  window.onkeyup = nav;
})();
